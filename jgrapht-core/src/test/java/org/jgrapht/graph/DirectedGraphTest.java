/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
import org.jgrapht.graph.specifics.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * A unit test for directed multigraph.
 *
 * @author Barak Naveh
 * @since Aug 9, 2003
 */
public class DirectedGraphTest
{
    // ~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testEdgeSetFactory()
    {
        DirectedMultigraph<String, DefaultEdge> g =
            new LinkedHashSetDirectedMultigraph<>(DefaultEdge.class);

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);

        DefaultEdge e1 = g.addEdge(v1, v2);
        DefaultEdge e2 = g.addEdge(v2, v1);
        DefaultEdge e3 = g.addEdge(v2, v3);
        DefaultEdge e4 = g.addEdge(v3, v1);

        Iterator<DefaultEdge> iter = g.edgeSet().iterator();
        assertEquals(e1, iter.next());
        assertEquals(e2, iter.next());
        assertEquals(e3, iter.next());
        assertEquals(e4, iter.next());
        assertFalse(iter.hasNext());

        assertEquals("([v1, v2, v3], [(v1,v2), (v2,v1), (v2,v3), (v3,v1)])", g.toString());
    }

    /**
     * .
     */
    @Test
    public void testEdgeOrderDeterminism()
    {
        Graph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);

        DefaultEdge e1 = g.addEdge(v1, v2);
        DefaultEdge e2 = g.addEdge(v2, v3);
        DefaultEdge e3 = g.addEdge(v3, v1);

        Iterator<DefaultEdge> iter = g.edgeSet().iterator();
        assertEquals(e1, iter.next());
        assertEquals(e2, iter.next());
        assertEquals(e3, iter.next());

        // some bonus tests
        assertTrue(Graphs.testIncidence(g, e1, v1));
        assertTrue(Graphs.testIncidence(g, e1, v2));
        assertFalse(Graphs.testIncidence(g, e1, v3));
        assertEquals(v2, Graphs.getOppositeVertex(g, e1, v1));
        assertEquals(v1, Graphs.getOppositeVertex(g, e1, v2));

        assertEquals("([v1, v2, v3], [(v1,v2), (v2,v3), (v3,v1)])", g.toString());
    }

    /**
     * .
     */
    @Test
    public void testEdgesOf()
    {
        Graph<String, DefaultEdge> g = createMultiTriangle();

        assertEquals(3, g.edgesOf(v1).size());
        assertEquals(3, g.edgesOf(v2).size());
        assertEquals(2, g.edgesOf(v3).size());
    }

    /**
     * .
     */
    @Test
    public void testInDegreeOf()
    {
        Graph<String, DefaultEdge> g = createMultiTriangle();

        assertEquals(2, g.inDegreeOf(v1));
        assertEquals(1, g.inDegreeOf(v2));
        assertEquals(1, g.inDegreeOf(v3));
    }

    /**
     * .
     */
    @Test
    public void testOutDegreeOf()
    {
        Graph<String, DefaultEdge> g = createMultiTriangle();

        assertEquals(1, g.outDegreeOf(v1));
        assertEquals(2, g.outDegreeOf(v2));
        assertEquals(1, g.outDegreeOf(v3));
    }

    /**
     * .
     */
    @Test
    public void testVertexOrderDeterminism()
    {
        Graph<String, DefaultEdge> g = createMultiTriangle();
        Iterator<String> iter = g.vertexSet().iterator();
        assertEquals(v1, iter.next());
        assertEquals(v2, iter.next());
        assertEquals(v3, iter.next());
    }

    private Graph<String, DefaultEdge> createMultiTriangle()
    {
        Graph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        initMultiTriangle(g);

        return g;
    }

    private void initMultiTriangle(Graph<String, DefaultEdge> g)
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);

        g.addEdge(v1, v2);
        g.addEdge(v2, v1);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
    }

    // ~ Inner Classes ----------------------------------------------------------

    /**
     * A graph implementation with an edge factory using linked hash sets.
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    private class LinkedHashSetDirectedMultigraph<V, E>
        extends
        DirectedMultigraph<V, E>
    {
        private static final long serialVersionUID = -1826738982402033648L;

        public LinkedHashSetDirectedMultigraph(Class<? extends E> edgeClass)
        {
            super(edgeClass);
        }

        @Override
        protected Specifics<V, E> createSpecifics(boolean directed)
        {
            return new FastLookupDirectedSpecifics<>(
                this, new LinkedHashMap<>(), v -> new LinkedHashSet<>());
        }
    }
}

// End DefaultGraphTest.java
