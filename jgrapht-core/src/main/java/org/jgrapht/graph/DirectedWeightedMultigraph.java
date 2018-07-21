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
 * A directed weighted multigraph. A directed weighted multigraph is a non-simple directed graph in
 * which no loops are permitted, but multiple (parallel) edges between any two vertices are
 * permitted, and edges have weights.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DirectedWeightedMultigraph<V, E>
    extends
    DirectedMultigraph<V, E>
{
    private static final long serialVersionUID = 1984381120642160572L;

    /**
     * Creates a new graph.
     *
     * @param edgeClass class on which to base the edge supplier
     */
    public DirectedWeightedMultigraph(Class<? extends E> edgeClass)
    {
        this(null, SupplierUtil.createSupplier(edgeClass));
    }

    /**
     * Creates a new graph.
     * 
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     */
    public DirectedWeightedMultigraph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier)
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
        E> GraphBuilder<V, E, ? extends DirectedWeightedMultigraph<V, E>> createBuilder(
            Class<? extends E> edgeClass)
    {
        return new GraphBuilder<>(new DirectedWeightedMultigraph<>(edgeClass));
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
        E> GraphBuilder<V, E, ? extends DirectedWeightedMultigraph<V, E>> createBuilder(
            Supplier<E> edgeSupplier)
    {
        return new GraphBuilder<>(new DirectedWeightedMultigraph<>(null, edgeSupplier));
    }

}

// End DirectedWeightedMultigraph.java
