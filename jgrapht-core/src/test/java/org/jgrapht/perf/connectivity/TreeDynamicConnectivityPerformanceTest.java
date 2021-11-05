/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.perf.connectivity;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Performance test for {@link TreeDynamicConnectivity}
 *
 * @author Timofey Chudakov
 */
@Fork(value = 3, warmups = 0)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 1)
public class TreeDynamicConnectivityPerformanceTest
{
    private static final Random RANDOM = new Random(17L);

    @Benchmark
    public List<Boolean> testTreeDynamicConnectivity(Data data)
    {
        List<Boolean> res = new ArrayList<>();
        for (int v1 : data.firstSet) {
            for (int v2 : data.secondSet) {
                data.connectivity.link(data.connectNode1, data.connectNode2);
                res.add(data.connectivity.connected(v1, v2));
                data.connectivity.cut(data.connectNode1, data.connectNode2);
            }
        }
        return res;
    }

    @Benchmark
    public List<Boolean> testTreeNaiveConnectivity(Data data)
    {
        List<Boolean> res = new ArrayList<>();
        for (int v1 : data.firstSet) {
            for (int v2 : data.secondSet) {
                DefaultEdge edge = data.forest.addEdge(data.connectNode1, data.connectNode2);
                res.add(isConnected(data.forest, v1, v2));
                data.forest.removeEdge(edge);
            }
        }
        return res;
    }

    private boolean isConnected(Graph<Integer, DefaultEdge> graph, int v1, int v2)
    {
        BreadthFirstIterator<Integer, DefaultEdge> iterator = new BreadthFirstIterator<>(graph, v1);
        while (iterator.hasNext()) {
            if (iterator.next().equals(v2)) {
                return true;
            }
        }
        return false;
    }

    @State(Scope.Benchmark)
    public static class Data
    {
        @Param({ "10", "100", "1000", "10000", "100000", "1000000" })
        public int treeSize;
        public Graph<Integer, DefaultEdge> forest;
        public TreeDynamicConnectivity<Integer> connectivity;
        public Set<Integer> firstSet;
        public Set<Integer> secondSet;
        int connectNode1;
        int connectNode2;

        @Setup(Level.Iteration)
        public void init()
        {
            BarabasiAlbertForestGenerator<Integer, DefaultEdge> gen =
                new BarabasiAlbertForestGenerator<>(1, treeSize, RANDOM);
            forest = new DefaultUndirectedGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                false);
            Graph<Integer,
                DefaultEdge> secondTree = new DefaultUndirectedGraph<>(
                    SupplierUtil.createIntegerSupplier(treeSize),
                    SupplierUtil.createDefaultEdgeSupplier(), false);
            connectivity = new TreeDynamicConnectivity<>();

            gen.generateGraph(forest);
            gen.generateGraph(secondTree);
            Graphs.addGraph(forest, secondTree);

            for (int v : forest.vertexSet()) {
                connectivity.add(v);
            }
            for (DefaultEdge e : forest.edgeSet()) {
                int from = forest.getEdgeSource(e);
                int to = forest.getEdgeTarget(e);

                connectivity.link(to, from);
            }

            assert !connectivity.connected(0, treeSize);

            firstSet = gen(0, treeSize, 5);
            secondSet = gen(treeSize, 2 * treeSize, 5);
            connectNode1 = RANDOM.nextInt(treeSize);
            connectNode2 = RANDOM.nextInt(treeSize) + treeSize;
        }

        private Set<Integer> gen(int from, int to, int num)
        {
            Set<Integer> res = new HashSet<>();
            int dist = to - from;
            while (res.size() < num) {
                res.add(RANDOM.nextInt(dist) + from);
            }
            return res;
        }
    }
}
