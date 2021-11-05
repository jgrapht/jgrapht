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
public class GnmRandomBipartiteGraphGeneratorTest
{
    private static final long SEED = 5;

    @Test
    public void testZeroNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomBipartiteGraphGenerator<>(0, 0, 10);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testBadParameters()
    {
        try {
            new GnmRandomBipartiteGraphGenerator<>(-1, 10, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnmRandomBipartiteGraphGenerator<>(10, -1, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnmRandomBipartiteGraphGenerator<>(10, 10, -5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDirectedGraphGnm1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomBipartiteGraphGenerator<>(4, 4, 20, SEED);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        int[][] edges = { { 3, 5 }, { 6, 3 }, { 2, 8 }, { 7, 2 }, { 6, 2 }, { 4, 5 }, { 7, 4 },
            { 2, 5 }, { 6, 1 }, { 5, 1 }, { 2, 7 }, { 1, 7 }, { 2, 6 }, { 3, 6 }, { 1, 5 },
            { 7, 3 }, { 1, 8 }, { 8, 3 }, { 4, 7 }, { 4, 8 } };

        assertEquals(4 + 4, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnm1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomBipartiteGraphGenerator<>(4, 4, 10, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        int[][] edges = { { 3, 5 }, { 1, 7 }, { 2, 8 }, { 2, 6 }, { 3, 8 }, { 4, 8 }, { 1, 6 },
            { 4, 7 }, { 4, 6 }, { 4, 5 } };

        assertEquals(4 + 4, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testGnmEdgesLimit()
    {
        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                new GnmRandomBipartiteGraphGenerator<>(4, 4, 17, SEED);
            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);
            fail("More edges than permitted");
        } catch (IllegalArgumentException e) {
        }

        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                new GnmRandomBipartiteGraphGenerator<>(4, 4, 33, SEED);
            Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);
            fail("More edges than permitted");
        } catch (IllegalArgumentException e) {
        }
    }
}
