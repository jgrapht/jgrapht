/*
 * (C) Copyright 2018-2018, by Semen Chudakov and Contributors.
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
package org.jgrapht.perf.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DeltaSteppingShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.util.SupplierUtil;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * A benchmark comparing {@link DeltaSteppingShortestPath} to {@link org.jgrapht.alg.shortestpath.DijkstraShortestPath}
 * and {@link org.jgrapht.alg.shortestpath.BellmanFordShortestPath}.
 * The benchmark test the algorithms on dense and sparse random graphs.
 *
 * @author Semen Chudakov
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 8, time = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DeltaSteppingShortestPathPerformance {

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDeltaSteppingRandom(RandomGraphState data) {
        return new DeltaSteppingShortestPath<>(data.graph, 1.0 / data.edgeDegree).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDijkstraRandom(RandomGraphState data) {
        return new DijkstraShortestPath<>(data.graph).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testBellmanFordRandom(RandomGraphState data) {
        return new BellmanFordShortestPath<>(data.graph).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDeltaSteppingDense(DenseGraphState data) {
        return new DeltaSteppingShortestPath<>(data.graph, 1.0 / data.graphSize).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDijkstraDense(DenseGraphState data) {
        return new DijkstraShortestPath<>(data.graph).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testBellmanFordDense(DenseGraphState data) {
        return new BellmanFordShortestPath<>(data.graph).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDeltaSteppingSparse(SparseGraphState data) {
        return new DeltaSteppingShortestPath<>(data.graph, 1.0 / data.edgeDegree).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testDijkstraSparse(SparseGraphState data) {
        return new DijkstraShortestPath<>(data.graph).getPaths(0);
    }

    @Benchmark
    public ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> testBellmanFordSparse(SparseGraphState data) {
        return new BellmanFordShortestPath<>(data.graph).getPaths(0);
    }

    @State(Scope.Benchmark)
    public static class RandomGraphState {
        @Param({"5000"})
        int numOfVertices;
        @Param({"100"})
        int edgeDegree;
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generateGraph() {
            graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

            GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
                    new GnmRandomGraphGenerator<>(numOfVertices, numOfVertices * edgeDegree - numOfVertices + 1);
            generator.generateGraph(graph);
            makeConnected(graph);
            addEdgeWeights(graph);
        }

        private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph) {
            Object[] vertices = graph.vertexSet().toArray();
            for (int i = 0; i < vertices.length - 1; i++) {
                graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
            }
        }

        private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph) {
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                graph.setEdgeWeight(edge, Math.random());
            }
        }
    }

    @State(Scope.Benchmark)
    public static class DenseGraphState {
        @Param({"1000"})
        int graphSize;
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generateGraph() {
            graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            CompleteGraphGenerator<Integer, DefaultWeightedEdge> generator = new CompleteGraphGenerator<>(graphSize);

            generator.generateGraph(graph);

            graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, Math.random()));
        }
    }

    @State(Scope.Benchmark)
    public static class SparseGraphState {
        @Param({"10000"})
        int graphSize;
        @Param({"50"})
        int edgeDegree;
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generate() {
            graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

            for (int i = 0; i < graphSize; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < graphSize; i++) {
                for (int j = 0; j < edgeDegree; j++) {
                    Graphs.addEdge(graph, i, (i + j) % graphSize, Math.random());
                }
            }
        }
    }
}