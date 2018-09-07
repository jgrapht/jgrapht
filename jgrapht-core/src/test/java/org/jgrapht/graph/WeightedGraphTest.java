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
package org.jgrapht.graph;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for different edge types on weighted graphs.
 * 
 * @author Dimitrios Michail
 */
public class WeightedGraphTest
{
    @Test
    public void testDefaultWeightedEdge()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        DefaultWeightedEdge e = g.addEdge(1, 2);
        assertEquals(g.getEdgeWeight(e), 1d, 1e-9);
        g.setEdgeWeight(e, 3d);
        assertEquals(g.getEdgeWeight(e), 3d, 1e-9);
    }

    @Test
    public void testStringAsWeightedEdge()
    {
        WeightedPseudograph<Integer, String> g =
            new WeightedPseudograph<Integer, String>(String.class);
        g.addVertex(1);
        g.addVertex(2);
        assertTrue(g.addEdge(1, 2, "1-2"));
        assertEquals(g.getEdgeWeight("1-2"), 1d, 1e-9);
        g.setEdgeWeight("1-2", 3d);
        assertEquals(g.getEdgeWeight("1-2"), 3d, 1e-9);
        assertTrue(g.containsEdge("1-2"));
        g.removeEdge("1-2");
        assertFalse(g.containsEdge("1-2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEdgeOnWeightedGraph()
    {
        WeightedPseudograph<Integer, String> g =
            new WeightedPseudograph<Integer, String>(String.class);
        g.getEdgeWeight("1-2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEdgeOnWeightedGraphSet()
    {
        WeightedPseudograph<Integer, String> g =
            new WeightedPseudograph<Integer, String>(String.class);
        g.setEdgeWeight("1-2", 2d);
    }

    public void testInvalidEdgeOnUnweightedGraph()
    {
        Pseudograph<Integer, String> g = new Pseudograph<Integer, String>(String.class);
        assertEquals(1d, g.getEdgeWeight("1-2"), 1e-9);
    }

    @Test
    public void testDefaultEdgeOnWeightedGraphs()
    {
        WeightedPseudograph<Integer, DefaultEdge> g =
            new WeightedPseudograph<Integer, DefaultEdge>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        DefaultEdge e = g.addEdge(1, 2);
        assertEquals(g.getEdgeWeight(e), 1d, 1e-9);
        g.setEdgeWeight(e, 3d);
        assertEquals(g.getEdgeWeight(e), 3d, 1e-9);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(e));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(e));
    }

}
