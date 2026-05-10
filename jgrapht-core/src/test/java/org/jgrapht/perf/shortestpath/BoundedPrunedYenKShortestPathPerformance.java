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
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * JMH benchmark comparing {@link YenKShortestPath} against
 * {@link BoundedPrunedYenKShortestPath} with both {@link DijkstraSpurEngine} and
 * {@link AStarSpurEngine}. Measures average time per {@code getPaths} call on randomly generated
 * weighted directed graphs of varying density and {@code k}.
 *
 * <p>
 * All variants are exact and return the same ordered sequence of path weights as
 * {@link YenKShortestPath}; this benchmark only measures wall-clock cost.
 *
 * @author Shai Eilat
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0, jvmArgs = "--illegal-access=permit")
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BoundedPrunedYenKShortestPathPerformance
{

    private static final Random RANDOM = new Random(19L);

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testYenKShortestPath(
        BoundedPrunedYenState state)
    {
        return computeResult(new YenKShortestPath<>(state.graph), state);
    }

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testBoundedPrunedYenWithDijkstra(
        BoundedPrunedYenState state)
    {
        return computeResult(
            new BoundedPrunedYenKShortestPath<>(state.graph, new DijkstraSpurEngine<>()), state);
    }

    @Benchmark
    public List<List<GraphPath<Integer, DefaultWeightedEdge>>> testBoundedPrunedYenWithAStar(
        BoundedPrunedYenState state)
    {
        return computeResult(
            new BoundedPrunedYenKShortestPath<>(state.graph, new AStarSpurEngine<>()), state);
    }

    private List<List<GraphPath<Integer, DefaultWeightedEdge>>> computeResult(
        org.jgrapht.alg.interfaces.KShortestPathAlgorithm<Integer, DefaultWeightedEdge> algorithm,
        BoundedPrunedYenState state)
    {
        List<List<GraphPath<Integer, DefaultWeightedEdge>>> result =
            new ArrayList<>(state.numberOfQueries);
        for (Pair<Integer, Integer> query : state.queries) {
            int source = query.getFirst();
            int target = query.getSecond();
            result.add(algorithm.getPaths(source, target, state.k));
        }
        return result;
    }

    @State(Scope.Benchmark)
    public static class BoundedPrunedYenState
    {
        @Param({ "100" })
        int n;
        @Param({ "0.1", "0.3" })
        double p;
        @Param({ "20", "100" })
        int k;
        @Param({ "10" })
        int numberOfQueries;

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator;
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
        List<Pair<Integer, Integer>> queries;

        @Setup(Level.Iteration)
        public void generateGraph()
        {
            generator = new GnpRandomGraphGenerator<>(n, p);
            graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            generator.generateGraph(graph);
            makeConnected(graph);
            addEdgeWeights(graph);
            queries = selectQueries();
        }

        private List<Pair<Integer, Integer>> selectQueries()
        {
            Set<Pair<Integer, Integer>> result =
                CollectionUtil.newHashSetWithExpectedSize(numberOfQueries);
            Object[] vertices = graph.vertexSet().toArray();
            while (result.size() < numberOfQueries) {
                int sourceIndex = (int) (Math.random() * vertices.length);
                int targetIndex = (int) (Math.random() * vertices.length);
                while (sourceIndex == targetIndex) {
                    targetIndex = (int) (Math.random() * vertices.length);
                }
                Integer source = (Integer) vertices[sourceIndex];
                Integer target = (Integer) vertices[targetIndex];
                result.add(Pair.of(source, target));
            }
            return new ArrayList<>(result);
        }

        private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph)
        {
            Object[] vertices = graph.vertexSet().toArray();
            for (int i = 0; i < vertices.length - 1; i++) {
                if (!graph.containsEdge((Integer) vertices[i], (Integer) vertices[i + 1])) {
                    graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
                }
            }
        }

        private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph)
        {
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                double weight = 1.0 + Math.abs(RANDOM.nextInt(1000));
                graph.setEdgeWeight(edge, weight);
            }
        }
    }
}
