package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Test {
    public static void main(String[] args) {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 4);
    }

    private static class IntegerEdge {
        int source;
        int target;

        public IntegerEdge(int a, int b) {
            source = a;
            target = b;
        }
    }
}
