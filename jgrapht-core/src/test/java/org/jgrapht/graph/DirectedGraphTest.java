/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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

import static org.junit.Assert.*;

/**
 * A unit test for directed multigraph.
 *
 * @author Barak Naveh
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
    }
}
