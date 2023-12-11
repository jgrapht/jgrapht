/*
 * (C) Copyright 2019-2023, by Peter Harman and Contributors.
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.experimental.categories.*;

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
public class GeometricTSPTest
{

    private static final OfDouble RNG = new Random().doubles(0.0, 100.0).iterator();

    public static List<Arguments> graphs()
    {
        List<Arguments> graphs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int size = (int) Math.pow(10, i);
            graphs.add(Arguments.of( generate(size), size ));
        }
        return graphs;
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
        Graph<Vector2D, DefaultWeightedEdge> graph, HamiltonianCycleAlgorithm<Vector2D, DefaultWeightedEdge> algorithm)
    {
        GraphPath<Vector2D, DefaultWeightedEdge> tour = algorithm.getTour(graph);
        assertHamiltonian(graph, tour);
    }

    @DisplayName("Greedy")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testGreedy(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new GreedyHeuristicTSP<>());
    }

    @DisplayName("Nearest insertion starting from shortest edge")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testNearestInsertionHeuristic(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new NearestInsertionHeuristicTSP<>());
    }

    @DisplayName("Nearest neighbour")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testNearestNeighbourHeuristic(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new NearestNeighborHeuristicTSP<>());
    }

    @DisplayName("Random")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testRandom(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new RandomTourTSP<>());
    }

    @DisplayName("Two-opt of nearest neighbour")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testTwoOptNearestNeighbour(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(
            graph,
            new TwoOptHeuristicTSP<>(new NearestNeighborHeuristicTSP<>()));
    }

    @DisplayName("Two-opt, 1 attempt from random")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testTwoOpt1(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new TwoOptHeuristicTSP<>(1));
    }

    @DisplayName("Greedy")
    @ParameterizedTest(name = "{1} points")
    @MethodSource("graphs")
    public void testChristofides(Graph<Vector2D, DefaultWeightedEdge> graph, int size)
    {
        testWith(graph, new ChristofidesThreeHalvesApproxMetricTSP<>());
    }

}
