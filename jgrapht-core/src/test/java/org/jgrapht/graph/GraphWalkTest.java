/*
 * (C) Copyright 2003-2023, by Barak Naveh and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.util.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Joris Kinable
 */
public class GraphWalkTest
{

    @Test
    public void testInvalidPath1()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
            graph.addVertex(0);
            graph.addEdge(0, 0);
            // Invalid: the path's edgeList should contain the edge (0,0)
            new GraphWalk<>(graph, 0, 0, Arrays.asList(0, 0), Collections.emptyList(), 0);
        });
    }

    @Test
    public void testInvalidPath2()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
            graph.addVertex(0);
            // Invalid: the path's vertexList and edgeList cannot both be empty
            new GraphWalk<>(graph, 0, 0, Collections.emptyList(), Collections.emptyList(), 0);
        });
    }

    @Test
    public void testInvalidPath3()
    {
        assertThrows(InvalidGraphWalkException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            graph.addVertex(0);
            // Invalid: The graph does not contain a self loop from 0 to 0.
            GraphWalk<Integer, DefaultEdge> gw =
                new GraphWalk<>(graph, 0, 0, Arrays.asList(0, 0), null, 0);
            gw.verify();
        });
    }

    @Test
    public void testInvalidPath4()
    {
        assertThrows(InvalidGraphWalkException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
            graph.addEdge(0, 1);
            graph.addEdge(1, 2);
            graph.addEdge(2, 3);

            // Invalid: The graph does not contain an edge from 1 to 3
            GraphWalk<Integer, DefaultEdge> gw =
                new GraphWalk<>(graph, 0, 2, Arrays.asList(0, 1, 3, 2), null, 0);
            gw.verify();
        });
    }

    @Test
    public void testInvalidPath5()
    {
        assertThrows(InvalidGraphWalkException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
            DefaultEdge e1 = graph.addEdge(0, 1);
            graph.addEdge(1, 2);
            DefaultEdge e3 = graph.addEdge(2, 3);

            // Invalid: the path jumps from vertex 1 to vertex 2 (edge (1,2) is skipped)
            GraphWalk<Integer, DefaultEdge> gw =
                new GraphWalk<>(graph, 0, 2, null, Arrays.asList(e1, e3), 0);
            gw.verify();
        });
    }

    @Test
    public void testValidPaths()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);

        // empty path
        GraphWalk<Integer, DefaultEdge> gw1 =
            new GraphWalk<>(graph, null, null, Collections.emptyList(), Collections.emptyList(), 0);
        gw1.verify();
        GraphWalk<Integer, DefaultEdge> gw2 =
            new GraphWalk<>(graph, null, null, null, Collections.emptyList(), 0);
        gw2.verify();
        GraphWalk<Integer, DefaultEdge> gw3 =
            new GraphWalk<>(graph, null, null, Collections.emptyList(), null, 0);
        gw3.verify();

        // singleton path
        GraphWalk<Integer, DefaultEdge> gw4 =
            new GraphWalk<>(graph, 0, 0, Collections.singletonList(0), Collections.emptyList(), 0);
        gw4.verify();
        GraphWalk<Integer, DefaultEdge> gw5 =
            new GraphWalk<>(graph, Collections.singletonList(0), 0);
        gw5.verify();

    }

    @Test
    public void testEmptyPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<GraphWalk<Integer, DefaultEdge>> paths = new ArrayList<>();
        paths.add(new GraphWalk<>(graph, null, null, Collections.emptyList(), 0));
        paths.add(new GraphWalk<>(graph, Collections.emptyList(), 0));
        for (GraphWalk<Integer, DefaultEdge> path : paths) {
            assertEquals(0, path.getLength());
            assertEquals(Collections.emptyList(), path.getVertexList());
            assertEquals(Collections.emptyList(), path.getEdgeList());
            assertTrue(path.isEmpty());
            assertEquals(GraphWalk.emptyWalk(graph), path);
        }
    }

    @Test
    public void testNonSimplePath()
    {
        CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<>(5);
        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        completeGraphGenerator.generateGraph(completeGraph);

        List<Integer> vertexList = Arrays.asList(0, 1, 2, 3, 2, 3, 4);
        List<DefaultEdge> edgeList = new ArrayList<>();
        for (int i = 0; i < vertexList.size() - 1; i++)
            edgeList.add(completeGraph.getEdge(vertexList.get(i), vertexList.get(i + 1)));
        GraphPath<Integer, DefaultEdge> p1 = new GraphWalk<>(completeGraph, 0, 4, edgeList, 10);
        assertEquals(0, p1.getStartVertex());
        assertEquals(4, p1.getEndVertex());
        assertEquals(vertexList, p1.getVertexList());
        assertEquals(edgeList.size(), p1.getLength());
        assertEquals(10.0, p1.getWeight(), 0.0000000001);

        GraphPath<Integer, DefaultEdge> p2 = new GraphWalk<>(completeGraph, vertexList, 10);
        assertEquals(0, p2.getStartVertex());
        assertEquals(4, p2.getEndVertex());
        assertEquals(edgeList, p2.getEdgeList());
        assertEquals(edgeList.size(), p2.getLength());
        assertEquals(10.0, p2.getWeight(), 0.0000000001);
    }

    @Test
    public void testReversePathUndirected()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        DefaultWeightedEdge e1 = Graphs.addEdge(graph, 0, 1, 2);
        DefaultWeightedEdge e2 = Graphs.addEdge(graph, 1, 2, 3);
        DefaultWeightedEdge e3 = Graphs.addEdge(graph, 2, 3, 4);

        GraphWalk<Integer, DefaultWeightedEdge> gw1 =
            new GraphWalk<>(graph, 0, 3, Arrays.asList(0, 1, 2, 3), null, 9);
        GraphWalk<Integer, DefaultWeightedEdge> gw2 =
            new GraphWalk<>(graph, 0, 3, null, Arrays.asList(e1, e2, e3), 9);

        GraphWalk<Integer, DefaultWeightedEdge> rev1 = gw1.reverse(gw -> gw1.getWeight());
        rev1.verify();
        GraphWalk<Integer, DefaultWeightedEdge> rev2 = gw2.reverse(gw -> gw2.getWeight());
        rev2.verify();

        GraphWalk<Integer, DefaultWeightedEdge> revPath =
            new GraphWalk<>(graph, 3, 0, null, Arrays.asList(e3, e2, e1), 9);
        assertEquals(revPath, rev1);
        assertEquals(revPath, rev2);

        rev1 = gw1.reverse();
        assertEquals(9.0, gw1.getWeight(), 0.0000000001);
        rev2 = gw2.reverse();
        assertEquals(9.0, gw2.getWeight(), 0.0000000001);
    }

    @Test
    public void testReverseInvalidPathDirected()
    {
        assertThrows(InvalidGraphWalkException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
            Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
            graph.addEdge(0, 1);
            graph.addEdge(1, 2);
            graph.addEdge(2, 3);

            GraphWalk<Integer, DefaultEdge> gw1 =
                new GraphWalk<>(graph, 0, 3, Arrays.asList(0, 1, 2, 3), null, 0);
            // Walk cannot be reversed since reverse arcs do not exist
            gw1.reverse(gw -> gw.edgeList.stream().mapToDouble(gw.graph::getEdgeWeight).sum());
        });
    }

    @Test
    public void testReversePathDirected()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        Graphs.addEdge(graph, 0, 1, 1);
        Graphs.addEdge(graph, 1, 2, 2);
        Graphs.addEdge(graph, 2, 3, 3);

        DefaultWeightedEdge e1 = Graphs.addEdge(graph, 3, 2, 4);
        DefaultWeightedEdge e2 = Graphs.addEdge(graph, 2, 1, 5);
        DefaultWeightedEdge e3 = Graphs.addEdge(graph, 1, 0, 6);

        GraphWalk<Integer, DefaultWeightedEdge> gw1 =
            new GraphWalk<>(graph, 0, 3, Arrays.asList(0, 1, 2, 3), null, 0);
        GraphWalk<Integer, DefaultWeightedEdge> rev1 =
            gw1.reverse(gw -> gw.getEdgeList().stream().mapToDouble(gw.graph::getEdgeWeight).sum());
        rev1.verify();
        GraphWalk<Integer, DefaultWeightedEdge> revPath =
            new GraphWalk<>(graph, 3, 0, null, Arrays.asList(e1, e2, e3), 15);

        assertEquals(revPath, rev1);
        assertEquals(15, rev1.getWeight(), 0.00000001);

        GraphWalk<Integer, DefaultWeightedEdge> rev2 = gw1.reverse();
        assertEquals(15, rev2.getWeight(), 0.00000001);
    }

    /**
     * Cannot extend empty path
     */
    @Test
    public void testIllegalConcatPath1()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
            graph.addVertex(0);
            GraphWalk<Integer, DefaultEdge> gw1 = GraphWalk.emptyWalk(graph);
            GraphWalk<Integer, DefaultEdge> gw2 = GraphWalk.singletonWalk(graph, 0, 10);
            gw1.concat(gw2, gw -> gw1.getWeight() + gw2.getWeight());
        });
    }

    /**
     * Cannot concat two paths which do not end/start at the same vertex
     */
    @Test
    public void testIllegalConcatPath2()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
            graph.addVertex(0);
            graph.addVertex(1);
            GraphWalk<Integer, DefaultEdge> gw1 = GraphWalk.singletonWalk(graph, 0, 10);
            GraphWalk<Integer, DefaultEdge> gw2 = GraphWalk.singletonWalk(graph, 1, 12);
            gw1.concat(gw2, gw -> gw1.getWeight() + gw2.getWeight());
        });
    }

    @Test
    public void testConcatPath1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        DefaultEdge e3 = graph.addEdge(2, 3);
        DefaultEdge e4 = graph.addEdge(3, 1);
        GraphWalk<Integer, DefaultEdge> gw1 =
            new GraphWalk<>(graph, 0, 2, Arrays.asList(0, 1, 2), null, 5);
        GraphWalk<Integer, DefaultEdge> gw2 =
            new GraphWalk<>(graph, 2, 1, null, Arrays.asList(e3, e4), 7);
        GraphWalk<Integer, DefaultEdge> gw3 =
            gw1.concat(gw2, gw -> gw1.getWeight() + gw2.getWeight());
        gw3.verify();

        GraphWalk<Integer, DefaultEdge> expected =
            new GraphWalk<>(graph, 0, 1, Arrays.asList(0, 1, 2, 3, 1), null, 12);
        assertEquals(expected, gw3);
        assertEquals(12, gw3.getWeight(), 0.00000001);
    }

    @Test
    public void testConcatPathWithSingleton()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1));
        graph.addEdge(0, 1);
        GraphWalk<Integer, DefaultEdge> gw1 =
            new GraphWalk<>(graph, 0, 1, Arrays.asList(0, 1), null, 5);
        GraphWalk<Integer, DefaultEdge> gw2 = GraphWalk.singletonWalk(graph, 1, 10);
        GraphWalk<Integer, DefaultEdge> gw3 =
            gw1.concat(gw2, gw -> gw1.getWeight() + gw2.getWeight());
        gw3.verify();
        // Concatenation with singleton shouldn't result in a different path.
        assertEquals(gw1, gw3);
    }

    @Test
    public void testFirstEmptyWalkEquality()
    {
        Graph<Integer, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
        GraphWalk<Integer, DefaultEdge> gw1 = GraphWalk.emptyWalk(graph1);

        Graph<Integer, DefaultEdge> graph2 = new SimpleGraph<>(DefaultEdge.class);
        graph2.addVertex(0);
        GraphWalk<Integer, DefaultEdge> gw2 = GraphWalk.singletonWalk(graph2, 0);
        assertNotEquals(gw1, gw2);
    }

}
