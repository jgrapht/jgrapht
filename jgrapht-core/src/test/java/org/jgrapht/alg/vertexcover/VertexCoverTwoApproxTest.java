/*
 * (C) Copyright 2003-2018, by Linda Buisman and Contributors.
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

import static org.jgrapht.alg.vertexcover.VertexCoverTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests 2-approximation vertex cover algorithms.
 *
 * @author Linda Buisman
 */
public abstract class VertexCoverTwoApproxTest implements VertexCoverTest {

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

            VertexCoverAlgorithm.VertexCover<Integer> optimalCover = new RecursiveExactVCImpl<>(g).getVertexCover();
            VertexCoverAlgorithm<Integer> mvc = createSolver(Graphs.undirectedGraph(g));

            VertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover();
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.size(),0);
            assertTrue(vertexCover.getWeight() <= optimalCover.getWeight() * 2); // Verify
            // 2-approximation
        }
    }
}
