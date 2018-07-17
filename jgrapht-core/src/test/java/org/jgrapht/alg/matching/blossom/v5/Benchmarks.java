package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, timeUnit = TimeUnit.MINUTES, time = 1)
@Measurement(iterations = 10, timeUnit = TimeUnit.MINUTES, time = 1)
public class Benchmarks {

    public static Options[] options = new Options[]{
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), //[0]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), //[1]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), //[2]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), //[3]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, true, true), //[4]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, true, false), //[5]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, false, true), //[6]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, false, false), //[7]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), //[8]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), //[9]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), //[10]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), //[11]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, true), //[12]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, false), //[13]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), //[14]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), //[15]
    };

    @Benchmark
    public void options0(Data data) {
        testBlossomV(data, options[0]);
    }

    @Benchmark
    public void options1(Data data) {
        testBlossomV(data, options[1]);
    }

    @Benchmark
    public void options2(Data data) {
        testBlossomV(data, options[2]);
    }

    @Benchmark
    public void options3(Data data) {
        testBlossomV(data, options[3]);
    }

    @Benchmark
    public void options4(Data data) {
        testBlossomV(data, options[4]);
    }

    @Benchmark
    public void options5(Data data) {
        testBlossomV(data, options[5]);
    }

    @Benchmark
    public void options6(Data data) {
        testBlossomV(data, options[6]);
    }

    @Benchmark
    public void options7(Data data) {
        testBlossomV(data, options[7]);
    }

    @Benchmark
    public void options8(Data data) {
        testBlossomV(data, options[8]);
    }

    @Benchmark
    public void options9(Data data) {
        testBlossomV(data, options[9]);
    }

    @Benchmark
    public void options10(Data data) {
        testBlossomV(data, options[10]);
    }

    @Benchmark
    public void options11(Data data) {
        testBlossomV(data, options[11]);
    }

    @Benchmark
    public void options12(Data data) {
        testBlossomV(data, options[12]);
    }

    @Benchmark
    public void options13(Data data) {
        testBlossomV(data, options[13]);
    }

    @Benchmark
    public void options14(Data data) {
        testBlossomV(data, options[14]);
    }

    @Benchmark
    public void options15(Data data) {
        testBlossomV(data, options[15]);
    }

    public void testBlossomV(Data data, Options options) {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching = new KolmogorovMinimumWeightPerfectMatching<>(data.graph, options);
        matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data {
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup
        public void generate() throws IOException {
            MatchingMain.generateTriangulation(1000, 1, 1000000, false);
            graph = MatchingMain.readEdgeList(MatchingMain.EDGE_LIST_PATH);
        }
    }
}
