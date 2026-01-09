/*
 * (C) Copyright 2025-2025, by Adam Bouzid and Contributors.
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
package org.jgrapht.alg.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LouvainClustering}.
 */
public class LouvainClusteringTest
{

    @Test
    public void emptyGraphProducesEmptyClustering()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(17));
        Clustering<Integer> result = clustering.getClustering();

        assertEquals(0, result.getNumberClusters());
        assertTrue(result.getClusters().isEmpty());
    }

    @Test
    public void singleVertexProducesSingletonCluster()
    {
        Graph<Integer, DefaultEdge> graph = GraphTypeBuilder
            .undirected().allowingMultipleEdges(false).allowingSelfLoops(false).weighted(false)
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        graph.addVertex(0);

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(42));
        Clustering<Integer> result = clustering.getClustering();

        assertEquals(1, result.getNumberClusters());
        assertEquals(Set.of(0), result.getClusters().get(0));
    }

    @Test
    public void twoDisconnectedVerticesFormSeparateClusters()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(23));
        Clustering<Integer> result = clustering.getClustering();

        assertEquals(2, result.getNumberClusters());
        assertTrue(result.getClusters().stream().anyMatch(c -> c.contains(0)));
        assertTrue(result.getClusters().stream().anyMatch(c -> c.contains(1)));
    }

    @Test
    public void completeGraphFormsSingleCluster()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                graph.addEdge(i, j);
            }
        }

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(29));
        Clustering<Integer> result = clustering.getClustering();

        assertEquals(1, result.getNumberClusters());
        assertEquals(5, result.getClusters().get(0).size());
    }

    @Test
    public void detectsTwoDenseCliques()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        // clique {0,1,2}
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);

        // clique {3,4,5}
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);

        // sparse bridge
        graph.addEdge(2, 3);

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(7));
        Clustering<Integer> result = clustering.getClustering();

        assertEquals(2, result.getNumberClusters());
        Set<Set<Integer>> actual =
            result.getClusters().stream().map(HashSet::new).collect(Collectors.toSet());
        Set<Set<Integer>> expected = new HashSet<>();
        expected.add(Set.of(0, 1, 2));
        expected.add(Set.of(3, 4, 5));
        assertEquals(expected, actual);
    }

    @Test
    public void respectsEdgeWeightsWhenFormingCommunities()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }

        graph.setEdgeWeight(graph.addEdge(0, 1), 4d);
        graph.setEdgeWeight(graph.addEdge(1, 2), 0.25d);
        graph.setEdgeWeight(graph.addEdge(2, 3), 4d);

        LouvainClustering<Integer, DefaultWeightedEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(5));
        List<Set<Integer>> clusters = clustering.getClustering().getClusters();

        assertEquals(2, clusters.size());
        Set<Set<Integer>> actual = clusters.stream().map(HashSet::new).collect(Collectors.toSet());
        Set<Set<Integer>> expected = new HashSet<>();
        expected.add(Set.of(0, 1));
        expected.add(Set.of(2, 3));
        assertEquals(expected, actual);
    }

    @Test
    public void resolutionParameterControlsCommunityGranularity()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 0);

        LouvainClustering<Integer, DefaultEdge> lowResolution =
            new LouvainClustering<>(graph, 0.5d, new Random(11));
        LouvainClustering<Integer, DefaultEdge> highResolution =
            new LouvainClustering<>(graph, 2d, new Random(11));

        int lowClusters = lowResolution.getClustering().getNumberClusters();
        int highClusters = highResolution.getClustering().getNumberClusters();

        assertTrue(lowClusters <= highClusters);
    }

    @Test
    public void deterministicWithSameSeed()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 0);

        LouvainClustering<Integer, DefaultEdge> clusteringA =
            new LouvainClustering<>(graph, 1d, new Random(31));
        LouvainClustering<Integer, DefaultEdge> clusteringB =
            new LouvainClustering<>(graph, 1d, new Random(31));

        Set<Set<Integer>> resultA = clusteringA.getClustering().getClusters().stream()
            .map(HashSet::new).collect(Collectors.toSet());
        Set<Set<Integer>> resultB = clusteringB.getClustering().getClusters().stream()
            .map(HashSet::new).collect(Collectors.toSet());

        assertEquals(resultA, resultB);
    }

    @Test
    public void cachesResultAcrossInvocations()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 1);

        LouvainClustering<Integer, DefaultEdge> clustering =
            new LouvainClustering<>(graph, 1d, new Random(41));

        Clustering<Integer> first = clustering.getClustering();
        Clustering<Integer> second = clustering.getClustering();

        assertSame(first, second);
    }

    @Test
    public void invalidResolutionThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);

        assertThrows(
            IllegalArgumentException.class,
            () -> new LouvainClustering<>(graph, 0d, new Random(53)));
    }
}
