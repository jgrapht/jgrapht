/*
 * (C) Copyright 2018-2020, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class EsauWilliamsCapacitatedMinimumSpanningTreeTest
{

    /**
     * This example is presented here: http://www.pitt.edu/~dtipper/2110/CMST_example.pdf
     */
    @Test
    public void testInstance1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);

        for (int i = 0; i < 7; ++i) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 7; ++i) {
            for (int j = i + 1; j < 7; ++j) {
                graph.addEdge(i, j);
            }
        }
        graph.setEdgeWeight(graph.getEdge(0, 1), 5);
        graph.setEdgeWeight(graph.getEdge(0, 2), 6);
        graph.setEdgeWeight(graph.getEdge(0, 3), 9);
        graph.setEdgeWeight(graph.getEdge(0, 4), 10);
        graph.setEdgeWeight(graph.getEdge(0, 5), 11);
        graph.setEdgeWeight(graph.getEdge(0, 6), 15);
        graph.setEdgeWeight(graph.getEdge(1, 2), 9);
        graph.setEdgeWeight(graph.getEdge(1, 3), 6);
        graph.setEdgeWeight(graph.getEdge(1, 4), 6);
        graph.setEdgeWeight(graph.getEdge(1, 5), 8);
        graph.setEdgeWeight(graph.getEdge(1, 6), 17);
        graph.setEdgeWeight(graph.getEdge(2, 3), 7);
        graph.setEdgeWeight(graph.getEdge(2, 4), 9);
        graph.setEdgeWeight(graph.getEdge(2, 5), 8);
        graph.setEdgeWeight(graph.getEdge(2, 6), 12);
        graph.setEdgeWeight(graph.getEdge(3, 4), 10);
        graph.setEdgeWeight(graph.getEdge(3, 5), 5);
        graph.setEdgeWeight(graph.getEdge(3, 6), 11);
        graph.setEdgeWeight(graph.getEdge(4, 5), 14);
        graph.setEdgeWeight(graph.getEdge(4, 6), 9);
        graph.setEdgeWeight(graph.getEdge(5, 6), 8);

        Map<Integer, Double> weights = new HashMap<>();
        weights.put(1, 1.0);
        weights.put(2, 1.0);
        weights.put(3, 2.0);
        weights.put(4, 1.0);
        weights.put(5, 1.0);
        weights.put(6, 1.0);

        CapacitatedSpanningTreeAlgorithm.CapacitatedSpanningTree<Integer,
            DefaultWeightedEdge> cmst =
                new EsauWilliamsCapacitatedMinimumSpanningTree<>(graph, 0, 3, weights, 1)
                    .getCapacitatedSpanningTree();

        assertNotNull(cmst);
        assertEquals(42.0, cmst.getWeight(), 0.0000001);

        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(1)),
            Pair.of(new HashSet<>(Arrays.asList(1, 4)), 2.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(2)),
            Pair.of(new HashSet<>(Arrays.asList(2, 5, 6)), 3.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(3)),
            Pair.of(new HashSet<>(Collections.singletonList(3)), 2.0));

        assertEquals(cmst.getLabels().get(1), cmst.getLabels().get(4), 0);
        assertEquals(cmst.getLabels().get(2), cmst.getLabels().get(5), 0);
        assertEquals(cmst.getLabels().get(2), cmst.getLabels().get(6), 0);
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(2));
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(3));
        assertNotEquals(cmst.getLabels().get(2), cmst.getLabels().get(1));
        assertNotEquals(cmst.getLabels().get(2), cmst.getLabels().get(3));

        for (DefaultWeightedEdge e : cmst.getEdges()) {
            assertTrue(
                e == graph.getEdge(0, 1) || e == graph.getEdge(0, 2) || e == graph.getEdge(0, 3)
                    || e == graph.getEdge(1, 4) || e == graph.getEdge(2, 5)
                    || e == graph.getEdge(5, 6));
        }
    }

    /**
     * Instance calculated by hand
     */
    @Test
    public void testInstance2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);

        for (int i = 0; i < 6; ++i) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 6; ++i) {
            for (int j = i + 1; j < 6; ++j) {
                graph.addEdge(i, j);
            }
        }
        graph.setEdgeWeight(graph.getEdge(0, 1), 7);
        graph.setEdgeWeight(graph.getEdge(0, 2), 5);
        graph.setEdgeWeight(graph.getEdge(0, 3), 1);
        graph.setEdgeWeight(graph.getEdge(0, 4), 2);
        graph.setEdgeWeight(graph.getEdge(0, 5), 8);
        graph.setEdgeWeight(graph.getEdge(1, 2), 8);
        graph.setEdgeWeight(graph.getEdge(1, 3), 5);
        graph.setEdgeWeight(graph.getEdge(1, 4), 2);
        graph.setEdgeWeight(graph.getEdge(1, 5), 2);
        graph.setEdgeWeight(graph.getEdge(2, 3), 2);
        graph.setEdgeWeight(graph.getEdge(2, 4), 5);
        graph.setEdgeWeight(graph.getEdge(2, 5), 6);
        graph.setEdgeWeight(graph.getEdge(3, 4), 9);
        graph.setEdgeWeight(graph.getEdge(3, 5), 5);
        graph.setEdgeWeight(graph.getEdge(4, 5), 1);

        Map<Integer, Double> weights = new HashMap<>();
        weights.put(1, 2.0);
        weights.put(2, 1.0);
        weights.put(3, 2.0);
        weights.put(4, 3.0);
        weights.put(5, 2.0);

        CapacitatedSpanningTreeAlgorithm.CapacitatedSpanningTree<Integer,
            DefaultWeightedEdge> cmst =
                new EsauWilliamsCapacitatedMinimumSpanningTree<>(graph, 0, 4, weights, 1)
                    .getCapacitatedSpanningTree();

        assertNotNull(cmst);
        assertEquals(14.0, cmst.getWeight(), 0.0000001);

        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(1)),
            Pair.of(new HashSet<>(Arrays.asList(1, 5)), 4.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(2)),
            Pair.of(new HashSet<>(Arrays.asList(2, 3)), 3.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(4)),
            Pair.of(new HashSet<>(Collections.singletonList(4)), 3.0));

        assertEquals(cmst.getLabels().get(1), cmst.getLabels().get(5), 0);
        assertEquals(cmst.getLabels().get(2), cmst.getLabels().get(3), 0);
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(3));
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(4));
        assertNotEquals(cmst.getLabels().get(3), cmst.getLabels().get(4));

        for (DefaultWeightedEdge e : cmst.getEdges()) {
            assertTrue(
                e == graph.getEdge(0, 1) || e == graph.getEdge(0, 3) || e == graph.getEdge(0, 4)
                    || e == graph.getEdge(1, 5) || e == graph.getEdge(3, 2));
        }
    }

    /**
     * Instance as in testInstance2() but without edge (0,5). That is, an incomplete graph is
     * tested.
     */
    @Test
    public void testInstance3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);

        for (int i = 0; i < 6; ++i) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 6; ++i) {
            for (int j = i + 1; j < 6; ++j) {
                if (i != 0 || j != 5) {
                    graph.addEdge(i, j);
                }
            }
        }
        graph.setEdgeWeight(graph.getEdge(0, 1), 7);
        graph.setEdgeWeight(graph.getEdge(0, 2), 5);
        graph.setEdgeWeight(graph.getEdge(0, 3), 1);
        graph.setEdgeWeight(graph.getEdge(0, 4), 2);
        graph.setEdgeWeight(graph.getEdge(1, 2), 8);
        graph.setEdgeWeight(graph.getEdge(1, 3), 5);
        graph.setEdgeWeight(graph.getEdge(1, 4), 2);
        graph.setEdgeWeight(graph.getEdge(1, 5), 2);
        graph.setEdgeWeight(graph.getEdge(2, 3), 2);
        graph.setEdgeWeight(graph.getEdge(2, 4), 5);
        graph.setEdgeWeight(graph.getEdge(2, 5), 6);
        graph.setEdgeWeight(graph.getEdge(3, 4), 9);
        graph.setEdgeWeight(graph.getEdge(3, 5), 5);
        graph.setEdgeWeight(graph.getEdge(4, 5), 1);

        Map<Integer, Double> weights = new HashMap<>();
        weights.put(1, 2.0);
        weights.put(2, 1.0);
        weights.put(3, 2.0);
        weights.put(4, 3.0);
        weights.put(5, 2.0);

        CapacitatedSpanningTreeAlgorithm.CapacitatedSpanningTree<Integer,
            DefaultWeightedEdge> cmst =
                new EsauWilliamsCapacitatedMinimumSpanningTree<>(graph, 0, 4, weights, 1)
                    .getCapacitatedSpanningTree();

        assertNotNull(cmst);
        assertEquals(14.0, cmst.getWeight(), 0.0000001);

        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(1)),
            Pair.of(new HashSet<>(Arrays.asList(1, 5)), 4.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(2)),
            Pair.of(new HashSet<>(Arrays.asList(2, 3)), 3.0));
        assertEquals(
            cmst.getPartition().get(cmst.getLabels().get(4)),
            Pair.of(new HashSet<>(Collections.singletonList(4)), 3.0));

        assertEquals(cmst.getLabels().get(1), cmst.getLabels().get(5), 0);
        assertEquals(cmst.getLabels().get(2), cmst.getLabels().get(3), 0);
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(3));
        assertNotEquals(cmst.getLabels().get(1), cmst.getLabels().get(4));
        assertNotEquals(cmst.getLabels().get(3), cmst.getLabels().get(4));

        for (DefaultWeightedEdge e : cmst.getEdges()) {
            assertTrue(
                e == graph.getEdge(0, 1) || e == graph.getEdge(0, 3) || e == graph.getEdge(0, 4)
                    || e == graph.getEdge(1, 5) || e == graph.getEdge(3, 2));
        }
    }

    @Test
    public void testInstanceWithRandomness()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);

        for (int i = 0; i < 7; ++i) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 7; ++i) {
            for (int j = i + 1; j < 7; ++j) {
                graph.addEdge(i, j);
            }
        }
        graph.setEdgeWeight(graph.getEdge(0, 1), 5);
        graph.setEdgeWeight(graph.getEdge(0, 2), 6);
        graph.setEdgeWeight(graph.getEdge(0, 3), 9);
        graph.setEdgeWeight(graph.getEdge(0, 4), 10);
        graph.setEdgeWeight(graph.getEdge(0, 5), 11);
        graph.setEdgeWeight(graph.getEdge(0, 6), 15);
        graph.setEdgeWeight(graph.getEdge(1, 2), 9);
        graph.setEdgeWeight(graph.getEdge(1, 3), 6);
        graph.setEdgeWeight(graph.getEdge(1, 4), 6);
        graph.setEdgeWeight(graph.getEdge(1, 5), 8);
        graph.setEdgeWeight(graph.getEdge(1, 6), 17);
        graph.setEdgeWeight(graph.getEdge(2, 3), 7);
        graph.setEdgeWeight(graph.getEdge(2, 4), 9);
        graph.setEdgeWeight(graph.getEdge(2, 5), 8);
        graph.setEdgeWeight(graph.getEdge(2, 6), 12);
        graph.setEdgeWeight(graph.getEdge(3, 4), 10);
        graph.setEdgeWeight(graph.getEdge(3, 5), 5);
        graph.setEdgeWeight(graph.getEdge(3, 6), 11);
        graph.setEdgeWeight(graph.getEdge(4, 5), 14);
        graph.setEdgeWeight(graph.getEdge(4, 6), 9);
        graph.setEdgeWeight(graph.getEdge(5, 6), 8);

        Map<Integer, Double> weights = new HashMap<>();
        weights.put(1, 1.0);
        weights.put(2, 1.0);
        weights.put(3, 2.0);
        weights.put(4, 1.0);
        weights.put(5, 1.0);
        weights.put(6, 1.0);

        for (int i = 0; i < 30; ++i) {
            CapacitatedSpanningTreeAlgorithm.CapacitatedSpanningTree<Integer,
                DefaultWeightedEdge> cmst =
                    new EsauWilliamsCapacitatedMinimumSpanningTree<>(graph, 0, 3, weights, 20)
                        .getCapacitatedSpanningTree();

            assertNotNull(cmst);
            assertTrue(cmst.isCapacitatedSpanningTree(graph, 0, 3, weights));
        }
    }

    /**
     * An unconected graph
     */
    @Test
    public void testUnconnectedGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedGraph<>(DefaultWeightedEdge.class);
        Map<Integer, Double> demands = new HashMap<>();
        graph.addVertex(0);
        demands.put(0, 1.0);
        graph.addVertex(1);
        demands.put(1, 1.0);

        double capacity = 30.0;

        boolean testOK = false;

        try {
            CapacitatedSpanningTreeAlgorithm<Integer,
                DefaultWeightedEdge> capacitatedSpanningTreeAlgorithm =
                    new EsauWilliamsCapacitatedMinimumSpanningTree<>(
                        graph, 0, capacity, demands, 1);
        } catch (IllegalArgumentException e) {
            testOK = true;
        }

        assertTrue(testOK);
    }

    /**
     * Graph violating the capacity constraint
     */
    @Test
    public void testViolatedCapacityConstraint()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedGraph<>(DefaultWeightedEdge.class);
        Map<Integer, Double> demands = new HashMap<>();
        graph.addVertex(0);
        demands.put(0, 1.0);
        graph.addVertex(1);
        demands.put(1, 1.0);
        graph.addEdge(0, 1);

        double capacity = -1.0;

        boolean testOK = false;

        try {
            CapacitatedSpanningTreeAlgorithm<Integer,
                DefaultWeightedEdge> capacitatedSpanningTreeAlgorithm =
                    new EsauWilliamsCapacitatedMinimumSpanningTree<>(
                        graph, 0, capacity, demands, 1);
        } catch (IllegalArgumentException e) {
            testOK = true;
        }

        assertTrue(testOK);
    }

    /**
     * Graph violating the capacity constraint
     */
    @Test
    public void testInvalidGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedGraph<>(DefaultWeightedEdge.class);
        Map<Integer, Double> demands = new HashMap<>();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);

        demands.put(0, 1.0);
        demands.put(1, 1.0);
        demands.put(2, 1.0);
        demands.put(3, 1.0);
        demands.put(4, 1.0);

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);

        double capacity = 2.0;

        boolean testOK = false;

        try {
            CapacitatedSpanningTreeAlgorithm<Integer,
                DefaultWeightedEdge> capacitatedSpanningTreeAlgorithm =
                    new EsauWilliamsCapacitatedMinimumSpanningTree<>(
                        graph, 0, capacity, demands, 1);
            capacitatedSpanningTreeAlgorithm.getCapacitatedSpanningTree();
        } catch (IllegalArgumentException e) {
            testOK = true;
        }

        assertTrue(testOK);
    }
}
