/*
 * (C) Copyright 2003-2018, by Linda Buisman and Contributors.
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

import static org.jgrapht.alg.vertexcover.VertexCoverTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests 2-approximation vertex cover algorithms.
 *
 * @author Linda Buisman
 */
public abstract class VertexCoverTwoApproxTest
    implements
    VertexCoverTest
{

    // ------- Approximation algorithms ------

    /**
     * Test 2-approximation algorithms for the minimum vertex cover problem.
     */
    @Test
    public void testFind2ApproximationCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(TEST_GRAPH_SIZE);
            VertexCoverAlgorithm<Integer> mvc = createSolver(Graphs.undirectedGraph(g));

            VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.size(), 0);
        }
    }

    /**
     * Test whether the 2 approximations are indeed within 2 times the optimum value
     */
    @Test
    public void testFind2ApproximationCover2()
    {

        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(70);

            VertexCoverAlgorithm.VertexCover<Integer> optimalCover =
                new RecursiveExactVCImpl<>(g).getVertexCover();
            VertexCoverAlgorithm<Integer> mvc = createSolver(Graphs.undirectedGraph(g));

            VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.size(), 0);
            assertTrue(vertexCover.getWeight() <= optimalCover.getWeight() * 2); // Verify
            // 2-approximation
        }
    }
}
