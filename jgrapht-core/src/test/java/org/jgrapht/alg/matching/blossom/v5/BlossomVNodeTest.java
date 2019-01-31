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

import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.*;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.NONE;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVNode}
 *
 * @author Timofey Chudakov
 */
public class BlossomVNodeTest
{

    private BlossomVOptions noneOptions = new BlossomVOptions(NONE);

    @Test
    public void testLabels()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);

        BlossomVNode node = vertexMap.get(1); // position doesn't matter

        node.label = INFINITY;
        assertTrue(node.isInfinityNode());

        node.label = PLUS;
        assertTrue(node.isPlusNode());

        node.label = MINUS;
        assertTrue(node.isMinusNode());

    }

    @Test
    public void testAncestors()
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
        primalUpdater.grow(edge12, false, false);

        assertEquals(node1, node2.getTreeParent());
        assertEquals(node2, node3.getTreeParent());
        assertEquals(node1, node3.getTreeGrandparent());
    }

    /**
     * Tests correct edge addition and correct edge direction
     */
    @Test
    public void testAddEdge()
    {
        BlossomVNode from = new BlossomVNode(-1);
        BlossomVNode to = new BlossomVNode(-1);
        BlossomVEdge nodeEdge = new BlossomVEdge(-1);
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

        for (BlossomVNode.IncidentEdgeIterator iterator = from.incidentEdgesIterator();
            iterator.hasNext();)
        {
            BlossomVEdge edge = iterator.next();
            int dir = iterator.getDir();
            assertSame(edge.head[dir], to);
        }

        for (BlossomVNode.IncidentEdgeIterator iterator = to.incidentEdgesIterator();
            iterator.hasNext();)
        {
            BlossomVEdge edge = iterator.next();
            int dir = iterator.getDir();
            assertSame(edge.head[dir], from);
        }
    }

    /**
     * Tests correct edge removal from linked lists of incidents edges
     */
    @Test
    public void testRemoveEdge()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);

        BlossomVEdge edge12 = edgeMap.get(e12);

        int dir = edge12.getDirFrom(node1);
        node1.removeEdge(edge12, dir);
        assertEquals(Collections.emptySet(), BlossomVDebugger.getEdgesOf(node1));

        node2.removeEdge(edge12, 1 - dir);
        assertEquals(Collections.emptySet(), BlossomVDebugger.getEdgesOf(node2));
    }

    /**
     * Tests iteration over all incident edges and correct edge direction
     */
    @Test
    public void testIncidentEdgeIterator1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node2 = vertexMap.get(2);
        BlossomVNode node3 = vertexMap.get(3);
        BlossomVNode node4 = vertexMap.get(4);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge14 = edgeMap.get(e14);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge34 = edgeMap.get(e34);

        testIncidentEdgeIteratorOf(node1, new HashSet<>(Arrays.asList(edge12, edge14)));
        testIncidentEdgeIteratorOf(node2, new HashSet<>(Arrays.asList(edge12, edge23, edge24)));
        testIncidentEdgeIteratorOf(node3, new HashSet<>(Arrays.asList(edge23, edge34)));
        testIncidentEdgeIteratorOf(node4, new HashSet<>(Arrays.asList(edge14, edge24, edge34)));
    }

    /**
     * Tests {@link BlossomVNode.IncidentEdgeIterator} for a particular node
     *
     * @param node node whose adjacent edge iterator is been tested
     * @param expectedIncidentEdges expected incident edges of the {@code node}
     */
    private void testIncidentEdgeIteratorOf(
        BlossomVNode node, Set<BlossomVEdge> expectedIncidentEdges)
    {
        Set<BlossomVEdge> adj = new HashSet<>();
        for (BlossomVNode.IncidentEdgeIterator iterator = node.incidentEdgesIterator();
            iterator.hasNext();)
        {
            BlossomVEdge edge = iterator.next();
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
    public void testRemoveFromChildList()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);

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
        BlossomVEdge edge14 = edgeMap.get(e14);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge16 = edgeMap.get(e16);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge14, false, false);

        Set<BlossomVNode> empty = new HashSet<>();

        assertEquals(
            new HashSet<>(Collections.singletonList(node3)), BlossomVDebugger.getChildrenOf(node2));
        node3.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.getChildrenOf(node2));

        assertEquals(
            new HashSet<>(Collections.singletonList(node5)), BlossomVDebugger.getChildrenOf(node4));
        node5.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.getChildrenOf(node4));

        assertEquals(
            new HashSet<>(Arrays.asList(node2, node4)), BlossomVDebugger.getChildrenOf(node1));
        node4.removeFromChildList();
        assertEquals(
            new HashSet<>(Collections.singletonList(node2)), BlossomVDebugger.getChildrenOf(node1));
        node2.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.getChildrenOf(node1));

        assertEquals(
            new HashSet<>(Arrays.asList(node1, node6)), BlossomVDebugger.getTreeRoots(state));
        node1.removeFromChildList();
        assertEquals(
            new HashSet<>(Collections.singletonList(node6)), BlossomVDebugger.getTreeRoots(state));
        node6.removeFromChildList();
        assertEquals(empty, BlossomVDebugger.getTreeRoots(state));
    }

    /**
     * Tests proper moving of child lists
     */
    @Test
    public void testMoveChildrenTo()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);

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
        BlossomVEdge edge14 = edgeMap.get(e14);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge78 = edgeMap.get(e78);

        // building tree structures
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge78);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        primalUpdater.grow(edge14, false, false);
        node1.tree.clearCurrentEdges();
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge67, false, false);
        node6.tree.setCurrentEdges();

        // node5 and node4 have no children
        node5.moveChildrenTo(node3);
        assertEquals(new HashSet<>(), BlossomVDebugger.getChildrenOf(node3));

        // moving child list of size 1 to empty list
        node2.moveChildrenTo(node4);

        assertEquals(
            new HashSet<>(Arrays.asList(node3, node5)), BlossomVDebugger.getChildrenOf(node4));
        // moving child list of size 2 to empty list
        node4.moveChildrenTo(node2);
        assertEquals(
            new HashSet<>(Arrays.asList(node3, node5)), BlossomVDebugger.getChildrenOf(node2));

        // moving child list to non-empty child list
        node1.moveChildrenTo(node6);
        assertEquals(
            new HashSet<>(Arrays.asList(node2, node4, node7)),
            BlossomVDebugger.getChildrenOf(node6));
    }

    /**
     * Tests correct search of penultimate blossom
     */
    @Test
    public void testGetPenultimateBlossom()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e15 = Graphs.addEdgeWithVertices(graph, 1, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);

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
        BlossomVEdge edge16 = edgeMap.get(e16);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge15 = edgeMap.get(e15);

        node1.tree.setCurrentEdges();
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.grow(edge12, true, false);
        BlossomVNode blossom1 = primalUpdater.shrink(edge13, false);
        primalUpdater.shrink(edge15, false);

        assertEquals(blossom1, node1.getPenultimateBlossom());
        assertEquals(blossom1, node2.getPenultimateBlossom());
        assertEquals(blossom1, node3.getPenultimateBlossom());
        assertEquals(node4, node4.getPenultimateBlossom());
        assertEquals(node5, node5.getPenultimateBlossom());
    }

    @Test
    public void testGetPenultimateBlossomAndFixBlossomGrandparent()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e15 = Graphs.addEdgeWithVertices(graph, 1, 5, 0);
        DefaultWeightedEdge e17 = Graphs.addEdgeWithVertices(graph, 1, 7, 0);
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e19 = Graphs.addEdgeWithVertices(graph, 1, 9, 0);

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
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge13 = edgeMap.get(e13);
        BlossomVEdge edge15 = edgeMap.get(e15);
        BlossomVEdge edge17 = edgeMap.get(e17);
        BlossomVEdge edge18 = edgeMap.get(e18);
        BlossomVEdge edge19 = edgeMap.get(e19);

        node1.tree.setCurrentEdges();
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.grow(edge12, true, false);
        BlossomVNode blossom1 = primalUpdater.shrink(edge13, false);
        BlossomVNode blossom2 = primalUpdater.shrink(edge15, false);
        BlossomVNode blossom3 = primalUpdater.shrink(edge17, false);
        blossom3.tree.clearCurrentEdges();
        node8.tree.setCurrentEdges();
        primalUpdater.augment(edge19);
        primalUpdater.grow(edge18, false, false);

        // let's assume the worst case: all blossomGrandparent references point to blossom3
        node1.blossomGrandparent =
            blossom1.blossomGrandparent = blossom2.blossomGrandparent = blossom3;
        assertEquals(blossom2, node1.getPenultimateBlossomAndFixBlossomGrandparent());
        assertNotEquals(blossom3, node1.blossomGrandparent);
        assertNotEquals(blossom3, blossom1.blossomGrandparent);
    }

}
