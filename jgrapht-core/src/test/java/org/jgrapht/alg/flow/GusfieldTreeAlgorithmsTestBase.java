/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

/**
 * Test base class for the GusfieldGomoryHuCutTree and GusfieldEquivalentFlow implementations
 *
 * @author Joris Kinable
 */
public abstract class GusfieldTreeAlgorithmsTestBase
{

    public abstract void validateAlgorithm(
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network);

    /**
     * Triangle graph example from the paper <it>Very simple methods for all pairs network flow
     * analysis</it> by Dan gusfield (Figure 1)
     */
    @Test
    public void testTriangleGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2));
        Graphs.addEdge(network, 0, 1, 3);
        Graphs.addEdge(network, 1, 2, 4);
        Graphs.addEdge(network, 0, 2, 7);
        validateAlgorithm(network);
    }

    /**
     * Square graph example from the paper <it>Very simple methods for all pairs network flow
     * analysis</it> by Dan gusfield (Figure 2)
     */
    @Test
    public void testSquareGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(network, 1, 2, 1);
        Graphs.addEdge(network, 3, 4, 1);
        Graphs.addEdge(network, 5, 6, 1);
        Graphs.addEdge(network, 5, 1, 1);
        Graphs.addEdge(network, 1, 3, 1);
        Graphs.addEdge(network, 6, 2, 1);
        Graphs.addEdge(network, 2, 4, 1);
        validateAlgorithm(network);
    }

    /**
     * Graph example from the paper <it>Multi-Terminal Network Flows</it> by Gomory, R. and Hu, T.
     */
    @Test
    public void testGomoryHuExampleGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(network, 1, 2, 10);
        Graphs.addEdge(network, 1, 6, 8);
        Graphs.addEdge(network, 2, 6, 3);
        Graphs.addEdge(network, 2, 3, 4);
        Graphs.addEdge(network, 2, 5, 2);
        Graphs.addEdge(network, 6, 3, 2);
        Graphs.addEdge(network, 6, 4, 2);
        Graphs.addEdge(network, 6, 5, 3);
        Graphs.addEdge(network, 5, 3, 4);
        Graphs.addEdge(network, 5, 4, 7);
        Graphs.addEdge(network, 3, 4, 5);
        validateAlgorithm(network);
    }

    @Test
    public void testGraphWithNoEdges()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(1, 2));
        validateAlgorithm(network);
    }

    /**
     * Some graph taken from the wikipedia article about Gomory-Hu trees
     */
    @Test
    public void testWikipediaGraph()
    {
        // Example wikipedia
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2, 3, 4, 5));
        Graphs.addEdge(network, 0, 1, 1);
        Graphs.addEdge(network, 0, 2, 7);
        Graphs.addEdge(network, 1, 2, 1);
        Graphs.addEdge(network, 1, 3, 3);
        Graphs.addEdge(network, 1, 4, 2);
        Graphs.addEdge(network, 2, 4, 4);
        Graphs.addEdge(network, 3, 4, 1);
        Graphs.addEdge(network, 3, 5, 6);
        Graphs.addEdge(network, 4, 5, 2);
        validateAlgorithm(network);
    }

    /**
     * Test disconnected graph
     */
    @Test
    public void testDisconnectedGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2, 3, 4));
        Graphs.addEdge(network, 0, 1, 3);
        Graphs.addEdge(network, 1, 2, 4);
        Graphs.addEdge(network, 0, 2, 7);
        Graphs.addEdge(network, 3, 4, 9);
        validateAlgorithm(network);
    }

    @Test
    public void testRandomGraphs()
    {
        Random rand = new Random(0);
        for (int i = 0; i < 10; i++) {
            SimpleWeightedGraph<Integer,
                DefaultWeightedEdge> randomGraph = new SimpleWeightedGraph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);
            int vertices = rand.nextInt((20 - 10) + 1) + 10; // 10-20 vertices
            double p = 0.01 * (rand.nextInt((85 - 50) + 1) + 50); // p=[0.5;0.85]
            GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> graphGen =
                new GnpRandomGraphGenerator<>(vertices, p);
            graphGen.generateGraph(randomGraph);
            for (DefaultWeightedEdge edge : randomGraph.edgeSet())
                randomGraph.setEdgeWeight(edge, rand.nextInt(150));
            validateAlgorithm(randomGraph);
        }
    }
}
