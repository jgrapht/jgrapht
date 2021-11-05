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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.IncomingEdgesSupport;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.google.common.collect.Iterables;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList.EliasFanoIndexedMonotoneLongBigListIterator;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

/**
 * An immutable directed graph with {@link IntIntPair} edges represented using quasi-succinct data
 * structures.
 *
 * <p>
 * The graph representation of this implementation uses the {@linkplain EliasFanoMonotoneLongBigList
 * Elias&ndash;Fano representation of monotone sequences} to represent the positions of ones in the
 * (linearized) adjacency matrix of the graph. Edges are represented by instances of
 * {@link IntIntPair}. Instances are serializable and thread safe.
 *
 * <p>
 * If the vertex set is compact (i.e., vertices are numbered from 0 consecutively), space usage will
 * be close to twice the information-theoretical lower bound (typically, a few times smaller than a
 * {@link SparseIntDirectedGraph}). If you {@link #SuccinctDirectedGraph(Graph, boolean) drop
 * support for incoming edges} the space will close to the information-theoretical lower bound .
 *
 * <p>
 * All accessors are very fast. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests}
 * are very fast and happen in almost constant time.
 *
 * <p>
 * {@link SuccinctIntDirectedGraph} is a much slower implementation with a similar footprint using
 * {@link Integer} as edge type. Please read the {@linkplain org.jgrapht.sux4j class documentation}
 * for more information.
 *
 * <p>
 * For convenience, and as a compromise with the approach of {@link SuccinctIntDirectedGraph}, this
 * class provides methods {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getEdgeFromIndex(long)
 * getEdgeFromIndex()} and
 * {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getIndexFromEdge(it.unimi.dsi.fastutil.ints.IntIntPair)
 * getIndexFromEdge()} that map bijectively the edge set into a contiguous set of longs.
 *
 * @author Sebastiano Vigna
 * @see SuccinctIntDirectedGraph
 */

public class SuccinctDirectedGraph
    extends
    AbstractSuccinctDirectedGraph<IntIntPair>
    implements
    Serializable
{
    private static final long serialVersionUID = 0L;

    /** The cumulative list of outdegrees. */
    private final EliasFanoIndexedMonotoneLongBigList cumulativeOutdegrees;
    /** The cumulative list of indegrees. */
    private final EliasFanoMonotoneLongBigList cumulativeIndegrees;
    /** The cumulative list of successor lists. */
    private final EliasFanoIndexedMonotoneLongBigList successors;
    /** The cumulative list of predecessor lists. */
    private final EliasFanoMonotoneLongBigList predecessors;

    /**
     * Creates a new immutable succinct directed graph from a given directed graph, choosing whether
     * to support incoming edges.
     *
     * @param graph a directed graph: for good results, vertices should be numbered consecutively
     *        starting from 0.
     * @param incomingEdgesSupport whether to support incoming edges or not.
     * @param <E> the graph edge type
     */
    public <
        E> SuccinctDirectedGraph(final Graph<Integer, E> graph, final boolean incomingEdgesSupport)
    {
        super((int) graph.iterables().vertexCount(), (int) graph.iterables().edgeCount());

        if (graph.getType().isUndirected())
            throw new IllegalArgumentException("This class supports directed graphs only");
        assert graph.getType().isDirected();
        final GraphIterables<Integer, E> iterables = graph.iterables();
        if (iterables.vertexCount() > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                "The number of nodes (" + iterables.vertexCount() + ") is greater than "
                    + Integer.MAX_VALUE);
        if (iterables.edgeCount() > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                "The number of edges (" + iterables.edgeCount() + ") is greater than "
                    + Integer.MAX_VALUE);

        cumulativeOutdegrees = new EliasFanoIndexedMonotoneLongBigList(
            n + 1, m, new CumulativeDegrees(n, graph::outDegreeOf));
        assert cumulativeOutdegrees.getLong(cumulativeOutdegrees.size64() - 1) == m;

        successors = new EliasFanoIndexedMonotoneLongBigList(
            m, (long) n << sourceShift,
            new CumulativeSuccessors<>(graph, iterables::outgoingEdgesOf, true));

        if (incomingEdgesSupport) {
            cumulativeIndegrees = new EliasFanoMonotoneLongBigList(
                n + 1, m, new CumulativeDegrees(n, graph::inDegreeOf));
            assert cumulativeIndegrees.getLong(cumulativeIndegrees.size64() - 1) == m;

            predecessors = new EliasFanoIndexedMonotoneLongBigList(
                m, (long) n * n - m,
                new CumulativeSuccessors<>(graph, iterables::incomingEdgesOf, false));
        }
        else {
            cumulativeIndegrees = predecessors = null;
        }
    }

    /**
     * Creates a new immutable succinct directed graph from a given directed graph, supporting both
     * outgoing and incoming edges.
     *
     * @param graph a directed graph: for good results, vertices should be numbered consecutively
     *        starting from 0.
     * @param <E> the graph edge type
     */
    public <E> SuccinctDirectedGraph(final Graph<Integer, E> graph)
    {
        this(graph, true);
    }

    /**
     * Creates a new immutable succinct directed graph from an edge list, choosing whether to
     * support incoming edges.
     *
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @param incomingEdgesSupport whether to support incoming edges or not.
     * @see #SuccinctDirectedGraph(Graph)
     */

    public SuccinctDirectedGraph(
        final int numVertices, final List<Pair<Integer, Integer>> edges,
        final boolean incomingEdgesSupport)
    {
        this(
            new SparseIntDirectedGraph(
                numVertices, edges, incomingEdgesSupport ? IncomingEdgesSupport.FULL_INCOMING_EDGES
            : IncomingEdgesSupport.NO_INCOMING_EDGES), incomingEdgesSupport);
    }

    /**
     * Creates a new immutable succinct directed graph from an edge list, supporting both outgoing
     * and incoming edges.
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @see #SuccinctDirectedGraph(Graph)
     */

    public SuccinctDirectedGraph(final int numVertices, final List<Pair<Integer, Integer>> edges)
    {
        this(numVertices, edges, true);
    }

    /**
     * Creates a new immutable succinct directed graph from a supplier of streams of edges, choosing
     * whether to support incoming edges.
     *
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param numEdges the number of edges.
     * @param edges a supplier of streams of edges.
     * @param incomingEdgesSupport whether to support incoming edges or not.
     * @see #SuccinctDirectedGraph(Graph)
     */

    public SuccinctDirectedGraph(
        final int numVertices, final int numEdges,
        final Supplier<Stream<Pair<Integer, Integer>>> edges, final boolean incomingEdgesSupport)
    {
        this(
            new SparseIntDirectedGraph(
                numVertices, numEdges, edges,
                incomingEdgesSupport ? IncomingEdgesSupport.FULL_INCOMING_EDGES
                    : IncomingEdgesSupport.NO_INCOMING_EDGES));
    }

    /**
     * Creates a new immutable succinct directed graph from a supplier of streams of edges,
     * supporting both outgoing and incoming edges.
     *
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param numEdges the number of edges.
     * @param edges a supplier of streams of edges.
     * @see #SuccinctDirectedGraph(Graph)
     */

    public SuccinctDirectedGraph(
        final int numVertices, final int numEdges,
        final Supplier<Stream<Pair<Integer, Integer>>> edges)
    {
        this(numVertices, numEdges, edges, true);
    }


    @Override
    public boolean containsEdge(final IntIntPair e)
    {
        return successors.indexOfUnsafe(((long) e.firstInt() << sourceShift) + e.secondInt()) != -1;
    }

    @Override
    public Set<IntIntPair> edgeSet()
    {
        return new ObjectOpenHashSet<>(iterables().edges().iterator());
    }

    @Override
    public Set<IntIntPair> edgesOf(final Integer vertex)
    {
        final Set<IntIntPair> result = outgoingEdgesOf(vertex);
        result.addAll(incomingEdgesOf(vertex));
        return result;
    }

    @Override
    public int inDegreeOf(final Integer vertex)
    {
        assertVertexExist(vertex);
        return (int) cumulativeIndegrees.getDelta(vertex);
    }

    @Override
    public Set<IntIntPair> incomingEdgesOf(final Integer target)
    {
        assertVertexExist(target);
        final int t = target;
        final long[] result = new long[2];
        cumulativeIndegrees.get(t, result);
        final int d = (int) (result[1] - result[0]);
        final LongBigListIterator iterator = predecessors.listIterator(result[0]);

        final ObjectOpenHashSet<IntIntPair> s = new ObjectOpenHashSet<>();
        long base = (long) n * t - result[0];

        for (int i = d; i-- != 0;) {
            final long source = iterator.nextLong() - base--;
            s.add(IntIntPair.of((int) source, t));
        }

        return s;
    }

    @Override
    public int outDegreeOf(final Integer vertex)
    {
        assertVertexExist(vertex);
        return (int) cumulativeOutdegrees.getDelta(vertex);
    }

    @Override
    public Set<IntIntPair> outgoingEdgesOf(final Integer vertex)
    {
        assertVertexExist(vertex);
        final int x = vertex;
        final long[] result = new long[2];
        cumulativeOutdegrees.get(x, result);
        final ObjectOpenHashSet<IntIntPair> s = new ObjectOpenHashSet<>();
        final LongBigListIterator iterator = successors.listIterator(result[0]);
        final long base = (long) x << sourceShift;

        for (int d = (int) (result[1] - result[0]); d-- != 0;)
            s.add(IntIntPair.of(x, (int) (iterator.nextLong() - base)));
        return s;
    }

    @Override
    public Integer getEdgeSource(final IntIntPair e)
    {
        return e.firstInt();
    }

    @Override
    public Integer getEdgeTarget(final IntIntPair e)
    {
        return e.secondInt();
    }

    /**
     * Returns the index associated with the given edge.
     *
     * @param e an edge of the graph.
     * @return the index associated with the edge, or &minus;1 if the edge is not part of the graph.
     * @see #getEdgeFromIndex(long)
     */
    public long getIndexFromEdge(final IntIntPair e)
    {
        final int source = e.firstInt();
        final int target = e.secondInt();
        if (source < 0 || source >= n || target < 0 || target >= n)
            throw new IllegalArgumentException();
        return successors.indexOfUnsafe(((long) source << sourceShift) + target);
    }

    /**
     * Returns the edge with given index.
     *
     * @param i an index between 0 (included) and the number of edges (excluded).
     * @return the pair with index {@code i}.
     * @see #getIndexFromEdge(IntIntPair)
     */
    public IntIntPair getEdgeFromIndex(final long i)
    {
        if (i < 0 || i >= m)
            throw new IllegalArgumentException();
        final long t = successors.getLong(i);
        return IntIntPair.of((int) (t >>> sourceShift), (int) (t & targetMask));
    }

    @Override
    public IntIntPair getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        final long index =
            successors.indexOfUnsafe(((long) sourceVertex << sourceShift) + targetVertex);
        return index != -1 ? IntIntPair.of(sourceVertex, targetVertex) : null;
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        return successors.indexOfUnsafe(((long) sourceVertex << sourceShift) + targetVertex) != -1;
    }

    private final static class SuccinctGraphIterables
        implements
        GraphIterables<Integer, IntIntPair>,
        Serializable
    {
        private static final long serialVersionUID = 0L;
        private final SuccinctDirectedGraph graph;

        private SuccinctGraphIterables()
        {
            graph = null;
        }

        private SuccinctGraphIterables(final SuccinctDirectedGraph graph)
        {
            this.graph = graph;
        }

        @Override
        public Graph<Integer, IntIntPair> getGraph()
        {
            return graph;
        }

        @Override
        public long vertexCount()
        {
            return graph.n;
        }

        @Override
        public long edgeCount()
        {
            return graph.m;
        }

        @Override
        public Iterable<IntIntPair> edges()
        {
            final int sourceShift = graph.sourceShift;
            final long targetMask = graph.targetMask;

            return () -> new Iterator<>()
            {
                private final EliasFanoIndexedMonotoneLongBigListIterator iterator = graph.successors.iterator();
                private final int n = graph.n;

                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                @Override
                public IntIntPair next()
                {
                    final long t = iterator.nextLong();
                    return IntIntPair.of((int) (t >>> sourceShift), (int) (t & targetMask));
                }

            };
        }

        @Override
        public Iterable<IntIntPair> edgesOf(final Integer source)
        {
            return Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source, true));
        }

        private Iterable<IntIntPair> incomingEdgesOf(final int target, final boolean skipLoops)
        {
            final SuccinctDirectedGraph graph = this.graph;
            final long[] result = new long[2];
            graph.cumulativeIndegrees.get(target, result);
            final int d = (int) (result[1] - result[0]);
            final EliasFanoIndexedMonotoneLongBigList successors = graph.successors;
            final LongBigListIterator iterator = graph.predecessors.listIterator(result[0]);

            return () -> new Iterator<>()
            {
                int i = d;
                IntIntPair edge = null;
                long n = graph.n;
                long base = target * n - result[0];

                @Override
                public boolean hasNext()
                {
                    if (edge == null && i > 0) {
                        i--;
                        final long source = iterator.nextLong() - base--;
                        if (skipLoops && source == target && i-- != 0)
                            return false;
                        edge = IntIntPair.of((int) source, target);
                    }
                    return edge != null;
                }

                @Override
                public IntIntPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final IntIntPair result = edge;
                    edge = null;
                    return result;
                }
            };
        }

        @Override
        public Iterable<IntIntPair> outgoingEdgesOf(final Integer vertex)
        {
            final int sourceShift = graph.sourceShift;
            final long targetMask = graph.targetMask;

            graph.assertVertexExist(vertex);
            final int x = vertex;
            final long[] result = new long[2];
            graph.cumulativeOutdegrees.get(x, result);
            final LongBigListIterator iterator = graph.successors.listIterator(result[0]);
            final long base = (long) x << sourceShift;

            return () -> new Iterator<>() {
                private int d = (int) (result[1] - result[0]);

                @Override
                public boolean hasNext()
                {
                    return d != 0;
                }

                @Override
                public IntIntPair next()
                {
                    if (d == 0)
                        throw new NoSuchElementException();
                    d--;
                    return IntIntPair.of(x, (int) (iterator.nextLong() - base));
                }

            };
        }

        @Override
        public Iterable<IntIntPair> incomingEdgesOf(final Integer vertex)
        {
            graph.assertVertexExist(vertex);
            return incomingEdgesOf(vertex, false);
        }
    }

    private final GraphIterables<Integer, IntIntPair> iterables = new SuccinctGraphIterables(this);

    @Override
    public GraphIterables<Integer, IntIntPair> iterables()
    {
        return iterables;
    }
}
