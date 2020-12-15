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

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.Transform;

public class ImmutableUndirectedGraphAdapterTest
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

        final ImmutableGraph g =
            ImmutableGraph
                .load(
                    ImmutableDirectedGraphAdapterTest
                        .storeTempGraph(Transform.symmetrize(m.immutableView())).toString());

        final ImmutableUndirectedGraphAdapter a = new ImmutableUndirectedGraphAdapter(g);

        assertEquals(g.numNodes(), a.vertexSet().size());
        for (int x = 0; x < g.numNodes(); x++) {
            final LazyIntIterator successors = g.successors(x);
            for (int y; (y = successors.nextInt()) != -1;)
                assertTrue(a.containsEdge(x, y));
        }

        assertFalse(a.containsEdge(null));

        assertEquals(6, a.iterables().edgeCount());
        assertEquals(6, a.edgeSet().size());
        assertNull(a.getEdge(2, 2));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));

        assertTrue(
            a.getEdgeSource(a.getEdge(0, 1)) == 0 && a.getEdgeTarget(a.getEdge(0, 1)) == 1
                || a.getEdgeSource(a.getEdge(0, 1)) == 1 && a.getEdgeTarget(a.getEdge(0, 1)) == 0);

        final ObjectOpenHashSet<IntIntSortedPair> edgesOf2 = new ObjectOpenHashSet<>(
            new IntIntSortedPair[] { IntIntSortedPair.of(2, 0), IntIntSortedPair.of(2, 3) });
        assertEquals(edgesOf2, a.edgesOf(2));
        assertEquals(edgesOf2, a.incomingEdgesOf(2));
        assertEquals(edgesOf2, a.outgoingEdgesOf(2));
        assertEquals(edgesOf2, new ObjectOpenHashSet<>(a.iterables().edgesOf(2).iterator()));
        assertEquals(
            edgesOf2,
            new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(2).iterator()));
        assertEquals(
            edgesOf2, new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(2).iterator()));

        final ObjectOpenHashSet<IntIntSortedPair> edgesOf1 = new ObjectOpenHashSet<>(
            new IntIntSortedPair[] { IntIntSortedPair.of(1, 0), IntIntSortedPair.of(1, 3),
                IntIntSortedPair.of(1, 1) });
        assertEquals(edgesOf1, a.edgesOf(1));
        assertEquals(edgesOf1, a.incomingEdgesOf(1));
        assertEquals(edgesOf1, a.outgoingEdgesOf(1));
        assertEquals(edgesOf1, new ObjectOpenHashSet<>(a.iterables().edgesOf(1).iterator()));
        assertEquals(
            edgesOf1, new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(1).iterator()));
        assertEquals(
            edgesOf1, new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(1).iterator()));

        assertEquals(Collections.singleton(IntIntSortedPair.of(0, 1)), a.getAllEdges(0, 1));
        assertEquals(
            Collections.singleton(IntIntSortedPair.of(0, 1)),
            new ObjectOpenHashSet<>(a.iterables().allEdges(0, 1).iterator()));

        assertEquals(4, a.degreeOf(1));
        assertEquals(3, a.inDegreeOf(1));
        assertEquals(3, a.outDegreeOf(1));
        assertEquals(4, a.iterables().degreeOf(1));
        assertEquals(3, a.iterables().inDegreeOf(1));
        assertEquals(3, a.iterables().outDegreeOf(1));
        assertEquals(2, a.degreeOf(2));
        assertEquals(2, a.inDegreeOf(2));
        assertEquals(2, a.outDegreeOf(2));
        assertEquals(2, a.iterables().degreeOf(2));
        assertEquals(2, a.iterables().inDegreeOf(2));
        assertEquals(2, a.iterables().outDegreeOf(2));
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
        final ImmutableGraph v = m.immutableView();

        final ImmutableGraph g = Transform.symmetrize(v);

        final ImmutableUndirectedGraphAdapter a = new ImmutableUndirectedGraphAdapter(g);
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
        final ImmutableGraph v = m.immutableView();

        final ImmutableGraph g = Transform.symmetrize(v);

        final ImmutableUndirectedGraphAdapter a = new ImmutableUndirectedGraphAdapter(g);
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
        SecurityException
    {
        final ArrayListMutableGraph m = new ArrayListMutableGraph();
        m.addNodes(100);
        for (int i = 0; i < 30; i++)
            m.addArc(0, i);
        final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
        final ImmutableUndirectedGraphAdapter a =
            new ImmutableUndirectedGraphAdapter(Transform.symmetrize(v));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(0, 1));
        assertEquals(IntIntPair.of(0, 1), a.getEdge(1, 0));
        assertEquals(null, a.getEdge(0, 50));
    }

    @Test
    public void testEdgeCoherence()
    {
        final ImmutableGraph m =
            new ArrayListMutableGraph(2, new int[][] { new int[] { 0, 1 }, new int[] { 1, 0 } })
                .immutableView();
        final ImmutableUndirectedGraphAdapter a = new ImmutableUndirectedGraphAdapter(m);

        assertEquals(a.getEdgeSource(a.getEdge(0, 1)), a.getEdgeSource(a.getEdge(1, 0)));
    }
}
