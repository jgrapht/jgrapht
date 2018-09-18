/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Check Incoming/Outgoing edges in directed and undirected graphs.
 *
 * @author Dimitrios Michail
 */
public class MutableGraphAdapterTest
{

    /**
     * Test the most general version of the directed graph.
     */
    @Test
    public void testDirectedGraph()
    {
        Graph<String, EndpointPair<String>> g =
            new MutableGraphAdapter<>(GraphBuilder.directed().allowsSelfLoops(true).build());

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        EndpointPair<String> e12 = g.addEdge("v1", "v2");
        EndpointPair<String> e23 = g.addEdge("v2", "v3");
        EndpointPair<String> e24 = g.addEdge("v2", "v4");
        EndpointPair<String> e44 = g.addEdge("v4", "v4");
        EndpointPair<String> e55 = g.addEdge("v5", "v5");
        EndpointPair<String> e52 = g.addEdge("v5", "v2");

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(4, g.degreeOf("v2"));
        assertEquals(1, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(3, g.degreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55)), g.edgesOf("v5"));

        assertEquals(0, g.inDegreeOf("v1"));
        assertEquals(2, g.inDegreeOf("v2"));
        assertEquals(1, g.inDegreeOf("v3"));
        assertEquals(2, g.inDegreeOf("v4"));
        assertEquals(1, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(), g.incomingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e55)), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(2, g.outDegreeOf("v2"));
        assertEquals(0, g.outDegreeOf("v3"));
        assertEquals(1, g.outDegreeOf("v4"));
        assertEquals(2, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e23, e24)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55)), g.outgoingEdgesOf("v5"));
    }

    /**
     * Test the most general version of the undirected graph.
     */
    @Test
    public void testUndirectedGraph()
    {
        Graph<String, EndpointPair<String>> g =
            new MutableGraphAdapter<>(GraphBuilder.undirected().allowsSelfLoops(true).build());

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertFalse(g.getType().isDirected());
        assertTrue(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        EndpointPair<String> e12 = g.addEdge("v1", "v2");
        EndpointPair<String> e23 = g.addEdge("v2", "v3");
        EndpointPair<String> e24 = g.addEdge("v2", "v4");
        EndpointPair<String> e44 = g.addEdge("v4", "v4");
        EndpointPair<String> e55 = g.addEdge("v5", "v5");
        EndpointPair<String> e52 = g.addEdge("v5", "v2");

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(4, g.degreeOf("v2"));
        assertEquals(1, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(3, g.degreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55)), g.edgesOf("v5"));

        assertEquals(1, g.inDegreeOf("v1"));
        assertEquals(4, g.inDegreeOf("v2"));
        assertEquals(1, g.inDegreeOf("v3"));
        assertEquals(3, g.inDegreeOf("v4"));
        assertEquals(3, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.incomingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55)), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(4, g.outDegreeOf("v2"));
        assertEquals(1, g.outDegreeOf("v3"));
        assertEquals(3, g.outDegreeOf("v4"));
        assertEquals(3, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52, e55)), g.outgoingEdgesOf("v5"));
    }

    /**
     * Tests serialization
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization()
        throws Exception
    {
        Graph<String, EndpointPair<String>> g =
            new MutableGraphAdapter<>(GraphBuilder.directed().allowsSelfLoops(true).build());

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        g.addEdge("v1", "v2");
        g.addEdge("v2", "v3");
        g.addEdge("v2", "v4");
        g.addEdge("v4", "v4");
        g.addEdge("v5", "v5");
        g.addEdge("v5", "v2");

        Graph<String, DefaultEdge> g2 =
            (Graph<String, DefaultEdge>) SerializationTestUtils.serializeAndDeserialize(g);

        assertFalse(g2.getType().isAllowingMultipleEdges());
        assertTrue(g2.getType().isAllowingSelfLoops());
        assertTrue(g2.getType().isDirected());
        assertFalse(g2.getType().isUndirected());
        assertFalse(g2.getType().isWeighted());
        assertTrue(g2.getType().isAllowingCycles());
        assertTrue(g2.containsVertex("v1"));
        assertTrue(g2.containsVertex("v2"));
        assertTrue(g2.containsVertex("v3"));
        assertTrue(g2.containsVertex("v4"));
        assertTrue(g2.containsVertex("v5"));
        assertTrue(g2.vertexSet().size() == 5);
        assertTrue(g2.edgeSet().size() == 6);
        assertTrue(g2.containsEdge("v1", "v2"));
        assertTrue(g2.containsEdge("v2", "v3"));
        assertTrue(g2.containsEdge("v2", "v4"));
        assertTrue(g2.containsEdge("v4", "v4"));
        assertTrue(g2.containsEdge("v5", "v5"));
        assertTrue(g2.containsEdge("v5", "v2"));

        assertEquals(g.toString(), g2.toString());
    }

    /**
     * Tests serialization
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization1()
        throws Exception
    {
        Graph<String,
            DefaultEdge> g = new MutableNetworkAdapter<>(
                NetworkBuilder
                    .undirected().allowsParallelEdges(false).allowsSelfLoops(true).build(),
                SupplierUtil.createRandomUUIDStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER);

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertFalse(g.getType().isDirected());
        assertTrue(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addEdge("v1", "v2");
        g.addEdge("v2", "v3");
        g.addEdge("v3", "v3");

        Graph<String, DefaultEdge> g2 =
            (Graph<String, DefaultEdge>) SerializationTestUtils.serializeAndDeserialize(g);

        assertFalse(g2.getType().isAllowingMultipleEdges());
        assertTrue(g2.getType().isAllowingSelfLoops());
        assertFalse(g2.getType().isDirected());
        assertTrue(g2.getType().isUndirected());
        assertFalse(g2.getType().isWeighted());
        assertTrue(g2.getType().isAllowingCycles());
        assertTrue(g2.containsVertex("v1"));
        assertTrue(g2.containsVertex("v2"));
        assertTrue(g2.containsVertex("v3"));
        assertTrue(g2.vertexSet().size() == 3);
        assertTrue(g2.edgeSet().size() == 3);
        assertTrue(g2.containsEdge("v1", "v2"));
        assertTrue(g2.containsEdge("v2", "v3"));
        assertTrue(g2.containsEdge("v3", "v3"));
    }

}
