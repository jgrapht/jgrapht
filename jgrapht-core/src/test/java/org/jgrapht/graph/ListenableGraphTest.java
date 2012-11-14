/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import junit.framework.TestCase;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;


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
     * @see TestCase#TestCase(String)
     */
    public ListenableGraphTest(final String name)
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

        final ListenableGraph<Object, DefaultEdge> g =
            new ListenableUndirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final GraphListener<Object, DefaultEdge> listener = new MyGraphListner();
        g.addGraphListener(listener);

        final String v1 = "v1";
        final String v2 = "v2";

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

        final ListenableGraph<Object, DefaultEdge> g =
            new ListenableUndirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final VertexSetListener<Object> listener = new MyGraphListner();
        g.addVertexSetListener(listener);

        final String v1 = "v1";
        final String v2 = "v2";

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
    private class MyGraphListner
        implements GraphListener<Object, DefaultEdge>
    {
        /**
         * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeAdded(final GraphEdgeChangeEvent<Object, DefaultEdge> e)
        {
            lastAddedEdge = e.getEdge();
        }

        /**
         * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeRemoved(final GraphEdgeChangeEvent<Object, DefaultEdge> e)
        {
            lastRemovedEdge = e.getEdge();
        }

        /**
         * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
         */
        @Override
        public void vertexAdded(final GraphVertexChangeEvent<Object> e)
        {
            lastAddedVertex = e.getVertex();
        }

        /**
         * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
         */
        @Override
        public void vertexRemoved(final GraphVertexChangeEvent<Object> e)
        {
            lastRemovedVertex = e.getVertex();
        }
    }
}

// End ListenableGraphTest.java
