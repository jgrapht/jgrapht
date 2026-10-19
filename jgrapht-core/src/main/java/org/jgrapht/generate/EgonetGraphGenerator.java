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
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.TreeSingleSourcePathsImpl;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsUnweightedGraph;

import java.util.Map;

/**
 * Generator that produces the egonet of a given vertex in a graph.
 *
 * <p>
 * The general egonet description that led to this implementation is described in the paper: Linton C. Freeman (1981),
 * Centered Graphs and The Structure of Ego Networks.
 *
 * <p>
 * The generator searches for paths from the given ego vertex to all vertices within the specified radius and generates
 * a new graph containing the vertices found, along with the edges connecting them.
 *
 * <p>
 * The distance of the vertices included on the resulting graph can be configured by using a custom radius, along
 * with whether the new graph should include the original ego vertex and whether new edge objects should be used.
 *
 * <p>
 * Note that this implementation works with undirected graphs by using the Breadth-First Shortest Path algorithm,
 * and with directed graphs by using Dijkstra's Shortest Path algorithm.
 *
 * @author Fotis Karagiannis
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class EgonetGraphGenerator<V, E> implements GraphGenerator<V, E, V>
{
    /**
     * Default constructor radius
     */
    public static final int DEFAULT_RADIUS = 1;
    /**
     * Default constructor ego inclusion
     */
    public static final boolean DEFAULT_INCLUDE_EGO = true;
    /**
     * Default constructor new edge usage
     */
    public static final boolean DEFAULT_USE_NEW_EDGES = true;

    private final Graph<V, E> graph;
    private final V ego;
    private final int radius;
    private final boolean includeEgo;
    private final boolean newEdges;

    /**
     * Constructor
     *
     * @param graph the given graph
     * @param ego the ego vertex (must belong to the graph)
     * @param radius the limit on path length
     * @param includeEgo include ego on the generated graph option
     * @param newEdges use new edge objects option
     */
    public EgonetGraphGenerator(Graph<V, E> graph, V ego, int radius, boolean includeEgo, boolean newEdges)
    {
        // Given graph must contain the ego vertex
        if(!graph.containsVertex(ego))
        {
            throw new IllegalArgumentException("Graph must contain the ego vertex!");
        }
        // Given graph must be directed or undirected
        // If it is weighted, AsUnweightedGraph is used
        if(graph.getType().isWeighted())
        {
            this.graph = new AsUnweightedGraph<>(GraphTests.requireDirectedOrUndirected(graph));
        }
        else
        {
            this.graph = GraphTests.requireDirectedOrUndirected(graph);
        }
        this.ego = ego;
        this.radius = radius;
        this.includeEgo = includeEgo;
        this.newEdges = newEdges;
    }

    public EgonetGraphGenerator(Graph<V, E> graph, V ego, int radius, boolean includeEgo)
    {
        this(graph, ego, radius, includeEgo, DEFAULT_USE_NEW_EDGES);
    }

    public EgonetGraphGenerator(Graph<V, E> graph, V ego, int radius)
    {
        this(graph, ego, radius, DEFAULT_INCLUDE_EGO, DEFAULT_USE_NEW_EDGES);
    }

    public EgonetGraphGenerator(Graph<V, E> graph, V ego, boolean includeEgo)
    {
        this(graph, ego, DEFAULT_RADIUS, includeEgo, DEFAULT_USE_NEW_EDGES);
    }

    public EgonetGraphGenerator(Graph<V, E> graph, V ego)
    {
        this(graph, ego, DEFAULT_RADIUS, DEFAULT_INCLUDE_EGO, DEFAULT_USE_NEW_EDGES);
    }

    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // Step 1: Find the vertexes of the egonet
        ShortestPathAlgorithm<V, E> alg;
        // use BFS for undirected graphs and Dijkstra for directed graphs
        if(graph.getType().isUndirected())
        {
            alg = new BFSShortestPath<>(graph, radius);
        }
        else
        {
            alg = new DijkstraShortestPath<>(graph, radius);
        }

        Map<V, Pair<Double, E>> searchMap = ((TreeSingleSourcePathsImpl<V, E>) alg.getPaths(ego)).getDistanceAndPredecessorMap();
        for(V v : searchMap.keySet())
        {
            target.addVertex(v);
        }
        if(!includeEgo)
        {
            target.removeVertex(ego);
        }

        // Step 2: Find the edges connected to the vertexes of the egonet
        for(V v : target.iterables().vertices())
        {
            for(E e : graph.outgoingEdgesOf(v))
            {
                V edgeTarget = graph.getEdgeTarget(e);
                // ignore edge if source==target
                if(v.equals(edgeTarget))
                {
                    continue;
                }

                if(target.containsVertex(edgeTarget))
                {
                    // add new or existing edges to the egonet graph
                    if(newEdges)
                    {
                        target.addEdge(v, edgeTarget);
                    }
                    else
                    {
                        target.addEdge(v, edgeTarget, e);
                    }
                }
            }
        }
    }
}
