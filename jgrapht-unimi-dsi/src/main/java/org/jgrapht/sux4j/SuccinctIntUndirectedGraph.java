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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph;

import com.google.common.collect.Iterables;

import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

/**
 * An immutable undirected graph with {@link Integer} edges represented using quasi-succinct data
 * structures.
 *
 * <p>
 * The graph representation of this implementation is similar to that of
 * {@link SparseIntDirectedGraph}: nodes and edges are initial intervals of the natural numbers.
 * Under the hood, however, this class uses the {@linkplain EliasFanoMonotoneLongBigList
 * Elias&ndash;Fano representation of monotone sequences} to represent the positions of ones in the
 * (linearized) adjacency matrix of the graph. Instances are serializable and thread safe.
 *
 * <p>
 * If the vertex set is compact (i.e., vertices are numbered from 0 consecutively), space usage will
 * be close to the information-theoretical lower bound (typically, a few times smaller than a
 * {@link SparseIntUndirectedGraph}).
 *
 * <p>
 * {@linkplain org.jgrapht.GraphIterables#outgoingEdgesOf(Object) Enumeration of edges} is very
 * slow. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests} are very fast and
 * happen in almost constant time.
 *
 * <p>
 * {@link SuccinctUndirectedGraph} is a much faster implementation with a similar footprint using
 * {@link IntIntSortedPair} as edge type. Please read the {@linkplain org.jgrapht.sux4j class
 * documentation} for more information.
 *
 * @author Sebastiano Vigna
 * @see SuccinctUndirectedGraph
 */

public class SuccinctIntUndirectedGraph
    extends
    AbstractSuccinctUndirectedGraph<Integer>
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
    public <E> SuccinctIntUndirectedGraph(final Graph<Integer, E> graph)
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
     * {@linkplain #SuccinctIntUndirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @see #SuccinctIntUndirectedGraph(Graph)
     */

    public SuccinctIntUndirectedGraph(
        final int numVertices, final List<Pair<Integer, Integer>> edges)
    {
        this(new SparseIntUndirectedGraph(numVertices, edges));
    }

    @Override
    public boolean containsEdge(final Integer e)
    {
        return e >= 0 && e < m;
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return IntSets.fromTo(0, m);
    }

    @Override
    public int degreeOf(final Integer vertex)
    {
        return (int) cumulativeIndegrees.getDelta(vertex)
            + (int) cumulativeOutdegrees.getDelta(vertex);
    }

    @Override
    public IntSet edgesOf(final Integer vertex)
    {
        final long[] result = new long[2];
        cumulativeOutdegrees.get(vertex, result);
        final IntSet s = new IntOpenHashSet(IntSets.fromTo((int) result[0], (int) result[1]));
        for (final int e : iterables.reverseSortedEdgesOfNoLoops(vertex))
            s.add(e);
        return s;
    }

    @Override
    public IntSet incomingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public IntSet outgoingEdgesOf(final Integer vertex)
    {
        return edgesOf(vertex);
    }

    @Override
    public Integer getEdgeSource(final Integer e)
    {
        assertEdgeExist(e);
        return (int) (successors.getLong(e) >>> sourceShift);
    }

    @Override
    public Integer getEdgeTarget(final Integer e)
    {
        assertEdgeExist(e);
        return (int) (successors.getLong(e) & targetMask);
    }

    @Override
    public Integer getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        int x = sourceVertex;
        int y = targetVertex;
        if (x > y) {
            final int t = x;
            x = y;
            y = t;
        }
        final long index = successors.indexOfUnsafe(((long) x << sourceShift) + y);
        return index != -1 ? (int) index : null;
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        return containsEdge(successors, sourceVertex, targetVertex);
    }

    /**
     * Ensures that the specified edge exists in this graph, or else throws exception.
     *
     * @param e edge
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified edge does not exist in this graph.
     */
    protected boolean assertEdgeExist(final Integer e)
    {
        if (e < 0 || e >= m)
            throw new IllegalArgumentException();
        return true;

    }

    private final static class SuccinctGraphIterables
        implements
        GraphIterables<Integer, Integer>,
        Serializable
    {
        private static final long serialVersionUID = 0L;
        private final SuccinctIntUndirectedGraph graph;

        private SuccinctGraphIterables()
        {
            graph = null;
        }

        private SuccinctGraphIterables(final SuccinctIntUndirectedGraph graph)
        {
            this.graph = graph;
        }

        @Override
        public Graph<Integer, Integer> getGraph()
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
        public Iterable<Integer> edgesOf(final Integer source)
        {
            final long[] result = new long[2];
            graph.cumulativeOutdegrees.get(source, result);
            return Iterables
                .concat(
                    IntSets.fromTo((int) result[0], (int) result[1]),
                    reverseSortedEdgesOfNoLoops(source));
        }

        private Iterable<Integer> reverseSortedEdgesOfNoLoops(final int target)
        {
            final long[] result = new long[2];
            graph.cumulativeIndegrees.get(target, result);
            final int d = (int) (result[1] - result[0]);
            final int sourceShift = graph.sourceShift;
            final EliasFanoIndexedMonotoneLongBigList successors = graph.successors;
            final LongBigListIterator iterator = graph.predecessors.listIterator(result[0]);

            return () -> new IntIterator()
            {
                int i = d;
                int edge = -1;
                long n = graph.n;
                long base = n * target - result[0];

                @Override
                public boolean hasNext()
                {
                    if (edge == -1 && i > 0) {
                        i--;
                        final long source = iterator.nextLong() - base--;
                        if (source == target && i-- == 0)
                            return false;
                        final long v = (source << sourceShift) + target;
                        assert v == successors.successor(v) : v + " != " + successors.successor(v);
                        edge = (int) successors.successorIndexUnsafe(v);
                        assert graph.getEdgeSource(edge).longValue() == source;
                        assert graph.getEdgeTarget(edge).longValue() == target;
                    }
                    return edge != -1;
                }

                @Override
                public int nextInt()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    final int result = edge;
                    edge = -1;
                    return result;
                }
            };
        }

        @Override
        public Iterable<Integer> incomingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }

        @Override
        public Iterable<Integer> outgoingEdgesOf(final Integer vertex)
        {
            return edgesOf(vertex);
        }
    }

    private final SuccinctGraphIterables iterables = new SuccinctGraphIterables(this);

    @Override
    public GraphIterables<Integer, Integer> iterables()
    {
        return iterables;
    }
}
