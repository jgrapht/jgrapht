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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.Transform;

public class ImmutableUndirectedBigGraphAdapterTest
{
    @Test
    public void testSmall()
        throws IllegalArgumentException,
        SecurityException,
        IOException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(4);
        m.addArc(0, 1);
        m.addArc(0, 2);
        m.addArc(1, 3);
        m.addArc(2, 3);
        m.addArc(1, 1);
        m.addArc(3, 3);

        final ImmutableGraph g = ImmutableGraph
            .load(
                ImmutableDirectedBigGraphAdapterTest
                    .storeTempGraph(ImmutableGraph.wrap(Transform.symmetrize(m.immutableView())))
                    .toString());

        final ImmutableUndirectedBigGraphAdapter a = new ImmutableUndirectedBigGraphAdapter(g);

        assertEquals(g.numNodes(), a.vertexSet().size());
        for (long x = 0; x < g.numNodes(); x++) {
            final LazyLongIterator successors = g.successors(x);
            for (long y; (y = successors.nextLong()) != -1;)
                assertTrue(a.containsEdge(x, y));
        }

        assertFalse(a.containsEdge(null));

        assertEquals(6, a.iterables().edgeCount());
        assertEquals(6, a.edgeSet().size());
        assertNull(a.getEdge(2L, 2L));
        assertEquals(LongLongSortedPair.of(0L, 1L), a.getEdge(0L, 1L));

        assertTrue(
            a.getEdgeSource(a.getEdge(0L, 1L)) == 0 && a.getEdgeTarget(a.getEdge(0L, 1L)) == 1
                || a.getEdgeSource(a.getEdge(0L, 1L)) == 1
                    && a.getEdgeTarget(a.getEdge(0L, 1L)) == 0);

        final ObjectOpenHashSet<LongLongSortedPair> edgesOf2 = new ObjectOpenHashSet<>(
            new LongLongSortedPair[] { LongLongSortedPair.of(2, 0), LongLongSortedPair.of(2, 3) });
        assertEquals(edgesOf2, a.edgesOf(2L));
        assertEquals(edgesOf2, a.incomingEdgesOf(2L));
        assertEquals(edgesOf2, a.outgoingEdgesOf(2L));
        assertEquals(
            edgesOf2, new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(2L).iterator()));
        assertEquals(
            edgesOf2, new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(2L).iterator()));

        final ObjectOpenHashSet<LongLongSortedPair> edgesOf1 = new ObjectOpenHashSet<>(
            new LongLongSortedPair[] { LongLongSortedPair.of(1, 0), LongLongSortedPair.of(1, 3),
                LongLongSortedPair.of(1, 1) });
        assertEquals(edgesOf1, a.edgesOf(1L));
        assertEquals(edgesOf1, a.incomingEdgesOf(1L));
        assertEquals(edgesOf1, a.outgoingEdgesOf(1L));
        assertEquals(edgesOf1, new ObjectOpenHashSet<>(a.iterables().edgesOf(1L).iterator()));
        assertEquals(
            edgesOf1, new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(1L).iterator()));
        assertEquals(
            edgesOf1, new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(1L).iterator()));

        assertEquals(Collections.singleton(LongLongSortedPair.of(0L, 1L)), a.getAllEdges(0L, 1L));
        assertEquals(
            Collections.singleton(LongLongSortedPair.of(0L, 1L)),
            new ObjectOpenHashSet<>(a.iterables().allEdges(0L, 1L).iterator()));

        assertEquals(4, a.degreeOf(1L));
        assertEquals(3, a.inDegreeOf(1L));
        assertEquals(3, a.outDegreeOf(1L));
        assertEquals(4, a.iterables().degreeOf(1L));
        assertEquals(3, a.iterables().inDegreeOf(1L));
        assertEquals(3, a.iterables().outDegreeOf(1L));
        assertEquals(2, a.degreeOf(2L));
        assertEquals(2, a.inDegreeOf(2L));
        assertEquals(2, a.outDegreeOf(2L));
        assertEquals(2, a.iterables().degreeOf(2L));
        assertEquals(2, a.iterables().inDegreeOf(2L));
        assertEquals(2, a.iterables().outDegreeOf(2L));
    }

    @Test
    public void testCopy()
        throws IllegalArgumentException,
        SecurityException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(4);
        m.addArc(0, 1);
        m.addArc(0, 2);
        m.addArc(1, 3);
        m.addArc(2, 3);
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();

        final it.unimi.dsi.webgraph.ImmutableGraph g = Transform.symmetrize(v);

        final ImmutableUndirectedBigGraphAdapter a =
            new ImmutableUndirectedBigGraphAdapter(ImmutableGraph.wrap(g));
        assertEquals(a, a.copy());
    }

    @Test
    public void testType()
        throws IllegalArgumentException,
        SecurityException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(4);
        m.addArc(0, 1);
        m.addArc(0, 2);
        m.addArc(1, 3);
        m.addArc(2, 3);
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();

        final it.unimi.dsi.webgraph.ImmutableGraph g = Transform.symmetrize(v);

        final ImmutableUndirectedBigGraphAdapter a =
            new ImmutableUndirectedBigGraphAdapter(ImmutableGraph.wrap(g));
        assertTrue(a.getType().isUndirected());
        assertFalse(a.getType().isDirected());
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongSortedPair.of(0L, 2L), LongLongSortedPair.of(0L, 1L),
                    LongLongSortedPair.of(1L, 3L), LongLongSortedPair.of(2L, 3L) }),
            a.edgeSet());
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongSortedPair.of(0L, 2L), LongLongSortedPair.of(0L, 1L),
                    LongLongSortedPair.of(1L, 3L), LongLongSortedPair.of(2L, 3L) }),
            new ObjectOpenHashSet<>(a.iterables().edges().iterator()));
    }

    @Test
    public void testAdjacencyCheck()
        throws IllegalArgumentException,
        SecurityException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(100);
        for (int i = 0; i < 30; i++)
            m.addArc(0, i);
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
        final ImmutableUndirectedBigGraphAdapter a = new ImmutableUndirectedBigGraphAdapter(
            ImmutableGraph.wrap(Transform.symmetrize(v)));
        assertEquals(LongLongPair.of(0L, 1L), a.getEdge(0L, 1L));
        assertEquals(LongLongPair.of(0L, 1L), a.getEdge(1L, 0L));
        assertEquals(null, a.getEdge(0L, 50L));
    }

    @Test
    public void testEdgeCoherence()
    {
        final ImmutableGraph m = ImmutableGraph
            .wrap(
                new ArrayListMutableGraph(2, new int[][] { new int[] { 0, 1 }, new int[] { 1, 0 } })
                    .immutableView());
        final ImmutableUndirectedBigGraphAdapter a =
            new ImmutableUndirectedBigGraphAdapter(m);

        assertEquals(a.getEdgeSource(a.getEdge(0L, 1L)), a.getEdgeSource(a.getEdge(1L, 0L)));
    }
}
