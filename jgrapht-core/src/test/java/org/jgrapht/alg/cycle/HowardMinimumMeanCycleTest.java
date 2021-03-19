/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.TestUtil;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphWalk;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link HowardMinimumMeanCycle}.
 */
public class HowardMinimumMeanCycleTest
{

    // test graph instances
    private int[][] graph1 =
        { { 1, 2, 1 }, { 1, 3, 10 }, { 2, 3, 3 }, { 3, 4, 2 }, { 4, 1, 8 }, { 4, 2, 0 } };
    private int[][] graph2 = { { 1, 3, 7 }, { 3, 2, 3 }, { 2, 0, 7 }, { 2, 1, 5 } };
    private int[][] graph3 = { { 0, 2, 16 }, { 0, 3, 0 }, { 3, 0, 14 }, { 5, 0, 16 }, { 0, 8, 12 },
        { 5, 1, 13 }, { 1, 6, 4 }, { 6, 1, 15 }, { 7, 1, 2 }, { 1, 9, 8 }, { 2, 6, 3 },
        { 7, 2, 15 }, { 9, 2, 10 }, { 3, 6, 1 }, { 3, 8, 8 }, { 8, 3, 18 }, { 4, 6, 7 },
        { 4, 9, 13 }, { 5, 6, 3 }, { 8, 6, 7 }, { 7, 8, 7 }, { 9, 7, 17 } };
    private int[][] graph4 = { { 0, 3, 19 }, { 4, 0, 0 }, { 0, 5, 8 }, { 5, 0, 17 }, { 0, 7, 10 },
        { 8, 0, 15 }, { 1, 4, 14 }, { 7, 1, 10 }, { 3, 2, 14 }, { 2, 4, 3 }, { 2, 5, 1 },
        { 2, 9, 1 }, { 5, 3, 18 }, { 6, 3, 4 }, { 3, 7, 2 }, { 8, 3, 8 }, { 5, 4, 17 }, { 6, 4, 5 },
        { 8, 4, 15 }, { 9, 4, 17 }, { 6, 5, 1 }, { 5, 7, 19 }, { 9, 5, 12 }, { 6, 8, 15 },
        { 8, 6, 19 }, { 7, 9, 6 } };
    private int[][] graph5 = { { 6, 0, 11 }, { 8, 0, 5 }, { 4, 1, 3 }, { 2, 3, 6 }, { 7, 2, 9 },
        { 3, 4, 19 }, { 3, 9, 6 }, { 4, 9, 8 }, { 6, 5, 5 }, { 5, 9, 16 }, { 6, 7, 16 },
        { 6, 8, 12 }, { 8, 9, 12 } };
    private int[][] graph6 = { { 0, 2, 16 }, { 0, 3, 0 }, { 3, 0, 14 }, { 5, 0, 16 }, { 0, 8, 12 },
        { 13, 0, 13 }, { 0, 14, 4 }, { 14, 0, 15 }, { 2, 1, 2 }, { 1, 4, 8 }, { 1, 8, 3 },
        { 9, 1, 15 }, { 11, 1, 10 }, { 1, 14, 1 }, { 2, 4, 8 }, { 4, 2, 18 }, { 2, 7, 7 },
        { 2, 10, 13 }, { 2, 11, 3 }, { 5, 3, 7 }, { 3, 7, 7 }, { 8, 3, 17 }, { 3, 12, 19 },
        { 13, 3, 0 }, { 3, 14, 8 }, { 14, 3, 17 }, { 4, 6, 10 }, { 7, 4, 15 }, { 4, 11, 14 },
        { 14, 4, 10 }, { 8, 5, 14 }, { 5, 9, 3 }, { 5, 10, 1 }, { 5, 14, 1 }, { 8, 6, 18 },
        { 9, 6, 4 }, { 6, 10, 2 }, { 11, 6, 8 }, { 13, 6, 17 }, { 14, 6, 5 }, { 9, 7, 15 },
        { 10, 7, 17 }, { 11, 7, 1 }, { 7, 12, 19 }, { 14, 7, 12 }, { 8, 10, 15 }, { 10, 8, 19 },
        { 8, 13, 6 }, { 9, 10, 2 }, { 12, 9, 17 }, { 13, 9, 8 }, { 10, 13, 3 }, { 11, 14, 2 },
        { 12, 14, 5 }, { 14, 12, 13 }, { 14, 13, 5 } };
    private int[][] graph7 = { { 0, 1, 5 }, { 2, 0, 8 }, { 0, 3, 8 }, { 4, 0, 10 }, { 0, 5, 7 },
        { 5, 0, 8 }, { 6, 0, 2 }, { 8, 0, 7 }, { 11, 0, 3 }, { 0, 14, 1 }, { 2, 1, 12 },
        { 1, 6, 16 }, { 1, 12, 11 }, { 12, 1, 18 }, { 1, 13, 10 }, { 2, 3, 9 }, { 2, 4, 9 },
        { 4, 2, 12 }, { 5, 2, 15 }, { 7, 2, 10 }, { 2, 8, 10 }, { 8, 2, 8 }, { 2, 10, 12 },
        { 2, 12, 6 }, { 12, 2, 10 }, { 3, 5, 13 }, { 3, 9, 8 }, { 11, 3, 8 }, { 13, 3, 2 },
        { 7, 4, 17 }, { 8, 4, 17 }, { 12, 4, 11 }, { 5, 6, 13 }, { 8, 5, 8 }, { 9, 5, 2 },
        { 5, 10, 11 }, { 5, 11, 6 }, { 5, 12, 12 }, { 12, 5, 17 }, { 5, 13, 13 }, { 6, 8, 7 },
        { 6, 9, 17 }, { 6, 13, 4 }, { 6, 14, 15 }, { 14, 6, 19 }, { 7, 8, 18 }, { 8, 7, 19 },
        { 7, 11, 18 }, { 11, 7, 8 }, { 12, 7, 10 }, { 7, 13, 4 }, { 13, 7, 17 }, { 8, 9, 15 },
        { 9, 8, 8 }, { 9, 12, 11 }, { 9, 14, 3 }, { 10, 11, 1 }, { 11, 10, 12 }, { 10, 13, 17 },
        { 11, 12, 2 }, { 12, 11, 18 }, { 11, 13, 9 }, { 13, 11, 5 }, { 11, 14, 3 }, { 14, 12, 8 },
        { 14, 13, 9 } };

    // expected mean values
    private double expectedMean1 = 1.6666666666666667;
    private double expectedMean2 = 5.0;
    private double expectedMean3 = 7;
    private double expectedMean4 = 8.25;
    private double expectedMean5 = Double.POSITIVE_INFINITY;
    private double expectedMean6 = 3.6000000000000001;
    private double expectedMean7 = 4.4285714285714288;

    // expected minimum mean path for graph instance
    private int[][] expectedCycle1 = { { 2, 3 }, { 3, 4 }, { 4, 2 } };
    private int[][] expectedCycle2 = { { 1, 3 }, { 3, 2 }, { 2, 1 } };
    private int[][] expectedCycle3 = { { 0, 3 }, { 3, 0 } };
    private int[][] expectedCycle4 = { { 0, 7 }, { 7, 9 }, { 9, 4, }, { 4, 0 } };
    private int[][] expectedCycle5 = null;
    private int[][] expectedCycle6 = { { 14, 6 }, { 6, 10 }, { 10, 13 }, { 13, 3 }, { 3, 14 } };
    private int[][] expectedCycle7 =
        { { 0, 14 }, { 14, 13 }, { 13, 3 }, { 3, 9 }, { 9, 5 }, { 5, 11 }, { 11, 0 } };

    @Test
    public void testGraph1()
    {
        testOnGraph(graph1, expectedMean1, expectedCycle1);
    }

    @Test
    public void testGraph2()
    {
        testOnGraph(graph2, expectedMean2, expectedCycle2);
    }

    @Test
    public void testGraph3()
    {
        testOnGraph(graph3, expectedMean3, expectedCycle3);
    }

    @Test
    public void testGraph4()
    {
        testOnGraph(graph4, expectedMean4, expectedCycle4);
    }

    @Test
    public void testGraph5()
    {
        testOnGraph(graph5, expectedMean5, expectedCycle5);
    }

    @Test
    public void testGraph6()
    {
        testOnGraph(graph6, expectedMean6, expectedCycle6);
    }

    @Test
    public void testGraph7()
    {
        testOnGraph(graph7, expectedMean7, expectedCycle7);
    }

    /**
     * Tests the algorithm on the graph instance {@code graphArray} using {@code expectedMean} and
     * {@code expectedCycleArray} to check correctness.
     *
     * @param graphArray graph instance
     * @param expectedMean mean value
     * @param expectedCycleArray minimum mean cycle
     */
    private void testOnGraph(int[][] graphArray, double expectedMean, int[][] expectedCycleArray)
    {
        Graph<Integer, DefaultEdge> graph = new DirectedWeightedPseudograph<>(DefaultEdge.class);
        TestUtil.constructGraph(graph, graphArray);
        GraphPath<Integer, DefaultEdge> expectedPath;
        if (expectedCycleArray == null) {
            expectedPath = null;
        } else {
            expectedPath = readPath(expectedCycleArray, graph);
        }

        HowardMinimumMeanCycle<Integer, DefaultEdge> mmc = new HowardMinimumMeanCycle<>(graph);
        GraphPath<Integer, DefaultEdge> actualPath = mmc.getCycle();
        double actualMean = mmc.getCycleMean();

        assertEquals(expectedMean, actualMean, 1e-9);
        assertEquals(expectedPath, actualPath);
    }

    /**
     * Constructs path stored in {@code path}.
     *
     * @param path path
     * @param graph graph
     * @return constructed path instance
     */
    private GraphPath<Integer, DefaultEdge> readPath(
        int[][] path, Graph<Integer, DefaultEdge> graph)
    {
        int startVertex = path[0][0];
        int endVertex = path[path.length - 1][1];
        List<DefaultEdge> edges = new ArrayList<>(path.length);
        double pathWeight = 0.0;

        for (int[] edgeArray : path) {
            int source = edgeArray[0];
            int target = edgeArray[1];

            double minimumWeight = Double.POSITIVE_INFINITY;
            DefaultEdge minimumWeightEdge = null;
            for (DefaultEdge edge : graph.getAllEdges(source, target)) {
                double edgeWeight = graph.getEdgeWeight(edge);
                if (edgeWeight < minimumWeight) {
                    minimumWeight = edgeWeight;
                    minimumWeightEdge = edge;
                }
            }
            edges.add(minimumWeightEdge);
            pathWeight += minimumWeight;
        }

        return new GraphWalk<>(graph, startVertex, endVertex, edges, pathWeight);
    }
}
