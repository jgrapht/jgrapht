package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.junit.Assert.assertEquals;

public class BlossomPerfectMatchingTest {


    /**
     * Smoke test
     */
    @Test
    public void testSolve1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(5, matching.getWeight(), EPS);
    }

    /**
     * Test on a small bipartite graph
     */
    @Test
    public void testSolve2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 3, 11);
        Graphs.addEdgeWithVertices(graph, 1, 4, 8);
        Graphs.addEdgeWithVertices(graph, 2, 3, 8);
        Graphs.addEdgeWithVertices(graph, 2, 4, 2);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(13, matching.getWeight(), EPS);
    }

    /**
     * Test on a $K_{3,3}$
     */
    @Test
    public void testSolve3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 4, 7);
        Graphs.addEdgeWithVertices(graph, 1, 5, 5);
        Graphs.addEdgeWithVertices(graph, 1, 6, 2);
        Graphs.addEdgeWithVertices(graph, 2, 4, 1);
        Graphs.addEdgeWithVertices(graph, 2, 5, 3);
        Graphs.addEdgeWithVertices(graph, 2, 6, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4, 7);
        Graphs.addEdgeWithVertices(graph, 3, 5, 10);
        Graphs.addEdgeWithVertices(graph, 3, 6, 7);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(12, matching.getWeight(), EPS);
    }

    /**
     * Test on a graph with odd cycle
     */
    @Test
    public void testSolve4() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 8);
        Graphs.addEdgeWithVertices(graph, 2, 3, 8);
        Graphs.addEdgeWithVertices(graph, 3, 4, 8);
        Graphs.addEdgeWithVertices(graph, 4, 1, 8);
        Graphs.addEdgeWithVertices(graph, 2, 4, 2);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(16, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve5() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3, 5);
        Graphs.addEdgeWithVertices(graph, 1, 4, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 5);
        Graphs.addEdgeWithVertices(graph, 2, 4, 1);
        Graphs.addEdgeWithVertices(graph, 3, 4, 1);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(3, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve6() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        Graphs.addEdgeWithVertices(graph, 1, 4, 7);
        Graphs.addEdgeWithVertices(graph, 1, 5, 10);
        Graphs.addEdgeWithVertices(graph, 2, 3, 5);
        Graphs.addEdgeWithVertices(graph, 2, 4, 7);
        Graphs.addEdgeWithVertices(graph, 2, 5, 10);
        Graphs.addEdgeWithVertices(graph, 3, 4, 10);
        Graphs.addEdgeWithVertices(graph, 3, 5, 2);
        Graphs.addEdgeWithVertices(graph, 4, 5, 3);

        Graphs.addEdgeWithVertices(graph, 3,6 ,8); // dummy edge

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        BlossomPerfectMatching.Statistics statistics = perfectMatching.getStatistics();
        System.out.println(statistics.growNum);
        System.out.println(statistics.shrinkNum);
        System.out.println(statistics.expandNum);

        assertEquals(12, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve7() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);


        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve8() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);


        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve9() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);


        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve10() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);


        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve11() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);


        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }


}
