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
package org.jgrapht.alg.matching;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.jgrapht.alg.matching.Initializer.InitializationType.NONE;
import static org.junit.Assert.*;

public class TreeEdgeTest {

    @Test
    public void testGetCurrentPlusMinusHeap() {
        Node root1 = new Node();
        Node root2 = new Node();
        Tree tree1 = new Tree(root1);
        Tree tree2 = new Tree(root2);
        TreeEdge treeEdge = State.addTreeEdge(tree1, tree2);

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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(new KolmogorovMinimumWeightPerfectMatching.Options(NONE));

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);

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

        assertEquals(new HashSet<>(Collections.singletonList(treeEdge13)), BlossomVDebugger.treeEdgesOf(tree1));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.treeEdgesOf(tree2));

        treeEdge13.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.treeEdgesOf(tree1).isEmpty());
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.treeEdgesOf(tree2));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge23)), BlossomVDebugger.treeEdgesOf(tree3));

        treeEdge23.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.treeEdgesOf(tree2).isEmpty());
        assertTrue(BlossomVDebugger.treeEdgesOf(tree3).isEmpty());

    }
}
