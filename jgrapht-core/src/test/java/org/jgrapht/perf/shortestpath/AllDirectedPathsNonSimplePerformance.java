/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * JMH benchmark for {@link AllDirectedPaths#getAllPaths(Object, Object, boolean, Integer)} in
 * non-simple-paths mode. Exercises the inner loop of {@code generatePaths} where the
 * per-pop {@code pathVertices} {@code HashSet} is only read in simple-paths mode but was
 * unconditionally rebuilt on every iteration in earlier revisions.
 *
 * <p>
 * Cyclic grid graph with self-loops on every vertex. Source = corner (0,0),
 * target = adjacent vertex (0,1). Non-simple-paths mode with {@code maxPathLength} swept across
 * a few values to amortise JIT warmup and produce enough partial paths to exercise the inner
 * loop of {@code generatePaths}.
 *
 * @author Shai Eilat
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0, jvmArgs = "--illegal-access=permit")
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 8, time = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AllDirectedPathsNonSimplePerformance
{

    @Benchmark
    public List<GraphPath<Integer, DefaultEdge>> testGetAllPathsNonSimple(CyclicGridState state)
    {
        return state.algorithm.getAllPaths(state.source, state.target, false, state.maxPathLength);
    }

    @State(Scope.Benchmark)
    public static class CyclicGridState
    {
        @Param({ "5" })
        int gridSize;
        @Param({ "6", "8", "10" })
        int maxPathLength;

        DefaultDirectedGraph<Integer, DefaultEdge> graph;
        AllDirectedPaths<Integer, DefaultEdge> algorithm;
        Integer source;
        Integer target;

        @Setup(Level.Trial)
        public void buildGraph()
        {
            graph = new DefaultDirectedGraph<>(DefaultEdge.class);
            int n = gridSize;
            for (int i = 0; i < n * n; i++) {
                graph.addVertex(i);
            }
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    int v = row * n + col;
                    graph.addEdge(v, v);
                    if (col + 1 < n) {
                        graph.addEdge(v, v + 1);
                        graph.addEdge(v + 1, v);
                    }
                    if (row + 1 < n) {
                        graph.addEdge(v, v + n);
                        graph.addEdge(v + n, v);
                    }
                }
            }
            algorithm = new AllDirectedPaths<>(graph);
            source = 0;
            target = 1;
        }
    }
}
