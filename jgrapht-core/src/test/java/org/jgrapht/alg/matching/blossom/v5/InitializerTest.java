/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;
import static org.junit.Assert.*;

public class InitializerTest {

    @Test
    public void testGreedyInitialization() {
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new Options(GREEDY));

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);

        Edge edge12 = state.edgeMap.get(e12);

        assertEquals(5, node1.dual + node2.dual, EPS);
        assertEquals(0, edge12.slack, EPS);

        assertTrue(node1.isOuter);
        assertTrue(node2.isOuter);

        assertFalse(node1.isTreeRoot);
        assertFalse(node2.isTreeRoot);

        assertEquals(2, state.nodeNum);
        assertEquals(1, state.edgeNum);
        assertEquals(0, state.treeNum);

        assertEquals(Collections.emptySet(), BlossomVDebugger.treeRoots(state));
        assertEquals(new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.edgesOf(node2));

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
    }

    @Test
    public void testInitializeNone() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 3);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 5);
        graph.addVertex(7);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new Options(NONE));

        assertEquals(7, state.nodeNum);
        assertEquals(7, state.treeNum);
        assertEquals(5, state.edgeNum);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);

        Tree tree1 = node1.tree;
        Tree tree2 = node2.tree;
        Tree tree3 = node3.tree;
        Tree tree4 = node4.tree;
        Tree tree5 = node5.tree;
        Tree tree6 = node6.tree;
        Tree tree7 = node7.tree;


        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge25 = state.edgeMap.get(e25);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);

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

        Set<Node> actualRoots = BlossomVDebugger.treeRoots(state);
        Collection<Node> expectedRoots = state.vertexMap.values();
        assertEquals(expectedRoots.size(), actualRoots.size());
        assertTrue(actualRoots.containsAll(expectedRoots));

        assertEquals(new HashSet<>(Collections.singletonList(edge12)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge23, edge25)), BlossomVDebugger.edgesOf(node2));
        assertEquals(new HashSet<>(Collections.singletonList(edge23)), BlossomVDebugger.edgesOf(node3));
        assertEquals(new HashSet<>(Collections.singletonList(edge45)), BlossomVDebugger.edgesOf(node4));
        assertEquals(new HashSet<>(Arrays.asList(edge25, edge45, edge56)), BlossomVDebugger.edgesOf(node5));
        assertEquals(new HashSet<>(Collections.singletonList(edge56)), BlossomVDebugger.edgesOf(node6));
        assertEquals(new HashSet<>(), BlossomVDebugger.edgesOf(node7));

        assertEquals(1, BlossomVDebugger.treeEdgesOf(tree1).size());
        assertEquals(3, BlossomVDebugger.treeEdgesOf(tree2).size());
        assertEquals(1, BlossomVDebugger.treeEdgesOf(tree3).size());
        assertEquals(1, BlossomVDebugger.treeEdgesOf(tree4).size());
        assertEquals(3, BlossomVDebugger.treeEdgesOf(tree5).size());
        assertEquals(1, BlossomVDebugger.treeEdgesOf(tree6).size());
        assertEquals(0, BlossomVDebugger.treeEdgesOf(tree7).size());
    }


}
