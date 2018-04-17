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

public class GreedyVCImplTest extends VertexCoverTest {

    @Override
    <V, E> MinimumVertexCoverAlgorithm<V, E> createSolver() {
        return new GreedyVCImpl<>();
    }

    // ------- Greedy algorithms ------

    /**
     * Test greedy algorithm for the minimum vertex cover problem.
     */
    @Test
    public void testFindGreedyCover()
    {
        MinimumVertexCoverAlgorithm<Integer, DefaultEdge> mvc = createSolver();

        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomPseudoGraph(TEST_GRAPH_SIZE);
            MinimumVertexCoverAlgorithm.VertexCover<Integer> vertexCover = mvc.getVertexCover(Graphs.undirectedGraph(g));
            assertTrue(isCover(g, vertexCover));
            assertEquals(vertexCover.getWeight(), 1.0 * vertexCover.getVertices().size(),0);
        }
    }
}