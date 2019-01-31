/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
import org.jgrapht.event.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link ListenableGraph} class.
 *
 * @author Barak Naveh
 */
public class ListenableGraphTest
{
    // ~ Instance fields --------------------------------------------------------

    Object lastAddedEdge;
    Object lastRemovedEdge;
    Object lastAddedVertex;
    Object lastRemovedVertex;
    Double lastWeightUpdate;

    /**
     * Tests GraphListener listener.
     */
    @Test
    public void testGraphListener()
    {
        init();

        ListenableGraph<Object, DefaultEdge> g =
            new DefaultListenableGraph<>(new SimpleGraph<>(DefaultEdge.class));
        GraphListener<Object, DefaultEdge> listener = new MyGraphListener<>();
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
    @Test
    public void testVertexSetListener()
    {
        init();

        ListenableGraph<Object, DefaultEdge> g =
            new DefaultListenableGraph<>(new SimpleGraph<>(DefaultEdge.class));
        VertexSetListener<Object> listener = new MyGraphListener<>();
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
    @Test
    public void testListenableDirectedWeightedGraph()
    {
        init();

        ListenableGraph<Object, DefaultWeightedEdge> g = new DefaultListenableGraph<>(
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class));

        GraphListener<Object, DefaultWeightedEdge> listener = new MyGraphListener<>();
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
        assertEquals(10.0, g.getEdgeWeight(e), 0);
        assertEquals(e, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);
    }

    @Test
    public void testListenableDirectedWeightedGraphWithCustomEdge()
    {
        init();

        ListenableGraph<Object, DefaultEdge> g =
            new DefaultListenableGraph<>(new DefaultDirectedWeightedGraph<>(DefaultEdge.class));

        GraphListener<Object, DefaultEdge> listener = new MyGraphListener<>();
        g.addGraphListener(listener);

        String v1 = "v1";
        String v2 = "v2";

        g.addVertex(v1);
        assertEquals(v1, lastAddedVertex);
        assertEquals(null, lastRemovedVertex);

        g.addVertex(v2);

        init();

        DefaultEdge e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 10.0);
        assertEquals(10.0, g.getEdgeWeight(e), 0);
        assertEquals(e, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);

        init();

        g.setEdgeWeight(e, 5.5d);
        assertEquals(5.5, g.getEdgeWeight(e), 1e-9);
        assertEquals(null, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);
        assertEquals(5.5, lastWeightUpdate, 1e-9);

        g.setEdgeWeight(e, 20.5d);
        assertEquals(20.5, g.getEdgeWeight(e), 1e-9);
        assertEquals(null, lastAddedEdge);
        assertEquals(null, lastRemovedEdge);
        assertEquals(20.5, lastWeightUpdate, 1e-9);
    }

    public void init()
    {
        lastAddedEdge = null;
        lastAddedVertex = null;
        lastRemovedEdge = null;
        lastRemovedVertex = null;
        lastWeightUpdate = null;
    }

    // ~ Inner Classes ----------------------------------------------------------

    /**
     * A listener on the tested graph.
     *
     * @author Barak Naveh
     */
    private class MyGraphListener<E>
        implements
        GraphListener<Object, E>
    {
        @Override
        public void edgeAdded(GraphEdgeChangeEvent<Object, E> e)
        {
            lastAddedEdge = e.getEdge();
        }

        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<Object, E> e)
        {
            lastRemovedEdge = e.getEdge();
        }

        @Override
        public void vertexAdded(GraphVertexChangeEvent<Object> e)
        {
            lastAddedVertex = e.getVertex();
        }

        @Override
        public void vertexRemoved(GraphVertexChangeEvent<Object> e)
        {
            lastRemovedVertex = e.getVertex();
        }

        @Override
        public void edgeWeightUpdated(GraphEdgeChangeEvent<Object, E> e)
        {
            lastWeightUpdate = e.getEdgeWeight();
        }
    }
}
