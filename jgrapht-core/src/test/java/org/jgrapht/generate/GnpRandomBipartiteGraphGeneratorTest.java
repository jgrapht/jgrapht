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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author Dimitrios Michail
 */
public class GnpRandomBipartiteGraphGeneratorTest
{
    private static final long SEED = 5;

    @Test
    public void testZeroNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(0, 0, 0.5);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testBadParameters()
    {
        try {
            new GnpRandomBipartiteGraphGenerator<>(-1, 0, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnpRandomBipartiteGraphGenerator<>(10, -3, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnpRandomBipartiteGraphGenerator<>(10, 10, -1.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnpRandomBipartiteGraphGenerator<>(10, 10, 2.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDirectedGraphGnp1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 0.5, SEED);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        int[][] edges = { { 5, 1 }, { 1, 6 }, { 6, 1 }, { 1, 7 }, { 1, 8 }, { 2, 5 }, { 6, 2 },
            { 7, 2 }, { 2, 8 }, { 5, 3 }, { 3, 6 }, { 7, 3 }, { 3, 8 }, { 4, 5 }, { 5, 4 },
            { 4, 6 }, { 6, 4 }, { 4, 7 }, { 4, 8 }, { 8, 4 } };

        assertEquals(4 + 4, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testDirectedGraphGnp2()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 1.0, SEED);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(4 + 4, g.vertexSet().size());
        assertEquals(32, g.edgeSet().size());
    }

    @Test
    public void testDirectedGraphGnp3()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 0.1, SEED);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        int[][] edges = { { 5, 1 }, { 7, 3 }, { 3, 8 }, { 8, 4 } };

        assertEquals(4 + 4, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnp1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 0.5, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        int[][] edges = { { 1, 6 }, { 1, 7 }, { 1, 8 }, { 2, 5 }, { 2, 7 }, { 3, 5 }, { 3, 8 },
            { 4, 6 }, { 4, 7 } };

        assertEquals(4 + 4, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnp2()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 1.0, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(4 + 4, g.vertexSet().size());
        assertEquals(4 * 4, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnp3()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomBipartiteGraphGenerator<>(4, 4, 0.0, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(4 + 4, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

}
