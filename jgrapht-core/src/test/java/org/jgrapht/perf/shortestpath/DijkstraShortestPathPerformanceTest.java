/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.perf.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.ALTAdmissibleHeuristic;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.alg.shortestpath.BidirectionalAStarShortestPath;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.util.StopWatch;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A small benchmark comparing Dijkstra like algorithms. The benchmark creates a random graph and
 * computes all-pairs shortest paths.
 *
 * @author Dimitrios Michail
 */
public class DijkstraShortestPathPerformanceTest {
    private static final int PERF_BENCHMARK_VERTICES_COUNT = 250;
    private static final double PERF_BENCHMARK_EDGES_PROP = 0.3;
    private static final int WARMUP_REPEAT = 5;
    private static final int REPEAT = 10;
    private static final long SEED = 13l;

    private static abstract class BenchmarkBase {
        protected Random rng = new Random(SEED);
        protected GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator = null;
        protected Graph<Integer, DefaultWeightedEdge> graph;

        abstract ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph);

        public void setup() {
            if (generator == null) {
                // lazily construct generator
                generator = new GnpRandomGraphGenerator<>(
                        PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_PROP, rng, false);
            }

            this.graph = GraphTypeBuilder
                    .directed().weighted(true).edgeClass(DefaultWeightedEdge.class)
                    .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(true)
                    .allowingSelfLoops(true).buildGraph();

            generator.generateGraph(graph);

            for (DefaultWeightedEdge e : graph.edgeSet()) {
                graph.setEdgeWeight(e, rng.nextDouble());
            }
        }

        public void run() {
            ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp = createSolver(graph);
            for (Integer v : graph.vertexSet()) {
                for (Integer u : graph.vertexSet()) {
                    sp.getPath(v, u);
                }
            }
        }
    }

    public static class DijkstraBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new DijkstraShortestPath<>(graph);
        }

        @Override
        public String toString() {
            return "Dijkstra";
        }
    }

    public static class BFSShortestPathBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new BFSShortestPath<>(graph);
        }

        @Override
        public String toString() {
            return "BFSShortestPath";
        }
    }

    public static class ClosestFirstIteratorBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new ShortestPathAlgorithm<Integer, DefaultWeightedEdge>() {

                @Override
                public GraphPath<Integer, DefaultWeightedEdge> getPath(Integer source, Integer sink) {
                    /*
                     * We do not really return a result here, just reach the target.
                     */
                    ClosestFirstIterator<Integer, DefaultWeightedEdge> iter =
                            new ClosestFirstIterator<>(graph, source, Double.POSITIVE_INFINITY);
                    while (iter.hasNext()) {
                        Integer vertex = iter.next();
                        if (vertex.equals(sink)) {
                            return null;
                        }
                    }
                    return null;
                }

                @Override
                public double getPathWeight(Integer source, Integer sink) {
                    GraphPath<Integer, DefaultWeightedEdge> p = getPath(source, sink);
                    if (p == null) {
                        return Double.POSITIVE_INFINITY;
                    } else {
                        return p.getWeight();
                    }
                }

                public org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths<Integer,
                        DefaultWeightedEdge> getPaths(Integer source) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public String toString() {
            return "Dijkstra with ClosestFirstIterator";
        }
    }

    public static class BidirectionalDijkstraBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new BidirectionalDijkstraShortestPath<>(graph);

        }

        @Override
        public String toString() {
            return "Bidirectional Dijkstra";
        }
    }

    public static class AStarNoHeuristicBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new AStarShortestPath<>(graph, (u, t) -> 0d);

        }

        @Override
        public String toString() {
            return "A* no heuristic";
        }
    }

    public static class AStarALTBenchmark
            extends
            BenchmarkBase {
        private int totalLandmarks;

        AStarALTBenchmark(int totalLandmarks) {
            this.totalLandmarks = totalLandmarks;
        }

        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            Integer[] vertices = graph.vertexSet().toArray(new Integer[0]);
            Set<Integer> landmarks = new HashSet<>();
            while (landmarks.size() < totalLandmarks) {
                landmarks.add(vertices[rng.nextInt(graph.vertexSet().size())]);
            }
            return new AStarShortestPath<>(graph, new ALTAdmissibleHeuristic<>(graph, landmarks));
        }

        @Override
        public String toString() {
            return "A* with ALT heuristic (" + totalLandmarks + " random landmarks)";
        }
    }

    public static class BidirectionalAStarNoHeuristicBenchmark
            extends
            BenchmarkBase {
        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            return new BidirectionalAStarShortestPath<>(graph, (u, t) -> 0d);
        }

        @Override
        public String toString() {
            return "Bidirectional A* no heuristic";
        }
    }

    public static class BidirectionalAStarALTBenchmark
            extends
            BenchmarkBase {
        private int totalLandmarks;

        BidirectionalAStarALTBenchmark(int totalLandmarks) {
            this.totalLandmarks = totalLandmarks;
        }

        @Override
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> createSolver(
                Graph<Integer, DefaultWeightedEdge> graph) {
            Integer[] vertices = graph.vertexSet().toArray(new Integer[0]);
            Set<Integer> landmarks = new HashSet<>();
            while (landmarks.size() < totalLandmarks) {
                landmarks.add(vertices[rng.nextInt(graph.vertexSet().size())]);
            }
            AStarAdmissibleHeuristic<Integer> heuristic =
                    new ALTAdmissibleHeuristic<>(graph, landmarks);
            return new BidirectionalAStarShortestPath<>(graph, heuristic);
        }

        @Override
        public String toString() {
            return "Bidirectional A* with ALT heuristic (" + totalLandmarks + " random landmarks)";
        }
    }

    @Test
    public void testBenchmark() {
        System.out.println("All-Pairs Shortest Paths Benchmark");
        System.out.println("---------");
        System.out.println(
                "Using G(n,p) random graph with n = " + PERF_BENCHMARK_VERTICES_COUNT + ", p = "
                        + PERF_BENCHMARK_EDGES_PROP);
        System.out.println("Warmup phase " + WARMUP_REPEAT + " executions");
        System.out.println("Averaging results over " + REPEAT + " executions");

        List<Supplier<BenchmarkBase>> algFactory = new ArrayList<>();
        algFactory.add(() -> new ClosestFirstIteratorBenchmark());
        algFactory.add(() -> new DijkstraBenchmark());
        algFactory.add(() -> new AStarNoHeuristicBenchmark());
        algFactory.add(() -> new AStarALTBenchmark(1));
        algFactory.add(() -> new AStarALTBenchmark(5));
        algFactory.add(() -> new BidirectionalDijkstraBenchmark());
        algFactory.add(() -> new BFSShortestPathBenchmark());
        algFactory.add(() -> new BidirectionalAStarALTBenchmark(1));
        algFactory.add(() -> new BidirectionalAStarALTBenchmark(5));
        algFactory.add(() -> new BidirectionalAStarNoHeuristicBenchmark());

        for (Supplier<BenchmarkBase> alg : algFactory) {

            System.gc();
            StopWatch watch = new StopWatch();

            BenchmarkBase benchmark = alg.get();
            System.out.printf("%-50s :", benchmark.toString());

            for (int i = 0; i < WARMUP_REPEAT; i++) {
                System.out.print("-");
                benchmark.setup();
                benchmark.run();
            }
            double avgGraphCreate = 0d;
            double avgExecution = 0d;
            for (int i = 0; i < REPEAT; i++) {
                System.out.print("+");
                watch.start();
                benchmark.setup();
                avgGraphCreate += watch.getElapsed(TimeUnit.MILLISECONDS);
                watch.start();
                benchmark.run();
                avgExecution += watch.getElapsed(TimeUnit.MILLISECONDS);
            }
            avgGraphCreate /= REPEAT;
            avgExecution /= REPEAT;

            System.out.print(" -> ");
            System.out
                    .printf("setup %.3f (ms) | execution %.3f (ms)\n", avgGraphCreate, avgExecution);
        }

    }

}
