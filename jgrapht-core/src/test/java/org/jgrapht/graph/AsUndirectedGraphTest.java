/*
 * (C) Copyright 2003-2021, by John V Sichi and Contributors.
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
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * A unit test for the AsDirectedGraph view.
 *
 * @author John V. Sichi
 */
public class AsUndirectedGraphTest
{
    // ~ Instance fields --------------------------------------------------------

    private Graph<String, DefaultEdge> directed;
    private DefaultEdge loop;
    private DefaultEdge e12;
    private DefaultEdge e23;
    private DefaultEdge e24;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private Graph<String, DefaultEdge> undirected;

    /**
     * .
     */
    @Test
    public void testAddEdge()
    {
        try {
            undirected.addEdge(v3, v4);
            Assert.fail(); // should not get here
        } catch (UnsupportedOperationException e) {
        }

        assertEquals(
            "([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v2,v4}, {v4,v4}])", undirected.toString());
    }

    /**
     * .
     */
    @Test
    public void testAddVertex()
    {
        String v5 = "v5";

        undirected.addVertex(v5);
        assertEquals(true, undirected.containsVertex(v5));
        assertEquals(true, directed.containsVertex(v5));
    }

    /**
     * .
     */
    @Test
    public void testDegreeOf()
    {
        assertEquals(1, undirected.degreeOf(v1));
        assertEquals(3, undirected.degreeOf(v2));
        assertEquals(1, undirected.degreeOf(v3));
        assertEquals(3, undirected.degreeOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testEdgesOf()
    {
        assertEquals(Set.of(e12), undirected.edgesOf(v1));
        assertEquals(Set.of(e12, e23, e24), undirected.edgesOf(v2));
        assertEquals(Set.of(e23), undirected.edgesOf(v3));
        assertEquals(Set.of(e24, loop), undirected.edgesOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testInDegreeOf()
    {
        assertEquals(1, undirected.inDegreeOf(v1));
        assertEquals(3, undirected.inDegreeOf(v2));
        assertEquals(1, undirected.inDegreeOf(v3));
        assertEquals(3, undirected.inDegreeOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testIncomingEdgesOf()
    {
        assertEquals(Set.of(e12), undirected.incomingEdgesOf(v1));
        assertEquals(Set.of(e12, e23, e24), undirected.incomingEdgesOf(v2));
        assertEquals(Set.of(e23), undirected.incomingEdgesOf(v3));
        assertEquals(Set.of(e24, loop), undirected.incomingEdgesOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testOutDegreeOf()
    {
        assertEquals(1, undirected.outDegreeOf(v1));
        assertEquals(3, undirected.outDegreeOf(v2));
        assertEquals(1, undirected.outDegreeOf(v3));
        assertEquals(3, undirected.outDegreeOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testOutgoingEdgesOf()
    {
        assertEquals(Set.of(e12), undirected.outgoingEdgesOf(v1));
        assertEquals(Set.of(e12, e23, e24), undirected.outgoingEdgesOf(v2));
        assertEquals(Set.of(e23), undirected.outgoingEdgesOf(v3));
        assertEquals(Set.of(e24, loop), undirected.outgoingEdgesOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testGetAllEdges()
    {
        Set<DefaultEdge> edges = undirected.getAllEdges(v3, v2);
        assertEquals(1, edges.size());
        assertEquals(directed.getEdge(v2, v3), edges.iterator().next());

        edges = undirected.getAllEdges(v4, v4);
        assertEquals(1, edges.size());
        assertEquals(loop, edges.iterator().next());
    }

    /**
     * .
     */
    @Test
    public void testGetEdge()
    {
        assertEquals(directed.getEdge(v1, v2), undirected.getEdge(v1, v2));
        assertEquals(directed.getEdge(v1, v2), undirected.getEdge(v2, v1));

        assertEquals(directed.getEdge(v4, v4), undirected.getEdge(v4, v4));
    }

    /**
     * .
     */
    @Test
    public void testRemoveEdge()
    {
        undirected.removeEdge(loop);
        assertEquals(false, undirected.containsEdge(loop));
        assertEquals(false, directed.containsEdge(loop));
    }

    /**
     * .
     */
    @Test
    public void testRemoveVertex()
    {
        undirected.removeVertex(v4);
        assertEquals(false, undirected.containsVertex(v4));
        assertEquals(false, directed.containsVertex(v4));
    }

    /**
     * .
     */
    @Test
    public void testToString()
    {
        assertEquals(
            "([v1, v2, v3, v4], [(v1,v2), (v2,v3), (v2,v4), (v4,v4)])", directed.toString());
        assertEquals(
            "([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v2,v4}, {v4,v4}])", undirected.toString());
    }

    /**
     * .
     */
    @Before
    public void setUp()
    {
        directed = new DefaultDirectedGraph<>(DefaultEdge.class);
        undirected = new AsUndirectedGraph<>(directed);

        directed.addVertex(v1);
        directed.addVertex(v2);
        directed.addVertex(v3);
        directed.addVertex(v4);
        e12 = directed.addEdge(v1, v2);
        e23 = directed.addEdge(v2, v3);
        e24 = directed.addEdge(v2, v4);
        loop = directed.addEdge(v4, v4);
    }
}
