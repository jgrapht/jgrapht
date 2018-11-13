/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.perf.matching.blossom.v5;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.matching.blossom.v5.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.*;

@Fork(value = 5, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 10, time = 8)
public class KolmogorovMinimumWeightPerfectMatchingPerformanceTest
{

    @Benchmark
    public MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> testBlossomV(Data data)
    {
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> matching =
            new KolmogorovMinimumWeightPerfectMatching<>(data.graph, data.options[data.optionNum]);
        return matching.getMatching();
    }

    @State(Scope.Benchmark)
    public static class Data
    {

        public BlossomVOptions[] options = BlossomVOptions.ALL_OPTIONS;
        @Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "19", "20", "21", "22", "23" })
        public int optionNum;
        @Param({ "300", "500" })
        public int graphSize;
        Graph<Integer, DefaultWeightedEdge> graph;

        @Setup(Level.Iteration)
        public void generate()
        {
            this.graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            CompleteGraphGenerator<Integer, DefaultWeightedEdge> generator =
                new CompleteGraphGenerator<>(graphSize);
            generator.generateGraph(graph);
        }
    }
}
