/*
 * (C) Copyright 2013-2023, by Nikolay Ognyanov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.jupiter.api.*;

import java.util.function.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectedSimpleCyclesTest
{
    private static final int MAX_SIZE = 9;
    private static final int[] RESULTS = { 0, 1, 3, 8, 24, 89, 415, 2372, 16072, 125673 };

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
        assertEquals(1, alg.findSimpleCycles().size());
        graph.addEdge(1, 1);
        assertEquals(2, alg.findSimpleCycles().size());
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        assertEquals(3, alg.findSimpleCycles().size());
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(4, alg.findSimpleCycles().size());
        graph.addEdge(6, 6);
        assertEquals(5, alg.findSimpleCycles().size());

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
            assertEquals(RESULTS[size], alg.findSimpleCycles().size());
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
        assertEquals(1, alg.findSimpleCycles().size());
        graph.addEdge(1, 1);
        assertEquals(2, alg.findSimpleCycles().size());
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        assertEquals(3, alg.findSimpleCycles().size());
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(4, alg.findSimpleCycles().size());
        graph.addEdge(6, 6);
        assertEquals(5, alg.findSimpleCycles().size());

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
            assertEquals(RESULTS[size], alg.findSimpleCycles().size());
        }
    }

}
