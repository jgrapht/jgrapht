/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Dimitrios Michail
 */
public class WattsStrogatzGraphGeneratorTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testLessThan3Nodes()
    {
        new WattsStrogatzGraphGenerator<>(2, 1, 0.5);
    }

    @Test
    public void testBadParameters()
    {
        try {
            new WattsStrogatzGraphGenerator<>(-1, 2, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 9, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 9, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(11, 11, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 2, -1.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 2, 2.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test4RegularNoRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularSomeRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.5, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularMoreRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.8, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularAddShortcutInsteadOfRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.5, true, new Random(seed));
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(6, g.vertexSet().size());
    }

    @Test
    public void test6RegularNoRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(12, 6, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(12, g.vertexSet().size());
        assertEquals(36, g.edgeSet().size());
    }

    @Test
    public void test6RegularSomeRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(12, 6, 0.7, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(12, g.vertexSet().size());
        assertEquals(36, g.edgeSet().size());
    }

    @Test
    public void test4RegularNoRewiringDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void testNonIntegerVertices()
    {
        final long seed = 5;

        GraphGenerator<String, DefaultEdge, String> gen =
            new WattsStrogatzGraphGenerator<>(10, 2, 0.1, seed);
        Graph<String, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(10, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
    }

}
