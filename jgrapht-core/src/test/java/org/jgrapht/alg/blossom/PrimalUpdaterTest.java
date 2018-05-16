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
        assertEquals(tree, node2.tree);
        assertEquals(tree, node3.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node3.isPlusNode());

        assertEquals(node2.treeParent, node1);
        assertEquals(node3.treeParent, node2);
        assertEquals(node1.firstTreeChild, node2);
        assertEquals(node2.firstTreeChild, node3);
    }

    /**
     * Tests updating of the tree structure (tree parent, node tree references, node labels)
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

        assertEquals(node2.treeParent, node1);
        assertEquals(node3.treeParent, node2);
        assertEquals(node4.treeParent, node3);
        assertEquals(node5.treeParent, node4);
        assertEquals(node6.treeParent, node3);
        assertEquals(node7.treeParent, node6);
    }

    /**
     * Tests proper addition of new tree edge without duplicates and tree edge heaps sizes
     */
    @Test
    public void testGrow3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 0);

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

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge34 = state.edgeMap.get(e34);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge36 = state.edgeMap.get(e36);
        Edge edge46 = state.edgeMap.get(e46);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge57 = state.edgeMap.get(e57);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);

        Set<TreeEdge> treeEdges1 = Debugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges1.size());
        TreeEdge treeEdge1 = treeEdges1.iterator().next();
        assertEquals(1, treeEdge1.plusPlusEdges.size());
        assertEquals(1, Debugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        primalUpdater.grow(edge34, false);

        Set<TreeEdge> treeEdges2 = Debugger.getTreeEdgesBetween(node1.tree, node6.tree);
        assertEquals(1, treeEdges2.size());
        TreeEdge treeEdge2 = treeEdges2.iterator().next();
        assertEquals(treeEdge1, treeEdge2);
        assertEquals(2, treeEdge1.plusPlusEdges.size());
        assertEquals(2, Debugger.getMinusPlusHeap(treeEdge1, node1.tree).size());

        Set<TreeEdge> treeEdges3 = Debugger.getTreeEdgesBetween(node1.tree, node7.tree);
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
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0);
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 1);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 0);
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 1);
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 0);
        DefaultWeightedEdge e68 = Graphs.addEdgeWithVertices(graph, 6, 8, 1);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);

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
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge46 = state.edgeMap.get(e46);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge26 = state.edgeMap.get(e26);
        Edge edge35 = state.edgeMap.get(e35);
        Edge edge36 = state.edgeMap.get(e36);
        Edge edge37 = state.edgeMap.get(e37);
        Edge edge68 = state.edgeMap.get(e68);
        Edge edge78 = state.edgeMap.get(e78);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge56);
        primalUpdater.augment(edge78);
        Debugger.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);

        assertEquals(4, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);

        assertEquals(1, node4.tree.plusInfinityEdges.size());
        assertEquals(1, node4.tree.plusPlusEdges.size());

        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(1, node1.tree.plusPlusEdges.size());

        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        int dir = Debugger.dirToOpposite(treeEdge, node1.tree);

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
        Debugger.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge45, false);

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
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);

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

        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(2, Debugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, Debugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
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

        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge16, false);

        Node node1 = state.vertexMap.get(1);
        node1.tree.eps = 1; // now edge57 becomes tight

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
        assertEquals(1, node1.dual, EPS);
        assertEquals(3, node2.dual, EPS);
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

    /**
     * Test shrink on a small test case
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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        Debugger.setCurrentEdges(node1.tree);
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

        assertEquals(blossom, node1.tree.root);

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
        primalUpdater.shrink(edge13);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(0, edge13.slack, EPS);
        assertEquals(0, edge23.slack, EPS);
        assertEquals(4, edge14.slack, EPS);
        assertEquals(6, edge24.slack, EPS);

        assertEquals(4, edge14.fibNode.getKey(), EPS);
        assertEquals(6, edge24.fibNode.getKey(), EPS);

        assertEquals(3, node1.blossomEps, EPS);
        assertEquals(3, node2.blossomEps, EPS);
        assertEquals(3, node3.blossomEps, EPS);

        TreeEdge treeEdge = Debugger.getTreeEdge(tree1, tree4);
        assertNotNull(treeEdge);

        assertEquals(2, treeEdge.plusPlusEdges.size());
    }

    @Test
    public void testShrink3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 6);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 7);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 3);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 2);
        DefaultWeightedEdge e16 = Graphs.addEdgeWithVertices(graph, 1, 6, 10);
        DefaultWeightedEdge e57 = Graphs.addEdgeWithVertices(graph, 5, 7, 8);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 9);
        DefaultWeightedEdge e47 = Graphs.addEdgeWithVertices(graph, 4, 7, 7);

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
        Debugger.setCurrentEdges(node1.tree);
        node1.tree.eps = 4;
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        node1.tree.eps += 2;

        node8.tree.eps = 2;
        primalUpdater.augment(edge78);
        Debugger.setCurrentEdges(node6.tree);
        node6.tree.eps = 3;
        primalUpdater.grow(edge67, false);

        Debugger.setCurrentEdges(node1.tree);
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

        Set<TreeEdge> treeEdges = Debugger.getTreeEdgesBetween(blossom.tree, node6.tree);
        assertEquals(1, treeEdges.size());
        TreeEdge treeEdge = treeEdges.iterator().next();
        assertEquals(2, treeEdge.plusPlusEdges.size());
        assertEquals(2, Debugger.getPlusMinusHeap(treeEdge, blossom.tree).size());
        assertEquals(0, Debugger.getMinusPlusHeap(treeEdge, blossom.tree).size());

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

        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        Debugger.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge67, false);
        Debugger.setCurrentEdges(node1.tree);
        Node blossom = primalUpdater.shrink(edge34);

        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node6.tree);
        assertNotNull(treeEdge);

        assertEquals(1, blossom.tree.plusInfinityEdges.size());
        assertEquals(3, treeEdge.plusPlusEdges.size());
        assertEquals(2, Debugger.getPlusMinusHeap(treeEdge, blossom.tree).size());
        assertEquals(0, Debugger.getMinusPlusHeap(treeEdge, blossom.tree).size());
    }

    /**
     * Tests blossom siblings, parent and grandParent updating
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

        // setting up the tree structure
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge34, false);
        primalUpdater.grow(edge36, false);
        primalUpdater.grow(edge89, false);
        Node blossom = primalUpdater.shrink(edge13);

        // validating the tree structure
        assertEquals(blossom, node4.treeParent);
        assertEquals(blossom, node6.treeParent);
        assertEquals(new HashSet<>(Arrays.asList(node4, node6)), Debugger.childrenOf(blossom));

        // validating the edges endpoints
        assertEquals(blossom, Debugger.getOpposite(edge18, node8));
        assertEquals(blossom, Debugger.getOpposite(edge19, node9));
        assertEquals(blossom, Debugger.getOpposite(edge28, node8));
        assertEquals(blossom, Debugger.getOpposite(edge29, node9));
        assertEquals(blossom, Debugger.getOpposite(edge39, node9));
        assertEquals(blossom, Debugger.getOpposite(edge310, node10));

    }

    /**
     * Tests removal of the (-,+) and addition of the (+,+) cross-tree edges and updating their slacks
     * and heaps. Tests setting correct node.blossomEps
     */
    @Test
    public void testShrink7() {
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
        Node blossom = primalUpdater.shrink(edge13);

        assertEquals(3, node1.blossomEps, EPS);
        assertEquals(3, node2.blossomEps, EPS);
        assertEquals(3, node3.blossomEps, EPS);

        assertEquals(5, edge24.slack, EPS);
        assertEquals(1, edge25.slack, EPS);
        assertEquals(5, edge26.slack, EPS);

        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node4.tree);
        assertNotNull(treeEdge);
        assertEquals(0, Debugger.getMinusPlusHeap(treeEdge, node1.tree).size());
        assertEquals(1, Debugger.getPlusMinusHeap(treeEdge, node1.tree).size());
        assertEquals(2, treeEdge.plusPlusEdges.size());

        assertEquals(5, edge24.fibNode.getKey(), EPS);
        assertEquals(1, edge25.fibNode.getKey(), EPS);
        assertEquals(5, edge26.fibNode.getKey(), EPS);
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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);
        primalUpdater.augment(edge35);

        Debugger.setCurrentEdges(node4.tree);
        primalUpdater.grow(edge34, false);
        primalUpdater.expand(blossom);

        // checking tree structure
        assertEquals(node4.tree, node3.tree);
        assertEquals(node4, node3.treeParent);
        assertEquals(node3, node5.treeParent);
        assertEquals(new HashSet<>(Collections.singletonList(node3)), Debugger.childrenOf(node4));
        assertEquals(new HashSet<>(Collections.singletonList(node5)), Debugger.childrenOf(node3));


        // checking edges new endpoints
        assertEquals(node3, edge34.getOpposite(node4));
        assertEquals(node3, edge35.getOpposite(node5));

        //checking the matching
        assertEquals(edge12, node1.matched);
        assertEquals(edge12, node2.matched);
        assertEquals(edge35, node3.matched);

        // checking the labeling and isOuter flag
        assertTrue(node1.isInftyNode());
        assertTrue(node2.isInftyNode());
        assertTrue(node3.isMinusNode());
        assertTrue(node1.isOuter);
        assertTrue(node2.isOuter);
        assertTrue(node3.isOuter);
    }

    /**
     * Test primal updates after blossom expanding
     */
    @Test(timeout = 2000)
    public void testExpand2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e51 = Graphs.addEdgeWithVertices(graph, 5, 1, 0);
        DefaultWeightedEdge e62 = Graphs.addEdgeWithVertices(graph, 6, 2, 0);
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 0);
        DefaultWeightedEdge e89 = Graphs.addEdgeWithVertices(graph, 8, 9, 0);
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0);
        DefaultWeightedEdge e18 = Graphs.addEdgeWithVertices(graph, 1, 8, 0);
        DefaultWeightedEdge e58 = Graphs.addEdgeWithVertices(graph, 5, 8, 0);
        DefaultWeightedEdge e48 = Graphs.addEdgeWithVertices(graph, 4, 8, 0);
        DefaultWeightedEdge e29 = Graphs.addEdgeWithVertices(graph, 2, 9, 0);
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 0);
        DefaultWeightedEdge e210 = Graphs.addEdgeWithVertices(graph, 2, 10, 0);
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

        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        primalUpdater.grow(edge51, false);
        Node blossom = primalUpdater.shrink(edge34);
        Debugger.setCurrentEdges(node8.tree);
        primalUpdater.grow(edge89, false);

        primalUpdater.augment(edge37);
        Debugger.setCurrentEdges(node6.tree);
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
        assertEquals(node6, node2.treeParent);
        assertEquals(node2, node1.treeParent);
        assertEquals(node1, node5.treeParent);
        assertEquals(node5, node4.treeParent);
        assertEquals(node4, node3.treeParent);
        assertEquals(node3, node7.treeParent);
        assertEquals(new HashSet<>(Collections.singletonList(node2)), Debugger.childrenOf(node6));
        assertEquals(new HashSet<>(Collections.singletonList(node1)), Debugger.childrenOf(node2));
        assertEquals(new HashSet<>(Collections.singletonList(node5)), Debugger.childrenOf(node1));
        assertEquals(new HashSet<>(Collections.singletonList(node4)), Debugger.childrenOf(node5));
        assertEquals(new HashSet<>(Collections.singletonList(node3)), Debugger.childrenOf(node4));
        assertEquals(new HashSet<>(Collections.singletonList(node7)), Debugger.childrenOf(node3));
    }

    /**
     * Tests dual changes after blossom expanding
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
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
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
        Debugger.setCurrentEdges(node1.tree);
        node1.tree.eps = 3;
        primalUpdater.grow(edge12, false);
        Node blossom = primalUpdater.shrink(edge13);

        node5.tree.eps = 1;
        primalUpdater.augment(edge35);
        Debugger.setCurrentEdges(node4.tree);
        node4.tree.eps = 1;
        primalUpdater.grow(edge34, false);
        primalUpdater.expand(blossom);

        assertEquals(3, node1.dual, EPS);
        assertEquals(1, node2.dual, EPS);
        assertEquals(2, node3.dual, EPS);

        assertEquals(0, edge12.slack, EPS);
        assertEquals(-1, edge13.slack, EPS);
        assertEquals(-1, edge23.slack, EPS);
        assertEquals(0, edge34.slack, EPS);
        assertEquals(0, edge35.slack, EPS);


    }


}
