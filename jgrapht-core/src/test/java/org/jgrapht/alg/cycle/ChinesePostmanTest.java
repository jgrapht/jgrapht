/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

/**
 * @author Joris Kinable
 */
public class ChinesePostmanTest
{

    @Test(expected = IllegalArgumentException.class)
    public void testGraphNoVertices()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        ChinesePostman<Integer, DefaultEdge> alg = new ChinesePostman<>();
        alg.getCPPSolution(g);
    }

    @Test
    public void testSingleEdgeGraph()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        Graphs.addEdge(g, 0, 1, 10);

        this.verifyClosedPath(g, 20, 2);
    }

    @Test
    public void testGraphWithSelfloop()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        Graphs.addEdge(g, 0, 1, 10);
        Graphs.addEdge(g, 0, 0, 20);

        this.verifyClosedPath(g, 40, 3);
    }

    @Test
    public void testGraphWithMultipleEdges()
    {
        Graph<Integer, DefaultWeightedEdge> g = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 1);
        Graphs.addEdge(g, 1, 4, 3);
        Graphs.addEdge(g, 2, 3, 20);
        Graphs.addEdge(g, 2, 3, 10);
        Graphs.addEdge(g, 3, 4, 2);

        this.verifyClosedPath(g, 42, 8);
    }

    @Test
    public void testUndirectedGraph1()
    {
        Graph<Character, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'));
        Graphs.addEdge(g, 'A', 'B', 50);
        Graphs.addEdge(g, 'A', 'C', 50);
        Graphs.addEdge(g, 'A', 'D', 50);
        Graphs.addEdge(g, 'B', 'D', 50);
        Graphs.addEdge(g, 'B', 'E', 70);
        Graphs.addEdge(g, 'B', 'F', 50);
        Graphs.addEdge(g, 'C', 'D', 70);
        Graphs.addEdge(g, 'C', 'G', 70);
        Graphs.addEdge(g, 'C', 'H', 120);
        Graphs.addEdge(g, 'D', 'F', 60);
        Graphs.addEdge(g, 'E', 'F', 70);
        Graphs.addEdge(g, 'F', 'H', 60);
        Graphs.addEdge(g, 'G', 'H', 70);

        this.verifyClosedPath(g, 1000, 16);
    }

    @Test
    public void testUndirectedGraph2()
    {
        Graph<Character, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList('A', 'B', 'C', 'D', 'E'));
        Graphs.addEdge(g, 'A', 'B', 8);
        Graphs.addEdge(g, 'A', 'C', 5);
        Graphs.addEdge(g, 'A', 'D', 6);
        Graphs.addEdge(g, 'B', 'C', 5);
        Graphs.addEdge(g, 'B', 'E', 6);
        Graphs.addEdge(g, 'C', 'D', 5);
        Graphs.addEdge(g, 'C', 'E', 5);
        Graphs.addEdge(g, 'D', 'E', 8);

        this.verifyClosedPath(g, 60, 10);
    }

    @Test
    public void testUndirectedGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        Graphs.addEdge(g, 1, 2, 5);
        Graphs.addEdge(g, 1, 4, 4);
        Graphs.addEdge(g, 1, 5, 1);
        Graphs.addEdge(g, 2, 3, 3);
        Graphs.addEdge(g, 2, 5, 1);
        Graphs.addEdge(g, 2, 7, 1);
        Graphs.addEdge(g, 3, 4, 2);
        Graphs.addEdge(g, 3, 5, 3);
        Graphs.addEdge(g, 3, 6, 1);
        Graphs.addEdge(g, 3, 7, 2);
        Graphs.addEdge(g, 4, 5, 1);
        Graphs.addEdge(g, 6, 7, 3);

        this.verifyClosedPath(g, 31, 15);
    }

    @Test
    public void testUndirectedGraph4()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Graphs.addEdge(g, 1, 2, 100);
        Graphs.addEdge(g, 1, 3, 150);
        Graphs.addEdge(g, 1, 4, 200);
        Graphs.addEdge(g, 2, 3, 120);
        Graphs.addEdge(g, 2, 5, 250);
        Graphs.addEdge(g, 3, 6, 200);
        Graphs.addEdge(g, 4, 5, 100);
        Graphs.addEdge(g, 4, 7, 80);
        Graphs.addEdge(g, 4, 8, 160);
        Graphs.addEdge(g, 5, 6, 100);
        Graphs.addEdge(g, 5, 7, 100);
        Graphs.addEdge(g, 6, 7, 150);
        Graphs.addEdge(g, 6, 10, 160);
        Graphs.addEdge(g, 7, 9, 100);
        Graphs.addEdge(g, 8, 9, 40);
        Graphs.addEdge(g, 9, 10, 80);

        this.verifyClosedPath(g, 2590, 20);
    }

    @Test
    public void testUndirectedGraph5()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(g, 1, 2, 1);
        Graphs.addEdge(g, 2, 3, 1);
        Graphs.addEdge(g, 3, 4, 1);
        Graphs.addEdge(g, 2, 5, 1);
        Graphs.addEdge(g, 3, 6, 1);

        this.verifyClosedPath(g, 10, 10);
    }

    // ---------------------Directed graph tests --------------------------

    @Test
    public void testDirectedGraphWithMultipleEdgesAndSelfLoop()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 1);
        Graphs.addEdge(g, 2, 3, 3);
        Graphs.addEdge(g, 2, 3, 20);
        Graphs.addEdge(g, 3, 4, 10);
        Graphs.addEdge(g, 4, 4, 5);
        Graphs.addEdge(g, 4, 1, 2);

        this.verifyClosedPath(g, 54, 9);
    }

    @Test
    public void testDirectedGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 1);
        Graphs.addEdge(g, 1, 4, 5);
        Graphs.addEdge(g, 2, 3, 2);
        Graphs.addEdge(g, 3, 1, 3);
        Graphs.addEdge(g, 4, 3, 4);

        this.verifyClosedPath(g, 18, 6);
    }

    @Test
    public void testDirectedGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 8);
        Graphs.addEdge(g, 1, 3, 3);
        Graphs.addEdge(g, 2, 3, 10);
        Graphs.addEdge(g, 2, 4, 5);
        Graphs.addEdge(g, 3, 4, 15);
        Graphs.addEdge(g, 4, 1, 4);

        this.verifyClosedPath(g, 76, 10);
    }

    @Test
    public void testDirectedGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 21);
        Graphs.addEdge(g, 2, 3, 8);
        Graphs.addEdge(g, 3, 1, 5);
        Graphs.addEdge(g, 3, 4, 20);
        Graphs.addEdge(g, 4, 2, 12);
        Graphs.addEdge(g, 4, 2, 2);

        this.verifyClosedPath(g, 104, 9);
    }

    @Test
    public void testDirectedGraph4()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(g, 1, 2, 10);
        Graphs.addEdge(g, 1, 3, 20);
        Graphs.addEdge(g, 2, 4, 50);
        Graphs.addEdge(g, 2, 5, 10);
        Graphs.addEdge(g, 3, 4, 20);
        Graphs.addEdge(g, 3, 5, 33);
        Graphs.addEdge(g, 4, 5, 5);
        Graphs.addEdge(g, 4, 6, 12);
        Graphs.addEdge(g, 5, 1, 12);
        Graphs.addEdge(g, 5, 6, 1);
        Graphs.addEdge(g, 6, 3, 22);

        this.verifyClosedPath(g, 276, 17);
    }

    @Test
    public void testDirectedGraph5()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        g.addEdge(1, 2);
        g.addEdge(1, 11);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 9);
        g.addEdge(5, 7);
        g.addEdge(6, 9);
        g.addEdge(7, 6);
        g.addEdge(8, 7);
        g.addEdge(9, 8);
        g.addEdge(9, 10);
        g.addEdge(10, 1);
        g.addEdge(11, 9);

        this.verifyClosedPath(g, 22, 22);
    }

    @Test
    public void temp()
    {
        ChinesePostman<Integer, DefaultEdge> alg = new ChinesePostman<>();
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        // Graph<Integer, DefaultEdge> g=new DefaultDirectedGraph<Integer,
        // DefaultEdge>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addEdge(0, 1);
        g.addEdge(1, 0);
        GraphPath<Integer, DefaultEdge> path = alg.getCPPSolution(g);
    }

    private <V,
        E> void verifyClosedPath(Graph<V, E> graph, double expectedWeight, int expectedLength)
    {

        ChinesePostman<V, E> alg = new ChinesePostman<>();
        GraphPath<V, E> path = alg.getCPPSolution(graph);

        Assert.assertEquals(expectedLength, path.getLength());
        Assert.assertEquals(expectedLength, path.getEdgeList().size());
        Assert.assertEquals(expectedWeight, path.getWeight(), 0.00000001);
        Assert.assertEquals(
            expectedWeight, path.getEdgeList().stream().mapToDouble(graph::getEdgeWeight).sum(),
            0.00000001);

        // all edges of the graph must be visited at least once
        Assert.assertTrue(path.getEdgeList().containsAll(graph.edgeSet()));

        Assert.assertTrue(graph.containsVertex(path.getStartVertex()));
        Assert.assertEquals(path.getStartVertex(), path.getEndVertex());

        // Verify that the path is an actual path in the graph
        Assert.assertEquals(path.getEdgeList().size() + 1, path.getVertexList().size());
        List<V> vertexList = path.getVertexList();
        List<E> edgeList = path.getEdgeList();

        // Check start and end vertex
        Assert.assertEquals(vertexList.get(0), path.getStartVertex());
        Assert.assertEquals(vertexList.get(vertexList.size() - 1), path.getEndVertex());

        // All vertices and edges in the path must be contained in the graph
        Assert.assertTrue(graph.vertexSet().containsAll(vertexList));
        Assert.assertTrue(graph.edgeSet().containsAll(edgeList));

        for (int i = 0; i < vertexList.size() - 1; i++) {
            V u = vertexList.get(i);
            V v = vertexList.get(i + 1);
            E edge = edgeList.get(i);

            if (graph.getType().isUndirected()) {
                Assert.assertEquals(Graphs.getOppositeVertex(graph, edge, u), v);
            } else { // Directed
                Assert.assertEquals(graph.getEdgeSource(edge), u);
                Assert.assertEquals(graph.getEdgeTarget(edge), v);
            }
        }
    }
}
