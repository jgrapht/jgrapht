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

import com.google.common.collect.Iterables;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;

/**
 * An adapter class for directed graphs using <a href="http://webgraph.di.unimi.it/">WebGraph
 * (big)</a>'s {@link ImmutableGraph}.
 *
 * <p>
 * This class is equivalent to {@link ImmutableDirectedGraphAdapter}, except that nodes are
 * instances of {@link Long}, and edges are instances of {@link LongLongPair}.
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.webgraph.ImmutableGraph standard WebGraph
 * graph} using the suitable {@linkplain ImmutableGraph#wrap(it.unimi.dsi.webgraph.ImmutableGraph)
 * wrapper}.
 *
 * @see ImmutableDirectedGraphAdapter
 * @author Sebastiano Vigna
 */

public class ImmutableDirectedBigGraphAdapter
    extends
    AbstractImmutableBigGraphAdapter<LongLongPair>
    implements
    FlyweightPrototype<ImmutableDirectedBigGraphAdapter>
{

    private final ImmutableGraph immutableTranspose;

    /**
     * Creates an adapter for a directed big immutable graph.
     *
     * <p>
     * It is responsibility of the caller that the two provided graphs are one the transpose of the
     * other (for each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an
     * arc <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). If this property is not true,
     * results will be unpredictable.
     *
     * @param immutableGraph a big immutable graph.
     * @param immutableTranspose its transpose.
     */
    public ImmutableDirectedBigGraphAdapter(
        final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose)
    {
        super(immutableGraph);
        this.immutableTranspose = immutableTranspose;
        if (immutableTranspose != null && n != immutableTranspose.numNodes())
            throw new IllegalArgumentException(
                "The graph has " + n + " nodes, but the transpose has "
                    + immutableTranspose.numNodes());
    }

    /**
     * Creates an adapter for a directed big immutable graph implementing only methods based on
     * outgoing edges.
     *
     * @param immutableGraph a big immutable graph.
     */
    public ImmutableDirectedBigGraphAdapter(final ImmutableGraph immutableGraph)
    {
        this(immutableGraph, null);
    }

    @Override
    protected LongLongPair makeEdge(final long x, final long y)
    {
        return LongLongPair.of(x, y);
    }

    @Override
    public boolean containsEdge(final LongLongPair e)
    {
        if (e == null)
            return false;
        if (e instanceof LongLongSortedPair)
            return false;
        return containsEdgeFast(e.leftLong(), e.rightLong());
    }

    @Override
    public Set<LongLongPair> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        final long m = iterables().edgeCount();
        final ObjectOpenHashBigSet<LongLongPair> edges = new ObjectOpenHashBigSet<>(m);
        for (long i = 0; i < n; i++) {
            final long x = nodeIterator.nextLong();
            final LazyLongIterator successors = nodeIterator.successors();
            for (long y; (y = successors.nextLong()) != -1;)
                edges.add(LongLongPair.of(x, y));
        }
        return edges;
    }

    @Override
    public int degreeOf(final Long vertex)
    {
        final long d = inDegreeOf(vertex) + outDegreeOf(vertex);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongPair> edgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator successors = immutableGraph.successors(source);
        for (long target; (target = successors.nextLong()) != -1;)
            set.add(LongLongPair.of(source, target));
        final LazyLongIterator predecessors = immutableTranspose.successors(source);
        for (long target; (target = predecessors.nextLong()) != -1;)
            if (source != target)
                set.add(LongLongPair.of(target, source));
        return set;
    }

    @Override
    public int inDegreeOf(final Long vertex)
    {
        final long d = immutableTranspose.outdegree(vertex);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongPair> incomingEdgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator predecessors = immutableTranspose.successors(source);
        for (long target; (target = predecessors.nextLong()) != -1;)
            set.add(LongLongPair.of(target, source));
        return set;
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
    public Set<LongLongPair> outgoingEdgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator successors = immutableGraph.successors(source);
        for (long target; (target = successors.nextLong()) != -1;)
            set.add(LongLongPair.of(source, target));
        return set;
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .weighted(false).modifiable(false).allowMultipleEdges(false).allowSelfLoops(true)
            .directed().build();
    }

    @Override
    public ImmutableDirectedBigGraphAdapter copy()
    {
        return new ImmutableDirectedBigGraphAdapter(
            immutableGraph.copy(), immutableTranspose != null ? immutableTranspose.copy() : null);
    }

    private final GraphIterables<Long, LongLongPair> iterables = new GraphIterables<>()
    {
        @Override
        public ImmutableDirectedBigGraphAdapter getGraph()
        {
            return ImmutableDirectedBigGraphAdapter.this;
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
            try {
                return m = immutableGraph.numArcs();
            } catch (final UnsupportedOperationException e) {
            }
            return m = ObjectIterables.size(edges());
        }

        @Override
        public long degreeOf(final Long vertex)
        {
            return inDegreeOf(vertex) + outDegreeOf(vertex);
        }

        @Override
        public Iterable<LongLongPair> edgesOf(final Long source)
        {
            return Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source, true));
        }

        @Override
        public long inDegreeOf(final Long vertex)
        {
            return immutableTranspose.outdegree(vertex);
        }

        private Iterable<LongLongPair> incomingEdgesOf(final long x, final boolean skipLoops)
        {
            return () -> new Iterator<>()
            {
                final LazyLongIterator successors = immutableTranspose.successors(x);
                long y = -1;

                @Override
                public boolean hasNext()
                {
                    if (y == -1) {
                        y = successors.nextLong();
                        if (skipLoops && x == y)
                            y = successors.nextLong();
                    }
                    return y != -1;
                }

                @Override
                public LongLongPair next()
                {
                    final LongLongPair edge = LongLongPair.of(y, x);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<LongLongPair> incomingEdgesOf(final Long vertex)
        {
            return incomingEdgesOf(vertex, false);
        }

        @Override
        public long outDegreeOf(final Long vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<LongLongPair> outgoingEdgesOf(final Long vertex)
        {
            return () -> new Iterator<>()
            {
                final long x = vertex;
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
                public LongLongPair next()
                {
                    final LongLongPair edge = LongLongPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<LongLongPair> edges()
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
                    while ((y = successors.nextLong()) == -1) {
                        if (!nodeIterator.hasNext())
                            return false;
                        x = nodeIterator.nextLong();
                        successors = nodeIterator.successors();
                    }
                    return true;
                }

                @Override
                public LongLongPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final LongLongPair edge = LongLongPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Long, LongLongPair> iterables()
    {
        return iterables;
    }
}
