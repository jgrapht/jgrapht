/*
 * (C) Copyright 2020-2023, by Timofey Chudakov and Contributors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @SafeVarargs
    public static <V, E> void addVertices(final Graph<V, E> graph, final V... vertices) {
        for (V vertex : vertices)
            graph.addVertex(vertex);
    }

    @SafeVarargs
    public static <V, E> void addHamiltonianCycle(final Graph<V, E> graph, final V... vertices) {
        addVertices(graph, vertices);
        int numVertices = vertices.length;
        for (int i = 0; i < numVertices; i++)
            graph.addEdge(vertices[i], vertices[(i + 1) % numVertices]);
    }

    public static <V, E> void randomiseGraph(final Graph<V, E> graph) {
        // Backup + remove vertices and edges
        List<V> vertices = new ArrayList<>(graph.vertexSet());
        List<EdgeInfo<V, E>> edgeInfos = EdgeInfo.getEdgeInfos(graph);
        graph.removeAllVertices(vertices);
        assert graph.vertexSet().size() == 0;
        assert graph.edgeSet().size() == 0;

        // Shuffle + re-add vertices and edges
        Collections.shuffle(vertices);
        Collections.shuffle(edgeInfos);
        vertices.forEach(graph::addVertex);
        edgeInfos.forEach(edgeInfo ->
            graph.addEdge(edgeInfo.getSourceVertex(), edgeInfo.getTargetVertex(), edgeInfo.getEdge())
        );
    }

}
