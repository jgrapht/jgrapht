/*
 * (C) Copyright 2013-2018, by Sarah Komla-Ebri and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * A strong connectivity inspector algorithm.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Sarah Komla-Ebri
 * @since September 2013
 */
public interface StrongConnectivityAlgorithm<V, E>
{
    /**
     * Return the underlying graph.
     *
     * @return the underlying graph
     */
    Graph<V, E> getGraph();

    /**
     * Returns true if the graph is strongly connected, false otherwise.
     *
     * @return true if the graph is strongly connected, false otherwise
     */
    boolean isStronglyConnected();

    /**
     * Computes a {@link List} of {@link Set}s, where each set contains vertices which together form
     * a strongly connected component within the given graph.
     *
     * @return <code>List</code> of <code>Set</code> s containing the strongly connected components
     */
    List<Set<V>> stronglyConnectedSets();

    /**
     * Computes a list of subgraphs of the given graph. Each subgraph will represent a strongly
     * connected component and will contain all vertices of that component. The subgraph will have
     * an edge $(u,v)$ iff $u$ and $v$ are contained in the strongly connected component.
     *
     * @return a list of subgraphs representing the strongly connected components
     */
    List<Graph<V, E>> getStronglyConnectedComponents();

    /**
     * Compute the condensation of the given graph. If each strongly connected component is
     * contracted to a single vertex, the resulting graph is a directed acyclic graph, the
     * condensation of the graph.
     * 
     * @return the condensation of the given graph
     */
    Graph<Graph<V, E>, DefaultEdge> getCondensation();
}

// End StrongConnectivityAlgorithm.java
