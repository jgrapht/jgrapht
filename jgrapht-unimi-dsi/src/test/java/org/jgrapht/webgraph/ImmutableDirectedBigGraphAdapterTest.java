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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Iterables;

import it.unimi.dsi.big.webgraph.EFGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

public class ImmutableDirectedBigGraphAdapterTest
{
    @Test
    public void testSmallRandom()
    {
        for (final int size : new int[] { 10, 100, 500 }) {
            final it.unimi.dsi.webgraph.ImmutableGraph mg =
                new ArrayListMutableGraph(new ErdosRenyiGraph(size, .1, 0L, true)).immutableView();
            final ImmutableGraph g = ImmutableGraph.wrap(mg);
            final ImmutableDirectedBigGraphAdapter a = new ImmutableDirectedBigGraphAdapter(
                g, ImmutableGraph.wrap(Transform.transpose(mg)));

            assertEquals(g.numNodes(), a.vertexSet().size());
            assertEquals(g.numNodes(), a.iterables().vertexCount());
            assertEquals(g.numArcs(), a.iterables().edgeCount());
            // Test cached value
            assertEquals(g.numArcs(), a.iterables().edgeCount());

            for (long x = 0L; x < size; x++) {
                final LazyLongIterator successors = g.successors(x);
                for (long y; (y = successors.nextLong()) != -1L;)
                    assertTrue(a.containsEdge(x, y));
            }

            assertNull(a.getAllEdges(0L, -1L));
            assertNull(a.getAllEdges(-1L, -1L));
            assertNull(a.getAllEdges(-1L, 0L));
            assertNull(a.getAllEdges(0L, null));
            assertNull(a.getAllEdges(null, 0L));
            assertNull(a.getAllEdges(null, null));
        }
    }

    public static File storeTempGraph(final ImmutableGraph g)
        throws IOException,
        IllegalArgumentException,
        SecurityException
    {
        final File basename = File
            .createTempFile(ImmutableDirectedBigGraphAdapterTest.class.getSimpleName(), "test");
        EFGraph.store(g, basename.toString());
        basename.deleteOnExit();
        new File(basename + EFGraph.GRAPH_EXTENSION).deleteOnExit();
        new File(basename + EFGraph.PROPERTIES_EXTENSION).deleteOnExit();
        new File(basename + EFGraph.OFFSETS_EXTENSION).deleteOnExit();
        return basename;
    }

    @Test
    public void testSmall()
        throws IllegalArgumentException,
        SecurityException,
        IOException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(3);
        m.addArc(0, 1);
        m.addArc(0, 2);
        m.addArc(1, 2);
        m.addArc(2, 2);

        final it.unimi.dsi.webgraph.ImmutableGraph mg = m.immutableView();
        final ImmutableGraph g = ImmutableGraph.wrap(mg);
        final ImmutableGraph t = ImmutableGraph.wrap(Transform.transpose(mg));
        final File basename = storeTempGraph(g);
        final EFGraph ef = EFGraph.load(basename.toString());

        final ImmutableDirectedBigGraphAdapter a = new ImmutableDirectedBigGraphAdapter(g, t);
        final ImmutableDirectedBigGraphAdapter b = new ImmutableDirectedBigGraphAdapter(
            ef, ImmutableGraph.wrap(Transform.transpose(mg)));

        assertEquals(g.numNodes(), a.vertexSet().size());
        for (long x = 0; x < g.numNodes(); x++) {
            final LazyLongIterator successors = g.successors(x);
            for (long y; (y = successors.nextLong()) != -1L;)
                assertTrue(a.containsEdge(x, y));
        }

        assertNull(a.getVertexSupplier());
        assertNull(a.getEdgeSupplier());

        assertFalse(a.containsVertex(null));

        assertNull(a.getEdge(0L, -1L));
        assertNull(a.getEdge(-1L, -1L));
        assertNull(a.getEdge(-1L, 0L));
        assertNull(a.getEdge(0L, g.numNodes()));
        assertNull(a.getEdge(g.numNodes(), g.numNodes()));
        assertNull(a.getEdge(g.numNodes(), 0L));
        assertNull(a.getEdge(0L, null));
        assertNull(a.getEdge(null, 0L));
        assertNull(a.getEdge(null, null));
        assertNull(a.getEdge(1L, 0L));
        assertEquals(LongLongPair.of(0L, 1L), a.getEdge(0L, 1L));

        assertNull(a.getAllEdges(0L, -1L));
        assertNull(a.getAllEdges(-1L, -1L));
        assertNull(a.getAllEdges(-1L, 0L));
        assertNull(a.getAllEdges(0L, null));
        assertNull(a.getAllEdges(null, 0L));
        assertNull(a.getAllEdges(null, null));
        assertEquals(Collections.emptySet(), a.getAllEdges(1L, 0L));
        assertEquals(Collections.singleton(LongLongPair.of(0L, 1L)), a.getAllEdges(0L, 1L));
        assertEquals(
            Collections.singleton(LongLongPair.of(0L, 1L)),
            new ObjectOpenHashSet<>(a.iterables().allEdges(0L, 1L).iterator()));

        assertFalse(a.containsEdge(0L, null));
        assertFalse(a.containsEdge(null, 0L));
        assertTrue(a.containsEdge(0L, 1L));

        assertTrue(b.containsVertex(0L));
        assertFalse(b.containsVertex(3L));
        assertFalse(b.containsVertex(-1L));

        assertTrue(b.containsEdge(LongLongPair.of(0L, 2L)));
        assertTrue(b.containsEdge(LongLongPair.of(1L, 2L)));
        assertFalse(b.containsEdge(LongLongPair.of(2L, 1L)));
        assertFalse(b.containsEdge(null));
        assertFalse(a.containsEdge(LongLongSortedPair.of(0L, 2L)));

        assertEquals(2, a.degreeOf(1L));
        assertEquals(4, a.degreeOf(2L));
        assertEquals(1, a.inDegreeOf(1L));
        assertEquals(1, a.outDegreeOf(1L));
        assertEquals(2, a.iterables().degreeOf(1L));
        assertEquals(1, a.iterables().inDegreeOf(1L));
        assertEquals(1, a.iterables().outDegreeOf(1L));

        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 1L), LongLongPair.of(0L, 2L),
                    LongLongPair.of(1L, 2L), LongLongPair.of(2L, 2L) }),
            new ObjectOpenHashSet<>(a.edgeSet()));
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 1L), LongLongPair.of(0L, 2L),
                    LongLongPair.of(1L, 2L), LongLongPair.of(2L, 2L) }),
            new ObjectOpenHashSet<>(a.iterables().edges().iterator()));

        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 1L), LongLongPair.of(1L, 2L) }),
            a.edgesOf(1L));
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 1L), LongLongPair.of(1L, 2L) }),
            new ObjectOpenHashSet<>(a.iterables().edgesOf(1L).iterator()));

        assertEquals(3, Iterables.size(a.edgesOf(2L)));
        assertEquals(3, Iterables.size(a.iterables().edgesOf(2L)));

        assertEquals(
            new ObjectOpenHashSet<>(new LongLongPair[] { LongLongPair.of(0L, 1L) }),
            a.incomingEdgesOf(1L));
        assertEquals(
            new ObjectOpenHashSet<>(new LongLongPair[] { LongLongPair.of(0L, 1L) }),
            new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(1L).iterator()));

        assertEquals(
            new ObjectOpenHashSet<>(new LongLongPair[] { LongLongPair.of(1L, 2L) }),
            a.outgoingEdgesOf(1L));
        assertEquals(
            new ObjectOpenHashSet<>(new LongLongPair[] { LongLongPair.of(1L, 2L) }),
            new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(1L).iterator()));

        final Set<Long> v = a.vertexSet();
        assertTrue(v.contains(0L));
        assertFalse(v.contains(-1L));
        assertFalse(v.contains(3L));
        assertEquals(
            new LongOpenHashSet(v.iterator()), new LongOpenHashSet(new long[] { 0L, 1L, 2L }));

        assertEquals(1, a.getEdgeSource(LongLongPair.of(1L, 2L)).longValue());
        assertEquals(2, a.getEdgeTarget(LongLongPair.of(1L, 2L)).longValue());

        assertEquals(1, a.getEdgeWeight(LongLongPair.of(1L, 2L)), 0);
        a.setEdgeWeight(LongLongPair.of(0L, 1L), 1);

    }


    @Test
    public void testCopy()
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
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
        final ImmutableDirectedBigGraphAdapter a = new ImmutableDirectedBigGraphAdapter(
            ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
        assertEquals(a, a.copy());
    }

    @Test
    public void testType()
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
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
        final ImmutableDirectedBigGraphAdapter a = new ImmutableDirectedBigGraphAdapter(
            ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
        assertTrue(a.getType().isDirected());
        assertFalse(a.getType().isUndirected());
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 2L), LongLongPair.of(0L, 1L),
                    LongLongPair.of(1L, 3L), LongLongPair.of(2L, 3L) }),
            a.edgeSet());
        assertEquals(
            new ObjectOpenHashSet<>(
                new LongLongPair[] { LongLongPair.of(0L, 2L), LongLongPair.of(0L, 1L),
                    LongLongPair.of(1L, 3L), LongLongPair.of(2L, 3L) }),
            new ObjectOpenHashSet<>(a.edgeSet().iterator()));
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
        ImmutableDirectedBigGraphAdapter a = new ImmutableDirectedBigGraphAdapter(
            ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
        assertEquals(LongLongPair.of(0L, 1L), a.getEdge(0L, 1L));
        assertEquals(null, a.getEdge(1L, 0L));
        assertEquals(null, a.getEdge(0L, 50L));

        a = new ImmutableDirectedBigGraphAdapter(ImmutableGraph.wrap(v));
        assertEquals(LongLongPair.of(0L, 1L), a.getEdge(0L, 1l));
        assertEquals(null, a.getEdge(1L, 0L));
        assertEquals(null, a.getEdge(0L, 50L));
    }
}
