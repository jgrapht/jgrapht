/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.TestUtil;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;

/**
 * Unit tests for the {@link FarthestInsertionHeuristicTSP}
 *
 * @author Jose Alejandro Cornejo Acosta
 */
public class FarthestInsertionHeuristicTSPTest
{
    /**
     * Directed graph
     */
    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            farthestInsertion.getTour(graph);
        });

    }

    /**
     * Empty graph
     */
    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            farthestInsertion.getTour(graph);
        });
    }

    /**
     * Not complete
     */
    @Test
    public void testNoCompleteGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            farthestInsertion.getTour(graph);
        });
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour1()
    {
        int[][] edges = {{1, 2, 5}};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        FarthestInsertionHeuristicTSP<Integer, DefaultEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        GraphPath<Integer, DefaultEdge> tour = farthestInsertion.getTour(graph);
        assertHamiltonian(graph, tour);
        Assertions.assertEquals(10, tour.getWeight(), 1e-9);
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour2()
    {
        int[][] edges = {{1, 2, 5}, {1, 3, 5}, {2, 3, 9},};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        FarthestInsertionHeuristicTSP<Integer, DefaultEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        GraphPath<Integer, DefaultEdge> tour = farthestInsertion.getTour(graph);
        assertHamiltonian(graph, tour);
        Assertions.assertEquals(19, tour.getWeight(), 1e-9);
    }

    /**
     * Test with dummy graph of five vertices
     */
    @Test
    public void testDummyGraph5()
    {
        int[][] allDist = {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var farthestInsH = new FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge>();

        var tour = farthestInsH.getTour(graph);
        Assertions.assertEquals(30, tour.getWeight(), 1e-9);
        Assertions.assertArrayEquals(new Integer[]{3, 2, 1, 0, 4, 3},
            tour.getVertexList().toArray(new Integer[0]));
    }

    @Test
    public void testDummyGraph5WithSubtour()
    {
        int[][] allDist = {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var farthestInsH = new FarthestInsertionHeuristicTSP
            <Integer, DefaultWeightedEdge>(new GraphWalk<>(graph, List.of(3, 2, 0, 4), -1));

        var tour = farthestInsH.getTour(graph);
        Assertions.assertEquals(30, tour.getWeight(), 1e-9);

        // vertex 1 should be inserted between vertices 2 and 0
        Assertions.assertArrayEquals(new Integer[]{3, 2, 1, 0, 4, 3},
            tour.getVertexList().toArray(new Integer[0]));
    }

    /**
     * Test with dummy graph of ten vertices
     */
    @Test
    public void testDummyGraph10()
    {
        int[][] allDist = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 0, 10, 11, 12, 13, 14, 15, 16, 17},
            {2, 10, 0, 18, 19, 20, 21, 22, 23, 24},
            {3, 11, 18, 0, 25, 26, 27, 28, 29, 30},
            {4, 12, 19, 25, 0, 31, 32, 33, 34, 35},
            {5, 13, 20, 26, 31, 0, 36, 37, 38, 39},
            {6, 14, 21, 27, 32, 36, 0, 40, 41, 42},
            {7, 15, 22, 28, 33, 37, 40, 0, 43, 44},
            {8, 16, 23, 29, 34, 38, 41, 43, 0, 45},
            {9, 17, 24, 30, 35, 39, 42, 44, 45, 0}};

        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var farthestInsertion = new FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge>();
        var tour = farthestInsertion.getTour(graph);
        Assertions.assertEquals(210, tour.getWeight(), 1e-9);
        Assertions.assertArrayEquals(new Integer[]{4, 5, 1, 6, 0, 7, 3, 8, 2, 9, 4},
            tour.getVertexList().toArray(new Integer[0]));
    }

    // utilities
    static Graph<Integer, DefaultWeightedEdge> createGraphFromMatrixDistances(int[][] allDist)
    {
        int n = allDist.length;
        var graph = GraphTypeBuilder
            .<Integer, DefaultWeightedEdge>undirected().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();

        // add edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i != j) {
                    Graphs.addEdgeWithVertices(graph, i, j, allDist[i][j]);
                }
            }
        }
        return graph;
    }
}
