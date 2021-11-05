/*
 * (C) Copyright 2016-2021, by Joris Kinable and Contributors.
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

/**
 * @author Joris Kinable
 */
public class EdmondsKarpMinimumSTCutTest
    extends
    MinimumSourceSinkCutTest
{
    @Override
    MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network)
    {
        return new EdmondsKarpMFImpl<>(network);
    }

    @Test
    public void testRandomDirectedGraphs()
    {
        for (int test = 0; test < NR_RANDOM_TESTS; test++) {
            Graph<Integer, DefaultWeightedEdge> network = generateDirectedGraph();
            int source = 0;
            int sink = network.vertexSet().size() - 1;

            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> ekSolver =
                this.createSolver(network);
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver =
                new PushRelabelMFImpl<>(network);

            double expectedCutWeight = prSolver.calculateMinCut(source, sink);

            double cutWeight = ekSolver.calculateMinCut(source, sink);
            Set<Integer> sourcePartition = ekSolver.getSourcePartition();
            Set<Integer> sinkPartition = ekSolver.getSinkPartition();
            Set<DefaultWeightedEdge> cutEdges = ekSolver.getCutEdges();

            this
                .verifyDirected(
                    network, source, sink, expectedCutWeight, cutWeight, sourcePartition,
                    sinkPartition, cutEdges);
        }
    }

    @Test
    public void testRandomUndirectedGraphs()
    {
        for (int test = 0; test < NR_RANDOM_TESTS; test++) {
            Graph<Integer, DefaultWeightedEdge> network = generateUndirectedGraph();
            int source = 0;
            int sink = network.vertexSet().size() - 1;

            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> ekSolver =
                this.createSolver(network);
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver =
                new PushRelabelMFImpl<>(network);

            double expectedCutWeight = prSolver.calculateMinCut(source, sink);

            double cutWeight = ekSolver.calculateMinCut(source, sink);
            Set<Integer> sourcePartition = ekSolver.getSourcePartition();
            Set<Integer> sinkPartition = ekSolver.getSinkPartition();
            Set<DefaultWeightedEdge> cutEdges = ekSolver.getCutEdges();

            this
                .verifyUndirected(
                    network, source, sink, expectedCutWeight, cutWeight, sourcePartition,
                    sinkPartition, cutEdges);
        }
    }

}
