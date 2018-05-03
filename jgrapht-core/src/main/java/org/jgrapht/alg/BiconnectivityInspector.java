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

import java.util.HashSet;
import java.util.Set;

/**
 * Inspects a graph for the biconnectivity property. See {@link BlockCutpointGraph} for more
 * information. A biconnected graph has only one block (i.e. no cutpoints).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @since July 5, 2007
 * @deprecated Moved to package org.jgrapht.connectivity
 */
@Deprecated
public class BiconnectivityInspector<V, E>
{
    private BlockCutpointGraph<V, E> blockCutpointGraph;

    /**
     * Running time = $O(m)$ where m is the number of edges.
     * 
     * @param graph the input graph
     */
    public BiconnectivityInspector(Graph<V, E> graph)
    {
        super();
        GraphTests.requireUndirected(graph, "Graph must be undirected");
        this.blockCutpointGraph = new BlockCutpointGraph<>(graph);
    }

    /**
     * Returns the biconnected vertex-components of the graph.
     * 
     * @return the biconnected vertec-components of the graph
     */
    public Set<Set<V>> getBiconnectedVertexComponents()
    {
        Set<Set<V>> biconnectedVertexComponents = new HashSet<>();
        for (Graph<V, E> subgraph : this.blockCutpointGraph.vertexSet()) {
            if (!subgraph.edgeSet().isEmpty()) {
                biconnectedVertexComponents.add(subgraph.vertexSet());
            }
        }

        return biconnectedVertexComponents;
    }

    /**
     * Returns the biconnected vertex-components containing the vertex. A biconnected
     * vertex-component contains all the vertices in the component. A vertex which is not a cutpoint
     * is contained in exactly one component. A cutpoint is contained is at least $2$ components.
     *
     * @param vertex the input vertex
     * @return set of all biconnected vertex-components containing the vertex.
     */
    public Set<Set<V>> getBiconnectedVertexComponents(V vertex)
    {
        Set<Set<V>> vertexComponents = new HashSet<>();
        for (Set<V> vertexComponent : getBiconnectedVertexComponents()) {
            if (vertexComponent.contains(vertex)) {
                vertexComponents.add(vertexComponent);
            }
        }
        return vertexComponents;
    }

    /**
     * Returns the cutpoints of the graph.
     * 
     * @return the cutpoints
     */
    public Set<V> getCutpoints()
    {
        return this.blockCutpointGraph.getCutpoints();
    }

    /**
     * Returns <code>true</code> if the graph is biconnected (no cutpoint), <code>false</code>
     * otherwise.
     * 
     * @return true if the graph is biconnected, false otherwise
     */
    public boolean isBiconnected()
    {
        return this.blockCutpointGraph.vertexSet().size() == 1;
    }
}

// End BiconnectivityInspector.java
