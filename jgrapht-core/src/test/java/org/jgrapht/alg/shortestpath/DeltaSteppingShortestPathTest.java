/*
 * (C) Copyright 2018-2020, by Semen Chudakov and Contributors.
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
import org.junit.rules.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test case for {@link DeltaSteppingShortestPath}.
 *
 * @author Semen Chudakov
 */
public class DeltaSteppingShortestPathTest
{

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

        new DeltaSteppingShortestPath<>(graph).getPaths(S);
    }

    @Test
    public void testNegativeWeightEdge()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(S, T));
        Graphs.addEdge(graph, S, T, -10.0);

        exception.expect(IllegalArgumentException.class);
        new DeltaSteppingShortestPath<>(graph).getPaths(S);
    }

    @Test
    public void testGetPath()
    {
        Graph<String, DefaultWeightedEdge> graph = generateSimpleGraph();

        assertEquals(
            Arrays.asList(S), new DeltaSteppingShortestPath<>(graph).getPath(S, S).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, T),
            new DeltaSteppingShortestPath<>(graph).getPath(S, T).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, T, X),
            new DeltaSteppingShortestPath<>(graph).getPath(S, X).getVertexList());
        assertEquals(
            Arrays.asList(S, Y),
            new DeltaSteppingShortestPath<>(graph).getPath(S, Y).getVertexList());
        assertEquals(
            Arrays.asList(S, Y, Z),
            new DeltaSteppingShortestPath<>(graph).getPath(S, Z).getVertexList());
    }

    @Test
    public void testGetPaths1()
    {
        Graph<String, DefaultWeightedEdge> graph = generateSimpleGraph();

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> paths1 =
            new DeltaSteppingShortestPath<>(graph, 0.999).getPaths(S);

        assertEquals(0d, paths1.getWeight(S), 1e-9);
        assertEquals(8d, paths1.getWeight(T), 1e-9);
        assertEquals(5d, paths1.getWeight(Y), 1e-9);
        assertEquals(9d, paths1.getWeight(X), 1e-9);
        assertEquals(7d, paths1.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> paths2 =
            new DeltaSteppingShortestPath<>(graph, 5.0).getPaths(S);

        assertEquals(0d, paths2.getWeight(S), 1e-9);
        assertEquals(8d, paths2.getWeight(T), 1e-9);
        assertEquals(5d, paths2.getWeight(Y), 1e-9);
        assertEquals(9d, paths2.getWeight(X), 1e-9);
        assertEquals(7d, paths2.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> path3 =
            new DeltaSteppingShortestPath<>(graph, 11.0).getPaths(S);

        assertEquals(0d, path3.getWeight(S), 1e-9);
        assertEquals(8d, path3.getWeight(T), 1e-9);
        assertEquals(5d, path3.getWeight(Y), 1e-9);
        assertEquals(9d, path3.getWeight(X), 1e-9);
        assertEquals(7d, path3.getWeight(Z), 1e-9);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> path4 =
            new DeltaSteppingShortestPath<>(graph).getPaths(S);

        assertEquals(0d, path4.getWeight(S), 1e-9);
        assertEquals(8d, path4.getWeight(T), 1e-9);
        assertEquals(5d, path4.getWeight(Y), 1e-9);
        assertEquals(9d, path4.getWeight(X), 1e-9);
        assertEquals(7d, path4.getWeight(Z), 1e-9);
    }

    @Test
    public void testGetPaths2()
    {
        int numOfVertices = 1000;
        int vertexDegree = 100;
        int numOfIterations = 100;
        int source = 0;
        for (int i = 0; i < numOfIterations; i++) {
            Graph<Integer, DefaultWeightedEdge> graph =
                generateRandomGraph(numOfVertices, vertexDegree * numOfVertices);
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
                new DeltaSteppingShortestPath<>(graph).getPaths(source);
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

    private Graph<Integer, DefaultWeightedEdge> generateRandomGraph(
        int numOfVertices, int numOfEdges)
    {
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(numOfVertices, numOfEdges - numOfVertices + 1);
        generator.generateGraph(graph);
        makeConnected(graph);
        addEdgeWeights(graph);

        return graph;
    }

    private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph)
    {
        Object[] vertices = graph.vertexSet().toArray();
        for (int i = 0; i < vertices.length - 1; i++) {
            graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
        }
    }

    private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph)
    {
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, Math.random());
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
