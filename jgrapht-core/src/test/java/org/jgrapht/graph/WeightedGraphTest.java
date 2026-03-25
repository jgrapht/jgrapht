/*
 * (C) Copyright 2017-2023, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testInvalidEdgeOnWeightedGraph()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            WeightedPseudograph<Integer, String> g =
                new WeightedPseudograph<Integer, String>(String.class);
            g.getEdgeWeight("1-2");
        });
    }

    @Test
    public void testInvalidEdgeOnWeightedGraphSet()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            WeightedPseudograph<Integer, String> g =
                new WeightedPseudograph<Integer, String>(String.class);
            g.setEdgeWeight("1-2", 2d);
        });
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
        assertEquals(1, g.getEdgeSource(e));
        assertEquals(2, g.getEdgeTarget(e));
    }

    @Test
    public void testCloneWithCustomEdgeType()
    {
        // from https://github.com/jgrapht/jgrapht/issues/1233
        DirectedWeightedMultigraph<Integer, String> graph1 =
            new DirectedWeightedMultigraph<>(String.class);
        graph1.addVertex(1);
        graph1.addVertex(2);
        graph1.addVertex(3);
        String edge1 = "Edge1";
        graph1.addEdge(1,2, edge1);

        DirectedWeightedMultigraph<Integer, String> graph2 =
            (DirectedWeightedMultigraph<Integer, String>) graph1.clone();
        assertEquals(graph1, graph2);

        graph1.setEdgeWeight(edge1, 15.0);
        DirectedWeightedMultigraph<Integer, String> graph3 =
            (DirectedWeightedMultigraph<Integer, String>) graph1.clone();
        assertEquals(graph1, graph3);        
    }
}
