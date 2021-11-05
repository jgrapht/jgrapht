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

import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;
import it.unimi.dsi.webgraph.Check;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.LazyIntIterators;
import it.unimi.dsi.webgraph.LazyIntSkippableIterator;
import it.unimi.dsi.webgraph.NodeIterator;

/**
 * An adapter class for undirected graphs using
 * <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s {@link ImmutableGraph}.
 *
 * <p>
 * Nodes are instances of {@link Integer} corresponding to the index of a node in WebGraph. Edges
 * are represented by an {@link IntIntSortedPair}. Edges are canonicalized so that the left element
 * is always smaller than or equal to the right element. Since the underlying graph is immutable,
 * the resulting graph is unmodifiable. Edges are immutable and can be tested for equality (e.g.,
 * stored in a dictionary).
 *
 * <p>
 * You need to load a symmetric {@link ImmutableGraph} using one of the available load methods
 * available, and then build an adapter:
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * adapter = new ImmutableUndirectedGraphAdapter(immutableGraph);
 * </pre>
 *
 * <p>
 * It is your responsibility that the provided graph is symmetric (for each arc
 * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
 * <var>x</var>). No check will be performed, but you can use the {@link Check} class to this
 * purpose. Note that {@linkplain GraphIterables#edgeCount() computing the number of edges of a
 * graph} requires a full scan of the edge set if {@link ImmutableGraph#numArcs()} is not supported
 * (the first time&mdash;then it will be cached).
 *
 * <p>
 * If you use a load method that does not provide random access, most methods will throw an
 * {@link UnsupportedOperationException}.
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.big.webgraph.ImmutableGraph big WebGraph
 * graph} with at most {@link Integer#MAX_VALUE} vertices using the suitable
 * {@linkplain it.unimi.dsi.big.webgraph.ImmutableGraph#wrap(ImmutableGraph) wrapper}.
 *
 * <h2>Thread safety</h2>
 *
 * <p>
 * This class is not thread safe: following the {@link FlyweightPrototype} pattern, users can access
 * concurrently the graph {@linkplain #copy() by getting lightweight copies}.
 *
 * <h2>Fast adjacency check</h2>
 *
 * <p>
 * As it happens for the sparse representation of JGraphT, usually a WebGraph compressed
 * representation requires scanning the adjacency list of a node to
 * {@linkplain #getEdge(Integer, Integer) test whether a specific arc exists}. However, if you adapt
 * a WebGraph class (such as {@link EFGraph}) which provides {@linkplain LazyIntSkippableIterator
 * skippable iterators} with fast skipping, adjacency can be tested more quickly (e.g., essentially
 * in constant time in the case of {@link EFGraph}).
 *
 * @see AbstractImmutableBigGraphAdapter
 * @author Sebastiano Vigna
 */

public class ImmutableUndirectedGraphAdapter
    extends
    AbstractImmutableGraphAdapter<IntIntSortedPair>
    implements
    FlyweightPrototype<ImmutableUndirectedGraphAdapter>
{
    /**
     * Creates an adapter for an undirected (i.e., symmetric) immutable graph.
     *
     * <p>
     * It is responsibility of the caller that the provided graph has is symmetric (for each arc
     * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
     * <var>x</var>). If this property is not true, results will be unpredictable.
     *
     * @param immutableGraph a symmetric immutable graph.
     */
    public ImmutableUndirectedGraphAdapter(final ImmutableGraph immutableGraph)
    {
        super(immutableGraph);
    }

    @Override
    protected IntIntSortedPair makeEdge(final int x, final int y)
    {
        return IntIntSortedPair.of(x, y);
    }

    @Override
    public boolean containsEdge(final IntIntSortedPair e)
    {
        if (e == null)
            return false;
        return containsEdgeFast(e.leftInt(), e.rightInt());
    }

    @Override
    public Set<IntIntSortedPair> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        final long m = iterables().edgeCount();
        final ObjectOpenHashBigSet<IntIntSortedPair> edges = new ObjectOpenHashBigSet<>(m);
        for (int i = 0; i < n; i++) {
            final int x = nodeIterator.nextInt();
            final LazyIntIterator successors = nodeIterator.successors();
            for (int y; (y = successors.nextInt()) != -1;)
                if (x <= y)
                    edges.add(IntIntSortedPair.of(x, y));
        }
        return edges;
    }

    @Override
    public int degreeOf(final Integer vertex)
    {
        final long d = inDegreeOf(vertex) + (containsEdgeFast(vertex, vertex) ? 1L : 0L);
        if (d > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<IntIntSortedPair> edgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<IntIntSortedPair> set = new ObjectLinkedOpenHashSet<>();
        final int source = vertex;
        final LazyIntIterator successors = immutableGraph.successors(source);
        for (int target; (target = successors.nextInt()) != -1;)
            set.add(IntIntSortedPair.of(source, target));
        return set;
    }

    @Override
    public int inDegreeOf(final Integer vertex)
    {
        return immutableGraph.outdegree(vertex);
    }

    @Override
    public Set<IntIntSortedPair> incomingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public int outDegreeOf(final Integer vertex)
    {
        return immutableGraph.outdegree(vertex);
    }

    @Override
    public Set<IntIntSortedPair> outgoingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .weighted(false).modifiable(false).allowMultipleEdges(false).allowSelfLoops(true)
            .undirected().build();
    }

    @Override
    public ImmutableUndirectedGraphAdapter copy()
    {
        return new ImmutableUndirectedGraphAdapter(immutableGraph.copy());
    }

    private final GraphIterables<Integer, IntIntSortedPair> iterables = new GraphIterables<>()
    {
        @Override
        public ImmutableUndirectedGraphAdapter getGraph()
        {
            return ImmutableUndirectedGraphAdapter.this;
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
        public long degreeOf(final Integer vertex)
        {
            return inDegreeOf(vertex) + (containsEdgeFast(vertex, vertex) ? 1 : 0);
        }

        @Override
        public Iterable<IntIntSortedPair> edgesOf(final Integer vertex)
        {
            final int x = vertex;
            return () -> new Iterator<>()
            {
                final LazyIntIterator successors = immutableGraph.successors(x);
                int y = successors.nextInt();

                @Override
                public boolean hasNext()
                {
                    if (y != -1)
                        return true;
                    return (y = successors.nextInt()) != -1;
                }

                @Override
                public IntIntSortedPair next()
                {
                    final IntIntSortedPair edge = IntIntSortedPair.of(y, x);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public long inDegreeOf(final Integer vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<IntIntSortedPair> incomingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public long outDegreeOf(final Integer vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<IntIntSortedPair> outgoingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public Iterable<IntIntSortedPair> edges()
        {
            return () -> new Iterator<>()
            {
                final NodeIterator nodeIterator = immutableGraph.nodeIterator();
                LazyIntIterator successors = LazyIntIterators.EMPTY_ITERATOR;
                int x, y = -1;

                @Override
                public boolean hasNext()
                {
                    if (y != -1)
                        return true;
                    do {
                        while ((y = successors.nextInt()) == -1) {
                            if (!nodeIterator.hasNext())
                                return false;
                            x = nodeIterator.nextInt();
                            successors = nodeIterator.successors();
                        }
                    } while (y < x);
                    return true;
                }

                @Override
                public IntIntSortedPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final IntIntSortedPair edge = IntIntSortedPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Integer, IntIntSortedPair> iterables()
    {
        return iterables;
    }
}
