package org.jgrapht.perf.matching.blossom.v5;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching;
import org.jgrapht.alg.matching.blossom.v5.Options;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, timeUnit = TimeUnit.MINUTES, time = 1)
@Measurement(iterations = 10, timeUnit = TimeUnit.MINUTES, time = 1)
public class KolmogorovMinimumWeightPerfectMatchingPerformanceTest {

    private MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> testBlossomV(Data data, Options options) {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching = new KolmogorovMinimumWeightPerfectMatching<>(data.graph, data.options[data.optionNum]);
        return matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data {
        public Options[] options = Options.ALL_OPTIONS;
        Graph<Integer, DefaultWeightedEdge> graph;
        @Param({"200", "500", "1000"})
        public int size;
        @Param({"1000", "10000", "1000000"})
        public int upperBound;
        @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"})
        public int optionNum;

        @Setup
        public void generate() {
            CompleteGraphGenerator<Integer, DefaultWeightedEdge> generator = new CompleteGraphGenerator<>(size);
            Random random = new Random(System.nanoTime());
            graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            generator.generateGraph(graph);
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                graph.setEdgeWeight(edge, random.nextInt(upperBound));
            }
        }
    }
}
