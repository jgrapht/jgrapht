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
import org.jgrapht.alg.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test class for the GusfieldEquivalentFlowTree implementation
 *
 * @author Joris Kinable
 */
public class GusfieldEquivalentFlowTreeTest
    extends
    GusfieldTreeAlgorithmsTestBase
{
    @Override
    public void validateAlgorithm(SimpleWeightedGraph<Integer, DefaultWeightedEdge> network)
    {
        GusfieldEquivalentFlowTree<Integer, DefaultWeightedEdge> alg =
            new GusfieldEquivalentFlowTree<>(network);
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> equivalentFlowTree =
            alg.getEquivalentFlowTree();

        // Verify that the Equivalent Flow tree is an actual tree
        assertTrue(GraphTests.isTree(equivalentFlowTree));

        // Find the minimum cut in the graph
        StoerWagnerMinimumCut<Integer, DefaultWeightedEdge> minimumCutAlg =
            new StoerWagnerMinimumCut<>(network);
        double expectedMinimumCut = minimumCutAlg.minCutWeight();
        double cheapestEdge = equivalentFlowTree
            .edgeSet().stream().mapToDouble(equivalentFlowTree::getEdgeWeight).min().getAsDouble();
        assertEquals(expectedMinimumCut, cheapestEdge, 0);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> minimumSTCutAlgorithm =
            new PushRelabelMFImpl<>(network);
        for (Integer i : network.vertexSet()) {
            for (Integer j : network.vertexSet()) {
                if (j <= i)
                    continue;

                // Check cut weights
                double expectedCutWeight = minimumSTCutAlgorithm.calculateMinCut(i, j);
                assertEquals(expectedCutWeight, alg.calculateMaximumFlow(i, j), 0);
                assertEquals(expectedCutWeight, alg.calculateMaximumFlow(j, i), 0);
                assertEquals(expectedCutWeight, alg.getMaximumFlowValue(), 0);

                // Verify the correctness of the tree
                // The cost of the cheapest edge in the path from i to j must equal the weight of an
                // i-j cut
                List<DefaultWeightedEdge> pathEdges =
                    DijkstraShortestPath.findPathBetween(equivalentFlowTree, i, j).getEdgeList();
                DefaultWeightedEdge cheapestEdgeInPath = pathEdges
                    .stream().min(Comparator.comparing(equivalentFlowTree::getEdgeWeight))
                    .orElseThrow(() -> new RuntimeException("path is empty?!"));
                assertEquals(expectedCutWeight, network.getEdgeWeight(cheapestEdgeInPath), 0);
            }
        }
    }
}
