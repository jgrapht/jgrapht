/*
 * (C) Copyright 2018-2018, by Lukas Harzenetter and Contributors.
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
import java.util.function.*;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeightedGraphAsWeightedGraphTest
{

    private Graph<String, DefaultWeightedEdge> backingGraph;

    private DefaultWeightedEdge loop;
    private final double defaultLoopWeight = 6781234453486d;

    private DefaultWeightedEdge e12;
    private final double defaultE12Weight = 6d;
    private final double e12Weight = -123.54d;

    private DefaultWeightedEdge e23;
    private final double defaultE23Weight = 456d;
    private final double e23Weight = 89d;

    private DefaultWeightedEdge e24;
    private final double defaultE24Weight = 0.587d;
    private final double e24Weight = 3d;

    private final String v1 = "v1";
    private final String v2 = "v2";
    private final String v3 = "v3";
    private final String v4 = "v4";

    private Graph<String, DefaultWeightedEdge> weightedGraph;

    /**
     * Set up using default writeWeightsThrough setting (false) for AsWeightedGraph.
     */
    private void setUp()
    {
        Map<DefaultWeightedEdge, Double> graphWeights = createBackingGraph();
        this.weightedGraph = new AsWeightedGraph<>(this.backingGraph, graphWeights);
    }

    /**
     * Set up using explicit writeWeightsThrough setting (false) for AsWeightedGraph.
     */
    private void setUp(boolean writeWeightsThrough)
    {
        Map<DefaultWeightedEdge, Double> graphWeights = createBackingGraph();
        this.weightedGraph =
            new AsWeightedGraph<>(this.backingGraph, graphWeights, writeWeightsThrough);
    }

    private Map<DefaultWeightedEdge, Double> createBackingGraph()
    {
        this.backingGraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        this.backingGraph.addVertex(v1);
        this.backingGraph.addVertex(v2);
        this.backingGraph.addVertex(v3);
        this.backingGraph.addVertex(v4);
        e12 = Graphs.addEdge(this.backingGraph, v1, v2, defaultE12Weight);
        e23 = Graphs.addEdge(this.backingGraph, v2, v3, defaultE23Weight);
        e24 = Graphs.addEdge(this.backingGraph, v2, v4, defaultE24Weight);
        loop = Graphs.addEdge(this.backingGraph, v4, v4, defaultLoopWeight);

        Map<DefaultWeightedEdge, Double> graphWeights = new HashMap<>();
        graphWeights.put(e12, e12Weight);
        graphWeights.put(e23, e23Weight);
        graphWeights.put(e24, e24Weight);

        return graphWeights;
    }

    @Test
    public void testSetEdgeWeight()
    {
        this.setUp(false);

        double newEdgeWeight = -999;
        this.weightedGraph.setEdgeWeight(e12, newEdgeWeight);

        assertEquals(newEdgeWeight, this.weightedGraph.getEdgeWeight(e12), 0);
        assertEquals(this.defaultE12Weight, this.backingGraph.getEdgeWeight(e12), 0);
    }

    @Test
    public void testSetEdgeWeightDefaultPropagation()
    {
        this.setUp();

        double newEdgeWeight = -999;
        this.weightedGraph.setEdgeWeight(e12, newEdgeWeight);

        assertEquals(newEdgeWeight, this.weightedGraph.getEdgeWeight(e12), 0);
        assertEquals(newEdgeWeight, this.backingGraph.getEdgeWeight(e12), 0);
    }

    @Test
    public void testSetEdgePropagatesChangesToBackingGraph()
    {
        this.setUp(true);

        double newEdgeWeight = -999;
        this.weightedGraph.setEdgeWeight(e12, newEdgeWeight);

        assertEquals(newEdgeWeight, this.weightedGraph.getEdgeWeight(e12), 0);
        assertEquals(newEdgeWeight, this.backingGraph.getEdgeWeight(e12), 0);
    }

    @Test
    public void testGetEdgeWeight()
    {
        this.setUp(false);
        assertEquals(e23Weight, this.weightedGraph.getEdgeWeight(e23), 0);
    }

    @Test
    public void testGetDefaultEdgeWeight()
    {
        this.setUp(false);
        assertEquals(defaultLoopWeight, this.weightedGraph.getEdgeWeight(loop), 0);
    }

    @Test
    public void testGetEdgeWeightOfNull()
    {
        this.setUp(false);
        try {
            this.weightedGraph.getEdgeWeight(null);
            fail("Expected a NullPointerException");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testWeightFunction()
    {
        Graph<Integer, DefaultWeightedEdge> g1 =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g1, 0, 1, 2);
        Graphs.addEdgeWithVertices(g1, 1, 2, 3);
        Graphs.addEdgeWithVertices(g1, 2, 0, 4);
        Function<DefaultWeightedEdge, Double> weightFunction =
            e -> Math.pow(g1.getEdgeWeight(e), 2);
        Graph<Integer, DefaultWeightedEdge> g2 =
            new AsWeightedGraph<>(g1, weightFunction, true, false);
        // Repeat twice to trigger caching
        for (int i = 0; i < 2; i++)
            for (DefaultWeightedEdge edge : g1.edgeSet())
                assertEquals(
                    g1.getEdgeWeight(edge) * g1.getEdgeWeight(edge), g2.getEdgeWeight(edge), 0);
    }
}
