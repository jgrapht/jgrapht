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

import java.util.function.Supplier;

import org.jgrapht.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.SupplierUtil;

/**
 * The default implementation of an undirected graph. A default undirected graph is a non-simple
 * undirected graph in which multiple (parallel) edges between any two vertices are <i>not</i>
 * permitted, but loops are.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DefaultUndirectedGraph<V, E>
    extends AbstractBaseGraph<V, E>
{
    private static final long serialVersionUID = -2066644490824847621L;

    /**
     * Creates a new graph.
     *
     * @param edgeClass class on which to base the edge supplier
     */
    public DefaultUndirectedGraph(Class<? extends E> edgeClass)
    {
        this(null, SupplierUtil.createSupplier(edgeClass), false);
    }

    /**
     * Creates a new graph.
     * 
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param weighted whether the graph is weighted or not
     */
    public DefaultUndirectedGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted)
    {
        super(
            vertexSupplier, edgeSupplier,
            new DefaultGraphType.Builder()
                .undirected().allowMultipleEdges(false).allowSelfLoops(true).weighted(weighted)
                .build());
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeClass class on which to base factory for edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V, E> GraphBuilder<V, E, ? extends DefaultUndirectedGraph<V, E>> createBuilder(
        Class<? extends E> edgeClass)
    {
        return new GraphBuilder<>(new DefaultUndirectedGraph<>(edgeClass));
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeSupplier the edge supplier of the new graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V, E> GraphBuilder<V, E, ? extends DefaultUndirectedGraph<V, E>> createBuilder(
        Supplier<E> edgeSupplier)
    {
        return new GraphBuilder<>(new DefaultUndirectedGraph<>(null, edgeSupplier, false));
    }

    /**
     * Creates a new undirected graph with the specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     * @deprecated Use suppliers instead 
     */
    @Deprecated
    public DefaultUndirectedGraph(EdgeFactory<V, E> ef)
    {
        this(ef, false);
    }

    /**
     * Creates a new graph with the specified edge factory.
     *
     * @param weighted if true the graph supports edge weights
     * @param ef the edge factory of the new graph.
     * @deprecated Use suppliers instead 
     */
    @Deprecated
    public DefaultUndirectedGraph(EdgeFactory<V, E> ef, boolean weighted)
    {
        super(ef, false, false, true, weighted);
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param ef the edge factory of the new graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     * @deprecated Use suppliers instead 
     */
    @Deprecated
    public static <V, E> GraphBuilder<V, E, ? extends DefaultUndirectedGraph<V, E>> createBuilder(
        EdgeFactory<V, E> ef)
    {
        return new GraphBuilder<>(new DefaultUndirectedGraph<>(ef));
    }
}

// End DefaultDirectedGraph.java
