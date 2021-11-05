/*
 * (C) Copyright 2003-2021, by Linda Buisman and Contributors.
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

import java.util.*;

import static org.jgrapht.alg.vertexcover.VertexCoverTestUtils.isCover;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests exact vertex cover algorithms.
 *
 * @author Linda Buisman
 */
public abstract class VertexCoverExactTest
    implements
    VertexCoverTest
{

    // ------- Exact algorithms ------

    /**
     * 4-cycle graph (optimal=2)
     */
    @Test
    public void test4Cycle()
    {
        Graph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g1, Arrays.asList(0, 1, 2, 3));
        g1.addEdge(0, 1);
        g1.addEdge(1, 2);
        g1.addEdge(2, 3);
        g1.addEdge(3, 0);
        VertexCoverAlgorithm<Integer> mvc1 = createSolver(g1);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc1.getVertexCover();
        assertTrue(isCover(g1, vertexCover));
        assertEquals(vertexCover.getWeight(), 2.0, 0);
    }

    /**
     * Wheel graph W_8 (Optimal=5)
     */
    @Test
    public void testWheel()
    {
        Graph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g1, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        g1.addEdge(1, 2);
        g1.addEdge(2, 3);
        g1.addEdge(3, 4);
        g1.addEdge(4, 5);
        g1.addEdge(5, 6);
        g1.addEdge(6, 7);
        g1.addEdge(7, 1);
        g1.addEdge(0, 1);
        g1.addEdge(0, 2);
        g1.addEdge(0, 3);
        g1.addEdge(0, 4);
        g1.addEdge(0, 5);
        g1.addEdge(0, 6);
        g1.addEdge(0, 7);
        VertexCoverAlgorithm<Integer> mvc1 = createSolver(g1);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc1.getVertexCover();
        assertTrue(isCover(g1, vertexCover));
        assertEquals(vertexCover.getWeight(), 5.0, 0);
    }

    /**
     * Cubic graph with 8 vertices (Optimal=7)
     */
    @Test
    public void testCubic()
    {
        Graph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g1, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        g1.addEdge(0, 1);
        g1.addEdge(0, 9);
        g1.addEdge(0, 7);
        g1.addEdge(1, 2);
        g1.addEdge(1, 5);
        g1.addEdge(2, 3);
        g1.addEdge(2, 4);
        g1.addEdge(3, 4);
        g1.addEdge(3, 5);
        g1.addEdge(4, 11);
        g1.addEdge(5, 6);
        g1.addEdge(6, 7);
        g1.addEdge(6, 8);
        g1.addEdge(7, 8);
        g1.addEdge(8, 10);
        g1.addEdge(9, 10);
        g1.addEdge(9, 11);
        g1.addEdge(10, 11);
        VertexCoverAlgorithm<Integer> mvc1 = createSolver(g1);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc1.getVertexCover();
        assertTrue(isCover(g1, vertexCover));
        assertEquals(vertexCover.getWeight(), 7.0, 0);
    }

    /**
     * Graph with 6 vertices in the shape >-< (Optimal=2)
     */
    @Test
    public void testWhisker()
    {
        Graph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g1, Arrays.asList(0, 1, 2, 3, 4, 5));
        g1.addEdge(0, 2);
        g1.addEdge(1, 2);
        g1.addEdge(2, 3);
        g1.addEdge(3, 4);
        g1.addEdge(3, 5);
        VertexCoverAlgorithm<Integer> mvc1 = createSolver(g1);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc1.getVertexCover();
        assertTrue(isCover(g1, vertexCover));
        assertEquals(vertexCover.getWeight(), 2.0, 0);
    }

    /**
     * Random graphs
     */
    @Test
    public void testExactMinimumCover1()
    {
        int[][] edges = { { 0, 5 }, { 0, 6 }, { 0, 8 }, { 0, 13 }, { 0, 18 }, { 0, 24 }, { 0, 26 },
            { 0, 32 }, { 0, 40 }, { 1, 8 }, { 1, 20 }, { 1, 36 }, { 1, 47 }, { 1, 50 }, { 2, 18 },
            { 2, 49 }, { 2, 56 }, { 3, 12 }, { 3, 20 }, { 3, 55 }, { 4, 16 }, { 4, 20 }, { 4, 25 },
            { 4, 34 }, { 4, 36 }, { 5, 9 }, { 5, 22 }, { 5, 29 }, { 5, 32 }, { 5, 39 }, { 5, 40 },
            { 5, 45 }, { 5, 54 }, { 6, 11 }, { 6, 34 }, { 7, 19 }, { 7, 26 }, { 7, 29 }, { 7, 35 },
            { 8, 12 }, { 8, 31 }, { 8, 39 }, { 8, 59 }, { 9, 22 }, { 9, 42 }, { 9, 51 }, { 9, 54 },
            { 9, 57 }, { 11, 15 }, { 11, 50 }, { 12, 15 }, { 12, 30 }, { 12, 31 }, { 12, 40 },
            { 12, 45 }, { 12, 49 }, { 13, 14 }, { 13, 16 }, { 13, 30 }, { 13, 37 }, { 13, 48 },
            { 14, 40 }, { 14, 49 }, { 14, 58 }, { 15, 22 }, { 15, 32 }, { 15, 57 }, { 16, 42 },
            { 16, 49 }, { 16, 52 }, { 16, 56 }, { 16, 58 }, { 17, 19 }, { 17, 29 }, { 17, 32 },
            { 17, 36 }, { 18, 25 }, { 18, 31 }, { 18, 39 }, { 19, 31 }, { 20, 21 }, { 20, 25 },
            { 20, 44 }, { 21, 45 }, { 21, 59 }, { 22, 34 }, { 22, 52 }, { 22, 59 }, { 23, 24 },
            { 23, 54 }, { 24, 57 }, { 25, 50 }, { 26, 27 }, { 26, 38 }, { 26, 45 }, { 26, 54 },
            { 26, 55 }, { 27, 42 }, { 28, 55 }, { 29, 30 }, { 29, 45 }, { 32, 42 }, { 33, 44 },
            { 33, 45 }, { 33, 50 }, { 33, 53 }, { 34, 36 }, { 34, 42 }, { 34, 46 }, { 35, 51 },
            { 35, 59 }, { 36, 43 }, { 36, 46 }, { 36, 48 }, { 36, 53 }, { 37, 50 }, { 38, 40 },
            { 38, 47 }, { 38, 58 }, { 40, 59 }, { 41, 57 }, { 43, 51 }, { 43, 54 }, { 44, 48 },
            { 44, 58 }, { 46, 47 }, { 47, 55 }, { 48, 56 }, { 50, 53 }, { 51, 57 }, { 52, 58 },
            { 55, 57 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 33.0, 0);
    }

    @Test
    public void testExactMinimumCover2()
    {
        int[][] edges = { { 0, 10 }, { 0, 20 }, { 0, 37 }, { 0, 58 }, { 1, 2 }, { 1, 10 },
            { 1, 27 }, { 1, 56 }, { 2, 49 }, { 2, 53 }, { 3, 20 }, { 3, 53 }, { 4, 15 }, { 5, 6 },
            { 5, 8 }, { 6, 11 }, { 6, 25 }, { 6, 56 }, { 7, 26 }, { 10, 25 }, { 10, 29 },
            { 11, 17 }, { 13, 34 }, { 13, 45 }, { 13, 57 }, { 15, 27 }, { 16, 45 }, { 17, 39 },
            { 18, 41 }, { 18, 48 }, { 20, 57 }, { 21, 49 }, { 21, 59 }, { 22, 35 }, { 22, 45 },
            { 23, 32 }, { 24, 32 }, { 24, 34 }, { 25, 27 }, { 25, 46 }, { 25, 59 }, { 27, 37 },
            { 28, 53 }, { 31, 45 }, { 33, 51 }, { 38, 39 }, { 39, 40 }, { 39, 44 }, { 44, 45 },
            { 48, 54 }, { 48, 55 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 22.0, 0);
    }

    @Test
    public void testExactMinimumCover3()
    {
        int[][] edges = { { 1, 5 }, { 1, 37 }, { 2, 48 }, { 4, 48 }, { 7, 56 }, { 15, 18 },
            { 20, 58 }, { 40, 50 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 6.0, 0);
    }

    @Test
    public void testExactMinimumCover4()
    {
        int[][] edges = { { 1, 55 }, { 4, 7 }, { 6, 13 }, { 11, 30 }, { 11, 40 }, { 16, 46 },
            { 17, 24 }, { 24, 31 }, { 29, 32 }, { 40, 52 }, { 45, 49 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 9.0, 0);
    }

    @Test
    public void testExactMinimumCover5()
    {
        int[][] edges = { { 0, 47 }, { 0, 48 }, { 0, 58 }, { 1, 17 }, { 1, 25 }, { 1, 36 },
            { 1, 55 }, { 2, 20 }, { 2, 46 }, { 3, 4 }, { 3, 17 }, { 4, 44 }, { 4, 54 }, { 5, 27 },
            { 6, 13 }, { 6, 25 }, { 6, 31 }, { 6, 38 }, { 6, 48 }, { 6, 56 }, { 7, 10 }, { 7, 14 },
            { 7, 31 }, { 7, 45 }, { 8, 13 }, { 8, 51 }, { 9, 23 }, { 10, 45 }, { 11, 22 },
            { 11, 37 }, { 11, 41 }, { 12, 21 }, { 13, 54 }, { 14, 24 }, { 14, 52 }, { 15, 19 },
            { 15, 56 }, { 17, 43 }, { 19, 24 }, { 19, 42 }, { 19, 53 }, { 20, 55 }, { 21, 41 },
            { 21, 55 }, { 22, 59 }, { 23, 29 }, { 25, 43 }, { 25, 50 }, { 26, 31 }, { 27, 43 },
            { 27, 54 }, { 28, 35 }, { 28, 41 }, { 30, 36 }, { 30, 42 }, { 30, 44 }, { 30, 51 },
            { 30, 59 }, { 31, 41 }, { 32, 53 }, { 32, 55 }, { 33, 36 }, { 33, 56 }, { 35, 54 },
            { 37, 44 }, { 38, 55 }, { 40, 41 }, { 41, 42 }, { 41, 43 }, { 41, 53 }, { 43, 45 },
            { 44, 52 }, { 45, 46 }, { 45, 50 }, { 45, 53 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 26.0, 0);
    }

    @Test
    public void testExactMinimumCover6()
    {
        int[][] edges = { { 2, 21 }, { 2, 41 }, { 3, 47 }, { 4, 48 }, { 5, 36 }, { 6, 57 },
            { 12, 46 }, { 13, 41 }, { 23, 26 }, { 25, 45 }, { 26, 28 }, { 26, 31 }, { 26, 52 },
            { 29, 49 }, { 30, 55 }, { 33, 36 }, { 35, 55 }, { 38, 45 }, { 51, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 12.0, 0);
    }

    @Test
    public void testExactMinimumCover7()
    {
        int[][] edges = { { 20, 51 }, { 21, 28 }, { 23, 55 }, { 23, 59 }, { 25, 59 }, { 33, 46 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 5.0, 0);
    }

    @Test
    public void testExactMinimumCover8()
    {
        int[][] edges = { { 0, 16 }, { 0, 52 }, { 0, 58 }, { 1, 8 }, { 1, 27 }, { 1, 38 },
            { 1, 49 }, { 1, 56 }, { 1, 57 }, { 2, 3 }, { 2, 20 }, { 2, 23 }, { 2, 28 }, { 2, 38 },
            { 3, 19 }, { 3, 20 }, { 3, 28 }, { 3, 37 }, { 3, 39 }, { 3, 59 }, { 4, 26 }, { 4, 31 },
            { 4, 41 }, { 5, 9 }, { 5, 33 }, { 5, 42 }, { 6, 26 }, { 6, 37 }, { 6, 55 }, { 7, 27 },
            { 7, 29 }, { 7, 59 }, { 8, 32 }, { 8, 41 }, { 8, 43 }, { 9, 28 }, { 9, 35 }, { 9, 42 },
            { 10, 14 }, { 10, 17 }, { 10, 38 }, { 11, 33 }, { 11, 57 }, { 12, 27 }, { 12, 31 },
            { 12, 34 }, { 12, 41 }, { 12, 50 }, { 12, 52 }, { 13, 16 }, { 13, 30 }, { 13, 36 },
            { 13, 44 }, { 14, 28 }, { 14, 51 }, { 15, 26 }, { 15, 43 }, { 15, 50 }, { 15, 53 },
            { 16, 19 }, { 16, 27 }, { 16, 48 }, { 16, 50 }, { 16, 52 }, { 17, 26 }, { 17, 55 },
            { 18, 45 }, { 18, 49 }, { 18, 57 }, { 19, 22 }, { 19, 26 }, { 19, 53 }, { 20, 26 },
            { 20, 58 }, { 21, 28 }, { 21, 40 }, { 21, 46 }, { 21, 57 }, { 22, 33 }, { 22, 52 },
            { 22, 56 }, { 22, 58 }, { 23, 28 }, { 23, 56 }, { 24, 26 }, { 24, 27 }, { 24, 29 },
            { 24, 31 }, { 24, 34 }, { 24, 43 }, { 24, 47 }, { 24, 49 }, { 24, 53 }, { 25, 27 },
            { 25, 56 }, { 25, 59 }, { 26, 32 }, { 26, 47 }, { 26, 54 }, { 26, 59 }, { 27, 47 },
            { 28, 57 }, { 29, 33 }, { 29, 37 }, { 30, 40 }, { 31, 33 }, { 31, 38 }, { 31, 41 },
            { 31, 48 }, { 31, 49 }, { 31, 58 }, { 32, 33 }, { 32, 37 }, { 33, 41 }, { 34, 35 },
            { 35, 40 }, { 37, 40 }, { 37, 51 }, { 37, 52 }, { 38, 50 }, { 38, 52 }, { 39, 45 },
            { 39, 50 }, { 39, 52 }, { 40, 59 }, { 41, 49 }, { 42, 51 }, { 42, 54 }, { 43, 51 },
            { 50, 52 }, { 54, 59 }, { 58, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 33.0, 0);
    }

    @Test
    public void testExactMinimumCover9()
    {
        int[][] edges = { { 0, 16 }, { 0, 19 }, { 0, 32 }, { 1, 4 }, { 1, 16 }, { 1, 18 },
            { 1, 26 }, { 2, 47 }, { 2, 55 }, { 3, 5 }, { 3, 9 }, { 3, 28 }, { 3, 31 }, { 4, 17 },
            { 4, 53 }, { 5, 55 }, { 6, 48 }, { 7, 39 }, { 7, 53 }, { 8, 32 }, { 8, 37 }, { 8, 57 },
            { 10, 18 }, { 10, 26 }, { 10, 29 }, { 10, 39 }, { 10, 49 }, { 10, 54 }, { 11, 13 },
            { 11, 45 }, { 12, 18 }, { 12, 32 }, { 12, 34 }, { 12, 37 }, { 12, 53 }, { 13, 42 },
            { 13, 43 }, { 14, 26 }, { 15, 38 }, { 16, 52 }, { 16, 54 }, { 18, 27 }, { 18, 39 },
            { 18, 46 }, { 18, 59 }, { 19, 41 }, { 19, 45 }, { 20, 37 }, { 20, 56 }, { 21, 53 },
            { 23, 47 }, { 23, 55 }, { 24, 25 }, { 24, 32 }, { 27, 48 }, { 27, 51 }, { 27, 52 },
            { 28, 34 }, { 29, 36 }, { 30, 52 }, { 31, 48 }, { 31, 49 }, { 32, 50 }, { 35, 37 },
            { 36, 37 }, { 37, 59 }, { 39, 52 }, { 40, 58 }, { 42, 45 }, { 42, 59 }, { 43, 48 },
            { 43, 57 }, { 48, 52 }, { 49, 52 }, { 52, 57 }, { 54, 56 }, { 54, 59 }, { 57, 59 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 27.0, 0);
    }

    @Test
    public void testExactMinimumCover10()
    {
        int[][] edges = { { 1, 21 }, { 2, 6 }, { 2, 43 }, { 2, 56 }, { 4, 7 }, { 4, 43 }, { 6, 7 },
            { 6, 58 }, { 7, 14 }, { 7, 23 }, { 7, 40 }, { 7, 57 }, { 9, 49 }, { 10, 39 },
            { 18, 25 }, { 18, 26 }, { 18, 34 }, { 20, 40 }, { 22, 32 }, { 23, 32 }, { 23, 34 },
            { 25, 39 }, { 26, 34 }, { 26, 41 }, { 27, 49 }, { 29, 42 }, { 29, 46 }, { 30, 55 },
            { 33, 47 }, { 34, 38 }, { 35, 43 }, { 36, 39 }, { 39, 59 }, { 40, 57 }, { 46, 52 },
            { 49, 51 } };

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);

        VertexCoverAlgorithm<Integer> mvc = createSolver(graph);
        VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();

        assertEquals(vertexCover.getWeight(), 16.0, 0);
    }
}
