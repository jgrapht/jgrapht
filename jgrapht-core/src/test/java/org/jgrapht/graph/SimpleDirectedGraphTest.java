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
/* ----------------------------
 * SimpleDirectedGraphTest.java
 * ----------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 25-Jul-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;


/**
 * A unit test for simple directed graph.
 *
 * @author Barak Naveh
 * @since Jul 25, 2003
 */
public class SimpleDirectedGraphTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    DirectedGraph<String, DefaultEdge> gEmpty;
    private DirectedGraph<String, DefaultEdge> g1;
    private DirectedGraph<String, DefaultEdge> g2;
    private DirectedGraph<String, DefaultEdge> g3;
    private DirectedGraph<String, DefaultEdge> g4;
    private DefaultEdge eLoop;
    private EdgeFactory<String, DefaultEdge> eFactory;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public SimpleDirectedGraphTest(String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Class to test for boolean addEdge(V, V, E)
     */
    public void testAddEdgeEdge()
    {
        init();

        try {
            g1.addEdge(v1, v1, eLoop); // loops not allowed
            assertFalse();
        } catch (IllegalArgumentException e) {
            assertTrue();
        }

        try {
            g3.addEdge(v1, v1, null);
            assertFalse(); // NPE
        } catch (NullPointerException e) {
            assertTrue();
        }

        DefaultEdge e = eFactory.createEdge(v2, v1);

        try {
            g1.addEdge("ya", "ya", e); // no such vertex in graph
            assertFalse();
        } catch (IllegalArgumentException ile) {
            assertTrue();
        }

        assertEquals(false, g2.addEdge(v2, v1, e));
        assertEquals(false, g3.addEdge(v2, v1, e));
        assertEquals(true, g4.addEdge(v2, v1, e));
    }

    /**
     * Class to test for Edge addEdge(Object, Object)
     */
    public void testAddEdgeObjectObject()
    {
        init();

        try {
            g1.addEdge(v1, v1); // loops not allowed
            assertFalse();
        } catch (IllegalArgumentException e) {
            assertTrue();
        }

        try {
            g3.addEdge(null, null);
            assertFalse(); // NPE
        } catch (NullPointerException e) {
            assertTrue();
        }

        try {
            g1.addEdge(v2, v1); // no such vertex in graph
            assertFalse();
        } catch (IllegalArgumentException ile) {
            assertTrue();
        }

        assertNull(g2.addEdge(v2, v1));
        assertNull(g3.addEdge(v2, v1));
        assertNotNull(g4.addEdge(v2, v1));
    }

    /**
     * .
     */
    public void testAddVertex()
    {
        init();

        assertEquals(1, g1.vertexSet().size());
        assertEquals(2, g2.vertexSet().size());
        assertEquals(3, g3.vertexSet().size());
        assertEquals(4, g4.vertexSet().size());

        assertFalse(g1.addVertex(v1));
        assertTrue(g1.addVertex(v2));
        assertEquals(2, g1.vertexSet().size());
    }

    /**
     * Class to test for boolean containsEdge(Edge)
     */
    public void testContainsEdgeEdge()
    {
        init();

        // TODO Implement containsEdge().
    }

    /**
     * Class to test for boolean containsEdge(Object, Object)
     */
    public void testContainsEdgeObjectObject()
    {
        init();

        assertFalse(g1.containsEdge(v1, v2));
        assertFalse(g1.containsEdge(v1, v1));

        assertTrue(g2.containsEdge(v1, v2));
        assertTrue(g2.containsEdge(v2, v1));

        assertTrue(g3.containsEdge(v1, v2));
        assertTrue(g3.containsEdge(v2, v1));
        assertTrue(g3.containsEdge(v3, v2));
        assertTrue(g3.containsEdge(v2, v3));
        assertTrue(g3.containsEdge(v1, v3));
        assertTrue(g3.containsEdge(v3, v1));

        assertFalse(g4.containsEdge(v1, v4));
        g4.addEdge(v1, v4);
        assertTrue(g4.containsEdge(v1, v4));

        assertFalse(g3.containsEdge(v4, v2));
        assertFalse(g3.containsEdge(null, null));
    }

    /**
     * .
     */
    public void testContainsVertex()
    {
        init();

        // TODO Implement containsVertex().
    }

    /**
     * .
     */
    public void testEdgeSet()
    {
        init();

        // TODO Implement edgeSet().
    }

    /**
     * .
     */
    public void testEdgesOf()
    {
        init();

        assertEquals(g4.edgesOf(v1).size(), 2);
        assertEquals(g3.edgesOf(v1).size(), 4);

        Iterator<DefaultEdge> iter = g3.edgesOf(v1).iterator();
        int count = 0;

        while (iter.hasNext()) {
            iter.next();
            count++;
        }

        assertEquals(count, 4);
    }

    /**
     * .
     */
    public void testGetAllEdges()
    {
        init(); // TODO Implement getAllEdges().
    }

    /**
     * .
     */
    public void testGetEdge()
    {
        init(); // TODO Implement getEdge().
    }

    /**
     * .
     */
    public void testGetEdgeFactory()
    {
        init(); // TODO Implement getEdgeFactory().
    }

    /**
     * .
     */
    public void testInDegreeOf()
    {
        init();

        assertEquals(0, g1.inDegreeOf(v1));

        assertEquals(1, g2.inDegreeOf(v1));
        assertEquals(1, g2.inDegreeOf(v2));

        assertEquals(2, g3.inDegreeOf(v1));
        assertEquals(2, g3.inDegreeOf(v2));
        assertEquals(2, g3.inDegreeOf(v3));

        assertEquals(1, g4.inDegreeOf(v1));
        assertEquals(1, g4.inDegreeOf(v2));
        assertEquals(1, g4.inDegreeOf(v3));
        assertEquals(1, g4.inDegreeOf(v4));

        try {
            g3.inDegreeOf(new String());
            assertFalse();
        } catch (IllegalArgumentException e) {
            assertTrue();
        }

        try {
            g3.inDegreeOf(null);
            assertFalse();
        } catch (NullPointerException e) {
            assertTrue();
        }
    }

    /**
     * .
     */
    public void testIncomingOutgoingEdgesOf()
    {
        init();

        Set<DefaultEdge> e1to2 = g2.outgoingEdgesOf(v1);
        Set<DefaultEdge> e2from1 = g2.incomingEdgesOf(v2);
        assertEquals(e1to2, e2from1);
    }

    /**
     * .
     */
    public void testOutDegreeOf()
    {
        init(); // TODO Implement outDegreeOf().
    }

    /**
     * .
     */
    public void testOutgoingEdgesOf()
    {
        init(); // TODO Implement outgoingEdgesOf().
    }

    /**
     * Class to test for boolean removeEdge(Edge)
     */
    public void testRemoveEdgeEdge()
    {
        init();

        assertEquals(g4.edgeSet().size(), 4);
        g4.removeEdge(v1, v2);
        assertEquals(g4.edgeSet().size(), 3);
        assertFalse(g4.removeEdge(eLoop));
        assertTrue(g4.removeEdge(g4.getEdge(v2, v3)));
        assertEquals(g4.edgeSet().size(), 2);
    }

    /**
     * Class to test for Edge removeEdge(Object, Object)
     */
    public void testRemoveEdgeObjectObject()
    {
        init(); // TODO Implement removeEdge().
    }
    
    public void testRemoveAllEdgesObjectObject()
    {
        init();
        
        assertEquals(2, g2.edgeSet().size());
        assertTrue(g2.containsEdge(v1, v2));
        Set<DefaultEdge> edges = g2.getAllEdges(v1, v2);
        assertEquals(edges, g2.removeAllEdges(v1, v2));
        assertEquals(1, g2.edgeSet().size());
        assertFalse(g2.containsEdge(v1, v2));
        
        assertEquals(4, g4.edgeSet().size());
        edges = g4.getAllEdges(v3, v4);
        assertEquals(edges, g4.removeAllEdges(v3, v4));
        assertEquals(3, g4.edgeSet().size());
        assertFalse(g4.containsEdge(v3, v4));
        // No edge to remove.
        assertEquals(Collections.emptySet(), g4.removeAllEdges(v3, v2));
        assertEquals(3, g4.edgeSet().size());
        // Missing vertex.
        assertEquals(null, g4.removeAllEdges(v1, "v5"));
    }

    /**
     * .
     */
    public void testRemoveVertex()
    {
        init();
        assertEquals(4, g4.vertexSet().size());
        assertTrue(g4.removeVertex(v1));
        assertEquals(3, g4.vertexSet().size());

        assertEquals(2, g4.edgeSet().size());
        assertFalse(g4.removeVertex(v1));
        assertTrue(g4.removeVertex(v2));
        assertEquals(1, g4.edgeSet().size());
        assertTrue(g4.removeVertex(v3));
        assertEquals(0, g4.edgeSet().size());
        assertEquals(1, g4.vertexSet().size());
        assertTrue(g4.removeVertex(v4));
        assertEquals(0, g4.vertexSet().size());
    }

    /**
     * .
     */
    public void testVertexSet()
    {
        init(); // TODO Implement vertexSet().
    }

    public void testReversedView()
    {
        init();

        DirectedGraph<String, DefaultEdge> g =
            new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        DirectedGraph<String, DefaultEdge> r =
            new EdgeReversedGraph<String, DefaultEdge>(g);

        g.addVertex(v1);
        g.addVertex(v2);
        DefaultEdge e = g.addEdge(v1, v2);

        verifyReversal(g, r, e);

        // We have implicitly verified that r is backed by g for additive
        // operations (since we constructed it before adding anything to g).
        // Now verify for deletion.

        g.removeEdge(e);

        assertTrue(r.edgeSet().isEmpty());
        assertEquals(0, r.inDegreeOf(v1));
        assertEquals(0, r.outDegreeOf(v1));
        assertEquals(0, r.inDegreeOf(v2));
        assertEquals(0, r.outDegreeOf(v2));
        assertTrue(r.incomingEdgesOf(v1).isEmpty());
        assertTrue(r.outgoingEdgesOf(v1).isEmpty());
        assertTrue(r.incomingEdgesOf(v2).isEmpty());
        assertTrue(r.outgoingEdgesOf(v2).isEmpty());
    }

    private void verifyReversal(
        DirectedGraph<String, DefaultEdge> g,
        DirectedGraph<String, DefaultEdge> r,
        DefaultEdge e)
    {
        assertTrue(r.containsVertex(v1));
        assertTrue(r.containsVertex(v2));

        assertEquals(g.vertexSet(), r.vertexSet());
        assertEquals(g.edgeSet(), r.edgeSet());

        assertTrue(r.containsEdge(v2, v1));
        assertSame(e, r.getEdge(v2, v1));
        assertFalse(r.containsEdge(v1, v2));
        assertNull(r.getEdge(v1, v2));

        Set<DefaultEdge> s = r.getAllEdges(v1, v2);
        assertEquals(0, s.size());

        s = r.getAllEdges(v2, v1);
        assertEquals(1, s.size());
        assertSame(e, s.iterator().next());

        assertEquals(1, r.inDegreeOf(v1));
        assertEquals(0, r.inDegreeOf(v2));
        assertEquals(0, r.outDegreeOf(v1));
        assertEquals(1, r.outDegreeOf(v2));

        assertEquals(g.edgeSet(), r.incomingEdgesOf(v1));
        assertTrue(r.outgoingEdgesOf(v1).isEmpty());
        assertTrue(r.incomingEdgesOf(v2).isEmpty());
        assertEquals(g.edgeSet(), r.outgoingEdgesOf(v2));

        assertSame(v2, r.getEdgeSource(e));
        assertSame(v1, r.getEdgeTarget(e));

        assertEquals("([v1, v2], [(v2,v1)])", r.toString());
    }

    private void init()
    {
        gEmpty =
            new SimpleDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1 = new SimpleDirectedGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g2 = new SimpleDirectedGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g3 = new SimpleDirectedGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g4 = new SimpleDirectedGraph<String, DefaultEdge>(
            DefaultEdge.class);

        eFactory = g1.getEdgeFactory();
        eLoop = eFactory.createEdge(v1, v1);

        g1.addVertex(v1);

        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addEdge(v1, v2);
        g2.addEdge(v2, v1);

        g3.addVertex(v1);
        g3.addVertex(v2);
        g3.addVertex(v3);
        g3.addEdge(v1, v2);
        g3.addEdge(v2, v1);
        g3.addEdge(v2, v3);
        g3.addEdge(v3, v2);
        g3.addEdge(v3, v1);
        g3.addEdge(v1, v3);

        g4.addVertex(v1);
        g4.addVertex(v2);
        g4.addVertex(v3);
        g4.addVertex(v4);
        g4.addEdge(v1, v2);
        g4.addEdge(v2, v3);
        g4.addEdge(v3, v4);
        g4.addEdge(v4, v1);
    }
}

// End SimpleDirectedGraphTest.java
