/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.matching.*;
import org.jgrapht.alg.matching.blossom.v5.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.stream.*;

/**
 * This class solves the Chinese Postman Problem (CPP), also known as the Route Inspection Problem.
 * The CPP asks to find a <i>closed walk</i> of minimum length that visits every edge of the graph
 * at least once. In weighted graphs, the <i>length</i> of the closed walk is defined as the sum of
 * its edge weights; in unweighted graphs, a closed walk with the least number of edges is returned
 * (the same result can be obtained for weighted graphs with uniform edge weights).
 * <p>
 * The algorithm works with directed and undirected graphs which may contain loops and/or multiple
 * edges. The runtime complexity is O(N^3) where N is the number of vertices in the graph. Mixed
 * graphs are currently not supported, as solving the CPP for mixed graphs is NP-hard. The graph on
 * which this algorithm is invoked must be strongly connected; invoking this algorithm on a graph
 * which is not strongly connected may result in undefined behavior. In case of weighted graphs, all
 * edge weights must be positive.
 *
 * If the input graph is Eulerian (see {@link GraphTests#isEulerian(Graph)} for details) use
 * {@link HierholzerEulerianCycle} instead.
 * <p>
 * The implementation is based on the following paper:<br>
 * Edmonds, J., Johnson, E.L. Matching, Euler tours and the Chinese postman, Mathematical
 * Programming (1973) 5: 88. doi:10.1007/BF01580113<br>
 *
 * More concise descriptions of the algorithms can be found here:
 * <ul>
 * <li><a href=
 * "http://www.suffolkmaths.co.uk/pages/Maths%20Projects/Projects/Topology%20and%20Graph%20Theory/Chinese%20Postman%20Problem.pdf">CPP
 * for Undirected graphs</a>
 * <li><a href=
 * "https://www-m9.ma.tum.de/graph-algorithms/directed-chinese-postman/index_en.html">CPP for
 * Directed graphs</a>
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class ChinesePostman<V, E>
{

    /**
     * Solves the Chinese Postman Problem on the given graph. For Undirected graph, this
     * implementation uses the @{@link KolmogorovMinimumWeightPerfectMatching} matching algorithm;
     * for directed graphs, @{@link KuhnMunkresMinimalWeightBipartitePerfectMatching} is used
     * instead. The input graph must be strongly connected. Otherwise the behavior of this class is
     * undefined.
     * 
     * @param graph the input graph (must be a strongly connected graph)
     * @return Eulerian circuit of minimum weight.
     */
    public GraphPath<V, E> getCPPSolution(Graph<V, E> graph)
    {
        // Mixed graphs are currently not supported. Solving the CPP for mixed graphs is NP-Hard
        GraphTests.requireDirectedOrUndirected(graph);

        // If graph has no vertices, or no edges, instantly return.
        if (graph.vertexSet().isEmpty() || graph.edgeSet().isEmpty())
            return new HierholzerEulerianCycle<V, E>().getEulerianCycle(graph);

        assert GraphTests.isStronglyConnected(graph);

        if (graph.getType().isUndirected())
            return solveCPPUndirected(graph);
        else
            return solveCPPDirected(graph);

    }

    /**
     * Solves the CPP for undirected graphs
     *
     * @param graph input graph
     * @return CPP solution (closed walk)
     */
    private GraphPath<V, E> solveCPPUndirected(Graph<V, E> graph)
    {

        // 1. Find all odd degree vertices (there should be an even number of those)
        List<V> oddDegreeVertices =
            graph.vertexSet().stream().filter(v -> graph.degreeOf(v) % 2 == 1).collect(
                Collectors.toList());

        // 2. Compute all pairwise shortest paths for the oddDegreeVertices
        Map<Pair<V, V>, GraphPath<V, E>> shortestPaths = new HashMap<>();
        ShortestPathAlgorithm<V, E> sp = new DijkstraShortestPath<>(graph);
        for (int i = 0; i < oddDegreeVertices.size() - 1; i++) {
            V u = oddDegreeVertices.get(i);
            ShortestPathAlgorithm.SingleSourcePaths<V, E> paths = sp.getPaths(u);
            for (int j = i + 1; j < oddDegreeVertices.size(); j++) {
                V v = oddDegreeVertices.get(j);
                shortestPaths.put(new UnorderedPair<>(u, v), paths.getPath(v));
            }
        }

        // 3. Solve a matching problem. For that we create an auxiliary graph.
        Graph<V, DefaultWeightedEdge> auxGraph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(auxGraph, oddDegreeVertices);

        for (V u : oddDegreeVertices) {
            for (V v : oddDegreeVertices) {
                if (u == v)
                    continue;
                Graphs.addEdge(
                    auxGraph, u, v, shortestPaths.get(new UnorderedPair<>(u, v)).getWeight());
            }
        }
        MatchingAlgorithm.Matching<V, DefaultWeightedEdge> matching =
            new KolmogorovMinimumWeightPerfectMatching<>(auxGraph).getMatching();

        // 4. On the original graph, add shortcuts between the odd vertices. These shortcuts have
        // been
        // identified by the matching algorithm. A shortcut from u to v
        // indirectly implies duplicating all edges on the shortest path from u to v
        Graph<V, E> eulerGraph = new Pseudograph<>(
            graph.getVertexSupplier(), graph.getEdgeSupplier(), graph.getType().isWeighted());
        Graphs.addGraph(eulerGraph, graph);
        Map<E, GraphPath<V, E>> shortcutEdges = new HashMap<>();
        for (DefaultWeightedEdge e : matching.getEdges()) {
            V u = auxGraph.getEdgeSource(e);
            V v = auxGraph.getEdgeTarget(e);
            E shortcutEdge = eulerGraph.addEdge(u, v);
            shortcutEdges.put(shortcutEdge, shortestPaths.get(new UnorderedPair<>(u, v)));
        }

        EulerianCycleAlgorithm<V, E> eulerianCycleAlgorithm = new HierholzerEulerianCycle<>();
        GraphPath<V, E> pathWithShortcuts = eulerianCycleAlgorithm.getEulerianCycle(eulerGraph);
        return replaceShortcutEdges(graph, pathWithShortcuts, shortcutEdges);
    }

    /**
     * Solves the CPP for directed graphs
     * 
     * @param graph input graph
     * @return CPP solution (closed walk)
     */
    private GraphPath<V, E> solveCPPDirected(Graph<V, E> graph)
    {

        // 1. Find all imbalanced vertices
        Map<V, Integer> imbalancedVertices = new LinkedHashMap<>();
        Set<V> negImbalancedVertices = new HashSet<>();
        Set<V> postImbalancedVertices = new HashSet<>();
        for (V v : graph.vertexSet()) {
            int imbalance = graph.outDegreeOf(v) - graph.inDegreeOf(v);

            if (imbalance == 0)
                continue;
            imbalancedVertices.put(v, Math.abs(imbalance));

            if (imbalance < 0)
                negImbalancedVertices.add(v);
            else
                postImbalancedVertices.add(v);
        }

        // 2. Compute all pairwise shortest paths from the negative imbalanced vertices to the
        // positive imbalanced vertices
        Map<Pair<V, V>, GraphPath<V, E>> shortestPaths = new HashMap<>();
        ShortestPathAlgorithm<V, E> sp = new DijkstraShortestPath<>(graph);
        for (V u : negImbalancedVertices) {
            ShortestPathAlgorithm.SingleSourcePaths<V, E> paths = sp.getPaths(u);
            for (V v : postImbalancedVertices) {
                shortestPaths.put(new Pair<>(u, v), paths.getPath(v));
            }
        }

        // 3. Solve a matching problem. For that we create an auxiliary bipartite graph. Partition1
        // contains all nodes with negative imbalance,
        // Partition2 contains all nodes with positive imbalance. Each imbalanced node is duplicated
        // a number of times. The number of duplicates of a
        // node equals its imbalance.
        Graph<Integer, DefaultWeightedEdge> auxGraph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Map<Integer, V> duplicateMap = new HashMap<>();
        Set<Integer> negImbalancedPartition = new HashSet<>();
        Set<Integer> postImbalancedPartition = new HashSet<>();
        int vertex = 0;

        for (V v : negImbalancedVertices) {
            for (int i = 0; i < imbalancedVertices.get(v); i++) {
                auxGraph.addVertex(vertex);
                duplicateMap.put(vertex, v);
                negImbalancedPartition.add(vertex);
                vertex++;
            }
        }
        for (V v : postImbalancedVertices) {
            for (int i = 0; i < imbalancedVertices.get(v); i++) {
                auxGraph.addVertex(vertex);
                duplicateMap.put(vertex, v);
                postImbalancedPartition.add(vertex);
                vertex++;
            }
        }

        for (Integer i : negImbalancedPartition) {
            for (Integer j : postImbalancedPartition) {
                V u = duplicateMap.get(i);
                V v = duplicateMap.get(j);
                Graphs.addEdge(auxGraph, i, j, shortestPaths.get(new Pair<>(u, v)).getWeight());
            }
        }
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(
                auxGraph, negImbalancedPartition, postImbalancedPartition).getMatching();

        // 4. On the original graph, add shortcuts between the imbalanced vertices. These shortcuts
        // have
        // been identified by the matching algorithm. A shortcut from u to v
        // indirectly implies duplicating all edges on the shortest path from u to v

        Graph<V, E> eulerGraph = new DirectedPseudograph<>(
            graph.getVertexSupplier(), graph.getEdgeSupplier(), graph.getType().isWeighted());
        Graphs.addGraph(eulerGraph, graph);
        Map<E, GraphPath<V, E>> shortcutEdges = new HashMap<>();
        for (DefaultWeightedEdge e : matching.getEdges()) {
            int i = auxGraph.getEdgeSource(e);
            int j = auxGraph.getEdgeTarget(e);
            V u = duplicateMap.get(i);
            V v = duplicateMap.get(j);
            E shortcutEdge = eulerGraph.addEdge(u, v);
            shortcutEdges.put(shortcutEdge, shortestPaths.get(new Pair<>(u, v)));
        }

        EulerianCycleAlgorithm<V, E> eulerianCycleAlgorithm = new HierholzerEulerianCycle<>();
        GraphPath<V, E> pathWithShortcuts = eulerianCycleAlgorithm.getEulerianCycle(eulerGraph);

        return replaceShortcutEdges(graph, pathWithShortcuts, shortcutEdges);
    }

    private GraphPath<V, E> replaceShortcutEdges(
        Graph<V, E> inputGraph, GraphPath<V, E> pathWithShortcuts,
        Map<E, GraphPath<V, E>> shortcutEdges)
    {
        V startVertex = pathWithShortcuts.getStartVertex();
        V endVertex = pathWithShortcuts.getEndVertex();
        List<V> vertexList = new ArrayList<>();
        List<E> edgeList = new ArrayList<>();

        List<V> verticesInPathWithShortcuts = pathWithShortcuts.getVertexList(); // should contain
                                                                                 // at least 2
                                                                                 // vertices
        List<E> edgesInPathWithShortcuts = pathWithShortcuts.getEdgeList(); // cannot be empty
        for (int i = 0; i < verticesInPathWithShortcuts.size() - 1; i++) {
            vertexList.add(verticesInPathWithShortcuts.get(i));
            E edge = edgesInPathWithShortcuts.get(i);

            if (shortcutEdges.containsKey(edge)) { // shortcut edge
                // replace shortcut edge by its implied path
                GraphPath<V, E> shortcut = shortcutEdges.get(edge);
                if (vertexList.get(vertexList.size() - 1).equals(shortcut.getStartVertex())) { // check
                                                                                               // direction
                                                                                               // of
                                                                                               // path
                    vertexList.addAll(
                        shortcut.getVertexList().subList(1, shortcut.getVertexList().size() - 1));
                    edgeList.addAll(shortcut.getEdgeList());
                } else {
                    List<V> reverseVertices = new ArrayList<>(
                        shortcut.getVertexList().subList(1, shortcut.getVertexList().size() - 1));
                    Collections.reverse(reverseVertices);
                    List<E> reverseEdges = new ArrayList<>(shortcut.getEdgeList());
                    Collections.reverse(reverseEdges);
                    vertexList.addAll(reverseVertices);
                    edgeList.addAll(reverseEdges);
                }
            } else { // original edge
                edgeList.add(edge);
            }
        }
        vertexList.add(endVertex);
        double pathWeight = edgeList.stream().mapToDouble(inputGraph::getEdgeWeight).sum();

        return new GraphWalk<>(
            inputGraph, startVertex, endVertex, vertexList, edgeList, pathWeight);
    }
}
