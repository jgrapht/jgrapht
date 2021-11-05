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

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph;

import com.google.common.collect.Iterables;

import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList.EliasFanoIndexedMonotoneLongBigListIterator;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

/**
 * An immutable undirected graph with {@link IntIntSortedPair} edges represented using
 * quasi-succinct data structures.
 *
 * <p>
 * The graph representation of this implementation uses the {@linkplain EliasFanoMonotoneLongBigList
 * Elias&ndash;Fano representation of monotone sequences} to represent the positions of ones in the
 * (linearized) adjacency matrix of the graph. Edges are represented by instances of
 * {@link IntIntSortedPair}. Instances are serializable and thread safe.
 *
 * <p>
 * If the vertex set is compact (i.e., vertices are numbered from 0 consecutively), space usage will
 * be close to the information-theoretical lower bound (typically, a few times smaller than a
 * {@link SparseIntUndirectedGraph}).
 *
 * <p>
 * All accessors are very fast. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests}
 * are very fast and happen in almost constant time.
 *
 * <p>
 * {@link SuccinctIntUndirectedGraph} is a much slower implementation with a similar footprint using
 * {@link Integer} as edge type. Please read the {@linkplain org.jgrapht.sux4j class documentation}
 * for more information.
 *
 * <p>
 * For convenience, and as a compromise with the approach of {@link SuccinctIntUndirectedGraph},
 * this class provides methods {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getEdgeFromIndex(long)
 * getEdgeFromIndex()} and
 * {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getIndexFromEdge(it.unimi.dsi.fastutil.ints.IntIntPair)
 * getIndexFromEdge()} that map bijectively the edge set into a contiguous set of longs.
 *
 * @author Sebastiano Vigna
 * @see SuccinctIntUndirectedGraph
 */


public class SuccinctUndirectedGraph
    extends
    AbstractSuccinctUndirectedGraph<IntIntSortedPair>
    implements
    Serializable
{
    private static final long serialVersionUID = 0L;

    /** The cumulative list of outdegrees (number of edges in sorted order, including loops). */
    private final EliasFanoIndexedMonotoneLongBigList cumulativeOutdegrees;
    /** The cumulative list of indegrees (number of edges in reversed order, including loops). */
    private final EliasFanoMonotoneLongBigList cumulativeIndegrees;
    /** The cumulative list of successor (edges in sorted order, including loops) lists. */
    private final EliasFanoIndexedMonotoneLongBigList successors;
    /** The cumulative list of predecessor (edges in reversed order, including loops) lists. */
    private final EliasFanoMonotoneLongBigList predecessors;

    /**
     * Creates a new immutable succinct undirected graph from a given undirected graph.
     *
     * @param graph an undirected graph: for good results, vertices should be numbered consecutively
     *        starting from 0.
     * @param <E> the graph edge type
     */
    public <E> SuccinctUndirectedGraph(final Graph<Integer, E> graph)
    {
        super((int) graph.iterables().vertexCount(), (int) graph.iterables().edgeCount());

        if (graph.getType().isDirected())
            throw new IllegalArgumentException("This class supports undirected graphs only");
        assert graph.getType().isUndirected();
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
            n + 1, m, new CumulativeDegrees<>(graph, true, iterables::edgesOf));
        cumulativeIndegrees = new EliasFanoMonotoneLongBigList(
            n + 1, m, new CumulativeDegrees<>(graph, false, iterables::edgesOf));
        assert cumulativeOutdegrees.getLong(cumulativeOutdegrees.size64() - 1) == m;
        assert cumulativeIndegrees.getLong(cumulativeIndegrees.size64() - 1) == m;

        successors = new EliasFanoIndexedMonotoneLongBigList(
            m, (long) n << sourceShift,
            new CumulativeSuccessors<>(graph, true, iterables::outgoingEdgesOf));
        predecessors = new EliasFanoIndexedMonotoneLongBigList(
            m, (long) n * n - m,
            new CumulativeSuccessors<>(graph, false, iterables::incomingEdgesOf));
    }

    /**
     * Creates a new immutable succinct undirected graph from an edge list.
     *
     * <p>
     * This constructor just builds a {@link SparseIntUndirectedGraph} and delegates to the
     * {@linkplain #SuccinctUndirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @see #SuccinctUndirectedGraph(Graph)
     */

    public SuccinctUndirectedGraph(
        final int numVertices, final List<Pair<Integer, Integer>> edges)
    {
        this(new SparseIntUndirectedGraph(numVertices, edges));
    }

    @Override
    public boolean containsEdge(final IntIntSortedPair e)
    {
        return successors.indexOfUnsafe(((long) e.firstInt() << sourceShift) + e.secondInt()) != -1;
    }

    @Override
    public Set<IntIntSortedPair> edgeSet()
    {
        return new ObjectOpenHashSet<>(iterables().edges().iterator());
    }

    @Override
    public int degreeOf(final Integer vertex)
    {
        return (int) cumulativeIndegrees.getDelta(vertex)
            + (int) cumulativeOutdegrees.getDelta(vertex);
    }

    @Override
    public Set<IntIntSortedPair> edgesOf(final Integer vertex)
    {
        assertVertexExist(vertex);
        final int x = vertex;
        final long[] result = new long[2];
        cumulativeOutdegrees.get(x, result);
        final Set<IntIntSortedPair> s = new ObjectOpenHashSet<>();
        final LongBigListIterator iterator = successors.listIterator(result[0]);
        final long base = (long) x << sourceShift;

        for (int d = (int) (result[1] - result[0]); d-- != 0;)
            s.add(IntIntSortedPair.of(x, (int) (iterator.nextLong() - base)));

        for (final IntIntSortedPair e : iterables.reverseSortedEdgesOfNoLoops(x))
            s.add(e);

        return s;
    }

    @Override
    public Set<IntIntSortedPair> incomingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public Set<IntIntSortedPair> outgoingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public Integer getEdgeSource(final IntIntSortedPair e)
    {
        return e.firstInt();
    }

    @Override
    public Integer getEdgeTarget(final IntIntSortedPair e)
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
    public long getIndexFromEdge(final IntIntSortedPair e)
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
     * @see #getIndexFromEdge(IntIntSortedPair)
     */
    public IntIntSortedPair getEdgeFromIndex(final long i)
    {
        if (i < 0 || i >= m)
            throw new IllegalArgumentException();
        final long t = successors.getLong(i);
        return IntIntSortedPair.of((int) (t >>> sourceShift), (int) (t & targetMask));
    }

    @Override
    public IntIntSortedPair getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        int x = sourceVertex;
        int y = targetVertex;
        if (x > y) {
            final int t = x;
            x = y;
            y = t;
        }
        final long index = successors.indexOfUnsafe(((long) x << sourceShift) + y);
        return index != -1 ? IntIntSortedPair.of(x, y) : null;
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        return containsEdge(successors, sourceVertex, targetVertex);
    }

    private final static class SuccinctGraphIterables
        implements
        GraphIterables<Integer, IntIntSortedPair>,
        Serializable
    {
        private static final long serialVersionUID = 0L;
        private final SuccinctUndirectedGraph graph;

        private SuccinctGraphIterables()
        {
            graph = null;
        }

        private SuccinctGraphIterables(final SuccinctUndirectedGraph graph)
        {
            this.graph = graph;
        }

        @Override
        public Graph<Integer, IntIntSortedPair> getGraph()
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
        public Iterable<IntIntSortedPair> edges()
        {
            final int sourceShift = graph.sourceShift;
            final long targetMask = graph.targetMask;

            return () -> new Iterator<>()
            {
                private final EliasFanoIndexedMonotoneLongBigListIterator iterator =
                    graph.successors.iterator();
                private final int n = graph.n;

                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                @Override
                public IntIntSortedPair next()
                {
                    final long t = iterator.nextLong();
                    return IntIntSortedPair.of((int) (t >>> sourceShift), (int) (t & targetMask));
                }

            };
        }

        @Override
        public Iterable<IntIntSortedPair> edgesOf(final Integer source)
        {
            return Iterables.concat(sortedEdges(source), reverseSortedEdgesOfNoLoops(source));
        }

        private Iterable<IntIntSortedPair> sortedEdges(final int source)
        {
            final int sourceShift = graph.sourceShift;
            final long targetMask = graph.targetMask;
            final long[] result = new long[2];
            graph.cumulativeOutdegrees.get(source, result);
            final var iterator = graph.successors.listIterator(result[0]);
            final long base = (long) source << sourceShift;

            return () -> new Iterator<>()
            {
                private int d = (int) (result[1] - result[0]);

                @Override
                public boolean hasNext()
                {
                    return d != 0;
                }

                @Override
                public IntIntSortedPair next()
                {
                    if (d == 0)
                        throw new NoSuchElementException();
                    d--;
                    return IntIntSortedPair.of(source, (int) (iterator.nextLong() - base));
                }
            };
        }

        private Iterable<IntIntSortedPair> reverseSortedEdgesOfNoLoops(final int target)
        {
            final long[] result = new long[2];
            graph.cumulativeIndegrees.get(target, result);
            final int d = (int) (result[1] - result[0]);
            final LongBigListIterator iterator = graph.predecessors.listIterator(result[0]);

            return () -> new Iterator<>()
            {
                int i = d;
                IntIntSortedPair edge = null;
                long n = graph.n;
                long base = n * target - result[0];

                @Override
                public boolean hasNext()
                {
                    if (edge == null && i > 0) {
                        i--;
                        final long source = iterator.nextLong() - base--;
                        if (source == target && i-- == 0)
                            return false;
                        edge = IntIntSortedPair.of((int) source, target);
                    }
                    return edge != null;
                }

                @Override
                public IntIntSortedPair next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final IntIntSortedPair result = edge;
                    edge = null;
                    return result;
                }
            };
        }

        @Override
        public Iterable<IntIntSortedPair> incomingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public Iterable<IntIntSortedPair> outgoingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }
    }

    private final SuccinctGraphIterables iterables = new SuccinctGraphIterables(this);

    @Override
    public GraphIterables<Integer, IntIntSortedPair> iterables()
    {
        return iterables;
    }
}
