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
package org.jgrapht.opt.graph.fastutil;

import java.util.function.Supplier;

import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractBaseGraph;

/**
 * A graph implementation using fastutil's map implementations for storage.
 * 
 * <p>The following example creates a simple undirected weighted graph: <blockquote>
 * 
 * <pre>
 * Graph&lt;String,
 *     DefaultWeightedEdge&gt; g = new FastutilMapGraph&lt;&gt;(
 *         SupplierUtil.createStringSupplier(), SupplierUtil.createDefaultWeightedEdgeSupplier(),
 *         DefaultGraphType.simple().asWeighted());
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>In case you have integer vertices, consider using the {@link FastutilMapIntVertexGraph}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see FastutilMapIntVertexGraph
 * 
 * @author Dimitrios Michail
 */
public class FastutilMapGraph<V, E>
    extends
    AbstractBaseGraph<V, E>
{
    private static final long serialVersionUID = -2261627370606792673L;

    /**
     * Construct a new graph.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     * @param fastLookups whether to index vertex pairs to allow (expected) constant time edge
     *        lookups (by vertex endpoints)
     * @throws IllegalArgumentException if the graph type is not supported by this implementation
     */
    public FastutilMapGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, GraphType type, boolean fastLookups)
    {
        super(
            vertexSupplier, edgeSupplier, type,
            fastLookups ? new FastutilFastLookupGSS<>()
                : new FastutilGSS<>());
    }

    /**
     * Construct a new graph.
     * 
     * <p>By default we index vertex pairs to allow (expected) constant time edge lookups.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     * @throws IllegalArgumentException if the graph type is not supported by this implementation
     */
    public FastutilMapGraph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, GraphType type)
    {
        this(vertexSupplier, edgeSupplier, type, true);
    }

}
