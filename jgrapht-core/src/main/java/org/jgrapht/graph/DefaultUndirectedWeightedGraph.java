/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;

import java.util.function.*;

/**
 * The default implementation of an undirected weighted graph. A default undirected weighted graph
 * is a non-simple undirected graph in which multiple (parallel) edges between any two vertices are
 * <i>not</i> permitted, but loops are. The edges of a weighted undirected graph have weights.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see DefaultUndirectedGraph
 */
public class DefaultUndirectedWeightedGraph<V, E>
    extends
    DefaultUndirectedGraph<V, E>
{
    private static final long serialVersionUID = -1008165881690129042L;

    /**
     * Creates a new graph.
     *
     * @param edgeClass class on which to base the edge supplier
     */
    public DefaultUndirectedWeightedGraph(Class<? extends E> edgeClass)
    {
        this(null, SupplierUtil.createSupplier(edgeClass));
    }

    /**
     * Creates a new graph.
     * 
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     */
    public DefaultUndirectedWeightedGraph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier)
    {
        super(vertexSupplier, edgeSupplier, true);
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeClass class on which to base factory for edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V,
        E> GraphBuilder<V, E, ? extends DefaultUndirectedWeightedGraph<V, E>> createBuilder(
            Class<? extends E> edgeClass)
    {
        return new GraphBuilder<>(new DefaultUndirectedWeightedGraph<>(edgeClass));
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeSupplier the edge supplier
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V,
        E> GraphBuilder<V, E, ? extends DefaultUndirectedWeightedGraph<V, E>> createBuilder(
            Supplier<E> edgeSupplier)
    {
        return new GraphBuilder<>(new DefaultUndirectedWeightedGraph<>(null, edgeSupplier));
    }

}
