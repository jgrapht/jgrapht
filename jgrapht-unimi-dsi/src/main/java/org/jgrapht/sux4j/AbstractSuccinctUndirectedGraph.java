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

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultGraphType;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;

/**
 * An abstract base class for all succinct undirected implementations.
 *
 * <p>
 * Two subclasses, {@link CumulativeSuccessors} and {@link CumulativeDegrees}, generate the monotone
 * lists that will be encoded using the Elias&ndash;Fano representation.
 *
 * <p>
 * We use the representation described in {@link AbstractSuccinctDirectedGraph} applied to the
 * directed graph obtained by choosing the direction <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> for
 * an edge <var>x</var>&nbsp;&mdash;&nbsp;<var>y</var> if <var>x</var>&nbsp;&le;&nbsp;<var>y</var>
 * (loops are represented twice). Each edge now appears exactly once in the list of outgoing edges,
 * and thus can be indexed as in the directed base.
 *
 * <p>
 * The set of vertices adjacent to a given vertex can then be retrieved by enumerating both outgoing
 * and incoming edges, being careful to avoid enumerating twice loops.
 *
 * <p>
 * However, in the case of a {@link SuccinctIntUndirectedGraph} after retrieving the source and
 * target of an incoming edge we need to index it. The slow indexing of the incoming edges is the
 * reason why a {@link SuccinctIntUndirectedGraph} enumerates edges very slowly, whereas a
 * {@link SuccinctUndirectedGraph} does not.
 *
 * @param <E> the graph edge type
 */

public abstract class AbstractSuccinctUndirectedGraph<E>
    extends
    AbstractSuccinctGraph<E>
{
    private static final long serialVersionUID = 0L;

    public AbstractSuccinctUndirectedGraph(final int n, final int m)
    {
        super(n, m);
    }

    /**
     * Turns all edges <var>x</var>&nbsp;&mdash;&nbsp;<var>y</var>,
     * <var>x</var>&nbsp;&le;&nbsp;<var>y</var>, into a monotone sequence using the encoding
     * <var>x</var>2<sup>&lceil;log&nbsp;<var>n</var>&rceil;</sup> + <var>y</var>, or all edges
     * <var>x</var>&nbsp;&mdash;&nbsp;<var>y</var>, <var>x</var>&nbsp;&ge;&nbsp;<var>y</var>, using
     * the encoding <var>x</var><var>n</var> + <var>y</var> - <var>e</var>, where <var>e</var> is
     * the index of the edge in lexicographical order, depending on the value of the {@code sorted}
     * parameter.
     *
     * @param <E> the graph edge type
     */

    protected final static class CumulativeSuccessors<E>
        implements
        LongIterator
    {
        private final Graph<Integer, E> graph;
        private final long n;
        private final int sourceShift;
        private final Function<Integer, Iterable<E>> succ;
        private final boolean sorted;

        private int x = -1, d, i, e;
        private long next = -1;
        private int[] s = IntArrays.EMPTY_ARRAY;

        protected CumulativeSuccessors(
            final Graph<Integer, E> graph, final boolean sorted,
            final Function<Integer, Iterable<E>> succ)
        {
            this.n = (int) graph.iterables().vertexCount();
            this.sourceShift = Fast.ceilLog2(n);
            this.graph = graph;
            this.sorted = sorted;
            this.succ = succ;
        }

        @Override
        public boolean hasNext()
        {
            if (next != -1)
                return true;
            if (x == n)
                return false;
            while (i == d) {
                if (++x == n)
                    return false;
                int d = 0;
                for (final E e : succ.apply(x)) {
                    final int y = Graphs.getOppositeVertex(graph, e, x);
                    if (sorted) {
                        if (x <= y) {
                            s = IntArrays.grow(s, d + 1);
                            s[d++] = y;
                        }
                    } else {
                        if (x >= y) {
                            s = IntArrays.grow(s, d + 1);
                            s[d++] = y;
                        }
                    }
                }
                Arrays.sort(s, 0, d);
                this.d = d;
                i = 0;
            }
            // The predecessor list will not be indexed, so we can gain a few bits of space by
            // subtracting the edge position in the list
            next = sorted ? s[i] + ((long) x << sourceShift) : s[i] + x * n - e++;
            i++;
            return true;
        }

        @Override
        public long nextLong()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            final long result = next;
            next = -1;
            return result;
        }
    }

    /**
     * Iterates over the cumulative degrees (starts with a zero). Depending on the value of
     * {@code sorted}, only edges whose adjacent vertex is greater than or equal to the base vertex
     * (or vice versa) are included.
     *
     * @param <E> the graph edge type
     */
    protected final static class CumulativeDegrees<E>
        implements
        LongIterator
    {
        private final int n;
        private int x = -1;
        private long cumul = 0;
        private final Function<Integer, Iterable<E>> succ;
        private final boolean sorted;
        private final Graph<Integer, E> graph;

        protected CumulativeDegrees(
            final Graph<Integer, E> graph, final boolean sorted,
            final Function<Integer, Iterable<E>> succ)
        {
            this.n = (int) graph.iterables().vertexCount();
            this.graph = graph;
            this.succ = succ;
            this.sorted = sorted;
        }

        @Override
        public boolean hasNext()
        {
            return x < n;
        }

        @Override
        public long nextLong()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            if (x == -1)
                return ++x;
            int d = 0;
            if (sorted) {
                for (final E e : succ.apply(x))
                    if (x <= Graphs.getOppositeVertex(graph, e, x))
                        d++;
            } else {
                for (final E e : succ.apply(x))
                    if (x >= Graphs.getOppositeVertex(graph, e, x))
                        d++;
            }
            x++;
            return cumul += d;
        }
    }

    @Override
    public int inDegreeOf(final Integer vertex)
    {
        return degreeOf(vertex);
    }

    @Override
    public int outDegreeOf(final Integer vertex)
    {
        return degreeOf(vertex);
    }

    protected boolean containsEdge(
        final EliasFanoIndexedMonotoneLongBigList successors, int x, int y)
    {
        if (x > y) {
            final int t = x;
            x = y;
            y = t;
        }
        return successors.indexOfUnsafe(((long) x << sourceShift) + y) != -1;
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .directed().weighted(false).modifiable(false).allowMultipleEdges(false)
            .allowSelfLoops(true).build();
    }

}
