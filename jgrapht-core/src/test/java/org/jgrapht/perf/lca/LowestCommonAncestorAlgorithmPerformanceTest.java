/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.perf.lca;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.lca.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class LowestCommonAncestorAlgorithmPerformanceTest
{

    public static final int PERF_BENCHMARK_VERTICES_COUNT = 100_000;
    public static final int PERF_BENCHMARK_QUERIES_COUNT = 300_000;

    @State(Scope.Benchmark)
    private static abstract class RandomTreeBenchmarkBase
    {

        public static final long SEED = 111222111;

        private LowestCommonAncestorAlgorithm<Integer> solver;
        private List<Pair<Integer, Integer>> queries;

        abstract LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Integer root);

        @Setup
        public void setup()
        {
            Random random = new Random(SEED);

            Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(1, PERF_BENCHMARK_VERTICES_COUNT, random);

            generator.generateGraph(tree, null);

            solver = createSolver(tree, tree.vertexSet().iterator().next());

            queries = LCATreeTestBase.generateQueries(
                PERF_BENCHMARK_QUERIES_COUNT, new ArrayList<>(tree.vertexSet()), random);
        }

        @Benchmark
        public void run()
        {
            solver.getBatchLCA(queries);
        }
    }

    @State(Scope.Benchmark)
    private static abstract class RandomForestBenchmarkBase
    {

        public static final int NUMBER_TREES = 10 * PERF_BENCHMARK_VERTICES_COUNT / 100; // 10%
        public static final long SEED = 111222111;

        private LowestCommonAncestorAlgorithm<Integer> solver;
        private List<Pair<Integer, Integer>> queries;

        abstract LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Set<Integer> roots);

        @Setup
        public void setup()
        {
            Random random = new Random(SEED);

            Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(
                    NUMBER_TREES, PERF_BENCHMARK_VERTICES_COUNT, random);

            generator.generateGraph(forest, null);

            Set<Integer> roots = new ConnectivityInspector<>(forest)
                .connectedSets().stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

            assert roots.size() == NUMBER_TREES;

            solver = createSolver(forest, roots);

            queries = LCATreeTestBase.generateQueries(
                PERF_BENCHMARK_QUERIES_COUNT, new ArrayList<>(forest.vertexSet()), random);
        }

        @Benchmark
        public void run()
        {
            solver.getBatchLCA(queries);
        }
    }

    public static class BinaryLiftingLCARandomTreeBenchmark
        extends
        RandomTreeBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Integer root)
        {
            return new BinaryLiftingLCAFinder<>(tree, root);
        }
    }

    public static class EulerTourRMQLCARandomTreeBenchmark
        extends
        RandomTreeBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Integer root)
        {
            return new EulerTourRMQLCAFinder<>(tree, root);
        }
    }

    public static class TarjanLCARandomTreeBenchmark
        extends
        RandomTreeBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Integer root)
        {
            return new TarjanLCAFinder<>(tree, root);
        }
    }

    public static class HeavyPathRandomTreeBenchmark
        extends
        RandomTreeBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Integer root)
        {
            return new HeavyPathLCAFinder<>(tree, root);
        }
    }

    public static class BinaryLiftingLCARandomForestBenchmark
        extends
        RandomForestBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Set<Integer> roots)
        {
            return new BinaryLiftingLCAFinder<>(tree, roots);
        }
    }

    public static class EulerTourRMQLCARandomForestBenchmark
        extends
        RandomForestBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Set<Integer> roots)
        {
            return new EulerTourRMQLCAFinder<>(tree, roots);
        }
    }

    public static class TarjanLCARandomForestBenchmark
        extends
        RandomForestBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Set<Integer> roots)
        {
            return new TarjanLCAFinder<>(tree, roots);
        }
    }

    public static class HeavyPathRandomForestBenchmark
        extends
        RandomForestBenchmarkBase
    {

        @Override
        LowestCommonAncestorAlgorithm<Integer> createSolver(
            Graph<Integer, DefaultEdge> tree, Set<Integer> roots)
        {
            return new HeavyPathLCAFinder<>(tree, roots);
        }
    }

    @Test
    public void testRandomTreeBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + BinaryLiftingLCARandomTreeBenchmark.class.getSimpleName() + ".*")
            .include(".*" + EulerTourRMQLCARandomTreeBenchmark.class.getSimpleName() + ".*")
            .include(".*" + TarjanLCARandomTreeBenchmark.class.getSimpleName() + ".*")
            .include(".*" + HeavyPathRandomTreeBenchmark.class.getSimpleName() + ".*")

            .mode(Mode.AverageTime).timeUnit(TimeUnit.NANOSECONDS).warmupTime(TimeValue.seconds(1))
            .warmupIterations(3).measurementTime(TimeValue.seconds(1)).measurementIterations(5)
            .forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }

    @Test
    public void testRandomForestBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + BinaryLiftingLCARandomForestBenchmark.class.getSimpleName() + ".*")
            .include(".*" + EulerTourRMQLCARandomForestBenchmark.class.getSimpleName() + ".*")
            .include(".*" + TarjanLCARandomForestBenchmark.class.getSimpleName() + ".*")
            .include(".*" + HeavyPathRandomForestBenchmark.class.getSimpleName() + ".*")

            .mode(Mode.AverageTime).timeUnit(TimeUnit.NANOSECONDS).warmupTime(TimeValue.seconds(1))
            .warmupIterations(3).measurementTime(TimeValue.seconds(1)).measurementIterations(5)
            .forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }
}
