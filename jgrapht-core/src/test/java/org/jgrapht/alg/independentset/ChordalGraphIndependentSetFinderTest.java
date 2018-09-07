/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.independentset;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalGraphIndependentSetFinder}
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphIndependentSetFinderTest
{

    /**
     * Tests finding of maximum independent set of an empty graph
     */
    @Test
    public void testGetMaximumIndependentSet1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(0, set.size());
    }

    /**
     * Tests finding of maximum independent set on a clique.
     */
    @Test
    public void testGetMaximumIndependentSet2()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(1, set.size());
    }

    /**
     * Tests finding of a maximum independent set on a non-chordal graph
     */
    @Test
    public void testGetMaximumIndependentSet3()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNull(set);
    }

    /**
     * Tests finding of a maximum independent set on a pseudograph
     */
    @Test
    public void testGetMaximumIndependentSet4()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 3, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(2, set.size());
        assertIsIndependentSet(graph, set);
    }

    /**
     * Checks whether every two vertices from {@code set} aren't adjacent.
     *
     * @param graph the tested graph.
     * @param set the tested set of vertices.
     * @param <V> the graph vertex type.
     * @param <E> the graph edge type.
     */
    private <V, E> void assertIsIndependentSet(Graph<V, E> graph, Set<V> set)
    {
        ArrayList<V> vertices = new ArrayList<>(set);
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < i; j++) {
                assertFalse(graph.containsEdge(vertices.get(i), vertices.get(j)));
            }
        }
    }
}
