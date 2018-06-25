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
import java.util.Set;

import static org.junit.Assert.*;

public class NodeTest {

    private KolmogorovMinimumWeightPerfectMatching.Options noneOptions = new KolmogorovMinimumWeightPerfectMatching.Options(Initializer.InitializationType.NONE);


    /**
     * Tests correct edge addition and correct edge direction
     */
    @Test
    public void testAddEdge() {
        Node from = new Node();
        Node to = new Node();
        Edge nodeEdge = new Edge();
        nodeEdge.headOriginal[0] = to;
        nodeEdge.headOriginal[1] = from;

        from.addEdge(nodeEdge, 0);
        to.addEdge(nodeEdge, 1);

        assertSame(from.first[0], nodeEdge);
        assertSame(to.first[1], nodeEdge);

        assertNull(from.first[1]);
        assertNull(to.first[0]);

        assertSame(nodeEdge.head[0], to);
        assertSame(nodeEdge.head[1], from);

        for (Node.IncidentEdgeIterator iterator = from.adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            int dir = iterator.getDir();
            assertSame(edge.head[dir], to);
        }

        for (Node.IncidentEdgeIterator iterator = to.adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            int dir = iterator.getDir();
            assertSame(edge.head[dir], from);
        }
    }

    /**
     * Tests iteration over all incident edges and correct edge direction
     */
    @Test
    public void testAdjacentEdgeIterator1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge14 = state.edgeMap.get(e14);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge34 = state.edgeMap.get(e34);

        testAdjacentEdgeIteratorOf(node1, new HashSet<>(Arrays.asList(edge12, edge14)));
        testAdjacentEdgeIteratorOf(node2, new HashSet<>(Arrays.asList(edge12, edge23, edge24)));
        testAdjacentEdgeIteratorOf(node3, new HashSet<>(Arrays.asList(edge23, edge34)));
        testAdjacentEdgeIteratorOf(node4, new HashSet<>(Arrays.asList(edge14, edge24, edge34)));
    }

    /**
     * Tests {@link Node.IncidentEdgeIterator} for a particular node
     *
     * @param node                  node whose adjacent edge iterator is been tested
     * @param expectedIncidentEdges expected incident edges of the {@code node}
     */
    private void testAdjacentEdgeIteratorOf(Node node, Set<Edge> expectedIncidentEdges) {
        Set<Edge> adj = new HashSet<>();
        for (Node.IncidentEdgeIterator iterator = node.adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            assertEquals(node, edge.head[1 - iterator.getDir()]);
            adj.add(edge);
        }
        assertEquals(adj, expectedIncidentEdges);
    }

    /**
     * Tests the proper removal of nodes from their child lists including removal of tree roots from
     * tree roots linked list
     */
    @Test
    public void testRemoveFromChildList() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);


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
        Edge edge14 = state.edgeMap.get(e14);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge16 = state.edgeMap.get(e16);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge14, false);

        Set<Node> empty = new HashSet<>();

        assertEquals(new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.childrenOf(node2));
        node3.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.childrenOf(node2));

        assertEquals(new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.childrenOf(node4));
        node5.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.childrenOf(node4));

        assertEquals(new HashSet<>(Arrays.asList(node2, node4)), BlossomVDebugger.childrenOf(node1));
        node4.removeFromChildList();
        assertEquals(new HashSet<>(Collections.singletonList(node2)), BlossomVDebugger.childrenOf(node1));
        node2.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.childrenOf(node1));

        assertEquals(new HashSet<>(Arrays.asList(node1, node6)), BlossomVDebugger.treeRoots(state));
        node1.removeFromChildList();
        assertEquals(new HashSet<>(Collections.singletonList(node6)), BlossomVDebugger.treeRoots(state));
        node6.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.treeRoots(state));
    }

    /**
     * Tests proper moving of child lists
     */
    @Test
    public void testMoveChildrenTo() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);

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
        Edge edge14 = state.edgeMap.get(e14);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge78 = state.edgeMap.get(e78);

        // building tree structures
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge78);
        state.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge14, false);
        state.clearCurrentEdges(node1.tree);
        state.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge67, false);
        state.setCurrentEdges(node6.tree);

        // node5 and node4 have no children
        node5.moveChildrenTo(node3);
        assertEquals(new HashSet<>(), BlossomVDebugger.childrenOf(node3));

        // moving child list of size 1 to empty list
        node2.moveChildrenTo(node4);

        assertEquals(new HashSet<>(Arrays.asList(node3, node5)), BlossomVDebugger.childrenOf(node4));
        //moving child list of size 2 to empty list
        node4.moveChildrenTo(node2);
        assertEquals(new HashSet<>(Arrays.asList(node3, node5)), BlossomVDebugger.childrenOf(node2));

        // moving child list to non-empty child list
        node1.moveChildrenTo(node6);
        assertEquals(new HashSet<>(Arrays.asList(node2, node4, node7)), BlossomVDebugger.childrenOf(node6));
    }

}
