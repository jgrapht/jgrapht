/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * Tests for class {@link HierholzerEulerianCycle}.
 * 
 * @author Dimitrios Michail
 */
public class HierholzerEulerianCycleTest
{
    @Test
    public void testNullEulerian()
    {
        Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g1));

        Graph<Integer, DefaultEdge> g2 = new DirectedPseudograph<>(DefaultEdge.class);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g2));
    }

    @Test
    public void testEmptyEulerian()
    {
        Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
        g1.addVertex(1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g1));
        g1.addVertex(2);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g1));
        g1.addVertex(3);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g1));

        Graph<Integer, DefaultEdge> g2 = new DirectedPseudograph<>(DefaultEdge.class);
        g2.addVertex(1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g2));
        g2.addVertex(2);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g2));
        g2.addVertex(3);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g2));
    }

    @Test
    public void testUndirectedDisconnectedEulerian()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2);

        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedDisconnectedNonEulerian()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2);
        g.addEdge(5, 6);

        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testDirectedDisconnectedEulerian()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2);

        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testDirectedDisconnectedNonEulerian()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2);
        g.addEdge(5, 6);

        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian1()
    {
        // complete graph of 6 vertices
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(6);
        gen.generateGraph(g);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian2()
    {
        // even degrees but disconnected
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian3()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 4);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian4()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian5()
    {
        // with loops
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 4);
        IntStream.rangeClosed(1, 6).forEach(i -> g.addEdge(i, i));
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian6()
    {
        // with loops
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        IntStream.rangeClosed(1, 6).forEach(i -> g.addEdge(i, i));
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testUndirectedEulerian7()
    {
        // complete graph of 5 vertices
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(5);
        gen.generateGraph(g);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testDirectedEulerian1()
    {
        // complete graph of 6 vertices
        Graph<Integer, DefaultEdge> g1 = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> gen1 = new CompleteGraphGenerator<>(6);
        gen1.generateGraph(g1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g1));

        // complete graph of 7 vertices
        Graph<Integer, DefaultEdge> g2 = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> gen2 = new CompleteGraphGenerator<>(7);
        gen2.generateGraph(g2);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g2));
    }

    @Test
    public void testDirectedEulerian2()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1));
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        g.addEdge(1, 1);
        g.addEdge(1, 1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        Graphs.addAllVertices(g, Arrays.asList(2));
        g.addEdge(2, 1);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testDirectedEulerian3()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        g.addEdge(2, 1);
        g.addEdge(3, 2);
        g.addEdge(4, 3);
        g.addEdge(5, 4);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        g.addEdge(1, 1);
        g.addEdge(2, 2);
        g.addEdge(3, 3);
        g.addEdge(4, 4);
        g.addEdge(5, 5);
        Assert.assertFalse(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
        g.addEdge(1, 5);
        Assert.assertTrue(new HierholzerEulerianCycle<Integer, DefaultEdge>().isEulerian(g));
    }

    @Test
    public void testEmptyWithSingleVertexUndirected()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
        assertEulerian(cycle);
    }

    @Test
    public void testEmptyMultipleVerticesUndirected()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
        assertEulerian(cycle);
    }

    @Test
    public void testEmptyWithSingleVertexDirected()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
        assertEulerian(cycle);
    }

    @Test
    public void testEmptyMultipleVerticesDirected()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleUndirected1()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 4);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleUndirected2()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 4);
        g.addEdge(5, 7);
        g.addEdge(5, 7);
        g.addEdge(7, 8);
        g.addEdge(7, 8);
        g.addEdge(5, 8);
        g.addEdge(5, 8);
        g.addEdge(8, 8);
        g.addEdge(8, 8);
        g.addEdge(3, 3);
        g.addEdge(3, 3);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleUndirected3()
    {
        final long seed = 17;
        Random rng = new Random(seed);
        for (int size = 13; size < 52; size += 2) {

            Graph<Integer, DefaultEdge> g = new Pseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(size);
            gen.generateGraph(g);
            for (Integer v : g.vertexSet()) {
                IntStream.rangeClosed(0, rng.nextInt(10)).forEach(i -> g.addEdge(v, v));
            }
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                IntStream
                    .rangeClosed(0, 2 * rng.nextInt(10))
                    .forEach(i -> g.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e)));
            }

            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

            assertEulerian(cycle);
        }
    }

    @Test
    public void testEulerianCycleUndirected4()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 4);
        g.addEdge(5, 7);
        g.addEdge(5, 7);
        g.addEdge(7, 8);
        g.addEdge(7, 8);
        g.addEdge(5, 8);
        g.addEdge(5, 8);
        g.addEdge(8, 8);
        g.addEdge(8, 8);
        g.addEdge(3, 3);
        g.addEdge(3, 3);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testRandomUndirected()
    {
        final int tests = 100;
        final int size = 50;
        final double p = 0.7;
        Random rng = new Random();

        GnpRandomGraphGenerator<Integer, DefaultEdge> rgg =
            new GnpRandomGraphGenerator<>(size, p, rng, true);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultEdge> g = new Pseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            rgg.generateGraph(g);

            // add one extra copy for each edge
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                g.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
            }

            // randomly add more loops
            for (Integer v : g.vertexSet()) {
                IntStream.rangeClosed(0, rng.nextInt(10)).forEach(j -> g.addEdge(v, v));
            }

            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
            assertEulerian(cycle);
        }
    }

    @Test
    public void testRandomUndirectedFixedSeed()
    {
        final int tests = 100;
        final int size = 50;
        final long seed = 17;
        final double p = 0.7;

        GnpRandomGraphGenerator<Integer, DefaultEdge> rgg =
            new GnpRandomGraphGenerator<>(size, p, seed);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultEdge> g = new Pseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            rgg.generateGraph(g);
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                g.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
            }
            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
            assertEulerian(cycle);
        }
    }

    @Test
    public void testEulerianCycleUndirectedVertexList()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(4, 3, 2));
        DefaultEdge e42 = g.addEdge(4, 2);
        DefaultEdge e34 = g.addEdge(3, 4);
        DefaultEdge e32 = g.addEdge(3, 2);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEquals(e32, cycle.getEdgeList().get(0));
        assertEquals(e34, cycle.getEdgeList().get(1));
        assertEquals(e42, cycle.getEdgeList().get(2));

        List<Integer> vl = cycle.getVertexList();
        assertEquals(2, vl.get(0).intValue());
        assertEquals(3, vl.get(1).intValue());
        assertEquals(4, vl.get(2).intValue());
        assertEquals(2, vl.get(3).intValue());

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleDirected1()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(4, 3);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleDirected2()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(4, 3);
        g.addEdge(5, 7);
        g.addEdge(7, 8);
        g.addEdge(8, 5);
        g.addEdge(5, 7);
        g.addEdge(7, 8);
        g.addEdge(8, 5);
        g.addEdge(8, 8);
        g.addEdge(8, 8);
        g.addEdge(8, 8);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleDirected3()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(4, 2);
        g.addEdge(3, 6);
        g.addEdge(3, 5);
        g.addEdge(5, 3);
        g.addEdge(6, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 6);
        g.addEdge(8, 1);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleDirected4()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        g.addEdge(1, 2);
        g.addEdge(2, 4);
        g.addEdge(2, 3);
        g.addEdge(4, 2);
        g.addEdge(3, 5);
        g.addEdge(3, 6);
        g.addEdge(5, 3);
        g.addEdge(6, 7);
        g.addEdge(6, 8);
        g.addEdge(7, 6);
        g.addEdge(8, 1);

        assertEulerian(new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g));
    }

    @Test
    public void testEulerianCycleDirected5()
    {
        // even degrees
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 4);
        g.addEdge(3, 4);
        g.addEdge(4, 3);

        GraphPath<Integer, DefaultEdge> cycle =
            new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

        assertEulerian(cycle);
    }

    @Test
    public void testEulerianCycleDirected()
    {
        final long seed = 17;
        Random rng = new Random(seed);

        for (int size = 5; size < 52; size += 2) {
            Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(size);
            gen.generateGraph(g);
            for (Integer v : g.vertexSet()) {
                IntStream.rangeClosed(0, rng.nextInt(10)).forEach(i -> g.addEdge(v, v));
            }
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                IntStream.rangeClosed(0, 2 * rng.nextInt(10)).forEach(i -> {
                    g.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
                    g.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
                });
            }

            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);

            assertEulerian(cycle);
        }
    }

    @Test
    public void testRandomDirected()
    {
        final int tests = 100;
        final int size = 50;
        final double p = 0.7;
        Random rng = new Random();

        GnpRandomGraphGenerator<Integer, DefaultEdge> rgg =
            new GnpRandomGraphGenerator<>(size, p, rng, true);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            rgg.generateGraph(g);
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                g.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
            }

            // randomly add more loops
            for (Integer v : g.vertexSet()) {
                IntStream.rangeClosed(0, rng.nextInt(10)).forEach(j -> g.addEdge(v, v));
            }

            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
            assertEulerian(cycle);
        }
    }

    @Test
    public void testRandomDirectedFixedSeed()
    {
        final int tests = 100;
        final int size = 50;
        final long seed = 17;
        final double p = 0.7;

        GnpRandomGraphGenerator<Integer, DefaultEdge> rgg =
            new GnpRandomGraphGenerator<>(size, p, seed);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            rgg.generateGraph(g);
            List<DefaultEdge> edges = new ArrayList<>(g.edgeSet());
            for (DefaultEdge e : edges) {
                g.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
            }
            GraphPath<Integer, DefaultEdge> cycle =
                new HierholzerEulerianCycle<Integer, DefaultEdge>().getEulerianCycle(g);
            assertEulerian(cycle);
        }
    }

    @Test
    public void testPseudograph()
    {
        /*
         * Test for issue 388 on github.
         */
        Graph<Character, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList('A', 'B', 'C', 'D', 'E'));
        Graphs.addEdge(g, 'A', 'B', 8);
        Graphs.addEdge(g, 'A', 'C', 5);
        Graphs.addEdge(g, 'A', 'D', 6);
        Graphs.addEdge(g, 'B', 'C', 5);
        Graphs.addEdge(g, 'B', 'E', 6);
        Graphs.addEdge(g, 'C', 'D', 5);
        Graphs.addEdge(g, 'C', 'E', 5);
        Graphs.addEdge(g, 'D', 'E', 8);
        Graphs.addEdge(g, 'A', 'D', 8);
        Graphs.addEdge(g, 'B', 'E', 8);

        GraphPath<Character, DefaultWeightedEdge> gp =
            new HierholzerEulerianCycle<Character, DefaultWeightedEdge>().getEulerianCycle(g);
        assertEquals('E', gp.getStartVertex().charValue());
        assertEquals("[E, B, E, D, A, D, C, B, A, C, E]", gp.getVertexList().toString());
        assertEulerian(gp);
    }

    // assert that a cycle is Eulerian
    private static <V, E> void assertEulerian(GraphPath<V, E> cycle)
    {
        assertNotNull(cycle.getGraph());
        Graph<V, E> g = cycle.getGraph();
        assertTrue(GraphTests.isEulerian(g));

        if (g.vertexSet().isEmpty()) {
            // we do not consider the null-graph to be connected
            assertTrue(false);
        } else if (GraphTests.isEmpty(g)) {
            assertTrue(cycle.getStartVertex() == null);
            assertTrue(cycle.getEndVertex() == null);
            assertTrue(cycle.getEdgeList().isEmpty());
        } else {
            boolean isDirected = g.getType().isDirected();
            assertNotNull(cycle.getStartVertex());
            assertEquals(cycle.getStartVertex(), cycle.getEndVertex());
            assertEquals(g.edgeSet().size(), cycle.getLength());
            E prev = null;
            Iterator<E> it = cycle.getEdgeList().iterator();
            Set<E> dupCheck = new HashSet<>();
            while (it.hasNext()) {
                E cur = it.next();
                assertTrue(dupCheck.add(cur));
                if (prev != null) {
                    if (isDirected) {
                        assertTrue(g.getEdgeSource(cur).equals(g.getEdgeTarget(prev)));
                    } else {
                        assertTrue(
                            g.getEdgeSource(cur).equals(g.getEdgeSource(prev))
                                || g.getEdgeSource(cur).equals(g.getEdgeTarget(prev))
                                || g.getEdgeTarget(cur).equals(g.getEdgeSource(prev))
                                || g.getEdgeTarget(cur).equals(g.getEdgeTarget(prev)));
                    }
                }
                prev = cur;
            }
        }
    }

}
