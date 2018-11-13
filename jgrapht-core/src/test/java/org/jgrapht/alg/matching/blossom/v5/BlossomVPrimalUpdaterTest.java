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
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVPrimalUpdater}
 *
 * @author Timofey Chudakov
 */
public class BlossomVPrimalUpdaterTest
{

    private BlossomVOptions noneOptions = new BlossomVOptions(NONE);

    /**
     * Tests one grow operation
     */
    @Test
    public void testGrow1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);

        primalUpdater.augment(edge23);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        assertEquals(1, state.statistics.growNum);

        assertEquals(1, state.treeNum);
        assertEquals(node1.tree, node2.tree);
        assertEquals(node1.tree, node3.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node3.isPlusNode());

        assertEquals(node2.getTreeParent(), node1);
        assertEquals(node3.getTreeParent(), node2);
        assertEquals(node1.firstTreeChild, node2);
        assertEquals(node2.firstTreeChild, node3);
    }

    /**
     * Tests updating of the tree structure (tree parent, node's tree reference, node's label). Uses
     * recursive grow flag
     */
    @Test
    public void testGrow2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge edge34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge edge45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge edge36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge edge67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);

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
        BlossomVTree tree = node1.tree;

        primalUpdater.augment(edgeMap.get(edge45));
        primalUpdater.augment(edgeMap.get(edge23));
        primalUpdater.augment(edgeMap.get(edge67));
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edgeMap.get(edge12), true, false);
        node1.tree.clearCurrentEdges();

        assertEquals(tree, node2.tree);
        assertEquals(tree, node3.tree);
        assertEquals(tree, node4.tree);
        assertEquals(tree, node5.tree);
        assertEquals(tree, node6.tree);
        assertEquals(tree, node7.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node4.isMinusNode());
        assertTrue(node6.isMinusNode());
        assertTrue(node3.isPlusNode());
        assertTrue(node5.isPlusNode());
        assertTrue(node7.isPlusNode());

        assertEquals(node1.firstTreeChild, node2);
        assertEquals(node2.firstTreeChild, node3);
        assertTrue(node3.firstTreeChild == node4 || node3.firstTreeChild == node6);
        assertEquals(node4.firstTreeChild, node5);
        assertEquals(node6.firstTreeChild, node7);

        assertEquals(node2.getTreeParent(), node1);
        assertEquals(node3.getTreeParent(), node2);
        assertEquals(node4.getTreeParent(), node3);
        assertEquals(node5.getTreeParent(), node4);
        assertEquals(node6.getTreeParent(), node3);
        assertEquals(node7.getTreeParent(), node6);
    }

    /**
     * Tests proper addition of new tree edges without duplicates, and size of heaps in the tree
     * edges
     */
    @Test
    public void testGrow3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        //
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);
        // other edges
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node6 = vertexMap.get(6);
        BlossomVNode node7 = vertexMap.get(7);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        Set<BlossomVTreeEdge> treeEdges1 =
            BlossomVDebugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges1.size());
        BlossomVTreeEdge treeEdge1 = treeEdges1.iterator().next();
        assertEquals(1, treeEdge1.plusPlusEdges.size());
        assertEquals(1, BlossomVDebugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge34, false, false);
        node1.tree.clearCurrentEdges();

        Set<BlossomVTreeEdge> treeEdges2 =
            BlossomVDebugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges2.size());
        BlossomVTreeEdge treeEdge2 = treeEdges2.iterator().next();
        assertEquals(treeEdge1, treeEdge2);
        assertEquals(2, treeEdge1.plusPlusEdges.size());
        assertEquals(2, BlossomVDebugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        Set<BlossomVTreeEdge> treeEdges3 =
            BlossomVDebugger.getTreeEdgesBetween(node1.tree, node7.tree);
        assertEquals(1, treeEdges3.size());
        BlossomVTreeEdge treeEdge3 = treeEdges3.iterator().next();
        assertEquals(1, treeEdge3.plusPlusEdges.size());
    }

    /**
     * Tests addition of new (-, +), (+,-) and (+, +) cross-tree edges to appropriate heaps and
     * addition of a new tree edge
     */
    @Test
    public void testGrow4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // in-tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        // neighbor tree
        DefaultWeightedEdge e68 = Graphs.addEdgeWithVertices(graph, 6, 8, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);
        // cross-tree and infinity edges
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 0);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node4 = vertexMap.get(4);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge78 = edgeMap.get(e78);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge56);
        primalUpdater.augment(edge78);
        node4.tree.setCurrentEdges();
        primalUpdater.grow(edge45, false, false);
        node4.tree.clearCurrentEdges();

        assertEquals(4, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        assertEquals(1, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node1.tree.plusPlusEdges.size());

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        int dir = BlossomVDebugger.getDirToOpposite(treeEdge, node1.tree);

        assertEquals(2, treeEdge.getCurrentMinusPlusHeap(dir).size());
        assertEquals(1, treeEdge.getCurrentPlusMinusHeap(dir).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
    }

    /**
     * Tests updating of the slacks of the incident edges to "-" and "+" grow nodes and updating
     * their keys in corresponding heaps
     */
    @Test
    public void testGrow5()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 5);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 3);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 3);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 3);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge26 = edgeMap.get(e26);
        BlossomVEdge edge35 = edgeMap.get(e35);
        BlossomVEdge edge36 = edgeMap.get(e36);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        node5.tree.eps = 1;
        node6.tree.eps = 1;
        primalUpdater.augment(edge56);
        node4.tree.eps = 3;
        node4.tree.setCurrentEdges();
        primalUpdater.grow(edge45, false, false);
        node4.tree.clearCurrentEdges();

        assertEquals(4, node5.dual, EPS);
        assertEquals(-2, node6.dual, EPS);

        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge56.slack, EPS);
        assertEquals(4, edge24.slack, EPS);
        assertEquals(4, edge26.slack, EPS);
        assertEquals(-2, edge35.slack, EPS);
        assertEquals(4, edge36.slack, EPS);

        // edge35 is (-, inf) edge, so it isn't present in any heap
        assertEquals(4, edge24.handle.getKey(), EPS);
        assertEquals(4, edge26.handle.getKey(), EPS);
        assertEquals(4, edge26.handle.getKey(), EPS);

        assertEquals(3, node4.tree.plusInfinityEdges.size());

        node1.tree.eps = 3;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        assertEquals(4, node2.dual, EPS);
        assertEquals(-2, node3.dual, EPS);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(1, edge24.slack, EPS);
        assertEquals(1, edge26.slack, EPS);
        assertEquals(1, edge35.slack, EPS);
        assertEquals(7, edge36.slack, EPS);

        assertEquals(1, edge24.handle.getKey(), EPS);
        assertEquals(1, edge26.handle.getKey(), EPS);
        assertEquals(1, edge35.handle.getKey(), EPS);
        assertEquals(7, edge36.handle.getKey(), EPS);

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(2, BlossomVDebugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, BlossomVDebugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
    }

    /**
     * Tests addition of (+, +) in-tree edges, (+, inf) edges and "-" pseudonodes to appropriate
     * heaps and removal of former (+, inf) edges
     */
    @Test
    public void testGrow6()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e71 = Graphs.addEdgeWithVertices(graph, 7, 1, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge71 = edgeMap.get(e71);

        primalUpdater.augment(edge34);
        node2.tree.setCurrentEdges();
        primalUpdater.grow(edge23, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge24, false);
        blossom.tree.clearCurrentEdges();

        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);

        assertEquals(3, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node1.tree.minusBlossoms.size());
        assertEquals(0, node1.tree.plusPlusEdges.size());

        primalUpdater.grow(edge71, false, false);

        assertEquals(1, node1.tree.minusBlossoms.size());
        assertEquals(1, node1.tree.plusPlusEdges.size());
        assertEquals(0, node1.tree.plusInfinityEdges.size());
    }

    /**
     * Tests finding a blossom root
     */
    @Test
    public void testFindBlossomRoot()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);

        BlossomVState<Integer, DefaultWeightedEdge> state =
            new BlossomVInitializer<>(graph).initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge16 = edgeMap.get(e16);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge57 = edgeMap.get(e57);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge34, false, false);
        primalUpdater.grow(edge16, false, false);
        node1.tree.clearCurrentEdges();

        BlossomVNode root = primalUpdater.findBlossomRoot(edge57);

        assertEquals(root, node1);
    }

    /**
     * Tests augment operation on a small test case. Checks updating of the matching, changing
     * labels, updating edge slack and nodes dual variables
     */
    @Test
    public void testAugment1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);

        node1.tree.eps = 1;
        node2.tree.eps = 3;

        BlossomVEdge edge12 = edgeMap.get(e12);

        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        primalUpdater.augment(edge12);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node1.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node2.label);
        assertEquals(0, state.treeNum);
        assertEquals(0, edge12.slack, EPS);
        assertEquals(1, node1.dual, EPS);
        assertEquals(3, node2.dual, EPS);
    }

    /**
     * Tests augment operation. Checks labeling, updated edges' slacks, matching, and tree structure
     */
    @Test
    public void testAugment2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 3);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);

        node2.tree.eps = 2;
        node3.tree.eps = 1;

        primalUpdater.augment(edge23);

        Assert.assertEquals(BlossomVNode.Label.INFINITY, node2.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node3.label);
        assertEquals(2, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(3, edge34.slack, EPS);
        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusInfinityEdges.size());
        assertTrue(BlossomVDebugger.getTreeEdgesOf(node1.tree).isEmpty());

        node4.tree.eps = 1;
        node5.tree.eps = 2;

        primalUpdater.augment(edge45);

        Assert.assertEquals(BlossomVNode.Label.INFINITY, node4.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node5.label);
        assertEquals(2, edge34.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(2, edge56.slack, EPS);
        assertEquals(1, node6.tree.plusInfinityEdges.size());
        assertTrue(BlossomVDebugger.getTreeEdgesOf(node6.tree).isEmpty());

        node1.tree.eps = 2;
        node6.tree.eps = 2;

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        assertEquals(node1.tree, node2.tree);
        assertEquals(node1.tree, node3.tree);

        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge56, false, false);
        node6.tree.clearCurrentEdges();

        node1.tree.eps += 1;
        node1.tree.eps += 1;

        primalUpdater.augment(edge34);

        Assert.assertEquals(BlossomVNode.Label.INFINITY, node1.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node2.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node3.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node4.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node5.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node6.label);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge34, node3.matched);
        assertEquals(edge34, node4.matched);
        assertEquals(edge56, node5.matched);
        assertEquals(edge56, node6.matched);
    }

    /**
     * Tests augment operation on a big test case. Checks matching and labeling
     */
    @Test
    public void testAugment3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e710 = Graphs.addEdgeWithVertices(graph, 7, 10, 2);

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge36 = edgeMap.get(e36);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge18 = edgeMap.get(e18);
        BlossomVEdge edge89 = edgeMap.get(e89);
        BlossomVEdge edge710 = edgeMap.get(e710);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge89);

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge18, true, false);
        primalUpdater.grow(edge12, true, false);
        node1.tree.clearCurrentEdges();

        node1.tree.eps = 2;

        primalUpdater.augment(edge710);

        Assert.assertEquals(BlossomVNode.Label.INFINITY, node1.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node2.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node3.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node4.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node5.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node6.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node7.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node8.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node9.label);
        Assert.assertEquals(BlossomVNode.Label.INFINITY, node10.label);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge36, node3.matched);
        assertEquals(edge36, node6.matched);
        assertEquals(edge45, node4.matched);
        assertEquals(edge45, node5.matched);
        assertEquals(edge89, node8.matched);
        assertEquals(edge89, node9.matched);

        assertEquals(0, edge710.slack, EPS);
    }

    /**
     * Tests tree edges
     */
    @Test
    public void testAugment4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e41 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVEdge edge12 = edgeMap.get(e12);

        BlossomVTree tree3 = vertexMap.get(3).tree;
        BlossomVTree tree4 = vertexMap.get(4).tree;

        BlossomVTreeEdge treeEdge34 = BlossomVDebugger.getTreeEdge(tree3, tree4);

        primalUpdater.augment(edge12);

        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge34)),
            BlossomVDebugger.getTreeEdgesOf(tree3));
        assertEquals(
            new HashSet<>(Collections.singletonList(treeEdge34)),
            BlossomVDebugger.getTreeEdgesOf(tree4));
    }

    /**
     * Test shrink on a small test case. Checks updates tree structure, marking, and other meta data
     */
    @Test
    public void testShrink1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge14 = edgeMap.get(e14);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge24 = edgeMap.get(e24);

        primalUpdater.augment(edge23);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);
        node1.tree.clearCurrentEdges();

        assertEquals(1, state.statistics.shrinkNum);
        assertEquals(1, state.blossomNum);

        assertFalse(node1.isTreeRoot);

        assertEquals(
            new HashSet<>(Arrays.asList(edge12, edge13)), BlossomVDebugger.getEdgesOf(node1));
        assertEquals(
            new HashSet<>(Arrays.asList(edge12, edge23)), BlossomVDebugger.getEdgesOf(node2));
        assertEquals(
            new HashSet<>(Arrays.asList(edge14, edge24)), BlossomVDebugger.getEdgesOf(blossom));

        assertEquals(blossom, node1.blossomParent);
        assertEquals(blossom, node2.blossomParent);
        assertEquals(blossom, node3.blossomParent);

        assertEquals(blossom, node1.blossomGrandparent);
        assertEquals(blossom, node2.blossomGrandparent);
        assertEquals(blossom, node3.blossomGrandparent);

        assertEquals(node1, edge14.getCurrentOriginal(blossom));
        assertEquals(node4, edge14.getCurrentOriginal(node4));
        assertEquals(blossom, edge14.getOpposite(node4));
        assertEquals(node4, edge14.getOpposite(blossom));

        assertEquals(node4, edge24.getCurrentOriginal(node4));
        assertEquals(node2, edge24.getCurrentOriginal(blossom));
        assertEquals(blossom, edge24.getOpposite(node4));
        assertEquals(node4, edge24.getOpposite(blossom));

        assertEquals(blossom, node1.tree.root);
        assertTrue(blossom.isOuter);

        assertFalse(node1.isMarked);
        assertFalse(node2.isMarked);
        assertFalse(node3.isMarked);
    }

    /**
     * Tests updating of the slacks after blossom shrinking and updating of the edge.handle.getKey()
     */
    @Test
    public void testShrink2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 4);

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
        BlossomVTree tree1 = node1.tree;
        BlossomVTree tree4 = node4.tree;

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge14 = edgeMap.get(e14);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge24 = edgeMap.get(e24);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        tree1.setCurrentEdges();
        node1.tree.eps = 3;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);
        node1.tree.clearCurrentEdges();

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge13.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(4, edge14.slack, EPS);
        assertEquals(6, edge24.slack, EPS);

        assertEquals(4, edge14.handle.getKey(), EPS);
        assertEquals(6, edge24.handle.getKey(), EPS);

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(tree1, tree4);
        assertNotNull(treeEdge);

        assertEquals(2, treeEdge.plusPlusEdges.size());
        assertEquals(-3, blossom.dual, EPS);
    }

    /**
     * Tests dual part of the shrink operation (updating edges' slacks, handle keys, etc.)
     */
    @Test
    public void testShrink3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // blossom edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 6);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 7);
        // neighbor tree edges
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 3);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 2);
        // cross-tree edges
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 10);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 8);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 9);
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 7);

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
        BlossomVNode node8 = vertexMap.get(8);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge51 = edgeMap.get(e51);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge78 = edgeMap.get(e78);
        BlossomVEdge edge16 = edgeMap.get(e16);
        BlossomVEdge edge57 = edgeMap.get(e57);
        BlossomVEdge edge58 = edgeMap.get(e58);
        BlossomVEdge edge47 = edgeMap.get(e47);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        node4.tree.eps = 1;
        node5.tree.eps = 3;
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.setCurrentEdges();
        node1.tree.eps = 4;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge51, false, false);
        node1.tree.clearCurrentEdges();
        node1.tree.eps += 2;

        node8.tree.eps = 2;
        primalUpdater.augment(edge78);
        node6.tree.setCurrentEdges();
        node6.tree.eps = 3;
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge67, false, false);
        node6.tree.clearCurrentEdges();

        node1.tree.setCurrentEdges();
        BlossomVNode blossom = primalUpdater.shrink(edge34, false);

        assertEquals(6, node1.dual, EPS);
        assertEquals(-1, node2.dual, EPS);
        assertEquals(3, node3.dual, EPS);
        assertEquals(3, node4.dual, EPS);
        assertEquals(1, node5.dual, EPS);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge51.slack, EPS);

        assertEquals(10, edge16.slack, EPS);
        assertEquals(10, edge57.slack, EPS);
        assertEquals(15, edge58.slack, EPS);
        assertEquals(7, edge47.slack, EPS);

        assertEquals(10, edge16.handle.getKey(), EPS);
        assertEquals(10, edge57.handle.getKey(), EPS);
        assertEquals(15, edge58.handle.getKey(), EPS);
        assertEquals(7, edge47.handle.getKey(), EPS);

        Set<BlossomVTreeEdge> treeEdges =
            BlossomVDebugger.getTreeEdgesBetween(blossom.tree, node6.tree);
        assertEquals(1, treeEdges.size());
        BlossomVTreeEdge treeEdge = treeEdges.iterator().next();
        assertEquals(2, treeEdge.plusPlusEdges.size());
        assertEquals(2, BlossomVDebugger.getPlusMinusHeap(treeEdge, blossom.tree).size());
        assertEquals(0, BlossomVDebugger.getMinusPlusHeap(treeEdge, blossom.tree).size());

        assertEquals(0, blossom.tree.plusPlusEdges.size());
    }

    /**
     * Tests addition and removal of cross-tree edges after blossom shrinking and addition of new
     * (+, inf) edges ("-" nodes now become "+" nodes)
     */
    @Test
    public void testShrink4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 1);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 0);
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 0);
        DefaultWeightedEdge e48 = Graphs.addEdgeWithVertices(graph, 4, 8, 0);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge51 = edgeMap.get(e51);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge78 = edgeMap.get(e78);
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge57 = edgeMap.get(e57);
        BlossomVEdge edge58 = edgeMap.get(e58);
        BlossomVEdge edge47 = edgeMap.get(e47);
        BlossomVEdge edge48 = edgeMap.get(e48);
        BlossomVEdge edge29 = edgeMap.get(e29);
        BlossomVEdge edge910 = edgeMap.get(e910);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge78);
        primalUpdater.augment(edge910);

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge51, false, false);
        node1.tree.clearCurrentEdges();
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge67, false, false);
        node6.tree.clearCurrentEdges();
        node1.tree.setCurrentEdges();
        BlossomVNode blossom = primalUpdater.shrink(edge34, false);
        blossom.tree.clearCurrentEdges();

        assertEquals(
            new HashSet<>(Arrays.asList(edge12, edge13, edge51)),
            BlossomVDebugger.getEdgesOf(node1));
        assertEquals(
            new HashSet<>(Arrays.asList(edge12, edge23)), BlossomVDebugger.getEdgesOf(node2));
        assertEquals(
            new HashSet<>(Arrays.asList(edge13, edge23, edge34)),
            BlossomVDebugger.getEdgesOf(node3));
        assertEquals(
            new HashSet<>(Arrays.asList(edge34, edge45)), BlossomVDebugger.getEdgesOf(node4));
        assertEquals(
            new HashSet<>(Arrays.asList(edge45, edge51)), BlossomVDebugger.getEdgesOf(node5));
        assertEquals(
            new HashSet<>(Arrays.asList(edge29, edge56, edge57, edge58, edge47, edge48)),
            BlossomVDebugger.getEdgesOf(blossom));

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node6.tree);
        assertNotNull(treeEdge);
        assertTrue(blossom.isOuter);

        assertEquals(1, blossom.tree.plusInfinityEdges.size());
        assertEquals(3, treeEdge.plusPlusEdges.size());
        assertEquals(2, BlossomVDebugger.getPlusMinusHeap(treeEdge, blossom.tree).size());
        assertEquals(0, BlossomVDebugger.getMinusPlusHeap(treeEdge, blossom.tree).size());
    }

    /**
     * Tests blossomSibling, blossomParent and blossomGrandParent references
     */
    @Test
    public void testShrink5()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge51 = edgeMap.get(e51);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge51, false, false);
        node1.tree.clearCurrentEdges();
        BlossomVNode blossom = primalUpdater.shrink(edge34, false);

        assertEquals(blossom, node1.blossomParent);
        assertEquals(blossom, node2.blossomParent);
        assertEquals(blossom, node3.blossomParent);
        assertEquals(blossom, node4.blossomParent);
        assertEquals(blossom, node5.blossomParent);

        assertEquals(blossom, node1.blossomGrandparent);
        assertEquals(blossom, node2.blossomGrandparent);
        assertEquals(blossom, node3.blossomGrandparent);
        assertEquals(blossom, node4.blossomGrandparent);
        assertEquals(blossom, node5.blossomGrandparent);

        Set<BlossomVNode> expectedBlossomNodes =
            new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5));
        Set<BlossomVNode> actualBlossomNodes = new HashSet<>(Collections.singletonList(node1));
        for (BlossomVNode current = node1.blossomSibling.getOpposite(node1); current != node1;
            current = current.blossomSibling.getOpposite(current))
        {
            assertNotNull(current);
            actualBlossomNodes.add(current);
        }
        assertEquals(expectedBlossomNodes, actualBlossomNodes);
    }

    /**
     * Tests proper edge moving
     */
    @Test
    public void testShrink6()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // first tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        // neighbor tree edges
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);
        // cross-tree edges
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e19 = Graphs.addEdgeWithVertices(graph, 1, 9, 0);
        DefaultWeightedEdge e28 = Graphs.addEdgeWithVertices(graph, 2, 8, 0);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 0);
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 0);
        DefaultWeightedEdge e310 = Graphs.addEdgeWithVertices(graph, 3, 10, 0);

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge36 = edgeMap.get(e36);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge89 = edgeMap.get(e89);
        BlossomVEdge edge910 = edgeMap.get(e910);
        BlossomVEdge edge18 = edgeMap.get(e18);
        BlossomVEdge edge19 = edgeMap.get(e19);
        BlossomVEdge edge28 = edgeMap.get(e28);
        BlossomVEdge edge29 = edgeMap.get(e29);
        BlossomVEdge edge39 = edgeMap.get(e39);
        BlossomVEdge edge310 = edgeMap.get(e310);

        // setting up the test case structure
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge34, false, false);
        primalUpdater.grow(edge36, false, false);
        primalUpdater.grow(edge89, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);
        blossom.tree.clearCurrentEdges();

        // validating the tree structure
        assertEquals(blossom, node4.getTreeParent());
        assertEquals(blossom, node6.getTreeParent());
        assertEquals(
            new HashSet<>(Arrays.asList(node4, node6)), BlossomVDebugger.getChildrenOf(blossom));

        // validating the edges endpoints
        assertEquals(blossom, edge18.getOpposite(node8));
        assertEquals(blossom, edge19.getOpposite(node9));
        assertEquals(blossom, edge28.getOpposite(node8));
        assertEquals(blossom, edge29.getOpposite(node9));
        assertEquals(blossom, edge39.getOpposite(node9));
        assertEquals(blossom, edge310.getOpposite(node10));

    }

    /**
     * Tests removal of the (-,+) and addition of the (+,+) cross-tree edges, updating their slacks
     * and updating heaps
     */
    @Test
    public void testShrink7()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // main tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 3);
        // neighbor tree edges
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);
        // cross-tree edges
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 3);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 3);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge25 = edgeMap.get(e25);
        BlossomVEdge edge26 = edgeMap.get(e26);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);

        node1.tree.eps = 3;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        node5.tree.eps = 2;
        node6.tree.eps = 2;
        primalUpdater.augment(edge56);

        node4.tree.eps = 2;
        node4.tree.setCurrentEdges();
        primalUpdater.grow(edge45, false, false);
        node4.tree.clearCurrentEdges();

        node1.tree.setCurrentEdges();
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);

        assertEquals(5, edge24.slack, EPS);
        assertEquals(1, edge25.slack, EPS);
        assertEquals(5, edge26.slack, EPS);

        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(0, BlossomVDebugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, BlossomVDebugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(2, treeEdge.plusPlusEdges.size());

        assertEquals(5, edge24.handle.getKey(), EPS);
        assertEquals(1, edge25.handle.getKey(), EPS);
        assertEquals(5, edge26.handle.getKey(), EPS);
    }

    /**
     * Tests dual updates of the inner (+, +), (-, +) and (+, inf) edges
     */
    @Test
    public void testShrink8()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 3);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 5);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 3);
        DefaultWeightedEdge e71 = Graphs.addEdgeWithVertices(graph, 7, 1, 4);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 8);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 8);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 8);

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
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge71 = edgeMap.get(e71);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge26 = edgeMap.get(e26);
        BlossomVEdge edge36 = edgeMap.get(e36);

        node2.tree.eps = 2;
        node4.tree.eps = 2;
        node7.tree.eps = 2;
        node3.tree.eps = 1;
        node5.tree.eps = 1;
        node6.tree.eps = 1;
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);

        node1.tree.eps = 2;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge71, false, false);
        node1.tree.eps += 1;
        primalUpdater.grow(edge34, false, false);
        node1.tree.eps += 1;
        BlossomVNode blossom = primalUpdater.shrink(edge56, false);
        blossom.tree.clearCurrentEdges();

        assertEquals(7, edge24.slack, EPS);
        assertEquals(5, edge26.slack, EPS);
        assertEquals(2, edge36.slack, EPS);

        assertEquals(0, blossom.tree.plusPlusEdges.size());
    }

    /**
     * Tests updating of the tree structure on a small test case
     */
    @Test
    public void testExpand1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 0);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge35 = edgeMap.get(e35);

        primalUpdater.augment(edge23);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);
        blossom.tree.clearCurrentEdges();
        primalUpdater.augment(edge35);

        node4.tree.setCurrentEdges();
        primalUpdater.grow(edge34, false, false);
        primalUpdater.expand(blossom, false);
        node4.tree.clearCurrentEdges();

        assertEquals(1, state.statistics.expandNum);

        // checking tree structure
        assertEquals(node4.tree, node3.tree);
        assertEquals(node4, node3.getTreeParent());
        assertEquals(node3, node5.getTreeParent());
        assertEquals(
            new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.getChildrenOf(node4));
        assertEquals(
            new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.getChildrenOf(node3));
        assertEquals(
            new HashSet<>(Arrays.asList(edge34, edge35, edge23, edge13)),
            BlossomVDebugger.getEdgesOf(node3));

        // checking edges new endpoints
        assertEquals(node3, edge34.getOpposite(node4));
        assertEquals(node3, edge35.getOpposite(node5));

        // checking the matching
        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge35, node3.matched);

        assertFalse(node1.isMarked);
        assertFalse(node2.isMarked);
        assertFalse(node3.isMarked);
        assertFalse(node4.isMarked);
        assertFalse(node5.isMarked);

        assertFalse(node1.isProcessed);
        assertFalse(node2.isProcessed);
        assertFalse(node3.isProcessed);
        assertFalse(node4.isProcessed);
        assertFalse(node5.isProcessed);

        // checking the labeling and isOuter flag
        assertTrue(node1.isInfinityNode());
        assertTrue(node2.isInfinityNode());
        assertTrue(node3.isMinusNode());
        assertTrue(node1.isOuter);
        assertTrue(node2.isOuter);
        assertTrue(node3.isOuter);
    }

    /**
     * Test primal updates after blossom expanding
     */
    @Test
    public void testExpand2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // blossom nodes
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);
        // blossom tree nodes
        DefaultWeightedEdge e62 = Graphs.addEdgeWithVertices(graph, 6, 2, 0);
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 0);
        // neighbor tree nodes
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);
        // cross-tree edges
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 0);
        DefaultWeightedEdge e48 = Graphs.addEdgeWithVertices(graph, 4, 8, 0);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 0);
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 0);
        // infinity edge
        DefaultWeightedEdge e210 = Graphs.addEdgeWithVertices(graph, 2, 10, 0);
        DefaultWeightedEdge e310 = Graphs.addEdgeWithVertices(graph, 3, 10, 0);

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge51 = edgeMap.get(e51);
        BlossomVEdge edge89 = edgeMap.get(e89);
        BlossomVEdge edge910 = edgeMap.get(e910);
        BlossomVEdge edge62 = edgeMap.get(e62);
        BlossomVEdge edge37 = edgeMap.get(e37);
        BlossomVEdge edge18 = edgeMap.get(e18);
        BlossomVEdge edge58 = edgeMap.get(e58);
        BlossomVEdge edge48 = edgeMap.get(e48);
        BlossomVEdge edge29 = edgeMap.get(e29);
        BlossomVEdge edge39 = edgeMap.get(e39);
        BlossomVEdge edge210 = edgeMap.get(e210);
        BlossomVEdge edge310 = edgeMap.get(e310);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge910);

        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge51, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge34, false);
        blossom.tree.clearCurrentEdges();

        node8.tree.setCurrentEdges();
        primalUpdater.grow(edge89, false, false);
        node8.tree.clearCurrentEdges();

        primalUpdater.augment(edge37);
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge62, false, false);
        primalUpdater.expand(blossom, false);

        // testing edges endpoints
        assertEquals(node2, edge62.getOpposite(node6));
        assertEquals(node3, edge37.getOpposite(node7));
        assertEquals(node1, edge18.getOpposite(node8));
        assertEquals(node5, edge58.getOpposite(node8));
        assertEquals(node4, edge48.getOpposite(node8));
        assertEquals(node2, edge29.getOpposite(node9));
        assertEquals(node2, edge210.getOpposite(node10));
        assertEquals(node3, edge39.getOpposite(node9));
        assertEquals(node3, edge310.getOpposite(node10));

        // testing the matching
        assertEquals(edge12, node2.matched);
        assertEquals(edge12, node1.matched);
        assertEquals(edge45, node5.matched);
        assertEquals(edge45, node4.matched);
        assertEquals(edge37, node3.matched);

        // testing the labeling
        assertTrue(node2.isMinusNode());
        assertTrue(node1.isPlusNode());
        assertTrue(node5.isMinusNode());
        assertTrue(node4.isPlusNode());
        assertTrue(node3.isMinusNode());

        // testing isOuter
        assertTrue(node2.isOuter);
        assertTrue(node1.isOuter);
        assertTrue(node5.isOuter);
        assertTrue(node4.isOuter);
        assertTrue(node3.isOuter);

        // testing node.tree
        assertEquals(node6.tree, node2.tree);
        assertEquals(node6.tree, node1.tree);
        assertEquals(node6.tree, node5.tree);
        assertEquals(node6.tree, node4.tree);
        assertEquals(node6.tree, node3.tree);

        // testing tree structure
        assertEquals(node6, node2.getTreeParent());
        assertEquals(node2, node1.getTreeParent());
        assertEquals(node1, node5.getTreeParent());
        assertEquals(node5, node4.getTreeParent());
        assertEquals(node4, node3.getTreeParent());
        assertEquals(node3, node7.getTreeParent());

        assertEquals(
            new HashSet<>(Collections.singletonList(node2)), BlossomVDebugger.getChildrenOf(node6));
        assertEquals(
            new HashSet<>(Collections.singletonList(node1)), BlossomVDebugger.getChildrenOf(node2));
        assertEquals(
            new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.getChildrenOf(node1));
        assertEquals(
            new HashSet<>(Collections.singletonList(node4)), BlossomVDebugger.getChildrenOf(node5));
        assertEquals(
            new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.getChildrenOf(node4));
        assertEquals(
            new HashSet<>(Collections.singletonList(node7)), BlossomVDebugger.getChildrenOf(node3));
        assertEquals(
            new HashSet<>(Arrays.asList(node6, node2, node1, node5, node4, node3, node7)),
            BlossomVDebugger.getTreeNodes(node6.tree));
    }

    /**
     * Tests dual part of the expand operation
     */
    @Test
    public void testExpand3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 5);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 5);

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

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge35 = edgeMap.get(e35);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        node1.tree.setCurrentEdges();
        node1.tree.eps = 3;
        primalUpdater.grow(edge12, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge13, false);
        blossom.tree.clearCurrentEdges();

        node5.tree.eps = 2;
        blossom.tree.eps += 2;
        primalUpdater.augment(edge35);
        node4.tree.eps = 2;
        node4.tree.setCurrentEdges();
        primalUpdater.grow(edge34, false, false);
        primalUpdater.expand(blossom, false);
        node4.tree.clearCurrentEdges();

        assertEquals(3, node1.dual, EPS);
        assertEquals(1, node2.dual, EPS);
        assertEquals(3, node3.dual, EPS);
        assertEquals(0, node4.dual, EPS);
        assertEquals(0, node5.dual, EPS);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(-2, edge13.slack, EPS);
        assertEquals(-2, edge23.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge35.slack, EPS);

    }

    /**
     * Tests dual part of the expand operation on a bigger test case
     */
    @Test
    public void testExpand4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // blossom edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 3);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 4);
        // edges of the tree, that will contain blossom
        DefaultWeightedEdge e65 = Graphs.addEdgeWithVertices(graph, 6, 5, 4);
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 4);
        // edges of neighbor tree
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);
        // edges between blossom and neighbor tree
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 8);
        DefaultWeightedEdge e59 = Graphs.addEdgeWithVertices(graph, 5, 9, 8);
        DefaultWeightedEdge e48 = Graphs.addEdgeWithVertices(graph, 4, 8, 8);
        DefaultWeightedEdge e49 = Graphs.addEdgeWithVertices(graph, 4, 9, 8);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 8);
        DefaultWeightedEdge e210 = Graphs.addEdgeWithVertices(graph, 2, 10, 8);
        // inner blossom edges
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 8);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 8);
        // edges between blossom nodes and node from the same tree
        DefaultWeightedEdge e27 = Graphs.addEdgeWithVertices(graph, 2, 7, 8);
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 8);

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge51 = edgeMap.get(e51);
        BlossomVEdge edge65 = edgeMap.get(e65);
        BlossomVEdge edge37 = edgeMap.get(e37);
        BlossomVEdge edge89 = edgeMap.get(e89);
        BlossomVEdge edge910 = edgeMap.get(e910);
        BlossomVEdge edge58 = edgeMap.get(e58);
        BlossomVEdge edge59 = edgeMap.get(e59);
        BlossomVEdge edge48 = edgeMap.get(e48);
        BlossomVEdge edge49 = edgeMap.get(e49);
        BlossomVEdge edge27 = edgeMap.get(e27);
        BlossomVEdge edge29 = edgeMap.get(e29);
        BlossomVEdge edge210 = edgeMap.get(e210);
        BlossomVEdge edge47 = edgeMap.get(e47);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge25 = edgeMap.get(e25);

        // setting up the blossom structure
        node2.tree.eps = 2;
        node3.tree.eps = 1;
        node4.tree.eps = 1;
        node5.tree.eps = 2;
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.eps = 2;
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge51, false, false);
        node1.tree.eps += 1;
        BlossomVNode blossom = primalUpdater.shrink(edge34, false);
        blossom.tree.clearCurrentEdges();

        // setting up the "-" blossom's tree structure
        node7.tree.eps = 1;
        blossom.tree.eps += 1;
        primalUpdater.augment(edge37);

        node6.tree.eps = 2;
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge65, false, false);
        node6.tree.clearCurrentEdges();

        // setting up the structure of the neighbor tree
        primalUpdater.augment(edge910);
        node8.tree.setCurrentEdges();
        primalUpdater.grow(edge89, false, false);
        node8.tree.setCurrentEdges();

        node6.tree.setCurrentEdges();
        primalUpdater.expand(blossom, false);
        node6.tree.clearCurrentEdges();
        BlossomVTreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node6.tree, node8.tree);

        // validating blossom node duals
        node1.tree = node2.tree = null;
        assertEquals(3, node1.dual, EPS);
        assertEquals(1, node2.dual, EPS);
        assertEquals(4, node3.dual, EPS); // tree eps is 2, node3.label = "-"
        assertEquals(0, node4.dual, EPS); // tree eps is 2, node4.label = "+"
        assertEquals(3, node5.dual, EPS); // tree eps is 2, node5.label = "-"

        // validating slacks of the edges in the tree structure
        assertEquals(0, edge65.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge37.slack, EPS);

        // validating the slacks of inner blossom edges
        assertEquals(7, edge24.slack, EPS);
        assertEquals(4, edge25.slack, EPS);

        // validating slacks of cross-tree edges
        // assertEquals(4, edge58.slack, EPS);
        assertEquals(4, edge59.slack, EPS);
        assertEquals(7, edge48.slack, EPS);
        assertEquals(7, edge49.slack, EPS);
        // validating slacks of the (+, inf) edges and a (-, inf) edge
        assertEquals(6, edge210.slack, EPS);
        assertEquals(7, edge27.slack, EPS);
        assertEquals(6, edge29.slack, EPS);

        // validating keys of the cross-tree and infinity edges in the heaps
        assertEquals(4, edge58.handle.getKey(), EPS);
        assertEquals(7, edge48.handle.getKey(), EPS);
        assertEquals(7, edge49.handle.getKey(), EPS);
        assertEquals(6, edge210.handle.getKey(), EPS);
        assertEquals(7, edge24.handle.getKey(), EPS);
        assertEquals(8, edge47.handle.getKey(), EPS);
        assertEquals(7, edge27.handle.getKey(), EPS);

        // validating slacks of the edges on the odd branch
        assertEquals(-2, edge51.slack, EPS);
        assertEquals(-2, edge23.slack, EPS);
        assertEquals(0, edge12.slack, EPS);

        // validating slack of the new (+, +) node
        assertEquals(8, edge47.slack, EPS);

        // validating tree edges amount
        assertNotNull(treeEdge);
        assertEquals(1, BlossomVDebugger.getTreeEdgesBetween(node6.tree, node8.tree).size());

        // validating sizes of the heaps of the tree edge
        assertEquals(1, treeEdge.plusPlusEdges.size());
        assertEquals(1, BlossomVDebugger.getMinusPlusHeap(treeEdge, node6.tree).size());
        assertEquals(1, BlossomVDebugger.getPlusMinusHeap(treeEdge, node6.tree).size());
        // validating sizes of tree heaps
        assertEquals(2, node6.tree.plusInfinityEdges.size());
        assertEquals(1, node6.tree.plusPlusEdges.size());
        assertEquals(0, node6.tree.minusBlossoms.size());
        assertEquals(1, node8.tree.plusInfinityEdges.size());
        assertEquals(0, node8.tree.plusPlusEdges.size());
        assertEquals(0, node8.tree.minusBlossoms.size());
    }

    /**
     * Tests preserving the state of the blossom, inner and infinity edges after shrink and expand
     * operations
     */
    @Test
    public void testExpand5()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // blossom edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 3);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 4);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 6);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 4);
        DefaultWeightedEdge e71 = Graphs.addEdgeWithVertices(graph, 7, 1, 3);
        // tree edges
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 1);
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 3);
        // inner blossom edges
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 8); // (-, inf) edge
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 8); // (+, inf) edge
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 8); // (-, -) edge
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 8); // (+, +) edge
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 8); // (+, -) edge
        // matched edge
        DefaultWeightedEdge e1011 = Graphs.addEdgeWithVertices(graph, 10, 11, 0);
        // infinity edges
        DefaultWeightedEdge e510 = Graphs.addEdgeWithVertices(graph, 5, 10, 8); // (-, inf) edge
        DefaultWeightedEdge e610 = Graphs.addEdgeWithVertices(graph, 6, 10, 8); // (+, inf) edge
        DefaultWeightedEdge e211 = Graphs.addEdgeWithVertices(graph, 2, 11, 8); // (inf, inf) edge

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
        BlossomVNode node8 = vertexMap.get(8);
        BlossomVNode node9 = vertexMap.get(9);
        BlossomVNode node10 = vertexMap.get(10);
        BlossomVNode node11 = vertexMap.get(11);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge71 = edgeMap.get(e71);

        BlossomVEdge edge78 = edgeMap.get(e78);
        BlossomVEdge edge39 = edgeMap.get(e39);

        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge26 = edgeMap.get(e26);
        BlossomVEdge edge35 = edgeMap.get(e35);
        BlossomVEdge edge46 = edgeMap.get(e46);
        BlossomVEdge edge47 = edgeMap.get(e47);

        BlossomVEdge edge510 = edgeMap.get(e510);
        BlossomVEdge edge610 = edgeMap.get(e610);
        BlossomVEdge edge211 = edgeMap.get(e211);

        node1.tree.eps = 2;
        node2.tree.eps = 1;
        node3.tree.eps = 3;
        node4.tree.eps = 1;
        node5.tree.eps = 3;
        node6.tree.eps = 3;
        node7.tree.eps = 1;

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge34, false, false);
        primalUpdater.grow(edge71, false, false);
        BlossomVNode blossom = primalUpdater.shrink(edge56, false);
        blossom.tree.clearCurrentEdges();
        primalUpdater.augment(edge39);
        node8.tree.setCurrentEdges();
        primalUpdater.grow(edge78, false, false);

        primalUpdater.expand(blossom, false);

        assertEquals(node7, edge78.getOpposite(node8));
        assertEquals(node3, edge39.getOpposite(node9));
        assertEquals(node5, edge510.getOpposite(node10));
        assertEquals(node6, edge610.getOpposite(node10));
        assertEquals(node2, edge211.getOpposite(node11));

        // tight edges
        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge56.slack, EPS);
        assertEquals(0, edge67.slack, EPS);
        assertEquals(0, edge71.slack, EPS);
        assertEquals(0, edge78.slack, EPS);
        assertEquals(0, edge39.slack, EPS);

        // inner edges
        assertEquals(3, edge13.slack, EPS);
        assertEquals(4, edge26.slack, EPS);
        assertEquals(2, edge35.slack, EPS);
        assertEquals(4, edge46.slack, EPS);
        assertEquals(6, edge47.slack, EPS);
        // boundary edges
        assertEquals(7, edge211.slack, EPS);
        assertEquals(5, edge510.slack, EPS);
        assertEquals(5, edge610.slack, EPS);

    }

}
