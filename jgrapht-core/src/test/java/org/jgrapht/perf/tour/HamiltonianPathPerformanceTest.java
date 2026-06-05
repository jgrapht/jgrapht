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
package org.jgrapht.perf.tour;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.tour.*;
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
 * <li>{@link #testBaseline()} runs a broader baseline covering all configured graph families
 * (path, cycle, complete, sparse Erdos-Renyi, three-leaf star, modular bridge-joined
 * triangles, and a directed acyclic chain with shortcuts) at {@code n in {8, 12, 16}}. Total
 * wall time on commodity hardware is in the low minutes.</li>
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
        PATH, CYCLE, COMPLETE, SPARSE, STAR_NEG, MODULAR_BRIDGES, DAG_POS
    }

    @Param({ "8", "12", "16" })
    public int n;

    @Param({ "PATH", "CYCLE", "COMPLETE", "SPARSE", "STAR_NEG", "MODULAR_BRIDGES", "DAG_POS" })
    public GraphFamily family;

    Graph<Integer, DefaultEdge> graph;

    @Setup(Level.Trial)
    public void buildGraph()
    {
        graph = GraphBuilders.build(family, n);
    }

    @Benchmark
    public HamiltonianPathSearchResult<Integer, DefaultEdge> backtracking()
    {
        return new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
    }

    @Benchmark
    public HamiltonianPathSearchResult<Integer, DefaultEdge> heldKarp()
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
                "--add-exports", "org.jgrapht.core/org.jgrapht.perf.tour.jmh_generated=ALL-UNNAMED",
                "--add-opens", "org.jgrapht.core/org.jgrapht.perf.tour=ALL-UNNAMED")
            .build();
        new Runner(opt).run();
    }

    /**
     * Baseline driver: covers all benchmark graph families at {@code n in {8, 12, 16}}.
     * Designed to be bounded; each cell takes a few seconds to measure.
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
                "--add-exports", "org.jgrapht.core/org.jgrapht.perf.tour.jmh_generated=ALL-UNNAMED",
                "--add-opens", "org.jgrapht.core/org.jgrapht.perf.tour=ALL-UNNAMED")
            .build();
        new Runner(opt).run();
    }
}
