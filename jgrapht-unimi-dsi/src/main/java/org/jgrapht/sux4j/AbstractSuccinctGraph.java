/*
 * (C) Copyright 2020-2021, by Sebastiano Vigna and Contributors.
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

package org.jgrapht.sux4j;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.graph.AbstractGraph;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectSets;

/**
 * An abstract base class for all succinct implementations.
 *
 * <p>
 * This class provides mutators throwing {@link UnsupportedOperationException} and operations
 * depending only on the number of vertices and edges.
 *
 * @param <E> the graph edge type
 */

public abstract class AbstractSuccinctGraph<E>
    extends
    AbstractGraph<Integer, E>
    implements
    Serializable
{
    private static final long serialVersionUID = 0L;

    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    /** The number of vertices in the graph. */
    protected final int n;
    /** The number of edges in the graph. */
    protected final int m;
    /** The shift used to read sources in the successor list. */
    protected final int sourceShift;
    /** The mask used to read targets in the successor list (lowest {@link #sourceShift} bits). */
    protected final long targetMask;

    public AbstractSuccinctGraph(final int n, final int m)
    {
        super();
        this.n = n;
        this.m = m;
        sourceShift = Fast.ceilLog2(n);
        targetMask = (1L << sourceShift) - 1;
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return IntSets.fromTo(0, n);
    }

    /**
     * Ensures that the specified vertex exists in this graph, or else throws exception.
     *
     * @param v vertex
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified vertex does not exist in this graph.
     */
    @Override
    protected boolean assertVertexExist(final Integer v)
    {
        if (v < 0 || v >= n)
            throw new IllegalArgumentException();
        return true;
    }

    @Override
    public boolean containsVertex(final Integer v)
    {
        return v >= 0 && v < n;
    }

    @Override
    public Set<E> getAllEdges(final Integer sourceVertex, final Integer targetVertex)
    {
        final E edge = getEdge(sourceVertex, targetVertex);
        return edge == null ? ObjectSets.emptySet() : ObjectSets.singleton(edge);
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<E> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public E addEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addEdge(final Integer sourceVertex, final Integer targetVertex, final E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addVertex(final Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public E removeEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeVertex(final Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public double getEdgeWeight(final E e)
    {
        return 1.0;
    }

    @Override
    public void setEdgeWeight(final E e, final double weight)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

}
