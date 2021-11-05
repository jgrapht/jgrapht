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

/**
 * An abstract base class for all succinct directed implementations.
 *
 * <p>
 * Two subclasses, {@link CumulativeSuccessors} and {@link CumulativeDegrees}, generate the monotone
 * lists that will be encoded using the Elias&ndash;Fano representation.
 *
 * <p>
 * First, we store the monotone lists of cumulative outdegrees and indegrees.
 *
 * <p>
 * Then, we store the outgoing edges <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> as a monotone
 * sequence using the encoding <var>x</var>2<sup>&lceil;log&nbsp;<var>n</var>&rceil;</sup> +
 * <var>y</var>. At that point the <var>k</var>-th edge can be obtained by retrieving the
 * <var>k</var>-th element of the sequence and some bit shifting (the encoding
 * <var>x</var><var>n</var> + <var>y</var> would be slightly more compact, but much slower to
 * decode). Since we know the list of cumulative outdegrees, we know which range of indices
 * corresponds to the edges outgoing from each vertex. If we need to know whether
 * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> is an edge we just look for
 * <var>x</var>2<sup>&lceil;log&nbsp;<var>n</var>&rceil;</sup> + <var>y</var> in the sequence.
 *
 * <p>
 * Finally, we store incoming edges <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> again as a monotone
 * sequence using the encoding <var>x</var><var>n</var> + <var>y</var> - <var>e</var>, where
 * <var>e</var> is the index of the edge in lexicographical order. In this case we just need to be
 * able to recover the edges associated with a vertex, so we can use a more compact format.
 *
 * <p>
 * However, in the case of a {@link SuccinctIntDirectedGraph} after retrieving the source and target
 * of an incoming edge we need to index it. The slow indexing of the incoming edges is the reason
 * why a {@link SuccinctIntDirectedGraph} enumerates incoming edges very slowly, whereas a
 * {@link SuccinctDirectedGraph} does not.
 *
 * @param <E> the graph edge type
 */

public abstract class AbstractSuccinctDirectedGraph<E>
    extends
    AbstractSuccinctGraph<E>
{
    private static final long serialVersionUID = 0L;

    public AbstractSuccinctDirectedGraph(final int n, final int m)
    {
        super(n, m);
    }

    /**
     * Turns all edges <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> into a monotone sequence using the
     * encoding <var>x</var>2<sup>&lceil;log&nbsp;<var>n</var>&rceil;</sup> + <var>y</var>, or the
     * encoding <var>x</var><var>n</var> + <var>y</var> - <var>e</var>, where <var>e</var> is the
     * index of the edge in lexicographical order, depending on the value of the {@code strict}
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
        private final boolean strict;

        private int x = -1, d, i, e;
        private long next = -1;
        private int[] s = IntArrays.EMPTY_ARRAY;

        protected CumulativeSuccessors(
            final Graph<Integer, E> graph, final Function<Integer, Iterable<E>> succ,
            final boolean strict)
        {
            this.n = graph.iterables().vertexCount();
            this.sourceShift = Fast.ceilLog2(n);
            this.graph = graph;
            this.succ = succ;
            this.strict = strict;
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
                    s = IntArrays.grow(s, d + 1);
                    s[d++] = Graphs.getOppositeVertex(graph, e, x);
                }
                Arrays.sort(s, 0, d);
                this.d = d;
                i = 0;
            }
            // The predecessor list will not be indexed, so we can gain a few bits of space by
            // subtracting the edge position in the list
            next = strict ? s[i] + ((long) x << sourceShift) : s[i] + x * n - e++;
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
     * Iterates over the cumulative degrees (starts with a zero).
     */
    protected final static class CumulativeDegrees
        implements
        LongIterator
    {
        private final Function<Integer, Integer> degreeOf;
        private final int n;
        private int i = -1;
        private long cumul = 0;

        protected CumulativeDegrees(final int n, final Function<Integer, Integer> degreeOf)
        {
            this.n = n;
            this.degreeOf = degreeOf;
        }

        @Override
        public boolean hasNext()
        {
            return i < n;
        }

        @Override
        public long nextLong()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            if (i == -1)
                return ++i;
            return cumul += degreeOf.apply(i++);
        }
    }

    @Override
    public int degreeOf(final Integer vertex)
    {
        return inDegreeOf(vertex) + outDegreeOf(vertex);
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .directed().weighted(false).modifiable(false).allowMultipleEdges(false)
            .allowSelfLoops(true).build();
    }
}
