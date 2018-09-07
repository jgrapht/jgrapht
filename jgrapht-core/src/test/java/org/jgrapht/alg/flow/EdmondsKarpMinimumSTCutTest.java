/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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

            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> ekSolver =
                this.createSolver(network);
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> prSolver =
                new PushRelabelMFImpl<>(network);

            double expectedCutWeight = prSolver.calculateMinCut(source, sink);

            double cutWeight = ekSolver.calculateMinCut(source, sink);
            Set<Integer> sourcePartition = ekSolver.getSourcePartition();
            Set<Integer> sinkPartition = ekSolver.getSinkPartition();
            Set<DefaultWeightedEdge> cutEdges = ekSolver.getCutEdges();

            this.verifyUndirected(
                network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition,
                cutEdges);
        }
    }

}
