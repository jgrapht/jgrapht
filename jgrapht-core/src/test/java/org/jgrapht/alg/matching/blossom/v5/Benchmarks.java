package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, timeUnit = TimeUnit.MINUTES, time = 1)
@Measurement(iterations = 10, timeUnit = TimeUnit.MINUTES, time = 1)
public class Benchmarks {

    @Benchmark
    public void options0(Data data) {
        testBlossomV(data, data.options[0]);
    }

    @Benchmark
    public void options1(Data data) {
        testBlossomV(data, data.options[1]);
    }

    @Benchmark
    public void options2(Data data) {
        testBlossomV(data, data.options[2]);
    }

    @Benchmark
    public void options3(Data data) {
        testBlossomV(data, data.options[3]);
    }

    @Benchmark
    public void options4(Data data) {
        testBlossomV(data, data.options[4]);
    }

    @Benchmark
    public void options5(Data data) {
        testBlossomV(data, data.options[5]);
    }

    @Benchmark
    public void options6(Data data) {
        testBlossomV(data, data.options[6]);
    }

    @Benchmark
    public void options7(Data data) {
        testBlossomV(data, data.options[7]);
    }

    @Benchmark
    public void options8(Data data) {
        testBlossomV(data, data.options[8]);
    }

    @Benchmark
    public void options9(Data data) {
        testBlossomV(data, data.options[9]);
    }

    @Benchmark
    public void options10(Data data) {
        testBlossomV(data, data.options[10]);
    }

    @Benchmark
    public void options11(Data data) {
        testBlossomV(data, data.options[11]);
    }

    @Benchmark
    public void options12(Data data) {
        testBlossomV(data, data.options[12]);
    }

    @Benchmark
    public void options13(Data data) {
        testBlossomV(data, data.options[13]);
    }

    @Benchmark
    public void options14(Data data) {
        testBlossomV(data, data.options[14]);
    }

    @Benchmark
    public void options15(Data data) {
        testBlossomV(data, data.options[15]);
    }

    private void testBlossomV(Data data, Options options) {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching = new KolmogorovMinimumWeightPerfectMatching<>(data.graph, options);
        matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data {
        public Options[] options = Options.ALL_OPTIONS;
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup
        public void generate() throws IOException {
            MatchingMain.generateTriangulation(1000, 1, 1000000, false);
            graph = MatchingMain.readEdgeList(MatchingMain.EDGE_LIST_PATH);
        }
    }
}
