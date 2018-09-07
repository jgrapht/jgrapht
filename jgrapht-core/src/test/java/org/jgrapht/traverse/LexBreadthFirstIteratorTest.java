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
import org.jgrapht.event.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link LexBreadthFirstIterator}
 *
 * @author Timofey Chudakov
 */
public class LexBreadthFirstIteratorTest
{

    /**
     * Tests basic properties of events fired by {@code LexBreadthFirstIterator}
     */
    @Test
    public void testEvents()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        LexBreadthFirstIterator<Integer, DefaultEdge> iterator =
            new LexBreadthFirstIterator<>(graph);
        MyTraversalListener<Integer, DefaultEdge> listener = new MyTraversalListener<>(graph);
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
    public void testLexicographicalBfsIterator1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        LexBreadthFirstIterator<Integer, DefaultEdge> iterator =
            new LexBreadthFirstIterator<>(graph);

        assertFalse(iterator.hasNext());

        iterator.next();
    }

    /**
     * Tests iterator for basic invariants.
     */
    @Test
    public void testLexicographicalBfsIterator2()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        LexBreadthFirstIterator<Integer, DefaultEdge> iterator =
            new LexBreadthFirstIterator<>(graph);
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
     * Tests iterator on disconnected graph.
     */
    @Test
    public void testLexicographicalBfsIterator3()
    {
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addVertex("d");
        LexBreadthFirstIterator<String, DefaultEdge> iterator =
            new LexBreadthFirstIterator<>(graph);

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
     * Tests iterator on pseudograph.
     */
    @Test
    public void testLexicographicalBfsIterator4()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 3);
        Graphs.addEdgeWithVertices(graph, 3, 3);
        LexBreadthFirstIterator<Integer, DefaultEdge> iterator =
            new LexBreadthFirstIterator<>(graph);
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

        assertTrue(graph.vertexSet().equals(returned));

        assertFalse(iterator.hasNext());
    }

    /**
     * TraversalListener for testing basic events invariants.
     */
    static class MyTraversalListener<V, E>
        extends
        TraversalListenerAdapter<V, E>
    {
        Set<V> verticesTraversed = new HashSet<>();
        Set<V> verticesFinished = new HashSet<>();
        Graph<V, E> graph;

        MyTraversalListener(Graph<V, E> graph)
        {
            this.graph = graph;
        }

        @Override
        public void vertexTraversed(VertexTraversalEvent<V> e)
        {
            assertTrue(graph.containsVertex(e.getVertex()));
            verticesTraversed.add(e.getVertex());
        }

        @Override
        public void vertexFinished(VertexTraversalEvent<V> e)
        {
            assertTrue(graph.containsVertex(e.getVertex()));
            verticesFinished.add(e.getVertex());
        }
    }
}
