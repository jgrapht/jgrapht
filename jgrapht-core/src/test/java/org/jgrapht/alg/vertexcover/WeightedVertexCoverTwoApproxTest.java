/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.jgrapht.alg.vertexcover.VertexCoverTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the weighted 2-approx vertex cover algorithms
 *
 * @author Joris Kinable
 */
public abstract class WeightedVertexCoverTwoApproxTest
    extends
    VertexCoverTwoApproxTest
    implements
    WeightedVertexCoverTest
{

    // ------- Approximation algorithms ------

    /**
     * Test 2-approximation algorithm for the minimum vertex cover problem. TODO: verify whether the
     * objective indeed is smaller than 2 times the optimum solution.
     */
    @Test
    public void testFind2ApproximationWeightedCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(TEST_GRAPH_SIZE);
            Map<Integer, Double> vertexWeights = WeightedVertexCoverTest.getRandomVertexWeights(g);
            VertexCoverAlgorithm<Integer> mvc =
                createWeightedSolver(Graphs.undirectedGraph(g), vertexWeights);

            VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();
            assertTrue(isCover(g, vertexCover));
            assertEquals(
                vertexCover.getWeight(), vertexCover.stream().mapToDouble(vertexWeights::get).sum(),
                0);
        }
    }
}
