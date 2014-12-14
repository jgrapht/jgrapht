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
/* ------------------------
 * ListenableGraphTest.java
 * ------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Aug-2003 : Initial revision (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 *
 */
package org.jgrapht.graph;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.event.*;


/**
 * Unit test for {@link ListenableGraph} class.
 *
 * @author Barak Naveh
 * @since Aug 3, 2003
 */
public class ListenableGraphTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    DefaultEdge lastAddedEdge;
    DefaultEdge lastRemovedEdge;
    Object lastAddedVertex;
    Object lastRemovedVertex;

    //~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public ListenableGraphTest(String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Tests GraphListener listener.
     */
    public void testGraphListener()
    {
        init();

        ListenableGraph<Object, DefaultEdge> g =
            new ListenableUndirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        GraphListener<Object, DefaultEdge> listener =
            new MyGraphListner<DefaultEdge>();
        g.addGraphListener(listener);

        String v1 = "v1";
        String v2 = "v2";

        // test vertex notification
        g.addVertex(v1);
        assertEquals(v1, lastAddedVertex);
        assertEquals(null, lastRemovedVertex);

        init();
        g.removeVertex(v1);
        assertEquals(v1, lastRemovedVertex);
        assertEquals(null, lastAddedVertex);

        // test edge notification
        g.addVertex(v1);
        g.addVertex(v2);

        init();

        DefaultEdge e = g.addEdge(v1, v2);
        assertEquals(e, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);

        init();
        assertTrue(g.removeEdge(e));
        assertEquals(e, lastRemovedEdge);
        assertEquals(null, lastAddedEdge);

        g.removeVertex(v1);
        g.removeVertex(v2);

        //
        // test notification stops when removing listener
        //
        g.removeGraphListener(listener);
        init();
        g.addVertex(v1);
        g.addVertex(v2);
        e = g.addEdge(v1, v2);
        g.removeEdge(e);

        assertEquals(null, lastAddedEdge);
        assertEquals(null, lastAddedVertex);
        assertEquals(null, lastRemovedEdge);
        assertEquals(null, lastRemovedVertex);
    }

    /**
     * Tests VertexSetListener listener.
     */
    public void testVertexSetListener()
    {
        init();

        ListenableGraph<Object, DefaultEdge> g =
            new ListenableUndirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        VertexSetListener<Object> listener =
            new MyGraphListner<DefaultEdge>();
        g.addVertexSetListener(listener);

        String v1 = "v1";
        String v2 = "v2";

        // test vertex notification
        g.addVertex(v1);
        assertEquals(v1, lastAddedVertex);
        assertEquals(null, lastRemovedVertex);

        init();
        g.removeVertex(v1);
        assertEquals(v1, lastRemovedVertex);
        assertEquals(null, lastAddedVertex);

        // test edge notification
        g.addVertex(v1);
        g.addVertex(v2);

        init();

        DefaultEdge e = g.addEdge(v1, v2);
        assertEquals(null, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);

        init();
        assertTrue(g.removeEdge(e));
        assertEquals(null, lastRemovedEdge);
        assertEquals(null, lastAddedEdge);

        g.removeVertex(v1);
        g.removeVertex(v2);

        //
        // test notification stops when removing listener
        //
        g.removeVertexSetListener(listener);
        init();
        g.addVertex(v1);
        g.addVertex(v2);
        e = g.addEdge(v1, v2);
        g.removeEdge(e);

        assertEquals(null, lastAddedEdge);
        assertEquals(null, lastAddedVertex);
        assertEquals(null, lastRemovedEdge);
        assertEquals(null, lastRemovedVertex);
    }

    /**
     * Tests that the combination of weights plus listener works.
     */
    public void testListenableDirectedWeightedGraph()
    {
        init();
        
        ListenableDirectedWeightedGraph<Object, DefaultWeightedEdge> g =
            new ListenableDirectedWeightedGraph<Object, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        GraphListener<Object, DefaultWeightedEdge> listener =
            new MyGraphListner<DefaultWeightedEdge>();
        g.addGraphListener(listener);

        String v1 = "v1";
        String v2 = "v2";

        g.addVertex(v1);
        assertEquals(v1, lastAddedVertex);
        assertEquals(null, lastRemovedVertex);

        g.addVertex(v2);

        init();

        DefaultWeightedEdge e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 10.0);
        assertEquals(10.0, g.getEdgeWeight(e));
        assertEquals(e, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);
    }
    
    private void init()
    {
        lastAddedEdge = null;
        lastAddedVertex = null;
        lastRemovedEdge = null;
        lastRemovedVertex = null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * A listener on the tested graph.
     *
     * @author Barak Naveh
     * @since Aug 3, 2003
     */
    private class MyGraphListner<E extends DefaultEdge>
        implements GraphListener<Object, E>
    {
        /**
         * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeAdded(GraphEdgeChangeEvent<Object, E> e)
        {
            lastAddedEdge = e.getEdge();
        }

        /**
         * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<Object, E> e)
        {
            lastRemovedEdge = e.getEdge();
        }

        /**
         * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
         */
        @Override
        public void vertexAdded(GraphVertexChangeEvent<Object> e)
        {
            lastAddedVertex = e.getVertex();
        }

        /**
         * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
         */
        @Override
        public void vertexRemoved(GraphVertexChangeEvent<Object> e)
        {
            lastRemovedVertex = e.getVertex();
        }
    }
}

// End ListenableGraphTest.java
