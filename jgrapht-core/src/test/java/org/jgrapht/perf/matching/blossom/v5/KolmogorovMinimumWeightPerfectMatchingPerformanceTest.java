package org.jgrapht.perf.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching;
import org.jgrapht.alg.matching.blossom.v5.BlossomVOptions;
import org.jgrapht.alg.matching.blossom.v5.MatchingMain;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, timeUnit = TimeUnit.MINUTES, time = 1)
@Measurement(iterations = 5, timeUnit = TimeUnit.MINUTES, time = 2)
public class KolmogorovMinimumWeightPerfectMatchingPerformanceTest {

    private MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> testBlossomV(Data data, BlossomVOptions options) {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching = new KolmogorovMinimumWeightPerfectMatching<>(data.graph, data.options[data.optionNum]);
        return matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data {
        public BlossomVOptions[] options = BlossomVOptions.ALL_OPTIONS;
        Graph<Integer, DefaultWeightedEdge> graph;
        @Param({"200", "500", "1000"})
        public int size;
        @Param({"1000", "10000", "1000000"})
        public int upperBound;
        @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"})
        public int optionNum;

        @Setup(Level.Iteration)
        public void generate() throws IOException {
            MatchingMain.generateTriangulation(1000, 1, 1000000, false, false);
            graph = MatchingMain.readEdgeList();
        }
    }
}
