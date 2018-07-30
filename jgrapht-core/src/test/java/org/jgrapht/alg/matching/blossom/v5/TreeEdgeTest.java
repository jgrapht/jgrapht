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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link TreeEdge}
 *
 * @author Timofey Chudakov
 */
public class TreeEdgeTest {

    @Test
    public void testGetCurrentPlusMinusHeap() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new Options(NONE));
        Map<Integer, Node> vertexMap = BlossomVDebugger.getVertexMap(state);

        Node node1 = vertexMap.get(1);
        Node node2 = vertexMap.get(2);

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node2.tree);

        assertNotSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(0));
        assertNotSame(treeEdge.getCurrentMinusPlusHeap(1), treeEdge.getCurrentPlusMinusHeap(1));
        assertSame(treeEdge.getCurrentPlusMinusHeap(0), treeEdge.getCurrentMinusPlusHeap(1));
        assertSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(1));
    }

    @Test
    public void testRemoveFromTreeEdgeList() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new Options(NONE));
        Map<Integer, Node> vertexMap = BlossomVDebugger.getVertexMap(state);

        Node node1 = vertexMap.get(1);
        Node node2 = vertexMap.get(2);
        Node node3 = vertexMap.get(3);

        Tree tree1 = node1.tree;
        Tree tree2 = node2.tree;
        Tree tree3 = node3.tree;

        TreeEdge treeEdge12 = BlossomVDebugger.getTreeEdge(tree1, tree2);
        TreeEdge treeEdge13 = BlossomVDebugger.getTreeEdge(tree1, tree3);
        TreeEdge treeEdge23 = BlossomVDebugger.getTreeEdge(tree2, tree3);

        assertNotNull(treeEdge12);
        assertNotNull(treeEdge13);
        assertNotNull(treeEdge23);

        treeEdge12.removeFromTreeEdgeList();

        assertEquals(new HashSet<>(Collections.singletonList(treeEdge13)), BlossomVDebugger.getTreeEdgesOf(tree1));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.getTreeEdgesOf(tree2));

        treeEdge13.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree1).isEmpty());
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.getTreeEdgesOf(tree2));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.getTreeEdgesOf(tree3));

        treeEdge23.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree2).isEmpty());
        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree3).isEmpty());

    }
}
