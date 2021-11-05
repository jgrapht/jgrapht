/*
 * (C) Copyright 2003-2021, by John V Sichi and Contributors.
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
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.jgrapht.graph.SerializationTestUtils.serializeAndDeserialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SerializationTest tests serialization and deserialization of JGraphT objects.
 * <p>
 * The following classes are tested here:
 * <ul>
 * <li>{@link SimpleGraph}</li>
 * <li>{@link Multigraph}</li>
 * <li>{@link Pseudograph}</li>
 * <li>{@link DefaultUndirectedGraph}</li>
 *
 * <li>{@link SimpleWeightedGraph}</li>
 * <li>{@link WeightedMultigraph}</li>
 * <li>{@link WeightedPseudograph}</li>
 * <li>{@link DefaultUndirectedWeightedGraph}</li>
 *
 * <li>{@link SimpleDirectedGraph}</li>
 * <li>{@link DirectedMultigraph}</li>
 * <li>{@link DirectedPseudograph}</li>
 * <li>{@link DefaultDirectedGraph}</li>
 *
 * <li>{@link SimpleDirectedWeightedGraph}</li>
 * <li>{@link DirectedWeightedMultigraph}</li>
 * <li>{@link DirectedWeightedPseudograph}</li>
 * <li>{@link DefaultDirectedWeightedGraph}</li>
 * </ul>
 *
 * @author John V. Sichi
 */
public class SerializationTest
{
    private static final String V1 = "V1";
    private static final String V2 = "V2";
    private static final String V3 = "V3";
    private static final List<String> VERTEX_LIST = Arrays.asList(V1, V2, V3);
    private static final List<List<String>> VERTEX_PAIRS = Arrays
        .asList(
            Arrays.asList(V1, V2), Arrays.asList(V2, V1), Arrays.asList(V1, V3),
            Arrays.asList(V3, V1), Arrays.asList(V2, V3), Arrays.asList(V3, V2));

    public static <V, E> void assertContainsAllVertices(Graph<V, E> graph, List<V> vertices)
    {
        for (V v : vertices) {
            assertTrue(graph.containsVertex(v));
        }
    }

    public static <V, E> void checkEdgesOf(Graph<V, E> graph, List<Integer> edges, List<V> vertices)
    {
        if (edges.size() != vertices.size()) {
            throw new IllegalArgumentException(
                "the size of list of #edges and vertices should match");
        }
        for (int i = 0; i < edges.size(); i++) {
            assertEquals(edges.get(i).intValue(), graph.edgesOf(vertices.get(i)).size());
        }
    }

    public static <E> void assertAllEdges(Graph<String, E> graph1, Graph<String, E> graph2)
    {
        for (int i = 0; i < VERTEX_PAIRS.size(); i++) {
            String a = VERTEX_PAIRS.get(i).get(0);
            String b = VERTEX_PAIRS.get(i).get(1);
            assertEquals(graph1.getAllEdges(a, b).size(), graph2.getAllEdges(a, b).size());
            assertEquals(graph1.containsEdge(a, b), graph2.containsEdge(a, b));
        }
    }

    private static <E> void verifyBasic(
        Graph<String, E> graph1, Graph<String, E> graph2, List<Integer> numberOfEdges)
    {
        assertContainsAllVertices(graph2, VERTEX_LIST);
        assertContainsAllVertices(graph1, VERTEX_LIST);

        assertAllEdges(graph1, graph2);

        checkEdgesOf(graph2, numberOfEdges, VERTEX_LIST);
        checkEdgesOf(graph1, numberOfEdges, VERTEX_LIST);

        assertEquals(graph1.toString(), graph2.toString());
    }

    private static <E> void assertWeight(
        Graph<String, E> graph1, Graph<String, E> graph2, List<Double> weights, String vertex1,
        String vertex2)
    {
        assertWeight(graph1, weights, vertex1, vertex2);
        assertWeight(graph2, weights, vertex1, vertex2);
    }

    private static <E> void assertWeight(
        Graph<String, E> graph, List<Double> weights, String vertex1, String vertex2)
    {
        Set<E> edgeSet = graph.getAllEdges(vertex1, vertex2);
        for (E e : edgeSet)
            assertTrue(e instanceof DefaultWeightedEdge);
        assertEquals(
            new HashSet<>(weights),
            edgeSet
                .stream().map(e -> (DefaultWeightedEdge) e).map(DefaultWeightedEdge::getWeight)
                .collect(Collectors.toSet()));
    }

    /**
     * Tests serialization of {@link SimpleGraph}.
     * <p>
     * undirected no self-loop no multiple-edges unweighted
     */
    @Test
    public void testSimpleGraph()
        throws Exception
    {
        SimpleGraph<String, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V1, V3);

        SimpleGraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(2, 2, 2));
    }

    /**
     * Tests serialization of {@link Multigraph}. undirected no self-loop multiple-edges unweighted
     */
    @Test
    public void testMultiGraph()
        throws Exception
    {
        Multigraph<String, DefaultEdge> graph1 = new Multigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V1, V3);

        Multigraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 3, 2));
    }

    /**
     * Tests serialization of {@link Pseudograph}. undirected self-loop multiple-edges unweighted
     */
    @Test
    public void testPseudograph()
        throws Exception
    {
        Pseudograph<String, DefaultEdge> graph1 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V1, V2); // multiple edge
        graph1.addEdge(V1, V1); // self loop
        graph1.addEdge(V2, V3);
        graph1.addEdge(V1, V3);

        Pseudograph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(4, 3, 2));
    }

    /**
     * Tests serialization of {@link DefaultUndirectedGraph}
     * <p>
     * undirected self-loops no multiple edges unweighted
     */
    @Test
    public void testDefaultUndirectedGraph()
        throws Exception
    {
        DefaultUndirectedGraph<String, DefaultEdge> graph1 =
            new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V1, V1);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V3, V1);

        DefaultUndirectedGraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 2, 2));
    }

    /**
     * Tests serialization of {@link SimpleWeightedGraph}
     * <p>
     * undirected no self-loops no-multiple edges weighted
     */
    @Test
    public void testSimpleWeightedGraph()
        throws Exception
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph1 =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e12 = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e12, 1.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        SimpleWeightedGraph<String, DefaultWeightedEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(2, 2, 2));

        assertWeight(graph1, graph2, Arrays.asList(1.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V3, V2);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V1, V3);
    }

    /**
     * Tests serialization of {@link WeightedMultigraph}
     * <p>
     * undirected no self-loops multiple edges weighted
     */
    @Test
    public void testWeightedMultigraph()
        throws Exception
    {
        WeightedMultigraph<String, DefaultWeightedEdge> graph1 =
            new WeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e12a = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e12b = graph1.addEdge(V1, V2);

        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e12a, 1.0);
        graph1.setEdgeWeight(e12b, 10.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        WeightedMultigraph<String, DefaultWeightedEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 3, 2));

        assertEquals(2, graph1.getAllEdges(V1, V2).size());
        assertEquals(2, graph2.getAllEdges(V1, V2).size());

        assertWeight(graph1, graph2, Arrays.asList(1.0, 10.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V3, V2);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V1, V3);
    }

    /**
     * Tests serialization of {@link WeightedPseudograph}
     * <p>
     * undirected self-loops multiple edges weighted
     */
    @Test
    public void testWeightedPseudograph()
        throws Exception
    {
        WeightedPseudograph<String, DefaultWeightedEdge> graph1 =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e11 = graph1.addEdge(V1, V1);
        DefaultWeightedEdge e12a = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e12b = graph1.addEdge(V1, V2);

        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e11, 100.0);
        graph1.setEdgeWeight(e12a, 1.0);
        graph1.setEdgeWeight(e12b, 10.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        WeightedPseudograph<String, DefaultWeightedEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(4, 3, 2));

        assertEquals(2, graph1.getAllEdges(V1, V2).size());
        assertEquals(2, graph2.getAllEdges(V1, V2).size());

        assertWeight(graph1, graph2, Arrays.asList(100.0), V1, V1);
        assertWeight(graph1, graph2, Arrays.asList(1.0, 10.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V3, V2);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V1, V3);
    }

    /**
     * Tests serialization of {@link DefaultUndirectedWeightedGraph}
     * <p>
     * undirected self-loops no multiple edges weighted
     */
    @Test
    public void testDefaultUndirectedWeightedGraph()
        throws Exception
    {
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph1 =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e11 = graph1.addEdge(V1, V1);
        DefaultWeightedEdge e12 = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e11, 100.0);
        graph1.setEdgeWeight(e12, 1.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph2 =
            serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 2, 2));

        assertWeight(graph1, graph2, Arrays.asList(100.0), V1, V1);
        assertWeight(graph1, graph2, Arrays.asList(1.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V3, V2);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V1, V3);
    }

    /**
     * Tests serialization of {@link SimpleDirectedGraph} directed no self-loop no multiple-edges
     * unweighted
     */
    @Test
    public void testSimpleDirectedGraph()
        throws Exception
    {
        SimpleDirectedGraph<String, DefaultEdge> graph1 =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V1, V3);

        SimpleDirectedGraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(2, 2, 2));
    }

    /**
     * Tests serialization of {@link DirectedMultigraph}
     * <p>
     * directed no-self loops multiple edges unweighted
     */
    @Test
    public void testDirectedMultigraph()
        throws Exception
    {
        DirectedMultigraph<String, DefaultEdge> graph1 =
            new DirectedMultigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V2, V3);

        DirectedMultigraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(1, 3, 2));
    }

    /**
     * Tests serialization of {@link DirectedPseudograph}
     * <p>
     * directed self-loops multiple-edges unweighted
     */
    @Test
    public void testDirectedPseudograph()
        throws Exception
    {
        DirectedPseudograph<String, DefaultEdge> graph1 =
            new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);

        graph1.addEdge(V1, V2);
        graph1.addEdge(V1, V2); // multi-edge

        graph1.addEdge(V2, V3);
        graph1.addEdge(V1, V1); // self-loop
        graph1.addEdge(V1, V3);

        DirectedPseudograph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(4, 3, 2));
    }

    /**
     * Tests serialization of {@link DefaultDirectedGraph}
     * <p>
     * directed self-loops no multiple-edges unweighted
     */
    @Test
    public void testDefaultDirectedGraph()
        throws Exception
    {
        DefaultDirectedGraph<String, DefaultEdge> graph1 =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        graph1.addEdge(V1, V1);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V2, V3);
        graph1.addEdge(V3, V1);

        DefaultDirectedGraph<String, DefaultEdge> graph2 = serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 2, 2));
    }

    /**
     * Tests serialization of {@link SimpleDirectedWeightedGraph}
     * <p>
     * directed no self-loops no multiple edges weighted
     */
    @Test
    public void testSimpleDirectedWeightedGraph()
        throws Exception
    {
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph1 =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e12 = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e12, 1.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph2 =
            serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(2, 2, 2));

        assertWeight(graph1, graph2, Arrays.asList(1.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V2, V3);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V3, V1);
    }

    /**
     * Tests serialization of {@link DirectedWeightedMultigraph}
     * <p>
     * directed no self-loops multiple edges weighted
     */
    @Test
    public void testDirectedWeightedMultiGraph()
        throws Exception
    {
        DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph1 =
            new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);
        DefaultWeightedEdge e12a = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e12b = graph1.addEdge(V1, V2);

        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e12a, 1.0);
        graph1.setEdgeWeight(e12b, 10.0);

        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph2 =
            serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 3, 2));

        assertEquals(2, graph2.getAllEdges(V1, V2).size());
        assertEquals(2, graph1.getAllEdges(V1, V2).size());

        assertWeight(graph1, graph2, Arrays.asList(1.0, 10.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V2, V3);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V3, V1);
    }

    /**
     * Tests serialization of {@link DirectedWeightedPseudograph}
     * <p>
     * directed self-loops multiple edges weighted
     */
    @Test
    public void testDirectedWeightedPseudograph()
        throws Exception
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> graph1 =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);

        DefaultWeightedEdge e11 = graph1.addEdge(V1, V1);

        DefaultWeightedEdge e12a = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e12b = graph1.addEdge(V1, V2);

        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e11, 100.0);

        graph1.setEdgeWeight(e12a, 1.0);
        graph1.setEdgeWeight(e12b, 10.0);

        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DirectedWeightedPseudograph<String, DefaultWeightedEdge> graph2 =
            serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(4, 3, 2));

        assertEquals(2, graph2.getAllEdges(V1, V2).size());
        assertEquals(2, graph1.getAllEdges(V1, V2).size());

        assertWeight(graph1, graph2, Arrays.asList(100.0), V1, V1);
        assertWeight(graph1, graph2, Arrays.asList(1.0, 10.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V2, V3);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V3, V1);
    }

    /**
     * Tests serialization of {@link DefaultDirectedWeightedGraph}
     * <p>
     * directed self-loops no multiple edges weighted
     */
    @Test
    public void testDefaultDirectedWeightedGraph()
        throws Exception
    {
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph1 =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, VERTEX_LIST);

        DefaultWeightedEdge e11 = graph1.addEdge(V1, V1);
        DefaultWeightedEdge e12 = graph1.addEdge(V1, V2);
        DefaultWeightedEdge e23 = graph1.addEdge(V2, V3);
        DefaultWeightedEdge e31 = graph1.addEdge(V3, V1);

        graph1.setEdgeWeight(e11, 100.0);
        graph1.setEdgeWeight(e12, 1.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph2 =
            serializeAndDeserialize(graph1);

        verifyBasic(graph1, graph2, Arrays.asList(3, 2, 2));

        assertWeight(graph1, graph2, Arrays.asList(100.0), V1, V1);
        assertWeight(graph1, graph2, Arrays.asList(1.0), V1, V2);
        assertWeight(graph1, graph2, Arrays.asList(2.0), V2, V3);
        assertWeight(graph1, graph2, Arrays.asList(3.0), V3, V1);
    }

    /**
     * Test Serialization of {@link AsGraphUnion}
     *
     * @throws Exception
     */
    @Test
    public void testAsGraphUnion()
        throws Exception
    {
        Graph<String, DefaultEdge> graph1 = new DirectedPseudograph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> graph2 = new DirectedPseudograph<>(DefaultEdge.class);
        graph1.addVertex(V1);
        graph1.addVertex(V2);
        graph1.addVertex(V3);
        graph2.addVertex(V1);
        graph2.addVertex(V2);
        graph2.addVertex(V3);
        graph1.addEdge(V1, V2);
        graph1.addEdge(V1, V3);
        graph2.addEdge(V2, V3);
        AsGraphUnion<String, DefaultEdge> graph3 = new AsGraphUnion<>(graph1, graph2);
        AsGraphUnion<String, DefaultEdge> graph4 = serializeAndDeserialize(graph3);
        verifyBasic(graph3, graph4, Arrays.asList(2, 2, 2));
    }

}
