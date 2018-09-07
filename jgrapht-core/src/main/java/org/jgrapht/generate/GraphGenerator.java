/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
 * @since Sep 16, 2003
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

// End GraphGenerator.java
