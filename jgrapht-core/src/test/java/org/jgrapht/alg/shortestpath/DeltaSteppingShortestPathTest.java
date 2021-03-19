/*
 * (C) Copyright 2018-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.util.Triple;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.rules.*;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test case for {@link DeltaSteppingShortestPath}.
 *
 * @author Semen Chudakov
 */
public class DeltaSteppingShortestPathTest
{
    /**
     * Seed value which is used to generate random graphs by
     * {@code generateRandomGraph(Graph, int, double)} method.
     */
    private static final long SEED = 17l;
    /**
     * Executor which is supplied to {@link DeltaSteppingShortestPath} in this test case.
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

    private static final String S = "S";
    private static final String T = "T";
    private static final String Y = "Y";
    private static final String X = "X";
    private static final String Z = "Z";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyGraph()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(S);

        new DeltaSteppingShortestPath<>(graph, executor).getPaths(S);
    }

    @Test
    public void testNegativeWeightEdge()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(S, T));
        Graphs.addEdge(graph, S, T, -10.0);

        exception.expect(IllegalArgumentException.class);
        new DeltaSteppingShortestPath<>(graph, executor).getPaths(S);
    }

    @Test
    public void testLineGraph()
    {
        int maxNumberOfVertices = 10;
        for (int numberOfVertices = 2; numberOfVertices < maxNumberOfVertices; ++numberOfVertices) {
            Triple<Graph<Integer, DefaultWeightedEdge>, List<Integer>,
                List<DefaultWeightedEdge>> testInput = generateLineGraphTestInput(numberOfVertices);
            Graph<Integer, DefaultWeightedEdge> graph = testInput.getFirst();
            List<Integer> vertices = testInput.getSecond();
            List<DefaultWeightedEdge> edges = testInput.getThird();
            GraphPath<Integer, DefaultWeightedEdge> shortestPath =
                new DeltaSteppingShortestPath<>(graph, executor).getPath(0, numberOfVertices - 1);
            assertEquals(numberOfVertices - 1, shortestPath.getWeight(), 1e-9);
            assertEquals(vertices, shortestPath.getVertexList());
            assertEquals(edges, shortestPath.getEdgeList());
        }
    }

    @Test
    public void testGetPath()
    {
        Graph<String, DefaultWeightedEdge> graph = generateSimpleGraph();

        assertEquals(
            Arrays.asList(S),
            new DeltaSteppingShortestPath<>(graph, executor).getPath(S, S).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, T),
            new DeltaSteppingShortestPath<>(graph, executor).getPath(S, T).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, T, X),
            new DeltaSteppingShortestPath<>(graph, executor).getPath(S, X).getVertexList());
        assertEquals(
            Arrays.asList(S, Y),
            new DeltaSteppingShortestPath<>(graph, executor).getPath(S, Y).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, Z),
            new DeltaSteppingShortestPath<>(graph, executor).getPath(S, Z).getVertexList());
    }

    @Test
    public void testGetPaths1()
    {
        Graph<String, DefaultWeightedEdge> graph = generateSimpleGraph();

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> paths1 =
            new DeltaSteppingShortestPath<>(graph, 0.999, executor).getPaths(S);

        assertEquals(0d, paths1.getWeight(S), 1e-9);
        assertEquals(8d, paths1.getWeight(T), 1e-9);
        assertEquals(5d, paths1.getWeight(Y), 1e-9);
        assertEquals(9d, paths1.getWeight(X), 1e-9);
        assertEquals(7d, paths1.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> paths2 =
            new DeltaSteppingShortestPath<>(graph, 5.0, executor).getPaths(S);

        assertEquals(0d, paths2.getWeight(S), 1e-9);
        assertEquals(8d, paths2.getWeight(T), 1e-9);
        assertEquals(5d, paths2.getWeight(Y), 1e-9);
        assertEquals(9d, paths2.getWeight(X), 1e-9);
        assertEquals(7d, paths2.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> path3 =
            new DeltaSteppingShortestPath<>(graph, 11.0, executor).getPaths(S);

        assertEquals(0d, path3.getWeight(S), 1e-9);
        assertEquals(8d, path3.getWeight(T), 1e-9);
        assertEquals(5d, path3.getWeight(Y), 1e-9);
        assertEquals(9d, path3.getWeight(X), 1e-9);
        assertEquals(7d, path3.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> path4 =
            new DeltaSteppingShortestPath<>(graph, executor).getPaths(S);

        assertEquals(0d, path4.getWeight(S), 1e-9);
        assertEquals(8d, path4.getWeight(T), 1e-9);
        assertEquals(5d, path4.getWeight(Y), 1e-9);
        assertEquals(9d, path4.getWeight(X), 1e-9);
        assertEquals(7d, path4.getWeight(Z), 1e-9);
    }

    @Test
    public void testGetPaths2()
    {
        int numOfVertices = 100;
        int vertexDegree = 50;
        int numOfIterations = 30;
        int source = 0;
        Random random = new Random(SEED);
        for (int i = 0; i < numOfIterations; i++) {
            Graph<Integer, DefaultWeightedEdge> graph =
                generateRandomGraph(numOfVertices, vertexDegree * numOfVertices, random);
            test(graph, source);
        }
    }

    private void test(Graph<Integer, DefaultWeightedEdge> graph, Integer source)
    {
        ShortestPathAlgorithm.SingleSourcePaths<Integer,
            DefaultWeightedEdge> dijkstraShortestPaths =
                new DijkstraShortestPath<>(graph).getPaths(source);
        ShortestPathAlgorithm.SingleSourcePaths<Integer,
            DefaultWeightedEdge> deltaSteppingShortestPaths =
                new DeltaSteppingShortestPath<>(graph, executor).getPaths(source);
        assertEqualPaths(dijkstraShortestPaths, deltaSteppingShortestPaths, graph.vertexSet());
    }

    private Graph<String, DefaultWeightedEdge> generateSimpleGraph()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList(S, T, Y, X, Z));

        Graphs.addEdge(graph, S, T, 10);
        Graphs.addEdge(graph, S, Y, 5);

        Graphs.addEdge(graph, T, Y, 2);
        Graphs.addEdge(graph, T, X, 1);

        Graphs.addEdge(graph, Y, T, 3);
        Graphs.addEdge(graph, Y, Z, 2);
        Graphs.addEdge(graph, Y, X, 9);

        Graphs.addEdge(graph, X, Z, 4);

        Graphs.addEdge(graph, Z, X, 6);
        Graphs.addEdge(graph, Z, S, 7);

        return graph;
    }

    private Triple<Graph<Integer, DefaultWeightedEdge>, List<Integer>,
        List<DefaultWeightedEdge>> generateLineGraphTestInput(int numberOfVertices)
    {
        Graph<Integer, DefaultWeightedEdge> result =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        List<Integer> vertices = new ArrayList<>(numberOfVertices);
        List<DefaultWeightedEdge> edges = new ArrayList<>(numberOfVertices - 1);
        for (int i = 0; i < numberOfVertices - 1; ++i) {
            DefaultWeightedEdge edge = Graphs.addEdgeWithVertices(result, i, i + 1);
            vertices.add(i);
            edges.add(edge);
        }
        vertices.add(numberOfVertices - 1);
        return Triple.of(result, vertices, edges);
    }

    private Graph<Integer, DefaultWeightedEdge> generateRandomGraph(
        int numOfVertices, int numOfEdges, Random random)
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(
                numOfVertices, numOfEdges - numOfVertices + 1, random, true, true);
        generator.generateGraph(graph);
        makeConnected(graph);
        addEdgeWeights(graph, random);

        return graph;
    }

    private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph)
    {
        Object[] vertices = graph.vertexSet().toArray();
        for (int i = 0; i < vertices.length - 1; i++) {
            graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
        }
    }

    private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph, Random random)
    {
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, random.nextDouble());
        }
    }

    private void assertEqualPaths(
        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> expected,
        ShortestPathAlgorithm.SingleSourcePaths<Integer, DefaultWeightedEdge> actual,
        Set<Integer> vertexSet)
    {
        for (Integer sink : vertexSet) {
            GraphPath<Integer, DefaultWeightedEdge> path1 = expected.getPath(sink);
            GraphPath<Integer, DefaultWeightedEdge> path2 = actual.getPath(sink);
            if (path1 == null) {
                assertNull(path2);
            } else {
                assertEquals(
                    expected.getPath(sink).getWeight(), actual.getPath(sink).getWeight(), 1e-9);
            }
        }
    }
}
