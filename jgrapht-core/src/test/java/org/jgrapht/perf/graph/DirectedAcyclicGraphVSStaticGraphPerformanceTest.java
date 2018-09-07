/*
 * (C) Copyright 2008-2018, by Peter Giles and Contributors.
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
package org.jgrapht.perf.graph;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.*;
import org.jgrapht.graph.DirectedAcyclicGraphTest.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.*;

/**
 * A somewhat frivolous test of the performance difference between doing a full cycle detection
 * (non-dynamic algorithm) for each edge added versus the dynamic algorithm used by
 * DirectedAcyclicGraph.
 * 
 * @author Peter Giles
 * @author Dimitrios Michail
 */
public class DirectedAcyclicGraphVSStaticGraphPerformanceTest
{
    @State(Scope.Benchmark)
    public static class DynamicCycleDetectorRandomGraphBenchmark
    {
        @Setup(Level.Iteration)
        public void setup()
        {
        }

        @Benchmark
        public void run()
        {
            int trialsPerConfiguration = 10;
            int maxVertices = 1024;
            int maxConnectednessFactor = 4;

            for (int numVertices = 1024; numVertices <= maxVertices; numVertices *= 2) {
                for (int connectednessFactor = 1; (connectednessFactor <= maxConnectednessFactor)
                    && (connectednessFactor < (numVertices - 1)); connectednessFactor *= 2)
                {
                    for (int seed = 0; seed < trialsPerConfiguration; seed++) { // test with random
                                                                                // graph
                                                                                // configurations
                        Graph<Long,
                            DefaultEdge> sourceGraph = new SimpleDirectedGraph<>(
                                SupplierUtil.createLongSupplier(),
                                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
                        RepeatableRandomGraphGenerator<Long, DefaultEdge> gen =
                            new RepeatableRandomGraphGenerator<>(
                                numVertices, numVertices * connectednessFactor, seed);
                        gen.generateGraph(sourceGraph);

                        DirectedAcyclicGraph<Long, DefaultEdge> dag =
                            new DirectedAcyclicGraph<>(DefaultEdge.class);

                        for (Long vertex : sourceGraph.vertexSet()) {
                            dag.addVertex(vertex);
                        }

                        for (DefaultEdge edge : sourceGraph.edgeSet()) {
                            Long edgeSource = sourceGraph.getEdgeSource(edge);
                            Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                            try {
                                dag.addEdge(edgeSource, edgeTarget);
                            } catch (IllegalArgumentException doNothing) {
                            }
                        }
                    }
                }
            }
        }
    }

    @State(Scope.Benchmark)
    public static class StaticGraphWithCycleDetectorRandomGraphBenchmark
    {
        @Setup(Level.Iteration)
        public void setup()
        {
        }

        @Benchmark
        public void run()
        {
            int trialsPerConfiguration = 10;
            int maxVertices = 1024;
            int maxConnectednessFactor = 4;

            for (int numVertices = 1024; numVertices <= maxVertices; numVertices *= 2) {
                for (int connectednessFactor = 1; (connectednessFactor <= maxConnectednessFactor)
                    && (connectednessFactor < (numVertices - 1)); connectednessFactor *= 2)
                {
                    for (int seed = 0; seed < trialsPerConfiguration; seed++) { // test with random
                                                                                // graph
                                                                                // configurations
                        Graph<Long,
                            DefaultEdge> sourceGraph = new SimpleDirectedGraph<>(
                                SupplierUtil.createLongSupplier(),
                                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
                        RepeatableRandomGraphGenerator<Long, DefaultEdge> gen =
                            new RepeatableRandomGraphGenerator<>(
                                numVertices, numVertices * connectednessFactor, seed);
                        gen.generateGraph(sourceGraph);

                        SimpleDirectedGraph<Long, DefaultEdge> compareGraph =
                            new SimpleDirectedGraph<>(DefaultEdge.class);

                        for (Long vertex : sourceGraph.vertexSet()) {
                            compareGraph.addVertex(vertex);
                        }

                        for (DefaultEdge edge : sourceGraph.edgeSet()) {
                            Long edgeSource = sourceGraph.getEdgeSource(edge);
                            Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                            DefaultEdge compareEdge = compareGraph.addEdge(edgeSource, edgeTarget);
                            CycleDetector<Long, DefaultEdge> cycleDetector =
                                new CycleDetector<>(compareGraph);

                            boolean cycleDetected = cycleDetector.detectCycles();

                            if (cycleDetected) {
                                // remove the edge from the compareGraph
                                compareGraph.removeEdge(compareEdge);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testDirectedAcyclicGraphVSStaticGraphRandomGraphBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + DynamicCycleDetectorRandomGraphBenchmark.class.getSimpleName() + ".*")
            .include(
                ".*" + StaticGraphWithCycleDetectorRandomGraphBenchmark.class.getSimpleName()
                    + ".*")
            .mode(Mode.SingleShotTime).timeUnit(TimeUnit.MILLISECONDS).warmupIterations(5)
            .measurementIterations(10).forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }

}
