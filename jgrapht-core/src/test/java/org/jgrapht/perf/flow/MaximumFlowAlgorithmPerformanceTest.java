/*
 * (C) Copyright 2015-2016, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.perf.flow;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

public class MaximumFlowAlgorithmPerformanceTest extends TestCase {

    public static final int PERF_BENCHMARK_VERTICES_COUNT   = 1000;
    public static final int PERF_BENCHMARK_EDGES_COUNT      = 100000;

    @State(Scope.Benchmark)
    private static abstract class RandomGraphBenchmarkBase {

        public static final long SEED = 1446523573696201013l;

        private MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver;

        private Integer source;
        private Integer sink;

        abstract MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(DirectedGraph<Integer, DefaultWeightedEdge> network);

        @Setup
        public void setup() {
            RandomGraphGenerator<Integer, DefaultWeightedEdge> rgg
                = new RandomGraphGenerator<>(PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_COUNT, SEED);

            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> network
                = new SimpleDirectedWeightedGraph<>((sourceVertex, targetVertex) -> {
                return new DefaultWeightedEdge();
            });

            rgg.generateGraph(
                network,
                new VertexFactory<Integer>() {
                    int i;
                    @Override
                    public Integer createVertex() {
                        return ++i;
                    }
                },
                null
            );

            solver = createSolver(network);

            Object[] vs = network.vertexSet().toArray();

            source  = (Integer) vs[0];
            sink    = (Integer) vs[vs.length - 1];
        }

        @Benchmark
        public void run() {
            solver.getMaximumFlow(source, sink);
        }
    }

    public static class EdmondsKarpMaximumFlowRandomGraphBenchmark extends RandomGraphBenchmarkBase {
        @Override
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(DirectedGraph<Integer, DefaultWeightedEdge> network) {
            return new EdmondsKarpMFImpl<>(network);
        }
    }

    public static class PushRelabelMaximumFlowRandomGraphBenchmark extends RandomGraphBenchmarkBase {
        @Override
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(DirectedGraph<Integer, DefaultWeightedEdge> network) {
            return new PushRelabelMFImpl<>(network);
        }
    }

    public void testRandomGraphBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + EdmondsKarpMaximumFlowRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + PushRelabelMaximumFlowRandomGraphBenchmark.class.getSimpleName() + ".*")

            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.NANOSECONDS)
            .warmupTime(TimeValue.seconds(1))
            .warmupIterations(3)
            .measurementTime(TimeValue.seconds(1))
            .measurementIterations(5)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }
}
