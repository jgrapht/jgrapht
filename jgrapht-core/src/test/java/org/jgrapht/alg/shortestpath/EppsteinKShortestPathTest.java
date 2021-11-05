/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link EppsteinKShortestPath} class.
 *
 * @author Semen Chudakov
 */
public class EppsteinKShortestPathTest
{
    final int[][] simpleGraph1 =
        { { 1, 2, 2 }, { 2, 3, 20 }, { 3, 4, 14 }, { 1, 5, 13 }, { 2, 6, 27 }, { 3, 7, 14 },
            { 4, 8, 15 }, { 5, 6, 9 }, { 6, 7, 10 }, { 7, 8, 25 }, { 5, 9, 15 }, { 6, 10, 20 },
            { 7, 11, 12 }, { 8, 12, 7 }, { 9, 10, 18 }, { 10, 11, 8 }, { 11, 12, 11 } };

    final int[][] cyclicGraph3 = { { 1, 2, 1 }, { 2, 3, 1 }, { 3, 4, 1 }, { 3, 4, 1 }, { 4, 3, 1 },
        { 4, 5, 1 }, { 5, 4, 1 } };

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeK()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        new EppsteinKShortestPath<>(graph).getPaths(1, 2, -1);
    }

    /**
     * If k equals $0$ and there is no paths in the graph between source and target, no exception
     * should be thrown and an empty list should be returned.
     */
    @Test
    public void testKEqualsZero()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new EppsteinKShortestPath<>(graph).getPaths(1, 2, 0);
        assertEquals(0, paths.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new EppsteinKShortestPath<>(graph).getPaths(1, 2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new EppsteinKShortestPath<>(graph).getPaths(1, 2, 1);
    }

    @Test
    public void testCyclicGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new EppsteinKShortestPath<>(graph).getPaths(1, 3, 6);
        List<Double> weights = Arrays.asList(2.0, 4.0, 6.0, 6.0, 8.0, 8.0);

        assertSameWeights(paths, weights);
    }

    /**
     * If the specified k is greater than the total number of paths between source and target, a
     * list of all existing paths should be returned and no exception should be thrown.
     */
    @Test
    public void testLessThanKPaths()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph1);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new EppsteinKShortestPath<>(graph).getPaths(1, 12, 12);
        List<Double> weights =
            Arrays.asList(55.0, 58.0, 59.0, 61.0, 62.0, 64.0, 65.0, 68.0, 68.0, 71.0);

        assertSameWeights(paths, weights);
    }

    private void assertSameWeights(
        List<GraphPath<Integer, DefaultWeightedEdge>> paths, List<Double> weights)
    {
        assertEquals(weights.size(), paths.size());
        for (int i = 0; i < paths.size(); i++) {
            assertEquals(weights.get(i), paths.get(i).getWeight(), 1e-9);
        }
    }

    private void readGraph(Graph<Integer, DefaultWeightedEdge> graph, int[][] representation)
    {
        for (int[] ints : representation) {
            Graphs.addEdgeWithVertices(graph, ints[0], ints[1], ints[2]);
        }
    }
}
