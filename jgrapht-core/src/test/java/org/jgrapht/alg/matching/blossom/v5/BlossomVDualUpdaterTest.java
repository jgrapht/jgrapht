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

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.NONE;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlossomVDualUpdater}
 *
 * @author Timofey Chudakov
 */
public class BlossomVDualUpdaterTest
{

    private BlossomVOptions noneOptions = new BlossomVOptions(NONE);

    @org.junit.Test
    public void testUpdateDuals1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, new BlossomVPrimalUpdater<>(state));
        assertTrue(dualUpdater.updateDuals(MULTIPLE_TREE_FIXED_DELTA) > 0);
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            assertEquals(root.tree.eps, 2.5, EPS);
        }
    }

    @Test
    public void testUpdateDuals2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 6);
        Graphs.addEdgeWithVertices(graph, 1, 3, 7);
        Graphs.addEdgeWithVertices(graph, 2, 3, 10);
        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, new BlossomVPrimalUpdater<>(state));
        dualUpdater.updateDuals(MULTIPLE_TREE_FIXED_DELTA);
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            assertEquals(root.tree.eps, 3, EPS);
        }
    }

    @Test
    public void testUpdateDualsSingle1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge edge = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomVInitializer<Integer, DefaultEdge> initializer = new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultEdge> state = initializer.initialize(noneOptions);
        BlossomVDualUpdater<Integer, DefaultEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, new BlossomVPrimalUpdater<>(state));
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);

        BlossomVTree tree = vertexMap.get(1).tree;
        dualUpdater.updateDualsSingle(tree);
        assertEquals(5, tree.eps, EPS);
    }

    public void testUpdateDualsSingle2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 2);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 2);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 4);

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, primalUpdater);
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
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge25 = edgeMap.get(e25);
        BlossomVEdge edge34 = edgeMap.get(e34);
        BlossomVEdge edge35 = edgeMap.get(e35);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge56 = edgeMap.get(e56);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, true, false);
        node1.tree.clearCurrentEdges();
        node6.tree.setCurrentEdges();
        primalUpdater.grow(edge56, true, false);
        node6.tree.clearCurrentEdges();

        assertTrue(dualUpdater.updateDualsSingle(node1.tree));
        assertEquals(2, node1.tree.eps, EPS);

        assertFalse(dualUpdater.updateDualsSingle(node6.tree));
        assertEquals(0, node6.tree.eps, EPS);
    }

    /**
     * Tests updating duals with connected components for basic invariants
     */
    @Test
    public void testUpdateDualsConnectedComponents1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 10);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0); // tight (-, +)
                                                                              // cross-tree edge
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 8); // infinity edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0); // matched free edge

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, primalUpdater);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node4 = vertexMap.get(4);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge56 = edgeMap.get(e56);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge56);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();

        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertEquals(10, dualChange, EPS);
        assertEquals(node1.tree.eps, node4.tree.eps, EPS);
    }

    /**
     * Tests updating duals with two connected components
     */
    @Test
    public void testUpdateDualsConnectedComponents2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);
        // cross-tree and infinity edges
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 10); // infinity edge
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0); // free matched edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 10); // infinity edge
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 6); // (-, +) cross-tree
                                                                              // edge
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 7); // (+, +) cross-tree
                                                                              // edge
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 5); // (+, -) cross-tree
                                                                              // edge

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, primalUpdater);
        Map<Integer, BlossomVNode> vertexMap = BlossomVDebugger.getVertexMap(state);
        Map<DefaultWeightedEdge, BlossomVEdge> edgeMap = BlossomVDebugger.getEdgeMap(state);

        BlossomVNode node1 = vertexMap.get(1);
        BlossomVNode node8 = vertexMap.get(8);

        BlossomVEdge edge12 = edgeMap.get(e12);
        BlossomVEdge edge23 = edgeMap.get(e23);
        BlossomVEdge edge45 = edgeMap.get(e45);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge78 = edgeMap.get(e78);

        // setting up the test case structure
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();
        node8.tree.setCurrentEdges();
        primalUpdater.grow(edge78, false, false);
        node8.tree.clearCurrentEdges();

        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertEquals(dualChange, 7, EPS);
    }

    /**
     * Tests updating duals with connected components on a big test case
     */
    @Test
    public void testUpdateDualsConnectedComponents3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0); // tight (-, +)
                                                                              // cross-tree edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e68 = Graphs.addEdgeWithVertices(graph, 6, 8, 0); // tight (-, +)
                                                                              // cross-tree edge
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0); // free matched edge
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 5);
        DefaultWeightedEdge e49 = Graphs.addEdgeWithVertices(graph, 4, 9, 5);
        DefaultWeightedEdge e710 = Graphs.addEdgeWithVertices(graph, 7, 10, 15);
        DefaultWeightedEdge e810 = Graphs.addEdgeWithVertices(graph, 8, 10, 15);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 4); // not tight (-, +)
                                                                              // cross-tree edge
        DefaultWeightedEdge e28 = Graphs.addEdgeWithVertices(graph, 2, 8, 6); // not tight (-, +)
                                                                              // cross-tree edge

        BlossomVInitializer<Integer, DefaultWeightedEdge> initializer =
            new BlossomVInitializer<>(graph);
        BlossomVState<Integer, DefaultWeightedEdge> state = initializer.initialize(noneOptions);
        BlossomVPrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater =
            new BlossomVPrimalUpdater<>(state);
        BlossomVDualUpdater<Integer, DefaultWeightedEdge> dualUpdater =
            new BlossomVDualUpdater<>(state, primalUpdater);
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
        BlossomVEdge edge24 = edgeMap.get(e24);
        BlossomVEdge edge56 = edgeMap.get(e56);
        BlossomVEdge edge67 = edgeMap.get(e67);
        BlossomVEdge edge68 = edgeMap.get(e68);
        BlossomVEdge edge910 = edgeMap.get(e910);
        BlossomVEdge edge39 = edgeMap.get(e39);
        BlossomVEdge edge49 = edgeMap.get(e49);
        BlossomVEdge edge710 = edgeMap.get(e710);
        BlossomVEdge edge810 = edgeMap.get(e810);
        BlossomVEdge edge46 = edgeMap.get(e46);
        BlossomVEdge edge28 = edgeMap.get(e28);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        node1.tree.setCurrentEdges();
        primalUpdater.grow(edge12, false, false);
        node1.tree.clearCurrentEdges();
        node5.tree.setCurrentEdges();
        primalUpdater.grow(edge56, false, false);
        node5.tree.clearCurrentEdges();

        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertTrue(dualChange > 0);
        assertEquals(node1.tree.eps, node4.tree.eps, EPS);
        assertEquals(node5.tree.eps, node8.tree.eps, EPS);
    }
}
