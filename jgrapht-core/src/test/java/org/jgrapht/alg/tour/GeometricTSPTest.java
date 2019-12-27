/*
 * (C) Copyright 2019-2019, by Peter Harman and Contributors.
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
package org.jgrapht.alg.tour;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.SlowTests;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests of Travelling Salesman Problem algorithms based on a random set of 2D
 * points, with graphs of increasing size
 *
 * @author Peter Harman
 */
@Category(SlowTests.class)
@RunWith(Parameterized.class)
public class GeometricTSPTest {

    private static final Logger LOG = Logger.getLogger(GeometricTSPTest.class.getName());

    private static final OfDouble RNG = new Random().doubles(0.0, 100.0).iterator();
    private final Graph<Point2D, DefaultWeightedEdge> graph;

    public GeometricTSPTest(Graph<Point2D, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    @Parameterized.Parameters
    public static Object[][] graphs() {
        List<Object[]> graphs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            graphs.add(new Object[]{generate((int) Math.pow(10, i))});
        }
        return graphs.toArray(new Object[0][]);
    }

    static Graph<Point2D, DefaultWeightedEdge> generate(int n) {
        Point2D[] points = new Point2D[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point2D.Double(RNG.next(), RNG.next());
        }
        return generate(points);
    }

    static Graph<Point2D, DefaultWeightedEdge> generate(Point2D[] points) {
        GraphBuilder<Point2D, DefaultWeightedEdge, Graph<Point2D, DefaultWeightedEdge>> builder = GraphTypeBuilder.undirected()
                .vertexClass(Point2D.class)
                .edgeClass(DefaultWeightedEdge.class)
                .weighted(true)
                .buildGraphBuilder();
        for (int i = 0; i < points.length; i++) {
            builder.addVertex(points[i]);
        }
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                builder.addEdge(points[i], points[j], points[i].distance(points[j]));
            }
        }
        return builder.build();
    }

    void testWith(String description, HamiltonianCycleAlgorithm<Point2D, DefaultWeightedEdge> algorithm) {
        long t0 = System.currentTimeMillis();
        GraphPath<Point2D, DefaultWeightedEdge> tour = algorithm.getTour(graph);
        long t = System.currentTimeMillis() - t0;
        LOG.log(Level.FINE, "TSP: Method {0}, Size {1}, Time {2} ms, Weight {3}",
                new Object[]{description, graph.vertexSet().size(), t, tour.getWeight()});
        assertHamiltonian(graph, tour);
    }

    @Test
    public void testGreedy() {
        testWith("Greedy", new GreedyHeuristicTSP<>());
    }

    @Test
    public void testNearestInsertionHeuristic() {
        testWith("Nearest insertion starting from shortest edge", new NearestInsertionHeuristicTSP<>());
    }

    @Test
    public void testNearestNeighbourHeuristic() {
        testWith("Nearest neighbour", new NearestNeighborHeuristicTSP<>());
    }

    @Test
    public void testRandom() {
        testWith("Random", new RandomTourTSP<>());
    }

    @Test
    public void testTwoOptNearestNeighbour() {
        testWith("Two-opt of nearest neighbour", new TwoOptHeuristicTSP<>(new NearestNeighborHeuristicTSP<>()));
    }

    @Test
    public void testTwoOpt1() {
        testWith("Two-opt, 1 attempt from random", new TwoOptHeuristicTSP<>(1));
    }

    @Test
    public void testChristofides() {
        testWith("Christofides", new ChristofidesThreeHalvesApproxMetricTSP<>());
    }

}
