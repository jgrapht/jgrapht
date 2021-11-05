/*
 * (C) Copyright 2007-2021, by France Telecom and Contributors.
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
 * A Block-Cutpoint graph (also known as a block-cut tree). If $G$ is a graph, the block-cutpoint
 * graph of $G$, denoted $BC(G)$ is the simple bipartite graph with bipartition $(A, B)$ where $A$
 * is the set of <a href="http://mathworld.wolfram.com/ArticulationVertex.html">cut-vertices</a>
 * (also known as articulation points) of $G$, and $B$ is the set of
 * <a href="http://mathworld.wolfram.com/Block.html">blocks</a> of $G$. $BC(G)$ contains an edge
 * $(a,b)$ for $a \in A$ and $b \in B$ if and only if block $b$ contains the cut-vertex $a$. A
 * vertex in $G$ is a cut-vertex if removal of the vertex from $G$ (and all edges incident to this
 * vertex) increases the number of connected components in the graph. A block of $G$ is a maximal
 * connected subgraph $H \subseteq G$ so that $H$ does not have a cut-vertex. Note that if $H$ is a
 * block, then either $H$ is 2-connected, or $|V(H)| \leq 2$. Each pair of blocks of $G$ share at
 * most one vertex, and that vertex is a cut-point in $G$. $BC(G)$ is a tree in which each leaf node
 * corresponds to a block of $G$.
 * <p>
 * Note: the block-cutpoint graph is not changed when the underlying graph is changed.
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author France Telecom S.A
 * @author Joris Kinable
 */
public class BlockCutpointGraph<V, E>
    extends
    SimpleGraph<Graph<V, E>, DefaultEdge>
{
    private static final long serialVersionUID = -9101341117013163934L;

    /* Input graph */
    private Graph<V, E> graph;

    /* Set of cutpoints */
    private Set<V> cutpoints;

    /* Set of blocks */
    private Set<Graph<V, E>> blocks;

    /* Mapping of a vertex to the block it belongs to. */
    private Map<V, Graph<V, E>> vertex2block = new HashMap<>();

    /**
     * Constructs a Block-Cutpoint graph
     *
     * @param graph the input graph
     */
    public BlockCutpointGraph(Graph<V, E> graph)
    {
        super(DefaultEdge.class);
        this.graph = graph;
        BiconnectivityInspector<V, E> biconnectivityInspector =
            new BiconnectivityInspector<>(graph);

        // Construct the Block-cut point graph
        cutpoints = biconnectivityInspector.getCutpoints();
        blocks = biconnectivityInspector.getBlocks();

        for (Graph<V, E> block : blocks)
            for (V v : block.vertexSet())
                vertex2block.put(v, block);
        Graphs.addAllVertices(this, blocks);

        for (V cutpoint : this.cutpoints) {
            Graph<V, E> subgraph = new AsSubgraph<>(graph, Collections.singleton(cutpoint));
            this.vertex2block.put(cutpoint, subgraph);
            this.addVertex(subgraph);

            for (Graph<V, E> block : biconnectivityInspector.getBlocks(cutpoint))
                addEdge(subgraph, block);
        }
    }

    /**
     * Returns the vertex if vertex is a cutpoint, and otherwise returns the block (biconnected
     * component) containing the vertex.
     *
     * @param vertex vertex
     * @return the biconnected component containing the vertex
     */
    public Graph<V, E> getBlock(V vertex)
    {
        assert this.graph.containsVertex(vertex);
        return this.vertex2block.get(vertex);
    }

    /**
     * Returns all blocks (biconnected components) in the graph
     * 
     * @return all blocks (biconnected components) in the graph.
     */
    public Set<Graph<V, E>> getBlocks()
    {
        return blocks;
    }

    /**
     * Returns the cutpoints of the initial graph.
     * 
     * @return the cutpoints of the initial graph
     */
    public Set<V> getCutpoints()
    {
        return cutpoints;
    }

    /**
     * Returns <code>true</code> if the vertex is a cutpoint, <code>false</code> otherwise.
     *
     * @param vertex vertex in the initial graph.
     * @return <code>true</code> if the vertex is a cutpoint, <code>false</code> otherwise.
     */
    public boolean isCutpoint(V vertex)
    {
        return cutpoints.contains(vertex);
    }

}
