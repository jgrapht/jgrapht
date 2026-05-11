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
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * JMH benchmark for {@link DijkstraManyToManyShortestPaths#getPaths(Object)}.
 *
 * <p>
 * Compares two variants in the same JVM:
 * <ul>
 *   <li>{@code testBaseClassLoop} faithfully reproduces the inherited
 *       {@code BaseManyToManyShortestPaths.getPaths(source)} default — for each vertex
 *       {@code v} in the graph, call {@code getPath(source, v)}, which dispatches
 *       through {@code getManyToManyPaths(singleton(source), singleton(v))} and runs a
 *       fresh Dijkstra from {@code source} on every iteration.</li>
 *   <li>{@code testOptimizedGetPaths} calls the
 *       {@link DijkstraManyToManyShortestPaths#getPaths(Object)} override that runs a
 *       single Dijkstra from {@code source} settling every reachable vertex.</li>
 * </ul>
 *
 * <p>
 * Both variants are exact and produce a {@code SingleSourcePaths} honouring the same
 * externally-visible contract; this benchmark only measures wall-clock cost.
 *
 * @author Shai Eilat
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0, jvmArgs = "--illegal-access=permit")
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DijkstraManyToManyShortestPathsGetPathsPerformance
{

    private static final long SEED = 19L;

    @Benchmark
    public List<SingleSourcePaths<Integer, DefaultWeightedEdge>> testBaseClassLoop(
        GetPathsState state)
    {
        List<SingleSourcePaths<Integer, DefaultWeightedEdge>> result =
            new ArrayList<>(state.sources.size());
        for (Integer source : state.sources) {
            Map<Integer, GraphPath<Integer, DefaultWeightedEdge>> paths = new HashMap<>();
            for (Integer v : state.graph.vertexSet()) {
                paths.put(v, state.algorithm.getPath(source, v));
            }
            result.add(new ListSingleSourcePathsImpl<>(state.graph, source, paths));
        }
        return result;
    }

    @Benchmark
    public List<SingleSourcePaths<Integer, DefaultWeightedEdge>> testOptimizedGetPaths(
        GetPathsState state)
    {
        List<SingleSourcePaths<Integer, DefaultWeightedEdge>> result =
            new ArrayList<>(state.sources.size());
        for (Integer source : state.sources) {
            result.add(state.algorithm.getPaths(source));
        }
        return result;
    }

    @State(Scope.Benchmark)
    public static class GetPathsState
    {
        @Param({ "50", "100", "200" })
        int n;
        @Param({ "0.1" })
        double p;
        @Param({ "5" })
        int numberOfSources;

        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> algorithm;
        List<Integer> sources;

        @Setup(Level.Iteration)
        public void generateGraph()
        {
            Random rng = new Random(SEED);
            graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            new GnpRandomGraphGenerator<Integer, DefaultWeightedEdge>(n, p, SEED).generateGraph(
                graph);
            makeConnected();
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                graph.setEdgeWeight(edge, 1.0 + rng.nextInt(1000));
            }
            algorithm = new DijkstraManyToManyShortestPaths<>(graph);
            sources = pickSources(rng);
        }

        private void makeConnected()
        {
            Object[] vertices = graph.vertexSet().toArray();
            for (int i = 0; i < vertices.length - 1; i++) {
                if (!graph.containsEdge((Integer) vertices[i], (Integer) vertices[i + 1])) {
                    graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
                }
            }
        }

        private List<Integer> pickSources(Random rng)
        {
            Object[] vertices = graph.vertexSet().toArray();
            Set<Integer> chosen = new LinkedHashSet<>();
            while (chosen.size() < numberOfSources) {
                chosen.add((Integer) vertices[rng.nextInt(vertices.length)]);
            }
            return new ArrayList<>(chosen);
        }
    }
}
