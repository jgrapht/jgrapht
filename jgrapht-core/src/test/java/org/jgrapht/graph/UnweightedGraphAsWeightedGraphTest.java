/*
 * (C) Copyright 2018-2021, by Lukas Harzenetter and Contributors.
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

import static junit.framework.TestCase.fail;
import static org.jgrapht.Graph.DEFAULT_EDGE_WEIGHT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnweightedGraphAsWeightedGraphTest
{

    private DefaultWeightedEdge loop;

    private DefaultWeightedEdge e12;
    private final double e12Weight = -123.54d;

    private DefaultWeightedEdge e23;
    private final double e23Weight = 89d;

    private DefaultWeightedEdge e24;
    private final double e24Weight = 3d;

    private final String v1 = "v1";
    private final String v2 = "v2";
    private final String v3 = "v3";
    private final String v4 = "v4";

    private Graph<String, DefaultWeightedEdge> weightedGraph;

    /**
     * Similar set up as created by {@link AsUndirectedGraphTest}.
     */
    @Before
    public void setUp()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DefaultUndirectedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);

        e12 = graph.addEdge(v1, v2);
        e23 = graph.addEdge(v2, v3);
        e24 = graph.addEdge(v2, v4);
        loop = graph.addEdge(v4, v4);

        Map<DefaultWeightedEdge, Double> graphWeights = new HashMap<>();
        graphWeights.put(e12, e12Weight);
        graphWeights.put(e23, e23Weight);
        graphWeights.put(e24, e24Weight);

        this.weightedGraph = new AsWeightedGraph<>(graph, graphWeights);
    }

    @Test
    public void testSetEdgeWeight()
    {
        double newEdgeWeight = -999;
        this.weightedGraph.setEdgeWeight(e12, newEdgeWeight);

        assertEquals(newEdgeWeight, this.weightedGraph.getEdgeWeight(e12), 0);
    }

    @Test
    public void testGetEdgeWeight()
    {
        assertEquals(e23Weight, this.weightedGraph.getEdgeWeight(e23), 0);
    }

    @Test
    public void testGetDefaultEdgeWeight()
    {
        assertEquals(DEFAULT_EDGE_WEIGHT, this.weightedGraph.getEdgeWeight(loop), 0);
    }

    @Test
    public void testGetEdgeWeightOfNull()
    {
        try {
            this.weightedGraph.getEdgeWeight(null);
            fail("Expected a NullPointerException");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testGetType()
    {
        assertTrue(this.weightedGraph.getType().isWeighted());
    }

    @Test
    public void createAsWeightedGraphWithWeightPropagationOnAnUnweightedGraph()
    {
        try {
            new AsWeightedGraph<>(
                new DefaultUndirectedGraph<>(String.class), new HashMap<>(), true);
            fail("Expected a IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
