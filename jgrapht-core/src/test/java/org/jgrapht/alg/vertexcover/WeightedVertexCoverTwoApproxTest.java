/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexCoverAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.util.Map;

import static org.jgrapht.alg.vertexcover.VertexCoverTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the weighted 2-approx vertex cover algorithms
 *
 * @author Joris Kinable
 */
public abstract class WeightedVertexCoverTwoApproxTest extends VertexCoverTwoApproxTest implements WeightedVertexCoverTest {

    // ------- Approximation algorithms ------

    /**
     * Test 2-approximation algorithm for the minimum vertex cover problem.
     * TODO: verify whether the objective indeed is smaller than 2 times the optimum solution.
     */
    @Test
    public void testFind2ApproximationWeightedCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(TEST_GRAPH_SIZE);
            Map<Integer, Double> vertexWeights = WeightedVertexCoverTest.getRandomVertexWeights(g);
            VertexCoverAlgorithm<Integer> mvc = createWeightedSolver(Graphs.undirectedGraph(g), vertexWeights);

            VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();
            assertTrue(isCover(g, vertexCover));
            assertEquals(
                    vertexCover.getWeight(),
                    vertexCover.stream().mapToDouble(vertexWeights::get).sum(),0);
        }
    }
}
