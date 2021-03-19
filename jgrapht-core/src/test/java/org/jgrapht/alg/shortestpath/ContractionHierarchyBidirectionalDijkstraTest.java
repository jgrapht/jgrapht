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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link ContractionHierarchyBidirectionalDijkstra}.
 */
public class ContractionHierarchyBidirectionalDijkstraTest
{
    /**
     * Seed for random numbers generator used in tests.
     */
    private static final long SEED = 19L;

    /**
     * Executor which is supplied to the {@link ContractionHierarchyBidirectionalDijkstra} algorithm
     * in this test case.
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

    /**
     * This test asserts that not exception is thrown when an algorithm object is initialized with
     * an empty graph.
     */
    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        ContractionHierarchyBidirectionalDijkstra<Integer, DefaultWeightedEdge> dijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(graph, executor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNotPresent()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        ContractionHierarchyBidirectionalDijkstra<Integer, DefaultWeightedEdge> dijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(graph, executor);
        dijkstra.getPath(1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTargetNotPresent()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        ContractionHierarchyBidirectionalDijkstra<Integer, DefaultWeightedEdge> dijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(graph, executor);
        dijkstra.getPath(1, 2);
    }

    @Test
    public void testNoPath()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        ContractionHierarchyBidirectionalDijkstra<Integer, DefaultWeightedEdge> dijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(graph, executor);
        GraphPath<Integer, DefaultWeightedEdge> path = dijkstra.getPath(1, 2);
        assertNull(path);
    }

    @Test
    public void testSimpleGraph()
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

        ContractionHierarchyBidirectionalDijkstra<Integer, DefaultWeightedEdge> dijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(graph, executor);

        assertEquals(Collections.singletonList(1), dijkstra.getPath(1, 1).getVertexList());
        assertEquals(Arrays.asList(1, 2), dijkstra.getPath(1, 2).getVertexList());
        assertEquals(Arrays.asList(1, 4, 5, 6, 3), dijkstra.getPath(1, 3).getVertexList());
        assertEquals(Arrays.asList(1, 4, 5, 6, 9), dijkstra.getPath(1, 9).getVertexList());
        assertEquals(Arrays.asList(7, 4, 1), dijkstra.getPath(7, 1).getVertexList());
        assertEquals(Arrays.asList(8, 5, 2), dijkstra.getPath(8, 2).getVertexList());
    }

    @Test
    public void testRingGraph()
    {
        int size = 100;
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        fillRingGraph(graph, size);
        test(graph, 0);
    }

    @Test
    public void testOnRandomGraphs()
    {
        int numOfVertices = 100;
        int vertexDegree = 5;
        int numOfIterations = 20;
        int source = 0;
        Random random = new Random(SEED);
        for (int i = 0; i < numOfIterations; i++) {
            test(generateRandomGraph(numOfVertices, vertexDegree * numOfVertices, random), source);
        }
    }

    /**
     * Creates a connected graph with {@code size} vertices in which every vertex is connected to
     * only $2$ other vertices.
     *
     * @param graph graph
     * @param size needed size
     */
    private void fillRingGraph(Graph<Integer, DefaultWeightedEdge> graph, int size)
    {
        Random random = new Random(SEED);
        for (int i = 0; i < size; ++i) {
            graph.addVertex(i);
        }
        for (int i = 0; i < size; ++i) {
            graph.addEdge(i, (i + 1) % size);
            graph.setEdgeWeight(graph.getEdge(i, (i + 1) % size), random.nextDouble());
        }
    }

    /**
     * Test correctness of {@link ContractionHierarchyBidirectionalDijkstra} on {@code graph}
     * starting at {@code source}.
     *
     * @param graph graph
     * @param source vertex in {@code graph}
     */
    private void test(Graph<Integer, DefaultWeightedEdge> graph, Integer source)
    {
        ShortestPathAlgorithm.SingleSourcePaths<Integer,
            DefaultWeightedEdge> dijkstraShortestPaths =
                new DijkstraShortestPath<>(graph).getPaths(source);

        ContractionHierarchy<Integer, DefaultWeightedEdge> data =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> contractionDijkstra =
            new ContractionHierarchyBidirectionalDijkstra<>(data).getPaths(source);

        assertEqualPaths(dijkstraShortestPaths, contractionDijkstra, graph.vertexSet());
    }

    /**
     * Generates an instance or random graph with {@code numOfVertices} vertices and
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
     * Checks computed single source shortest paths tree for equality,
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
            GraphPath<Integer, DefaultWeightedEdge> expectedPath = expected.getPath(sink);
            GraphPath<Integer, DefaultWeightedEdge> actualPath = actual.getPath(sink);
            assertEquals(expectedPath, actualPath);
        }
    }
}
