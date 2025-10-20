/*
 * Copyright (C) 2025, by Rayene Abbassi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LouvainClustering}.
 */
public class LouvainClusteringTest
{

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
}
