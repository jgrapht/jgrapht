/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
package org.jgrapht.perf.hamiltonian;

import org.jgrapht.*;
import org.jgrapht.alg.hamiltonian.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * JMH benchmark covering {@link BacktrackingHamiltonianPath} and
 * {@link HeldKarpHamiltonianPath} on representative graph families.
 *
 * <p>
 * Hamiltonian path search is NP-complete in general; the benchmarked cells are intentionally
 * small so that each measurement iteration completes well within JMH's per-iteration budget.
 * Two driver methods are provided:
 * <ul>
 * <li>{@link #testSmoke()} fires a minimal subset (one family, one size) to verify that the
 * benchmark wiring builds and runs at all. Use it before {@link #testBaseline()}.</li>
 * <li>{@link #testBaseline()} runs a broader baseline (path, cycle, complete, sparse Erdos-Renyi
 * graph families at {@code n in {8, 12}}). Total wall time on commodity hardware is in the low
 * minutes.</li>
 * </ul>
 * Both drivers force exactly one fork, short warmup/measurement budgets, and {@code -p}-style
 * runtime overrides via {@link OptionsBuilder#param(String, String...)} are honoured by JMH if
 * the caller needs to narrow the cell set further from the CLI.
 *
 * @author seilat
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class HamiltonianPathPerformanceTest
{

    public enum GraphFamily
    {
        PATH, CYCLE, COMPLETE, SPARSE
    }

    @Param({ "8", "12" })
    public int n;

    @Param({ "PATH", "CYCLE", "COMPLETE", "SPARSE" })
    public GraphFamily family;

    Graph<Integer, DefaultEdge> graph;

    @Setup(Level.Trial)
    public void buildGraph()
    {
        Random random = new Random(0xBEEFCAFE12345678L ^ ((long) n << 16) ^ family.ordinal());
        graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
        }
        switch (family) {
        case PATH:
            for (int i = 0; i < n - 1; i++) {
                graph.addEdge(i, i + 1);
            }
            break;
        case CYCLE:
            for (int i = 0; i < n; i++) {
                graph.addEdge(i, (i + 1) % n);
            }
            break;
        case COMPLETE:
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    graph.addEdge(i, j);
                }
            }
            break;
        case SPARSE:
            // p ~= 3/n keeps average degree around 3, where backtracking pruning helps most.
            double p = 3.0 / n;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < p) {
                        graph.addEdge(i, j);
                    }
                }
            }
            break;
        }
    }

    @Benchmark
    public GraphPath<Integer, DefaultEdge> backtracking()
    {
        return new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
    }

    @Benchmark
    public GraphPath<Integer, DefaultEdge> heldKarp()
    {
        return new HeldKarpHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
    }

    /**
     * Smoke driver: one family, one size, one warmup iteration, one measurement iteration.
     * Use this before {@link #testBaseline()} to confirm the benchmark wiring compiles and
     * fires correctly. Runtime is approximately a few seconds.
     */
    @Test
    public void testSmoke()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + HamiltonianPathPerformanceTest.class.getSimpleName() + ".*")
            .param("n", "8")
            .param("family", "PATH")
            .mode(Mode.AverageTime).timeUnit(TimeUnit.MILLISECONDS)
            .warmupIterations(1).warmupTime(TimeValue.seconds(1))
            .measurementIterations(1).measurementTime(TimeValue.seconds(1))
            .forks(1).shouldFailOnError(true).shouldDoGC(true)
            .jvmArgsAppend(
                "--add-exports", "org.jgrapht.core/org.jgrapht.perf.hamiltonian.jmh_generated=ALL-UNNAMED",
                "--add-opens", "org.jgrapht.core/org.jgrapht.perf.hamiltonian=ALL-UNNAMED")
            .build();
        new Runner(opt).run();
    }

    /**
     * Baseline driver: covers the four graph families at {@code n in {8, 12}}. Designed to be
     * bounded; each cell takes a few seconds to measure.
     */
    @Test
    public void testBaseline()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + HamiltonianPathPerformanceTest.class.getSimpleName() + ".*")
            .mode(Mode.AverageTime).timeUnit(TimeUnit.MILLISECONDS)
            .warmupIterations(1).warmupTime(TimeValue.seconds(1))
            .measurementIterations(2).measurementTime(TimeValue.seconds(1))
            .forks(1).shouldFailOnError(true).shouldDoGC(true)
            .jvmArgsAppend(
                "--add-exports", "org.jgrapht.core/org.jgrapht.perf.hamiltonian.jmh_generated=ALL-UNNAMED",
                "--add-opens", "org.jgrapht.core/org.jgrapht.perf.hamiltonian=ALL-UNNAMED")
            .build();
        new Runner(opt).run();
    }
}
