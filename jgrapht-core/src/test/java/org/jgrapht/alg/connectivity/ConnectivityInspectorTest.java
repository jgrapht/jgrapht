/*
 * (C) Copyright 2003-2026, by Barak Naveh and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * .
 *
 * @author Barak Naveh
 */
public class ConnectivityInspectorTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";

    // ~ Instance fields --------------------------------------------------------

    //
    DefaultEdge e1;
    DefaultEdge e2;
    DefaultEdge e3;
    DefaultEdge e3B;
    DefaultEdge u;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @return a graph
     */
    public Pseudograph<String, DefaultEdge> create()
    {
        Pseudograph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        assertEquals(0, g.vertexSet().size());
        g.addVertex(V1);
        assertEquals(1, g.vertexSet().size());
        g.addVertex(V2);
        assertEquals(2, g.vertexSet().size());
        g.addVertex(V3);
        assertEquals(3, g.vertexSet().size());
        g.addVertex(V4);
        assertEquals(4, g.vertexSet().size());

        assertEquals(0, g.edgeSet().size());

        e1 = g.addEdge(V1, V2);
        assertEquals(1, g.edgeSet().size());

        e2 = g.addEdge(V2, V3);
        assertEquals(2, g.edgeSet().size());

        e3 = g.addEdge(V3, V1);
        assertEquals(3, g.edgeSet().size());

        e3B = g.addEdge(V3, V1);
        assertEquals(4, g.edgeSet().size());
        assertNotNull(e3B);

        u = g.addEdge(V1, V1);
        assertEquals(5, g.edgeSet().size());
        u = g.addEdge(V1, V1);
        assertEquals(6, g.edgeSet().size());

        return g;
    }

    /**
     * .
     */
    @Test
    public void testDirectedGraph()
    {
        ListenableGraph<String, DefaultEdge> g =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);

        g.addEdge(V1, V2);

        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);

        assertFalse(inspector.isConnected());

        g.addEdge(V1, V3);

        assertTrue(inspector.isConnected());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testListenerVertexAddBeforeQuery(boolean directed)
    {
        ListenableGraph<String, DefaultEdge> g = createListenableGraph(directed);
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);

        g.addVertex(V1);
        g.addVertex(V2);
        assertEquals(Set.of(Set.of(V1), Set.of(V2)), new HashSet<>(inspector.connectedSets()));
        assertEquals(2, inspector.connectedSets().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testListenerVertexAddAfterRemoval(boolean directed)
    {
        ListenableGraph<String, DefaultEdge> g = createListenableGraph(directed);
        g.addVertex(V1);
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);
        assertTrue(inspector.isConnected());

        g.removeVertex(V1);
        g.addVertex(V2);
        assertEquals(List.of(Set.of(V2)), inspector.connectedSets());
        g.addVertex(V3);
        assertEquals(Set.of(Set.of(V2), Set.of(V3)), new HashSet<>(inspector.connectedSets()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testListenerEdgeAddBeforeQuery(boolean directed)
    {
        ListenableGraph<String, DefaultEdge> g = createListenableGraph(directed);
        g.addVertex(V1);
        g.addVertex(V2);
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);

        g.addEdge(V1, V2);
        assertEquals(List.of(Set.of(V1, V2)), inspector.connectedSets());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testListenerEdgeAddAfterRemovalAndPartialQuery(boolean directed)
    {
        ListenableGraph<String, DefaultEdge> g = createListenableGraph(directed);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);
        assertTrue(inspector.isConnected());

        g.removeEdge(V1, V2);
        assertEquals(Set.of(V1), inspector.connectedSetOf(V1));
        assertEquals(Set.of(V2, V3), inspector.connectedSetOf(V3));
        g.addEdge(V1, V2);
        assertEquals(Set.of(V1, V2, V3), inspector.connectedSetOf(V1));
        assertEquals(Set.of(V1, V2, V3), inspector.connectedSetOf(V3));
        assertEquals(List.of(Set.of(V1, V2, V3)), inspector.connectedSets());
    }

    private ListenableGraph<String, DefaultEdge> createListenableGraph(boolean directed)
    {
        return new DefaultListenableGraph<>(
            directed ? new DefaultDirectedGraph<>(DefaultEdge.class)
                : new SimpleGraph<>(DefaultEdge.class));
    }

    /**
     * .
     */
    @Test
    public void testIsGraphConnected()
    {
        Pseudograph<String, DefaultEdge> g = create();
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);

        assertFalse(inspector.isConnected());

        g.removeVertex(V4);
        inspector = new ConnectivityInspector<>(g);
        assertTrue(inspector.isConnected());

        g.removeVertex(V1);
        assertEquals(1, g.edgeSet().size());

        g.removeEdge(e2);
        g.addEdge(V2, V2);
        assertEquals(1, g.edgeSet().size());

        inspector = new ConnectivityInspector<>(g);
        assertFalse(inspector.isConnected());
    }

}
