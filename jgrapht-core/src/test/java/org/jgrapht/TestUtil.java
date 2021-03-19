/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht;

import org.jgrapht.graph.*;

/**
 * Test related utility methods.
 */
public class TestUtil
{

    public static void constructGraph(Graph<Integer, DefaultEdge> graph, int[][] edges)
    {
        boolean weighted = edges.length > 0 && edges[0].length > 2;
        for (int[] edge : edges) {
            DefaultEdge graphEdge = Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
            if (weighted) {
                graph.setEdgeWeight(graphEdge, edge[2]);
            }
        }
    }

    public static <V> void constructGraph(Graph<V, DefaultEdge> graph, V[][] edges)
    {
        for (V[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        }
    }

    public static <V> Graph<V, DefaultEdge> createUndirected(V[][] edges)
    {
        Graph<V, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

    public static Graph<Integer, DefaultEdge> createUndirected(int[][] edges)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

    public static Graph<Integer, DefaultEdge> createDirected(int[][] edges)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

    public static <V> Graph<V, DefaultEdge> createDirected(V[][] edges)
    {
        Graph<V, DefaultEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

    public static Graph<Integer, DefaultEdge> createPseudograph(int[][] edges)
    {
        Graph<Integer, DefaultEdge> graph = new WeightedPseudograph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

    public static <V> Graph<V, DefaultEdge> createPseudograph(V[][] edges)
    {
        Graph<V, DefaultEdge> graph = new WeightedPseudograph<>(DefaultEdge.class);
        constructGraph(graph, edges);
        return graph;
    }

}
