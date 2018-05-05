package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.junit.Test;

import static org.jgrapht.alg.blossom.BlossomInitializer.InitializationType.NONE;
import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DualUpdaterTest {

    @org.junit.Test
    public void testUpdateDuals1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge edge = Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(NONE);
        DualUpdater<Integer, DefaultEdge> dualUpdater = new DualUpdater<>(state, new PrimalUpdater<>(state));
        assertTrue(dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA));
        for (State.TreeRootsIterator iterator = state.treeRootsIterator(); iterator.hasNext(); ) {
            Node root = iterator.next();
            assertEquals(root.tree.eps, 2.5, EPS);
        }
    }

    @Test
    public void testUpdateDuals2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 3, 7);
        Graphs.addEdgeWithVertices(graph, 2, 3, 10);
        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(NONE);
        DualUpdater<Integer, DefaultEdge> dualUpdater = new DualUpdater<>(state, new PrimalUpdater<>(state));
        dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA);
        for (State.TreeRootsIterator iterator = state.treeRootsIterator(); iterator.hasNext(); ) {
            Tree tree = iterator.next().tree;
            assertEquals(tree.eps, 3, EPS);
        }
    }
}
