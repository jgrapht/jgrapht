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
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Check Incoming/Outgoing edges in directed and undirected graphs.
 *
 * @author Dimitrios Michail
 */
public class ImmutableGraphAdapterTest
{

    /**
     * Test the most general version of the directed graph.
     */
    @Test
    public void testDirectedGraph()
    {
        MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();

        graph.addNode("v1");
        graph.addNode("v2");
        graph.addNode("v3");
        graph.addNode("v4");
        graph.addNode("v5");
        graph.putEdge("v1", "v2");
        graph.putEdge("v2", "v3");
        graph.putEdge("v2", "v4");
        graph.putEdge("v4", "v4");
        graph.putEdge("v5", "v2");

        Graph<String, EndpointPair<String>> g =
            new ImmutableGraphAdapter<>(ImmutableGraph.copyOf(graph));

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(4, g.degreeOf("v2"));
        assertEquals(1, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(1, g.degreeOf("v5"));

        EndpointPair<String> e12 = EndpointPair.ordered("v1", "v2");
        EndpointPair<String> e23 = EndpointPair.ordered("v2", "v3");
        EndpointPair<String> e24 = EndpointPair.ordered("v2", "v4");
        EndpointPair<String> e44 = EndpointPair.ordered("v4", "v4");
        EndpointPair<String> e52 = EndpointPair.ordered("v5", "v2");

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52)), g.edgesOf("v5"));

        assertEquals(0, g.inDegreeOf("v1"));
        assertEquals(2, g.inDegreeOf("v2"));
        assertEquals(1, g.inDegreeOf("v3"));
        assertEquals(2, g.inDegreeOf("v4"));
        assertEquals(0, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(), g.incomingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(2, g.outDegreeOf("v2"));
        assertEquals(0, g.outDegreeOf("v3"));
        assertEquals(1, g.outDegreeOf("v4"));
        assertEquals(1, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e23, e24)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52)), g.outgoingEdgesOf("v5"));

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
            g.addEdge("v1", "v5", null);
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
     * Test the most general version of the directed graph.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization()
        throws Exception
    {
        MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();

        graph.addNode("v1");
        graph.addNode("v2");
        graph.addNode("v3");
        graph.addNode("v4");
        graph.addNode("v5");
        graph.putEdge("v1", "v2");
        graph.putEdge("v2", "v3");
        graph.putEdge("v2", "v4");
        graph.putEdge("v4", "v4");
        graph.putEdge("v5", "v2");

        Graph<String, EndpointPair<String>> initialGraph =
            new ImmutableGraphAdapter<>(ImmutableGraph.copyOf(graph));

        Graph<String, EndpointPair<String>> g = (Graph<String,
            EndpointPair<String>>) SerializationTestUtils.serializeAndDeserialize(initialGraph);

        assertFalse(g.getType().isAllowingMultipleEdges());
        assertTrue(g.getType().isAllowingSelfLoops());
        assertTrue(g.getType().isDirected());
        assertFalse(g.getType().isUndirected());
        assertFalse(g.getType().isWeighted());
        assertTrue(g.getType().isAllowingCycles());

        assertEquals(1, g.degreeOf("v1"));
        assertEquals(4, g.degreeOf("v2"));
        assertEquals(1, g.degreeOf("v3"));
        assertEquals(3, g.degreeOf("v4"));
        assertEquals(1, g.degreeOf("v5"));

        EndpointPair<String> e12 = EndpointPair.ordered("v1", "v2");
        EndpointPair<String> e23 = EndpointPair.ordered("v2", "v3");
        EndpointPair<String> e24 = EndpointPair.ordered("v2", "v4");
        EndpointPair<String> e44 = EndpointPair.ordered("v4", "v4");
        EndpointPair<String> e52 = EndpointPair.ordered("v5", "v2");

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.edgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24, e52)), g.edgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.edgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.edgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52)), g.edgesOf("v5"));

        assertEquals(0, g.inDegreeOf("v1"));
        assertEquals(2, g.inDegreeOf("v2"));
        assertEquals(1, g.inDegreeOf("v3"));
        assertEquals(2, g.inDegreeOf("v4"));
        assertEquals(0, g.inDegreeOf("v5"));

        assertEquals(new HashSet<>(), g.incomingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e12, e52)), g.incomingEdgesOf("v2"));
        assertEquals(new HashSet<>(Arrays.asList(e23)), g.incomingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e24, e44)), g.incomingEdgesOf("v4"));
        assertEquals(new HashSet<>(), g.incomingEdgesOf("v5"));

        assertEquals(1, g.outDegreeOf("v1"));
        assertEquals(2, g.outDegreeOf("v2"));
        assertEquals(0, g.outDegreeOf("v3"));
        assertEquals(1, g.outDegreeOf("v4"));
        assertEquals(1, g.outDegreeOf("v5"));

        assertEquals(new HashSet<>(Arrays.asList(e12)), g.outgoingEdgesOf("v1"));
        assertEquals(new HashSet<>(Arrays.asList(e23, e24)), g.outgoingEdgesOf("v2"));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf("v3"));
        assertEquals(new HashSet<>(Arrays.asList(e44)), g.outgoingEdgesOf("v4"));
        assertEquals(new HashSet<>(Arrays.asList(e52)), g.outgoingEdgesOf("v5"));

    }

}
