/*
 * (C) Copyright 2018-2023, by CAE Tech Limited and Contributors.
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
package org.jgrapht.alg.decomposition;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;
import org.jgrapht.alg.matching.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for Dulmage-Mendelsohn, based on MaximumCardinailityBipartiteMatchingTest
 *
 * @author Peter Harman
 * @author Joris Kinable
 */
public class DulmageMendelsohnDecompositionTest
{

    public static List<GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge>> generators()
    {
        List<GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge>> out = new ArrayList<>();
        Random random = new Random(1);
        for (int vertices = 20; vertices < 120; vertices++) {
            int edges = random.nextInt(maxEdges(vertices) / 2);
            int imbalance = randomImbalance(random, vertices);
            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                new GnmRandomBipartiteGraphGenerator<>(
                    vertices - imbalance, vertices + imbalance, edges, 0);
            out.add(generator);
        }
        return out;
    }

    @ParameterizedTest
    @MethodSource("generators")
    public void testGeneratedGraph(GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(graph);
        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm =
            new DulmageMendelsohnDecomposition<>(
                graph, generator.getFirstPartition(), generator.getSecondPartition());
        assertValidDecomposition(
            graph, dm.getDecomposition(true), generator.getFirstPartition(),
            generator.getSecondPartition());
        assertValidDecomposition(
            graph, dm.getDecomposition(false), generator.getFirstPartition(),
            generator.getSecondPartition());
    }

    /**
     * Assert that the structure of the decomposition is valid
     *
     * @param <V>
     * @param <E>
     * @param graph
     * @param decomposition
     * @param partition1
     * @param partition2
     */
    private static <V, E> void assertValidDecomposition(
        Graph<V, E> graph, DulmageMendelsohnDecomposition.Decomposition<V, E> decomposition,
        Set<V> partition1, Set<V> partition2)
    {
        // Is the perfect matched set actually perfectly matched?
        Set<V> allPerfectlyMatched = new HashSet<>();
        Set<V> partition1PerfectlyMatched = new HashSet<>();
        Set<V> partition2PerfectlyMatched = new HashSet<>();
        for (Set<V> set : decomposition.getPerfectMatchedSets()) {
            allPerfectlyMatched.addAll(set);
            for (V v : set) {
                if (partition1.contains(v)) {
                    partition1PerfectlyMatched.add(v);
                }
                if (partition2.contains(v)) {
                    partition2PerfectlyMatched.add(v);
                }
            }
            ;
        }
        ;
        Matching<V,
            E> perfectMatching = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(
                new AsSubgraph<>(graph, allPerfectlyMatched), partition1PerfectlyMatched,
                partition2PerfectlyMatched).getMatching();
        assertTrue(perfectMatching.isPerfect(), "Core of decomposition must perfectly match");
        // Do all the vertices in the graph appear in the decomposition, and only in one part of it?
        for (V v : graph.vertexSet()) {
            if (allPerfectlyMatched.contains(v)) {
                assertFalse(
                    decomposition.getPartition1DominatedSet().contains(v),
                    "Vertex appears in multiple sets in decomposition");
                assertFalse(
                    decomposition.getPartition2DominatedSet().contains(v),
                    "Vertex appears in multiple sets in decomposition");
            } else if (decomposition.getPartition1DominatedSet().contains(v)) {
                assertFalse(
                    allPerfectlyMatched.contains(v),
                    "Vertex appears in multiple sets in decomposition");
                assertFalse(
                    decomposition.getPartition2DominatedSet().contains(v),
                    "Vertex appears in multiple sets in decomposition");
            } else {
                assertTrue(
                    decomposition.getPartition2DominatedSet().contains(v),
                    "Vertex appears in multiple sets in decomposition");
            }
        }
        ;
        // Are the partition1/2 dominated sets dominated as expected?
        int n1 = 0;
        int n2 = 0;
        for (V v : decomposition.getPartition1DominatedSet()) {
            if (partition1.contains(v)) {
                n1++;
            } else {
                n2++;
            }
        }
        assertTrue(
            n1 > n2 || (n1 == 0 && n2 == 0),
            "Partition 1 dominated set is not dominated by partition 1");
        n1 = 0;
        n2 = 0;
        for (V v : decomposition.getPartition2DominatedSet()) {
            if (partition1.contains(v)) {
                n1++;
            } else {
                n2++;
            }
        }
        assertTrue(
            n1 < n2 || (n1 == 0 && n2 == 0),
            "Partition 2 dominated set is not dominated by partition 2");
    }

    /**
     * Calculate the maximum number of edges for number of vertices
     *
     * @param n
     * @return
     */
    private static int maxEdges(int n)
    {
        if (n % 2 == 0) {
            return Math.multiplyExact(n / 2, n - 1);
        } else {
            return Math.multiplyExact(n, (n - 1) / 2);
        }
    }

    /**
     * Generate a random difference between the size of partition1 and partition2
     *
     * @param random
     * @param n
     * @return
     */
    private static int randomImbalance(Random random, int n)
    {
        int max = Math.floorDiv(n, 4);
        return random.nextInt(max * 2) - max;
    }
}
