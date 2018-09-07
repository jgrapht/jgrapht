/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
 * Implementation of a <a href=http://mathworld.wolfram.com/SimpleGraph.html>Simple Graph</a>.
 * A Simple Graph is an undirected graph containing no <a href="http://mathworld.wolfram.com/GraphLoop.html">graph loops</a> or
 * <a href="http://mathworld.wolfram.com/MultipleEdge.html">multiple edges</a>. This particular implementation supports
 * both weighted and unweighted edges.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 */
public class SimpleGraph<V, E>
    extends
    AbstractBaseGraph<V, E>
{
    private static final long serialVersionUID = 4607246833824317836L;

    /**
     * Creates a new simple graph.
     *
     * @param edgeClass class on which to base the edge supplier
     */
    public SimpleGraph(Class<? extends E> edgeClass)
    {
        this(null, SupplierUtil.createSupplier(edgeClass), false);
    }

    /**
     * Creates a new simple graph.
     * 
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param weighted whether the graph is weighted or not
     */
    public SimpleGraph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted)
    {
        super(
            vertexSupplier, edgeSupplier,
            new DefaultGraphType.Builder()
                .undirected().allowMultipleEdges(false).allowSelfLoops(false).weighted(weighted)
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
    public static <V, E> GraphBuilder<V, E, ? extends SimpleGraph<V, E>> createBuilder(
        Class<? extends E> edgeClass)
    {
        return new GraphBuilder<>(new SimpleGraph<>(edgeClass));
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeSupplier the edge supplier of the new graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V,
        E> GraphBuilder<V, E, ? extends SimpleGraph<V, E>> createBuilder(Supplier<E> edgeSupplier)
    {
        return new GraphBuilder<>(new SimpleGraph<>(null, edgeSupplier, false));
    }

}

// End SimpleGraph.java
