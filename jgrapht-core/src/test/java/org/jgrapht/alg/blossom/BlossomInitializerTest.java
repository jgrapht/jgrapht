package org.jgrapht.alg.blossom;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlossomInitializerTest {

    @Test
    public void testGreedyInitialization() {
        DefaultUndirectedWeightedGraph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.setEdgeWeight(graph.addEdge(1, 2), 10);
        graph.setEdgeWeight(graph.addEdge(1, 3), 5);
        graph.setEdgeWeight(graph.addEdge(1, 4), 4);
        graph.setEdgeWeight(graph.addEdge(2, 3), 2);
        graph.setEdgeWeight(graph.addEdge(2, 4), 3);
        graph.setEdgeWeight(graph.addEdge(3, 4), 8);

        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State state = initializer.initialize(BlossomInitializer.InitializationType.GREEDY);

        /*matcher.forEachEdge(edge -> {
            assertTrue(edge.slack >= 0);
        });
        matcher.forEachNode(node -> {
            if (node.matched != null) {
                assertEquals(0, node.matched.slack, 0.0);
            }
            assertTrue(node.dual >= 0);
        });*/
    }

}
