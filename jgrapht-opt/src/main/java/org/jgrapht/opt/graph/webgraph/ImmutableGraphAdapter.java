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

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.ints.IntSets;
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
import it.unimi.dsi.webgraph.Transform;

/**
 * A graph adapter class using <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s
 * {@link ImmutableGraph}.
 *
 * <p>
 * Nodes are instance of {@link Integer} corresponding to the index of a node in WebGraph. Edges are
 * represented by an {@link IntIntPair}, for directed graph, or by an {@link IntIntSortedPair}, for
 * an undirected graph. In directed, the left and right element are the source and the target of the
 * edge. In the undirected case, edges are canonicalized so that the left element is always smaller
 * than or equal to the right element. Since the underlying graph is immutable, the resulting graph
 * is unmodifiable. Edges are immutable and can be tested for equality (e.g., stored in a
 * dictionary).
 *
 * <p>
 * WebGraph provides methods for successors only, so to adapt a directed graph you must provide both
 * a graph and its transpose (methods to compute the transpose are available in {@link Transform}).
 *
 * You need to load an {@link ImmutableGraph} and its transpose using one of the load methods
 * available, and use the {@link #directed(ImmutableGraph, ImmutableGraph)} factory method.
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * immutableTranspose = ImmutableGraph.loadMapped("mygraph-t");
 * adapter = ImmutableGraphAdapterEndpointPair.directed(immutableGraph, immutableTranspose);
 * </pre>
 *
 * <p>
 * It is your responsibility that the two provided graphs are one the transpose of the other (for
 * each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an arc
 * <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). No check will be performed. Note that
 * {@linkplain GraphIterables#edgeCount() computing the number of edges of an directed graph}
 * requires a full scan of the edge set if {@link ImmutableGraph#numArcs()} is not supported (the
 * first time&mdash;then it will be cached).
 *
 * <p>
 * If you use a load method that does not provide random access, most methods will throw an
 * {@link UnsupportedOperationException}. The first graph will be used to implement
 * {@link #outgoingEdgesOf(Integer)}, and the second graph to implement
 * {@link #incomingEdgesOf(Integer)}.
 *
 * <p>
 * If you know that you will never used methods based on incoming edges
 * ({@link #incomingEdgesOf(Integer)}, {@link #inDegreeOf(Integer)}, {@link #edgesOf(Integer)},
 * {@link #degreeOf(Integer)}), you can also use the factory method using just a graph, but all such
 * methods will throw a {@link NullPointerException}:
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * adapter = ImmutableGraphAdapter.directed(immutableGraph);
 * </pre>
 *
 * <p>
 * If your graph is symmetric, you can adapt it as an undirected graph:
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * adapter = ImmutableGraphAdapter.undirected(immutableGraph);
 * </pre>
 *
 * <p>
 * It is your responsibility that the provided graph is symmetric (for each arc
 * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
 * <var>x</var>). No check will be performed, but you can use the {@link Check} class to this
 * purpose. Note that {@linkplain GraphIterables#edgeCount() computing the number of edges of an
 * undirected graph} requires a full scan of the edge set (the first time&mdash;then it will be
 * cached).
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.big.webgraph.ImmutableGraph big WebGraph
 * graph} with less than {@link Integer#MAX_VALUE} vertices using the suitable
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
 * @see ImmutableBigGraphAdapter
 * @author Sebastiano Vigna
 */

public class ImmutableGraphAdapter
    extends
    AbstractGraph<Integer, IntIntPair>
    implements
    FlyweightPrototype<ImmutableGraphAdapter>
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
    public static ImmutableGraphAdapter undirected(final ImmutableGraph immutableGraph)
    {
        return new ImmutableGraphAdapter(immutableGraph, immutableGraph);
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
    public static ImmutableGraphAdapter directed(
        final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose)
    {
        return new ImmutableGraphAdapter(immutableGraph, immutableTranspose);
    }

    /**
     * Creates an adapter for a directed immutable graph exposing only methods based on outgoing
     * edges.
     *
     * @param immutableGraph an immutable graph.
     * @return an {@linkplain GraphType#isDirected() directed} {@link Graph} providing only methods
     *         based on outgoing edges; all other methods will throw a {@link NullPointerException}.
     */
    public static ImmutableGraphAdapter directed(final ImmutableGraph immutableGraph)
    {
        return new ImmutableGraphAdapter(immutableGraph, null);
    }

    protected ImmutableGraphAdapter(
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
    public Set<IntIntPair> getAllEdges(
        final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        if (x < 0 || x >= n || y < 0 || y >= n)
            return null;

        return containsEdgeFast(x, y)
            ? Collections
                .singleton(
                    directed ? IntIntPair.of(x, y)
                        : IntIntSortedPair.of(x, y))
            : Collections.emptySet();
    }

    @Override
    public IntIntPair getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        return containsEdgeFast(x, y) ? (directed ? IntIntPair.of(x, y) : IntIntSortedPair.of(x, y))
            : null;
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<IntIntPair> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public IntIntPair addEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(
        final Integer sourceVertex, final Integer targetVertex, final IntIntPair e)
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
    public boolean containsEdge(final IntIntPair e)
    {
        if (e == null)
            return false;
        if (directed == (e instanceof IntIntSortedPair))
            return false;
        return containsEdgeFast(e.leftInt(), e.rightInt());
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return false;
        return containsEdgeFast(sourceVertex, targetVertex);
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
    public Set<IntIntPair> edgeSet()
    {
        final NodeIterator nodeIterator = immutableGraph.nodeIterator();
        long m = 16; // Min hash table size
        try {
            m = immutableGraph.numArcs();
        } catch (final UnsupportedOperationException e) {
        }
        final ObjectOpenHashBigSet<IntIntPair> edges = new ObjectOpenHashBigSet<>(m);
        for (int i = 0; i < n; i++) {
            final int x = nodeIterator.nextInt();
            final LazyIntIterator successors = nodeIterator.successors();
            if (directed)
                for (int y; (y = successors.nextInt()) != -1;)
                    edges.add(IntIntPair.of(x, y));
            else
                for (int y; (y = successors.nextInt()) != -1;)
                    if (x <= y)
                        edges.add(IntIntSortedPair.of(x, y));
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
    public Set<IntIntPair> edgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<IntIntPair> set = new ObjectLinkedOpenHashSet<>();
        final int source = vertex;
        if (directed) {
            final LazyIntIterator successors = immutableGraph.successors(source);
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(IntIntPair.of(source, target));
            final LazyIntIterator predecessors = immutableTranspose.successors(source);
            for (int target; (target = predecessors.nextInt()) != -1;)
                if (source != target)
                    set.add(IntIntPair.of(target, source));
        } else {
            final LazyIntIterator successors = immutableGraph.successors(source);
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(IntIntSortedPair.of(source, target));
        }
        return set;
    }

    @Override
    public int inDegreeOf(final Integer vertex)
    {
        return immutableTranspose.outdegree(vertex);
    }

    @Override
    public Set<IntIntPair> incomingEdgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<IntIntPair> set = new ObjectLinkedOpenHashSet<>();
        final int source = vertex;
        final LazyIntIterator predecessors = immutableTranspose.successors(source);
        if (directed) {
            for (int target; (target = predecessors.nextInt()) != -1;)
                set.add(IntIntPair.of(target, source));
        } else {
            for (int target; (target = predecessors.nextInt()) != -1;)
                set.add(IntIntSortedPair.of(target, source));
        }

        return set;
    }

    @Override
    public int outDegreeOf(final Integer vertex)
    {
        return immutableGraph.outdegree(vertex);
    }

    @Override
    public Set<IntIntPair> outgoingEdgesOf(final Integer vertex)
    {
        final ObjectLinkedOpenHashSet<IntIntPair> set = new ObjectLinkedOpenHashSet<>();
        final int source = vertex;
        final LazyIntIterator successors = immutableGraph.successors(source);
        if (directed) {
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(IntIntPair.of(source, target));
        } else {
            for (int target; (target = successors.nextInt()) != -1;)
                set.add(IntIntSortedPair.of(source, target));
        }
        return set;
    }

    @Override
    public IntIntPair removeEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final IntIntPair e)
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
    public Integer getEdgeSource(final IntIntPair e)
    {
        return e.leftInt();
    }

    @Override
    public Integer getEdgeTarget(final IntIntPair e)
    {
        return e.rightInt();
    }

    @Override
    public double getEdgeWeight(final IntIntPair e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final IntIntPair e, final double weight)
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
    public ImmutableGraphAdapter copy()
    {
        if (directed)
            return new ImmutableGraphAdapter(
                immutableGraph.copy(), immutableTranspose.copy());
        final ImmutableGraph copy = immutableGraph.copy();
        return new ImmutableGraphAdapter(copy, copy);
    }

    private final GraphIterables<Integer, IntIntPair> ITERABLES = new GraphIterables<>()
    {
        @Override
        public ImmutableGraphAdapter getGraph()
        {
            return ImmutableGraphAdapter.this;
        }

        @Override
        public long vertexCount()
        {
            return n;
        }

        @Override
        public long edgeCount()
        {
            if (ImmutableGraphAdapter.this.m != -1)
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
        public long degreeOf(final Integer vertex)
        {
            return directed ? inDegreeOf(vertex) + outDegreeOf(vertex)
                : inDegreeOf(vertex) + (containsEdge(vertex, vertex) ? 1 : 0);
        }

        @Override
        public Iterable<IntIntPair> edgesOf(final Integer source)
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

        private Iterable<IntIntPair> incomingEdgesOf(
            final int x, final boolean skipLoops)
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
                public IntIntPair next()
                {
                    final IntIntPair edge =
                        directed ? IntIntPair.of(y, x) : IntIntSortedPair.of(y, x);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<IntIntPair> incomingEdgesOf(final Integer vertex)
        {
            return incomingEdgesOf(vertex, false);
        }

        @Override
        public long outDegreeOf(final Integer vertex)
        {
            return immutableGraph.outdegree(vertex);
        }

        @Override
        public Iterable<IntIntPair> outgoingEdgesOf(final Integer vertex)
        {
            return () -> new Iterator<>()
            {
                final int x = vertex;
                final LazyIntIterator successors = immutableGraph.successors(x);
                int y = successors.nextInt();

                @Override
                public boolean hasNext()
                {
                    if (y == -1)
                        y = successors.nextInt();
                    return y != -1;
                }

                @Override
                public IntIntPair next()
                {
                    final IntIntPair edge =
                        directed ? IntIntPair.of(x, y) : IntIntSortedPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }

        @Override
        public Iterable<IntIntPair> edges()
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
                public IntIntPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final IntIntPair edge =
                        directed ? IntIntPair.of(x, y) : IntIntSortedPair.of(x, y);
                    y = -1;
                    return edge;
                }
            };
        }
    };

    @Override
    public GraphIterables<Integer, IntIntPair> iterables()
    {
        return ITERABLES;
    }
}
