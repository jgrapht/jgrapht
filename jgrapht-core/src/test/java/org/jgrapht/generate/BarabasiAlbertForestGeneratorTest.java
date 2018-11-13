/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link BarabasiAlbertForestGenerator}.
 *
 * @author Alexandru Valeanu
 */
public class BarabasiAlbertForestGeneratorTest
{

    @Test
    public void testBadParameters()
    {
        try {
            new BarabasiAlbertForestGenerator<>(0, 10, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new BarabasiAlbertForestGenerator<>(-1, 10, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new BarabasiAlbertForestGenerator<>(10, 9, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testUndirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(5, 20, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
        assertEquals(5, new ConnectivityInspector<>(g).connectedSets().size());
    }

    @Test
    public void testNoAdditionalNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(20, 20);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
        assertEquals(20, new ConnectivityInspector<>(g).connectedSets().size());
    }

    @Test
    public void testUndirectedWithOneInitialNode()
    {
        final long seed = 7;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(1, 20, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
        assertEquals(1, new ConnectivityInspector<>(g).connectedSets().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(2, 10, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(10, g.vertexSet().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectedWithOneInitialNode()
    {
        final long seed = 13;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(2, 20, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUndirectedWithGraphWhichAlreadyHasSomeVertices()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertForestGenerator<>(3, 10, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        g.addVertex(1000);

        gen.generateGraph(g);

        assertEquals(10, g.vertexSet().size());
    }

    @Test
    public void testRandomTrees()
    {
        Random random = new Random(0x88);

        final int NUM_TESTS = 10_000;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(100);
            final int T = 1 + random.nextInt(N);

            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                new BarabasiAlbertForestGenerator<>(T, N);
            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);

            assertEquals(N, g.vertexSet().size());
            assertEquals(T, new ConnectivityInspector<>(g).connectedSets().size());
        }
    }
}
