/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.perf.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.spanning.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * A small benchmark comparing spanning tree algorithms on random graphs.
 * 
 * @author Dimitrios Michail
 * @author Alexandru Valeanu
 */
public class MinimumSpanningTreePerformanceTest
{
    private static final int PERF_BENCHMARK_VERTICES_COUNT_DENSE = 1500;
    private static final double PERF_BENCHMARK_EDGES_PROP_DENSE = 0.65;

    private static final int PERF_BENCHMARK_VERTICES_COUNT_SPARSE = 100_000;
    private static final int PERF_BENCHMARK_EDGES_COUNT_SPARSE = 500_000;

    private static final int WARMUP_REPEAT = 5;
    private static final int REPEAT = 10;
    private static final long SEED = 13L;

    private static abstract class BenchmarkBase
    {
        protected Random rng = new Random(SEED);
        protected GraphGenerator<Integer, DefaultWeightedEdge, Integer> generatorSparseGraphs =
            null;
        protected GraphGenerator<Integer, DefaultWeightedEdge, Integer> generatorDenseGraphs = null;
        protected Graph<Integer, DefaultWeightedEdge> sparseGraph, denseGraph;

        abstract SpanningTreeAlgorithm<DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> graph);

        public void setupDense()
        {
            if (generatorDenseGraphs == null) {
                // lazily construct generators
                generatorDenseGraphs = new GnpRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT_DENSE, PERF_BENCHMARK_EDGES_PROP_DENSE, rng,
                    false);
            }

            DirectedWeightedPseudograph<Integer,
                DefaultWeightedEdge> weightedDenseGraph = new DirectedWeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

            this.denseGraph = weightedDenseGraph;

            generatorDenseGraphs.generateGraph(weightedDenseGraph);

            for (DefaultWeightedEdge e : weightedDenseGraph.edgeSet()) {
                weightedDenseGraph.setEdgeWeight(e, rng.nextDouble());
            }
        }

        public void setupSparse()
        {
            if (generatorSparseGraphs == null) {
                // lazily construct generator
                generatorSparseGraphs = new GnmRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT_SPARSE, PERF_BENCHMARK_EDGES_COUNT_SPARSE);
            }

            DirectedWeightedPseudograph<Integer,
                DefaultWeightedEdge> weightedSparseGraph = new DirectedWeightedPseudograph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

            this.sparseGraph = weightedSparseGraph;

            generatorSparseGraphs.generateGraph(sparseGraph);

            for (DefaultWeightedEdge e : weightedSparseGraph.edgeSet()) {
                weightedSparseGraph.setEdgeWeight(e, rng.nextDouble());
            }
        }

        public void runDense()
        {
            SpanningTreeAlgorithm<DefaultWeightedEdge> algo = createSolver(denseGraph);
            algo.getSpanningTree();
        }

        public void runSparse()
        {
            SpanningTreeAlgorithm<DefaultWeightedEdge> algo = createSolver(sparseGraph);
            algo.getSpanningTree();
        }
    }

    public static class PrimBenchmark
        extends
        BenchmarkBase
    {
        @Override
        SpanningTreeAlgorithm<DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> graph)
        {
            return new PrimMinimumSpanningTree<>(graph);
        }

        @Override
        public String toString()
        {
            return "Prim";
        }
    }

    public static class KruskalBenchmark
        extends
        BenchmarkBase
    {
        @Override
        SpanningTreeAlgorithm<DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> graph)
        {
            return new KruskalMinimumSpanningTree<>(graph);
        }

        @Override
        public String toString()
        {
            return "Kruskal";
        }
    }

    public static class BoruvkaBenchmark
        extends
        BenchmarkBase
    {
        @Override
        SpanningTreeAlgorithm<DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> graph)
        {
            return new BoruvkaMinimumSpanningTree<>(graph);
        }

        @Override
        public String toString()
        {
            return "Boruvka";
        }
    }

    @Test
    public void testBenchmarkDenseGraphs()
    {
        System.out.println("Minimum Spanning Tree Benchmark using dense graphs");
        System.out.println("-------------------------------");
        System.out
            .println(
                "Using G(n,p) random graph with n = " + PERF_BENCHMARK_VERTICES_COUNT_DENSE
                    + ", p = " + PERF_BENCHMARK_EDGES_PROP_DENSE);
        System.out.println("Warmup phase " + WARMUP_REPEAT + " executions");
        System.out.println("Averaging results over " + REPEAT + " executions");

        List<Supplier<BenchmarkBase>> algFactory = new ArrayList<>();
        algFactory.add(PrimBenchmark::new);
        algFactory.add(KruskalBenchmark::new);
        algFactory.add(BoruvkaBenchmark::new);

        for (Supplier<BenchmarkBase> alg : algFactory) {

            System.gc();
            StopWatch watch = new StopWatch();

            BenchmarkBase benchmark = alg.get();
            System.out.printf("%-30s :", benchmark.toString());

            for (int i = 0; i < WARMUP_REPEAT; i++) {
                System.out.print("-");
                benchmark.setupDense();
                benchmark.runDense();
            }
            double avgGraphCreate = 0d;
            double avgExecution = 0d;
            for (int i = 0; i < REPEAT; i++) {
                System.out.print("+");
                watch.start();
                benchmark.setupDense();
                avgGraphCreate += watch.getElapsed(TimeUnit.MILLISECONDS);
                watch.start();
                benchmark.runDense();
                avgExecution += watch.getElapsed(TimeUnit.MILLISECONDS);
            }
            avgGraphCreate /= REPEAT;
            avgExecution /= REPEAT;

            System.out.print(" -> ");
            System.out
                .printf("setup %.3f (ms) | execution %.3f (ms)\n", avgGraphCreate, avgExecution);
        }
    }

    @Test
    public void testBenchmarkSparseGraphs()
    {
        System.out.println("Minimum Spanning Tree Benchmark using sparse graphs");
        System.out.println("-------------------------------");
        System.out
            .println(
                "Using G(n,M) random graph with n = " + PERF_BENCHMARK_VERTICES_COUNT_SPARSE
                    + ", M = " + PERF_BENCHMARK_EDGES_COUNT_SPARSE);
        System.out.println("Warmup phase " + WARMUP_REPEAT + " executions");
        System.out.println("Averaging results over " + REPEAT + " executions");

        List<Supplier<BenchmarkBase>> algFactory = new ArrayList<>();
        algFactory.add(PrimBenchmark::new);
        algFactory.add(KruskalBenchmark::new);
        algFactory.add(BoruvkaBenchmark::new);

        for (Supplier<BenchmarkBase> alg : algFactory) {

            System.gc();
            StopWatch watch = new StopWatch();

            BenchmarkBase benchmark = alg.get();
            System.out.printf("%-30s :", benchmark.toString());

            for (int i = 0; i < WARMUP_REPEAT; i++) {
                System.out.print("-");
                benchmark.setupSparse();
                benchmark.runSparse();
            }
            double avgGraphCreate = 0d;
            double avgExecution = 0d;
            for (int i = 0; i < REPEAT; i++) {
                System.out.print("+");
                watch.start();
                benchmark.setupSparse();
                avgGraphCreate += watch.getElapsed(TimeUnit.MILLISECONDS);
                watch.start();
                benchmark.runSparse();
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
