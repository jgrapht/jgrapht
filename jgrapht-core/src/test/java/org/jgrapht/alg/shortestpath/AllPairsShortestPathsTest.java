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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Dimitrios Michail
 */
public class AllPairsShortestPathsTest
{
    @Test
    public void testRandomFixedSeed()
    {
        final long seed = 47;
        Random rng = new Random(seed);
        testAllPairsShortestPaths(rng);
    }

    @Test
    public void testRandomFixedSeed8()
    {
        final long seed = 8;
        Random rng = new Random(seed);
        testAllPairsShortestPaths(rng);
    }

    @Test
    public void testRandomFixedSeed13()
    {
        final long seed = 13;
        Random rng = new Random(seed);
        testAllPairsShortestPaths(rng);
    }

    @Test
    public void testRandomFixedSeed17()
    {
        final long seed = 17;
        Random rng = new Random(seed);
        testAllPairsShortestPaths(rng);
    }

    private void testAllPairsShortestPaths(Random rng)
    {
        final int tests = 5;
        final int n = 20;
        final double p = 0.35;
        final int landmarksCount = 2;

        List<Function<Graph<Integer, DefaultWeightedEdge>,
            ShortestPathAlgorithm<Integer, DefaultWeightedEdge>>> algs = new ArrayList<>();
        algs.add((g) -> new DijkstraShortestPath<>(g));
        algs.add((g) -> new BidirectionalDijkstraShortestPath<>(g));
        algs.add((g) -> new AStarShortestPath<>(g, (u, t) -> 0d));
        algs.add((g) -> {
            Integer[] vertices = g.vertexSet().toArray(new Integer[0]);
            Set<Integer> landmarks = new HashSet<>();
            while (landmarks.size() < landmarksCount) {
                landmarks.add(vertices[rng.nextInt(g.vertexSet().size())]);
            }
            return new AStarShortestPath<>(g, new ALTAdmissibleHeuristic<>(g, landmarks));
        });

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
            new GnpRandomGraphGenerator<>(n, p, rng, true);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);
            gen.generateGraph(g);

            // assign random weights
            for (DefaultWeightedEdge e : g.edgeSet()) {
                g.setEdgeWeight(e, rng.nextDouble());
            }

            double[][] dist = new double[n][n];

            int j = 0;
            for (Function<Graph<Integer, DefaultWeightedEdge>,
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge>> spProvider : algs)
            {
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = spProvider.apply(g);
                for (Integer v : g.vertexSet()) {
                    for (Integer u : g.vertexSet()) {
                        GraphPath<Integer, DefaultWeightedEdge> path = alg.getPath(v, u);

                        double d;
                        if (path == null) {
                            d = Double.POSITIVE_INFINITY;
                        } else {
                            d = path.getWeight();
                        }

                        if (j == 0) {
                            dist[v][u] = d;
                        } else {
                            assertEquals(dist[v][u], d, 1e-9);
                        }
                    }
                }
                j++;
            }

        }

    }

}
