/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph.builder;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for the graph type builder.
 * 
 * @author Dimitrios Michail
 */
public class GraphTypeBuilderTest
{

    @Test
    public void testGraphTypeBuilder()
    {
        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .<Integer, DefaultEdge> directed().allowingMultipleEdges(true)
                .allowingSelfLoops(true).edgeClass(DefaultEdge.class).buildGraph();
        assertTrue(graph.getType().isDirected());
        assertTrue(graph.getType().isAllowingMultipleEdges());
        assertTrue(graph.getType().isAllowingSelfLoops());
        assertNotNull(graph.getEdgeSupplier());
        assertNull(graph.getVertexSupplier());
    }

    @Test
    public void testGraphTypeBuilderWithEdgeSupplier()
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true)
                .edgeSupplier(() -> new DefaultWeightedEdge())
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        assertTrue(graph.getType().isDirected());
        assertTrue(graph.getType().isAllowingMultipleEdges());
        assertTrue(graph.getType().isAllowingSelfLoops());
        assertNotNull(graph.getEdgeSupplier());
        assertNotNull(graph.getVertexSupplier());
    }

    @Test
    public void testGraphTypeBuilderWithVertexClass()
    {
        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexClass(Integer.class).edgeClass(DefaultEdge.class).buildGraph();
        assertTrue(graph.getType().isDirected());
        assertTrue(graph.getType().isAllowingMultipleEdges());
        assertTrue(graph.getType().isAllowingSelfLoops());
        assertNotNull(graph.getEdgeSupplier());
        assertNotNull(graph.getVertexSupplier());
    }

    @Test
    public void testGraphTypeBuilderUndirected()
    {
        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .<Integer, DefaultEdge> undirected().allowingMultipleEdges(true)
                .allowingSelfLoops(false).edgeClass(DefaultEdge.class).buildGraph();
        assertTrue(graph.getType().isUndirected());
        assertTrue(graph.getType().isAllowingMultipleEdges());
        assertFalse(graph.getType().isAllowingSelfLoops());
        assertNotNull(graph.getEdgeSupplier());
    }

    @Test
    public void testGraphTypeBuilderFromGraph()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graph<Integer, DefaultEdge> graph1 = GraphTypeBuilder.forGraph(graph).buildGraph();

        assertTrue(graph1.getType().isUndirected());
        assertTrue(graph1.getType().isAllowingMultipleEdges());
        assertTrue(graph1.getType().isAllowingSelfLoops());
        assertNotNull(graph1.getEdgeSupplier());
        assertEquals(graph.getEdgeSupplier(), graph1.getEdgeSupplier());
    }

}
