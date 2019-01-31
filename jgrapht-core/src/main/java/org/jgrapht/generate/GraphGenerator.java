/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * An interface for generating new graph structures.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <T> type for returning implementation-specific mappings from String roles to graph
 *        elements
 *
 * @author John V. Sichi
 */
public interface GraphGenerator<V, E, T>
{

    /**
     * Generate a graph structure. The topology of the generated graph is dependent on the
     * implementation. For graphs in which not all vertices share the same automorphism equivalence
     * class, the generator may produce a labeling indicating the roles played by generated
     * elements. This is the purpose of the resultMap parameter. For example, a generator for a
     * wheel graph would designate a hub vertex. Role names used as keys in resultMap should be
     * declared as public static final Strings by implementation classes.
     *
     * @param target receives the generated edges and vertices; if this is non-empty on entry, the
     *        result will be a disconnected graph since generated elements will not be connected to
     *        existing elements
     * @param resultMap if non-null, receives implementation-specific mappings from String roles to
     *        graph elements (or collections of graph elements)
     * 
     * @throws UnsupportedOperationException if the graph does not have appropriate vertex and edge
     *         suppliers, in order to be able to create new vertices and edges. Methods
     *         {@link Graph#getEdgeSupplier()} and {@link Graph#getVertexSupplier()} must not return
     *         <code>null</code>.
     */
    void generateGraph(Graph<V, E> target, Map<String, T> resultMap);

    /**
     * Generate a graph structure.
     *
     * @param target receives the generated edges and vertices; if this is non-empty on entry, the
     *        result will be a disconnected graph since generated elements will not be connected to
     *        existing elements
     * @throws UnsupportedOperationException if the graph does not have appropriate vertex and edge
     *         suppliers, in order to be able to create new vertices and edges
     */
    default void generateGraph(Graph<V, E> target)
    {
        generateGraph(target, null);
    }

}
