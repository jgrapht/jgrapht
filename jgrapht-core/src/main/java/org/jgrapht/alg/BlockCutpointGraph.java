/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Definition of a <a href="http://mathworld.wolfram.com/Block.html">block of a graph</a> in
 * MathWorld.<br>
 * Definition and lemma taken from the article
 * <a href="http://www.albany.edu/~goel/publications/rosencrantz2005.pdf"> Structure-Based
 * Resilience Metrics for Service-Oriented Networks</a>:
 *
 * <ul>
 * <li><b>Definition 4.5</b> Let $G(V; E)$ be a connected undirected graph. The block-cut point graph
 * ($BC$ graph) of $G$, denoted by $GB(VB; EB)$, is the bipartite graph defined as follows. (a) $VB$ has one
 * node corresponding to each block and one node corresponding to each cut point of $G$. (b) Each edge
 * $fx$; $yg$ in $EB$ joins a block node $x$ to a cut point $y$ if the block corresponding to $x$ contains the
 * cut point node corresponding to $y$.</li>
 * <li><b>Lemma 4.4</b> Let $G(V; E)$ be a connected undirected graph. (a) Each pair of blocks of $G$
 * share at most one node, and that node is a cutpoint. (b) The $BC$ graph of $G$ is a tree in which
 * each leaf node corresponds to a block of $G$.</li>
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @since July 5, 2007
 * @deprecated Moved to package org.jgrapht.connectivity
 */
@Deprecated
public class BlockCutpointGraph<V, E>
    extends SimpleGraph<Graph<V, E>, DefaultEdge>
{
    private static final long serialVersionUID = -9101341117013163934L;

    private Set<V> cutpoints = new HashSet<>();

    /**
     * DFS (Depth-First-Search) tree.
     */
    private Graph<V, DefaultEdge> dfsTree;

    private Graph<V, E> graph;

    private int numOrder;

    private Deque<BCGEdge> stack = new ArrayDeque<>();

    private Map<V, Set<Graph<V, E>>> vertex2biconnectedSubgraphs = new HashMap<>();

    private Map<V, Graph<V, E>> vertex2block = new HashMap<>();

    private Map<V, Integer> vertex2numOrder = new HashMap<>();

    /**
     * Running time = $O(m)$ where m is the number of edges.
     * 
     * @param graph the input graph
     */
    public BlockCutpointGraph(Graph<V, E> graph)
    {
        super(DefaultEdge.class);
        this.graph = GraphTests.requireUndirected(graph, "Graph must be undirected");

        this.dfsTree = new SimpleDirectedGraph<>(DefaultEdge.class);
        V s = graph.vertexSet().iterator().next();
        this.dfsTree.addVertex(s);
        dfsVisit(s, s);

        if (this.dfsTree.edgesOf(s).size() > 1) {
            this.cutpoints.add(s);
        } else {
            this.cutpoints.remove(s);
        }

        for (V cutpoint : this.cutpoints) {
            Graph<V, E> subgraph = new SimpleGraph<>(this.graph.getEdgeFactory());
            subgraph.addVertex(cutpoint);
            this.vertex2block.put(cutpoint, subgraph);
            addVertex(subgraph);
            Set<Graph<V, E>> biconnectedSubgraphs = getBiconnectedSubgraphs(cutpoint);
            for (Graph<V, E> biconnectedSubgraph : biconnectedSubgraphs) {
                assert (vertexSet().contains(biconnectedSubgraph));
                addEdge(subgraph, biconnectedSubgraph);
            }
        }
    }

    /**
     * Returns the vertex if vertex is a cutpoint, and otherwise returns the block (biconnected
     * component) containing the vertex.
     *
     * @param vertex vertex in the initial graph.
     * @return the biconnected component containing the vertex
     */
    public Graph<V, E> getBlock(V vertex)
    {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }

        return this.vertex2block.get(vertex);
    }

    /**
     * Returns the cutpoints of the initial graph.
     * 
     * @return the cutpoints of the initial graph
     */
    public Set<V> getCutpoints()
    {
        return this.cutpoints;
    }

    /**
     * Returns <code>true</code> if the vertex is a cutpoint, <code>false</code> otherwise.
     *
     * @param vertex vertex in the initial graph.
     * @return <code>true</code> if the vertex is a cutpoint, <code>false</code> otherwise.
     */
    public boolean isCutpoint(V vertex)
    {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }

        return this.cutpoints.contains(vertex);
    }

    private void biconnectedComponentFinished(V s, V n)
    {
        this.cutpoints.add(s);

        Set<V> vertexComponent = new HashSet<>();
        Set<BCGEdge> edgeComponent = new HashSet<>();
        BCGEdge edge = this.stack.removeLast();
        while ((getNumOrder(edge.getSource()) >= getNumOrder(n)) && !this.stack.isEmpty()) {
            edgeComponent.add(edge);

            vertexComponent.add(edge.getSource());
            vertexComponent.add(edge.getTarget());

            edge = this.stack.removeLast();
        }
        edgeComponent.add(edge);
        // edgeComponent is an equivalence class.

        vertexComponent.add(edge.getSource());
        vertexComponent.add(edge.getTarget());

        Graph<V, E> biconnectedSubgraph =
            new MaskSubgraph<>(this.graph, v -> !vertexComponent.contains(v), e -> false);
        for (V vertex : vertexComponent) {
            this.vertex2block.put(vertex, biconnectedSubgraph);
            getBiconnectedSubgraphs(vertex).add(biconnectedSubgraph);
        }
        addVertex(biconnectedSubgraph);
    }

    private int dfsVisit(V s, V father)
    {
        this.numOrder++;
        int minS = this.numOrder;
        setNumOrder(s, this.numOrder);

        for (E edge : this.graph.edgesOf(s)) {
            V n = Graphs.getOppositeVertex(this.graph, edge, s);
            if (getNumOrder(n) == 0) {
                this.dfsTree.addVertex(n);
                BCGEdge dfsEdge = new BCGEdge(s, n);
                this.dfsTree.addEdge(s, n, dfsEdge);

                this.stack.add(dfsEdge);

                // minimum of the traverse orders of the "attach points" of
                // the vertex n.
                int minN = dfsVisit(n, s);
                minS = Math.min(minN, minS);
                if (minN >= getNumOrder(s)) {
                    // s is a cutpoint.
                    // it has a son whose "attach depth" is greater or equal.
                    biconnectedComponentFinished(s, n);
                }
            } else if ((getNumOrder(n) < getNumOrder(s)) && !n.equals(father)) {
                BCGEdge backwardEdge = new BCGEdge(s, n);
                this.stack.add(backwardEdge);

                // n is an "attach point" of s. {s->n} is a backward edge.
                minS = Math.min(getNumOrder(n), minS);
            }
        }

        // minimum of the traverse orders of the "attach points" of
        // the vertex s.
        return minS;
    }

    /**
     * Returns the biconnected components containing the vertex. A vertex which is not a cutpoint is
     * contained in exactly one component. A cutpoint is contained is at least $2$ components.
     *
     * @param vertex vertex in the initial graph.
     */
    private Set<Graph<V, E>> getBiconnectedSubgraphs(V vertex)
    {
        return this.vertex2biconnectedSubgraphs.computeIfAbsent(vertex, k -> new HashSet<>());
    }

    /**
     * Returns the traverse order of the vertex in the DFS.
     */
    private int getNumOrder(V vertex)
    {
        assert (vertex != null);
        return this.vertex2numOrder.getOrDefault(vertex, 0);
    }

    private void setNumOrder(V vertex, int numOrder)
    {
        this.vertex2numOrder.put(vertex, numOrder);
    }

    private class BCGEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = -5115006161815760059L;

        private V source;

        private V target;

        public BCGEdge(V source, V target)
        {
            super();
            this.source = source;
            this.target = target;
        }

        @Override
        public V getSource()
        {
            return this.source;
        }

        @Override
        public V getTarget()
        {
            return this.target;
        }
    }
}

// End BlockCutpointGraph.java
