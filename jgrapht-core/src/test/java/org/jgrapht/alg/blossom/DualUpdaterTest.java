package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
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
        assertTrue(dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA));
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
        DefaultWeightedEdge e24 = Graphs.addEdgeWithVertices(graph, 2, 4, 2);
        DefaultWeightedEdge e25 = Graphs.addEdgeWithVertices(graph, 2, 5, 2);
        DefaultWeightedEdge e34 = Graphs.addEdgeWithVertices(graph, 3, 4, 2);
        DefaultWeightedEdge e35 = Graphs.addEdgeWithVertices(graph, 3, 5, 4);
        DefaultWeightedEdge e45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultWeightedEdge e56 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);

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
        primalUpdater.grow(edge12);
        primalUpdater.grow(edge56);

        assertTrue(dualUpdater.updateDualsSingle(node1.tree));
        assertEquals(2, node1.tree.eps, EPS);

        assertFalse(dualUpdater.updateDualsSingle(node6.tree));
        assertEquals(0, node6.tree.eps, EPS);
    }
}
