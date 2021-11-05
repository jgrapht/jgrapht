/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph.guava;

import com.google.common.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Check Incoming/Outgoing edges in directed and undirected graphs.
 *
 * @author Dimitrios Michail
 */
public class ImmutableNetworkAdapterTest
{

    /**
     * Test the most general version of the directed graph.
     */
    @Test
    public void testDirectedGraph()
    {
        MutableNetwork<String, DefaultEdge> network =
            NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();

        network.addNode("v1");
        network.addNode("v2");
        network.addNode("v3");
        network.addNode("v4");
        network.addNode("v5");
        DefaultEdge e12 = new DefaultEdge();
        network.addEdge("v1", "v2", e12);
        DefaultEdge e23_1 = new DefaultEdge();
        network.addEdge("v2", "v3", e23_1);
        DefaultEdge e23_2 = new DefaultEdge();
        network.addEdge("v2", "v3", e23_2);
        DefaultEdge e24 = new DefaultEdge();
        network.addEdge("v2", "v4", e24);
        DefaultEdge e44 = new DefaultEdge();
        network.addEdge("v4", "v4", e44);
        DefaultEdge e55_1 = new DefaultEdge();
        network.addEdge("v5", "v5", e55_1);
        DefaultEdge e52 = new DefaultEdge();
        network.addEdge("v5", "v2", e52);
        DefaultEdge e55_2 = new DefaultEdge();
        network.addEdge("v5", "v5", e55_2);

        Graph<String, DefaultEdge> g =
            new ImmutableNetworkAdapter<>(ImmutableNetwork.copyOf(network));

        assertTrue(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(5, g.degreeOf("v2"));
        assertEquals(2, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(5, g.degreeOf("v5"));

        assertEquals(Set.of(e12), g.edgesOf("v1"));
        assertEquals(Set.of(e12, e23_1, e23_2, e24, e52), g.edgesOf("v2"));
        assertEquals(Set.of(e23_1, e23_2), g.edgesOf("v3"));
        assertEquals(Set.of(e24, e44), g.edgesOf("v4"));
        assertEquals(Set.of(e52, e55_1, e55_2), g.edgesOf("v5"));

        assertEquals(0, g.inDegreeOf("v1"));
        assertEquals(2, g.inDegreeOf("v2"));
        assertEquals(2, g.inDegreeOf("v3"));
        assertEquals(2, g.inDegreeOf("v4"));
        assertEquals(2, g.inDegreeOf("v5"));

        assertEquals(Set.of(), g.incomingEdgesOf("v1"));
        assertEquals(Set.of(e12, e52), g.incomingEdgesOf("v2"));
        assertEquals(Set.of(e23_1, e23_2), g.incomingEdgesOf("v3"));
        assertEquals(Set.of(e24, e44), g.incomingEdgesOf("v4"));
        assertEquals(Set.of(e55_1, e55_2), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(3, g.outDegreeOf("v2"));
        assertEquals(0, g.outDegreeOf("v3"));
        assertEquals(1, g.outDegreeOf("v4"));
        assertEquals(3, g.outDegreeOf("v5"));

        assertEquals(Set.of(e12), g.outgoingEdgesOf("v1"));
        assertEquals(Set.of(e23_1, e23_2, e24), g.outgoingEdgesOf("v2"));
        assertEquals(Set.of(), g.outgoingEdgesOf("v3"));
        assertEquals(Set.of(e44), g.outgoingEdgesOf("v4"));
        assertEquals(Set.of(e52, e55_1, e55_2), g.outgoingEdgesOf("v5"));

        // test indeed immutable
        try {
            g.addVertex("new");
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

        try {
            g.addEdge("v1", "v5");
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

        try {
            g.addEdge("v1", "v5", new DefaultEdge());
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

        try {
            g.removeVertex("v1");
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

        try {
            g.removeEdge("v1", "v2");
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

        try {
            g.removeEdge(e12);
            fail("Network not immutable");
        } catch (UnsupportedOperationException e) {
            // nothing
        }

    }

    /**
     * Tests serialization
     */
    @Test
    public void testSerialization()
        throws Exception
    {
        MutableNetwork<String, DefaultEdge> network =
            NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();

        network.addNode("v1");
        network.addNode("v2");
        network.addNode("v3");
        network.addNode("v4");
        network.addNode("v5");
        DefaultEdge e12 = new DefaultEdge();
        network.addEdge("v1", "v2", e12);
        DefaultEdge e23_1 = new DefaultEdge();
        network.addEdge("v2", "v3", e23_1);
        DefaultEdge e23_2 = new DefaultEdge();
        network.addEdge("v2", "v3", e23_2);
        DefaultEdge e24 = new DefaultEdge();
        network.addEdge("v2", "v4", e24);
        DefaultEdge e44 = new DefaultEdge();
        network.addEdge("v4", "v4", e44);
        DefaultEdge e55_1 = new DefaultEdge();
        network.addEdge("v5", "v5", e55_1);
        DefaultEdge e52 = new DefaultEdge();
        network.addEdge("v5", "v2", e52);
        DefaultEdge e55_2 = new DefaultEdge();
        network.addEdge("v5", "v5", e55_2);

        Graph<String, DefaultEdge> g =
            new ImmutableNetworkAdapter<>(ImmutableNetwork.copyOf(network));

        assertTrue(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());
        assertFalse(g.getType().isModifiable());

        assertTrue(g.containsEdge("v5", "v2"));

        Graph<String, DefaultEdge> g2 = SerializationTestUtils.serializeAndDeserialize(g);

        assertTrue(g2.getType().isAllowingMultipleEdges());
        assertTrue(g2.getType().isAllowingSelfLoops());
        assertTrue(g2.getType().isDirected());
        assertFalse(g2.getType().isUndirected());
        assertFalse(g2.getType().isWeighted());
        assertTrue(g2.getType().isAllowingCycles());
        assertFalse(g2.getType().isModifiable());

        assertTrue(g2.containsVertex("v1"));
        assertTrue(g2.containsVertex("v2"));
        assertTrue(g2.containsVertex("v3"));
        assertTrue(g2.containsVertex("v4"));
        assertTrue(g2.containsVertex("v5"));
        assertTrue(g2.vertexSet().size() == 5);
        assertTrue(g2.edgeSet().size() == 8);
        assertTrue(g2.containsEdge("v1", "v2"));
        assertTrue(g2.containsEdge("v2", "v3"));
        assertTrue(g2.containsEdge("v2", "v4"));
        assertTrue(g2.containsEdge("v4", "v4"));
        assertTrue(g2.containsEdge("v5", "v5"));
        assertTrue(g2.containsEdge("v5", "v2"));

        assertEquals(g.toString(), g2.toString());
    }

}
