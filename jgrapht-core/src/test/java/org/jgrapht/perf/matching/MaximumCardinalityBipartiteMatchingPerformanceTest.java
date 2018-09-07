/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.perf.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.matching.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A small benchmark comparing matching algorithms for bipartite graphs.
 * 
 * @author Joris Kinable
 */
public class MaximumCardinalityBipartiteMatchingPerformanceTest
{

    public static final int PERF_BENCHMARK_VERTICES_COUNT = 2000;
    public static final double PERF_BENCHMARK_EDGES_PROP = 0.7;

    @State(Scope.Benchmark)
    private static abstract class RandomGraphBenchmarkBase
    {
        public static final long SEED = 13l;

        private GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator = null;
        private Graph<Integer, DefaultEdge> graph;
        private Set<Integer> firstPartition;
        private Set<Integer> secondPartition;

        abstract MatchingAlgorithm<Integer, DefaultEdge> createSolver(
            Graph<Integer, DefaultEdge> graph, Set<Integer> firstPartition,
            Set<Integer> secondPartition);

        @Setup(Level.Iteration)
        public void setup()
        {
            if (generator == null) {
                // lazily construct generator
                generator = new GnpRandomBipartiteGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_VERTICES_COUNT / 2,
                    PERF_BENCHMARK_EDGES_PROP, SEED);
            }

            graph = new Pseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            generator.generateGraph(graph);
            firstPartition = generator.getFirstPartition();
            secondPartition = generator.getSecondPartition();
        }

        @Benchmark
        public void run()
        {
            long time = System.currentTimeMillis();
            MatchingAlgorithm.Matching<Integer, DefaultEdge> m =
                createSolver(graph, firstPartition, secondPartition).getMatching();
            time = System.currentTimeMillis() - time;
            System.out.println(
                "time: " + time + " obj :" + m.getEdges().size() + " vertices: "
                    + graph.vertexSet().size() + " edges: " + graph.edgeSet().size());
        }
    }

    public static class EdmondsMaxCardinalityBipartiteMatchingBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        MatchingAlgorithm<Integer, DefaultEdge> createSolver(
            Graph<Integer, DefaultEdge> graph, Set<Integer> firstPartition,
            Set<Integer> secondPartition)
        {
            return new EdmondsMaximumCardinalityMatching<>(graph);
        }
    }

    public static class HopcroftKarpMaximumCardinalityBipartiteMatchingBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        MatchingAlgorithm<Integer, DefaultEdge> createSolver(
            Graph<Integer, DefaultEdge> graph, Set<Integer> firstPartition,
            Set<Integer> secondPartition)
        {
            return new HopcroftKarpMaximumCardinalityBipartiteMatching<>(
                graph, firstPartition, secondPartition);
        }
    }

    @Test
    public void testRandomGraphBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(
                ".*" + EdmondsMaxCardinalityBipartiteMatchingBenchmark.class.getSimpleName() + ".*")
            .include(
                ".*" + HopcroftKarpMaximumCardinalityBipartiteMatchingBenchmark.class
                    .getSimpleName() + ".*")
            .mode(Mode.SingleShotTime).timeUnit(TimeUnit.MILLISECONDS).warmupIterations(5)
            .measurementIterations(10).forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }
}
