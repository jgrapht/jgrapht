package org.jgrapht.perf.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.BlossomVOptions;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching;
import org.jgrapht.alg.matching.blossom.v5.MatchingMain;
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
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generate() {
            this.graph = MatchingMain.generateComplete(500, 100000);
        }
    }
}
