/*
 * (C) Copyright 2013-2021, by Sarah Komla-Ebri and Contributors.
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
