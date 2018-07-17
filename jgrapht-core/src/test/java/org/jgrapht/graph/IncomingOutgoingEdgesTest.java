/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.graph;

import org.jgrapht.*;
import org.junit.*;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Check Incoming/Outgoing edges in directed and undirected graphs.
 *
 * @author Dimitrios Michail
 */
public class IncomingOutgoingEdgesTest
{

    public static void testDirectedGraph(Supplier<Graph<String, DefaultEdge>> graphSupplier)
    {
        Graph<String, DefaultEdge> g = graphSupplier.get();
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        DefaultEdge e12 = g.addEdge("v1", "v2");
        DefaultEdge e23_1 = g.addEdge("v2", "v3");
        DefaultEdge e23_2 = g.addEdge("v2", "v3");
        DefaultEdge e24 = g.addEdge("v2", "v4");
        DefaultEdge e44 = g.addEdge("v4", "v4");
        DefaultEdge e55_1 = g.addEdge("v5", "v5");
        DefaultEdge e52 = g.addEdge("v5", "v2");
        DefaultEdge e55_2 = g.addEdge("v5", "v5");

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(5, g.degreeOf("v2"));
        assertEquals(2, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(5, g.degreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23_1, e23_2, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55_1, e55_2)), g.edgesOf("v5"));

        assertEquals(0, g.inDegreeOf("v1"));
        assertEquals(2, g.inDegreeOf("v2"));
        assertEquals(2, g.inDegreeOf("v3"));
        assertEquals(2, g.inDegreeOf("v4"));
        assertEquals(2, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(), g.incomingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e55_1, e55_2)), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(3, g.outDegreeOf("v2"));
        assertEquals(0, g.outDegreeOf("v3"));
        assertEquals(1, g.outDegreeOf("v4"));
        assertEquals(3, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2, e24)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55_1, e55_2)), g.outgoingEdgesOf("v5"));
    }

    /**
     * Test the most general version of the directed graph.
     */
    @Test
    public void testDirectedGraph()
    {
        testDirectedGraph(() -> new DirectedPseudograph<>(DefaultEdge.class));
    }

    /**
     * Test the most general version of the undirected graph.
     */
    public static void testUndirectedGraph(Supplier<Graph<String, DefaultEdge>> graphSupplier)
    {
        Graph<String, DefaultEdge> g = graphSupplier.get();
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        DefaultEdge e12 = g.addEdge("v1", "v2");
        DefaultEdge e23_1 = g.addEdge("v2", "v3");
        DefaultEdge e23_2 = g.addEdge("v2", "v3");
        DefaultEdge e24 = g.addEdge("v2", "v4");
        DefaultEdge e44 = g.addEdge("v4", "v4");
        DefaultEdge e55_1 = g.addEdge("v5", "v5");
        DefaultEdge e52 = g.addEdge("v5", "v2");
        DefaultEdge e55_2 = g.addEdge("v5", "v5");

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(5, g.degreeOf("v2"));
        assertEquals(2, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(5, g.degreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23_1, e23_2, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55_1, e55_2)), g.edgesOf("v5"));

        assertEquals(1, g.inDegreeOf("v1"));
        assertEquals(5, g.inDegreeOf("v2"));
        assertEquals(2, g.inDegreeOf("v3"));
        assertEquals(3, g.inDegreeOf("v4"));
        assertEquals(5, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.incomingEdgesOf("v1"));
        assertEquals(
            new HashSet<>(Arrays.asList(e12, e23_1, e23_2, e24, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55_1, e55_2)), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(5, g.outDegreeOf("v2"));
        assertEquals(2, g.outDegreeOf("v3"));
        assertEquals(3, g.outDegreeOf("v4"));
        assertEquals(5, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(
            new HashSet<>(Arrays.asList(e12, e23_1, e23_2, e24, e52)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23_1, e23_2)), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55_1, e55_2)), g.outgoingEdgesOf("v5"));
    }

    /**
     * Test the most general version of the undirected graph.
     */
    @Test
    public void testUndirectedGraph()
    {
        testUndirectedGraph(() -> new Pseudograph<>(DefaultEdge.class));
    }

}
