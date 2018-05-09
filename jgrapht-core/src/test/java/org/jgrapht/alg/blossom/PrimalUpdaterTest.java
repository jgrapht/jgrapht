package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;
import static org.jgrapht.alg.blossom.Node.Label.INFTY;
import static org.junit.Assert.*;

public class PrimalUpdaterTest {

    @Test
    public void testGrow1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge growEdge = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge matchEdge = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);

        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        primalUpdater.augment(state.edgeMap.get(matchEdge));
        primalUpdater.grow(state.edgeMap.get(growEdge));

        assertEquals(1, state.treeNum);
        Node root = state.vertexMap.get(1);
        Node node1 = state.vertexMap.get(2);
        Node node2 = state.vertexMap.get(3);
        Tree tree = root.tree;
        assertSame(tree, node1.getTree());
        assertSame(tree, node2.getTree());

        assertTrue(node1.isMinusNode());
        assertTrue(node2.isPlusNode());

        assertSame(node1.parent, root);
        assertSame(node2.parent, node1);
        assertSame(root.firstTreeChild, node1);
        assertSame(node1.firstTreeChild, node2);

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

        primalUpdater.grow(state.edgeMap.get(edge12));

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

        assertSame(node2.parent, node1);
        assertSame(node3.parent, node2);
        assertSame(node4.parent, node3);
        assertSame(node5.parent, node4);
        assertSame(node6.parent, node3);
        assertSame(node7.parent, node6);
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

        assertEquals(1, node1.tree.plusInfinityEdges.size());
        assertEquals(2, node4.tree.plusInfinityEdges.size());
        assertEquals(2, node5.tree.plusInfinityEdges.size());

        primalUpdater.augment(edge45);

        assertEquals(1, node6.tree.plusInfinityEdges.size());

        primalUpdater.grow(edge12);


        assertEquals(2, tree1.plusInfinityEdges.size());


        primalUpdater.grow(edge56);


        /*assertEquals(1, treeEdge.getCurrentPlusMinusHeap(tree2.currentDirection).size());
        assertEquals(1, treeEdge.getCurrentMinusPlusHeap(tree2.currentDirection).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());*/
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

        primalUpdater.grow(edge12);
        primalUpdater.grow(edge16);

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

        primalUpdater.grow(edge12);

        assertEquals(node1.tree, node2.tree);
        assertEquals(node1.tree, node3.tree);

        primalUpdater.grow(edge56);

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

        primalUpdater.grow(edge18);
        primalUpdater.grow(edge12);

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
    }

}
