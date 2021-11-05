/*
 * (C) Copyright 2015-2021, by Andrew Chen and Contributors.
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
package org.jgrapht.graph.builder;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphBuilderTest
{
    // ~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private String v5 = "v5";
    private String v6 = "v6";
    private String v7 = "v7";
    private String v8 = "v8";

    @Test
    public void testAddVertex()
    {
        Graph<String, DefaultEdge> g =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addVertex(v1).addVertices(v2, v3).build();

        assertEquals(3, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
        assertTrue(g.vertexSet().containsAll(Arrays.asList(v1, v2, v3)));
    }

    @Test
    public void testAddEdge()
    {
        DefaultWeightedEdge e1 = new DefaultWeightedEdge();
        DefaultWeightedEdge e2 = new DefaultWeightedEdge();

        Graph<String,
            DefaultWeightedEdge> g = new GraphBuilder<>(
                new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class))
                        .addEdge(v1, v2).addEdgeChain(v3, v4, v5, v6).addEdge(v7, v8, 10.0)
                        .addEdge(v1, v7, e1).addEdge(v1, v8, e2, 42.0).buildAsUnmodifiable();

        assertEquals(8, g.vertexSet().size());
        assertEquals(7, g.edgeSet().size());
        assertTrue(g.vertexSet().containsAll(Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8)));
        assertTrue(g.containsEdge(v1, v2));
        assertTrue(g.containsEdge(v3, v4));
        assertTrue(g.containsEdge(v4, v5));
        assertTrue(g.containsEdge(v5, v6));
        assertTrue(g.containsEdge(v7, v8));
        assertTrue(g.containsEdge(v1, v7));
        assertTrue(g.containsEdge(v1, v8));
        assertEquals(e1, g.getEdge(v1, v7));
        assertEquals(e2, g.getEdge(v1, v8));
        assertEquals(10.0, g.getEdgeWeight(g.getEdge(v7, v8)), 0);
        assertEquals(42.0, g.getEdgeWeight(g.getEdge(v1, v8)), 0);
    }

    @Test
    public void testAddGraph()
    {
        Graph<String,
            DefaultEdge> g1 = DefaultDirectedGraph
                .<String, DefaultEdge> createBuilder(DefaultEdge.class).addVertex(v1)
                .addEdge(v2, v3).buildAsUnmodifiable();

        Graph<String, DefaultEdge> g2 =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addGraph(g1).addEdge(v1, v4).build();

        assertEquals(4, g2.vertexSet().size());
        assertEquals(2, g2.edgeSet().size());
        assertTrue(g2.vertexSet().containsAll(Arrays.asList(v1, v2, v3, v3)));
        assertTrue(g2.containsEdge(v2, v3));
        assertTrue(g2.containsEdge(v1, v4));
    }

    @Test
    public void testRemoveVertex()
    {
        Graph<String, DefaultEdge> g1 =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addEdge(v1, v3).addEdgeChain(v2, v3, v4, v5).buildAsUnmodifiable();

        Graph<String, DefaultEdge> g2 =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addGraph(g1).removeVertex(v2).removeVertices(v4, v5).build();

        assertEquals(2, g2.vertexSet().size());
        assertEquals(1, g2.edgeSet().size());
        assertTrue(g2.vertexSet().containsAll(Arrays.asList(v1, v3)));
        assertTrue(g2.containsEdge(v1, v3));
    }

    @Test
    public void testRemoveEdge()
    {
        DefaultEdge e = new DefaultEdge();

        Graph<String, DefaultEdge> g1 =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addEdgeChain(v1, v2, v3, v4).addEdge(v1, v4, e).buildAsUnmodifiable();

        Graph<String, DefaultEdge> g2 =
            new GraphBuilder<>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class))
                .addGraph(g1).removeEdge(v2, v3).removeEdge(e).build();

        assertEquals(4, g2.vertexSet().size());
        assertEquals(2, g2.edgeSet().size());
        assertTrue(g2.vertexSet().containsAll(Arrays.asList(v1, v2, v3, v4)));
        assertTrue(g2.containsEdge(v1, v2));
        assertTrue(g2.containsEdge(v3, v4));
    }

    @Test
    public void testAddVertexPseudograph()
    {
        Pseudograph<String, DefaultEdge> g = Pseudograph
            .<String, DefaultEdge> createBuilder(DefaultEdge.class).addVertex(v1).build();
        assertEquals(1, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
        assertTrue(g.vertexSet().containsAll(Collections.singletonList(v1)));
    }

}
