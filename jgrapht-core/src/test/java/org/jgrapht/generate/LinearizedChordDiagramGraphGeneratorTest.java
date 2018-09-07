/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for {@link LinearizedChordDiagramGraphGenerator}.
 * 
 * @author Dimitrios Michail
 */
public class LinearizedChordDiagramGraphGeneratorTest
{
    @Test
    public void testBadParameters()
    {
        try {
            new LinearizedChordDiagramGraphGenerator<>(0, 10);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new LinearizedChordDiagramGraphGenerator<>(-1, 10);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new LinearizedChordDiagramGraphGenerator<>(5, 0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new LinearizedChordDiagramGraphGenerator<>(5, -1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiGraph()
    {
        final long seed = 5;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(10, 2, seed);
        Graph<Integer, DefaultEdge> g = new Multigraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleGraph()
    {
        final long seed = 5;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(10, 2, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectedMultiGraph()
    {
        final long seed = 5;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(10, 2, seed);
        Graph<Integer, DefaultEdge> g = new DirectedMultigraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectedSimpleGraph()
    {
        final long seed = 5;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(10, 2, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);
    }

    @Test
    public void testUndirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(20, 1, seed);
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
    }

    @Test
    public void testUndirectedTwoEdges()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(20, 2, seed);
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
    }

    @Test
    public void testDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new LinearizedChordDiagramGraphGenerator<>(20, 1, seed);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(20, g.vertexSet().size());
    }

}
