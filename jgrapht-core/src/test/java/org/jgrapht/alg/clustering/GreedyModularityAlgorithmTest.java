/*
 * (C) Copyright 2020-2021, by Antonia Tsiftsi and Contributors.
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

import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests
 *
 * @author Antonia Tsiftsi
 */
public class GreedyModularityAlgorithmTest
{
    @Test
    public void test1()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);

        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        g.addEdge(7, 5);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 3), clustering.getClusters().get(0));
        assertEquals(Set.of(4, 5, 6, 7), clustering.getClusters().get(1));
    }

    @Test
    public void test2()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);
        g.addVertex(12);
        g.addVertex(13);
        g.addVertex(14);
        g.addVertex(15);

        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(0, 4);
        g.addEdge(0, 5);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(1, 7);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(2, 6);
        g.addEdge(3, 7);
        g.addEdge(4, 10);
        g.addEdge(5, 7);
        g.addEdge(5, 11);
        g.addEdge(6, 7);
        g.addEdge(6, 11);
        g.addEdge(8, 9);
        g.addEdge(8, 10);
        g.addEdge(8, 11);
        g.addEdge(8, 14);
        g.addEdge(8, 15);
        g.addEdge(9, 12);
        g.addEdge(9, 14);
        g.addEdge(10, 11);
        g.addEdge(10, 12);
        g.addEdge(10, 13);
        g.addEdge(10, 14);
        g.addEdge(11, 13);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6, 7), clustering.getClusters().get(0));
        assertEquals(Set.of(8, 9, 10, 11, 12, 13, 14, 15), clustering.getClusters().get(1));
    }

    @Test
    public void testZacharyKarateClub()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);
        g.addVertex(12);
        g.addVertex(13);
        g.addVertex(14);
        g.addVertex(15);
        g.addVertex(16);
        g.addVertex(17);
        g.addVertex(18);
        g.addVertex(19);
        g.addVertex(20);
        g.addVertex(21);
        g.addVertex(22);
        g.addVertex(23);
        g.addVertex(24);
        g.addVertex(25);
        g.addVertex(26);
        g.addVertex(27);
        g.addVertex(28);
        g.addVertex(29);
        g.addVertex(30);
        g.addVertex(31);
        g.addVertex(32);
        g.addVertex(33);
        g.addVertex(34);

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(1, 5);
        g.addEdge(1, 6);
        g.addEdge(1, 7);
        g.addEdge(5, 7);
        g.addEdge(6, 7);
        g.addEdge(1, 8);
        g.addEdge(2, 8);
        g.addEdge(3, 8);
        g.addEdge(4, 8);
        g.addEdge(1, 9);
        g.addEdge(3, 9);
        g.addEdge(3, 10);
        g.addEdge(1, 11);
        g.addEdge(5, 11);
        g.addEdge(6, 11);
        g.addEdge(1, 12);
        g.addEdge(1, 13);
        g.addEdge(4, 13);
        g.addEdge(1, 14);
        g.addEdge(2, 14);
        g.addEdge(3, 14);
        g.addEdge(4, 14);
        g.addEdge(6, 17);
        g.addEdge(7, 17);
        g.addEdge(1, 18);
        g.addEdge(2, 18);
        g.addEdge(1, 20);
        g.addEdge(2, 20);
        g.addEdge(1, 22);
        g.addEdge(2, 22);
        g.addEdge(24, 26);
        g.addEdge(25, 26);
        g.addEdge(3, 28);
        g.addEdge(24, 28);
        g.addEdge(25, 28);
        g.addEdge(3, 29);
        g.addEdge(24, 30);
        g.addEdge(27, 30);
        g.addEdge(2, 31);
        g.addEdge(9, 31);
        g.addEdge(1, 32);
        g.addEdge(25, 32);
        g.addEdge(26, 32);
        g.addEdge(29, 32);
        g.addEdge(3, 33);
        g.addEdge(9, 33);
        g.addEdge(15, 33);
        g.addEdge(16, 33);
        g.addEdge(19, 33);
        g.addEdge(21, 33);
        g.addEdge(23, 33);
        g.addEdge(24, 33);
        g.addEdge(30, 33);
        g.addEdge(31, 33);
        g.addEdge(32, 33);
        g.addEdge(9, 34);
        g.addEdge(10, 34);
        g.addEdge(14, 34);
        g.addEdge(15, 34);
        g.addEdge(16, 34);
        g.addEdge(19, 34);
        g.addEdge(20, 34);
        g.addEdge(21, 34);
        g.addEdge(23, 34);
        g.addEdge(24, 34);
        g.addEdge(27, 34);
        g.addEdge(28, 34);
        g.addEdge(29, 34);
        g.addEdge(30, 34);
        g.addEdge(31, 34);
        g.addEdge(32, 34);
        g.addEdge(33, 34);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(3, clustering.getNumberClusters());
        assertEquals(Set.of(2, 3, 4, 8, 10, 13, 14, 18, 22), clustering.getClusters().get(0));
        assertEquals(Set.of(1, 5, 6, 7, 11, 12, 17, 20), clustering.getClusters().get(1));
        assertEquals(
            Set.of(32, 33, 34, 9, 15, 16, 19, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31),
            clustering.getClusters().get(2));
    }

    @Test
    public void test4()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);
        g.addVertex(12);
        g.addVertex(13);
        g.addVertex(14);
        g.addVertex(15);
        g.addVertex(16);

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 5);
        g.addEdge(1, 6);
        g.addEdge(1, 8);
        g.addEdge(1, 9);
        g.addEdge(1, 11);
        g.addEdge(1, 12);
        g.addEdge(1, 14);
        g.addEdge(1, 15);
        g.addEdge(1, 16);
        g.addEdge(2, 3);
        g.addEdge(4, 5);
        g.addEdge(6, 7);
        g.addEdge(7, 8);
        g.addEdge(9, 10);
        g.addEdge(10, 11);
        g.addEdge(12, 13);
        g.addEdge(13, 14);
        g.addEdge(15, 16);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(4, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 3, 4, 5, 15, 16), clustering.getClusters().get(0));
        assertEquals(Set.of(6, 7, 8), clustering.getClusters().get(1));
        assertEquals(Set.of(9, 10, 11), clustering.getClusters().get(2));
        assertEquals(Set.of(12, 13, 14), clustering.getClusters().get(3));
    }

    @Test
    public void test5()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);

        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 10);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        g.addEdge(7, 5);
        g.addEdge(8, 5);
        g.addEdge(8, 6);
        g.addEdge(8, 10);
        g.addEdge(9, 7);
        g.addEdge(9, 10);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 3, 4), clustering.getClusters().get(0));
        assertEquals(Set.of(5, 6, 7, 8, 9, 10), clustering.getClusters().get(1));
    }

    @Test
    public void test6()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .directed().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);
        g.addVertex(12);

        g.addEdge(1, 12);
        g.addEdge(2, 3);
        g.addEdge(3, 8);
        g.addEdge(4, 3);
        g.addEdge(4, 5);
        g.addEdge(5, 7);
        g.addEdge(6, 3);
        g.addEdge(7, 9);
        g.addEdge(8, 1);
        g.addEdge(8, 2);
        g.addEdge(8, 7);
        g.addEdge(9, 1);
        g.addEdge(9, 10);
        g.addEdge(10, 6);
        g.addEdge(10, 11);
        g.addEdge(11, 9);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(4, clustering.getNumberClusters());
        assertEquals(Set.of(2, 3, 8), clustering.getClusters().get(0));
        assertEquals(Set.of(4, 5, 7), clustering.getClusters().get(1));
        assertEquals(Set.of(6, 9, 10, 11), clustering.getClusters().get(2));
        assertEquals(Set.of(1, 12), clustering.getClusters().get(3));
    }

    @Test
    public void test7()
    {
        Graph<Integer, DefaultEdge> g = GraphTypeBuilder
            .directed().allowingSelfLoops(true).vertexSupplier(SupplierUtil.createIntegerSupplier())
            .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);

        g.addEdge(1, 2);
        g.addEdge(1, 10);
        g.addEdge(3, 2);
        g.addEdge(3, 8);
        g.addEdge(4, 6);
        g.addEdge(6, 3);
        g.addEdge(6, 5);
        g.addEdge(7, 6);
        g.addEdge(8, 6);
        g.addEdge(9, 3);
        g.addEdge(9, 10);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(3, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 10), clustering.getClusters().get(0));
        assertEquals(Set.of(4, 5, 6, 7), clustering.getClusters().get(1));
        assertEquals(Set.of(3, 8, 9), clustering.getClusters().get(2));
    }

    @Test
    public void test8()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);

        g.addEdge(1, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(1, 6);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(5, 6);
        g.addEdge(5, 5);
        g.addEdge(6, 7);
        g.addEdge(6, 11);
        g.addEdge(7, 8);
        g.addEdge(7, 10);
        g.addEdge(8, 10);
        g.addEdge(9, 10);

        ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
        ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

        assertEquals(3, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 3, 4), clustering.getClusters().get(0));
        assertEquals(Set.of(5, 6, 11), clustering.getClusters().get(1));
        assertEquals(Set.of(7, 8, 9, 10), clustering.getClusters().get(2));
    }
}
