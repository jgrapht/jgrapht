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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link MaximumCardinalityIterator}
 *
 * @author Timofey Chudakov
 */
public class MaximumCardinalityIteratorTest
{

    /**
     * Tests basic properties of events fired by {@code LexBreadthFirstIterator}.
     */
    @Test
    public void testEvents()
    {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, "a", "b");
        Graphs.addEdgeWithVertices(graph, "b", "c");
        Graphs.addEdgeWithVertices(graph, "c", "a");
        Graphs.addEdgeWithVertices(graph, "b", "d");
        LexBreadthFirstIteratorTest.MyTraversalListener<String, DefaultEdge> listener =
            new LexBreadthFirstIteratorTest.MyTraversalListener<>(graph);
        MaximumCardinalityIterator<String, DefaultEdge> iterator =
            new MaximumCardinalityIterator<>(graph);
        iterator.addTraversalListener(listener);
        for (int i = 0; i < 4; i++) {
            iterator.next();
        }
        assertEquals(graph.vertexSet(), listener.verticesTraversed);
        assertEquals(graph.vertexSet(), listener.verticesFinished);
    }

    /**
     * Tests iterator on empty graph.
     */
    @Test(expected = NoSuchElementException.class)
    public void testMaximumCardinalityIterator1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        MaximumCardinalityIterator<Integer, DefaultEdge> iterator =
            new MaximumCardinalityIterator<>(graph);

        assertFalse(iterator.hasNext());

        iterator.next();
    }

    /**
     * Tests iterator on basic invariants.
     */
    @Test
    public void testMaximumCardinalityIterator2()
    {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, "a", "b");
        Graphs.addEdgeWithVertices(graph, "b", "c");
        Graphs.addEdgeWithVertices(graph, "b", "d");
        Graphs.addEdgeWithVertices(graph, "c", "d");
        MaximumCardinalityIterator<String, DefaultEdge> iterator =
            new MaximumCardinalityIterator<>(graph);
        Set<String> returned = new HashSet<>();

        assertTrue(iterator.hasNext());
        String vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(graph.vertexSet().equals(returned));

        assertFalse(iterator.hasNext());
    }

    /**
     * Tests iterator on disconnected graph.
     */
    @Test
    public void testMaximumCardinalityIterator3()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        MaximumCardinalityIterator<Integer, DefaultEdge> iterator =
            new MaximumCardinalityIterator<>(graph);
        Set<Integer> returned = new HashSet<>();

        assertTrue(iterator.hasNext());
        Integer vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(graph.vertexSet().equals(returned));

        assertFalse(iterator.hasNext());
    }

    /**
     * Tests iterator on pseudograph.
     */
    @Test
    public void testMaximumCardinalityIterator4()
    {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, "a", "a");
        Graphs.addEdgeWithVertices(graph, "a", "b");
        Graphs.addEdgeWithVertices(graph, "a", "b");
        Graphs.addEdgeWithVertices(graph, "a", "c");
        Graphs.addEdgeWithVertices(graph, "a", "c");
        Graphs.addEdgeWithVertices(graph, "b", "c");
        Graphs.addEdgeWithVertices(graph, "b", "c");
        Graphs.addEdgeWithVertices(graph, "c", "c");
        Graphs.addEdgeWithVertices(graph, "c", "c");
        MaximumCardinalityIterator<String, DefaultEdge> iterator =
            new MaximumCardinalityIterator<>(graph);
        Set<String> returned = new HashSet<>();

        assertTrue(iterator.hasNext());
        String vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(iterator.hasNext());
        vertex = iterator.next();
        returned.add(vertex);
        assertTrue(graph.containsVertex(vertex));

        assertTrue(graph.vertexSet().equals(returned));

        assertFalse(iterator.hasNext());
    }

}
