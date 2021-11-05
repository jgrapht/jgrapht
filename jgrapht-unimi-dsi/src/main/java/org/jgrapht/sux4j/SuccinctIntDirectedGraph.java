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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.IncomingEdgesSupport;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.google.common.collect.Iterables;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

/**
 * An immutable directed graph with {@link Integer} edges represented using quasi-succinct data
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
 * be close to twice the information-theoretical lower bound (typically, a few times smaller than a
 * {@link SparseIntDirectedGraph}). If you {@link #SuccinctIntDirectedGraph(Graph, boolean) drop
 * support for incoming edges} the space will close to the information-theoretical lower bound .
 *
 * <p>
 * {@linkplain org.jgrapht.GraphIterables#outgoingEdgesOf(Object) Enumeration of outgoing edges} is
 * quite fast, but {@linkplain org.jgrapht.GraphIterables#incomingEdgesOf(Object) enumeration of
 * incoming edges} is very slow. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests}
 * are very fast and happen in almost constant time.
 *
 * <p>
 * {@link SuccinctDirectedGraph} is a much faster implementation with a similar footprint using
 * {@link IntIntPair} as edge type. Please read the {@linkplain org.jgrapht.sux4j class
 * documentation} for more information.
 *
 * @author Sebastiano Vigna
 * @see SuccinctDirectedGraph
 */

public class SuccinctIntDirectedGraph
    extends
    AbstractSuccinctDirectedGraph<Integer>
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
    public <E> SuccinctIntDirectedGraph(
        final Graph<Integer, E> graph, final boolean incomingEdgesSupport)
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
    public <E> SuccinctIntDirectedGraph(final Graph<Integer, E> graph)
    {
        this(graph, true);
    }

    /**
     * Creates a new immutable succinct directed graph from an edge list, choosing whether to
     * support incoming edges.
     *
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctIntDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @param incomingEdgesSupport whether to support incoming edges or not.
     * @see #SuccinctIntDirectedGraph(Graph)
     */

    public SuccinctIntDirectedGraph(
        final int numVertices, final List<Pair<Integer, Integer>> edges,
        final boolean incomingEdgesSupport)
    {
        this(
            new SparseIntDirectedGraph(
                numVertices, edges, incomingEdgesSupport ? IncomingEdgesSupport.FULL_INCOMING_EDGES
                    : IncomingEdgesSupport.NO_INCOMING_EDGES),
            incomingEdgesSupport);
    }

    /**
     * Creates a new immutable succinct directed graph from an edge list, supporting both outgoing
     * and incoming edges.
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctIntDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param edges the edge list.
     * @see #SuccinctIntDirectedGraph(Graph)
     */

    public SuccinctIntDirectedGraph(final int numVertices, final List<Pair<Integer, Integer>> edges)
    {
        this(numVertices, edges, true);
    }

    /**
     * Creates a new immutable succinct directed graph from a supplier of streams of edges, choosing
     * whether to support incoming edges.
     *
     * <p>
     * This constructor just builds a {@link SparseIntDirectedGraph} and delegates to the
     * {@linkplain #SuccinctIntDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param numEdges the number of edges.
     * @param edges a supplier of streams of edges.
     * @param incomingEdgesSupport whether to support incoming edges or not.
     * @see #SuccinctIntDirectedGraph(Graph)
     */

    public SuccinctIntDirectedGraph(
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
     * {@linkplain #SuccinctIntDirectedGraph(Graph) main constructor}.
     *
     * @param numVertices the number of vertices.
     * @param numEdges the number of edges.
     * @param edges a supplier of streams of edges.
     * @see #SuccinctIntDirectedGraph(Graph)
     */

    public SuccinctIntDirectedGraph(
        final int numVertices, final int numEdges,
        final Supplier<Stream<Pair<Integer, Integer>>> edges)
    {
        this(numVertices, numEdges, edges, true);
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
    public IntSet edgesOf(final Integer vertex)
    {
        final IntSet result = outgoingEdgesOf(vertex);
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
    public IntSet incomingEdgesOf(final Integer target)
    {
        assertVertexExist(target);
        final int t = target;
        final long[] result = new long[2];
        cumulativeIndegrees.get(t, result);
        final int d = (int) (result[1] - result[0]);
        final LongBigListIterator iterator = predecessors.listIterator(result[0]);

        final IntOpenHashSet s = new IntOpenHashSet();
        long base = (long) n * t - result[0];

        for (int i = d; i-- != 0;) {
            final long source = iterator.nextLong() - base--;
            final int e = (int) (successors.successorIndexUnsafe((source << sourceShift) + t));
            assert getEdgeSource(e).longValue() == source;
            assert getEdgeTarget(e).longValue() == target;
            s.add(e);
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
    public IntSet outgoingEdgesOf(final Integer vertex)
    {
        assertVertexExist(vertex);
        final long[] result = new long[2];
        cumulativeOutdegrees.get(vertex, result);
        return IntSets.fromTo((int) result[0], (int) result[1]);
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
        final long index =
            successors.indexOfUnsafe(((long) sourceVertex << sourceShift) + targetVertex);
        return index != -1 ? (int) index : null;
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        return successors.indexOfUnsafe(((long) sourceVertex << sourceShift) + targetVertex) != -1;
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
        private final SuccinctIntDirectedGraph graph;

        private SuccinctGraphIterables()
        {
            graph = null;
        }

        private SuccinctGraphIterables(final SuccinctIntDirectedGraph graph)
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
            return Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source, true));
        }

        private Iterable<Integer> incomingEdgesOf(final int target, final boolean skipLoops)
        {
            final SuccinctIntDirectedGraph graph = this.graph;
            final long[] result = new long[2];
            graph.cumulativeIndegrees.get(target, result);
            final int d = (int) (result[1] - result[0]);
            final EliasFanoIndexedMonotoneLongBigList successors = graph.successors;
            final LongBigListIterator iterator = graph.predecessors.listIterator(result[0]);
            final int sourceShift = graph.sourceShift;

            return () -> new IntIterator()
            {
                int i = d;
                int edge = -1;
                long n = graph.n;
                long base = target * n - result[0];

                @Override
                public boolean hasNext()
                {
                    if (edge == -1 && i > 0) {
                        i--;
                        final long source = iterator.nextLong() - base--;
                        if (skipLoops && source == target && i-- != 0)
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
            return incomingEdgesOf(vertex, false);
        }
    }

    private final GraphIterables<Integer, Integer> iterables = new SuccinctGraphIterables(this);

    @Override
    public GraphIterables<Integer, Integer> iterables()
    {
        return iterables;
    }
}
