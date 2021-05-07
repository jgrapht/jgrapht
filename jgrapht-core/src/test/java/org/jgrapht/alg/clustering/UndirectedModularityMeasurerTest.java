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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests
 *
 * @author Dimitrios Michail
 */
public class UndirectedModularityMeasurerTest
{

    @Test
    public void testOptimalPartition()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List.of(Set.of(0, 1, 2, 3, 4), Set.of(5, 6, 7, 8));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, 0.4112426, 1e-6);

    }

    @Test
    public void testSingle()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List.of(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, 0.0, 1e-6);

    }

    @Test
    public void testSuboptimal()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List.of(Set.of(0, 3, 4), Set.of(1, 2, 5, 6, 7, 8));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, 0.118343, 1e-6);

    }

    @Test
    public void testNegative()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List
            .of(
                Set.of(0), Set.of(3), Set.of(4), Set.of(1), Set.of(2), Set.of(5), Set.of(6),
                Set.of(7), Set.of(8));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, -0.118343, 1e-6);

    }

    @Test
    public void test24()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 0; i < 16; i++) {
            g.addVertex(i);
        }

        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(2, 8);
        g.addEdge(3, 4);
        g.addEdge(3, 6);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);
        g.addEdge(8, 9);
        g.addEdge(8, 10);
        g.addEdge(9, 10);
        g.addEdge(9, 11);
        g.addEdge(10, 11);
        g.addEdge(11, 12);
        g.addEdge(12, 13);
        g.addEdge(12, 14);
        g.addEdge(13, 14);
        g.addEdge(13, 15);
        g.addEdge(14, 15);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List
            .of(
                Set.of(0, 1, 2, 3, 4), Set.of(5, 6, 7), Set.of(8, 9, 10, 11),
                Set.of(12, 13, 14, 15));
        double mod = measurer.modularity(partitions);

        assertEquals(0.565104, mod, 1e-6);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidPartition()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List.of(Set.of(0, 3, 4), Set.of(1, 2, 5, 7, 8, 9));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, 0.118343, 1e-6);

    }

    @Test
    public void testWithSelfLoops()
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);

        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);

        g.addEdge(0, 1);
        g.addEdge(0, 4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);

        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(7, 8);

        // add self-loops
        g.addEdge(4, 4);
        g.addEdge(7, 7);

        UndirectedModularityMeasurer<Integer, DefaultWeightedEdge> measurer =
            new UndirectedModularityMeasurer<>(g);

        List<Set<Integer>> partitions = List.of(Set.of(0, 1, 2, 3, 4), Set.of(5, 6, 7, 8));
        double mod = measurer.modularity(partitions);

        assertEquals(mod, 0.42444444444444446, 1e-6);

    }

}
