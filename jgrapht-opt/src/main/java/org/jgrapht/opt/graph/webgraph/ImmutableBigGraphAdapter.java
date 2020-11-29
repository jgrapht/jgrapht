/*
 * (C) Copyright 2020, by Sebastiano Vigna.
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

package org.jgrapht.opt.graph.webgraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.GraphIterables;
import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultGraphType.Builder;

import com.google.common.collect.Iterables;
import com.google.common.graph.Graph;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.LazyLongSkippableIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;

/**
 * A graph adapter class using <a href="http://webgraph.di.unimi.it/">WebGraph (big)</a>'s
 * {@link ImmutableGraph}.
 *
 * <p>
 * This class is equivalent to {@link ImmutableGraphAdapterEndpointPair}, except that nodes are
 * instances of {@link Long}, and edges are instances of {@link LongLongPair} or
 * {@link LongLongSortedPair}.
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.webgraph.ImmutableGraph standard WebGraph
 * graph} using the suitable {@linkplain ImmutableGraph#wrap(it.unimi.dsi.webgraph.ImmutableGraph)
 * wrapper}.
 *
 * @see ImmutableGraphAdapterEndpointPair
 * @see ImmutableBigGraphAdapterLongArray
 * @author Sebastiano Vigna
 */

public class ImmutableBigGraphAdapter
    extends
    AbstractGraph<Long, LongLongPair>
    implements
    FlyweightPrototype<ImmutableBigGraphAdapter>
{

    /** The underlying graph. */
    private final ImmutableGraph immutableGraph;
    /**
     * The transpose of {@link #immutableGraph}, for a directed graph with full support;
     * {@code null}, for a directed graph with access to outgoing edges, only;
     * {@link #immutableGraph}, for an undirected graph (in which case, {@link #immutableGraph} must
     * be symmetric).
     */
    private final ImmutableGraph immutableTranspose;
    /** The cached value of {@link #immutableGraph} != {@link #immutableTranspose}. */
    private final boolean directed;
    /** The number of nodes of {@link #immutableGraph}. */
    private final long n;
    /**
     * The number of edges, cached, or -1 if it still unknown. This will have to be computed by
     * enumeration for undirected graphs, as we do not know how many loops are present, and for
     * graphs which do not support {@link ImmutableGraph#numArcs()}.
     */
    private long m = -1;

    /**
     * Creates an adapter for an undirected (i.e., symmetric) big immutable graph.
     *
     * <p>
     * It is your responsibility that the provided graph has is symmetric (for each arc
     * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
     * <var>x</var>). If this property is not true, results will be unpredictable.
     *
     * @param immutableGraph a symmetric big immutable graph.
     * @return an {@linkplain GraphType#isUndirected() undirected} {@link Graph}.
     */
    public static ImmutableBigGraphAdapter undirected(
        final ImmutableGraph immutableGraph)
    {
        return new ImmutableBigGraphAdapter(immutableGraph, immutableGraph);
    }

    /**
     * Creates an adapter for a directed big immutable graph.
     *
     * <p>
     * It is your responsibility that the two provided graphs are one the transpose of the other
     * (for each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an arc
     * <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). If this property is not true,
     * results will be unpredictable.
     *
     * @param immutableGraph a big immutable graph.
     * @param immutableTranspose its transpose.
     * @return an {@linkplain GraphType#isDirected() directed} {@link Graph}.
     */
    public static ImmutableBigGraphAdapter directed(
        final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose)
    {
        return new ImmutableBigGraphAdapter(immutableGraph, immutableTranspose);
    }

    /**
     * Creates an adapter for a big directed immutable graph exposing only methods based on outgoing
     * edges.
     *
     * @param immutableGraph a big immutable graph.
     * @return an {@linkplain GraphType#isDirected() directed} {@link Graph} providing only methods
     *         based on outgoing edges; all other methods will throw a {@link NullPointerException}.
     */
    public static ImmutableBigGraphAdapter directed(final ImmutableGraph immutableGraph)
    {
        return new ImmutableBigGraphAdapter(immutableGraph, null);
    }

    protected ImmutableBigGraphAdapter(
        final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose)
    {
        this.immutableGraph = immutableGraph;
        this.immutableTranspose = immutableTranspose;
        this.directed = immutableGraph != immutableTranspose;
        this.n = immutableGraph.numNodes();
        if (immutableTranspose != null && n != immutableTranspose.numNodes())
            throw new IllegalArgumentException(
                "The graph has " + n + " nodes, but the transpose has "
                    + immutableTranspose.numNodes());
    }

    @Override
    public Set<LongLongPair> getAllEdges(final Long sourceVertex, final Long targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final long x = sourceVertex;
        final long y = targetVertex;
        if (x < 0 || x >= n || y < 0 || y >= n)
            return null;
        return containsEdgeFast(x, y)
            ? Collections
                .singleton(
                    directed ? LongLongPair.of(x, y) : LongLongSortedPair.of(x, y))
            : Collections.emptySet();
    }

    @Override
    public LongLongPair getEdge(final Long sourceVertex, final Long targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final long x = sourceVertex;
        final long y = targetVertex;
        return containsEdgeFast(x, y)
            ? (directed ? LongLongPair.of(x, y) : LongLongSortedPair.of(x, y)) : null;
    }

    @Override
    public Supplier<Long> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<LongLongPair> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public LongLongPair addEdge(final Long sourceVertex, final Long targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(
        final Long sourceVertex, final Long targetVertex, final LongLongPair e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long addVertex()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addVertex(final Long v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsEdge(final LongLongPair e)
    {
        if (e == null)
            return false;
        if (directed == (e instanceof LongLongSortedPair))
            return false;
        return containsEdgeFast(e.leftLong(), e.rightLong());
    }

    @Override
    public boolean containsEdge(final Long sourceVertex, final Long targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return false;
        return containsEdgeFast(sourceVertex, targetVertex);
    }

    private boolean containsEdgeFast(final long x, final long y)
    {
        if (x < 0 || x >= n || y < 0 || y >= n)
            return false;
        final LazyLongIterator successors = immutableGraph.successors(x);
        if (successors instanceof LazyLongSkippableIterator) {
            // Fast skipping available
            return y == ((LazyLongSkippableIterator) successors).skipTo(y);
        } else
            for (long target; (target = successors.nextLong()) != -1;)
                if (target == y)
                    return true;
        return false;
    }

    @Override
    public boolean containsVertex(final Long v)
    {
        if (v == null)
            return false;
        final long x = v;
        return x >= 0 && x < n;
    }

    @Override
    public Set<LongLongPair> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        long m = 16; // Min hash table size
        try {
            m = immutableGraph.numArcs();
        } catch (final UnsupportedOperationException e) {
        }
        final ObjectOpenHashBigSet<LongLongPair> edges = new ObjectOpenHashBigSet<>(m);
        for (long i = 0; i < n; i++) {
            final long x = nodeIterator.nextLong();
            final LazyLongIterator successors = nodeIterator.successors();
            if (directed)
                for (long y; (y = successors.nextLong()) != -1;)
                    edges.add(LongLongPair.of(x, y));
            else
                for (long y; (y = successors.nextLong()) != -1;)
                    if (x <= y)
                        edges.add(LongLongSortedPair.of(x, y));
        }
        return edges;
    }

    @Override
    public int degreeOf(final Long vertex)
    {
        if (directed) {
            final long d = inDegreeOf(vertex) + outDegreeOf(vertex);
            if (d >= Integer.MAX_VALUE)
                throw new ArithmeticException();
            return (int) d;
        } else
            return inDegreeOf(vertex) + (containsEdge(vertex, vertex) ? 1 : 0);
    }

    @Override
    public Set<LongLongPair> edgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        if (directed) {
            final LazyLongIterator successors = immutableGraph.successors(source);
            for (long target; (target = successors.nextLong()) != -1;)
                set.add(LongLongPair.of(source, target));
            final LazyLongIterator predecessors = immutableTranspose.successors(source);
            for (long target; (target = predecessors.nextLong()) != -1;)
                if (source != target)
                    set.add(LongLongPair.of(target, source));
        } else {
            final LazyLongIterator predecessors = immutableTranspose.successors(source);
            for (long target; (target = predecessors.nextLong()) != -1;)
                set.add(LongLongSortedPair.of(target, source));
        }
        return set;
    }

    @Override
    public int inDegreeOf(final Long vertex)
    {
        final long d = immutableTranspose.outdegree(vertex);
        if (d >= Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongPair> incomingEdgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator predecessors = immutableTranspose.successors(source);
        if (directed)
            for (long target; (target = predecessors.nextLong()) != -1;)
                set.add(LongLongPair.of(target, source));
        else
            for (long target; (target = predecessors.nextLong()) != -1;)
                set.add(LongLongSortedPair.of(target, source));
        return set;
    }

    @Override
    public int outDegreeOf(final Long vertex)
    {
        final long d = immutableGraph.outdegree(vertex);
        if (d >= Integer.MAX_VALUE)
            throw new ArithmeticException();
        return (int) d;
    }

    @Override
    public Set<LongLongPair> outgoingEdgesOf(final Long vertex)
    {
        final ObjectLinkedOpenHashSet<LongLongPair> set = new ObjectLinkedOpenHashSet<>();
        final long source = vertex;
        final LazyLongIterator successors = immutableGraph.successors(source);
        if (directed)
            for (long target; (target = successors.nextLong()) != -1;)
                set.add(LongLongPair.of(source, target));
        else
            for (long target; (target = successors.nextLong()) != -1;)
                set.add(LongLongSortedPair.of(source, target));
        return set;
    }

    @Override
    public LongLongPair removeEdge(final Long sourceVertex, final Long targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final LongLongPair e)
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
    public Long getEdgeSource(final LongLongPair e)
    {
        return e.leftLong();
    }

    @Override
    public Long getEdgeTarget(final LongLongPair e)
    {
        return e.rightLong();
    }

    @Override
    public double getEdgeWeight(final LongLongPair e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final LongLongPair e, final double weight)
    {
        if (weight != 1)
            throw new UnsupportedOperationException();
    }

    @Override
    public GraphType getType()
    {
        final Builder builder = new DefaultGraphType.Builder()
            .weighted(false).modifiable(false).allowMultipleEdges(false).allowSelfLoops(true);
        return directed ? builder.directed().build() : builder.undirected().build();
    }

    @Override
    public ImmutableBigGraphAdapter copy()
    {
        if (directed)
            return new ImmutableBigGraphAdapter(
                immutableGraph.copy(), immutableTranspose.copy());
        final ImmutableGraph copy = immutableGraph.copy();
        return new ImmutableBigGraphAdapter(copy, copy);
    }

    private final GraphIterables<Long, LongLongPair> ITERABLES = new GraphIterables<>()
    {
        @Override
        public ImmutableBigGraphAdapter getGraph()
        {
            return ImmutableBigGraphAdapter.this;
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
            if (directed) {
                try {
                    return m = immutableGraph.numArcs();
                } catch (final UnsupportedOperationException e) {
                }
            }
            return m = ObjectIterables.size(edges());
        }

        @Override
        public long degreeOf(final Long vertex)
        {
            return directed ? inDegreeOf(vertex) + outDegreeOf(vertex)
                : inDegreeOf(vertex) + (containsEdge(vertex, vertex) ? 1 : 0);
        }

        @Override
        public Iterable<LongLongPair> edgesOf(final Long source)
        {
            return directed
                ? Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source, true))
                : outgoingEdgesOf(source);
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
                    final LongLongPair edge =
                        directed ? LongLongPair.of(y, x) : LongLongSortedPair.of(y, x);
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
                final LazyLongIterator successors = immutableGraph.successors(vertex);
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
                    final LongLongPair edge =
                        directed ? LongLongPair.of(vertex, y) : LongLongSortedPair.of(vertex, y);
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
                    do {
                        while ((y = successors.nextLong()) == -1) {
                            if (!nodeIterator.hasNext())
                                return false;
                            x = nodeIterator.nextLong();
                            successors = nodeIterator.successors();
                        }
                    } while (!directed && y < x);
                    return true;
                }

                @Override
                public LongLongPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final LongLongPair edge =
                        directed ? LongLongPair.of(x, y) : LongLongSortedPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Long, LongLongPair> iterables()
    {
        return ITERABLES;
    }
}
