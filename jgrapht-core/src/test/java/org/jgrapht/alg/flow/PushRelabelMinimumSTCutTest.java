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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Joris Kinable
 */
public class PushRelabelMinimumSTCutTest
    extends
    MinimumSourceSinkCutTest
{
    @Override
    MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network)
    {
        return new PushRelabelMFImpl<>(network);
    }

    @Test
    public void testDisconnected1()
    {
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2, 3, 4, 5));
        network.addEdge(2, 4);
        network.addEdge(3, 4);
        network.addEdge(1, 4);
        network.addEdge(0, 1);
        network.addEdge(2, 0);
        network.addEdge(1, 0);
        network.addEdge(4, 0);
        network.addEdge(4, 1);
        network.addEdge(1, 3);
        network.addEdge(4, 3);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver = this.createSolver(network);
        double cutWeight = prSolver.calculateMinCut(0, 5);
        assertEquals(0d, cutWeight, 0);
    }

    @Test
    public void testDisconnected2()
    {
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2));
        network.addEdge(0, 1);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver = this.createSolver(network);
        double cutWeight = prSolver.calculateMinCut(0, 2);
        assertEquals(0d, cutWeight, 0);
    }

    @Test
    public void testRandomDirectedGraphs()
    {
        for (int test = 0; test < NR_RANDOM_TESTS; test++) {
            Graph<Integer, DefaultWeightedEdge> network = generateDirectedGraph();
            int source = 0;
            int sink = network.vertexSet().size() - 1;

            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver =
                this.createSolver(network);
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> ekSolver =
                new EdmondsKarpMFImpl<>(network);

            double expectedCutWeight = ekSolver.calculateMinCut(source, sink);

            double cutWeight = prSolver.calculateMinCut(source, sink);
            Set<Integer> sourcePartition = prSolver.getSourcePartition();
            Set<Integer> sinkPartition = prSolver.getSinkPartition();
            Set<DefaultWeightedEdge> cutEdges = prSolver.getCutEdges();

            this.verifyDirected(
                network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition,
                cutEdges);
        }
    }

    @Test
    public void testRandomUndirectedGraphs()
    {
        for (int test = 0; test < NR_RANDOM_TESTS; test++) {
            Graph<Integer, DefaultWeightedEdge> network = generateUndirectedGraph();
            int source = 0;
            int sink = network.vertexSet().size() - 1;

            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver =
                this.createSolver(network);
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> ekSolver =
                new EdmondsKarpMFImpl<>(network);

            double expectedCutWeight = ekSolver.calculateMinCut(source, sink);

            double cutWeight = prSolver.calculateMinCut(source, sink);
            Set<Integer> sourcePartition = prSolver.getSourcePartition();
            Set<Integer> sinkPartition = prSolver.getSinkPartition();
            Set<DefaultWeightedEdge> cutEdges = prSolver.getCutEdges();

            this.verifyUndirected(
                network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition,
                cutEdges);
        }
    }
}
