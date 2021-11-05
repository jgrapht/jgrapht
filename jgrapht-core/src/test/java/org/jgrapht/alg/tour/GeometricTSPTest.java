/*
 * (C) Copyright 2019-2021, by Peter Harman and Contributors.
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

import org.apache.commons.math3.geometry.euclidean.twod.*;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.junit.*;
import org.junit.experimental.categories.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;
import java.util.PrimitiveIterator.*;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;

/**
 * Tests of Travelling Salesman Problem algorithms based on a random set of 2D points, with graphs
 * of increasing size
 *
 * @author Peter Harman
 */
@Category(SlowTests.class)
@RunWith(Parameterized.class)
public class GeometricTSPTest
{

    private static final OfDouble RNG = new Random().doubles(0.0, 100.0).iterator();
    private final Graph<Vector2D, DefaultWeightedEdge> graph;

    public GeometricTSPTest(Graph<Vector2D, DefaultWeightedEdge> graph, Integer size)
    {
        this.graph = graph;
    }

    @Parameterized.Parameters(name = "{1} Points")
    public static Object[][] graphs()
    {
        List<Object[]> graphs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int size = (int) Math.pow(10, i);
            graphs.add(new Object[] { generate(size), size });
        }
        return graphs.toArray(new Object[0][]);
    }

    static Graph<Vector2D, DefaultWeightedEdge> generate(int n)
    {
        Vector2D[] points = new Vector2D[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Vector2D(RNG.next(), RNG.next());
        }
        return generate(points);
    }

    static Graph<Vector2D, DefaultWeightedEdge> generate(Vector2D[] points)
    {
        GraphBuilder<Vector2D, DefaultWeightedEdge,
            Graph<Vector2D, DefaultWeightedEdge>> builder = GraphTypeBuilder
                .undirected().vertexClass(Vector2D.class).edgeClass(DefaultWeightedEdge.class)
                .weighted(true).buildGraphBuilder();
        for (Vector2D point : points) {
            builder.addVertex(point);
        }
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                builder.addEdge(points[i], points[j], points[i].distance(points[j]));
            }
        }
        return builder.build();
    }

    void testWith(
        String description, HamiltonianCycleAlgorithm<Vector2D, DefaultWeightedEdge> algorithm)
    {
        GraphPath<Vector2D, DefaultWeightedEdge> tour = algorithm.getTour(graph);
        assertHamiltonian(graph, tour);
    }

    @Test
    public void testGreedy()
    {
        testWith("Greedy", new GreedyHeuristicTSP<>());
    }

    @Test
    public void testNearestInsertionHeuristic()
    {
        testWith(
            "Nearest insertion starting from shortest edge", new NearestInsertionHeuristicTSP<>());
    }

    @Test
    public void testNearestNeighbourHeuristic()
    {
        testWith("Nearest neighbour", new NearestNeighborHeuristicTSP<>());
    }

    @Test
    public void testRandom()
    {
        testWith("Random", new RandomTourTSP<>());
    }

    @Test
    public void testTwoOptNearestNeighbour()
    {
        testWith(
            "Two-opt of nearest neighbour",
            new TwoOptHeuristicTSP<>(new NearestNeighborHeuristicTSP<>()));
    }

    @Test
    public void testTwoOpt1()
    {
        testWith("Two-opt, 1 attempt from random", new TwoOptHeuristicTSP<>(1));
    }

    @Test
    public void testChristofides()
    {
        testWith("Christofides", new ChristofidesThreeHalvesApproxMetricTSP<>());
    }

}
