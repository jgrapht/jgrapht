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
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * Test class for the PadbergRaoOddMinimumCutset implementation
 *
 * @author Joris Kinable
 */
public class PadbergRaoOddMinimumCutsetTest
{

    private void runTest(
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network, Set<Integer> oddVertices,
        boolean useTreeCompression)
    {
        PadbergRaoOddMinimumCutset<Integer, DefaultWeightedEdge> padbergRaoOddMinimumCutset =
            new PadbergRaoOddMinimumCutset<>(network);
        double cutValue =
            padbergRaoOddMinimumCutset.calculateMinCut(oddVertices, useTreeCompression);
        Set<Integer> sourcePartition = padbergRaoOddMinimumCutset.getSourcePartition();
        Set<Integer> sinkPartition = padbergRaoOddMinimumCutset.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges = padbergRaoOddMinimumCutset.getCutEdges();

        Set<Integer> intersection = new HashSet<>(sourcePartition);
        intersection.retainAll(sinkPartition);
        assertTrue(intersection.isEmpty());
        Set<Integer> union = new HashSet<>(sourcePartition);
        union.addAll(sinkPartition);
        assertEquals(network.vertexSet(), union);

        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(sourcePartition, oddVertices));
        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(sinkPartition, oddVertices));

        Set<DefaultWeightedEdge> expectedCutEdges = network
            .edgeSet().stream()
            .filter(
                e -> sourcePartition.contains(network.getEdgeSource(e))
                    ^ sourcePartition.contains(network.getEdgeTarget(e)))
            .collect(Collectors.toSet());
        assertEquals(expectedCutEdges, cutEdges);
        double expectedWeight = cutEdges.stream().mapToDouble(network::getEdgeWeight).sum();
        assertEquals(expectedWeight, cutValue, 0);

        // Verify whether the returned odd cut-set is indeed of minimum weight. To verify this, we
        // exhaustively iterate over all possible cutsets.
        GusfieldGomoryHuCutTree<Integer, DefaultWeightedEdge> gusfieldGomoryHuCutTreeAlgorithm =
            new GusfieldGomoryHuCutTree<>(network);
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> gomoryHuCutTree =
            gusfieldGomoryHuCutTreeAlgorithm.getGomoryHuTree();
        Set<DefaultWeightedEdge> edges = new LinkedHashSet<>(gomoryHuCutTree.edgeSet());
        boolean foundBest = false; // Just to make sure that our brute-force approach is exhaustive
        for (DefaultWeightedEdge edge : edges) {
            Integer source = gomoryHuCutTree.getEdgeSource(edge);
            Integer target = gomoryHuCutTree.getEdgeTarget(edge);
            double edgeWeight = gomoryHuCutTree.getEdgeWeight(edge);
            gomoryHuCutTree.removeEdge(edge); // Temporarily remove edge
            Set<Integer> partition =
                new ConnectivityInspector<>(gomoryHuCutTree).connectedSetOf(source);
            if (PadbergRaoOddMinimumCutset.isOddVertexSet(partition, oddVertices)) { // If the
                                                                                     // source
                // partition forms an
                // odd cutset, check
                // whether the cut
                // isn't better than
                // the one we already
                // found.
                assertTrue(cutValue <= edgeWeight);
                foundBest |= cutValue == edgeWeight;
            }
            gomoryHuCutTree.addEdge(source, target, edge); // Place edge back
        }
        assertTrue(foundBest);
    }

    @Test
    public void testIsOddSetMethod()
    {
        Set<Integer> vertices = Set.of(1, 2, 3, 4, 5, 6);
        Set<Integer> oddVertices1 = Set.of(1, 2, 3, 7);
        Set<Integer> oddVertices2 = Set.of(1, 2, 3, 4);
        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(vertices, oddVertices1));
        assertFalse(PadbergRaoOddMinimumCutset.isOddVertexSet(vertices, oddVertices2));
    }

    /**
     * Test the example graph from the paper Odd Minimum Cut-Sets and b-Matchings by Padberg and Rao
     */
    @Test
    public void testExampleGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(network, 1, 2, 10);
        Graphs.addEdge(network, 1, 6, 8);
        Graphs.addEdge(network, 2, 6, 3);
        Graphs.addEdge(network, 2, 3, 4);
        Graphs.addEdge(network, 2, 5, 2);
        Graphs.addEdge(network, 6, 3, 2);
        Graphs.addEdge(network, 6, 4, 2);
        Graphs.addEdge(network, 6, 5, 3);
        Graphs.addEdge(network, 5, 3, 4);
        Graphs.addEdge(network, 5, 4, 7);
        Graphs.addEdge(network, 3, 4, 5);

        Set<Integer> oddVertices = Set.of(2, 3, 5, 6);
        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);

    }

    /**
     * Test disconnected graph
     */
    @Test
    public void testDisconnectedGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2, 3, 4));
        Graphs.addEdge(network, 0, 1, 3);
        Graphs.addEdge(network, 1, 2, 4);
        Graphs.addEdge(network, 0, 2, 7);
        Graphs.addEdge(network, 3, 4, 9);
        Set<Integer> oddVertices = Set.of(0, 1, 2, 4);
        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);
    }

    /**
     * Another graph to test
     */
    @Test
    public void testGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        network.addVertex(7);
        network.addVertex(10);
        network.addVertex(12);
        network.addVertex(3);
        network.addVertex(1);
        network.addVertex(5);
        network.addVertex(6);
        Graphs.addEdge(network, 1, 12, 1.0);
        Graphs.addEdge(network, 3, 5, 1.0);
        Graphs.addEdge(network, 5, 6, 1.0);
        Graphs.addEdge(network, 6, 12, 4.0);

        Set<Integer> oddVertices = new LinkedHashSet<Integer>(Arrays.asList(7, 10, 12, 3));

        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);

    }

    /**
     * Test random graphs
     */
    @Test
    public void testRandomGraphs()
    {
        Random rand = new Random(0);
        for (int i = 0; i < 8; i++) {
            SimpleWeightedGraph<Integer,
                DefaultWeightedEdge> randomGraph = new SimpleWeightedGraph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);
            int vertices = rand.nextInt((30 - 10) + 1) + 10; // 10-30 vertices
            double p = 0.01 * (rand.nextInt((85 - 50) + 1) + 50); // p=[0.5;0.85]
            GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> graphGen =
                new GnpRandomGraphGenerator<>(vertices, p);
            graphGen.generateGraph(randomGraph);
            for (DefaultWeightedEdge edge : randomGraph.edgeSet())
                randomGraph.setEdgeWeight(edge, rand.nextInt(150));

            for (int j = 0; j < 8; j++) {
                // Select a random subset of vertices of even cardinality. These will be the 'odd'
                // vertices.
                int max = vertices - 1;
                int min = 2;
                if (max % 2 == 1)
                    --max;
                int nrOfOddVertices = min + 2 * (int) (rand.nextDouble() * ((max - min) / 2 + 1)); // even
                // number
                // between
                // 2
                // and
                // |V|-1

                Set<Integer> oddVertices =
                    CollectionUtil.newLinkedHashSetWithExpectedSize(nrOfOddVertices);
                List<Integer> allVertices = new ArrayList<>(randomGraph.vertexSet());
                for (int k = 0; k < nrOfOddVertices; k++) {
                    oddVertices.add(allVertices.remove(rand.nextInt(allVertices.size())));
                }
                this.runTest(randomGraph, oddVertices, true);
                this.runTest(randomGraph, oddVertices, false);
            }
        }
    }

}
