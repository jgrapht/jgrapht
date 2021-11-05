/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.*;
import static org.junit.Assert.*;

/**
 * Tests for the {@link ContractionHierarchyPrecomputation}.
 */
public class ContractionHierarchyPrecomputationTest
{
    /**
     * Seed for random numbers generator used in tests.
     */
    private static final long SEED = 19L;

    /**
     * Executor which is supplied to the {@link ContractionHierarchyPrecomputation} algorithm in
     * this test case.
     */
    private static ThreadPoolExecutor executor;

    @BeforeClass
    public static void createExecutor()
    {
        executor =
            ConcurrencyUtil.createThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    @AfterClass
    public static void shutdownExecutor()
        throws InterruptedException
    {
        ConcurrencyUtil.shutdownExecutionService(executor);
    }

    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertNotNull(contractionGraph);
        assertNotNull(contractionMapping);

        assertTrue(contractionGraph.vertexSet().isEmpty());
        assertTrue(contractionGraph.edgeSet().isEmpty());
        assertTrue(contractionMapping.keySet().isEmpty());
    }

    @Test
    public void testDirectedGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(2, contractionGraph.edgeSet().size());

        assertTrue(
            contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(
            contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
    }

    @Test
    public void testDirectedGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 1, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        Graphs.addEdgeWithVertices(graph, 3, 2, 1);
        Graphs.addEdgeWithVertices(graph, 3, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(6, contractionGraph.edgeSet().size());
    }

    @Test
    public void testDirectedGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        Graphs.addEdgeWithVertices(graph, 3, 4, 1);
        Graphs.addEdgeWithVertices(graph, 3, 5, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(5, contractionGraph.vertexSet().size());
        assertEquals(4, contractionGraph.edgeSet().size());

        List<Pair<Integer, Integer>> vertexPairs =
            Arrays.asList(Pair.of(1, 3), Pair.of(2, 3), Pair.of(3, 4), Pair.of(3, 5));

        for (Pair<Integer, Integer> pair : vertexPairs) {
            assertTrue(
                contractionGraph
                    .containsEdge(
                        contractionMapping.get(pair.getFirst()),
                        contractionMapping.get(pair.getSecond())));
        }
    }

    @Test
    public void testUndirectedGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(4, contractionGraph.edgeSet().size());

        List<Pair<Integer, Integer>> vertexPairs =
            Arrays.asList(Pair.of(1, 2), Pair.of(2, 1), Pair.of(2, 3), Pair.of(3, 2));

        for (Pair<Integer, Integer> pair : vertexPairs) {
            assertTrue(
                contractionGraph
                    .containsEdge(
                        contractionMapping.get(pair.getFirst()),
                        contractionMapping.get(pair.getSecond())));
        }
    }

    @Test
    public void testUndirectedGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        Graphs.addEdgeWithVertices(graph, 3, 1, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();

        assertEquals(3, graph.vertexSet().size());

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(6, contractionGraph.edgeSet().size());
    }

    @Test
    public void testUndirectedGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        Graphs.addEdgeWithVertices(graph, 3, 4, 1);
        Graphs.addEdgeWithVertices(graph, 3, 5, 1);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(5, contractionGraph.vertexSet().size());
        assertEquals(8, contractionGraph.edgeSet().size());

        List<Pair<Integer, Integer>> vertexPairs = Arrays
            .asList(
                Pair.of(1, 3), Pair.of(3, 1), Pair.of(2, 3), Pair.of(3, 2), Pair.of(3, 4),
                Pair.of(4, 3), Pair.of(3, 5), Pair.of(5, 3));

        for (Pair<Integer, Integer> pair : vertexPairs) {
            assertTrue(
                contractionGraph
                    .containsEdge(
                        contractionMapping.get(pair.getFirst()),
                        contractionMapping.get(pair.getSecond())));
        }
    }

    @Test
    public void testUndirectedGraph4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4, 1);

        Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        Graphs.addEdgeWithVertices(graph, 2, 5, 1);

        Graphs.addEdgeWithVertices(graph, 3, 6, 1);

        Graphs.addEdgeWithVertices(graph, 4, 5, 1);
        Graphs.addEdgeWithVertices(graph, 4, 7, 1);

        Graphs.addEdgeWithVertices(graph, 5, 6, 1);
        Graphs.addEdgeWithVertices(graph, 5, 8, 1);

        Graphs.addEdgeWithVertices(graph, 6, 9, 1);

        Graphs.addEdgeWithVertices(graph, 7, 8, 3);
        Graphs.addEdgeWithVertices(graph, 8, 9, 3);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(9, contractionGraph.vertexSet().size());
        assertEquals(24, contractionGraph.edgeSet().size());

        List<Pair<Integer, Integer>> vertexPairs = Arrays
            .asList(
                Pair.of(1, 2), Pair.of(2, 1), Pair.of(1, 4), Pair.of(4, 1), Pair.of(2, 3),
                Pair.of(3, 2), Pair.of(2, 5), Pair.of(5, 2), Pair.of(3, 6), Pair.of(6, 3),

                Pair.of(4, 5), Pair.of(5, 4), Pair.of(4, 7), Pair.of(7, 4), Pair.of(5, 6),
                Pair.of(6, 5), Pair.of(5, 8), Pair.of(8, 5), Pair.of(6, 9), Pair.of(9, 6),

                Pair.of(7, 8), Pair.of(8, 7), Pair.of(8, 9), Pair.of(9, 8));

        for (Pair<Integer, Integer> pair : vertexPairs) {
            assertTrue(
                contractionGraph
                    .containsEdge(
                        contractionMapping.get(pair.getFirst()),
                        contractionMapping.get(pair.getSecond())));
        }
    }

    @Test
    public void testPseudograph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2, 3);

        Graphs.addEdgeWithVertices(graph, 2, 1, 1);
        Graphs.addEdgeWithVertices(graph, 2, 1, 2);

        Graphs.addEdgeWithVertices(graph, 2, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 2, 2);

        ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> contractor =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor);
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            contractor.computeContractionHierarchy();

        assertNotNull(hierarchy);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            hierarchy.getContractionMapping();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(2, contractionGraph.vertexSet().size());
        assertEquals(2, contractionGraph.edgeSet().size());

        assertTrue(
            contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(
            contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(1)));

        assertEquals(
            1,
            contractionGraph
                .getEdgeWeight(
                    contractionGraph.getEdge(contractionMapping.get(1), contractionMapping.get(2))),
            1e-9);
        assertEquals(
            1,
            contractionGraph
                .getEdgeWeight(
                    contractionGraph.getEdge(contractionMapping.get(2), contractionMapping.get(1))),
            1e-9);
    }

    @Test
    public void testOnRandomGraphs()
    {
        int numOfGraphs = 20;
        int numOfVertices = 30;
        double probability = 0.2;

        for (int i = 0; i < numOfGraphs; ++i) {
            DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
                new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

            generateRandomGraph(graph, numOfVertices, probability);

            ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
                new ContractionHierarchyPrecomputation<>(graph, executor)
                    .computeContractionHierarchy();

            assertCorrectMapping(graph, hierarchy);
            assertNoEdgesRemoved(graph, hierarchy);
            assertCorrectEdgeWeights(graph, hierarchy);
            assertCorrectContractionEdges(graph, hierarchy);
        }
    }

    /**
     * Asserts that {@code mapping} includes all vertices in {@code graph} as keys, all vertices in
     * {@code contractionGraph} as values and the values in {@code mapping} are unique.
     *
     * @param graph graph
     * @param hierarchy contraction hierarchy
     */
    private void assertCorrectMapping(
        Graph<Integer, DefaultWeightedEdge> graph,
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy)
    {
        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> mapping = hierarchy.getContractionMapping();

        assertEquals(graph.vertexSet(), mapping.keySet());
        Set<ContractionVertex<Integer>> uniqueValues = new HashSet<>(mapping.values());
        assertEquals(graph.vertexSet().size(), uniqueValues.size());
        assertEquals(contractionGraph.vertexSet(), uniqueValues);
    }

    /**
     * Asserts that for every edge in {@code graph} between $s$ and $t$ there exists an edge in
     * {@code contractionGraph} between contracted $s$ and $t$.
     *
     * @param graph graph
     * @param hierarchy contraction hierarchy
     */
    private void assertNoEdgesRemoved(
        Graph<Integer, DefaultWeightedEdge> graph,
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy)
    {
        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> mapping = hierarchy.getContractionMapping();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Integer source = graph.getEdgeSource(edge);
            Integer target = graph.getEdgeTarget(edge);

            assertTrue(contractionGraph.containsEdge(mapping.get(source), mapping.get(target)));
        }
    }

    /**
     * Asserts that every edge in {@code graph} between $s$ and $t$ has greater or equal weight than
     * an edge in {@code contractedGraph} between the contracted $s$ and $t$.
     *
     * @param graph graph
     * @param hierarchy contraction hierarchy
     */
    private void assertCorrectEdgeWeights(
        Graph<Integer, DefaultWeightedEdge> graph,
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy)
    {
        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> mapping = hierarchy.getContractionMapping();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Integer source = graph.getEdgeSource(edge);
            Integer target = graph.getEdgeTarget(edge);
            ContractionVertex<Integer> contractedSource = mapping.get(source);
            ContractionVertex<Integer> contractedTarget = mapping.get(target);

            double oldWeight = graph.getEdgeWeight(edge);
            double newWeight = contractionGraph
                .getEdgeWeight(contractionGraph.getEdge(contractedSource, contractedTarget));
            assertTrue(oldWeight >= newWeight);
        }
    }

    /**
     * Asserts for every edge $e1$ in {@code contractionGraph} which is not a shortcuts and contains
     * an edge $e2$ of the original {@code graph} that weight og $e1$ is equal to the weight of
     * $e2$. Secondly for vertices in the {@code graph} which correspond to source $s$ and target
     * $t$ of $e1$ asserts that there is an edge $e2$ between them and the weight of $e2$ is minimum
     * among all edges between $s$ and $t$.
     *
     * @param graph graph
     * @param hierarchy contraction hierarchy
     */
    private void assertCorrectContractionEdges(
        Graph<Integer, DefaultWeightedEdge> graph,
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy)
    {
        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph =
            hierarchy.getContractionGraph();
        Map<Integer, ContractionVertex<Integer>> mapping = hierarchy.getContractionMapping();

        Map<ContractionVertex<Integer>, Integer> inverseMapping = new HashMap<>();
        for (Map.Entry<Integer, ContractionVertex<Integer>> entry : mapping.entrySet()) {
            inverseMapping.put(entry.getValue(), entry.getKey());
        }

        for (ContractionEdge<DefaultWeightedEdge> contractionEdge : contractionGraph.edgeSet()) {
            if (contractionEdge.edge != null) { // it is not a shortcut edge
                double edgeWeight = graph.getEdgeWeight(contractionEdge.edge);
                assertEquals(edgeWeight, contractionGraph.getEdgeWeight(contractionEdge), 1e-9);

                ContractionVertex<Integer> contractedSource =
                    contractionGraph.getEdgeSource(contractionEdge);
                ContractionVertex<Integer> contractedTarget =
                    contractionGraph.getEdgeTarget(contractionEdge);

                Integer source = inverseMapping.get(contractedSource);
                Integer target = inverseMapping.get(contractedTarget);

                boolean containsEdge = false;
                for (DefaultWeightedEdge edge : graph.getAllEdges(source, target)) {
                    assertTrue(graph.getEdgeWeight(edge) >= edgeWeight);
                    if (edge.equals(contractionEdge.edge)) {
                        containsEdge = true;
                    }
                }
                assertTrue(containsEdge);
            }
        }
    }

    /**
     * Generates random graph from the $G(n, p)$ model.
     *
     * @param graph graph instance for the generator
     * @param n the number of nodes
     * @param p the edge probability
     */
    private void generateRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p)
    {
        Random random = new Random(SEED);
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnpRandomGraphGenerator<>(n, p, SEED);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, random.nextDouble()));
    }
}
