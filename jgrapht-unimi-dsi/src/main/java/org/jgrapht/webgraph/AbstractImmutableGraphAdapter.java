/*
 * (C) Copyright 2020-2020, by Sebastiano Vigna and Contributors.
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

package org.jgrapht.webgraph;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.graph.AbstractGraph;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.LazyIntSkippableIterator;

/**
 * An abstract base class for adapters using <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s
 * {@link ImmutableGraph}. Nodes are instances of {@link Integer} corresponding to the index of a
 * node in WebGraph.
 *
 * @param <E> the type of an edge.
 * @author Sebastiano Vigna
 */

public abstract class AbstractImmutableGraphAdapter<E extends IntIntPair>
    extends
    AbstractGraph<Integer, E>
{

    /** The underlying graph. */
    protected final ImmutableGraph immutableGraph;
    /** The number of nodes of {@link #immutableGraph}. */
    protected final int n;
    /**
     * The number of edges, cached, or -1 if it still unknown. This will have to be computed by
     * enumeration for undirected graphs, as we do not know how many loops are present, and for
     * graphs which do not support {@link ImmutableGraph#numArcs()}.
     */
    protected long m = -1;

    protected AbstractImmutableGraphAdapter(final ImmutableGraph immutableGraph)
    {
        this.immutableGraph = immutableGraph;
        this.n = immutableGraph.numNodes();
    }

    @Override
    public Set<E> getAllEdges(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        if (x < 0 || x >= n || y < 0 || y >= n)
            return null;

        return containsEdgeFast(x, y) ? Collections.singleton(makeEdge(x, y))
            : Collections.emptySet();
    }

    protected abstract E makeEdge(int x, int y);

    @Override
    public E getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        return containsEdgeFast(x, y) ? makeEdge(x, y) : null;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(final Integer sourceVertex, final Integer targetVertex, final E e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addVertex(final Integer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return false;
        return containsEdgeFast(sourceVertex, targetVertex);
    }

    protected boolean containsEdgeFast(final int x, final int y)
    {
        if (x < 0 || x >= n || y < 0 || y >= n)
            return false;
        final LazyIntIterator successors = immutableGraph.successors(x);
        if (successors instanceof LazyIntSkippableIterator) {
            // Fast skipping available
            return y == ((LazyIntSkippableIterator) successors).skipTo(y);
        } else
            for (int target; (target = successors.nextInt()) != -1;)
                if (target == y)
                    return true;
        return false;
    }

    @Override
    public boolean containsVertex(final Integer v)
    {
        if (v == null)
            return false;
        final int x = v;
        return x >= 0 && x < n;
    }

    @Override
    public E removeEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeVertex(final Integer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return IntSets.fromTo(0, n);
    }

    @Override
    public Integer getEdgeSource(final E e)
    {
        return e.leftInt();
    }

    @Override
    public Integer getEdgeTarget(final E e)
    {
        return e.rightInt();
    }

    @Override
    public double getEdgeWeight(final E e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final E e, final double weight)
    {
        if (weight != 1)
            throw new UnsupportedOperationException();
    }
}
