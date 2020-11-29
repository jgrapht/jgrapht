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

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

public class ImmutableGraphAdapterTest
{
    @Test
    public void testSmallRandom()
    {
        for (final int size : new int[] { 10, 100, 500 }) {
            final ImmutableGraph g =
                new ArrayListMutableGraph(new ErdosRenyiGraph(size, .1, 0, true)).immutableView();
            final ImmutableGraphAdapter a =
                ImmutableGraphAdapter.directed(g, Transform.transpose(g));

            assertEquals(g.numNodes(), a.vertexSet().size());
            assertEquals(g.numNodes(), a.iterables().vertexCount());
            assertEquals(g.numArcs(), a.iterables().edgeCount());
            for (int x = 0; x < size; x++) {
                final LazyIntIterator successors = g.successors(x);
                for (int y; (y = successors.nextInt()) != -1;)
                    assertTrue(a.containsEdge(x, y));
            }

            assertNull(a.getAllEdges(0, -1));
            assertNull(a.getAllEdges(-1, -1));
            assertNull(a.getAllEdges(-1, 0));
            assertNull(a.getAllEdges(0, null));
            assertNull(a.getAllEdges(null, 0));
            assertNull(a.getAllEdges(null, null));
        }
    }

    public static File storeTempGraph(final ImmutableGraph g)
        throws IOException,
        IllegalArgumentException,
        SecurityException
    {
        final File basename = File
            .createTempFile(ImmutableGraphAdapterTest.class.getSimpleName(), "test");
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

        final ImmutableGraph g = m.immutableView();
        final ImmutableGraph t = Transform.transpose(g);
        final File basename = storeTempGraph(g);
        final EFGraph ef = EFGraph.load(basename.toString());

        /*
         * Assertions.assertThrows(IllegalArgumentException.class, () -> {
         *
         * @SuppressWarnings("unused") final ImmutableGraphAdapter dummy =
         * ImmutableGraphAdapter.directed(g, new ArrayListMutableGraph().immutableView()); });
         */

        final ImmutableGraphAdapter a = ImmutableGraphAdapter.directed(g, t);
        final ImmutableGraphAdapter b = ImmutableGraphAdapter.directed(ef, Transform.transpose(ef));

        assertEquals(g.numNodes(), a.vertexSet().size());
        for (int x = 0; x < g.numNodes(); x++) {
            final LazyIntIterator successors = g.successors(x);
            for (int y; (y = successors.nextInt()) != -1;)
                assertTrue(a.containsEdge(x, y));
        }

        assertNull(a.getVertexSupplier());
        assertNull(a.getEdgeSupplier());

        assertFalse(a.containsVertex(null));

        assertNull(a.getEdge(0, -1));
        assertNull(a.getEdge(-1, -1));
        assertNull(a.getEdge(-1, 0));
        assertNull(a.getEdge(0, g.numNodes()));
        assertNull(a.getEdge(g.numNodes(), g.numNodes()));
        assertNull(a.getEdge(g.numNodes(), 0));
        assertNull(a.getEdge(0, null));
        assertNull(a.getEdge(null, 0));
        assertNull(a.getEdge(null, null));
        assertNull(a.getEdge(1, 0));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));

        assertNull(a.getAllEdges(0, -1));
        assertNull(a.getAllEdges(-1, -1));
        assertNull(a.getAllEdges(-1, 0));
        assertNull(a.getAllEdges(0, null));
        assertNull(a.getAllEdges(null, 0));
        assertNull(a.getAllEdges(null, null));
        assertEquals(Collections.emptySet(), a.getAllEdges(1, 0));
        assertEquals(Collections.singleton(IntIntPair.of(0, 1)), a.getAllEdges(0, 1));
        assertEquals(
            Collections.singleton(IntIntPair.of(0, 1)),
            new ObjectOpenHashSet<>(a.iterables().allEdges(0, 1).iterator()));

        assertFalse(a.containsEdge(0, null));
        assertFalse(a.containsEdge(null, 0));
        assertTrue(a.containsEdge(0, 1));

        assertTrue(b.containsVertex(0));
        assertFalse(b.containsVertex(3));
        assertFalse(b.containsVertex(-1));

        assertTrue(b.containsEdge(IntIntPair.of(0, 2)));
        assertTrue(b.containsEdge(IntIntPair.of(1, 2)));
        assertFalse(b.containsEdge(IntIntPair.of(2, 1)));
        assertFalse(b.containsEdge(null));

        assertEquals(2, a.degreeOf(1));
        assertEquals(4, a.degreeOf(2));
        assertEquals(1, a.inDegreeOf(1));
        assertEquals(1, a.outDegreeOf(1));
        assertEquals(2, a.iterables().degreeOf(1));
        assertEquals(1, a.iterables().inDegreeOf(1));
        assertEquals(1, a.iterables().outDegreeOf(1));

        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 1), IntIntPair.of(0, 2), IntIntPair.of(1, 2),
                    IntIntPair.of(2, 2) }),
            a.edgeSet());
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 1), IntIntPair.of(0, 2), IntIntPair.of(1, 2),
                    IntIntPair.of(2, 2) }),
            new ObjectOpenHashSet(a.iterables().edges().iterator()));

        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 1), IntIntPair.of(1, 2) }),
            a.edgesOf(1));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 1), IntIntPair.of(1, 2) }),
            new ObjectOpenHashSet<>(a.iterables().edgesOf(1).iterator()));

        assertEquals(3, Iterables.size(a.iterables().edgesOf(2)));

        assertEquals(
            new ObjectOpenHashSet(new IntIntPair[] { IntIntPair.of(0, 1) }),
            a.incomingEdgesOf(1));
        assertEquals(
            new ObjectOpenHashSet(new IntIntPair[] { IntIntPair.of(0, 1) }),
            new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(1).iterator()));

        assertEquals(
            new ObjectOpenHashSet(new IntIntPair[] { IntIntPair.of(1, 2) }),
            a.outgoingEdgesOf(1));
        assertEquals(
            new ObjectOpenHashSet(new IntIntPair[] { IntIntPair.of(1, 2) }),
            new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(1).iterator()));

        final Set<Integer> v = a.vertexSet();
        assertTrue(v.contains(0));
        assertFalse(v.contains(-1));
        assertFalse(v.contains(3));
        assertEquals(new IntOpenHashSet(v.iterator()), new IntOpenHashSet(new int[] { 0, 1, 2 }));
        assertEquals(1, a.getEdgeSource(IntIntPair.of(1, 2)).longValue());

        assertEquals(2, a.getEdgeTarget(IntIntPair.of(1, 2)).longValue());

        assertEquals(1.0, a.getEdgeWeight(IntIntPair.of(1, 2)), 0);
        a.setEdgeWeight(IntIntPair.of(0, 1), 1);

        /*
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addEdge(0, 1); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addEdge(0, 1,
         * IntIntPair.of(0, 1)); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.removeEdge(0, 1);
         * });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> {
         * a.removeEdge(IntIntPair.of(0, 1)); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> {
         * a.setEdgeWeight(IntIntPair.of(0, 1), 0); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addVertex(); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addVertex(0); });
         *
         * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.removeVertex(0);
         * });
         */
    }

    @Test
    public void testSmallUndirected()
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

        final ImmutableGraph g =
            ImmutableGraph.load(storeTempGraph(Transform.symmetrize(m.immutableView())).toString());

        final ImmutableGraphAdapter a = ImmutableGraphAdapter.undirected(g);

        assertEquals(g.numNodes(), a.vertexSet().size());
        for (int x = 0; x < g.numNodes(); x++) {
            final LazyIntIterator successors = g.successors(x);
            for (int y; (y = successors.nextInt()) != -1;)
                assertTrue(a.containsEdge(x, y));
        }

        assertEquals(6, a.iterables().edgeCount());
        assertEquals(6, a.edgeSet().size());
        assertNull(a.getEdge(2, 2));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));

        assertTrue(
            a.getEdgeSource(a.getEdge(0, 1)) == 0 && a.getEdgeTarget(a.getEdge(0, 1)) == 1
                || a.getEdgeSource(a.getEdge(0, 1)) == 1 && a.getEdgeTarget(a.getEdge(0, 1)) == 0);

        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(2, 0), IntIntSortedPair.of(2, 3) }),
            a.edgesOf(2));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(0, 2), IntIntSortedPair.of(3, 2) }),
            a.incomingEdgesOf(2));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(2, 0), IntIntSortedPair.of(2, 3) }),
            a.outgoingEdgesOf(2));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(2, 0), IntIntSortedPair.of(2, 3) }),
            new ObjectOpenHashSet<>(a.iterables().edgesOf(2).iterator()));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(0, 2), IntIntSortedPair.of(3, 2) }),
            new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(2).iterator()));
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(2, 0), IntIntSortedPair.of(2, 3) }),
            new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(2).iterator()));
        assertEquals(Collections.singleton(IntIntSortedPair.of(0, 1)), a.getAllEdges(0, 1));

        assertEquals(4, a.degreeOf(1));
        assertEquals(4, a.iterables().degreeOf(1));
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
        final ImmutableGraph v = m.immutableView();
        ImmutableGraphAdapter a = ImmutableGraphAdapter.directed(v, Transform.transpose(v));
        assertEquals(a, a.copy());

        final ImmutableGraph g = Transform.symmetrize(v);

        a = ImmutableGraphAdapter.undirected(g);
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
        final ImmutableGraph v = m.immutableView();
        ImmutableGraphAdapter a = ImmutableGraphAdapter.directed(v, Transform.transpose(v));
        assertTrue(a.getType().isDirected());
        assertFalse(a.getType().isUndirected());
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 2), IntIntPair.of(0, 1), IntIntPair.of(1, 3),
                    IntIntPair.of(2, 3) }),
            a.edgeSet());
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntPair[] { IntIntPair.of(0, 2), IntIntPair.of(0, 1), IntIntPair.of(1, 3),
                    IntIntPair.of(2, 3) }),
            new ObjectOpenHashSet<>(a.iterables().edges().iterator()));

        final ImmutableGraph g = Transform.symmetrize(v);

        a = ImmutableGraphAdapter.undirected(g);
        assertTrue(a.getType().isUndirected());
        assertFalse(a.getType().isDirected());
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(0, 2), IntIntSortedPair.of(0, 1),
                    IntIntSortedPair.of(1, 3), IntIntSortedPair.of(2, 3) }),
            a.edgeSet());
        assertEquals(
            new ObjectOpenHashSet<>(
                new IntIntSortedPair[] { IntIntSortedPair.of(0, 2), IntIntSortedPair.of(0, 1),
                    IntIntSortedPair.of(1, 3), IntIntSortedPair.of(2, 3) }),
            new ObjectOpenHashSet<>(a.iterables().edges().iterator()));
    }

    @Test
    public void testAdjacencyCheck()
        throws IllegalArgumentException,
        SecurityException,
        IOException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(100);
        for (int i = 0; i < 30; i++)
            m.addArc(0, i);
        final ImmutableGraph v = m.immutableView();
        ImmutableGraphAdapter a = ImmutableGraphAdapter.directed(v, Transform.transpose(v));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));
        assertEquals(null, a.getEdge(1, 0));
        assertEquals(null, a.getEdge(0, 50));

        a = ImmutableGraphAdapter.directed(v);
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));
        assertEquals(null, a.getEdge(1, 0));
        assertEquals(null, a.getEdge(0, 50));
    }

    @Test
    public void testEdgeCoherence()
    {
        final ImmutableGraph m =
            new ArrayListMutableGraph(2, new int[][] { new int[] { 0, 1 }, new int[] { 1, 0 } })
                .immutableView();
        final ImmutableGraphAdapter a = ImmutableGraphAdapter.undirected(m);

        assertEquals(a.getEdgeSource(a.getEdge(0, 1)), a.getEdgeSource(a.getEdge(1, 0)));
    }
}
