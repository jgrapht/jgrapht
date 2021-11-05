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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandomGenerator;

public class SuccinctDirectedGraphTest
{
    @Test
    public void testDirected()
    {
        final DefaultDirectedGraph<Integer, DefaultEdge> d =
            new DefaultDirectedGraph<>(new Supplier<Integer>()
            {
                private int id = 0;

                @Override
                public Integer get()
                {
                    return id++;
                }
            }, SupplierUtil.createDefaultEdgeSupplier(), false);
        for (int i = 0; i < 5; i++)
            d.addVertex(i);
        d.addEdge(0, 1);
        d.addEdge(0, 2);
        d.addEdge(1, 2);
        d.addEdge(2, 1);
        d.addEdge(2, 3);
        d.addEdge(3, 0);
        d.addEdge(3, 3);
        d.addEdge(3, 4);
        d.addEdge(4, 1);

        final SuccinctDirectedGraph s = new SuccinctDirectedGraph(d);
        assertEquals(2, s.outDegreeOf(0));
        assertEquals(1, s.outDegreeOf(1));
        assertEquals(2, s.outDegreeOf(2));
        assertEquals(3, s.outDegreeOf(3));
        assertEquals(1, s.outDegreeOf(4));
        assertEquals(1, s.inDegreeOf(0));
        assertEquals(3, s.inDegreeOf(1));
        assertEquals(2, s.inDegreeOf(2));
        assertEquals(2, s.inDegreeOf(3));
        assertEquals(1, s.inDegreeOf(4));

        assertEquals(IntIntPair.of(0, 1), s.getEdge(0, 1));
        assertEquals(IntIntPair.of(0, 2), s.getEdge(0, 2));
        assertEquals(IntIntPair.of(1, 2), s.getEdge(1, 2));
        assertEquals(IntIntPair.of(2, 1), s.getEdge(2, 1));
        assertEquals(IntIntPair.of(2, 3), s.getEdge(2, 3));
        assertEquals(IntIntPair.of(3, 0), s.getEdge(3, 0));
        assertEquals(IntIntPair.of(3, 3), s.getEdge(3, 3));
        assertEquals(IntIntPair.of(3, 4), s.getEdge(3, 4));
        assertEquals(IntIntPair.of(4, 1), s.getEdge(4, 1));

        assertNull(s.getEdge(0, 0));
        assertNull(s.getEdge(0, 3));
        assertNull(s.getEdge(0, 4));
        assertNull(s.getEdge(1, 0));
        assertNull(s.getEdge(1, 1));
        assertNull(s.getEdge(1, 3));
        assertNull(s.getEdge(1, 4));
        assertNull(s.getEdge(2, 0));
        assertNull(s.getEdge(2, 2));
        assertNull(s.getEdge(2, 4));
        assertNull(s.getEdge(3, 1));
        assertNull(s.getEdge(3, 2));
        assertNull(s.getEdge(4, 0));
        assertNull(s.getEdge(4, 4));
        assertNull(s.getEdge(4, 2));
        assertNull(s.getEdge(4, 3));

        assertTrue(s.containsEdge(0, 1));
        assertTrue(s.containsEdge(0, 2));
        assertTrue(s.containsEdge(1, 2));
        assertTrue(s.containsEdge(2, 1));
        assertTrue(s.containsEdge(2, 3));
        assertTrue(s.containsEdge(3, 0));
        assertTrue(s.containsEdge(3, 3));
        assertTrue(s.containsEdge(3, 4));
        assertTrue(s.containsEdge(4, 1));

        assertFalse(s.containsEdge(0, 0));
        assertFalse(s.containsEdge(0, 3));
        assertFalse(s.containsEdge(0, 4));
        assertFalse(s.containsEdge(1, 0));
        assertFalse(s.containsEdge(1, 1));
        assertFalse(s.containsEdge(1, 3));
        assertFalse(s.containsEdge(1, 4));
        assertFalse(s.containsEdge(2, 0));
        assertFalse(s.containsEdge(2, 2));
        assertFalse(s.containsEdge(2, 4));
        assertFalse(s.containsEdge(3, 1));
        assertFalse(s.containsEdge(3, 2));
        assertFalse(s.containsEdge(4, 0));
        assertFalse(s.containsEdge(4, 4));
        assertFalse(s.containsEdge(4, 2));
        assertFalse(s.containsEdge(4, 3));

        assertEquals(Set.of(IntIntPair.of(0, 1), IntIntPair.of(0, 2)), s.outgoingEdgesOf(0));
        assertEquals(Set.of(IntIntPair.of(1, 2)), s.outgoingEdgesOf(1));
        assertEquals(Set.of(IntIntPair.of(2, 1), IntIntPair.of(2, 3)), s.outgoingEdgesOf(2));
        assertEquals(
            Set.of(IntIntPair.of(3, 0), IntIntPair.of(3, 3), IntIntPair.of(3, 4)),
            s.outgoingEdgesOf(3));
        assertEquals(Set.of(IntIntPair.of(4, 1)), s.outgoingEdgesOf(4));

        assertEquals(Set.of(IntIntPair.of(3, 0)), s.incomingEdgesOf(0));
        assertEquals(
            Set.of(IntIntPair.of(0, 1), IntIntPair.of(2, 1), IntIntPair.of(4, 1)),
            s.incomingEdgesOf(1));
        assertEquals(Set.of(IntIntPair.of(0, 2), IntIntPair.of(1, 2)), s.incomingEdgesOf(2));
        assertEquals(Set.of(IntIntPair.of(2, 3), IntIntPair.of(3, 3)), s.incomingEdgesOf(3));
        assertEquals(Set.of(IntIntPair.of(3, 4)), s.incomingEdgesOf(4));

        assertEquals(
            Set.of(IntIntPair.of(3, 0)),
            new ObjectOpenHashSet<>(s.iterables().incomingEdgesOf(0).iterator()));
        assertEquals(
            Set.of(IntIntPair.of(0, 1), IntIntPair.of(2, 1), IntIntPair.of(4, 1)),
            new ObjectOpenHashSet<>(s.iterables().incomingEdgesOf(1).iterator()));
        assertEquals(
            Set.of(IntIntPair.of(0, 2), IntIntPair.of(1, 2)),
            new ObjectOpenHashSet<>(s.iterables().incomingEdgesOf(2).iterator()));
        assertEquals(
            Set.of(IntIntPair.of(2, 3), IntIntPair.of(3, 3)),
            new ObjectOpenHashSet<>(s.iterables().incomingEdgesOf(3).iterator()));
        assertEquals(
            Set.of(IntIntPair.of(3, 4)),
            new ObjectOpenHashSet<>(s.iterables().incomingEdgesOf(4).iterator()));

        Iterator<IntIntPair> iterator = s.iterables().edgesOf(0).iterator();
        while (iterator.hasNext())
            iterator.next();
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        iterator = s.iterables().outgoingEdgesOf(0).iterator();
        while (iterator.hasNext())
            iterator.next();
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        iterator = s.iterables().incomingEdgesOf(0).iterator();
        while (iterator.hasNext())
            iterator.next();
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSink()
    {
        final DefaultDirectedGraph<Integer, DefaultEdge> d =
            new DefaultDirectedGraph<>(new Supplier<Integer>()
            {
                private int id = 0;

                @Override
                public Integer get()
                {
                    return id++;
                }
            }, SupplierUtil.createDefaultEdgeSupplier(), false);
        for (int i = 0; i < 3; i++)
            d.addVertex(i);
        d.addEdge(0, 1);
        d.addEdge(2, 0);
        final SuccinctDirectedGraph s = new SuccinctDirectedGraph(d);
        assertEquals(IntIntPair.of(0, 1), s.getEdge(0, 1));
        assertEquals(IntIntPair.of(2, 0), s.getEdge(2, 0));
    }

    @Test
    public void testRandomDense()
    {
        final GnpRandomGraphGenerator<Integer, DefaultEdge> r =
            new GnpRandomGraphGenerator<>(1000, .1, 0, false);
        final DefaultDirectedGraph<Integer, DefaultEdge> s =
            new DefaultDirectedGraph<>(new Supplier<Integer>()
            {
                private int id = 0;

                @Override
                public Integer get()
                {
                    return id++;
                }
            }, SupplierUtil.createDefaultEdgeSupplier(), false);
        r.generateGraph(s);
        final SuccinctDirectedGraph t = new SuccinctDirectedGraph(s);
        for (final IntIntPair e : t.edgeSet())
            assertTrue(e.toString(), s.containsEdge(t.getEdgeSource(e), t.getEdgeTarget(e)));
        for (final DefaultEdge e : s.edgeSet())
            assertTrue(e.toString(), t.containsEdge(s.getEdgeSource(e), s.getEdgeTarget(e)));
        final XoRoShiRo128PlusPlusRandomGenerator random =
            new XoRoShiRo128PlusPlusRandomGenerator();
        final int n = (int) s.iterables().vertexCount();
        for (int i = 0; i < 10000; i++) {
            final int x = random.nextInt(n);
            final int y = random.nextInt(n);
            assertEquals(s.containsEdge(x, y), t.containsEdge(x, y));
        }
    }

    @Test
    public void testRandomSparse()
    {
        final GnpRandomGraphGenerator<Integer, DefaultEdge> r =
            new GnpRandomGraphGenerator<>(1000, .001, 0, false);
        final DefaultDirectedGraph<Integer, DefaultEdge> s =
            new DefaultDirectedGraph<>(new Supplier<Integer>()
            {
                private int id = 0;

                @Override
                public Integer get()
                {
                    return id++;
                }
            }, SupplierUtil.createDefaultEdgeSupplier(), false);
        r.generateGraph(s);
        final SuccinctDirectedGraph t = new SuccinctDirectedGraph(s);
        for (final IntIntPair e : t.edgeSet())
            assertTrue(e.toString(), s.containsEdge(t.getEdgeSource(e), t.getEdgeTarget(e)));
        for (final DefaultEdge e : s.edgeSet())
            assertTrue(e.toString(), t.containsEdge(s.getEdgeSource(e), s.getEdgeTarget(e)));
        final XoRoShiRo128PlusPlusRandomGenerator random =
            new XoRoShiRo128PlusPlusRandomGenerator();
        final int n = (int) s.iterables().vertexCount();
        for (int i = 0; i < 10000; i++) {
            final int x = random.nextInt(n);
            final int y = random.nextInt(n);
            assertEquals(s.containsEdge(x, y), t.containsEdge(x, y));
        }
    }

    @Test
    public void testOutgoingOnly()
    {
        final List<Pair<Integer, Integer>> edges =
            List.of(Pair.of(0, 1), Pair.of(0, 2), Pair.of(1, 2), Pair.of(2, 1));
        final SuccinctDirectedGraph s = new SuccinctDirectedGraph(3, edges, false);
        assertEquals(2, s.outDegreeOf(0));
    }
}
