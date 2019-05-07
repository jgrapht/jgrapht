/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

import static org.jgrapht.graph.SerializationTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SerializationTest tests serialization and deserialization of JGraphT objects.
 *
 * @author John V. Sichi
 */
public class SerializationTest
{
    /**
     * Tests serialization of DirectedMultigraph.
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DirectedMultigraph()
        throws Exception
    {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, getVertexList());
        graph.addEdge(getV1(), getV2());
        graph.addEdge(getV2(), getV3());
        graph.addEdge(getV2(), getV3());

        graph = (DirectedMultigraph<String, DefaultEdge>) serializeAndDeserialize(graph);
        assertContainsAllVertices(graph, getVertexList());
        assertTrue(graph.containsEdge(getV1(), getV2()));
        assertTrue(graph.containsEdge(getV2(), getV3()));
        checkEdgesOf(graph, Arrays.asList(1, 3, 2), getVertexList());
    }

    /**
     * Tests serialization of DirectedAcyclicGraph
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DirectedAcyclicGraph()
        throws Exception
    {
        DirectedAcyclicGraph<String, DefaultEdge> graph1 =
            new DirectedAcyclicGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());
        graph1.addEdge(getV1(), getV2());
        graph1.addEdge(getV2(), getV3());
        graph1.addEdge(getV1(), getV3());

        DirectedAcyclicGraph<String, DefaultEdge> graph2 =
            (DirectedAcyclicGraph<String, DefaultEdge>) serializeAndDeserialize(graph1);
        assertContainsAllVertices(graph2, getVertexList());
        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV1(), getV3()));
        checkEdgesOf(graph2, Arrays.asList(2, 2, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

    /**
     * Tests serialization of DirectedPseudograph
     * <p>
     * directed, with self-loops, with multi-edges, un-weighted
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DirectedPseudograph()
        throws Exception
    {
        DirectedPseudograph<String, DefaultEdge> graph1 =
            new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());

        graph1.addEdge(getV1(), getV2());
        graph1.addEdge(getV1(), getV2()); // multi-edge

        graph1.addEdge(getV2(), getV3());
        graph1.addEdge(getV1(), getV1()); // self-loop
        graph1.addEdge(getV1(), getV3());

        DirectedPseudograph<String, DefaultEdge> graph2 =
            (DirectedPseudograph<String, DefaultEdge>) serializeAndDeserialize(graph1);
        assertContainsAllVertices(graph2, getVertexList());

        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV1(), getV3()));
        assertTrue(graph2.containsEdge(getV1(), getV1()));

        assertEquals(2, graph2.getAllEdges(getV1(), getV2()).size());
        checkEdgesOf(graph2, Arrays.asList(4, 3, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

    /**
     * Tests serialization of DefaultDirectedGraph.
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DefaultDirectedGraph()
        throws Exception
    {
        DefaultDirectedGraph<String, DefaultEdge> graph1 =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());
        graph1.addEdge(getV1(), getV2());
        graph1.addEdge(getV2(), getV3());
        graph1.addEdge(getV3(), getV1()); // contains loop

        DefaultDirectedGraph<String, DefaultEdge> graph2 =
            (DefaultDirectedGraph<String, DefaultEdge>) serializeAndDeserialize(graph1);

        assertContainsAllVertices(graph2, getVertexList());
        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV3(), getV1()));
        checkEdgesOf(graph2, Arrays.asList(2, 2, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

    /**
     * Tests serialization of DefaultUndirectedGraph
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DefaultUndirectedGraph()
        throws Exception
    {
        DefaultUndirectedGraph<String, DefaultEdge> graph1 =
            new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());
        graph1.addEdge(getV1(), getV2());
        graph1.addEdge(getV2(), getV3());
        graph1.addEdge(getV3(), getV1()); // contains loop

        DefaultUndirectedGraph<String, DefaultEdge> graph2 =
            (DefaultUndirectedGraph<String, DefaultEdge>) serializeAndDeserialize(graph1);

        assertContainsAllVertices(graph2, getVertexList());

        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV1()));

        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV3(), getV2()));

        assertTrue(graph2.containsEdge(getV3(), getV1()));
        assertTrue(graph2.containsEdge(getV1(), getV3()));

        checkEdgesOf(graph2, Arrays.asList(2, 2, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

    /**
     * Tests serialization of DefaultUndirectedWeightedGraph
     * <p>
     * undirected, weighted, self-loops allowed, no-multiple edges
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DefaultUndirectedWeightedGraph()
        throws Exception
    {
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph1 =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());
        DefaultWeightedEdge e12 = graph1.addEdge(getV1(), getV2());
        DefaultWeightedEdge e23 = graph1.addEdge(getV2(), getV3());
        DefaultWeightedEdge e31 = graph1.addEdge(getV3(), getV1());

        graph1.setEdgeWeight(e12, 1.0);
        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph2 =
            (DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge>) serializeAndDeserialize(
                graph1);

        assertContainsAllVertices(graph2, getVertexList());

        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV1()));

        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV3(), getV2()));

        assertTrue(graph2.containsEdge(getV3(), getV1()));
        assertTrue(graph2.containsEdge(getV1(), getV3()));

        assertEquals(
            1.0, graph2.getAllEdges(getV1(), getV2()).iterator().next().getWeight(), 0.00001);
        assertEquals(
            2.0, graph2.getAllEdges(getV3(), getV2()).iterator().next().getWeight(), 0.00001);
        assertEquals(
            3.0, graph2.getAllEdges(getV1(), getV3()).iterator().next().getWeight(), 0.00001);

        checkEdgesOf(graph2, Arrays.asList(2, 2, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

    /**
     * Tests serialization of DirectedWeightedMultigraph
     * <p>
     * weighted, with multiple edges, no self-loops
     */
    @SuppressWarnings("unchecked") @Test public void testSerialization_DirectedWeightedMultiGraph()
        throws Exception
    {
        DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph1 =
            new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph1, getVertexList());
        DefaultWeightedEdge e12a = graph1.addEdge(getV1(), getV2());
        DefaultWeightedEdge e12b = graph1.addEdge(getV1(), getV2());

        DefaultWeightedEdge e23 = graph1.addEdge(getV2(), getV3());
        DefaultWeightedEdge e31 = graph1.addEdge(getV3(), getV1());

        graph1.setEdgeWeight(e12a, 1.0);
        graph1.setEdgeWeight(e12b, 10.0);

        graph1.setEdgeWeight(e23, 2.0);
        graph1.setEdgeWeight(e31, 3.0);

        DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph2 =
            (DirectedWeightedMultigraph<String, DefaultWeightedEdge>) serializeAndDeserialize(
                graph1);

        assertContainsAllVertices(graph2, getVertexList());

        assertTrue(graph2.containsEdge(getV1(), getV2()));
        assertTrue(graph2.containsEdge(getV2(), getV3()));
        assertTrue(graph2.containsEdge(getV3(), getV1()));

        assertEquals(2, graph2.getAllEdges(getV1(), getV2()).size());

        assertEquals(new HashSet<>(Arrays.asList(1.0, 10.0)),
            graph2.getAllEdges(getV1(), getV2()).stream()
                .mapToDouble(DefaultWeightedEdge::getWeight).boxed().collect(Collectors.toSet()));

        assertEquals(
            1.0, graph2.getAllEdges(getV1(), getV2()).iterator().next().getWeight(), 0.00001);
        assertEquals(
            2.0, graph2.getAllEdges(getV2(), getV3()).iterator().next().getWeight(), 0.00001);
        assertEquals(
            3.0, graph2.getAllEdges(getV3(), getV1()).iterator().next().getWeight(), 0.00001);

        checkEdgesOf(graph2, Arrays.asList(3, 3, 2), getVertexList());

        assertEquals(graph1.toString(), graph2.toString());
    }

}
