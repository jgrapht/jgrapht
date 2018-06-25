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
package org.jgrapht.alg.matching.blossom_v;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jgrapht.alg.matching.blossom_v.Initializer.InitializationType.NONE;
import static org.jgrapht.alg.matching.blossom_v.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom_v.Node.Label.INFINITY;
import static org.junit.Assert.*;

public class PrimalUpdaterTest {

    private KolmogorovMinimumWeightPerfectMatching.Options noneOptions = new KolmogorovMinimumWeightPerfectMatching.Options(NONE);

    /**
     * Tests one grow operation
     */
    @Test
    public void testGrow1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);

        primalUpdater.augment(edge23);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

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
     * Tests updating of the tree structure (tree parent, node's tree reference, node's label). Uses recursive grow flag
     */
    @Test
    public void testGrow2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge edge34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge edge45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge edge36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge edge67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Tree tree = node1.tree;

        primalUpdater.augment(state.edgeMap.get(edge45));
        primalUpdater.augment(state.edgeMap.get(edge23));
        primalUpdater.augment(state.edgeMap.get(edge67));
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(state.edgeMap.get(edge12), true);
        state.clearCurrentEdges(node1.tree);

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
     * Tests proper addition of new tree edges without duplicates, and size of heaps in the tree edges
     */
    @Test
    public void testGrow3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

        Set<TreeEdge> treeEdges1 = BlossomVDebugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges1.size());
        TreeEdge treeEdge1 = treeEdges1.iterator().next();
        assertEquals(1, treeEdge1.plusPlusEdges.size());
        assertEquals(1, BlossomVDebugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge34, false);
        state.clearCurrentEdges(node1.tree);

        Set<TreeEdge> treeEdges2 = BlossomVDebugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges2.size());
        TreeEdge treeEdge2 = treeEdges2.iterator().next();
        assertEquals(treeEdge1, treeEdge2);
        assertEquals(2, treeEdge1.plusPlusEdges.size());
        assertEquals(2, BlossomVDebugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        Set<TreeEdge> treeEdges3 = BlossomVDebugger.getTreeEdgesBetween(node1.tree, node7.tree);
        assertEquals(1, treeEdges3.size());
        TreeEdge treeEdge3 = treeEdges3.iterator().next();
        assertEquals(1, treeEdge3.plusPlusEdges.size());
    }

    /**
     * Tests addition of new (-, +), (+,-) and (+, +) cross-tree edges to appropriate heaps
     * and addition of a new tree edge
     */
    @Test
    public void testGrow4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node4 = state.vertexMap.get(4);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge78 = state.edgeMap.get(e78);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge56);
        primalUpdater.augment(edge78);
        state.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);
        state.clearCurrentEdges(node4.tree);

        assertEquals(4, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

        assertEquals(1, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node1.tree.plusPlusEdges.size());

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        int dir = BlossomVDebugger.dirToOpposite(treeEdge, node1.tree);

        assertEquals(2, treeEdge.getCurrentMinusPlusHeap(dir).size());
        assertEquals(1, treeEdge.getCurrentPlusMinusHeap(dir).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
    }

    /**
     * Tests updating of the slacks of the incident edges to "-" and "+" grow nodes and
     * updating their keys in corresponding heaps
     */
    @Test
    public void testGrow5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 5);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 3);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 3);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 3);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge35 = state.edgeMap.get(e35);
        Edge edge36 = state.edgeMap.get(e36);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        node5.tree.eps = 1;
        node6.tree.eps = 1;
        primalUpdater.augment(edge56);
        node4.tree.eps = 3;
        state.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);
        state.clearCurrentEdges(node4.tree);

        assertEquals(4, node5.dual, EPS);
        assertEquals(-2, node6.dual, EPS);

        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge56.slack, EPS);
        assertEquals(4, edge24.slack, EPS);
        assertEquals(4, edge26.slack, EPS);
        assertEquals(-2, edge35.slack, EPS);
        assertEquals(4, edge36.slack, EPS);

        // edge35 is (-, inf) edge, so it isn't present in any heap
        assertEquals(4, edge24.fibNode.getKey(), EPS);
        assertEquals(4, edge26.fibNode.getKey(), EPS);
        assertEquals(4, edge26.fibNode.getKey(), EPS);

        assertEquals(3, node4.tree.plusInfinityEdges.size());

        node1.tree.eps = 3;
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

        assertEquals(4, node2.dual, EPS);
        assertEquals(-2, node3.dual, EPS);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(1, edge24.slack, EPS);
        assertEquals(1, edge26.slack, EPS);
        assertEquals(1, edge35.slack, EPS);
        assertEquals(7, edge36.slack, EPS);

        assertEquals(1, edge24.fibNode.getKey(), EPS);
        assertEquals(1, edge26.fibNode.getKey(), EPS);
        assertEquals(1, edge35.fibNode.getKey(), EPS);
        assertEquals(7, edge36.fibNode.getKey(), EPS);

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(2, BlossomVDebugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, BlossomVDebugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
    }

    /**
     * Tests addition of (+, +) in-tree edges, (+, inf) edges and "-" pseudonodes to appropriate heaps
     * and removal of former (+, inf) edges
     */
    @Test
    public void testGrow6() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e71 = Graphs.addEdgeWithVertices(graph, 7, 1, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge71 = state.edgeMap.get(e71);


        primalUpdater.augment(edge34);
        state.setCurrentEdges(node2.tree);
        primalUpdater.grow(edge23, false);
        Node blossom = primalUpdater.shrink(edge24);
        state.clearCurrentEdges(blossom.tree);

        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);

        assertEquals(3, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node1.tree.minusBlossoms.size());
        assertEquals(0, node1.tree.plusPlusEdges.size());

        primalUpdater.grow(edge71, false);

        assertEquals(1, node1.tree.minusBlossoms.size());
        assertEquals(1, node1.tree.plusPlusEdges.size());
        assertEquals(0, node1.tree.plusInfinityEdges.size());
    }

    /**
     * Tests finding a blossom root
     */
    @Test
    public void testFindBlossomRoot() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);

        State<Integer, DefaultWeightedEdge> state = new Initializer<>(graph).initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge16 = state.edgeMap.get(e16);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge57 = state.edgeMap.get(e57);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge16, false);
        state.clearCurrentEdges(node1.tree);

        Node root = primalUpdater.findBlossomRoot(edge57);

        assertEquals(root, node1);
    }

    /**
     * Tests augment operation on a small test case. Checks updating of the matching, changing labels,
     * updating edge slack and nodes dual variables
     */
    @Test
    public void testAugment1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);

        node1.tree.eps = 1;
        node2.tree.eps = 3;

        Edge edge12 = state.edgeMap.get(e12);

        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        primalUpdater.augment(edge12);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(INFINITY, node1.label);
        assertEquals(INFINITY, node2.label);
        assertEquals(0, state.treeNum);
        assertEquals(0, edge12.slack, EPS);
        assertEquals(1, node1.dual, EPS);
        assertEquals(3, node2.dual, EPS);
    }

    /**
     * Tests augment operation. Checks labeling, updated edges' slacks, matching, and tree structure
     */
    @Test
    public void testAugment2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 3);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);

        node2.tree.eps = 2;
        node3.tree.eps = 1;

        primalUpdater.augment(edge23);

        assertEquals(INFINITY, node2.label);
        assertEquals(INFINITY, node3.label);
        assertEquals(2, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(3, edge34.slack, EPS);
        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusInfinityEdges.size());
        assertTrue(BlossomVDebugger.treeEdgesOf(node1.tree).isEmpty());

        node4.tree.eps = 1;
        node5.tree.eps = 2;

        primalUpdater.augment(edge45);

        assertEquals(INFINITY, node4.label);
        assertEquals(INFINITY, node5.label);
        assertEquals(2, edge34.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(2, edge56.slack, EPS);
        assertEquals(1, node6.tree.plusInfinityEdges.size());
        assertTrue(BlossomVDebugger.treeEdgesOf(node6.tree).isEmpty());

        node1.tree.eps = 2;
        node6.tree.eps = 2;

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

        assertEquals(node1.tree, node2.tree);
        assertEquals(node1.tree, node3.tree);

        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge56, false);
        state.clearCurrentEdges(node6.tree);

        node1.tree.eps += 1;
        node1.tree.eps += 1;

        primalUpdater.augment(edge34);

        assertEquals(INFINITY, node1.label);
        assertEquals(INFINITY, node2.label);
        assertEquals(INFINITY, node3.label);
        assertEquals(INFINITY, node4.label);
        assertEquals(INFINITY, node5.label);
        assertEquals(INFINITY, node6.label);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge34, node3.matched);
        assertEquals(edge34, node4.matched);
        assertEquals(edge56, node5.matched);
        assertEquals(edge56, node6.matched);

        assertTrue(BlossomVDebugger.childrenOf(node1).isEmpty());
        assertTrue(BlossomVDebugger.childrenOf(node2).isEmpty());
        assertTrue(BlossomVDebugger.childrenOf(node3).isEmpty());
        assertTrue(BlossomVDebugger.childrenOf(node4).isEmpty());
        assertTrue(BlossomVDebugger.childrenOf(node5).isEmpty());
        assertTrue(BlossomVDebugger.childrenOf(node6).isEmpty());

    }

    /**
     * Tests augment operation on a big test case. Checks matching and labeling
     */
    @Test
    public void testAugment3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e710 = Graphs.addEdgeWithVertices(graph, 7, 10, 2);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge36 = state.edgeMap.get(e36);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge18 = state.edgeMap.get(e18);
        Edge edge89 = state.edgeMap.get(e89);
        Edge edge710 = state.edgeMap.get(e710);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge89);

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge18, true);
        primalUpdater.grow(edge12, true);
        state.clearCurrentEdges(node1.tree);

        node1.tree.eps = 2;

        primalUpdater.augment(edge710);

        assertEquals(INFINITY, node1.label);
        assertEquals(INFINITY, node2.label);
        assertEquals(INFINITY, node3.label);
        assertEquals(INFINITY, node4.label);
        assertEquals(INFINITY, node5.label);
        assertEquals(INFINITY, node6.label);
        assertEquals(INFINITY, node7.label);
        assertEquals(INFINITY, node8.label);
        assertEquals(INFINITY, node9.label);
        assertEquals(INFINITY, node10.label);

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
    public void testAugment4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e41 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Edge edge12 = state.edgeMap.get(e12);

        Tree tree3 = state.vertexMap.get(3).tree;
        Tree tree4 = state.vertexMap.get(4).tree;

        TreeEdge treeEdge34 = BlossomVDebugger.getTreeEdge(tree3, tree4);

        primalUpdater.augment(edge12);

        assertEquals(new HashSet<>(Collections.singletonList(treeEdge34)), BlossomVDebugger.treeEdgesOf(tree3));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge34)), BlossomVDebugger.treeEdgesOf(tree4));
    }

    /**
     * Test shrink on a small test case. Checks updates tree structure, marking, and other meta data
     */
    @Test
    public void testShrink1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge14 = state.edgeMap.get(e14);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);

        primalUpdater.augment(edge23);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);
        state.clearCurrentEdges(node1.tree);

        assertEquals(1, state.statistics.shrinkNum);
        assertEquals(1, state.blossomNum);

        assertFalse(node1.isTreeRoot);

        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge23)), BlossomVDebugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge14, edge24)), BlossomVDebugger.edgesOf(blossom));

        assertEquals(blossom, node1.blossomParent);
        assertEquals(blossom, node2.blossomParent);
        assertEquals(blossom, node3.blossomParent);

        assertEquals(blossom, node1.blossomGrandparent);
        assertEquals(blossom, node2.blossomGrandparent);
        assertEquals(blossom, node3.blossomGrandparent);

        assertEquals(node1, BlossomVDebugger.getOppositeOriginal(edge14, node4));
        assertEquals(node4, BlossomVDebugger.getOppositeOriginal(edge14, node1));
        assertEquals(blossom, edge14.getOpposite(node4));
        assertEquals(node4, edge14.getOpposite(blossom));

        assertEquals(node4, BlossomVDebugger.getOppositeOriginal(edge24, node2));
        assertEquals(node2, BlossomVDebugger.getOppositeOriginal(edge24, node4));
        assertEquals(blossom, edge24.getOpposite(node4));
        assertEquals(node4, edge24.getOpposite(blossom));

        assertEquals(blossom, node1.tree.root);
        assertTrue(blossom.isOuter);

        assertFalse(node1.isMarked);
        assertFalse(node2.isMarked);
        assertFalse(node3.isMarked);
    }

    /**
     * Tests updating of the slacks after blossom shrinking and updating of the edge.fibNode.getKey()
     */
    @Test
    public void testShrink2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Tree tree1 = node1.tree;
        Tree tree4 = node4.tree;

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge14 = state.edgeMap.get(e14);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        state.setCurrentEdges(tree1);
        node1.tree.eps = 3;
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);
        state.clearCurrentEdges(node1.tree);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge13.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(4, edge14.slack, EPS);
        assertEquals(6, edge24.slack, EPS);

        assertEquals(4, edge14.fibNode.getKey(), EPS);
        assertEquals(6, edge24.fibNode.getKey(), EPS);

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(tree1, tree4);
        assertNotNull(treeEdge);

        assertEquals(2, treeEdge.plusPlusEdges.size());
        assertEquals(-3, blossom.dual, EPS);
    }

    /**
     * Tests dual part of the shrink operation (updating edges' slacks, fibNode keys, etc.)
     */
    @Test
    public void testShrink3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge51 = state.edgeMap.get(e51);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge78 = state.edgeMap.get(e78);
        Edge edge16 = state.edgeMap.get(e16);
        Edge edge57 = state.edgeMap.get(e57);
        Edge edge58 = state.edgeMap.get(e58);
        Edge edge47 = state.edgeMap.get(e47);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        node4.tree.eps = 1;
        node5.tree.eps = 3;
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        state.setCurrentEdges(node1.tree);
        node1.tree.eps = 4;
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        state.clearCurrentEdges(node1.tree);
        node1.tree.eps += 2;

        node8.tree.eps = 2;
        primalUpdater.augment(edge78);
        state.setCurrentEdges(node6.tree);
        node6.tree.eps = 3;
        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge67, false);
        state.clearCurrentEdges(node6.tree);

        state.setCurrentEdges(node1.tree);
        Node blossom = primalUpdater.shrink(edge34);

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

        assertEquals(10, edge16.fibNode.getKey(), EPS);
        assertEquals(10, edge57.fibNode.getKey(), EPS);
        assertEquals(15, edge58.fibNode.getKey(), EPS);
        assertEquals(7, edge47.fibNode.getKey(), EPS);

        Set<TreeEdge> treeEdges = BlossomVDebugger.getTreeEdgesBetween(blossom.tree, node6.tree);
        assertEquals(1, treeEdges.size());
        TreeEdge treeEdge = treeEdges.iterator().next();
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
    public void testShrink4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge51 = state.edgeMap.get(e51);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge78 = state.edgeMap.get(e78);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge57 = state.edgeMap.get(e57);
        Edge edge58 = state.edgeMap.get(e58);
        Edge edge47 = state.edgeMap.get(e47);
        Edge edge48 = state.edgeMap.get(e48);
        Edge edge29 = state.edgeMap.get(e29);
        Edge edge910 = state.edgeMap.get(e910);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge78);
        primalUpdater.augment(edge910);

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        state.clearCurrentEdges(node1.tree);
        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge67, false);
        state.clearCurrentEdges(node6.tree);
        state.setCurrentEdges(node1.tree);
        Node blossom = primalUpdater.shrink(edge34);
        state.clearCurrentEdges(blossom.tree);

        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge51)), BlossomVDebugger.edgesOf(node1));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge23)), BlossomVDebugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge13, edge23, edge34)), BlossomVDebugger.edgesOf(node3));
        assertEquals(new HashSet<>(Arrays.asList(edge34, edge45)), BlossomVDebugger.edgesOf(node4));
        assertEquals(new HashSet<>(Arrays.asList(edge45, edge51)), BlossomVDebugger.edgesOf(node5));
        assertEquals(new HashSet<>(Arrays.asList(edge29, edge56, edge57, edge58, edge47, edge48)), BlossomVDebugger.edgesOf(blossom));

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node6.tree);
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
    public void testShrink5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge51 = state.edgeMap.get(e51);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        state.clearCurrentEdges(node1.tree);
        Node blossom = primalUpdater.shrink(edge34);

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

        Set<Node> expectedBlossomNodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5));
        Set<Node> actualBlossomNodes = new HashSet<>(Collections.singletonList(node1));
        for (Node current = node1.blossomSibling.getOpposite(node1); current != node1; current = current.blossomSibling.getOpposite(current)) {
            assertNotNull(current);
            actualBlossomNodes.add(current);
        }
        assertEquals(expectedBlossomNodes, actualBlossomNodes);
    }


    /**
     * Tests proper edge moving
     */
    @Test
    public void testShrink6() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge36 = state.edgeMap.get(e36);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge89 = state.edgeMap.get(e89);
        Edge edge910 = state.edgeMap.get(e910);
        Edge edge18 = state.edgeMap.get(e18);
        Edge edge19 = state.edgeMap.get(e19);
        Edge edge28 = state.edgeMap.get(e28);
        Edge edge29 = state.edgeMap.get(e29);
        Edge edge39 = state.edgeMap.get(e39);
        Edge edge310 = state.edgeMap.get(e310);

        // setting up the test case structure
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge36, false);
        primalUpdater.grow(edge89, false);
        Node blossom = primalUpdater.shrink(edge13);
        state.clearCurrentEdges(blossom.tree);

        // validating the tree structure
        assertEquals(blossom, node4.getTreeParent());
        assertEquals(blossom, node6.getTreeParent());
        assertEquals(new HashSet<>(Arrays.asList(node4, node6)), BlossomVDebugger.childrenOf(blossom));

        // validating the edges endpoints
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge18, node8));
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge19, node9));
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge28, node8));
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge29, node9));
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge39, node9));
        assertEquals(blossom, BlossomVDebugger.getOpposite(edge310, node10));

    }

    /**
     * Tests removal of the (-,+) and addition of the (+,+) cross-tree edges, updating their slacks
     * and updating heaps
     */
    @Test
    public void testShrink7() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge25 = state.edgeMap.get(e25);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);

        node1.tree.eps = 3;
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        state.clearCurrentEdges(node1.tree);

        node5.tree.eps = 2;
        node6.tree.eps = 2;
        primalUpdater.augment(edge56);

        node4.tree.eps = 2;
        state.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);
        state.clearCurrentEdges(node4.tree);

        state.setCurrentEdges(node1.tree);
        Node blossom = primalUpdater.shrink(edge13);

        assertEquals(5, edge24.slack, EPS);
        assertEquals(1, edge25.slack, EPS);
        assertEquals(5, edge26.slack, EPS);

        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(0, BlossomVDebugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, BlossomVDebugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(2, treeEdge.plusPlusEdges.size());

        assertEquals(5, edge24.fibNode.getKey(), EPS);
        assertEquals(1, edge25.fibNode.getKey(), EPS);
        assertEquals(5, edge26.fibNode.getKey(), EPS);
    }

    /**
     * Tests dual updates of the inner (+, +), (-, +) and (+, inf) edges
     */
    @Test
    public void testShrink8() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge71 = state.edgeMap.get(e71);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge36 = state.edgeMap.get(e36);

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
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge71, false);
        node1.tree.eps += 1;
        primalUpdater.grow(edge34, false);
        node1.tree.eps += 1;
        Node blossom = primalUpdater.shrink(edge56);
        state.clearCurrentEdges(blossom.tree);

        assertEquals(7, edge24.slack, EPS);
        assertEquals(5, edge26.slack, EPS);
        assertEquals(2, edge36.slack, EPS);

        assertEquals(0, blossom.tree.plusPlusEdges.size());
    }

    /**
     * Tests updating of the tree structure on a small test case
     */
    @Test
    public void testExpand1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge35 = state.edgeMap.get(e35);

        primalUpdater.augment(edge23);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);
        state.clearCurrentEdges(blossom.tree);
        primalUpdater.augment(edge35);

        state.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge34, false);
        primalUpdater.expand(blossom);
        state.clearCurrentEdges(node4.tree);

        assertEquals(1, state.statistics.expandNum);

        // checking tree structure
        assertEquals(node4.tree, node3.tree);
        assertEquals(node4, node3.getTreeParent());
        assertEquals(node3, node5.getTreeParent());
        assertEquals(new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.childrenOf(node4));
        assertEquals(new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.childrenOf(node3));
        assertEquals(new HashSet<>(Arrays.asList(edge34, edge35, edge23, edge13)), BlossomVDebugger.edgesOf(node3));


        // checking edges new endpoints
        assertEquals(node3, edge34.getOpposite(node4));
        assertEquals(node3, edge35.getOpposite(node5));

        //checking the matching
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
    public void testExpand2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge51 = state.edgeMap.get(e51);
        Edge edge89 = state.edgeMap.get(e89);
        Edge edge910 = state.edgeMap.get(e910);
        Edge edge62 = state.edgeMap.get(e62);
        Edge edge37 = state.edgeMap.get(e37);
        Edge edge18 = state.edgeMap.get(e18);
        Edge edge58 = state.edgeMap.get(e58);
        Edge edge48 = state.edgeMap.get(e48);
        Edge edge29 = state.edgeMap.get(e29);
        Edge edge39 = state.edgeMap.get(e39);
        Edge edge210 = state.edgeMap.get(e210);
        Edge edge310 = state.edgeMap.get(e310);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge910);

        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        Node blossom = primalUpdater.shrink(edge34);
        state.clearCurrentEdges(blossom.tree);

        state.setCurrentEdges(node8.tree);
        primalUpdater.grow(edge89, false);
        state.clearCurrentEdges(node8.tree);

        primalUpdater.augment(edge37);
        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge62, false);
        primalUpdater.expand(blossom);

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

        assertEquals(new HashSet<>(Collections.singletonList(node2)), BlossomVDebugger.childrenOf(node6));
        assertEquals(new HashSet<>(Collections.singletonList(node1)), BlossomVDebugger.childrenOf(node2));
        assertEquals(new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.childrenOf(node1));
        assertEquals(new HashSet<>(Collections.singletonList(node4)), BlossomVDebugger.childrenOf(node5));
        assertEquals(new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.childrenOf(node4));
        assertEquals(new HashSet<>(Collections.singletonList(node7)), BlossomVDebugger.childrenOf(node3));
        assertEquals(new HashSet<>(Arrays.asList(node6, node2, node1, node5, node4, node3, node7)), BlossomVDebugger.getTreeNodes(node6.tree));
    }

    /**
     * Tests dual part of the expand operation
     */
    @Test
    public void testExpand3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 5);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 5);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge35 = state.edgeMap.get(e35);

        node2.tree.eps = 1;
        node3.tree.eps = 1;
        primalUpdater.augment(edge23);
        state.setCurrentEdges(node1.tree);
        node1.tree.eps = 3;
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);
        state.clearCurrentEdges(blossom.tree);

        node5.tree.eps = 2;
        blossom.tree.eps += 2;
        primalUpdater.augment(edge35);
        node4.tree.eps = 2;
        state.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge34, false);
        primalUpdater.expand(blossom);
        state.clearCurrentEdges(node4.tree);

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
    public void testExpand4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge51 = state.edgeMap.get(e51);
        Edge edge65 = state.edgeMap.get(e65);
        Edge edge37 = state.edgeMap.get(e37);
        Edge edge89 = state.edgeMap.get(e89);
        Edge edge910 = state.edgeMap.get(e910);
        Edge edge58 = state.edgeMap.get(e58);
        Edge edge59 = state.edgeMap.get(e59);
        Edge edge48 = state.edgeMap.get(e48);
        Edge edge49 = state.edgeMap.get(e49);
        Edge edge27 = state.edgeMap.get(e27);
        Edge edge29 = state.edgeMap.get(e29);
        Edge edge210 = state.edgeMap.get(e210);
        Edge edge47 = state.edgeMap.get(e47);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge25 = state.edgeMap.get(e25);

        // setting up the blossom structure
        node2.tree.eps = 2;
        node3.tree.eps = 1;
        node4.tree.eps = 1;
        node5.tree.eps = 2;
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.eps = 2;
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        node1.tree.eps += 1;
        Node blossom = primalUpdater.shrink(edge34);
        state.clearCurrentEdges(blossom.tree);


        // setting up the "-" blossom's tree structure
        node7.tree.eps = 1;
        blossom.tree.eps += 1;
        primalUpdater.augment(edge37);

        node6.tree.eps = 2;
        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge65, false);
        state.clearCurrentEdges(node6.tree);

        // setting up the structure of the neighbor tree
        primalUpdater.augment(edge910);
        state.setCurrentEdges(node8.tree);
        primalUpdater.grow(edge89, false);
        state.setCurrentEdges(node8.tree);

        state.setCurrentEdges(node6.tree);
        primalUpdater.expand(blossom);
        state.clearCurrentEdges(node6.tree);
        TreeEdge treeEdge = BlossomVDebugger.getTreeEdge(node6.tree, node8.tree);


        // validating blossom node duals
        node1.tree = node2.tree = null;
        assertEquals(3, node1.dual, EPS);
        assertEquals(1, node2.dual, EPS);
        assertEquals(4, node3.dual, EPS);  // tree eps is 2, node3.label = "-"
        assertEquals(0, node4.dual, EPS);  // tree eps is 2, node4.label = "+"
        assertEquals(3, node5.dual, EPS);  // tree eps is 2, node5.label = "-"

        // validating slacks of the edges in the tree structure
        assertEquals(0, edge65.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge37.slack, EPS);

        // validating the slacks of inner blossom edges
        assertEquals(7, edge24.slack, EPS);
        assertEquals(4, edge25.slack, EPS);

        // validating slacks of cross-tree edges
        //assertEquals(4, edge58.slack, EPS);
        assertEquals(4, edge59.slack, EPS);
        assertEquals(7, edge48.slack, EPS);
        assertEquals(7, edge49.slack, EPS);
        // validating slacks of the (+, inf) edges and a (-, inf) edge
        assertEquals(6, edge210.slack, EPS);
        assertEquals(7, edge27.slack, EPS);
        assertEquals(6, edge29.slack, EPS);

        // validating keys of the cross-tree and infinity edges in the heaps
        assertEquals(4, edge58.fibNode.getKey(), EPS);
        assertEquals(7, edge48.fibNode.getKey(), EPS);
        assertEquals(7, edge49.fibNode.getKey(), EPS);
        assertEquals(6, edge210.fibNode.getKey(), EPS);
        assertEquals(7, edge24.fibNode.getKey(), EPS);
        assertEquals(8, edge47.fibNode.getKey(), EPS);
        assertEquals(7, edge27.fibNode.getKey(), EPS);

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
     * Tests preserving the state of the blossom, inner and infinity edges after shrink and expand operations
     */
    @Test
    public void testExpand5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
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

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Node node8 = state.vertexMap.get(8);
        Node node9 = state.vertexMap.get(9);
        Node node10 = state.vertexMap.get(10);
        Node node11 = state.vertexMap.get(11);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge71 = state.edgeMap.get(e71);

        Edge edge78 = state.edgeMap.get(e78);
        Edge edge39 = state.edgeMap.get(e39);

        Edge edge13 = state.edgeMap.get(e13);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge35 = state.edgeMap.get(e35);
        Edge edge46 = state.edgeMap.get(e46);
        Edge edge47 = state.edgeMap.get(e47);

        Edge edge510 = state.edgeMap.get(e510);
        Edge edge610 = state.edgeMap.get(e610);
        Edge edge211 = state.edgeMap.get(e211);

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
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge71, false);
        Node blossom = primalUpdater.shrink(edge56);
        state.clearCurrentEdges(blossom.tree);
        primalUpdater.augment(edge39);
        state.setCurrentEdges(node8.tree);
        primalUpdater.grow(edge78, false);

        primalUpdater.expand(blossom);

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

        assertTrue(blossom.isRemoved);
    }

}
