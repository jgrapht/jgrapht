/*
 * (C) Copyright 2018-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests the weighted exact vertex cover algorithms.
 *
 * @author Joris Kinable
 */
public abstract class WeightedVertexCoverExactTest
    extends
    VertexCoverExactTest
    implements
    WeightedVertexCoverTest
{

    // ------- Exact algorithms ------

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover1()
    {
        int[] weightArray = { 18, 16, 13, 14, 12, 0, 20, 11, 10, 10, 10, 6, 6, 12, 15, 6, 24, 2, 6,
            6, 12, 7, 6, 11, 23, 3, 5, 23, 4, 24, 22, 17, 24, 7, 15, 14, 23, 12, 3, 18, 3, 20, 3, 5,
            19, 25, 8, 13, 22, 0, 20, 7, 21, 9, 0, 6, 0, 18, 16, 1 };

        int[][] edges = { { 1, 21 }, { 2, 4 }, { 2, 13 }, { 2, 44 }, { 2, 45 }, { 3, 24 },
            { 3, 31 }, { 3, 35 }, { 3, 42 }, { 5, 14 }, { 5, 36 }, { 6, 9 }, { 6, 13 }, { 6, 25 },
            { 6, 46 }, { 7, 47 }, { 7, 58 }, { 8, 12 }, { 8, 33 }, { 9, 21 }, { 9, 30 }, { 10, 59 },
            { 12, 15 }, { 12, 43 }, { 12, 57 }, { 13, 32 }, { 13, 33 }, { 13, 59 }, { 14, 26 },
            { 14, 48 }, { 16, 57 }, { 21, 31 }, { 22, 57 }, { 23, 44 }, { 23, 56 }, { 24, 49 },
            { 25, 34 }, { 25, 46 }, { 26, 33 }, { 26, 40 }, { 26, 59 }, { 27, 59 }, { 28, 33 },
            { 30, 51 }, { 36, 48 }, { 36, 54 }, { 37, 38 }, { 38, 43 }, { 40, 41 }, { 41, 58 },
            { 44, 50 }, { 45, 49 }, { 47, 49 }, { 48, 56 }, { 49, 55 }, { 52, 54 }, { 54, 55 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 185.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover2()
    {
        int[] weightArray = { 13, 11, 3, 5, 16, 0, 16, 16, 14, 25, 15, 23, 4, 12, 23, 20, 19, 12,
            15, 18, 25, 15, 9, 2, 20, 6, 21, 17, 16, 21, 20, 9, 0, 23, 7, 24, 17, 15, 19, 12, 4, 13,
            1, 19, 7, 22, 20, 6, 13, 2, 5, 19, 4, 0, 11, 16, 13, 1, 15, 25 };

        int[][] edges = { { 0, 20 }, { 0, 34 }, { 0, 46 }, { 0, 48 }, { 0, 58 }, { 1, 2 }, { 1, 5 },
            { 1, 18 }, { 2, 7 }, { 2, 22 }, { 2, 41 }, { 2, 51 }, { 2, 55 }, { 2, 56 }, { 3, 7 },
            { 3, 41 }, { 3, 48 }, { 3, 57 }, { 4, 36 }, { 4, 44 }, { 4, 54 }, { 5, 29 }, { 5, 30 },
            { 5, 47 }, { 6, 55 }, { 6, 59 }, { 7, 19 }, { 7, 28 }, { 8, 18 }, { 8, 46 }, { 9, 36 },
            { 10, 12 }, { 10, 13 }, { 10, 21 }, { 10, 39 }, { 11, 53 }, { 12, 20 }, { 12, 51 },
            { 13, 25 }, { 13, 57 }, { 13, 58 }, { 14, 32 }, { 14, 34 }, { 14, 44 }, { 14, 55 },
            { 15, 19 }, { 15, 30 }, { 16, 28 }, { 16, 55 }, { 17, 27 }, { 17, 29 }, { 17, 38 },
            { 17, 41 }, { 19, 30 }, { 19, 51 }, { 19, 59 }, { 20, 55 }, { 21, 33 }, { 22, 25 },
            { 22, 30 }, { 22, 32 }, { 22, 40 }, { 24, 43 }, { 25, 26 }, { 25, 32 }, { 26, 39 },
            { 26, 59 }, { 27, 38 }, { 28, 35 }, { 28, 51 }, { 29, 31 }, { 29, 34 }, { 29, 53 },
            { 31, 36 }, { 32, 34 }, { 32, 49 }, { 34, 38 }, { 35, 38 }, { 35, 40 }, { 35, 50 },
            { 36, 38 }, { 36, 45 }, { 36, 49 }, { 36, 56 }, { 37, 58 }, { 38, 40 }, { 38, 59 },
            { 39, 44 }, { 39, 45 }, { 39, 59 }, { 41, 44 }, { 42, 45 }, { 43, 46 }, { 43, 49 },
            { 44, 46 }, { 46, 51 }, { 47, 56 }, { 48, 56 }, { 50, 57 }, { 54, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 339.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover3()
    {
        int[] weightArray = { 20, 15, 16, 0, 20, 7, 1, 25, 0, 23, 6, 7, 8, 11, 3, 18, 25, 12, 20,
            18, 24, 10, 9, 25, 0, 9, 22, 18, 23, 17, 23, 3, 12, 8, 9, 21, 2, 0, 20, 0, 14, 6, 13,
            16, 17, 25, 5, 10, 20, 4, 16, 0, 5, 21, 9, 7, 12, 15, 5, 25 };

        int[][] edges = { { 0, 7 }, { 0, 45 }, { 0, 54 }, { 2, 39 }, { 3, 10 }, { 3, 20 },
            { 4, 20 }, { 4, 37 }, { 5, 29 }, { 6, 12 }, { 7, 17 }, { 7, 29 }, { 8, 29 }, { 8, 55 },
            { 10, 25 }, { 11, 33 }, { 12, 51 }, { 12, 58 }, { 13, 50 }, { 15, 30 }, { 16, 17 },
            { 16, 24 }, { 16, 32 }, { 17, 55 }, { 18, 31 }, { 18, 45 }, { 18, 49 }, { 19, 41 },
            { 20, 48 }, { 21, 27 }, { 21, 56 }, { 23, 30 }, { 25, 28 }, { 26, 45 }, { 30, 40 },
            { 30, 45 }, { 30, 52 }, { 31, 43 }, { 31, 50 }, { 32, 48 }, { 33, 55 }, { 36, 42 },
            { 36, 47 }, { 37, 39 }, { 38, 42 }, { 38, 49 }, { 41, 44 }, { 49, 58 }, { 51, 55 },
            { 51, 58 }, { 53, 57 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 220.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover4()
    {
        int[] weightArray = { 0, 20, 10, 0, 0, 15, 18, 20, 12, 18, 1, 13, 1, 25, 14, 6, 10, 16, 18,
            10, 12, 24, 22, 23, 3, 13, 9, 21, 5, 17, 22, 20, 13, 12, 22, 4, 5, 18, 0, 14, 25, 6, 1,
            18, 22, 15, 4, 6, 13, 10, 2, 21, 24, 16, 6, 6, 23, 9, 9, 2 };

        int[][] edges = { { 0, 19 }, { 1, 6 }, { 1, 16 }, { 2, 47 }, { 2, 58 }, { 3, 49 },
            { 3, 53 }, { 4, 57 }, { 5, 19 }, { 5, 28 }, { 6, 16 }, { 6, 26 }, { 6, 35 }, { 7, 10 },
            { 7, 17 }, { 7, 25 }, { 7, 51 }, { 7, 59 }, { 8, 51 }, { 10, 27 }, { 10, 57 },
            { 11, 20 }, { 11, 23 }, { 12, 43 }, { 12, 50 }, { 13, 55 }, { 14, 28 }, { 14, 31 },
            { 14, 48 }, { 15, 21 }, { 15, 29 }, { 15, 57 }, { 17, 44 }, { 18, 20 }, { 19, 45 },
            { 20, 22 }, { 20, 26 }, { 20, 27 }, { 21, 27 }, { 21, 28 }, { 21, 52 }, { 22, 23 },
            { 22, 27 }, { 22, 48 }, { 23, 33 }, { 27, 41 }, { 28, 51 }, { 30, 42 }, { 30, 52 },
            { 30, 57 }, { 35, 50 }, { 36, 57 }, { 37, 50 }, { 38, 43 }, { 41, 47 }, { 46, 52 },
            { 47, 57 }, { 55, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 238.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover5()
    {
        int[] weightArray = { 1, 10, 13, 17, 0, 25, 4, 15, 8, 14, 20, 23, 10, 2, 21, 10, 4, 18, 4,
            20, 25, 5, 20, 19, 11, 15, 18, 8, 19, 3, 3, 24, 3, 8, 6, 12, 8, 12, 2, 8, 2, 1, 5, 23,
            11, 18, 22, 6, 19, 0, 19, 11, 4, 24, 24, 5, 11, 16, 24, 10 };

        int[][] edges = { { 0, 30 }, { 3, 4 }, { 6, 15 }, { 7, 10 }, { 9, 56 }, { 13, 42 },
            { 19, 49 }, { 22, 44 }, { 47, 58 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 50.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover6()
    {
        int[] weightArray = { 11, 11, 17, 25, 16, 9, 11, 5, 5, 18, 21, 3, 15, 12, 7, 14, 14, 10, 19,
            12, 21, 17, 8, 0, 1, 3, 21, 8, 23, 0, 23, 7, 2, 1, 24, 18, 4, 25, 22, 6, 3, 10, 7, 4, 0,
            24, 5, 16, 5, 8, 19, 11, 5, 14, 15, 19, 18, 3, 5, 3 };

        int[][] edges = { { 0, 12 }, { 1, 14 }, { 1, 19 }, { 1, 24 }, { 1, 28 }, { 1, 49 },
            { 1, 58 }, { 2, 46 }, { 3, 6 }, { 3, 27 }, { 4, 19 }, { 4, 29 }, { 4, 33 }, { 5, 48 },
            { 5, 49 }, { 5, 53 }, { 6, 19 }, { 6, 40 }, { 7, 12 }, { 7, 21 }, { 7, 30 }, { 9, 10 },
            { 9, 24 }, { 9, 26 }, { 10, 11 }, { 10, 24 }, { 10, 57 }, { 11, 29 }, { 13, 27 },
            { 14, 44 }, { 15, 44 }, { 15, 51 }, { 17, 50 }, { 17, 56 }, { 18, 22 }, { 18, 31 },
            { 18, 32 }, { 18, 44 }, { 19, 26 }, { 19, 32 }, { 19, 34 }, { 19, 59 }, { 20, 30 },
            { 20, 31 }, { 21, 48 }, { 21, 51 }, { 22, 59 }, { 23, 41 }, { 24, 38 }, { 24, 45 },
            { 25, 41 }, { 25, 42 }, { 26, 28 }, { 26, 35 }, { 27, 35 }, { 28, 32 }, { 28, 50 },
            { 29, 35 }, { 29, 36 }, { 30, 33 }, { 30, 35 }, { 30, 50 }, { 31, 34 }, { 31, 38 },
            { 31, 43 }, { 32, 36 }, { 33, 43 }, { 34, 36 }, { 34, 55 }, { 34, 57 }, { 35, 42 },
            { 35, 45 }, { 35, 56 }, { 36, 58 }, { 37, 47 }, { 38, 45 }, { 38, 49 }, { 38, 58 },
            { 39, 50 }, { 40, 49 }, { 42, 58 }, { 43, 58 }, { 50, 51 }, { 56, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 286.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover7()
    {
        int[] weightArray = { 24, 13, 20, 22, 17, 18, 14, 3, 10, 10, 3, 13, 25, 3, 24, 7, 12, 24,
            20, 11, 11, 14, 10, 7, 16, 0, 20, 16, 25, 24, 4, 3, 23, 14, 5, 7, 21, 17, 25, 24, 9, 22,
            13, 19, 20, 21, 21, 24, 22, 20, 5, 12, 18, 14, 2, 4, 9, 24, 1, 1 };

        int[][] edges = { { 0, 7 }, { 0, 8 }, { 0, 16 }, { 0, 26 }, { 0, 27 }, { 0, 32 }, { 0, 46 },
            { 1, 5 }, { 1, 10 }, { 1, 13 }, { 1, 34 }, { 1, 43 }, { 1, 48 }, { 2, 15 }, { 2, 36 },
            { 2, 49 }, { 3, 12 }, { 3, 33 }, { 3, 58 }, { 4, 7 }, { 4, 39 }, { 4, 40 }, { 4, 53 },
            { 5, 35 }, { 5, 49 }, { 6, 21 }, { 7, 13 }, { 7, 24 }, { 7, 31 }, { 8, 19 }, { 8, 35 },
            { 8, 48 }, { 9, 23 }, { 10, 12 }, { 10, 45 }, { 11, 20 }, { 11, 58 }, { 12, 21 },
            { 12, 32 }, { 12, 40 }, { 12, 47 }, { 12, 53 }, { 13, 19 }, { 13, 24 }, { 13, 35 },
            { 14, 21 }, { 14, 56 }, { 15, 27 }, { 17, 18 }, { 17, 44 }, { 18, 30 }, { 18, 40 },
            { 18, 48 }, { 20, 22 }, { 20, 31 }, { 20, 34 }, { 20, 48 }, { 21, 38 }, { 22, 23 },
            { 22, 25 }, { 22, 35 }, { 22, 59 }, { 23, 26 }, { 25, 36 }, { 25, 49 }, { 25, 56 },
            { 26, 27 }, { 26, 41 }, { 26, 51 }, { 27, 29 }, { 27, 36 }, { 27, 38 }, { 27, 46 },
            { 27, 50 }, { 27, 52 }, { 27, 58 }, { 27, 59 }, { 28, 29 }, { 28, 40 }, { 28, 50 },
            { 29, 31 }, { 29, 56 }, { 30, 46 }, { 30, 55 }, { 31, 35 }, { 31, 55 }, { 32, 55 },
            { 32, 57 }, { 32, 58 }, { 35, 40 }, { 35, 48 }, { 37, 39 }, { 37, 49 }, { 37, 51 },
            { 38, 45 }, { 38, 55 }, { 40, 44 }, { 41, 46 }, { 41, 48 }, { 41, 59 }, { 42, 51 },
            { 43, 56 }, { 47, 50 }, { 48, 52 }, { 49, 57 }, { 50, 51 }, { 50, 58 }, { 53, 58 },
            { 54, 58 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 401.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover8()
    {
        int[] weightArray = { 19, 24, 0, 19, 17, 12, 15, 4, 22, 23, 6, 21, 19, 20, 3, 18, 22, 19, 2,
            4, 19, 8, 23, 15, 21, 12, 4, 1, 21, 23, 11, 8, 18, 6, 11, 14, 0, 4, 11, 11, 22, 2, 1,
            11, 0, 21, 20, 12, 13, 0, 16, 15, 24, 12, 15, 4, 24, 3, 20, 8 };

        int[][] edges = { { 0, 6 }, { 0, 13 }, { 0, 47 }, { 0, 55 }, { 1, 5 }, { 1, 55 }, { 1, 57 },
            { 2, 16 }, { 2, 24 }, { 3, 8 }, { 3, 26 }, { 3, 29 }, { 3, 53 }, { 3, 58 }, { 4, 16 },
            { 5, 16 }, { 5, 45 }, { 5, 53 }, { 6, 7 }, { 6, 17 }, { 6, 27 }, { 6, 33 }, { 6, 34 },
            { 7, 16 }, { 7, 17 }, { 7, 22 }, { 7, 25 }, { 7, 54 }, { 8, 29 }, { 8, 44 }, { 8, 59 },
            { 9, 35 }, { 9, 51 }, { 9, 52 }, { 10, 11 }, { 10, 40 }, { 12, 45 }, { 13, 16 },
            { 13, 31 }, { 13, 44 }, { 13, 48 }, { 13, 49 }, { 14, 35 }, { 14, 48 }, { 15, 41 },
            { 15, 56 }, { 16, 23 }, { 17, 18 }, { 17, 33 }, { 18, 27 }, { 18, 49 }, { 18, 59 },
            { 19, 24 }, { 19, 45 }, { 20, 29 }, { 20, 36 }, { 21, 58 }, { 22, 25 }, { 22, 49 },
            { 22, 56 }, { 23, 26 }, { 23, 58 }, { 25, 36 }, { 25, 52 }, { 26, 34 }, { 26, 39 },
            { 26, 40 }, { 26, 52 }, { 27, 31 }, { 27, 40 }, { 27, 44 }, { 27, 56 }, { 28, 44 },
            { 29, 39 }, { 29, 40 }, { 29, 41 }, { 29, 42 }, { 30, 32 }, { 30, 49 }, { 30, 58 },
            { 31, 32 }, { 32, 35 }, { 32, 39 }, { 32, 59 }, { 33, 47 }, { 33, 48 }, { 33, 54 },
            { 34, 36 }, { 34, 47 }, { 35, 59 }, { 36, 37 }, { 36, 38 }, { 37, 45 }, { 37, 55 },
            { 38, 45 }, { 38, 48 }, { 39, 40 }, { 39, 42 }, { 40, 49 }, { 40, 57 }, { 41, 42 },
            { 41, 50 }, { 43, 53 }, { 43, 58 }, { 44, 46 }, { 45, 47 }, { 45, 48 }, { 48, 56 },
            { 50, 56 }, { 51, 52 }, { 51, 53 }, { 51, 57 }, { 51, 58 }, { 53, 59 }, { 54, 55 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 336.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover9()
    {
        int[] weightArray = { 19, 0, 13, 1, 2, 18, 3, 17, 5, 13, 1, 17, 20, 7, 18, 21, 9, 13, 11,
            23, 4, 8, 14, 22, 13, 10, 4, 17, 0, 8, 24, 6, 3, 3, 8, 25, 20, 4, 19, 19, 4, 11, 3, 2,
            9, 18, 10, 23, 15, 2, 22, 14, 15, 3, 2, 15, 19, 5, 2, 11 };

        int[][] edges = { { 0, 11 }, { 0, 28 }, { 0, 41 }, { 1, 5 }, { 1, 8 }, { 1, 13 }, { 1, 36 },
            { 2, 18 }, { 2, 35 }, { 2, 54 }, { 2, 56 }, { 3, 8 }, { 3, 30 }, { 3, 41 }, { 3, 59 },
            { 4, 58 }, { 5, 32 }, { 6, 14 }, { 6, 31 }, { 6, 41 }, { 6, 46 }, { 7, 47 }, { 9, 10 },
            { 9, 26 }, { 9, 28 }, { 9, 50 }, { 10, 11 }, { 10, 28 }, { 10, 47 }, { 10, 56 },
            { 11, 27 }, { 12, 55 }, { 13, 20 }, { 13, 45 }, { 13, 59 }, { 14, 37 }, { 16, 28 },
            { 16, 40 }, { 17, 20 }, { 17, 39 }, { 17, 57 }, { 18, 34 }, { 18, 38 }, { 19, 53 },
            { 19, 58 }, { 20, 24 }, { 20, 35 }, { 20, 41 }, { 20, 45 }, { 20, 54 }, { 21, 23 },
            { 21, 27 }, { 22, 37 }, { 23, 37 }, { 23, 45 }, { 23, 47 }, { 25, 27 }, { 25, 29 },
            { 25, 30 }, { 26, 31 }, { 26, 50 }, { 27, 29 }, { 27, 48 }, { 27, 50 }, { 29, 48 },
            { 30, 42 }, { 30, 58 }, { 31, 49 }, { 32, 38 }, { 33, 45 }, { 33, 54 }, { 34, 40 },
            { 34, 46 }, { 34, 59 }, { 35, 54 }, { 35, 57 }, { 36, 53 }, { 38, 41 }, { 40, 43 },
            { 41, 54 }, { 42, 59 }, { 44, 49 }, { 47, 54 }, { 50, 56 }, { 51, 57 }, { 52, 58 },
            { 53, 55 }, { 53, 56 }, { 55, 57 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 234.0, 0);
    }

    @Test
    @Category(SlowTests.class)
    public void testExactMinimumCover10()
    {
        int[] weightArray = { 0, 12, 13, 7, 23, 21, 8, 20, 12, 21, 23, 1, 16, 13, 2, 9, 18, 24, 18,
            13, 0, 13, 4, 12, 16, 23, 5, 13, 15, 14, 15, 18, 23, 17, 23, 9, 12, 0, 16, 21, 7, 13, 9,
            21, 16, 12, 22, 5, 16, 6, 5, 7, 8, 6, 21, 6, 13, 22, 4, 25 };

        int[][] edges = { { 0, 18 }, { 0, 54 }, { 1, 18 }, { 4, 26 }, { 5, 7 }, { 5, 15 },
            { 7, 20 }, { 8, 54 }, { 10, 28 }, { 10, 34 }, { 11, 14 }, { 11, 24 }, { 13, 19 },
            { 14, 59 }, { 15, 19 }, { 16, 35 }, { 17, 39 }, { 19, 37 }, { 19, 38 }, { 22, 37 },
            { 22, 42 }, { 22, 56 }, { 23, 33 }, { 23, 49 }, { 30, 57 }, { 31, 33 }, { 33, 47 },
            { 34, 36 }, { 34, 46 }, { 34, 55 }, { 35, 52 }, { 37, 44 }, { 37, 45 }, { 37, 52 },
            { 39, 45 }, { 48, 57 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        HashMap<Integer, Double> weights = new HashMap<>();
        for (int i = 0; i < weightArray.length; i++)
            weights.put(i, (double) weightArray[i]);

        VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(graph, weights);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 183.0, 0);
    }
}
