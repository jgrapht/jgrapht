/*
 * (C) Copyright 2026-2026, by Fotis Karagiannis and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.TestUtil;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for EgonetGraphGenerator
 *
 * @author Fotis Karagiannis
 */
public class EgonetGraphGeneratorTest
{
    public static final int EGO = 0;

    @Test
    public void testUndirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});
        Graph<Integer, DefaultEdge> egonet = TestUtil
                .createUndirected(new int[][]{});
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{0, 2}, {0, 3}, {0, 4}, {3, 4}, {1, 0}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createUndirected(expectedEdges);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if new edges are used
        assertNotEquals(g.getEdge(0, 2), egonet.getEdge(0, 2));
    }

    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createDirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});
        Graph<Integer, DefaultEdge> egonet = TestUtil
                .createDirected(new int[][]{});
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{0, 2}, {0, 3}, {0, 4}, {3, 4}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createDirected(expectedEdges);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if new edges are used
        assertNotEquals(g.getEdge(0, 2), egonet.getEdge(0, 2));
    }

    @Test
    public void testWeightedGraph()
    {
        Graph<Integer, DefaultWeightedEdge> g = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        for (int i = 0; i < 6; i++)
        {
            g.addVertex();
        }
        g.setEdgeWeight(g.addEdge(0, 2), 2d);
        g.setEdgeWeight(g.addEdge(0, 3), 1d);
        g.setEdgeWeight(g.addEdge(0, 4), 12d);
        g.setEdgeWeight(g.addEdge(1, 0), 4d);
        g.setEdgeWeight(g.addEdge(3, 4), 10d);
        g.setEdgeWeight(g.addEdge(3, 5), 7d);
        g.setEdgeWeight(g.addEdge(4, 5), 8d);
        Graph<Integer, DefaultWeightedEdge> egonet = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{0, 2}, {0, 3}, {0, 4}, {3, 4}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createDirected(expectedEdges);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if new edges are used
        assertNotEquals(g.getEdge(0, 2), egonet.getEdge(0, 2));
    }

    @Test
    public void testRadius()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});
        Graph<Integer, DefaultEdge> egonet = TestUtil
                .createUndirected(new int[][]{});
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO, 2);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{0, 2}, {0, 3}, {0, 4}, {3, 4}, {3, 5}, {4, 5}, {1, 0}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createUndirected(expectedEdges);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if new edges are used
        assertNotEquals(g.getEdge(0, 2), egonet.getEdge(0, 2));
    }

    @Test
    public void testExcludeEgo()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});
        Graph<Integer, DefaultEdge> egonet = TestUtil
                .createUndirected(new int[][]{});
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO, 1, false);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{3, 4}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createUndirected(expectedEdges);
        expectedEgonet.addVertex(1);
        expectedEgonet.addVertex(2);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if new edges are used
        assertNotEquals(g.getEdge(3, 4), egonet.getEdge(3, 4));
    }

    @Test
    public void testOldEdges()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});
        Graph<Integer, DefaultEdge> egonet = TestUtil
                .createUndirected(new int[][]{});
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new EgonetGraphGenerator<>(g, EGO, 1, true, false);
        gen.generateGraph(egonet);

        int[][] expectedEdges = {{0, 2}, {0, 3}, {0, 4}, {3, 4}, {1, 0}};
        Graph<Integer, DefaultEdge> expectedEgonet = TestUtil
                .createUndirected(expectedEdges);

        assertEquals(egonet.vertexSet(), expectedEgonet.vertexSet());
        for(int[] e : expectedEdges)
        {
            assertTrue(egonet.containsEdge(e[0], e[1]));
        }
        // test if old edges are used
        assertEquals(g.getEdge(0, 2), egonet.getEdge(0, 2));
    }
}
