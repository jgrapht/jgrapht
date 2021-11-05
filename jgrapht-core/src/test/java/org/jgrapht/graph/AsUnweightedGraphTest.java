/*
 * (C) Copyright 2018, by Lukas Harzenetter and Contributors.
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

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AsUnweightedGraphTest
{

    private DefaultWeightedEdge loop;
    private DefaultWeightedEdge e12;
    private DefaultWeightedEdge e23;
    private DefaultWeightedEdge e24;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private Graph<String, DefaultWeightedEdge> unweightedGraph;

    /**
     * Similar set up as created by {@link AsUndirectedGraphTest}.
     */
    @Before
    public void setUp()
    {
        Graph<String, DefaultWeightedEdge> undirectedWeightedGraph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        this.unweightedGraph = new AsUnweightedGraph<>(undirectedWeightedGraph);

        undirectedWeightedGraph.addVertex(v1);
        undirectedWeightedGraph.addVertex(v2);
        undirectedWeightedGraph.addVertex(v3);
        undirectedWeightedGraph.addVertex(v4);
        e12 = Graphs.addEdge(undirectedWeightedGraph, v1, v2, 6d);
        e23 = Graphs.addEdge(undirectedWeightedGraph, v2, v3, 456d);
        e24 = Graphs.addEdge(undirectedWeightedGraph, v2, v4, 0.587d);
        loop = Graphs.addEdge(undirectedWeightedGraph, v4, v4, 6781234453486d);
    }

    @Test
    public void getEdgeWeightOfE12()
    {
        assertEquals(Graph.DEFAULT_EDGE_WEIGHT, this.unweightedGraph.getEdgeWeight(e12), 0);
    }

    @Test
    public void getEdgeWeightOfE23()
    {
        assertEquals(Graph.DEFAULT_EDGE_WEIGHT, this.unweightedGraph.getEdgeWeight(e23), 0);
    }

    @Test
    public void getEdgeWeightOfE24()
    {
        assertEquals(Graph.DEFAULT_EDGE_WEIGHT, this.unweightedGraph.getEdgeWeight(e24), 0);
    }

    @Test
    public void getEdgeWeightOfLoop()
    {
        assertEquals(Graph.DEFAULT_EDGE_WEIGHT, this.unweightedGraph.getEdgeWeight(loop), 0);
    }

    @Test
    public void setEdgeWeight()
    {
        try {
            this.unweightedGraph.setEdgeWeight(e23, 81);
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("Edge weight is not supported"));
        }
    }

    @Test
    public void getType()
    {
        assertFalse(this.unweightedGraph.getType().isWeighted());
    }

    @Test
    public void failOnCreationOfUnweightedGraph()
    {
        try {
            new AsUnweightedGraph<>(null);
            fail("Expected an NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }
}
