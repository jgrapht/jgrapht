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

import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.MINUS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.NONE;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVTree}
 *
 * @author Timofey Chudakov
 */
public class BlossomVTreeTest
{

    private BlossomVOptions noneOptions = new BlossomVOptions(NONE);

    @Test
    public void testTreeNodeIterator()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);
        BlossomVNode node3 = vertexMap.get(3);
        BlossomVNode node4 = vertexMap.get(4);
        BlossomVNode node5 = vertexMap.get(5);
        BlossomVNode node6 = vertexMap.get(6);
        BlossomVNode node7 = vertexMap.get(7);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge36 = edgeMap.get(e36);
        BlossomVEdge edge67 = edgeMap.get(e67);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, true, false);

        int i = 0;
        Set<BlossomVNode> actualNodes = new HashSet<>();
        for (BlossomVTree.TreeNodeIterator iterator = node1.tree.treeNodeIterator();
            iterator.hasNext();)
        {
            i++;
            actualNodes.add(iterator.next());
        }
        assertEquals(7, i);
        assertEquals(
            new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5, node6, node7)),
            actualNodes);
    }

    @Test
    public void testTreeEdgeIterator()
    {
        BlossomVNode node1 = new BlossomVNode(-1); // positions doesn't matter here
        BlossomVNode node2 = new BlossomVNode(-1);
        BlossomVNode node3 = new BlossomVNode(-1);
        BlossomVNode node4 = new BlossomVNode(-1);
        BlossomVNode node5 = new BlossomVNode(-1);
        BlossomVTree tree1 = new BlossomVTree(node1);
        BlossomVTree tree2 = new BlossomVTree(node2);
        BlossomVTree tree3 = new BlossomVTree(node3);
        BlossomVTree tree4 = new BlossomVTree(node4);
        BlossomVTree tree5 = new BlossomVTree(node5);
        BlossomVTreeEdge treeEdge1 = BlossomVTree.addTreeEdge(tree1, tree2);
        BlossomVTreeEdge treeEdge2 = BlossomVTree.addTreeEdge(tree1, tree3);
        BlossomVTreeEdge treeEdge3 = BlossomVTree.addTreeEdge(tree4, tree1);
        BlossomVTreeEdge treeEdge4 = BlossomVTree.addTreeEdge(tree5, tree1);
        Set<BlossomVTreeEdge> expectedOutEdges = new HashSet<>(Arrays.asList(treeEdge1, treeEdge2));
        Set<BlossomVTreeEdge> expectedInEdges = new HashSet<>(Arrays.asList(treeEdge3, treeEdge4));
        Set<BlossomVTreeEdge> actualOutEdges = new HashSet<>();
        Set<BlossomVTreeEdge> actualInEdges = new HashSet<>();
        for (BlossomVTree.TreeEdgeIterator iterator = tree1.treeEdgeIterator();
            iterator.hasNext();)
        {
            BlossomVTreeEdge edge = iterator.next();
            int currentDir = iterator.getCurrentDirection();
            if (currentDir == 0) {
                actualOutEdges.add(edge);
            } else {
                actualInEdges.add(edge);
            }
            assertSame(tree1, edge.head[1 - currentDir]);
        }
        assertEquals(expectedOutEdges, actualOutEdges);
        assertEquals(expectedInEdges, actualInEdges);
    }

    @Test
    public void testAddMinusBlossom()
    {
        BlossomVNode root = new BlossomVNode(-1);
        BlossomVTree tree = new BlossomVTree(root);

        BlossomVNode blossom = new BlossomVNode(-1);
        blossom.label = MINUS;
        blossom.isOuter = true;
        blossom.isBlossom = true;
        tree.addMinusBlossom(blossom);

        assertNotNull(blossom.handle);
        assertSame(blossom.handle.getValue(), blossom);
    }

}
