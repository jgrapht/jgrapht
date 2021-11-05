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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dimitrios Michail
 */
public class ALTAdmissibleHeuristicTest
{

    @Test
    public void testRandom()
    {
        final int tests = 3;
        final int n = 30;
        final double p = 0.35;
        final int landmarksCount = 2;

        Random rng = new Random(47);

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs
            .add(
                () -> new DirectedWeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER));
        graphs
            .add(
                () -> new WeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER));

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, true);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g);

                // assign random weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    g.setEdgeWeight(e, rng.nextDouble());
                }

                // pick random landmarks
                Integer[] allVertices = g.vertexSet().toArray(new Integer[0]);
                Set<Integer> landmarks = new HashSet<>();
                while (landmarks.size() < landmarksCount) {
                    landmarks.add(allVertices[rng.nextInt(n)]);
                }

                AStarAdmissibleHeuristic<Integer> h = new ALTAdmissibleHeuristic<>(g, landmarks);
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp1 =
                    new DijkstraShortestPath<>(g);
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp2 =
                    new AStarShortestPath<>(g, h);

                for (Integer v : g.vertexSet()) {
                    for (Integer u : g.vertexSet()) {
                        GraphPath<Integer, DefaultWeightedEdge> p1 = sp1.getPath(v, u);
                        GraphPath<Integer, DefaultWeightedEdge> p2 = sp2.getPath(v, u);
                        assertEquals(p1.getWeight(), p2.getWeight(), 1e-9);
                    }
                }

            }
        }

    }

    @Test
    public void testRandomAdmissible()
    {
        final int tests = 3;
        final int n = 35;
        final double p = 0.3;

        Random rng = new Random(33);

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs
            .add(
                () -> new DirectedWeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER));
        graphs
            .add(
                () -> new WeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER));

        Comparator<Double> comparator = new ToleranceDoubleComparator();

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, true);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g);

                // assign random weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    g.setEdgeWeight(e, rng.nextDouble());
                }

                for (Integer l : g.vertexSet()) {
                    AStarAdmissibleHeuristic<Integer> h =
                        new ALTAdmissibleHeuristic<>(g, Collections.singleton(l));
                    for (Integer v : g.vertexSet()) {
                        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp =
                            new DijkstraShortestPath<>(g);
                        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = sp.getPaths(v);
                        for (Integer u : g.vertexSet()) {
                            GraphPath<Integer, DefaultWeightedEdge> path = paths.getPath(u);
                            // System.out.println(h.getCostEstimate(v, u) + " <= " +
                            // path.getWeight());
                            assertTrue(
                                comparator.compare(h.getCostEstimate(v, u), path.getWeight()) <= 0);
                        }
                    }
                }
            }
        }

    }

}
