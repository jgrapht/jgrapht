/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.ConcurrencyUtil;
import org.jgrapht.util.SupplierUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.TransitNodeRouting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for the {@link TransitNodeRoutingShortestPath}.
 *
 * @author Semen Chudakov
 */
public class TransitNodeRoutingShortestPathTest
{
    /**
     * Seed for random numbers generator used in tests.
     */
    private static final long SEED = 19L;

    /**
     * Executor which is supplied to {@link TransitNodeRoutingShortestPath},
     * {@link TransitNodeRoutingPrecomputation} and {@link ContractionHierarchyPrecomputation} in
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
    public void testOneVertex()
    {
        Integer vertex = 1;
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(vertex);

        TransitNodeRoutingShortestPath<Integer, DefaultWeightedEdge> shortestPath =
            new TransitNodeRoutingShortestPath<>(graph, executor);

        GraphPath<Integer, DefaultWeightedEdge> path = shortestPath.getPath(vertex, vertex);
        GraphWalk<Integer, DefaultWeightedEdge> expectedPath = new GraphWalk<>(
            graph, vertex, vertex, Collections.singletonList(vertex), Collections.emptyList(), 0.0);
        assertEquals(expectedPath, path);
    }

    @Test
    public void testTwoVertices()
    {
        Integer v1 = 1;
        Integer v2 = 2;

        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge1 = Graphs.addEdgeWithVertices(graph, v1, v2, 1.0);
        DefaultWeightedEdge edge2 = Graphs.addEdgeWithVertices(graph, v1, v2, 2.0);
        DefaultWeightedEdge edge3 = Graphs.addEdgeWithVertices(graph, v2, v1, 1.0);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        TransitNodeRouting<Integer, DefaultWeightedEdge> routing =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, 1, executor)
                .computeTransitNodeRouting();
        TransitNodeRoutingShortestPath<Integer, DefaultWeightedEdge> shortestPath =
            new TransitNodeRoutingShortestPath<>(routing);

        GraphPath<Integer, DefaultWeightedEdge> expectedPath1 = new GraphWalk<>(
            graph, v1, v2, Arrays.asList(v1, v2), Collections.singletonList(edge1), 1.0);
        assertEquals(expectedPath1, shortestPath.getPath(v1, v2));

        GraphPath<Integer, DefaultWeightedEdge> expectedPath2 = new GraphWalk<>(
            graph, v2, v1, Arrays.asList(v2, v1), Collections.singletonList(edge3), 1.0);
        assertEquals(expectedPath2, shortestPath.getPath(v2, v1));
    }

    @Test
    public void testThreeVertices()
    {
        Integer v1 = 1;
        Integer v2 = 2;
        Integer v3 = 3;

        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge1 = Graphs.addEdgeWithVertices(graph, v1, v2, 1.0);
        DefaultWeightedEdge edge2 = Graphs.addEdgeWithVertices(graph, v2, v3, 2.0);
        DefaultWeightedEdge edge3 = Graphs.addEdgeWithVertices(graph, v3, v2, 1.0);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        TransitNodeRouting<Integer, DefaultWeightedEdge> routing =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, 1, executor)
                .computeTransitNodeRouting();
        TransitNodeRoutingShortestPath<Integer, DefaultWeightedEdge> shortestPath =
            new TransitNodeRoutingShortestPath<>(routing);

        GraphPath<Integer, DefaultWeightedEdge> expectedPath1 = new GraphWalk<>(
            graph, v1, v2, Arrays.asList(v1, v2), Collections.singletonList(edge1), 1.0);
        assertEquals(expectedPath1, shortestPath.getPath(v1, v2));
        assertNull(shortestPath.getPath(v2, v1));

        GraphPath<Integer, DefaultWeightedEdge> expectedPath2 = new GraphWalk<>(
            graph, v2, v3, Arrays.asList(v2, v3), Collections.singletonList(edge2), 2.0);
        assertEquals(expectedPath2, shortestPath.getPath(v2, v3));
        GraphPath<Integer, DefaultWeightedEdge> expectedPath3 = new GraphWalk<>(
            graph, v3, v2, Arrays.asList(v3, v2), Collections.singletonList(edge3), 1.0);
        assertEquals(expectedPath3, shortestPath.getPath(v3, v2));

        GraphPath<Integer, DefaultWeightedEdge> expectedPath4 = new GraphWalk<>(
            graph, v1, v3, Arrays.asList(v1, v2, v3), Arrays.asList(edge1, edge2), 3.0);
        assertEquals(expectedPath4, shortestPath.getPath(v1, v3));
        assertNull(shortestPath.getPath(v3, v1));
    }

    @Test
    public void testOnRandomGraphs()
    {
        int numOfVertices = 30;
        int vertexDegree = 5;
        int numOfIterations = 20;
        int source = 0;
        Random random = new Random(SEED);
        for (int i = 0; i < numOfIterations; ++i) {
            testOnGraph(
                generateRandomGraph(numOfVertices, vertexDegree * numOfVertices, random), source);
        }
    }

    /**
     * Test correctness of {@link TransitNodeRoutingShortestPath} on {@code graph} starting at
     * {@code source}.
     *
     * @param graph graph
     * @param source vertex in {@code graph}
     */
    private void testOnGraph(Graph<Integer, DefaultWeightedEdge> graph, Integer source)
    {
        ShortestPathAlgorithm.SingleSourcePaths<Integer,
            DefaultWeightedEdge> dijkstraShortestPaths =
                new DijkstraShortestPath<>(graph).getPaths(source);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        TransitNodeRouting<Integer, DefaultWeightedEdge> routing =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, executor)
                .computeTransitNodeRouting();

        TransitNodeRoutingShortestPath<Integer,
            DefaultWeightedEdge> transitNodeRoutingShortestPath =
                new TransitNodeRoutingShortestPath<>(routing);
        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> tnrShortestPaths =
            transitNodeRoutingShortestPath.getPaths(source);

        assertEqualPaths(dijkstraShortestPaths, tnrShortestPaths, graph.vertexSet());
    }

    /**
     * Generates an instance of random graph with {@code numOfVertices} vertices and
     * {@code numOfEdges} edges.
     *
     * @param numOfVertices number of vertices
     * @param numOfEdges number of edges
     * @return generated graph
     */
    private Graph<Integer, DefaultWeightedEdge> generateRandomGraph(
        int numOfVertices, int numOfEdges, Random random)
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(numOfVertices, numOfEdges - numOfVertices + 1, SEED);
        generator.generateGraph(graph);
        makeConnected(graph);
        addEdgeWeights(graph, random);

        return graph;
    }

    /**
     * Makes {@code graph} connected.
     *
     * @param graph graph
     */
    private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph)
    {
        Object[] vertices = graph.vertexSet().toArray();
        for (int i = 0; i < vertices.length - 1; ++i) {
            graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
            graph.addEdge((Integer) vertices[i + 1], (Integer) vertices[i]);
        }
    }

    /**
     * Sets edge weights to edges in {@code graph}.
     *
     * @param graph graph
     * @param random random numbers generator
     */
    private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph, Random random)
    {
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, random.nextDouble());
        }
    }

    /**
     * Checks computed single source shortest paths tree for equality.
     *
     * @param expected expected paths
     * @param actual actual paths
     * @param vertexSet vertices
     */
    private void assertEqualPaths(
        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> expected,
        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> actual,
        Set<Integer> vertexSet)
    {
        for (Integer sink : vertexSet) {
            assertEquals(expected.getPath(sink), actual.getPath(sink));
        }
    }
}
