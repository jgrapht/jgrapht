/*
 * (C) Copyright 2015-2021, by Alexey Kudinkin and Contributors.
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

import java.util.*;
import java.util.concurrent.*;

public class MaximumFlowAlgorithmPerformanceTest
{

    public static final int NUMBER_OF_GRAPHS = 20;
    public static final int PERF_BENCHMARK_VERTICES_COUNT = 1000;
    public static final int PERF_BENCHMARK_EDGES_COUNT = 100000;

    @State(Scope.Benchmark)
    private static abstract class RandomGraphBenchmarkBase
    {

        public static final long SEED = 1446523573696201013L;

        private List<Graph<Integer, DefaultWeightedEdge>> graphs;

        abstract MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network);

        @Setup
        public void setup()
        {
            graphs = new ArrayList<>();

            GraphGenerator<Integer, DefaultWeightedEdge, Integer> rgg =
                new GnmRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_COUNT, SEED);

            for (int i = 0; i < NUMBER_OF_GRAPHS; i++) {
                SimpleDirectedWeightedGraph<Integer,
                    DefaultWeightedEdge> network = new SimpleDirectedWeightedGraph<>(
                        SupplierUtil.createIntegerSupplier(0),
                        SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

                rgg.generateGraph(network);
                graphs.add(network);
            }
        }

        @Benchmark
        public void run()
        {
            for (Graph<Integer, DefaultWeightedEdge> g : graphs) {
                createSolver(g).getMaximumFlow(0, g.vertexSet().size() - 1);
            }
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
