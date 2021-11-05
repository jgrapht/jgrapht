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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.GraphIterables;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;

/**
 * An adapter class for undirected graphs using <a href="http://webgraph.di.unimi.it/">WebGraph
 * (big)</a>'s {@link ImmutableGraph}.
 *
 * <p>
 * This class is equivalent to {@link ImmutableUndirectedGraphAdapter}, except that nodes are
 * instances of {@link Long}, and edges are instances of {@link LongLongSortedPair}.
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.webgraph.ImmutableGraph standard WebGraph
 * graph} using the suitable {@linkplain ImmutableGraph#wrap(it.unimi.dsi.webgraph.ImmutableGraph)
 * wrapper}.
 *
 * @see ImmutableUndirectedGraphAdapter
 * @author Sebastiano Vigna
 */

public class ImmutableUndirectedBigGraphAdapter
    extends
    AbstractImmutableBigGraphAdapter<LongLongSortedPair>
    implements
    FlyweightPrototype<ImmutableUndirectedBigGraphAdapter>
{

    /**
     * Creates an adapter for an undirected (i.e., symmetric) big immutable graph.
     *
     * <p>
     * It is responsibility of the caller that the provided graph has is symmetric (for each arc
     * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
     * <var>x</var>). If this property is not true, results will be unpredictable.
     *
     * @param immutableGraph a symmetric big immutable graph.
     */
    public ImmutableUndirectedBigGraphAdapter(final ImmutableGraph immutableGraph)
    {
        super(immutableGraph);
    }

    @Override
    protected LongLongSortedPair makeEdge(final long x, final long y)
    {
        return LongLongSortedPair.of(x, y);
    }

    @Override
    public boolean containsEdge(final LongLongSortedPair e)
    {
        if (e == null)
            return false;
        return containsEdgeFast(e.leftLong(), e.rightLong());
    }

    @Override
    public Set<LongLongSortedPair> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        final long m = iterables().edgeCount();
        final ObjectOpenHashBigSet<LongLongSortedPair> edges = new ObjectOpenHashBigSet<>(m);
        for (long i = 0; i < n; i++) {
            final long x = nodeIterator.nextLong();
            final LazyLongIterator successors = nodeIterator.successors();
            for (long y; (y = successors.nextLong()) != -1;)
                if (x <= y)
                    edges.add(LongLongSortedPair.of(x, y));
        }
        return edges;
    }

    @Override
    public int degreeOf(final Long vertex)
    {
        final long d = inDegreeOf(vertex) + (containsEdgeFast(vertex, vertex) ? 1L : 0L);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongSortedPair> edgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongSortedPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator predecessors = immutableGraph.successors(source);
        for (long target; (target = predecessors.nextLong()) != -1;)
            set.add(LongLongSortedPair.of(source, target));
        return set;
    }

    @Override
    public int inDegreeOf(final Long vertex)
    {
        final long d = immutableGraph.outdegree(vertex);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongSortedPair> incomingEdgesOf(final Long vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public int outDegreeOf(final Long vertex)
    {
        final long d = immutableGraph.outdegree(vertex);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongSortedPair> outgoingEdgesOf(final Long vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public LongLongSortedPair removeEdge(final Long sourceVertex, final Long targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final LongLongSortedPair e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeVertex(final Long v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Long> vertexSet()
    {
        return LongSets.fromTo(0, n);
    }

    @Override
    public Long getEdgeSource(final LongLongSortedPair e)
    {
        return e.leftLong();
    }

    @Override
    public Long getEdgeTarget(final LongLongSortedPair e)
    {
        return e.rightLong();
    }

    @Override
    public double getEdgeWeight(final LongLongSortedPair e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final LongLongSortedPair e, final double weight)
    {
        if (weight != 1)
            throw new UnsupportedOperationException();
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .weighted(false).modifiable(false).allowMultipleEdges(false).allowSelfLoops(true)
            .undirected().build();
    }

    @Override
    public ImmutableUndirectedBigGraphAdapter copy()
    {
        return new ImmutableUndirectedBigGraphAdapter(immutableGraph.copy());
    }

    private final GraphIterables<Long, LongLongSortedPair> iterables = new GraphIterables<>()
    {
        @Override
        public ImmutableUndirectedBigGraphAdapter getGraph()
        {
            return ImmutableUndirectedBigGraphAdapter.this;
        }

        @Override
        public long vertexCount()
        {
            return n;
        }

        @Override
        public long edgeCount()
        {
            if (m != -1)
                return m;
            return m = ObjectIterables.size(edges());
        }

        @Override
        public long degreeOf(final Long vertex)
        {
            return inDegreeOf(vertex) + (containsEdgeFast(vertex, vertex) ? 1 : 0);
        }

        @Override
        public Iterable<LongLongSortedPair> edgesOf(final Long vertex)
        {
            final long x = vertex;
            return () -> new Iterator<>()
            {
                final LazyLongIterator successors = immutableGraph.successors(x);
                long y = -1;

                @Override
                public boolean hasNext()
                {
                    if (y == -1)
                        y = successors.nextLong();
                    return y != -1;
                }

                @Override
                public LongLongSortedPair next()
                {
                    final LongLongSortedPair edge = LongLongSortedPair.of(y, x);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public long inDegreeOf(final Long vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        public Iterable<LongLongSortedPair> incomingEdgesOf(final Long vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public long outDegreeOf(final Long vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<LongLongSortedPair> outgoingEdgesOf(final Long vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public Iterable<LongLongSortedPair> edges()
        {
            return () -> new Iterator<>()
            {
                final NodeIterator nodeIterator = immutableGraph.nodeIterator();
                LazyLongIterator successors = LazyLongIterators.EMPTY_ITERATOR;
                long x, y = -1;

                @Override
                public boolean hasNext()
                {
                    if (y != -1)
                        return true;
                    do {
                        while ((y = successors.nextLong()) == -1) {
                            if (!nodeIterator.hasNext())
                                return false;
                            x = nodeIterator.nextLong();
                            successors = nodeIterator.successors();
                        }
                    } while (y < x);
                    return true;
                }

                @Override
                public LongLongSortedPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final LongLongSortedPair edge = LongLongSortedPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Long, LongLongSortedPair> iterables()
    {
        return iterables;
    }
}
