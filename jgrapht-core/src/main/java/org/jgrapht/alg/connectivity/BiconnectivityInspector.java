/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Allows obtaining various connectivity aspects of a graph. The <i>inspected graph</i> is specified
 * at construction time and cannot be modified. No restrictions are imposed on the input graph.
 * Multigraphs and pseudographs are also supported. The inspector traverses connected components
 * (undirected graphs) or weakly connected components (directed graphs). To find strongly connected
 * components, use {@link KosarajuStrongConnectivityInspector} instead. This class offers an
 * alternative implementation of some of the functionality encountered in
 * {@link ConnectivityInspector}. It is likely to perform somewhat slower than
 * {@link ConnectivityInspector}, but offers more functionality in return.
 * <p>
 * The algorithm implemented in this class is Hopcroft and Tarjan's biconnected components
 * algorithm, described in: Hopcroft, J. Tarjan, R. Algorithm 447: efficient algorithms for graph
 * manipulation, 1973. Communications of the ACM. 16 (6): 372–378. This implementation runs in
 * linear time $O(|V|+|E|)$ and is based on a recursive depth-first search. More information about
 * this subject be be found in this wikipedia
 * <a href="https://en.wikipedia.org/wiki/Biconnected_component">article</a>.
 * 
 * <p>
 * The inspector methods work in a lazy fashion: no computations are performed unless immediately
 * necessary. Computation are done once and results are cached within this class for future need.
 * The core of this class is built around a recursive Depth-first search.
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class BiconnectivityInspector<V, E>
{
    /**
     * Constructs a new BiconnectivityInspector
     * 
     * @param graph the input graph
     */
    public BiconnectivityInspector(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
        if (graph.getType().isDirected())
            this.graph = new AsUndirectedGraph<>(graph);
    }

    private Graph<V, E> graph;

    private Set<Graph<V, E>> blocks;

    private Set<V> cutpoints;

    private Set<E> bridges;

    /* Set which holds the vertices in the connected component which is being processed */
    private Set<V> connectedSet;

    /* Set which holds all connected components, expressed in vertex sets */
    private Set<Set<V>> connectedSets;

    /* Set of connected components */
    private Set<Graph<V, E>> connectedComponents;

    /* Mapping of vertices to the blocks they are contained in */
    private Map<V, Set<Graph<V, E>>> vertex2blocks;

    /* Mapping of vertices to the connected components they are contained in */
    private Map<V, Graph<V, E>> vertex2components;

    /* Discovery time of a vertex. */
    private int time;

    /* Stack which keeps track of edges in biconnected components */
    private Deque<E> stack;

    /* Map which tracks when a vertex is discovered in the DFS search */
    private Map<V, Integer> discTime = new HashMap<>();

    /**
     * Returns the <a href="http://mathworld.wolfram.com/ArticulationVertex.html">cutpoints</a>
     * (articulation points) of the graph. A vertex is a cutpoint if removal of that vertex (and all
     * edges incident to that vertex) would increase the number of (weakly) connected components in
     * the graph.
     *
     * @return the cutpoints of the graph
     */
    public Set<V> getCutpoints()
    {
        performLazyInspection();
        return this.cutpoints;
    }

    /**
     * Returns the graph's bridges. An edge is a
     * <a href="http://mathworld.wolfram.com/GraphBridge.html">bridge</a> if removal of that edge
     * would increase the number of (weakly) connected components in the graph. Note that this
     * definition remains applicable in case of multigraphs or pseudographs.
     *
     * @return the graph's bridges
     */
    public Set<E> getBridges()
    {
        performLazyInspection();
        return this.bridges;
    }

    /**
     * Returns a set of <a href="http://mathworld.wolfram.com/Block.html">blocks</a> (biconnected
     * components) containing the specified vertex. A block is a maximal biconnected subgraph. Each
     * non-cutpoint resides in at most one block. Each cutpoint resides in at least two blocks.
     *
     * @param vertex vertex in the initial graph.
     * @return the blocks containing the given vertex
     */
    public Set<Graph<V, E>> getBlocks(V vertex)
    {
        assert graph.containsVertex(vertex);

        if (vertex2blocks == null) {
            vertex2blocks = new HashMap<>();
            for (V v : graph.vertexSet())
                vertex2blocks.put(v, new LinkedHashSet<>());

            for (Graph<V, E> block : this.getBlocks()) {
                for (V v : block.vertexSet())
                    vertex2blocks.get(v).add(block);
            }
        }
        return this.vertex2blocks.get(vertex);
    }

    /**
     * Returns all <a href="http://mathworld.wolfram.com/Block.html">blocks</a> (biconnected
     * components) in the graph. A block is a maximal biconnected subgraph.
     * 
     * @return all blocks (biconnected components) in the graph.
     */
    public Set<Graph<V, E>> getBlocks()
    {
        performLazyInspection();
        return this.blocks;
    }

    /**
     * Returns all connected components in the graph. In case the graph is directed, this method
     * returns all weakly connected components.
     * 
     * @return all connected components in the graph if the graph is undirected, or all weakly
     *         connected components if the graph is directed.
     */
    public Set<Graph<V, E>> getConnectedComponents()
    {
        if (connectedComponents == null) {
            performLazyInspection();
            connectedComponents = new LinkedHashSet<>();
            for (Set<V> vertexComponent : connectedSets)
                connectedComponents.add(new AsSubgraph<>(this.graph, vertexComponent));
        }
        return connectedComponents;
    }

    /**
     * Returns the connected component containing the given vertex. If the underlying graph is
     * directed, this method returns a weakly connected component.
     * 
     * @param vertex vertex
     * @return the connected component containing the given vertex, or a weakly connected component
     *         if the underlying graph is directed.
     */
    public Graph<V, E> getConnectedComponent(V vertex)
    {
        assert this.graph.containsVertex(vertex);
        if (vertex2components == null) {
            vertex2components = new HashMap<>();
            for (Graph<V, E> component : this.getConnectedComponents())
                for (V v : component.vertexSet())
                    vertex2components.put(v, component);
        }
        return vertex2components.get(vertex);
    }

    /**
     * Tests if the inspected graph is biconnected. A biconnected graph is a connected graph on two
     * or more vertices having no cutpoints.
     *
     * @return true if the graph is biconnected, false otherwise
     */
    public boolean isBiconnected()
    {
        performLazyInspection();
        return graph.vertexSet().size() >= 2 && blocks.size() == 1;
    }

    /**
     * Test if the inspected graph is connected. A graph is connected when, while ignoring edge
     * directionality, there exists a path between every pair of vertices. In a connected graph,
     * there are no unreachable vertices. When the inspected graph is a <i>directed</i> graph, this
     * method returns true if and only if the inspected graph is <i>weakly</i> connected. An empty
     * graph is <i>not</i> considered connected.
     *
     * @return <code>true</code> if and only if inspected graph is connected.
     */
    public boolean isConnected()
    {
        performLazyInspection();
        return connectedSets.size() == 1;
    }

    private void init()
    {
        blocks = new LinkedHashSet<>();
        cutpoints = new LinkedHashSet<>();
        bridges = new LinkedHashSet<>();
        connectedSets = new LinkedHashSet<>();
        stack = new ArrayDeque<>(graph.edgeSet().size());
        for (V v : graph.vertexSet())
            discTime.put(v, -1);
    }

    private void performLazyInspection()
    {
        if (blocks == null) {
            init();
            // Iterate over all connected components
            for (V v : graph.vertexSet()) {
                if (discTime.get(v) == -1) {
                    connectedSet = new HashSet<>();
                    dfs(v, null);

                    // Stack can be non-empty when dfs finishes, for instance if the graph has no
                    // cutpoints.
                    // Construct the final component from the remaining edges.
                    if (!stack.isEmpty())
                        buildBlock(0);
                    connectedSets.add(connectedSet);
                }
            }
            if (this.graph.getType().isAllowingMultipleEdges()) {
                // check parallel edges: an edge is not a bridge when there are multiple edges
                // between the same pair of vertices
                for (Iterator<E> it = bridges.iterator(); it.hasNext();) {
                    E edge = it.next();
                    int nrParallelEdges = graph
                        .getAllEdges(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)).size();
                    if (nrParallelEdges > 1)
                        it.remove();
                }
            }
        }
    }

    /**
     * Each time a cutpoint is discovered, this method computes the biconnected component
     * 
     * @param discTimeCutpoint discovery time of cutpoint
     */
    private void buildBlock(int discTimeCutpoint)
    {
        Set<V> vertexComponent = new HashSet<>();

        while (!stack.isEmpty()) {
            E edge = stack.peek();
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (discTime.get(source) < discTimeCutpoint || discTime.get(target) < discTimeCutpoint)
                break;
            stack.pop();
            vertexComponent.add(source);
            vertexComponent.add(target);
        }
        blocks.add(new AsSubgraph<>(this.graph, vertexComponent));
    }

    /**
     * Performs a depth-first search, starting from vertex v
     * 
     * @param v vertex
     * @param parent parent of v
     * @return lowpoint of v
     */
    private int dfs(V v, V parent)
    {
        int lowV = ++this.time;
        discTime.put(v, time);
        connectedSet.add(v);
        int children = 0;

        for (E edge : this.graph.edgesOf(v)) {
            V nv = Graphs.getOppositeVertex(this.graph, edge, v);
            if (discTime.get(nv) == -1) { // Node hasn't been discovered yet
                children++;

                this.stack.push(edge);

                int lowNV = dfs(nv, v);
                lowV = Math.min(lowNV, lowV);

                if (lowNV > discTime.get(v))
                    bridges.add(edge);

                // 1. nonroot vertex v is a cutpoint iff there is a child y of v such that
                // lowpoint(y) >= depth(v)
                // 2. root vertex v is a cutpoint if it has more than 1 child
                if ((parent != null && lowNV >= discTime.get(v))
                    || (parent == null && children > 1))
                {
                    this.cutpoints.add(v); // v is a cutpoint
                    buildBlock(discTime.get(v)); // construct biconnected component
                }
            } else if ((discTime.get(nv) < discTime.get(v)) && !nv.equals(parent)) { // found
                                                                                     // backedge
                this.stack.push(edge);
                lowV = Math.min(discTime.get(nv), lowV);
            }
        }
        return lowV;
    }

}
