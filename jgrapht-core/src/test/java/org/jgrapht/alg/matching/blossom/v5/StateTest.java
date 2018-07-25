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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;
import static org.junit.Assert.assertEquals;

public class StateTest {

    @Test
    public void testAddTreeEdge() {
        Tree tree1 = new Tree(new Node(-1)); // TODO change this test
        Tree tree2 = new Tree(new Node(-1));
        TreeEdge treeEdge = State.addTreeEdge(tree1, tree2);
        int currentDir = tree2.currentDirection;
        assertEquals(tree2, treeEdge.head[currentDir]);
        assertEquals(tree1, treeEdge.head[1 - currentDir]);
    }

    @Test
    public void testMoveEdge() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new Options(NONE));
        Map<Integer, Node> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, Edge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        Node node1 = vertexMap.get(1);
        Node node2 = vertexMap.get(2);
        Node node3 = vertexMap.get(3);

        Edge edge12 = edgeMap.get(e12);
        Edge edge13 = edgeMap.get(e13);
        Edge edge23 = edgeMap.get(e23);

        state.moveEdgeTail(node2, node3, edge12);
        assertEquals(node3, edge12.getOpposite(node1));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(Collections.singletonList(edge23)), BlossomVDebugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), BlossomVDebugger.edgesOf(node3));

        state.moveEdgeTail(node2, node1, edge23);
        assertEquals(node1, edge13.getOpposite(node3));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(), BlossomVDebugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), BlossomVDebugger.edgesOf(node3));
    }

}
