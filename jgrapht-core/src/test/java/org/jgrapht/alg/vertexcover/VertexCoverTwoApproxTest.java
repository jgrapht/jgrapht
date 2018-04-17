/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests 2-approximation vertex cover algorithms.
 *
 * @author Alexandru Valeanu
 */
public abstract class VertexCoverTwoApproxTest extends VertexCoverTest {

    // ------- Approximation algorithms ------

    /**
     * Test 2-approximation algorithms for the minimum vertex cover problem.
     */
    @Test
    public void testFind2ApproximationCover()
    {
        MinimumVertexCoverAlgorithm<Integer, DefaultEdge> mvc = createSolver();

        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(TEST_GRAPH_SIZE);

            MinimumVertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover(Graphs.undirectedGraph(g));
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.getVertices().size(),0);
        }
    }

    /**
     * Test whether the 2 approximations are indeed within 2 times the optimum value
     */
    @Test
    public void testFind2ApproximationCover2()
    {
        MinimumVertexCoverAlgorithm<Integer, DefaultEdge> mvc = createSolver();

        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(70);

            MinimumVertexCoverAlgorithm.VertexCover<Integer> optimalCover = new RecursiveExactVCImpl<Integer, DefaultEdge>()
                    .getVertexCover(Graphs.undirectedGraph(g));

            MinimumVertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover(Graphs.undirectedGraph(g));
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.getVertices().size(),0);
            assertTrue(vertexCover.getWeight() <= optimalCover.getWeight() * 2); // Verify
            // 2-approximation
        }
    }
}
