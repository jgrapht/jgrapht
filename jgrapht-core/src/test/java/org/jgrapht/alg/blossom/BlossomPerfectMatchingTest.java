package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.util.IntegerVertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.BlossomPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_AFTER;
import static org.jgrapht.alg.blossom.BlossomPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.GREEDY;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class BlossomPerfectMatchingTest {
    private BlossomPerfectMatching.Options options;

    public BlossomPerfectMatchingTest(BlossomPerfectMatching.Options options) {
        this.options = options;
    }

    @Parameterized.Parameters
    public static Object[] params() {
        return new Object[]{
                new BlossomPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_FIXED_DELTA, NONE),  // [0]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_FIXED_DELTA, GREEDY),  // [1]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_CONNECTED_COMPONENTS, NONE), // [2]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_CONNECTED_COMPONENTS, GREEDY),  // [3]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_FIXED_DELTA, NONE),  // [4]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_FIXED_DELTA, GREEDY),  // [5]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_CONNECTED_COMPONENTS, NONE),  // [6]
                new BlossomPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_CONNECTED_COMPONENTS, GREEDY),  // [7]
        };
    }

    /**
     * Smoke test
     */
    @Test
    public void testSolve1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
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

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
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

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
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

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
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

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
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

        Graphs.addEdgeWithVertices(graph, 3, 6, 8); // dummy edge

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        BlossomPerfectMatching.Statistics statistics = perfectMatching.getStatistics();
        System.out.println(statistics.growNum);
        System.out.println(statistics.shrinkNum);
        System.out.println(statistics.expandNum);
        System.out.println(matching);
        assertEquals(12, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve7() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 4, 7);
        Graphs.addEdgeWithVertices(graph, 1, 5, 3);
        Graphs.addEdgeWithVertices(graph, 1, 6, 9);
        Graphs.addEdgeWithVertices(graph, 2, 4, 8);
        Graphs.addEdgeWithVertices(graph, 2, 5, 2);
        Graphs.addEdgeWithVertices(graph, 2, 6, 9);
        Graphs.addEdgeWithVertices(graph, 3, 4, 6);
        Graphs.addEdgeWithVertices(graph, 3, 5, 1);
        Graphs.addEdgeWithVertices(graph, 3, 6, 10);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(17, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve8() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 3, 2);
        Graphs.addEdgeWithVertices(graph, 1, 4, 8);
        Graphs.addEdgeWithVertices(graph, 1, 5, 10);
        Graphs.addEdgeWithVertices(graph, 1, 6, 8);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 4, 4);
        Graphs.addEdgeWithVertices(graph, 2, 5, 8);
        Graphs.addEdgeWithVertices(graph, 2, 6, 0);
        Graphs.addEdgeWithVertices(graph, 3, 4, 8);
        Graphs.addEdgeWithVertices(graph, 3, 5, 5);
        Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        Graphs.addEdgeWithVertices(graph, 4, 5, 7);
        Graphs.addEdgeWithVertices(graph, 4, 6, 0);
        Graphs.addEdgeWithVertices(graph, 5, 6, 0);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(6, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve9() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3, 6);
        Graphs.addEdgeWithVertices(graph, 1, 4, 1);
        Graphs.addEdgeWithVertices(graph, 1, 5, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 6);
        Graphs.addEdgeWithVertices(graph, 2, 4, 6);
        Graphs.addEdgeWithVertices(graph, 2, 5, 5);
        Graphs.addEdgeWithVertices(graph, 3, 4, 7);
        Graphs.addEdgeWithVertices(graph, 3, 5, 8);
        Graphs.addEdgeWithVertices(graph, 4, 5, 8);
        Graphs.addEdgeWithVertices(graph, 1, 6, 8);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(20, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve10() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 4);
        Graphs.addEdgeWithVertices(graph, 1, 3, 4);
        Graphs.addEdgeWithVertices(graph, 1, 4, 6);
        Graphs.addEdgeWithVertices(graph, 1, 5, 8);
        Graphs.addEdgeWithVertices(graph, 2, 3, 8);
        Graphs.addEdgeWithVertices(graph, 2, 4, 10);
        Graphs.addEdgeWithVertices(graph, 2, 5, 8);
        Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5, 9);
        Graphs.addEdgeWithVertices(graph, 4, 5, 4);
        Graphs.addEdgeWithVertices(graph, 1, 6, 9);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(21, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve11() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        Graphs.addEdgeWithVertices(graph, 1, 4, 8);
        Graphs.addEdgeWithVertices(graph, 1, 5, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 2);
        Graphs.addEdgeWithVertices(graph, 2, 4, 8);
        Graphs.addEdgeWithVertices(graph, 2, 5, 1);
        Graphs.addEdgeWithVertices(graph, 3, 4, 8);
        Graphs.addEdgeWithVertices(graph, 3, 5, 5);
        Graphs.addEdgeWithVertices(graph, 4, 5, 10);
        Graphs.addEdgeWithVertices(graph, 1, 6, 7);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(16, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve12() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3, 6);
        Graphs.addEdgeWithVertices(graph, 1, 4, 3);
        Graphs.addEdgeWithVertices(graph, 1, 5, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3, 7);
        Graphs.addEdgeWithVertices(graph, 2, 4, 7);
        Graphs.addEdgeWithVertices(graph, 2, 5, 7);
        Graphs.addEdgeWithVertices(graph, 3, 4, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5, 2);
        Graphs.addEdgeWithVertices(graph, 1, 6, 2);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(11, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve13() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 0, 8);
        Graphs.addEdgeWithVertices(graph, 2, 0, 5);
        Graphs.addEdgeWithVertices(graph, 2, 1, 9);
        Graphs.addEdgeWithVertices(graph, 3, 0, 2);
        Graphs.addEdgeWithVertices(graph, 3, 1, 6);
        Graphs.addEdgeWithVertices(graph, 3, 2, 7);
        Graphs.addEdgeWithVertices(graph, 4, 0, 3);
        Graphs.addEdgeWithVertices(graph, 4, 1, 5);
        Graphs.addEdgeWithVertices(graph, 4, 2, 5);
        Graphs.addEdgeWithVertices(graph, 4, 3, 7);
        Graphs.addEdgeWithVertices(graph, 5, 4, 6);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(17, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve14() {
        options = new BlossomPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_FIXED_DELTA, NONE);
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 0, 9);
        Graphs.addEdgeWithVertices(graph, 2, 0, 8);
        Graphs.addEdgeWithVertices(graph, 2, 1, 3);
        Graphs.addEdgeWithVertices(graph, 3, 0, 5);
        Graphs.addEdgeWithVertices(graph, 3, 1, 4);
        Graphs.addEdgeWithVertices(graph, 3, 2, 10);
        Graphs.addEdgeWithVertices(graph, 4, 0, 3);
        Graphs.addEdgeWithVertices(graph, 4, 1, 2);
        Graphs.addEdgeWithVertices(graph, 4, 2, 4);
        Graphs.addEdgeWithVertices(graph, 4, 3, 8);
        Graphs.addEdgeWithVertices(graph, 5, 1, 4);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(13, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve15() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 0, 8);
        Graphs.addEdgeWithVertices(graph, 2, 0, 7);
        Graphs.addEdgeWithVertices(graph, 2, 1, 10);
        Graphs.addEdgeWithVertices(graph, 3, 0, 8);
        Graphs.addEdgeWithVertices(graph, 3, 1, 5);
        Graphs.addEdgeWithVertices(graph, 3, 2, 3);
        Graphs.addEdgeWithVertices(graph, 4, 0, 9);
        Graphs.addEdgeWithVertices(graph, 4, 1, 5);
        Graphs.addEdgeWithVertices(graph, 4, 2, 4);
        Graphs.addEdgeWithVertices(graph, 4, 3, 10);
        Graphs.addEdgeWithVertices(graph, 5, 1, 4);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(16, matching.getWeight(), EPS);
    }

    @Test
    public void testSolve16() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve17() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve18() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve19() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();
    }

    @Test
    public void testSolve20() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        CompleteGraphGenerator<Integer, DefaultWeightedEdge> generator = new CompleteGraphGenerator<>(10);
        generator.generateGraph(graph, new IntegerVertexFactory());

        BlossomPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new BlossomPerfectMatching<>(graph, options);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.solve();

        assertEquals(5, matching.getWeight(), EPS);
    }


}
