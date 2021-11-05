/*
 * (C) Copyright 2015-2021, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class PushRelabelMFImplTest
    extends
    MaximumFlowAlgorithmTest
{
    @Override
    MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network)
    {
        return new PushRelabelMFImpl<>(network);
    }

    @Test
    public void testSimpleDirectedWeightedGraph()
    {
        SimpleDirectedWeightedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultEdge.class);

        graph.addVertex(-1);
        graph.addVertex(-2);
        graph.addVertex(0);
        graph.addVertex(1);

        graph.addEdge(-1, 0);
        graph.setEdgeWeight(graph.getEdge(-1, 0), 1.0);

        graph.addEdge(0, -2);
        graph.setEdgeWeight(graph.getEdge(0, -2), 0.9999999999999999);

        graph.addEdge(-1, 1);
        graph.setEdgeWeight(graph.getEdge(-1, 1), 1.0);

        graph.addEdge(1, -2);
        graph.setEdgeWeight(graph.getEdge(1, -2), 1.66498);

        graph.addEdge(0, 1);
        graph.setEdgeWeight(graph.getEdge(0, 1), 0.66498);

        graph.addEdge(1, 0);
        graph.setEdgeWeight(graph.getEdge(1, 0), 0.66498);

        PushRelabelMFImpl<Integer, DefaultEdge> mf = new PushRelabelMFImpl<>(graph);

        Assert.assertEquals(2.0, mf.calculateMinCut(-1, -2), 1e-9);
    }

    @Test
    public void testPushRelabelWithNonIdenticalNode()
    {
        SimpleDirectedGraph<String, DefaultEdge> g1 =
            new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        g1.addVertex("v0");
        g1.addVertex("v1");
        g1.addVertex("v2");
        g1.addVertex("v3");
        g1.addVertex("v4");
        g1.addEdge("v0", "v2");
        g1.addEdge("v3", "v4");
        g1.addEdge("v1", "v0");
        g1.addEdge("v0", "v4");
        g1.addEdge("v0", "v1");
        g1.addEdge("v2", "v1");

        MaximumFlowAlgorithm<String, DefaultEdge> mf1 = new PushRelabelMFImpl<>(g1);
        String sourceFlow = "v3";
        String sinkFlow = "v0";
        double flow = mf1.getMaximumFlowValue(sourceFlow, sinkFlow);
        assertEquals(0.0, flow, 0);
    }
}
