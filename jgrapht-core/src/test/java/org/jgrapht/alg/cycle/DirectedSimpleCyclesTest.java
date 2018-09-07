/*
 * (C) Copyright 2013-2017, by Nikolay Ognyanov and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.function.*;

import static org.junit.Assert.*;

public class DirectedSimpleCyclesTest
{
    private static int MAX_SIZE = 9;
    private static int[] RESULTS = { 0, 1, 3, 8, 24, 89, 415, 2372, 16072, 125673 };

    @Test
    public void test()
    {
        testAlgorithm(g -> new TiernanSimpleCycles<Integer, DefaultEdge>(g));
        testAlgorithm(g -> new TarjanSimpleCycles<Integer, DefaultEdge>(g));
        testAlgorithm(g -> new JohnsonSimpleCycles<Integer, DefaultEdge>(g));
        testAlgorithm(g -> new SzwarcfiterLauerSimpleCycles<Integer, DefaultEdge>(g));
        testAlgorithm(g -> new HawickJamesSimpleCycles<Integer, DefaultEdge>(g));

        testAlgorithmWithWeightedGraph(
            g -> new TiernanSimpleCycles<Integer, DefaultWeightedEdge>(g));
        testAlgorithmWithWeightedGraph(
            g -> new TarjanSimpleCycles<Integer, DefaultWeightedEdge>(g));
        testAlgorithmWithWeightedGraph(
            g -> new JohnsonSimpleCycles<Integer, DefaultWeightedEdge>(g));
        testAlgorithmWithWeightedGraph(
            g -> new SzwarcfiterLauerSimpleCycles<Integer, DefaultWeightedEdge>(g));
        testAlgorithmWithWeightedGraph(
            g -> new HawickJamesSimpleCycles<Integer, DefaultWeightedEdge>(g));
    }

    private void testAlgorithm(
        Function<Graph<Integer, DefaultEdge>,
            DirectedSimpleCycles<Integer, DefaultEdge>> algProvider)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }
        DirectedSimpleCycles<Integer, DefaultEdge> alg = algProvider.apply(graph);
        graph.addEdge(0, 0);
        assertTrue(alg.findSimpleCycles().size() == 1);
        graph.addEdge(1, 1);
        assertTrue(alg.findSimpleCycles().size() == 2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        assertTrue(alg.findSimpleCycles().size() == 3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertTrue(alg.findSimpleCycles().size() == 4);
        graph.addEdge(6, 6);
        assertTrue(alg.findSimpleCycles().size() == 5);

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new DefaultDirectedGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    graph.addEdge(i, j);
                }
            }
            alg = algProvider.apply(graph);
            assertTrue(alg.findSimpleCycles().size() == RESULTS[size]);
        }
    }

    private void testAlgorithmWithWeightedGraph(
        Function<Graph<Integer, DefaultWeightedEdge>,
            DirectedSimpleCycles<Integer, DefaultWeightedEdge>> algProvider)
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }
        DirectedSimpleCycles<Integer, DefaultWeightedEdge> alg = algProvider.apply(graph);
        graph.addEdge(0, 0);
        assertTrue(alg.findSimpleCycles().size() == 1);
        graph.addEdge(1, 1);
        assertTrue(alg.findSimpleCycles().size() == 2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        assertTrue(alg.findSimpleCycles().size() == 3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertTrue(alg.findSimpleCycles().size() == 4);
        graph.addEdge(6, 6);
        assertTrue(alg.findSimpleCycles().size() == 5);

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    graph.addEdge(i, j);
                }
            }
            alg = algProvider.apply(graph);
            assertTrue(alg.findSimpleCycles().size() == RESULTS[size]);
        }
    }

}
