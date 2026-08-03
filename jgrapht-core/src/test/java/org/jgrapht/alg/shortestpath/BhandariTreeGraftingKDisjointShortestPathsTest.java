package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class BhandariTreeGraftingKDisjointShortestPathsTest extends KDisjointShortestPathsTestCase {
    @Override
    protected <V, E> KShortestPathAlgorithm<V, E> getKShortestPathAlgorithm(Graph<V, E> graph) {
        return new BhandariTreeGraftingKDisjointShortestPaths<>(graph);
    }


    /**
     * Test of the following case, with k = 2.
     *
     * @formatter:off <pre>
     *           A---------C
     *          /     1     \
     *        1/             \1
     *        /               \
     *       /                 \
     * source         D-------- sink
     *       \       /   -3    /
     *        \     /-2       /
     *     100 \   /         /
     *          \ /         /7
     *           B---------/
     *
     * </pre>
     * @formatter:on Vertices are source,sink,A,B,C,D
     * Weights are the numbers close to each edge
     * Edges are directed from left to right, except, sink->D, D->B
     * The initial shortest path tree has shortest paths over the sink vertex to D and B.
     * In the first iteration, the vertices D, and B should be pruned. The second shortest path should be source->B->sink.
     */
    @Test
    public void testDualPath() {
        GraphBuilder<String, DefaultWeightedEdge, ? extends SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>> builder = SimpleDirectedWeightedGraph.createBuilder(DefaultWeightedEdge::new);

        final String source = "source";
        final String sink = "sink";
        final String A = "A";
        final String B = "B";
        final String C = "C";
        final String D = "D";

        builder.addEdge(source, A, 1);
        builder.addEdge(source, B, 100);

        builder.addEdge(A, C, 1);
        builder.addEdge(C, sink, 1);

        builder.addEdge(sink, D, -3);
        builder.addEdge(D, B, -2);
        builder.addEdge(B, sink, 7);
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = builder.build();

        List<DefaultWeightedEdge> path = Arrays.asList(
                graph.getEdge(source, A),
                graph.getEdge(A, C),
                graph.getEdge(C, sink)
        );

        List<GraphPath<String, DefaultWeightedEdge>> paths = new BhandariTreeGraftingKDisjointShortestPaths<>(graph).getPaths(source, sink, 2);
        Assert.assertEquals(2, paths.size());
        Assert.assertEquals(3, paths.get(0).getWeight(), 0.001);
        Assert.assertEquals(107, paths.get(1).getWeight(), 0.001);
    }

    /**
     * Create a graph in such a way that the transformGraph step creates a negative cycle with improperly configured tolerance
     * The graph looks like the following (directed edges from source towards sink).
     *
     * <pre>
     * @formatter:off
     *           A         D         G
     *          / \       / \       / \
     *         /   \     /   \     /   \
     *        /     \   /     \   /     \
     *       /       \ /       \ /       \
     * source         C         F         sink
     *       \       / \       / \       /
     *        \     /   \     /   \     /
     *         \   /     \   /     \   /
     *          \ /       \ /       \ /
     *           B         E         H
     * </pre>
     *
     * @formatter:on Clearly, there are two edge-disjoint paths from source to sink.
     * The edge-weights are symmetric across the horizontal line from source to sink, e.g., A->C.weight == B->C.weight.
     * All edges, except D->F, and E->F have the same weight (1.0).
     * The weights of D->F, and E->F is large enough that E->F.weight + [small number] == E->F.weight due to rounding numbers
     * In the first iteration of the algorithm, the shortest path from sink to source is found. In this shortest path, each edge
     * is inverted and its weight negated. This (may) induce a negative cycle in the graph due to floating point rounding numbers.
     * For example, assume the first shortest path is source->A->C->D->F->G->sink. The algorithm then update the shortest path tree.
     * When it hits vertex F again, it will have the weight of the path source->B->C->E->F.
     * Due to rounding errors, this will be equal to the large weights of E->F.
     * There is a negative edge from F->D, so the shortest path tree expands that way, with an accumulated weight of 0.
     * The weight of edge D->C is non-zero and negative, which means we now have a negative cycle.
     */
    @Test
    public void toleranceTest() {
        double defaultWeight = 1.0d;
        double weight = defaultWeight;
        double epsilon = defaultWeight;
        while (weight + epsilon * 3 != weight) {
            weight *= 2;
        }
        weight *= 2;
        final String source = "source";
        final String sink = "sink";
        final String A = "A";
        final String B = "B";
        final String C = "C";
        final String D = "D";
        final String E = "E";
        final String F = "F";
        final String G = "G";
        final String H = "H";

        GraphBuilder<Object, DefaultWeightedEdge, ? extends SimpleDirectedWeightedGraph<Object, DefaultWeightedEdge>> builder = SimpleDirectedWeightedGraph.createBuilder(DefaultWeightedEdge::new);
        builder.addEdge(source, A, defaultWeight);
        builder.addEdge(source, B, defaultWeight);
        builder.addEdge(A, C, defaultWeight);
        builder.addEdge(B, C, defaultWeight);

        builder.addEdge(C, D, defaultWeight);
        builder.addEdge(D, F, weight);

        builder.addEdge(C, E, defaultWeight);
        builder.addEdge(E, F, weight);

        builder.addEdge(F, G, defaultWeight);
        builder.addEdge(F, H, defaultWeight);
        builder.addEdge(G, sink, defaultWeight);
        builder.addEdge(H, sink, defaultWeight);

        SimpleDirectedWeightedGraph<Object, DefaultWeightedEdge> graph = builder.build();
        BhandariTreeGraftingKDisjointShortestPaths<Object, DefaultWeightedEdge> ksp = new BhandariTreeGraftingKDisjointShortestPaths<>(graph, ToleranceDoubleComparator.DEFAULT_EPSILON);
        try {
            List<GraphPath<Object, DefaultWeightedEdge>> paths = ksp.getPaths(source, sink, 2);
            Assert.fail();
        } catch (RuntimeException re) {
        }

        // If we configure the epsilon correctly for these weights, we should get the desired paths.
        ksp = new BhandariTreeGraftingKDisjointShortestPaths<>(graph, defaultWeight * 10);
        List<GraphPath<Object, DefaultWeightedEdge>> paths = ksp.getPaths(source, sink, 2);
        Assert.assertEquals(2, paths.size());
    }
}
