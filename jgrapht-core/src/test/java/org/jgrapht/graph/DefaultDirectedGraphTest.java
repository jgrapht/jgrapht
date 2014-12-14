/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------------------
 * DefaultDirectedGraphTest.java
 * -----------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 09-Aug-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;


/**
 * A unit test for directed multigraph.
 *
 * @author Barak Naveh
 * @since Aug 9, 2003
 */
public class DefaultDirectedGraphTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testEdgeSetFactory()
    {
        DirectedMultigraph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.setEdgeSetFactory(new LinkedHashSetFactory<String, DefaultEdge>());
        initMultiTriangle(g);
    }

    /**
     * .
     */
    public void testEdgeOrderDeterminism()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(
                DefaultEdge.class);
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

        assertEquals(
            "([v1, v2, v3], [(v1,v2), (v2,v3), (v3,v1)])",
            g.toString());
    }

    /**
     * .
     */
    public void testEdgesOf()
    {
        DirectedGraph<String, DefaultEdge> g =
            createMultiTriangle();

        assertEquals(3, g.edgesOf(v1).size());
        assertEquals(3, g.edgesOf(v2).size());
        assertEquals(2, g.edgesOf(v3).size());
    }

    /**
     * .
     */
    public void testInDegreeOf()
    {
        DirectedGraph<String, DefaultEdge> g =
            createMultiTriangle();

        assertEquals(2, g.inDegreeOf(v1));
        assertEquals(1, g.inDegreeOf(v2));
        assertEquals(1, g.inDegreeOf(v3));
    }

    /**
     * .
     */
    public void testOutDegreeOf()
    {
        DirectedGraph<String, DefaultEdge> g =
            createMultiTriangle();

        assertEquals(1, g.outDegreeOf(v1));
        assertEquals(2, g.outDegreeOf(v2));
        assertEquals(1, g.outDegreeOf(v3));
    }

    /**
     * .
     */
    public void testVertexOrderDeterminism()
    {
        DirectedGraph<String, DefaultEdge> g =
            createMultiTriangle();
        Iterator<String> iter = g.vertexSet().iterator();
        assertEquals(v1, iter.next());
        assertEquals(v2, iter.next());
        assertEquals(v3, iter.next());
    }

    private DirectedGraph<String, DefaultEdge>
    createMultiTriangle()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(
                DefaultEdge.class);
        initMultiTriangle(g);

        return g;
    }

    private void initMultiTriangle(
        DirectedGraph<String, DefaultEdge> g)
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);

        g.addEdge(v1, v2);
        g.addEdge(v2, v1);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class LinkedHashSetFactory<V, E>
        implements EdgeSetFactory<V, E>
    {
        /**
         * .
         *
         * @param vertex
         *
         * @return an empty list.
         */
        @Override
        public Set<E> createEdgeSet(V vertex)
        {
            return new LinkedHashSet<E>();
        }
    }
}

// End DefaultDirectedGraphTest.java
