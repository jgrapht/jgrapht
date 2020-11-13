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

import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.LazyIntIterators;
import it.unimi.dsi.webgraph.LazyIntSkippableIterator;
import it.unimi.dsi.webgraph.NodeIterator;

/**
 * A graph adapter class using <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s
 * {@link ImmutableGraph}.
 *
 * <p>
 * Nodes are instance of {@link Integer} corresponding to the index of a node in WebGraph. Since the
 * underlying graph is immutable, the resulting graph is unmodifiable. The adapter uses two-elements
 * arrays of integers to represent edges of directed graphs, where the first element of the array is
 * the source node and the second element the target node. For undirected graphs, the order is
 * immaterial, but {@link #getEdgeSource(int[])} and {@link #getEdgeTarget(int[])} will return
 * consistently the minimum and maximum between the two vertices, and {@link #edgeSet()} /
 * {@link GraphIterables#edges()} will return arrays in which the first element is lesser than or
 * equal to the second element.
 *
 * <p>
 * This implementation has the same features of {@link ImmutableGraphAdapterEndpointPair}, but it
 * uses much less space because of the compact edge representation. However, since edges are
 * represented by arrays, they cannot be compared by equality, so, for example, the edge sets of two
 * equal graphs will not be equal. If you need an implementation supporting edge equality you can
 * use {@link ImmutableGraphAdapterEndpointPair}, which however uses much more space: on 64-bit
 * JVMs, edge representation is more than twice larger, and it uses three objects instead of one.
 *
 *
 * @see ImmutableGraphAdapterEndpointPair
 * @author Sebastiano Vigna
 */

public class ImmutableGraphAdapterIntArray
    extends
    AbstractGraph<Integer, int[]>
    implements
    FlyweightPrototype<ImmutableGraphAdapterIntArray>
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
    private final int n;
    /**
     * The number of edges, cached, or -1 if it still unknown. This will have to be computed by
     * enumeration for undirected graphs, as we do not know how many loops are present, and for
     * graphs which do not support {@link ImmutableGraph#numArcs()}.
     */
    private long m = -1;

    /**
     * Creates an adapter for an undirected (i.e., symmetric) immutable graph.
     *
     * <p>
     * It is your responsibility that the provided graph has is symmetric (for each arc
     * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
     * <var>x</var>). If this property is not true, results will be unpredictable.
     *
     * @param immutableGraph a symmetric immutable graph.
     * @return an {@linkplain GraphType#isUndirected() undirected} {@link Graph}.
     */
    public static ImmutableGraphAdapterIntArray undirected(final ImmutableGraph immutableGraph)
    {
        return new ImmutableGraphAdapterIntArray(immutableGraph, immutableGraph);
    }

    /**
     * Creates an adapter for a directed immutable graph.
     *
     * <p>
     * It is your responsibility that the two provided graphs are one the transpose of the other
     * (for each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an arc
     * <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). If this property is not true,
     * results will be unpredictable.
     *
     * @param immutableGraph an immutable graph.
     * @param immutableTranspose its transpose.
     * @return an {@linkplain GraphType#isDirected() directed} {@link Graph}.
     */
    public static ImmutableGraphAdapterIntArray directed(
        final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose)
    {
        return new ImmutableGraphAdapterIntArray(immutableGraph, immutableTranspose);
    }

    /**
     * Creates an adapter for a directed immutable graph exposing only methods based on outgoing
     * edges.
     *
     * @param immutableGraph an immutable graph.
     * @return an {@linkplain GraphType#isDirected() directed} {@link Graph} providing only methods
     *         based on outgoing edges; all other methods will throw a {@link NullPointerException}.
     */
    public static ImmutableGraphAdapterIntArray directed(final ImmutableGraph immutableGraph)
    {
        return new ImmutableGraphAdapterIntArray(immutableGraph, null);
    }

    protected ImmutableGraphAdapterIntArray(
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
    public Set<int[]> getAllEdges(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        if (x < 0 || x >= n || y < 0 || y >= n)
            return null;
        return containsEdgeFast(x, y) ? Collections.singleton(new int[] { x, y })
            : Collections.emptySet();
    }

    @Override
    public int[] getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex.intValue();
        final int y = targetVertex.intValue();
        return containsEdgeFast(x, y) ? (new int[] { x, y }) : null;
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<int[]> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public int[] addEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(final Integer sourceVertex, final Integer targetVertex, final int[] e)
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
    public boolean containsEdge(final int[] e)
    {
        if (e == null)
            return false;
        return containsEdgeFast(e[0], e[1]);
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return false;
        return containsEdgeFast(sourceVertex.intValue(), targetVertex.intValue());
    }

    private boolean containsEdgeFast(final int x, final int y)
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
    public Set<int[]> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        long m = 16; // Min hash table size
        try {
            m = immutableGraph.numArcs();
        } catch (final UnsupportedOperationException e) {
        }
        final ObjectOpenHashBigSet<int[]> edges = new ObjectOpenHashBigSet<>(m);
        for (int i = 0; i < n; i++) {
            final int x = nodeIterator.nextInt();
            final LazyIntIterator successors = nodeIterator.successors();
            if (directed)
                for (int y; (y = successors.nextInt()) != -1;)
                    edges.add(new int[] { x, y });
            else
                for (int y; (y = successors.nextInt()) != -1;)
                    if (x <= y)
                        edges.add(new int[] { x, y });
        }
        return edges;
    }

    @Override
    public int degreeOf(final Integer vertex)
    {
        return directed ? inDegreeOf(vertex) + outDegreeOf(vertex)
            : inDegreeOf(vertex) + (containsEdge(vertex, vertex) ? 1 : 0);
    }

    @Override
    public Set<int[]> edgesOf(final Integer source)
    {
        final ObjectLinkedOpenHashSet<int[]> set = new ObjectLinkedOpenHashSet<>();
        if (directed) {
            final LazyIntIterator successors = immutableGraph.successors(source);
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(new int[] { source, target });
            final LazyIntIterator predecessors = immutableTranspose.successors(source);
            for (int target; (target = predecessors.nextInt()) != -1;)
                if (source != target)
                    set.add(new int[] { target, source });
        } else {
            final LazyIntIterator successors = immutableGraph.successors(source);
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(new int[] { source, target });
        }
        return set;
    }

    @Override
    public int inDegreeOf(final Integer vertex)
    {
        return immutableTranspose.outdegree(vertex);
    }

    @Override
    public Set<int[]> incomingEdgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<int[]> set = new ObjectLinkedOpenHashSet<>();
        final LazyIntIterator predecessors = immutableTranspose.successors(vertex);
        for (int target; (target = predecessors.nextInt()) != -1;)
            set.add(new int[] { target, vertex });
        return set;
    }

    @Override
    public int outDegreeOf(final Integer vertex)
    {
        return immutableGraph.outdegree(vertex);
    }

    @Override
    public Set<int[]> outgoingEdgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<int[]> set = new ObjectLinkedOpenHashSet<>();
        final LazyIntIterator successors = immutableGraph.successors(vertex);
        if (directed)
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(new int[] { vertex, target });
        else
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(new int[] { vertex, target });
        return set;
    }

    @Override
    public int[] removeEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final int[] e)
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
    public Integer getEdgeSource(final int[] e)
    {
        return directed ? e[0] : Math.min(e[0], e[1]);
    }

    @Override
    public Integer getEdgeTarget(final int[] e)
    {
        return directed ? e[1] : Math.max(e[0], e[1]);
    }

    @Override
    public double getEdgeWeight(final int[] e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final int[] e, final double weight)
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
    public ImmutableGraphAdapterIntArray copy()
    {
        if (directed)
            return new ImmutableGraphAdapterIntArray(
                immutableGraph.copy(), immutableTranspose.copy());
        final ImmutableGraph copy = immutableGraph.copy();
        return new ImmutableGraphAdapterIntArray(copy, copy);
    }

    public static long size(final Iterable<?> iterable)
    {
        long c = 0;
        for (@SuppressWarnings("unused") final Object dummy : iterable)
            c++;
        return c;
    }

    private final GraphIterables<Integer, int[]> ITERABLES = new GraphIterables<>()
    {
        @Override
        public ImmutableGraphAdapterIntArray getGraph()
        {
            return ImmutableGraphAdapterIntArray.this;
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
            return m = size(edges());
        }

        @Override
        public long degreeOf(final Integer vertex)
        {
            return directed ? inDegreeOf(vertex) + outDegreeOf(vertex)
                : inDegreeOf(vertex) + (containsEdge(vertex, vertex) ? 1 : 0);
        }

        @Override
        public Iterable<int[]> edgesOf(final Integer source)
        {
            return directed
                ? Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source, true))
                : outgoingEdgesOf(source);
        }

        @Override
        public long inDegreeOf(final Integer vertex)
        {
            return immutableTranspose.outdegree(vertex);
        }

        private Iterable<int[]> incomingEdgesOf(final int x, final boolean skipLoops)
        {
            return () -> new Iterator<>()
            {
                final LazyIntIterator successors = immutableTranspose.successors(x);
                int y = successors.nextInt();

                @Override
                public boolean hasNext()
                {
                    if (y == -1) {
                        y = successors.nextInt();
                        if (skipLoops && x == y)
                            y = successors.nextInt();
                    }
                    return y != -1;
                }

                @Override
                public int[] next()
                {
                    final int[] edge = new int[] { y, x };
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<int[]> incomingEdgesOf(final Integer vertex)
        {
            return incomingEdgesOf(vertex, false);
        }

        @Override
        public long outDegreeOf(final Integer vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<int[]> outgoingEdgesOf(final Integer vertex)
        {
            return () -> new Iterator<>()
            {
                final int x = vertex;
                final LazyIntIterator successors = immutableGraph.successors(vertex);
                int y = successors.nextInt();

                @Override
                public boolean hasNext()
                {
                    if (y == -1)
                        y = successors.nextInt();
                    return y != -1;
                }

                @Override
                public int[] next()
                {
                    final int[] edge = new int[] { x, y };
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<int[]> edges()
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
                    } while (!directed && y < x);
                    return true;
                }

                @Override
                public int[] next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final int[] edge = new int[] { x, y };
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Integer, int[]> iterables()
    {
        return ITERABLES;
    }
}
