/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.*;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatchingTest.checkMatchingAndDualSolution;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVInitializer}
 *
 * @author Timofey Chudakov
 */
public class BlossomVInitializerTest
{

    private BlossomVOptions fractionalOptions = new BlossomVOptions(FRACTIONAL);

    /**
     * Tests greedy initialization
     */
    @Test
    public void testGreedyInitialization()
    {
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state =
            initializer.initialize(new BlossomVOptions(GREEDY));
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);

        BlossomVEdge edge12 = edgeMap.get(e12);

        assertEquals(5, node1.dual + node2.dual, EPS);
        assertEquals(0, edge12.slack, EPS);

        assertTrue(node1.isOuter);
        assertTrue(node2.isOuter);

        assertFalse(node1.isTreeRoot);
        assertFalse(node2.isTreeRoot);

        assertEquals(2, state.nodeNum);
        assertEquals(1, state.edgeNum);
        assertEquals(0, state.treeNum);

        assertEquals(Collections.emptySet(), BlossomVDebugger.getTreeRoots(state));
        assertEquals(
            new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.getEdgesOf(node1));
        assertEquals(
            new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.getEdgesOf(node2));

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
    }

    /**
     * Tests simple initialization
     */
    @Test
    public void testSimpleInitialization()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 3);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 5);
        graph.addVertex(7);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state =
            initializer.initialize(new BlossomVOptions(NONE));
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        assertEquals(7, state.nodeNum);
        assertEquals(7, state.treeNum);
        assertEquals(5, state.edgeNum);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);
        BlossomVNode node3 = vertexMap.get(3);
        BlossomVNode node4 = vertexMap.get(4);
        BlossomVNode node5 = vertexMap.get(5);
        BlossomVNode node6 = vertexMap.get(6);
        BlossomVNode node7 = vertexMap.get(7);

        BlossomVTree tree1 = node1.tree;
        BlossomVTree tree2 = node2.tree;
        BlossomVTree tree3 = node3.tree;
        BlossomVTree tree4 = node4.tree;
        BlossomVTree tree5 = node5.tree;
        BlossomVTree tree6 = node6.tree;
        BlossomVTree tree7 = node7.tree;

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge25 = edgeMap.get(e25);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);

        assertEquals(0, node1.dual, EPS);
        assertEquals(0, node2.dual, EPS);
        assertEquals(0, node3.dual, EPS);
        assertEquals(0, node4.dual, EPS);
        assertEquals(0, node5.dual, EPS);
        assertEquals(0, node6.dual, EPS);
        assertEquals(0, node7.dual, EPS);

        assertTrue(node1.isOuter);
        assertTrue(node2.isOuter);
        assertTrue(node3.isOuter);
        assertTrue(node4.isOuter);
        assertTrue(node5.isOuter);
        assertTrue(node6.isOuter);
        assertTrue(node7.isOuter);

        assertTrue(node1.isTreeRoot);
        assertTrue(node2.isTreeRoot);
        assertTrue(node3.isTreeRoot);
        assertTrue(node4.isTreeRoot);
        assertTrue(node5.isTreeRoot);
        assertTrue(node6.isTreeRoot);
        assertTrue(node7.isTreeRoot);

        assertEquals(1, edge12.slack, EPS);
        assertEquals(2, edge23.slack, EPS);
        assertEquals(3, edge25.slack, EPS);
        assertEquals(4, edge45.slack, EPS);
        assertEquals(5, edge56.slack, EPS);

        Set<BlossomVNode> actualRoots = BlossomVDebugger.getTreeRoots(state);
        Collection<BlossomVNode> expectedRoots = vertexMap.values();
        assertEquals(expectedRoots.size(), actualRoots.size());
        assertTrue(actualRoots.containsAll(expectedRoots));

        assertEquals(
            new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.getEdgesOf(node1));
        assertEquals(
            new HashSet<>(Arrays.asList(edge12, edge23, edge25)),
            BlossomVDebugger.getEdgesOf(node2));
        assertEquals(
            new HashSet<>(Collections.singletonList(edge23)), BlossomVDebugger.getEdgesOf(node3));
        assertEquals(
            new HashSet<>(Collections.singletonList(edge45)), BlossomVDebugger.getEdgesOf(node4));
        assertEquals(
            new HashSet<>(Arrays.asList(edge25, edge45, edge56)),
            BlossomVDebugger.getEdgesOf(node5));
        assertEquals(
            new HashSet<>(Collections.singletonList(edge56)), BlossomVDebugger.getEdgesOf(node6));
        assertEquals(new HashSet<>(), BlossomVDebugger.getEdgesOf(node7));

        assertEquals(1, BlossomVDebugger.getTreeEdgesOf(tree1).size());
        assertEquals(3, BlossomVDebugger.getTreeEdgesOf(tree2).size());
        assertEquals(1, BlossomVDebugger.getTreeEdgesOf(tree3).size());
        assertEquals(1, BlossomVDebugger.getTreeEdgesOf(tree4).size());
        assertEquals(3, BlossomVDebugger.getTreeEdgesOf(tree5).size());
        assertEquals(1, BlossomVDebugger.getTreeEdgesOf(tree6).size());
        assertEquals(0, BlossomVDebugger.getTreeEdgesOf(tree7).size());
    }

    /**
     * Tests fractional matching initialization on a bipartite graph with $V = {0,1,2}\cup{4,5,6}$
     */
    @Test
    public void testFractionalInitialization1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 3, 8 }, { 0, 4, 3 }, { 0, 5, 3 }, { 1, 3, 2 },
            { 1, 4, 5 }, { 1, 5, 2 }, { 2, 3, 7 }, { 2, 4, 3 }, { 2, 5, 4 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();
        KolmogorovMinimumWeightPerfectMatching.Statistics statistics =
            perfectMatching.getStatistics();

        assertEquals(8, matching.getWeight(), EPS);
        assertEquals(0, statistics.growNum);
        assertEquals(0, statistics.shrinkNum);
        assertEquals(0, statistics.expandNum);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on a bipartite graph with $V = {0,1,2}\cup{4,5,6}$
     */
    @Test
    public void testFractionalInitialization2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 3, 4 }, { 0, 4, 4 }, { 0, 5, 4 }, { 1, 3, 5 },
            { 1, 4, 8 }, { 1, 5, 10 }, { 2, 3, 4 }, { 2, 4, 6 }, { 2, 5, 5 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();
        KolmogorovMinimumWeightPerfectMatching.Statistics statistics =
            perfectMatching.getStatistics();

        assertEquals(14, matching.getWeight(), EPS);
        assertEquals(0, statistics.growNum);
        assertEquals(0, statistics.shrinkNum);
        assertEquals(0, statistics.expandNum);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on a bipartite graph with $V =
     * {0,1,2,3}\cup{4,5,6,7}$
     */
    @Test
    public void testFractionalInitialization3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 5, 6 }, { 0, 6, 8 }, { 1, 5, 5 }, { 1, 6, 5 },
            { 1, 7, 3 }, { 2, 4, 2 }, { 2, 5, 1 }, { 2, 6, 8 }, { 3, 5, 5 }, { 3, 7, 9 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();
        KolmogorovMinimumWeightPerfectMatching.Statistics statistics =
            perfectMatching.getStatistics();

        assertEquals(18, matching.getWeight(), EPS);
        assertEquals(0, statistics.growNum);
        assertEquals(0, statistics.shrinkNum);
        assertEquals(0, statistics.expandNum);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on a bipartite graph with $V =
     * {0,1,2,3}\cup{4,5,6,7}$
     */
    @Test
    public void testFractionalInitialization4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 5, 2 }, { 0, 6, 2 }, { 0, 7, 1 }, { 1, 4, 6 },
            { 1, 7, 10 }, { 2, 4, 7 }, { 2, 6, 8 }, { 2, 7, 10 }, { 3, 4, 5 }, { 3, 5, 9 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();
        KolmogorovMinimumWeightPerfectMatching.Statistics statistics =
            perfectMatching.getStatistics();

        assertEquals(24, matching.getWeight(), EPS);
        assertEquals(0, statistics.growNum);
        assertEquals(0, statistics.shrinkNum);
        assertEquals(0, statistics.expandNum);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on triangulation of 8 points
     */
    @Test
    public void testFractionalInitialization5()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 1, 0, 2 }, { 1, 2, 5 }, { 0, 2, 4 }, { 1, 4, 5 },
            { 2, 4, 2 }, { 1, 3, 2 }, { 1, 5, 4 }, { 3, 5, 3 }, { 4, 5, 5 }, { 3, 6, 4 },
            { 5, 6, 2 }, { 5, 7, 3 }, { 6, 7, 4 }, { 4, 7, 4 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();

        assertEquals(11, matching.getWeight(), EPS);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on triangulation of 8 points
     */
    @Test
    public void testFractionalInitialization6()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 1, 5 }, { 0, 2, 9 }, { 1, 2, 6 }, { 2, 3, 4 },
            { 2, 4, 5 }, { 3, 4, 3 }, { 1, 4, 8 }, { 1, 5, 8 }, { 0, 5, 11 }, { 4, 5, 7 },
            { 4, 6, 3 }, { 5, 6, 5 }, { 6, 7, 3 }, { 5, 7, 6 }, { 4, 7, 6 }, { 3, 7, 9 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();

        assertEquals(18, matching.getWeight(), EPS);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());

    }

    /**
     * Tests fractional matching initialization on triangulation of 8 points
     */
    @Test
    public void testFractionalInitialization7()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges =
            new int[][] { { 0, 1, 2 }, { 0, 2, 8 }, { 1, 2, 7 }, { 0, 4, 8 }, { 1, 4, 7 },
                { 2, 4, 6 }, { 2, 3, 9 }, { 2, 5, 6 }, { 3, 5, 6 }, { 2, 6, 6 }, { 5, 6, 5 },
                { 4, 6, 2 }, { 5, 7, 9 }, { 6, 7, 7 }, { 3, 7, 14 }, { 4, 7, 7 }, { 0, 7, 15 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();

        assertEquals(21, matching.getWeight(), EPS);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on triangulation of 8 points
     */
    @Test
    public void testFractionalInitialization8()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 1, 7 }, { 0, 2, 8 }, { 0, 3, 8 }, { 1, 3, 4 },
            { 1, 5, 9 }, { 1, 6, 13 }, { 2, 4, 6 }, { 2, 3, 11 }, { 3, 4, 10 }, { 3, 5, 6 },
            { 4, 5, 8 }, { 4, 7, 7 }, { 5, 6, 4 }, { 5, 7, 4 }, { 6, 7, 1 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();

        assertEquals(20, matching.getWeight(), EPS);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());
    }

    /**
     * Tests fractional matching initialization on triangulation of 8 points
     */
    @Test
    public void testFractionalInitialization9()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int[][] edges = new int[][] { { 0, 1, 4 }, { 0, 2, 4 }, { 0, 5, 14 }, { 1, 2, 3 },
            { 1, 3, 1 }, { 1, 5, 11 }, { 2, 3, 4 }, { 2, 4, 4 }, { 2, 7, 11 }, { 3, 4, 1 },
            { 3, 5, 10 }, { 4, 5, 10 }, { 4, 6, 10 }, { 4, 7, 9 }, { 5, 6, 3 }, { 6, 7, 8 } };
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching =
            new KolmogorovMinimumWeightPerfectMatching<>(graph, fractionalOptions);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            perfectMatching.getMatching();

        assertEquals(17, matching.getWeight(), EPS);
        assertTrue(perfectMatching.testOptimality());
        checkMatchingAndDualSolution(matching, perfectMatching.getDualSolution());

    }

}
