/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

import org.junit.*;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SerializationTest tests serialization and deserialization of JGraphT objects.
 *
 * @author John V. Sichi
 */
public class SerializationTest
{
    // ~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";

    /**
     * Tests serialization of DirectedMultigraph.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDirectedMultigraph()
        throws Exception
    {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addEdge(v1, v2);
        graph.addEdge(v2, v3);
        graph.addEdge(v2, v3);

        graph = (DirectedMultigraph<String, DefaultEdge>) serializeAndDeserialize(graph);
        assertTrue(graph.containsVertex(v1));
        assertTrue(graph.containsVertex(v2));
        assertTrue(graph.containsVertex(v3));
        assertTrue(graph.containsEdge(v1, v2));
        assertTrue(graph.containsEdge(v2, v3));
        assertEquals(1, graph.edgesOf(v1).size());
        assertEquals(3, graph.edgesOf(v2).size());
        assertEquals(2, graph.edgesOf(v3).size());
    }

    /**
     * Tests serialization of DirectedAcyclicGraph
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDirectedAcyclicGraph()
        throws Exception
    {
        DirectedAcyclicGraph<String, DefaultEdge> graph1 =
            new DirectedAcyclicGraph<>(DefaultEdge.class);
        graph1.addVertex(v1);
        graph1.addVertex(v2);
        graph1.addVertex(v3);
        graph1.addEdge(v1, v2);
        graph1.addEdge(v2, v3);
        graph1.addEdge(v1, v3);

        DirectedAcyclicGraph<String, DefaultEdge> graph2 =
            (DirectedAcyclicGraph<String, DefaultEdge>) serializeAndDeserialize(graph1);
        assertTrue(graph2.containsVertex(v1));
        assertTrue(graph2.containsVertex(v2));
        assertTrue(graph2.containsVertex(v3));
        assertTrue(graph2.containsEdge(v1, v2));
        assertTrue(graph2.containsEdge(v2, v3));
        assertTrue(graph2.containsEdge(v1, v3));
        assertEquals(2, graph2.edgesOf(v1).size());
        assertEquals(2, graph2.edgesOf(v2).size());
        assertEquals(2, graph2.edgesOf(v3).size());

        assertEquals(graph1.toString(), graph2.toString());
    }

    private Object serializeAndDeserialize(Object obj)
        throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);

        out.writeObject(obj);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);

        obj = in.readObject();
        return obj;
    }
}
