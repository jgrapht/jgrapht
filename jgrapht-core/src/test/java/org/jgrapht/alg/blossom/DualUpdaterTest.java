package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;
import static org.junit.Assert.*;

public class DualUpdaterTest {

    @org.junit.Test
    public void testUpdateDuals1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, new PrimalUpdater<>(state));
        assertTrue(dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA) > 0);
        for (State.TreeRootsIterator iterator = state.treeRootsIterator(); iterator.hasNext(); ) {
            Node root = iterator.next();
            assertEquals(root.tree.eps, 2.5, EPS);
        }
    }

    @Test
    public void testUpdateDuals2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 6);
        Graphs.addEdgeWithVertices(graph, 1, 3, 7);
        Graphs.addEdgeWithVertices(graph, 2, 3, 10);
        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, new PrimalUpdater<>(state));
        dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA);
        for (State.TreeRootsIterator iterator = state.treeRootsIterator(); iterator.hasNext(); ) {
            Tree tree = iterator.next().tree;
            assertEquals(tree.eps, 3, EPS);
        }
    }

    @Test
    public void testUpdateDualsSingle1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge edge = Graphs.addEdgeWithVertices(graph, 1, 2);
        graph.setEdgeWeight(edge, 5);
        Initializer<Integer, DefaultEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(NONE);
        DualUpdater<Integer, DefaultEdge> dualUpdater = new DualUpdater<>(state, new PrimalUpdater<>(state));
        Tree tree = state.vertexMap.get(1).tree;
        dualUpdater.updateDualsSingle(tree);
        assertEquals(5, tree.eps, EPS);
    }

    public void testUpdateDualsSingle2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 2);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 2);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 4);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, primalUpdater);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);

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
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, true);
        Debugger.clearCurrentEdges(node1.tree);
        Debugger.setCurrentEdges(node6.tree);
        primalUpdater.grow(edge56, true);
        Debugger.clearCurrentEdges(node6.tree);

        assertTrue(dualUpdater.updateDualsSingle(node1.tree));
        assertEquals(2, node1.tree.eps, EPS);

        assertFalse(dualUpdater.updateDualsSingle(node6.tree));
        assertEquals(0, node6.tree.eps, EPS);
    }

    /**
     * Tests updating duals with connected components for basic invariants
     */
    @Test
    public void testUpdateDualsConnectedComponents1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 10);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0); // tight (-, +) cross-tree edge
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 8); // infinity edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0); // matched free edge

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, primalUpdater);

        Node node1 = state.vertexMap.get(1);
        Node node4 = state.vertexMap.get(4);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge56 = state.edgeMap.get(e56);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge56);
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Debugger.clearCurrentEdges(node1.tree);

        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertEquals(10, dualChange, EPS);
        assertEquals(node1.tree.eps, node4.tree.eps, EPS);
    }

    /**
     * Tests updating duals with two connected components
     */
    @Test
    public void testUpdateDualsConnectedComponents2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // tree edges
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e78 = Graphs.addEdgeWithVertices(graph, 7, 8, 0);
        // cross-tree and infinity edges
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 10); // infinity edge
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);  // free matched edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 10); // infinity edge
        DefaultWeightedEdge e26 = Graphs.addEdgeWithVertices(graph, 2, 6, 6); // (-, +) cross-tree edge
        DefaultWeightedEdge e36 = Graphs.addEdgeWithVertices(graph, 3, 6, 7); // (+, +) cross-tree edge
        DefaultWeightedEdge e37 = Graphs.addEdgeWithVertices(graph, 3, 7, 5); // (+, -) cross-tree edge

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, primalUpdater);

        Node node1 = state.vertexMap.get(1);
        Node node8 = state.vertexMap.get(8);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge23 = state.edgeMap.get(e23);
        Edge edge45 = state.edgeMap.get(e45);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge78 = state.edgeMap.get(e78);

        // setting up the test case structure
        primalUpdater.augment(edge23);
        primalUpdater.augment(edge45);
        primalUpdater.augment(edge67);
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Debugger.clearCurrentEdges(node1.tree);
        Debugger.setCurrentEdges(node8.tree);
        primalUpdater.grow(edge78, false);
        Debugger.clearCurrentEdges(node8.tree);

        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertEquals(dualChange, 7, EPS);
    }

    /**
     * Tests updating duals with connected components on a big test case
     */
    @Test
    public void testUpdateDualsConnectedComponents3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 0); // tight (-, +) cross-tree edge
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);
        DefaultWeightedEdge e67 = Graphs.addEdgeWithVertices(graph, 6, 7, 0);
        DefaultWeightedEdge e68 = Graphs.addEdgeWithVertices(graph, 6, 8, 0); // tight (-, +) cross-tree edge
        DefaultWeightedEdge e910 = Graphs.addEdgeWithVertices(graph, 9, 10, 0); // free matched edge
        DefaultWeightedEdge e39 = Graphs.addEdgeWithVertices(graph, 3, 9, 5);
        DefaultWeightedEdge e49 = Graphs.addEdgeWithVertices(graph, 4, 9, 5);
        DefaultWeightedEdge e710 = Graphs.addEdgeWithVertices(graph, 7, 10, 15);
        DefaultWeightedEdge e810 = Graphs.addEdgeWithVertices(graph, 8, 10, 15);
        DefaultWeightedEdge e46 = Graphs.addEdgeWithVertices(graph, 4, 6, 4); // not tight (-, +) cross-tree edge
        DefaultWeightedEdge e28 = Graphs.addEdgeWithVertices(graph, 2, 8, 6); // not tight (-, +) cross-tree edge

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);
        PrimalUpdater<Integer, DefaultWeightedEdge> primalUpdater = new PrimalUpdater<>(state);
        DualUpdater<Integer, DefaultWeightedEdge> dualUpdater = new DualUpdater<>(state, primalUpdater);

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
        Edge edge24 = state.edgeMap.get(e24);
        Edge edge56 = state.edgeMap.get(e56);
        Edge edge67 = state.edgeMap.get(e67);
        Edge edge68 = state.edgeMap.get(e68);
        Edge edge910 = state.edgeMap.get(e910);
        Edge edge39 = state.edgeMap.get(e39);
        Edge edge49 = state.edgeMap.get(e49);
        Edge edge710 = state.edgeMap.get(e710);
        Edge edge810 = state.edgeMap.get(e810);
        Edge edge46 = state.edgeMap.get(e46);
        Edge edge28 = state.edgeMap.get(e28);

        primalUpdater.augment(edge23);
        primalUpdater.augment(edge67);
        primalUpdater.augment(edge910);
        Debugger.setCurrentEdges(node1.tree);
        primalUpdater.grow(edge12, false);
        Debugger.clearCurrentEdges(node1.tree);
        TreeEdge treeEdge = Debugger.getTreeEdge(node1.tree, node8.tree);
        Debugger.setCurrentEdges(node5.tree);
        primalUpdater.grow(edge56, false);
        Debugger.clearCurrentEdges(node5.tree);


        double dualChange = dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
        assertTrue(dualChange > 0);
        assertEquals(node1.tree.eps, node4.tree.eps, EPS);
        assertEquals(node5.tree.eps, node8.tree.eps, EPS);
    }
}
