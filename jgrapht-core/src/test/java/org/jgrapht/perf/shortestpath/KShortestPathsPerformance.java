package org.jgrapht.perf.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.EppsteinKShortestPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.alg.shortestpath.PathValidator;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.util.CollectionUtil;
import org.jgrapht.util.SupplierUtil;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0, jvmArgs = "--illegal-access=permit")
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 8, time = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class KShortestPathsPerformance {

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testYenKShortestPaths(RandomGraphState state) {
        return computeResult(new YenKShortestPath<>(state.graph, state.pathValidator), state);
    }

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testKShortestSimplePaths(RandomGraphState state) {
        return computeResult(new KShortestSimplePaths<>(state.graph, state.pathValidator), state);
    }

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testEppsteinKShortestPaths(RandomGraphState state) {
        return computeResult(new EppsteinKShortestPath<>(state.graph), state);
    }

    private List<List<GraphPath<Integer, DefaultWeightedEdge>>> computeResult(
            KShortestPathAlgorithm<Integer, DefaultWeightedEdge> algorithm, RandomGraphState state) {
        List<List<GraphPath<Integer, DefaultWeightedEdge>>> result = new ArrayList<>(state.numberOfQueries);
        for (Pair<Integer, Integer> query : state.queries) {
            int source = query.getFirst();
            int target = query.getSecond();
            result.add(algorithm.getPaths(source, target, state.k));
        }
        return result;
    }

    @State(Scope.Benchmark)
    public static class RandomGraphState {
        @Param({"50"})
        int n;
        @Param({"0.1", "0.2"})
        double p;
        @Param({"50"})
        int k;
        @Param({"true", "false"})
        boolean createPathValidator;
        @Param({"10"})
        int numberOfRandomEdges;
        @Param({"10"})
        int numberOfQueries;


        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator;

        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph;

        PathValidator<Integer, DefaultWeightedEdge> pathValidator;

        List<Pair<Integer, Integer>> queries;


        @Setup(Level.Iteration)
        public void generateGraph() {
            generator = new GnpRandomGraphGenerator<>(n, p);
            graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            generator.generateGraph(graph);
            makeConnected(graph);
            addEdgeWeights(graph);

            queries = selectQueries();
            if (createPathValidator) {
                pathValidator = getPathValidator();
            } else {
                pathValidator = null;
            }
        }

        private List<Pair<Integer, Integer>> selectQueries() {
            Set<Pair<Integer, Integer>> result = new HashSet<>(numberOfQueries);
            Object[] vertices = graph.vertexSet().toArray();
            while (result.size() < numberOfQueries) {
                int sourceIndex = (int) (Math.random() * vertices.length);
                int targetIndex = (int) (Math.random() * vertices.length);
                Integer source = (Integer) vertices[sourceIndex];
                Integer target = (Integer) vertices[targetIndex];
                result.add(Pair.of(source, target));
            }
            return new ArrayList<>(result);
        }

        private PathValidator<Integer, DefaultWeightedEdge> getPathValidator() {
            Set<DefaultWeightedEdge> randomEdges = getRandomEdges();
            return (path, edge) -> !randomEdges.contains(edge);
        }

        private Set<DefaultWeightedEdge> getRandomEdges() {
            Set<DefaultWeightedEdge> result = CollectionUtil.newHashSetWithExpectedSize(numberOfRandomEdges);
            Object[] edges = graph.edgeSet().toArray();
            while (result.size() != numberOfQueries) {
                int index = (int) (Math.random() * edges.length);
                result.add((DefaultWeightedEdge) edges[index]);
            }
            return result;
        }

        private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph) {
            Object[] vertices = graph.vertexSet().toArray();
            for (int i = 0; i < vertices.length - 1; i++) {
                graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
            }
        }

        private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph) {
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                graph.setEdgeWeight(edge, Math.random());
            }
        }
    }
}

