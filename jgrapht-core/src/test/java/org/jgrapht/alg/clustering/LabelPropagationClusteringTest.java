/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests
 *
 * @author Dimitrios Michail
 */
public class LabelPropagationClusteringTest
{
    @Test
    public void test1()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 0; i < 9; i++)
            g.addVertex();

        g.addEdge(0, 1);
        g.addEdge(0, 5);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 8);
        g.addEdge(4, 5);
        g.addEdge(4, 7);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        LabelPropagationClustering<Integer, DefaultEdge> alg =
            new LabelPropagationClustering<>(g, 0, new Random(13));
        Clustering<Integer> clustering = alg.getClustering();

        assertEquals(1, clustering.getNumberClusters());
        List<Set<Integer>> clusters = clustering.getClusters();

        assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8), clusters.get(0));
    }

    @Test
    public void test2()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 0; i < 8; i++)
            g.addVertex();

        // clique1
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);

        // clique2
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 7);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);

        // one edge between them
        g.addEdge(3, 4);

        LabelPropagationClustering<Integer, DefaultEdge> alg =
            new LabelPropagationClustering<>(g, 0, new Random(13));
        Clustering<Integer> clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        List<Set<Integer>> clusters = clustering.getClusters();

        assertEquals(Set.of(0, 1, 2, 3), clusters.get(0));
        assertEquals(Set.of(4, 5, 6, 7), clusters.get(1));
    }

    @Test
    public void test3()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 0; i < 12; i++) {
            g.addVertex();
        }

        // clique1
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);

        // clique2
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 7);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);

        // clique3
        g.addEdge(8, 9);
        g.addEdge(8, 10);
        g.addEdge(8, 11);
        g.addEdge(9, 10);
        g.addEdge(9, 11);
        g.addEdge(10, 11);

        // one edge between them
        g.addEdge(3, 4);
        g.addEdge(7, 8);

        LabelPropagationClustering<Integer, DefaultEdge> alg =
            new LabelPropagationClustering<>(g, 0, new Random(13));
        Clustering<Integer> clustering = alg.getClustering();

        assertEquals(3, clustering.getNumberClusters());
        List<Set<Integer>> clusters = clustering.getClusters();

        assertEquals(Set.of(0, 1, 2, 3), clusters.get(0));
        assertEquals(Set.of(4, 5, 6, 7), clusters.get(1));
        assertEquals(Set.of(8, 9, 10, 11), clusters.get(2));
    }

    @Test
    public void testWithIsolatedVertex()
    {
        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(0, 2);

        LabelPropagationClustering<Integer, DefaultEdge> alg =
            new LabelPropagationClustering<>(graph, 0, new Random(31));
        Clustering<Integer> clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        List<Set<Integer>> clusters = clustering.getClusters();

        assertEquals(Set.of(0, 1, 2), clusters.get(0));
        assertEquals(Set.of(3), clusters.get(1));
    }

}
