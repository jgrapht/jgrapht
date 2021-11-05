/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for {@link IntVertexDijkstraShortestPath}.
 *
 * @author Dimitrios Michail
 */
public class IntVertexDijkstraShortestPathTest
{
    @Test
    public void testUndirected()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4));
        g.setEdgeWeight(g.addEdge(0, 1), 2.0);
        g.setEdgeWeight(g.addEdge(0, 2), 3.0);
        g.setEdgeWeight(g.addEdge(0, 4), 100.0);
        g.setEdgeWeight(g.addEdge(1, 3), 5.0);
        g.setEdgeWeight(g.addEdge(2, 3), 20.0);
        g.setEdgeWeight(g.addEdge(3, 4), 5.0);

        IntVertexDijkstraShortestPath<DefaultWeightedEdge> algo =
            new IntVertexDijkstraShortestPath<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source0 = algo.getPaths(0);
        assertEquals(source0.getWeight(0), 0d, 1e-9);
        assertEquals(source0.getWeight(1), 2d, 1e-9);
        assertEquals(source0.getWeight(2), 3d, 1e-9);
        assertEquals(source0.getWeight(3), 7d, 1e-9);
        assertEquals(source0.getWeight(4), 12d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source1 = algo.getPaths(1);
        assertEquals(source1.getWeight(0), 2d, 1e-9);
        assertEquals(source1.getWeight(1), 0d, 1e-9);
        assertEquals(source1.getWeight(2), 5d, 1e-9);
        assertEquals(source1.getWeight(3), 5d, 1e-9);
        assertEquals(source1.getWeight(4), 10d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source2 = algo.getPaths(2);
        assertEquals(source2.getWeight(0), 3d, 1e-9);
        assertEquals(source2.getWeight(1), 5d, 1e-9);
        assertEquals(source2.getWeight(2), 0d, 1e-9);
        assertEquals(source2.getWeight(3), 10d, 1e-9);
        assertEquals(source2.getWeight(4), 15d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source3 = algo.getPaths(3);
        assertEquals(source3.getWeight(0), 7d, 1e-9);
        assertEquals(source3.getWeight(1), 5d, 1e-9);
        assertEquals(source3.getWeight(2), 10d, 1e-9);
        assertEquals(source3.getWeight(3), 0d, 1e-9);
        assertEquals(source3.getWeight(4), 5d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source4 = algo.getPaths(4);
        assertEquals(source4.getWeight(0), 12d, 1e-9);
        assertEquals(source4.getWeight(1), 10d, 1e-9);
        assertEquals(source4.getWeight(2), 15d, 1e-9);
        assertEquals(source4.getWeight(3), 5d, 1e-9);
        assertEquals(source4.getWeight(4), 0d, 1e-9);
    }

    @Test
    public void testUndirectedWithIdMap()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(100, 1, 2, 3, 4));
        g.setEdgeWeight(g.addEdge(100, 1), 2.0);
        g.setEdgeWeight(g.addEdge(100, 2), 3.0);
        g.setEdgeWeight(g.addEdge(100, 4), 100.0);
        g.setEdgeWeight(g.addEdge(1, 3), 5.0);
        g.setEdgeWeight(g.addEdge(2, 3), 20.0);
        g.setEdgeWeight(g.addEdge(3, 4), 5.0);

        IntVertexDijkstraShortestPath<DefaultWeightedEdge> algo =
            new IntVertexDijkstraShortestPath<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source100 = algo.getPaths(100);
        assertEquals(source100.getWeight(100), 0d, 1e-9);
        assertEquals(source100.getWeight(1), 2d, 1e-9);
        assertEquals(source100.getWeight(2), 3d, 1e-9);
        assertEquals(source100.getWeight(3), 7d, 1e-9);
        assertEquals(source100.getWeight(4), 12d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source1 = algo.getPaths(1);
        assertEquals(source1.getWeight(100), 2d, 1e-9);
        assertEquals(source1.getWeight(1), 0d, 1e-9);
        assertEquals(source1.getWeight(2), 5d, 1e-9);
        assertEquals(source1.getWeight(3), 5d, 1e-9);
        assertEquals(source1.getWeight(4), 10d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source2 = algo.getPaths(2);
        assertEquals(source2.getWeight(100), 3d, 1e-9);
        assertEquals(source2.getWeight(1), 5d, 1e-9);
        assertEquals(source2.getWeight(2), 0d, 1e-9);
        assertEquals(source2.getWeight(3), 10d, 1e-9);
        assertEquals(source2.getWeight(4), 15d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source3 = algo.getPaths(3);
        assertEquals(source3.getWeight(100), 7d, 1e-9);
        assertEquals(source3.getWeight(1), 5d, 1e-9);
        assertEquals(source3.getWeight(2), 10d, 1e-9);
        assertEquals(source3.getWeight(3), 0d, 1e-9);
        assertEquals(source3.getWeight(4), 5d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source4 = algo.getPaths(4);
        assertEquals(source4.getWeight(100), 12d, 1e-9);
        assertEquals(source4.getWeight(1), 10d, 1e-9);
        assertEquals(source4.getWeight(2), 15d, 1e-9);
        assertEquals(source4.getWeight(3), 5d, 1e-9);
        assertEquals(source4.getWeight(4), 0d, 1e-9);
    }

    @Test
    public void testDirected()
    {
        testDirected(0);
    }

    @Test
    public void testDirectedWithIdMap()
    {
        testDirected(10000);
    }

    private void testDirected(int offset)
    {
        Graph<Integer,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        Graphs
            .addAllVertices(
                g,
                Arrays
                    .asList(
                        offset + 0, offset + 1, offset + 2, offset + 3, offset + 4, offset + 5));
        DefaultWeightedEdge e01 = g.addEdge(offset + 0, offset + 1);
        g.setEdgeWeight(e01, 10.0);
        DefaultWeightedEdge e02 = g.addEdge(offset + 0, offset + 2);
        g.setEdgeWeight(e02, 20.0);
        DefaultWeightedEdge e24 = g.addEdge(offset + 2, offset + 4);
        g.setEdgeWeight(e24, 33.0);
        DefaultWeightedEdge e23 = g.addEdge(offset + 2, offset + 3);
        g.setEdgeWeight(e23, 20.0);
        DefaultWeightedEdge e14 = g.addEdge(offset + 1, offset + 4);
        g.setEdgeWeight(e14, 10.0);
        DefaultWeightedEdge e13 = g.addEdge(offset + 1, offset + 3);
        g.setEdgeWeight(e13, 50.0);
        DefaultWeightedEdge e34 = g.addEdge(offset + 3, offset + 4);
        g.setEdgeWeight(e34, 20.0);
        DefaultWeightedEdge e45 = g.addEdge(offset + 4, offset + 5);
        g.setEdgeWeight(e45, 1.0);
        DefaultWeightedEdge e35 = g.addEdge(offset + 3, offset + 5);
        g.setEdgeWeight(e35, 2.0);

        IntVertexDijkstraShortestPath<DefaultWeightedEdge> algo =
            new IntVertexDijkstraShortestPath<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source0 = algo.getPaths(offset + 0);
        assertEquals(source0.getWeight(offset + 0), 0d, 1e-9);
        assertEquals(Arrays.asList(), source0.getPath(offset + 0).getEdgeList());
        assertEquals(source0.getWeight(offset + 1), 10d, 1e-9);
        assertEquals(Arrays.asList(e01), source0.getPath(offset + 1).getEdgeList());
        assertEquals(source0.getWeight(offset + 2), 20d, 1e-9);
        assertEquals(Arrays.asList(e02), source0.getPath(offset + 2).getEdgeList());
        assertEquals(source0.getWeight(offset + 3), 40d, 1e-9);
        assertEquals(Arrays.asList(e02, e23), source0.getPath(offset + 3).getEdgeList());
        assertEquals(source0.getWeight(offset + 4), 20d, 1e-9);
        assertEquals(Arrays.asList(e01, e14), source0.getPath(offset + 4).getEdgeList());
        assertEquals(source0.getWeight(offset + 5), 21d, 1e-9);
        assertEquals(Arrays.asList(e01, e14, e45), source0.getPath(offset + 5).getEdgeList());

        SingleSourcePaths<Integer, DefaultWeightedEdge> source1 = algo.getPaths(offset + 1);
        assertTrue(Double.isInfinite(source1.getWeight(offset + 0)));
        assertEquals(source1.getWeight(offset + 1), 0d, 1e-9);
        assertTrue(Double.isInfinite(source1.getWeight(offset + 2)));
        assertEquals(source1.getWeight(offset + 3), 50d, 1e-9);
        assertEquals(source1.getWeight(offset + 4), 10d, 1e-9);
        assertEquals(source1.getWeight(offset + 5), 11d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source2 = algo.getPaths(offset + 2);
        assertTrue(Double.isInfinite(source2.getWeight(offset + 0)));
        assertTrue(Double.isInfinite(source2.getWeight(offset + 1)));
        assertEquals(source2.getWeight(offset + 2), 0d, 1e-9);
        assertEquals(source2.getWeight(offset + 3), 20d, 1e-9);
        assertEquals(source2.getWeight(offset + 4), 33d, 1e-9);
        assertEquals(source2.getWeight(offset + 5), 22d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source3 = algo.getPaths(offset + 3);
        assertTrue(Double.isInfinite(source3.getWeight(offset + 0)));
        assertTrue(Double.isInfinite(source3.getWeight(offset + 1)));
        assertTrue(Double.isInfinite(source3.getWeight(offset + 2)));
        assertEquals(source3.getWeight(offset + 3), 0d, 1e-9);
        assertEquals(source3.getWeight(offset + 4), 20d, 1e-9);
        assertEquals(source3.getWeight(offset + 5), 2d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source4 = algo.getPaths(offset + 4);
        assertTrue(Double.isInfinite(source4.getWeight(offset + 0)));
        assertTrue(Double.isInfinite(source4.getWeight(offset + 1)));
        assertTrue(Double.isInfinite(source4.getWeight(offset + 2)));
        assertTrue(Double.isInfinite(source4.getWeight(offset + 3)));
        assertEquals(source4.getWeight(offset + 4), 0d, 1e-9);
        assertEquals(source4.getWeight(offset + 5), 1d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source5 = algo.getPaths(offset + 5);
        assertTrue(Double.isInfinite(source5.getWeight(offset + 0)));
        assertTrue(Double.isInfinite(source5.getWeight(offset + 1)));
        assertTrue(Double.isInfinite(source5.getWeight(offset + 2)));
        assertTrue(Double.isInfinite(source5.getWeight(offset + 3)));
        assertTrue(Double.isInfinite(source5.getWeight(offset + 4)));
        assertEquals(source5.getWeight(offset + 5), 0d, 1e-9);
    }

    @Test
    public void testNonNegativeWeights()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2));

        DefaultWeightedEdge we12 = g.addEdge(1, 2);
        g.setEdgeWeight(we12, -100.0);

        try {
            new IntVertexDijkstraShortestPath<>(g).getPath(1, 2);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

}
