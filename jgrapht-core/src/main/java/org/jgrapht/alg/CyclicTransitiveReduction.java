/*
 * (C) Copyright 2018-2020, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.alg.tour.DirectedHamiltonianCycle;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements a <a href="https://en.wikipedia.org/wiki/Transitive_reduction">transitive reduction</a> algorithm which
 * also works correctly for directed graphs containing cycles because the simple {@link TransitiveReduction} algorithm
 * fails in this case, falsely deleting all edges from a cyclical graph.
 * <p></p>
 * This algorithm utilises existing JGraphT functionality such as:
 * <ul>
 *   <li>transitive reduction for DAGs (directed acyclical graphs)</li>
 *   <li>cycle detection</li>
 *   <li>strong connectivity inspector incl. graph condensation</li>
 * </ul>
 * In addition to that the algorithm performs edge pruning both within and between SSCs (strongly connected
 * components) as well as condensed graph expansion (basically the inverse operation) back into the original graph.
 * <p></p>
 * The result will be a minimal, transitively reduced graph, only containing edges which were also present in the
 * original graph. This comes at a cost of performance because it requires additional checks which could be omitted if
 * we would relax the requirement to just return a minimal graph with identical reachability. Then for example it would
 * be permitted
 * <ul>
 *   <li>
 *     to replace any SCC by a simple cycle connecting all vertices, potentially containing synthetical edges. Instead,
 *     this algorithm guarantees to find an existing Hamiltonian cycle within each SCC and reduces the SCC to it.
 *   </li>
 *   <li>
 *     to replace redundant edges between SCCs by a single, potentially synthetical edge between any two source and
 *     target vertices. Instead, this algorithm guarantees that it retains an one existing link between SCCs, removing
 *     all other edges as redundant.
 *   </li>
 * </ul>
 * <p></p>
 * <b>Implementation note:</b> This algorithm was compared with an improved version of an existing algorithm found in a
 * C library from the <a href="https://www.win.tue.nl/emcmc/cutter/">CUTTER project</a> related to the paper
 * <a href="https://bmcbioinformatics.biomedcentral.com/track/pdf/10.1186/1471-2105-13-281.pdf">Efficient reconstruction
 * of biological networks via transitive reduction on general purpose graphics processors</a> and ported to Java,
 * operating directly on int or boolean arrays. The performance was comparable only for small cyclical digraphs, but the
 * (non-parellel) CUTTER algorithm, when compared to this one, performs worse and worse for bigger networks with more
 * vertices and more SCCs. Furthermore, for DAGs CUTTER loses even more dramatically because it uses the Warshall
 * transitive closure algorithm which is very slow for large DAGs. So there was no apparent advantage in keeping the
 * array-based algorithm despite my initial hope that it might be faster than this algorithm. Hence, I decided to keep
 * and improve this one instead.
 *
 * @param <V> vertex type
 * @param <E> edge type
 * @author Alexander Kriegisch
 */
public class CyclicTransitiveReduction<V, E> {
  protected final Graph<V, E> directedGraph;
  protected boolean allowSyntheticEdges = false;

  /**
   * Create an instance of a transitive reduction algorithm capable of handling cyclic graphs.
   *
   * @param directedGraph the directed graph that will be reduced transitively. The input graph must not allow
   *                      self-loops, must be unweighted and must not allow multiple edges between vertices.
   */
  public CyclicTransitiveReduction(Graph<V, E> directedGraph) {
    GraphTests.requireDirected(directedGraph, "Graph must be directed");
    if (directedGraph.getType().isAllowingSelfLoops())
      throw new IllegalArgumentException("Graph must not allow self-loops");
    if (directedGraph.getType().isWeighted())
      throw new IllegalArgumentException("Graph must be unweighted");
    if (directedGraph.getType().isAllowingMultipleEdges())
      throw new IllegalArgumentException("Graph must not allow multiple edges");
    this.directedGraph = directedGraph;
  }

  /**
   * Determine if the transitive reduction algorithm is meant to speed up processing by allowing two types of
   * "synthetic" edges:
   * <ul>
   *   <li>
   *     Replace each SCC (strongly connected component) by a simple circle connecting all vertices in arbitrary order
   *     and direction, without regard of original vertices. This is much faster than finding an actual Hamiltonian
   *     cycle within each SCC and does not affect the SCC's or total graph's reachability in any way, making it a legal
   *     reduction. The downside is that the reduction can result in "synthetic" edges which originally did not exist.
   *   </li>
   *   <li>
   *     Replace all existing connections between two SCCs by a single edge in the right direction, but without regard
   *     of original vertices. This is much faster than finding and retaining an existing edge by checking all
   *     connections between vertex sets of two SCCs and does not affect the graph's reachability in any way, making it
   *     a legal reduction. The downside is that the reduction can result in "synthetic" edges which originally did not
   *     exist.
   *   </li>
   * </ul>
   * If synthetic edges are allowed, {@link #reduce()} shall be very fast (linear time according to the number of SCC
   * vertices). Otherwise it has to find a Hamiltonian cycle (HC) within each SCC, potentially making slower by growing
   * orders of magnitude for bigger SCCs because HC runtime grows exponentially with the number of vertices in the SCC.
   *
   * @param allow Allow synthetic edges? The default value when not calling this method is {@code false}.
   * @return this object instance
   */
  public CyclicTransitiveReduction<V, E> allowSyntheticEdges(boolean allow) {
    allowSyntheticEdges = allow;
    return this;
  }

  /**
   * Removes all transitive edges from the directed, possibly cyclic graph passed as input parameter, e.g.
   * <pre>
   * Graph&lt;String, DefaultEdge&gt; graph = GraphTypeBuilder
   *   .&lt;String, DefaultEdge&gt;directed()
   *   .allowingMultipleEdges(false)
   *   .allowingSelfLoops(false)
   *   .edgeClass(DefaultEdge.class)
   *   .buildGraph();
   * new CyclicTransitiveReduction&lt;String, DefaultEdge&gt;(graph).reduce();
   * </pre>
   * The resulting graph will be minimal and only use originally existing edges, no synthetical ones. I.e. that each SCC
   * (strongly connected component) will be reduced to a Hamiltonian cycle and there will be only a single edge
   * connecting SCCs. For more details, please read the class description.
   */
  public synchronized void reduce() {
    // Simple transitive reduction algorithm is a bit faster for acyclic digraphs (but faulty for cyclic ones!)
    if (!new CycleDetector<>(directedGraph).detectCycles()) {
      TransitiveReduction.INSTANCE.reduce(directedGraph, false);
      return;
    }
    Graph<Graph<V, E>, DefaultEdge> condensedGraph = condenseGraph(directedGraph);
    pruneCondensedGraph(condensedGraph);
    expandCondensedGraph(condensedGraph, directedGraph);
  }

  /**
   * Condense a directed, possibly cyclical graph by {@link StrongConnectivityAlgorithm#getCondensation()}.
   * @param directedGraph original graph to be condensed
   * @return condensed graph with each strongly connected component (SCC) being a subgraph
   */
  protected Graph<Graph<V, E>, DefaultEdge> condenseGraph(Graph<V, E> directedGraph) {
    return new KosarajuStrongConnectivityInspector<>(directedGraph).getCondensation();
  }

  /**
   * Transitively reduce the given condensed graph derived from an original directed graph by
   * {@link #condenseGraph(Graph)} and replace each strongly connected component (SCC) by a simple circle connecting all
   * its respective vertices.
   * <p></p>
   * If synthetic edges are allowed, this method is very fast (linear time according to the number of SCC vertices).
   * Otherwise it has to find a Hamiltonian cycle (HC) within each SCC, potentially making slower by growing orders of
   * magnitude for bigger SCCs because HC runtime grows exponentially with the number of vertices in the SCC.
   *
   * @param condensedGraph condensed graph to be pruned
   */
  protected void pruneCondensedGraph(Graph<Graph<V, E>, DefaultEdge> condensedGraph) {
    TransitiveReduction.INSTANCE.reduce(condensedGraph, false);

    for (Graph<V, E> scComponent : condensedGraph.vertexSet()) {
      Set<E> sccEdges = scComponent.edgeSet();
      if (sccEdges.size() < 3)
        continue;
      Set<E> sccEdgesCopy = new HashSet<>(sccEdges);
      List<V> cycle;
      if (allowSyntheticEdges) {
        cycle = new ArrayList<>(scComponent.vertexSet());
        scComponent.removeAllEdges(sccEdgesCopy);
        for (int i = 0; i < cycle.size(); i++) {
          V sourceVertex = cycle.get(i);
          V targetVertex = cycle.get((i + 1) % cycle.size());
          // Add edge to original graph first, otherwise there will be an exception when trying to add it to a subgraph
          // of the condensation
          directedGraph.addEdge(sourceVertex, targetVertex);
          scComponent.addEdge(sourceVertex, targetVertex);
        }
      }
      else {
        GraphPath<V, E> graphPath = new DirectedHamiltonianCycle<V, E>().getTour(scComponent);
        assert graphPath != null;
        cycle = graphPath.getVertexList();
        // First and last vertices are identical -> remove one of them
        cycle.remove(0);

        for (E edge : sccEdgesCopy) {
          int sourceIndex = cycle.indexOf(scComponent.getEdgeSource(edge));
          assert sourceIndex >= 0;
          int targetIndex = cycle.indexOf(scComponent.getEdgeTarget(edge));
          assert targetIndex >= 0;
          int indexDelta = Math.abs(sourceIndex - targetIndex);
          if (indexDelta != 1 && indexDelta + 1 != cycle.size())
            scComponent.removeEdge(edge);
        }
      }
    }
  }

  /**
   * Re-expand a condensed graph previously created by {@link #condenseGraph(Graph)} after possible clean-ups by
   * {@link #pruneCondensedGraph(Graph)}) back into the original directed graph, also cleaning up  redundant edges
   * between strongly connected components during the process.
   *
   * @param condensedGraph condensed graph to be re-expanded
   * @param directedGraph target graph to be expanded into
   */
  protected void expandCondensedGraph(Graph<Graph<V, E>, DefaultEdge> condensedGraph, Graph<V, E> directedGraph) {
    // Per condensed graph edge, find one existing edge in the original graph to represent
    // the corresponding connection between two SCCs
    Set<E> sccLinks = new HashSet<>();
    condensedGraph.edgeSet()
      .forEach(edge -> {
        Set<V> sourceVertices = condensedGraph.getEdgeSource(edge).vertexSet();
        Set<V> targetVertices = condensedGraph.getEdgeTarget(edge).vertexSet();
        E sccLink = allowSyntheticEdges
          ? createSyntheticSCCLink(sourceVertices, targetVertices)
          : findSCCLink(sourceVertices, targetVertices);
        assert sccLink != null;
        sccLinks.add(sccLink);
      });

    // Remove all edges from the original graph except for the SCC links
    new HashSet<E>(directedGraph.edgeSet()).stream()
      .filter(edge -> !sccLinks.contains(edge))
      .forEach(directedGraph::removeEdge);

    // Re-add all edges within SCCs
    condensedGraph.vertexSet().forEach(scComponent ->
      scComponent.edgeSet().forEach(edge ->
        directedGraph.addEdge(scComponent.getEdgeSource(edge), scComponent.getEdgeTarget(edge), edge)
      )
    );
  }

  /**
   * Find an existing edge in a directed graph, linking two SCCs (strongly connected components) within that graph
   *
   * @param sourceVertices source SCC vertex set
   * @param targetVertices target SCC vertex set
   * @return the first edge found which links any source vertex to any target vertex
   */
  private E findSCCLink(Set<V> sourceVertices, Set<V> targetVertices) {
    for (V sourceVertex : sourceVertices) {
      for (V targetVertex : targetVertices) {
        if (directedGraph.containsEdge(sourceVertex, targetVertex))
          return directedGraph.getEdge(sourceVertex, targetVertex);
      }
    }
    return null;
  }

  /**
   * Create a synthetic edge in a directed graph, linking two SCCs (strongly connected components) within that graph
   *
   * @param sourceVertices source SCC vertex set
   * @param targetVertices target SCC vertex set
   * @return the synthetic edge created (or an identical, already existing one) which links any source vertex to any
   * target vertex
   */
  private E createSyntheticSCCLink(Set<V> sourceVertices, Set<V> targetVertices) {
    V sourceVertex = sourceVertices.iterator().next();
    V targetVertex = targetVertices.iterator().next();
    directedGraph.addEdge(sourceVertex, targetVertex);
    return directedGraph.getEdge(sourceVertex, targetVertex);
  }

}
