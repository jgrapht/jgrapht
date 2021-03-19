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
package org.jgrapht.generate.netgen;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.jgrapht.generate.netgen.NetworkGenerator.MAX_SUPPLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link NetworkGenerator}
 *
 * @author Timofey Chudakov
 */
public class NetworkGeneratorTest
{

    private static final long SEED = 1;
    private static final double EPS = 1e-9;

    private final Random rng = new Random(SEED);

    private static <V, E> void validateNetwork(
        Graph<V, E> network, NetworkInfo<V, E> networkInfo, NetworkGeneratorConfig config)
    {
        List<V> pureSources = networkInfo.getPureSources();
        assertEquals(config.getPureSourceNum(), pureSources.size());

        List<V> tSources = networkInfo.getTransshipmentSources();
        assertEquals(config.getTransshipSourceNum(), tSources.size());

        List<V> tNodes = networkInfo.getTransshipmentNodes();
        assertEquals(config.getTransshipNodeNum(), tNodes.size());

        List<V> pureSinks = networkInfo.getPureSinks();
        assertEquals(config.getPureSinkNum(), pureSinks.size());

        List<V> tSinks = networkInfo.getTransshipmentSinks();
        assertEquals(config.getTransshipSinkNum(), tSinks.size());

        List<List<V>> vertexClasses = new ArrayList<>();

        vertexClasses.add(pureSources);
        vertexClasses.add(tSources);
        vertexClasses.add(tNodes);
        vertexClasses.add(pureSinks);
        vertexClasses.add(tSinks);

        // validate that none of the vertices is of 2 types at the same time
        for (int i = 1; i < vertexClasses.size(); i++) {
            for (int j = 0; j < i; j++) {
                List<V> firstList = vertexClasses.get(i);
                List<V> secondList = vertexClasses.get(j);

                assertTrue(Collections.disjoint(firstList, secondList));
            }
        }

        // validate that every vertex belongs to the network
        for (List<V> vertexList : vertexClasses) {
            for (V vertex : vertexList) {
                assertTrue(network.containsVertex(vertex));
            }
        }

        // validate arc num constraint
        assertEquals(config.getArcNum(), network.edgeSet().size());

        // validate that none of the pure sources has incoming arcs
        for (V pureSource : pureSources) {
            assertTrue(network.incomingEdgesOf(pureSource).isEmpty());
        }

        // validate that none of the pure sinks has outgoing arcs
        for (V pureSink : pureSinks) {
            assertTrue(network.outgoingEdgesOf(pureSink).isEmpty());
        }

    }

    private static <V, E, N extends Number> void validateCapacities(
        Graph<V, E> graph, Function<E, N> capacities, NetworkInfo<V, E> info,
        NetworkGeneratorConfig config)
    {
        Set<E> skeletonArcs = new HashSet<>(info.getSkeletonArcs());

        for (E edge : graph.edgeSet()) {
            if (!skeletonArcs.contains(edge)) {
                assertTrue(capacities.apply(edge).doubleValue() + EPS >= config.getMinCap());
                assertTrue(capacities.apply(edge).doubleValue() - EPS <= config.getMaxCap());
            }
        }
    }

    private static <V, E, I extends Number> void validateCosts(
        Graph<V, E> graph, Function<E, I> costs, NetworkGeneratorConfig config)
    {
        for (E edge : graph.edgeSet()) {
            assertTrue(costs.apply(edge).doubleValue() >= config.getMinCost() - EPS);
            assertTrue(costs.apply(edge).doubleValue() <= config.getMaxCost() + EPS);
        }
    }

    private static <V,
        E> void validateSupplies(MinimumCostFlowProblem<V, E> problem, NetworkInfo<V, E> info)
    {
        Function<V, Integer> supplyFunction = problem.getNodeSupply();
        for (V source : info.getSources()) {
            assertTrue(supplyFunction.apply(source) > 0);
        }

        for (V sink : info.getSinks()) {
            assertTrue(supplyFunction.apply(sink) < 0);
        }
    }

    private static <V, E, I extends Number> void compareFunctions(
        Graph<V, E> firstGraph, Graph<V, E> secondGraph, Function<E, I> firstFunc,
        Function<E, I> secondFunc)
    {
        for (E firstArc : firstGraph.edgeSet()) {
            V source = firstGraph.getEdgeSource(firstArc);
            V target = firstGraph.getEdgeTarget(firstArc);

            E secondArc = secondGraph.getEdge(source, target);

            assertEquals(firstFunc.apply(firstArc), secondFunc.apply(secondArc));
        }
    }

    private static <V, E> void assertBipartiteMatchingProblemsAreEqual(
        BipartiteMatchingProblem<V, E> firstProblem, BipartiteMatchingProblem<V, E> secondProblem)
    {
        Graph<V, E> firstGraph = firstProblem.getGraph();
        Graph<V, E> secondGraph = secondProblem.getGraph();

        assertGraphsAreEqual(firstGraph, secondGraph);

        assertEquals(firstProblem.getPartition1(), secondProblem.getPartition1());
        assertEquals(firstProblem.getPartition2(), secondProblem.getPartition2());

        compareFunctions(
            firstGraph, secondGraph, firstProblem.getCosts(), secondProblem.getCosts());
    }

    private static <V, E> void assertMaxFlowProblemsAreEqual(
        MaximumFlowProblem<V, E> firstProblem, MaximumFlowProblem<V, E> secondProblem)
    {
        Graph<V, E> firstGraph = firstProblem.getGraph();
        Graph<V, E> secondGraph = secondProblem.getGraph();

        assertGraphsAreEqual(firstGraph, secondGraph);

        assertEquals(firstProblem.getSources(), secondProblem.getSources());
        assertEquals(firstProblem.getSinks(), secondProblem.getSinks());

        compareFunctions(
            firstGraph, secondGraph, firstProblem.getCapacities(), secondProblem.getCapacities());
    }

    private static <V, E> void assertMinCostFlowProblemsAreEqual(
        MinimumCostFlowProblem<V, E> firstProblem, MinimumCostFlowProblem<V, E> secondProblem)
    {
        Graph<V, E> firstGraph = firstProblem.getGraph();
        Graph<V, E> secondGraph = secondProblem.getGraph();

        assertGraphsAreEqual(firstGraph, secondGraph);

        Function<V, Integer> firstSupply = firstProblem.getNodeSupply();
        Function<V, Integer> secondSupply = secondProblem.getNodeSupply();

        for (V vertex : firstGraph.vertexSet()) {
            assertEquals(firstSupply.apply(vertex), secondSupply.apply(vertex));
        }

        compareFunctions(
            firstGraph, secondGraph, firstProblem.getArcCapacityLowerBounds(),
            secondProblem.getArcCapacityLowerBounds());
        compareFunctions(
            firstGraph, secondGraph, firstProblem.getArcCapacityUpperBounds(),
            secondProblem.getArcCapacityUpperBounds());
        compareFunctions(
            firstGraph, secondGraph, firstProblem.getArcCosts(), secondProblem.getArcCosts());

    }

    private static <V, E> void assertGraphsAreEqual(Graph<V, E> firstGraph, Graph<V, E> secondGraph)
    {
        assertEquals(firstGraph.vertexSet(), firstGraph.vertexSet());

        assertEquals(firstGraph.edgeSet().size(), secondGraph.edgeSet().size());

        for (E firstEdge : firstGraph.edgeSet()) {
            E secondEdge = secondGraph
                .getEdge(firstGraph.getEdgeSource(firstEdge), firstGraph.getEdgeTarget(firstEdge));
            assertEquals(
                firstGraph.getEdgeWeight(firstEdge), secondGraph.getEdgeWeight(secondEdge), EPS);
        }
    }

    private static <V, E> void assertIsFeasible(BipartiteMatchingProblem<V, E> problem)
    {
        Graph<V, E> graph = problem.getGraph();
        Graph<V, E> undirectedGraph = new AsUndirectedGraph<>(graph);

        MatchingAlgorithm<V, E> matchingAlgorithm =
            new HopcroftKarpMaximumCardinalityBipartiteMatching<>(
                undirectedGraph, problem.getPartition1(), problem.getPartition2());

        MatchingAlgorithm.Matching<V, E> matching = matchingAlgorithm.getMatching();

        assertEquals(graph.vertexSet().size(), 2 * matching.getEdges().size());
    }

    private static <V, E> double getMaxFlowValue(MaximumFlowProblem<V, E> problem)
    {
        MaximumFlowProblem<V, E> convertedProblem = problem.toSingleSourceSingleSinkProblem();

        Graph<V, E> graph = convertedProblem.getGraph();
        convertedProblem.dumpCapacities();

        MaximumFlowAlgorithm<V, E> algorithm = new PushRelabelMFImpl<>(graph);
        MaximumFlowAlgorithm.MaximumFlow<E> flow =
            algorithm.getMaximumFlow(convertedProblem.getSource(), convertedProblem.getSink());

        return flow.getValue();
    }

    private MinimumCostFlowProblem<Integer, DefaultEdge> generateMinCostFlowProblem(
        NetworkGeneratorConfig config, long seed)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), true);
        NetworkGenerator<Integer, DefaultEdge> generator = new NetworkGenerator<>(config, seed);
        MinimumCostFlowProblem<Integer, DefaultEdge> problem =
            generator.generateMinimumCostFlowProblem(graph);

        NetworkInfo<Integer, DefaultEdge> info = generator.getNetworkInfo();
        validateNetwork(graph, info, config);
        validateSupplies(problem, info);
        validateCapacities(graph, problem.getArcCapacityUpperBounds(), info, config);
        validateCosts(graph, problem.getArcCosts(), config);

        return problem;
    }

    private MaximumFlowProblem<Integer, DefaultEdge> generateMaxFlowProblem(
        NetworkGeneratorConfig config, long seed)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), true);
        NetworkGenerator<Integer, DefaultEdge> generator = new NetworkGenerator<>(config, seed);
        MaximumFlowProblem<Integer, DefaultEdge> problem = generator.generateMaxFlowProblem(graph);

        NetworkInfo<Integer, DefaultEdge> info = generator.getNetworkInfo();
        validateNetwork(graph, info, config);
        validateCapacities(graph, problem.getCapacities(), info, config);

        return problem;
    }

    private static BipartiteMatchingProblem<Integer, DefaultEdge> generateBipartiteMatchingProblem(
        NetworkGeneratorConfig config, long seed)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedWeightedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier());
        NetworkGenerator<Integer, DefaultEdge> generator = new NetworkGenerator<>(config, seed);
        BipartiteMatchingProblem<Integer, DefaultEdge> problem =
            generator.generateBipartiteMatchingProblem(graph);

        NetworkInfo<Integer, DefaultEdge> info = generator.getNetworkInfo();
        validateNetwork(graph, info, config);
        validateCosts(graph, problem.getCosts(), config);

        return problem;
    }

    private void testMinCostFlowProblem(NetworkGeneratorConfig config, long seed)
    {
        MinimumCostFlowProblem<Integer, DefaultEdge> firstProblem =
            generateMinCostFlowProblem(config, seed);
        MinimumCostFlowProblem<Integer, DefaultEdge> secondProblem =
            generateMinCostFlowProblem(config, seed);

        assertMinCostFlowProblemsAreEqual(firstProblem, secondProblem);
    }

    private void testMaxFlowProblem(NetworkGeneratorConfig config, long seed)
    {
        MaximumFlowProblem<Integer, DefaultEdge> firstProblem =
            generateMaxFlowProblem(config, seed);
        MaximumFlowProblem<Integer, DefaultEdge> secondProblem =
            generateMaxFlowProblem(config, seed);

        assertMaxFlowProblemsAreEqual(firstProblem, secondProblem);

        double maxFlow = getMaxFlowValue(firstProblem);
        assertTrue(maxFlow + EPS > config.getTotalSupply());
    }

    private void testBipartiteMatchingProblem(NetworkGeneratorConfig config, long seed)
    {
        BipartiteMatchingProblem<Integer, DefaultEdge> firstProblem =
            generateBipartiteMatchingProblem(config, seed);
        BipartiteMatchingProblem<Integer, DefaultEdge> secondProblem =
            generateBipartiteMatchingProblem(config, seed);

        assertIsFeasible(firstProblem);
        assertBipartiteMatchingProblemsAreEqual(firstProblem, secondProblem);
    }

    @Test
    public void testMinCostFlow_MinimumArcNum()
    {
        List<Integer> tNodes = List.of(0, 1, 2, 5, 10, 20, 30);
        for (int sourceNum = 1; sourceNum < 4; sourceNum++) {
            for (int tSourceNum = 0; tSourceNum <= sourceNum; tSourceNum++) {
                for (int sinkNum = 1; sinkNum < 4; sinkNum++) {
                    for (int tSinkNum = 0; tSinkNum <= sinkNum; tSinkNum++) {
                        for (int tNodeNum : tNodes) {
                            int arcNum = (int) NetworkGeneratorConfig
                                .getMinimumArcNum(sourceNum, tNodeNum, sinkNum);
                            NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                                .setParams(
                                    sourceNum + tNodeNum + sinkNum, arcNum, sourceNum, sinkNum,
                                    tSourceNum, tSinkNum, Math.max(sourceNum, sinkNum), 1, 100, 1,
                                    100, 100, 0)
                                .build();
                            testMinCostFlowProblem(config, rng.nextLong());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testMinCostFlow_MaximumArcNum()
    {
        List<Integer> tNodes = List.of(0, 1, 2, 5, 10, 20, 30);
        for (int sourceNum = 1; sourceNum < 4; sourceNum++) {
            for (int tSourceNum = 0; tSourceNum <= sourceNum; tSourceNum++) {
                for (int sinkNum = 1; sinkNum < 4; sinkNum++) {
                    for (int tSinkNum = 0; tSinkNum <= sinkNum; tSinkNum++) {
                        for (int tNodeNum : tNodes) {
                            int arcNum = (int) NetworkGeneratorConfig
                                .getMaximumArcNum(
                                    sourceNum, tSourceNum, tNodeNum, tSinkNum, sinkNum);
                            NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                                .setParams(
                                    sourceNum + tNodeNum + sinkNum, arcNum, sourceNum, sinkNum,
                                    tSourceNum, tSinkNum, 10 * sourceNum, 1, 100, 1, 100, 100, 0)
                                .build();
                            testMinCostFlowProblem(config, rng.nextLong());
                        }
                    }
                }
            }
        }
    }

    // @Test
    public void test()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setParams(
                100 * 1000, 2000 * 1000, 1000, 1000, 500, 500, MAX_SUPPLY, 1, 100000, 1, 100000,
                100, 0)
            .build();
        testMinCostFlowProblem(config, rng.nextLong());
    }

    @Test
    public void testMinCostFlow_MinimumArcNumA()
    {
        int sourceNum = 2;
        int tSourceNum = 0;
        int tNodeNum = 1;
        int tSinkNum = 0;
        int sinkNum = 3;
        int arcNum = (int) NetworkGeneratorConfig.getMinimumArcNum(sourceNum, tNodeNum, sinkNum);
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setParams(
                sourceNum + tNodeNum + sinkNum, arcNum, sourceNum, sinkNum, tSourceNum, tSinkNum,
                10 * sourceNum, 1, 100, 1, 100, 100, 0)
            .build();
        testMinCostFlowProblem(config, rng.nextLong());
    }

    @Test
    public void testMaxFlow_MinimumNumberOfArcs()
    {
        for (int sourceNum = 1; sourceNum < 5; sourceNum++) {
            for (int sinkNum = 1; sinkNum < 5; sinkNum++) {
                for (int tNodeNum = 0; tNodeNum < 30; tNodeNum++) {
                    int arcNum =
                        (int) NetworkGeneratorConfig.getMinimumArcNum(sourceNum, tNodeNum, sinkNum);
                    NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                        .setMaximumFlowProblemParams(
                            sourceNum + tNodeNum + sinkNum, arcNum, 10 * sourceNum, 1, 100,
                            sourceNum, sinkNum)
                        .build();
                    testMaxFlowProblem(config, rng.nextLong());
                }
            }
        }
    }

    @Test
    public void testMaxFlow_MaximumNumberOfArcs()
    {
        for (int sourceNum = 1; sourceNum < 5; sourceNum++) {
            for (int sinkNum = 1; sinkNum < 5; sinkNum++) {
                for (int tNodeNum = 0; tNodeNum < 30; tNodeNum++) {
                    int arcNum =
                        (int) NetworkGeneratorConfig.getMaximumArcNum(sourceNum, tNodeNum, sinkNum);
                    NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                        .setMaximumFlowProblemParams(
                            sourceNum + tNodeNum + sinkNum, arcNum, 10 * sourceNum, 1, 100,
                            sourceNum, sinkNum)
                        .build();
                    testMaxFlowProblem(config, rng.nextLong());
                }
            }
        }
    }

    @Test
    public void testMaxFlow_RandomNumberOfArcs()
    {
        for (int sourceNum = 1; sourceNum < 5; sourceNum++) {
            for (int sinkNum = 1; sinkNum < 5; sinkNum++) {
                for (int tNodeNum = 0; tNodeNum < 30; tNodeNum++) {
                    int lB =
                        (int) NetworkGeneratorConfig.getMinimumArcNum(sourceNum, tNodeNum, sinkNum);
                    int uB =
                        (int) NetworkGeneratorConfig.getMaximumArcNum(sourceNum, tNodeNum, sinkNum);
                    int arcNum = rng.nextInt(uB - lB + 1) + lB;
                    NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                        .setMaximumFlowProblemParams(
                            sourceNum + tNodeNum + sinkNum, arcNum, 10 * sourceNum, 1, 100,
                            sourceNum, sinkNum)
                        .build();
                    testMaxFlowProblem(config, rng.nextLong());
                }
            }
        }
    }

    @Test
    public void testBipartiteMatchingProblem_MinimumNumberOfArcs()
    {
        for (int i = 1; i < 50; i++) {
            NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                .setBipartiteMatchingProblemParams(2 * i, i, 1, 100).build();
            testBipartiteMatchingProblem(config, rng.nextLong());
        }
    }

    @Test
    public void testBipartiteMatchingProblem_MaximumNumberOfArcs()
    {
        for (int i = 1; i < 50; i++) {
            NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                .setBipartiteMatchingProblemParams(2 * i, i * i, 1, 100).build();
            testBipartiteMatchingProblem(config, rng.nextLong());
        }
    }

    @Test
    public void testBipartiteMatchingProblem_RandomNumberOfArcs()
    {
        for (int i = 1; i < 50; i++) {
            int max = i * i;
            int arcNum = rng.nextInt(max - i + 1) + i;
            NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
                .setBipartiteMatchingProblemParams(2 * i, arcNum, 1, 100).build();
            testBipartiteMatchingProblem(config, rng.nextLong());
        }
    }

    @Test
    public void testBipartiteMatchingProblem_LargeProblem()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setBipartiteMatchingProblemParams(1000, 250000, 1, 100).build();
        testBipartiteMatchingProblem(config, rng.nextLong());
    }
}
