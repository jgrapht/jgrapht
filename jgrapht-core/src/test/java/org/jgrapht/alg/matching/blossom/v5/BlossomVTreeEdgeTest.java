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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.NONE;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVTreeEdge}
 *
 * @author Timofey Chudakov
 */
public class BlossomVTreeEdgeTest
{

    @Test
    public void testGetCurrentPlusMinusHeap()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state =
            initializer.initialize(new BlossomVOptions(NONE));
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node2.tree);

        assertNotSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(0));
        assertNotSame(treeEdge.getCurrentMinusPlusHeap(1), treeEdge.getCurrentPlusMinusHeap(1));
        assertSame(treeEdge.getCurrentPlusMinusHeap(0), treeEdge.getCurrentMinusPlusHeap(1));
        assertSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(1));
    }

    @Test
    public void testRemoveFromTreeEdgeList()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state =
            initializer.initialize(new BlossomVOptions(NONE));
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);
        BlossomVNode node3 = vertexMap.get(3);

        BlossomVTree tree1 = node1.tree;
        BlossomVTree tree2 = node2.tree;
        BlossomVTree tree3 = node3.tree;

        BlossomVTreeEdge treeEdge12 = BlossomVDebugger.getTreeEdge(tree1, tree2);
        BlossomVTreeEdge treeEdge13 = BlossomVDebugger.getTreeEdge(tree1, tree3);
        BlossomVTreeEdge treeEdge23 = BlossomVDebugger.getTreeEdge(tree2, tree3);

        assertNotNull(treeEdge12);
        assertNotNull(treeEdge13);
        assertNotNull(treeEdge23);

        treeEdge12.removeFromTreeEdgeList();

        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge13)),
            BlossomVDebugger.getTreeEdgesOf(tree1));
        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge23)),
            BlossomVDebugger.getTreeEdgesOf(tree2));

        treeEdge13.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree1).isEmpty());
        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge23)),
            BlossomVDebugger.getTreeEdgesOf(tree2));
        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge23)),
            BlossomVDebugger.getTreeEdgesOf(tree3));

        treeEdge23.removeFromTreeEdgeList();

        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree2).isEmpty());
        assertTrue(BlossomVDebugger.getTreeEdgesOf(tree3).isEmpty());

    }
}
