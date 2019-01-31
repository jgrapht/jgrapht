/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.perf.clique;

import org.jgrapht.*;
import org.jgrapht.alg.clique.*;
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
 * A small benchmark comparing maximal clique enumeration algorithms.
 * 
 * @author Dimitrios Michail
 */
public class MaximalCliqueEnumerationPerformanceTest
{

    public static final int PERF_BENCHMARK_VERTICES_COUNT = 75;
    public static final double PERF_BENCHMARK_EDGES_PROP = 0.8;

    @State(Scope.Benchmark)
    private static abstract class RandomGraphBenchmarkBase
    {
        public static final long SEED = 13l;

        private GraphGenerator<Integer, DefaultEdge, Integer> generator = null;
        private Graph<Integer, DefaultEdge> graph;

        abstract Iterable<Set<Integer>> createSolver(Graph<Integer, DefaultEdge> graph);

        @Setup(Level.Iteration)
        public void setup()
        {
            if (generator == null) {
                // lazily construct generator
                generator = new GnpRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_PROP, SEED, false);
            }

            graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            generator.generateGraph(graph);
        }

        @Benchmark
        public void run()
        {
            Iterator<Set<Integer>> it = createSolver(graph).iterator();
            while (it.hasNext()) {
                it.next();
            }
        }
    }

    public static class BronKerboschRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        Iterable<Set<Integer>> createSolver(Graph<Integer, DefaultEdge> graph)
        {
            return new BronKerboschCliqueFinder<>(graph);
        }
    }

    public static class PivotBronKerboschRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        Iterable<Set<Integer>> createSolver(Graph<Integer, DefaultEdge> graph)
        {
            return new PivotBronKerboschCliqueFinder<>(graph);
        }
    }

    public static class DegeneracyBronKerboschRandomGraphBenchmark
        extends
        RandomGraphBenchmarkBase
    {
        @Override
        Iterable<Set<Integer>> createSolver(Graph<Integer, DefaultEdge> graph)
        {
            return new DegeneracyBronKerboschCliqueFinder<>(graph);
        }
    }

    @Test
    public void testMaximalCliqueRandomGraphBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + BronKerboschRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + PivotBronKerboschRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + DegeneracyBronKerboschRandomGraphBenchmark.class.getSimpleName() + ".*")
            .mode(Mode.SingleShotTime).timeUnit(TimeUnit.MILLISECONDS).warmupIterations(5)
            .measurementIterations(10).forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }
}
