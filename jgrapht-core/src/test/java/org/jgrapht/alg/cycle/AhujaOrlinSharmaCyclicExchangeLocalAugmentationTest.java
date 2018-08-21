/*
 * (C) Copyright 2003-2018, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AhujaOrlinSharmaCyclicExchangeLocalAugmentation}.
 *
 * @author Christoph Grüne
 */
public class AhujaOrlinSharmaCyclicExchangeLocalAugmentationTest {

    @Test
    public void testImprovementGraph1() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(0);
        cycle.add(4);
        cycle.add(0);


        Map<Integer, Integer> labels = new HashMap<>();

        for(int i = 0; i < 3; ++i) {
            labels.put(i, 1);
            labels.put(i + 3, 2);
            labels.put(i + 6, 3);
        }

        Graph<Integer, DefaultEdge> graph = generateImprovementGraphForPartitionProblem(labels, 1);

        graph.setEdgeWeight(graph.getEdge(0, 4), -5);
        graph.setEdgeWeight(graph.getEdge(4, 8), -5);

        int lengthBound = 2;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(-4, calculatedCycle.getWeight(), 0.0000001);

    }

    @Test
    public void testImprovementGraph2() {
        LinkedList<Integer> cycle = new LinkedList<>();
        for(int i = 0; i <= 20; i += 5) {
            cycle.add(i);
        }
        cycle.add(0);


        Map<Integer, Integer> labels = new HashMap<>();

        for(int i = 0; i < 5; ++i) {
            labels.put(i, 1);
            labels.put(i + 5, 2);
            labels.put(i + 10, 3);
            labels.put(i + 15, 4);
            labels.put(i + 20, 5);
        }

        Graph<Integer, DefaultEdge> graph = generateImprovementGraphForPartitionProblem(labels, 4.9);

        graph.setEdgeWeight(graph.getEdge(0, 5), -1);
        graph.setEdgeWeight(graph.getEdge(5, 10), -1);
        graph.setEdgeWeight(graph.getEdge(10, 15), -1);
        graph.setEdgeWeight(graph.getEdge(15, 20), -1);
        graph.setEdgeWeight(graph.getEdge(20, 0), -1);

        int lengthBound = 5;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(-5, calculatedCycle.getWeight(), 0.0000001);

    }

    @Test
    public void testImprovementGraph3() {
        LinkedList<Integer> cycle = new LinkedList<>();
        for(int i = 0; i <= 20; i += 5) {
            cycle.add(i);
        }
        cycle.add(0);


        Map<Integer, Integer> labels = new HashMap<>();

        for(int i = 0; i < 5; ++i) {
            labels.put(i, 1);
            labels.put(i + 5, 2);
            labels.put(i + 10, 3);
            labels.put(i + 15, 4);
            labels.put(i + 20, 5);
        }

        Graph<Integer, DefaultEdge> graph = generateImprovementGraphForPartitionProblem(labels, 3.9);

        graph.setEdgeWeight(graph.getEdge(0, 5), -1);
        graph.setEdgeWeight(graph.getEdge(5, 10), -1);
        graph.setEdgeWeight(graph.getEdge(10, 15), -1);
        graph.setEdgeWeight(graph.getEdge(15, 20), -1);

        int lengthBound = 5;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(3.9 - 4, calculatedCycle.getWeight(), 0.0000001);

    }

    @Test
    public void testImprovementGraph4() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(3);
        cycle.add(4);
        cycle.add(5);
        cycle.add(0);
        cycle.add(1);
        cycle.add(2);
        cycle.add(3);

        Map<Integer, Integer> labels = new HashMap<>();
        labels.put(0, 0);
        labels.put(1, 1);
        labels.put(2, 2);
        labels.put(3, 3);
        labels.put(4, 4);
        labels.put(5, 5);

        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);
        for(int i = 0; i < 6; ++i) {
            graph.addVertex(i);
        }
        for(int i = 0; i < 6; ++i) {
            graph.setEdgeWeight(graph.addEdge(i, (i + 1) % 6), 1);

        }
        graph.setEdgeWeight(graph.addEdge(1, 4), 1);

        graph.setEdgeWeight(graph.getEdge(1, 4), -3);
        graph.setEdgeWeight(graph.getEdge(3, 4), -6);

        int lengthBound1 = 4;

        GraphWalk<Integer, DefaultEdge> calculatedCycle1
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound1, labels).getLocalAugmentationCycle();

        assertEquals(Double.MAX_VALUE, calculatedCycle1.getWeight(), 0.0000001);
        assertEquals(new ArrayList<>(lengthBound1), calculatedCycle1.getVertexList());
        assertTrue(calculatedCycle1.isEmpty());

        int lengthBound2 = 6;

        GraphWalk<Integer, DefaultEdge> calculatedCycle2
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound2, labels).getLocalAugmentationCycle();

        assertEquals(cycle, calculatedCycle2.getVertexList());
        assertEquals(-1, calculatedCycle2.getWeight(), 0.0000001);
    }

    @Test
    public void testImprovementGraph5() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(0);
        cycle.add(0);

        Map<Integer, Integer> labels = new HashMap<>();
        labels.put(0, 0);
        labels.put(1, 1);
        labels.put(2, 2);
        labels.put(3, 3);
        labels.put(4, 4);
        labels.put(5, 5);

        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);
        for(int i = 0; i < 6; ++i) {
            graph.addVertex(i);
            DefaultEdge e = graph.addEdge(i, i);
            graph.setEdgeWeight(e,-1);
        }
        for(int i = 0; i < 6; ++i) {
            graph.setEdgeWeight(graph.addEdge(i, (i + 1) % 6), 1);

        }
        graph.setEdgeWeight(graph.addEdge(1, 4), 1);

        graph.setEdgeWeight(graph.getEdge(1, 4), -3);
        graph.setEdgeWeight(graph.getEdge(3, 4), -6);

        int lengthBound = 6;

        GraphWalk<Integer, DefaultEdge> calculatedCycle1
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertEquals(-1, calculatedCycle1.getWeight(), 0.0000001);
        assertEquals(cycle, calculatedCycle1.getVertexList());
        assertEquals(Integer.valueOf(0), calculatedCycle1.getStartVertex());
        assertEquals(Integer.valueOf(0), calculatedCycle1.getEndVertex());
    }

    @Test
    public void testImprovementGraph6() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(0);
        cycle.add(5);
        cycle.add(2);
        cycle.add(9);
        cycle.add(10);
        cycle.add(0);

        Map<Integer, Integer> labels = new HashMap<>();
        labels.put(0, 0);
        labels.put(1, 1);
        labels.put(2, 2);
        labels.put(3, 1);
        labels.put(4, 2);
        labels.put(5, 1);
        labels.put(6, 2);
        labels.put(7, 1);
        labels.put(8, 2);
        labels.put(9, 3);
        labels.put(10, 4);

        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);
        for(int i = 0; i < 11; ++i) {
            graph.addVertex(i);
        }
        graph.setEdgeWeight(graph.addEdge(0,1), -2);
        graph.setEdgeWeight(graph.addEdge(0,3), -1);
        graph.setEdgeWeight(graph.addEdge(0,5), -3);
        graph.setEdgeWeight(graph.addEdge(0,7), -2);

        graph.setEdgeWeight(graph.addEdge(1,2), 1);
        graph.setEdgeWeight(graph.addEdge(1,4), 0);
        graph.setEdgeWeight(graph.addEdge(1,6), 1);
        graph.setEdgeWeight(graph.addEdge(1,8), 0);

        graph.setEdgeWeight(graph.addEdge(2,9), 0);

        graph.setEdgeWeight(graph.addEdge(3,2), 0);
        graph.setEdgeWeight(graph.addEdge(3,4), 1);
        graph.setEdgeWeight(graph.addEdge(3,6), 1);
        graph.setEdgeWeight(graph.addEdge(3,8), 0);

        graph.setEdgeWeight(graph.addEdge(4,9), 0);

        graph.setEdgeWeight(graph.addEdge(5,2), 0);
        graph.setEdgeWeight(graph.addEdge(5,4), 1);
        graph.setEdgeWeight(graph.addEdge(5,6), 1);
        graph.setEdgeWeight(graph.addEdge(5,8), 1);

        graph.setEdgeWeight(graph.addEdge(6,9), 0);

        graph.setEdgeWeight(graph.addEdge(7,2), 0);
        graph.setEdgeWeight(graph.addEdge(7,4), 1);
        graph.setEdgeWeight(graph.addEdge(7,6), 0);
        graph.setEdgeWeight(graph.addEdge(7,8), 0);

        graph.setEdgeWeight(graph.addEdge(8,9), 0);

        graph.setEdgeWeight(graph.addEdge(9,10), 0);

        graph.setEdgeWeight(graph.addEdge(10,0), 0);

        int lengthBound = 6;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertNotNull(calculatedCycle);
        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(-3.0, calculatedCycle.getWeight(), 0.00000001);
    }

    @Test
    public void testImprovementGraph7() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(0);
        cycle.add(6);
        cycle.add(7);
        cycle.add(8);
        cycle.add(0);

        Map<Integer, Integer> labels = new HashMap<>();
        labels.put(0, 0);
        labels.put(1, 1);
        labels.put(2, 2);
        labels.put(3, 1);
        labels.put(4, 2);
        labels.put(5, 1);
        labels.put(6, 1);
        labels.put(7, 3);
        labels.put(8, 4);

        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);
        for(int i = 0; i < 9; ++i) {
            graph.addVertex(i);
        }
        graph.setEdgeWeight(graph.addEdge(0,1), -2);
        graph.setEdgeWeight(graph.addEdge(0,3), -1);
        graph.setEdgeWeight(graph.addEdge(0,5), -3);
        graph.setEdgeWeight(graph.addEdge(0,6), -3);

        graph.setEdgeWeight(graph.addEdge(1,2), 1);
        graph.setEdgeWeight(graph.addEdge(1,4), 0);

        graph.setEdgeWeight(graph.addEdge(2,7), 0);

        graph.setEdgeWeight(graph.addEdge(3,2), 0);
        graph.setEdgeWeight(graph.addEdge(3,4), 1);

        graph.setEdgeWeight(graph.addEdge(4,7), 0);

        graph.setEdgeWeight(graph.addEdge(5,2), 0);
        graph.setEdgeWeight(graph.addEdge(5,4), 1);
        graph.setEdgeWeight(graph.addEdge(5,7), 1);

        graph.setEdgeWeight(graph.addEdge(6,2), 0);
        graph.setEdgeWeight(graph.addEdge(6,4), 1);
        graph.setEdgeWeight(graph.addEdge(6,7), 0);

        graph.setEdgeWeight(graph.addEdge(7,8), 0);

        graph.setEdgeWeight(graph.addEdge(8,0), 0);

        int lengthBound = 6;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertNotNull(calculatedCycle);
        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(-3.0, calculatedCycle.getWeight(), 0.00000001);
    }

    @Test
    public void testImprovementGraph8() {
        LinkedList<Integer> cycle = new LinkedList<>();
        cycle.add(0);
        cycle.add(6);
        cycle.add(7);
        cycle.add(8);
        cycle.add(0);

        Map<Integer, Integer> labels = new HashMap<>();
        labels.put(0, 0);
        labels.put(1, 1);
        labels.put(2, 2);
        labels.put(3, 1);
        labels.put(4, 2);
        labels.put(5, 1);
        labels.put(6, 1);
        labels.put(7, 3);
        labels.put(8, 4);

        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);
        for(int i = 0; i < 9; ++i) {
            graph.addVertex(i);
        }
        graph.setEdgeWeight(graph.addEdge(0,1), -4);
        graph.setEdgeWeight(graph.addEdge(0,3), -4);
        graph.setEdgeWeight(graph.addEdge(0,5), -3);
        graph.setEdgeWeight(graph.addEdge(0,6), -4);

        graph.setEdgeWeight(graph.addEdge(1,2), 1);
        graph.setEdgeWeight(graph.addEdge(1,4), 1);

        graph.setEdgeWeight(graph.addEdge(2,7), 0);

        graph.setEdgeWeight(graph.addEdge(3,2), 1);
        graph.setEdgeWeight(graph.addEdge(3,4), 0);

        graph.setEdgeWeight(graph.addEdge(4,7), 0);

        graph.setEdgeWeight(graph.addEdge(5,7), 0);

        graph.setEdgeWeight(graph.addEdge(6,7), 0);

        graph.setEdgeWeight(graph.addEdge(7,8), 0);

        graph.setEdgeWeight(graph.addEdge(8,0), 0);

        int lengthBound = 6;

        GraphWalk<Integer, DefaultEdge> calculatedCycle
                = new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(graph, lengthBound, labels).getLocalAugmentationCycle();

        assertNotNull(calculatedCycle);
        assertEquals(cycle, calculatedCycle.getVertexList());
        assertEquals(-4.0, calculatedCycle.getWeight(), 0.00000001);
    }

    private Graph<Integer, DefaultEdge> generateImprovementGraphForPartitionProblem(Map<Integer, Integer> labels, double initialWeight) {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(null, SupplierUtil.createDefaultEdgeSupplier(), true);

        for(Integer v1 : labels.keySet()) {
            graph.addVertex(v1);
        }

        for(Integer v1 : labels.keySet()) {
            for(Integer v2 : labels.keySet()) {
                if(!labels.get(v1).equals(labels.get(v2))) {
                    graph.addEdge(v1, v2);
                    graph.setEdgeWeight(graph.getEdge(v1, v2), initialWeight);
                    graph.addEdge(v2, v1);
                    graph.setEdgeWeight(graph.getEdge(v1, v2), initialWeight);
                }
            }
        }

        return graph;
    }
}
