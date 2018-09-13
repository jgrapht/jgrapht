/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.perf.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.BlossomVOptions;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Fork(value = 5, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 10, time = 8)
public class KolmogorovMinimumWeightPerfectMatchingPerformanceTest {

    @Benchmark
    public MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> testBlossomV(Data data) {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching = new KolmogorovMinimumWeightPerfectMatching<>(data.graph, data.options[data.optionNum]);
        return matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data {

        public BlossomVOptions[] options = BlossomVOptions.ALL_OPTIONS;
        @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"})
        public int optionNum;
        @Param({"300", "500"})
        public int graphSize;
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generate() {
            this.graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            CompleteGraphGenerator<Integer, DefaultWeightedEdge> generator = new CompleteGraphGenerator<>(graphSize);
            generator.generateGraph(graph);
        }
    }
}
