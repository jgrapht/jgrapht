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
import org.jgrapht.alg.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the GusfieldGomoryHuCutTree implementation
 *
 * @author Joris Kinable
 */
public class GusfieldGomoryHuCutTreeTest
    extends
    GusfieldTreeAlgorithmsTestBase
{

    @Override
    public void validateAlgorithm(SimpleWeightedGraph<Integer, DefaultWeightedEdge> network)
    {
        GusfieldGomoryHuCutTree<Integer, DefaultWeightedEdge> alg =
            new GusfieldGomoryHuCutTree<>(network);
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> gomoryHuTree = alg.getGomoryHuTree();

        // Verify that the Gomory-Hu tree is an actual tree
        assertTrue(GraphTests.isTree(gomoryHuTree));

        // Find the minimum cut in the graph
        StoerWagnerMinimumCut<Integer, DefaultWeightedEdge> minimumCutAlg =
            new StoerWagnerMinimumCut<>(network);
        double expectedMinimumCut = minimumCutAlg.minCutWeight();
        double cheapestEdge = gomoryHuTree
            .edgeSet().stream().mapToDouble(gomoryHuTree::getEdgeWeight).min().getAsDouble();
        assertEquals(expectedMinimumCut, cheapestEdge, 0);
        assertEquals(expectedMinimumCut, alg.calculateMinCut(), 0);
        Set<Integer> partition = alg.getSourcePartition();
        double cutWeight = network
            .edgeSet().stream()
            .filter(
                e -> partition.contains(network.getEdgeSource(e))
                    ^ partition.contains(network.getEdgeTarget(e)))
            .mapToDouble(network::getEdgeWeight).sum();
        assertEquals(expectedMinimumCut, cutWeight, 0);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> minimumSTCutAlgorithm =
            new PushRelabelMFImpl<>(network);
        for (Integer i : network.vertexSet()) {
            for (Integer j : network.vertexSet()) {
                if (j <= i)
                    continue;

                // Check cut weights
                double expectedCutWeight = minimumSTCutAlgorithm.calculateMinCut(i, j);
                assertEquals(expectedCutWeight, alg.getMaximumFlowValue(i, j), 0);
                assertEquals(expectedCutWeight, alg.getMaximumFlowValue(j, i), 0);
                assertEquals(expectedCutWeight, alg.calculateMinCut(j, i), 0);
                assertEquals(expectedCutWeight, alg.calculateMinCut(i, j), 0);
                assertEquals(expectedCutWeight, alg.getCutCapacity(), 0);

                // Check cut partitions
                Set<Integer> sourcePartition = alg.getSourcePartition();
                assertTrue(sourcePartition.contains(i));
                Set<Integer> sinkPartition = alg.getSinkPartition();
                assertTrue(sinkPartition.contains(j));
                Set<Integer> intersection = new HashSet<>(sourcePartition);
                intersection.retainAll(sinkPartition);
                assertTrue(intersection.isEmpty());
                cutWeight = network
                    .edgeSet().stream()
                    .filter(
                        e -> sourcePartition.contains(network.getEdgeSource(e))
                            ^ sourcePartition.contains(network.getEdgeTarget(e)))
                    .mapToDouble(network::getEdgeWeight).sum();
                assertEquals(expectedCutWeight, cutWeight, 0);

                // Verify the correctness of the tree
                // a. the cost of the cheapest edge in the path from i to j must equal the weight of
                // an i-j cut
                SimpleWeightedGraph<Integer, DefaultWeightedEdge> gomoryHuTreeCopy =
                    alg.getGomoryHuTree();
                List<DefaultWeightedEdge> pathEdges =
                    DijkstraShortestPath.findPathBetween(gomoryHuTreeCopy, i, j).getEdgeList();
                DefaultWeightedEdge cheapestEdgeInPath = pathEdges
                    .stream().min(Comparator.comparing(gomoryHuTreeCopy::getEdgeWeight))
                    .orElseThrow(() -> new RuntimeException("path is empty?!"));
                assertEquals(expectedCutWeight, network.getEdgeWeight(cheapestEdgeInPath), 0);
            }
        }
    }
}
