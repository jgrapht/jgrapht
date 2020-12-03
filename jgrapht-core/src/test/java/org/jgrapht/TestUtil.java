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

import org.jgrapht.alg.util.Pair;
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

    /**
     * Add vertices to a graph.
     *
     * @param graph graph to add the vertices to
     * @param vertices vertices to be added to the graph
     * @param <V> vertex type
     * @param <E> edge type
     */
    @SafeVarargs
    public static <V, E> void addVertices(final Graph<V, E> graph, final V... vertices) {
        for (V vertex : vertices)
            graph.addVertex(vertex);
    }

    /**
     * Add edges to a graph. If any of vertices used to describe the edges to be added are not contained in the graph
     * yet, they will be added automatically.
     *
     * @param graph graph to add the edges to
     * @param edges pairs of vertices describing edges to be added to the graph
     * @param <V> vertex type
     * @param <E> edge type
     */
    @SafeVarargs
    public static <V, E> void addEdges(final Graph<V, E> graph, final Pair<V,V>... edges) {
        for (Pair<V, V> edge : edges) {
            graph.addVertex(edge.getFirst());
            graph.addVertex(edge.getSecond());
            graph.addEdge(edge.getFirst(), edge.getSecond());
        }
    }

    /**
     * Add vertices to a graph and then connect them by a simple circle. If this method is called upon an empty graph,
     * the cycle and hence the graph shall be Hamiltonian.
     *
     * @param graph graph to add the cycle to
     * @param vertices vertices to be added to the graph
     * @param <V> vertex type
     * @param <E> edge type
     */
    @SafeVarargs
    public static <V, E> void addCycle(final Graph<V, E> graph, final V... vertices) {
        addVertices(graph, vertices);
        int numVertices = vertices.length;
        for (int i = 0; i < numVertices; i++)
            graph.addEdge(vertices[i], vertices[(i + 1) % numVertices]);
    }

    /**
     * Backup all vertices and edges from a graph, remove them and then re-add them in random order. This is useful for
     * testing algorithms strongly depending on the order in which they traverse a graph, as it can help to avoid blind
     * spots.
     *
     * @param graph graph to be randomized
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V, E> void randomizeGraph(final Graph<V, E> graph) {
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
