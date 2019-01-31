/*
 * (C) Copyright 2007-2017, by France Telecom and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Joris Kinable
 */
public class BiconnectivityInspectorTest
{
    @Test
    public void testBiconnected()
    {
        BiconnectedGraph graph = new BiconnectedGraph();

        BiconnectivityInspector<String, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertTrue(inspector.isBiconnected());
        assertEquals(0, inspector.getCutpoints().size());
    }

    @Test
    public void testLinearGraph()
    {
        int nbVertices = 5;
        Graph<Object, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        LinearGraphGenerator<Object, DefaultEdge> generator =
            new LinearGraphGenerator<>(nbVertices);
        generator.generateGraph(graph);

        BiconnectivityInspector<Object, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertEquals(nbVertices - 2, inspector.getCutpoints().size());
    }

    @Test
    public void testNotBiconnected()
    {
        NotBiconnectedGraph graph = new NotBiconnectedGraph();

        BiconnectivityInspector<String, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertEquals(2, inspector.getCutpoints().size());
    }

    @Test
    public void testBorderCases()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // empty graph
        g.addVertex(0);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // graph on 1 vertex
        g.addVertex(1);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // graph on 2 vertices
                                                                       // without edges
        g.addEdge(0, 1);
        assertTrue(new BiconnectivityInspector<>(g).isBiconnected()); // graph with one edge
    }

    @Test
    public void testConnectedComponents1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(4, 5);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);
        assertEquals(2, inspector.getConnectedComponents().size());
        assertFalse(inspector.isConnected());

        Graph<Integer, DefaultEdge> g1 = new AsSubgraph<>(g, new HashSet<>(Arrays.asList(1, 2, 3)));
        Graph<Integer, DefaultEdge> g2 = new AsSubgraph<>(g, new HashSet<>(Arrays.asList(4, 5)));

        for (Integer v : g1.vertexSet())
            assertEquals(g1, inspector.getConnectedComponent(v));
        for (Integer v : g2.vertexSet())
            assertEquals(g2, inspector.getConnectedComponent(v));
    }

    @Test
    public void testWikiGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        int[][] edges = { { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 },
            { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertTrue(inspector.isConnected());

        Set<Integer> expectedCutpoints = new HashSet<>(Arrays.asList(4, 5, 6, 7, 9));
        assertEquals(expectedCutpoints, inspector.getCutpoints());

        Set<DefaultEdge> expectedBridges = new HashSet<>();
        expectedBridges.add(g.getEdge(4, 5));
        expectedBridges.add(g.getEdge(5, 6));
        expectedBridges.add(g.getEdge(6, 7));
        expectedBridges.add(g.getEdge(7, 8));
        expectedBridges.add(g.getEdge(9, 10));
        assertEquals(expectedBridges, inspector.getBridges());

        // Check vertex to block mapping
        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(1, 2, 3, 4)))); // 0
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(4, 5)))); // 1
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(5, 6)))); // 2
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(6, 7)))); // 3
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(7, 8)))); // 4
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(9, 10)))); // 5
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(7, 9, 11, 12, 13, 14)))); // 6

        for (int v : Arrays.asList(1, 2, 3))
            assertEquals(Collections.singleton(blocks.get(0)), inspector.getBlocks(v));
        assertEquals(Collections.singleton(blocks.get(4)), inspector.getBlocks(8));
        for (int v : Arrays.asList(11, 12, 13, 14)) {
            assertEquals(Collections.singleton(blocks.get(6)), inspector.getBlocks(v));
        }

        // cutpoints reside in multiple blocks
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(0), blocks.get(1))), inspector.getBlocks(4));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(1), blocks.get(2))), inspector.getBlocks(5));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(2), blocks.get(3))), inspector.getBlocks(6));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(3), blocks.get(4), blocks.get(6))),
            inspector.getBlocks(7));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(5), blocks.get(6))), inspector.getBlocks(9));

    }

    @Test
    public void testMultiGraph()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2));
        DefaultEdge bridge = g.addEdge(0, 1);
        g.addEdge(1, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 2);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertEquals(Collections.singleton(1), inspector.getCutpoints());
        assertEquals(Collections.singleton(bridge), inspector.getBridges());

        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(0, 1)))); // 0
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(1, 2)))); // 1

        assertEquals(new HashSet<>(blocks), inspector.getBlocks());
    }

    @Test
    public void testMultiGraph2()
    {
        Graph<Integer, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        int[][] edges = { { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 },
            { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 }, { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 },
            { 6, 7 }, { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertTrue(inspector.isConnected());

        Set<Integer> expectedCutpoints = new HashSet<>(Arrays.asList(4, 5, 6, 7, 9));
        assertEquals(expectedCutpoints, inspector.getCutpoints());

        assertEquals(Collections.emptySet(), inspector.getBridges());

        // Check vertex to block mapping
        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(1, 2, 3, 4)))); // 0
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(4, 5)))); // 1
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(5, 6)))); // 2
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(6, 7)))); // 3
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(7, 8)))); // 4
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(9, 10)))); // 5
        blocks.add(new AsSubgraph<>(g, new HashSet<>(Arrays.asList(7, 9, 11, 12, 13, 14)))); // 6

        for (int v : Arrays.asList(1, 2, 3))
            assertEquals(Collections.singleton(blocks.get(0)), inspector.getBlocks(v));
        assertEquals(Collections.singleton(blocks.get(4)), inspector.getBlocks(8));
        for (int v : Arrays.asList(11, 12, 13, 14)) {
            assertEquals(Collections.singleton(blocks.get(6)), inspector.getBlocks(v));
        }

        // cutpoints reside in multiple blocks
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(0), blocks.get(1))), inspector.getBlocks(4));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(1), blocks.get(2))), inspector.getBlocks(5));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(2), blocks.get(3))), inspector.getBlocks(6));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(3), blocks.get(4), blocks.get(6))),
            inspector.getBlocks(7));
        assertEquals(
            new HashSet<>(Arrays.asList(blocks.get(5), blocks.get(6))), inspector.getBlocks(9));
    }
}
