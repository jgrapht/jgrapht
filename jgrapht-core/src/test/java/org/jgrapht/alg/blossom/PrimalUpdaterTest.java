package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;
import static org.jgrapht.alg.blossom.Node.Label.INFTY;
import static org.junit.Assert.*;

public class PrimalUpdaterTest {

    /**
     * Tests grow operation on a small test case
     */
    @Test
    public void testGrow1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        primalUpdater.augment(state.edgeMap.get(e23));
        primalUpdater.grow(state.edgeMap.get(e12), false);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Tree tree = node1.tree;

        assertEquals(1, state.treeNum);
        assertSame(tree, node2.tree);
        assertSame(tree, node3.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node3.isPlusNode());

        assertSame(node2.treeParent, node1);
        assertSame(node3.treeParent, node2);
        assertSame(node1.firstTreeChild, node2);
        assertSame(node2.firstTreeChild, node3);
    }

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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        assertEquals(1, node3.tree.plusInfinityEdges.size());

        primalUpdater.augment(state.edgeMap.get(edge23));

        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node6.tree.plusInfinityEdges.size());

        primalUpdater.augment(state.edgeMap.get(edge67));

        primalUpdater.grow(state.edgeMap.get(edge12), true);

        assertSame(tree, node2.tree);
        assertSame(tree, node3.tree);
        assertSame(tree, node4.tree);
        assertSame(tree, node5.tree);
        assertSame(tree, node6.tree);
        assertSame(tree, node7.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node4.isMinusNode());
        assertTrue(node6.isMinusNode());
        assertTrue(node3.isPlusNode());
        assertTrue(node5.isPlusNode());
        assertTrue(node7.isPlusNode());

        assertSame(node1.firstTreeChild, node2);
        assertSame(node2.firstTreeChild, node3);
        assertTrue(node3.firstTreeChild == node4 || node3.firstTreeChild == node6);
        assertSame(node4.firstTreeChild, node5);
        assertSame(node6.firstTreeChild, node7);

        assertSame(node2.treeParent, node1);
        assertSame(node3.treeParent, node2);
        assertSame(node4.treeParent, node3);
        assertSame(node5.treeParent, node4);
        assertSame(node6.treeParent, node3);
        assertSame(node7.treeParent, node6);
    }

    @Test
    public void testGrow3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 2);// if weight 0 -> will be augmented
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 2);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);

        Tree tree1 = node1.tree;
        Tree tree2 = node4.tree;

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge25 = state.edgeMap.get(e25);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge35 = state.edgeMap.get(e35);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge56 = state.edgeMap.get(e56);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.grow(edge12, true);


        assertEquals(2, tree1.plusInfinityEdges.size());
        assertEquals(0, Debugger.treeEdgesOf(tree1).size());


        primalUpdater.grow(edge56, true);


    }

    @Test
    public void testFindBlossomRoot() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 2); // to prevent augmentation

        State<Integer, DefaultWeightedEdge> state = new Initializer<>(graph).initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

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

        primalUpdater.grow(edge12, true);
        primalUpdater.grow(edge16, true);

        Node node1 = state.vertexMap.get(1);
        node1.tree.eps = 1; // now edge57 becomes tight

        Node root = primalUpdater.findBlossomRoot(edge57);

        assertSame(root, node1);
    }

    @Test
    public void testAugment1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);

        node1.tree.eps = 1;
        node2.tree.eps = 3;

        Edge edge12 = state.edgeMap.get(e12);

        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        primalUpdater.augment(edge12);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(INFTY, node1.label);
        assertEquals(INFTY, node2.label);
        assertEquals(0, state.treeNum);
        assertEquals(0, edge12.slack, EPS);
    }

    @Test
    public void testAugment2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 3);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        assertEquals(INFTY, node2.label);
        assertEquals(INFTY, node3.label);
        assertEquals(2, edge12.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(3, edge34.slack, EPS);
        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusInfinityEdges.size());

        node4.tree.eps = 1;
        node5.tree.eps = 2;

        primalUpdater.augment(edge45);

        assertEquals(INFTY, node4.label);
        assertEquals(INFTY, node5.label);
        assertEquals(2, edge34.slack, EPS);
        assertEquals(0, edge45.slack, EPS);
        assertEquals(2, edge56.slack, EPS);
        assertEquals(1, node6.tree.plusInfinityEdges.size());

        node1.tree.eps = 2;
        node6.tree.eps = 2;

        primalUpdater.grow(edge12, true);

        assertEquals(node1.tree, node2.tree);
        assertEquals(node1.tree, node3.tree);

        primalUpdater.grow(edge56, true);

        assertEquals(node6.tree, node5.tree);
        assertEquals(node6.tree, node4.tree);

        node1.tree.eps += 1;
        node1.tree.eps += 1;

        primalUpdater.augment(edge34);

        assertEquals(INFTY, node1.label);
        assertEquals(INFTY, node2.label);
        assertEquals(INFTY, node3.label);
        assertEquals(INFTY, node4.label);
        assertEquals(INFTY, node5.label);
        assertEquals(INFTY, node6.label);

        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge34, node3.matched);
        assertEquals(edge34, node4.matched);
        assertEquals(edge56, node5.matched);
        assertEquals(edge56, node6.matched);

    }

    @Test
    public void testAugment3() {
    }

    @Test
    public void testAugment4() {
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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        primalUpdater.grow(edge18, true);
        primalUpdater.grow(edge12, true);

        node1.tree.eps = 2;

        primalUpdater.augment(edge710);

        assertEquals(INFTY, node1.label);
        assertEquals(INFTY, node2.label);
        assertEquals(INFTY, node3.label);
        assertEquals(INFTY, node4.label);
        assertEquals(INFTY, node5.label);
        assertEquals(INFTY, node6.label);
        assertEquals(INFTY, node7.label);
        assertEquals(INFTY, node8.label);
        assertEquals(INFTY, node9.label);
        assertEquals(INFTY, node10.label);

        assertEquals(0, edge710.slack, EPS);
    }

    @Test
    public void testAugment5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e41 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge41 = state.edgeMap.get(e41);

        Tree tree1 = state.vertexMap.get(1).tree;
        Tree tree2 = state.vertexMap.get(2).tree;
        Tree tree3 = state.vertexMap.get(3).tree;
        Tree tree4 = state.vertexMap.get(4).tree;

        TreeEdge treeEdge34 = Debugger.getTreeEdge(tree3, tree4);

        primalUpdater.augment(edge12);

        assertEquals(new HashSet<>(Collections.singletonList(treeEdge34)), Debugger.treeEdgesOf(tree3));
        assertEquals(new HashSet<>(Collections.singletonList(treeEdge34)), Debugger.treeEdgesOf(tree4));
    }


    @Test
    public void testShrink1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        primalUpdater.augment(edge23);
        Debugger.setCurrentEdges(tree1);
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);


        assertEquals(blossom, node1.blossomParent);
        assertEquals(blossom, node2.blossomParent);
        assertEquals(blossom, node3.blossomParent);

        assertEquals(blossom, node1.blossomGrandparent);
        assertEquals(blossom, node2.blossomGrandparent);
        assertEquals(blossom, node3.blossomGrandparent);

        assertEquals(node1, edge14.getOppositeOriginal(node4));
        assertEquals(node4, edge14.getOppositeOriginal(node1));
        assertEquals(blossom, edge14.getOpposite(node4));
        assertEquals(node4, edge14.getOpposite(blossom));

        assertEquals(node4, edge24.getOppositeOriginal(node2));
        assertEquals(node2, edge24.getOppositeOriginal(node4));
        assertEquals(blossom, edge24.getOpposite(node4));
        assertEquals(node4, edge24.getOpposite(blossom));

        assertEquals(blossom, tree1.root);
    }

    @Test
    public void testShrink2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e14 = Graphs.addEdgeWithVertices(graph, 1, 4, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        Debugger.setCurrentEdges(tree1);
        node1.tree.eps = 3;
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);

        assertEquals(3, edge12.slack, EPS);
        assertEquals(3, edge13.slack, EPS);
        assertEquals(4, edge14.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(9, edge24.slack, EPS);

        TreeEdge treeEdge = Debugger.getTreeEdge(tree1, tree4);
        assertNotNull(treeEdge);

        assertEquals(2, treeEdge.plusPlusEdges.size());
    }

    @Test
    public void testShrink3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 0);
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 0);
        DefaultWeightedEdge e48 = Graphs.addEdgeWithVertices(graph, 4, 8, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge78);

        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        Debugger.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge67, false);
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.shrink(edge34);

        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node6.tree);
        assertNotNull(treeEdge);
        int dir = Debugger.dirToOpposite(treeEdge, node1.tree);

        assertEquals(3, treeEdge.plusPlusEdges.size());
        assertEquals(2, treeEdge.getCurrentPlusMinusHeap(dir).size());
    }

    /**
     * Tests blossom siblings and parent and grandParent updating
     */
    @Test
    public void testShrink4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
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
        for (Node current = node1.blossomSibling; current != node1; current = current.blossomSibling) {
            assertNotNull(current);
            actualBlossomNodes.add(current);
        }
        assertEquals(expectedBlossomNodes, actualBlossomNodes);
    }

    /**
     * Tests proper edge moving
     */
    @Test(timeout = 2000)
    public void testShrink5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e19 = Graphs.addEdgeWithVertices(graph, 1, 9, 0);
        DefaultWeightedEdge e28 = Graphs.addEdgeWithVertices(graph, 2, 8, 0);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 0);
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 0);
        DefaultWeightedEdge e310 = Graphs.addEdgeWithVertices(graph, 3, 10, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge36, false);
        primalUpdater.grow(edge89, false);
        Node blossom = primalUpdater.shrink(edge13);


        assertEquals(blossom, node4.treeParent);
        assertEquals(blossom, node6.treeParent);

        assertEquals(blossom, Debugger.getOpposite(edge18, node8));
        assertEquals(blossom, Debugger.getOpposite(edge19, node9));
        assertEquals(blossom, Debugger.getOpposite(edge28, node8));
        assertEquals(blossom, Debugger.getOpposite(edge29, node9));
        assertEquals(blossom, Debugger.getOpposite(edge39, node9));
        assertEquals(blossom, Debugger.getOpposite(edge310, node10));
    }

    /**
     * Tests removal of the (-,+) and addition of the (+,+) cross-tree edges and updating their slacks
     * and heaps
     */
    @Test
    public void testShrink6() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 3);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 3);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 3);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);

        node5.tree.eps = 2;
        node6.tree.eps = 2;
        primalUpdater.augment(edge56);

        node4.tree.eps = 2;
        Debugger.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);

        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.shrink(edge13);



    }


}
