/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.*;

public class MaximumFlowAlgorithmPerformanceTest
{

    public static final int PERF_BENCHMARK_VERTICES_COUNT = 1000;
    public static final int PERF_BENCHMARK_EDGES_COUNT = 100000;

    @State(Scope.Benchmark)
    private static abstract class RandomGraphBenchmarkBase
    {

        public static final long SEED = 1446523573696201013l;

        private MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver;

        private Integer source;
        private Integer sink;

        abstract MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network);

        @Setup
        public void setup()
        {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> rgg =
                new GnmRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_COUNT, SEED);

            SimpleDirectedWeightedGraph<Integer,
                DefaultWeightedEdge> network = new SimpleDirectedWeightedGraph<>(
                    SupplierUtil.createIntegerSupplier(1),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

            rgg.generateGraph(network);

            solver = createSolver(network);

            Object[] vs = network.vertexSet().toArray();

            source = (Integer) vs[0];
            sink = (Integer) vs[vs.length - 1];
        }

        @Benchmark
        public void run()
        {
            solver.getMaximumFlow(source, sink);
        }
    }

    public static class EdmondsKarpMaximumFlowRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network)
        {
            return new EdmondsKarpMFImpl<>(network);
        }
    }

    public static class PushRelabelMaximumFlowRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network)
        {
            return new PushRelabelMFImpl<>(network);
        }
    }

    public static class DinicMaximumFlowRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {

        @Override
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network)
        {
            return new DinicMFImpl<>(network);
        }
    }

    @Test
    public void testRandomGraphBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + EdmondsKarpMaximumFlowRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + PushRelabelMaximumFlowRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + DinicMaximumFlowRandomGraphBenchmark.class.getSimpleName() + ".*")

            .mode(Mode.AverageTime).timeUnit(TimeUnit.NANOSECONDS).warmupTime(TimeValue.seconds(1))
            .warmupIterations(3).measurementTime(TimeValue.seconds(1)).measurementIterations(5)
            .forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }
}
