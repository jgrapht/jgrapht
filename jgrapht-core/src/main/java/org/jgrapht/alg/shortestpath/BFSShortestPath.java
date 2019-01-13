/*
 * (C) Copyright 2018-2019, by Karri Sai Satish Kumar Reddy and Contributors.
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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

/**
 * The BFS Shortest Path algorithm.
 *
 * <p>
 * An implementation of <a href="https://www.geeksforgeeks.org/shortest-path-unweighted-graph/">BFS
 * shortest path algorithm</a> to compute shortest paths from a single source vertex to all other 
 * vertices in an unweighted graph.
 *
 * <p>
 * The running time is $O(|V|+|E|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Karri Sai Satish Kumar Reddy
 */
public class BFSShortestPath<V,E>
    extends
    BaseShortestPathAlgorithm<V, E>
{

    public BFSShortestPath(Graph<V, E> graph)
    {
        super(graph);
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        /*
         * Initialize distance and predecessor.
         */
        int n = graph.vertexSet().size();
        Map<V, Double> distance = new HashMap<>();
        Map<V, E> pred = new HashMap<>();
        for (V v : graph.vertexSet()) {
            distance.put(v, Double.POSITIVE_INFINITY);
        }
        distance.put(source, 0d);
        
        /*
         * Declaring queue and visited array 
         */
        Queue<V> queue = new LinkedList<>(); 
        HashMap<V, Boolean> visited = new HashMap<V, Boolean>();
        
        
        queue.add(source);
        visited.put(source, true);
        
        /*
         * It takes the top most vertex from queue,relax its outgoing edges,updates 
         * the distance of the neighbouring vertices ans pushes them into queue
         */
        while(!queue.isEmpty())
        {
            V v = queue.poll();
            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if(!visited.containsKey(u))
                {
                    visited.put(u, true);
                    queue.add(u);
                    double newDist = distance.get(v) + graph.getEdgeWeight(e);
                    distance.put(u, newDist);
                    pred.put(u, e);
                }
            }
        }
        
        
        /*
         * Transform result
         */
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = new HashMap<>();
        for (V v : graph.vertexSet()) {
            distanceAndPredecessorMap.put(v, Pair.of(distance.get(v), pred.get(v)));
        }
        return new TreeSingleSourcePathsImpl<>(graph, source, distanceAndPredecessorMap);
        
        
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
        
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        return getPaths(source).getPath(sink);   
    }
    
    /**
     * Find a path between two vertices.
     * 
     * @param graph the graph to be searched
     * @param source the vertex at which the path should start
     * @param sink the vertex at which the path should end
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return a shortest path, or null if no path exists
     */
    public static <V, E> GraphPath<V, E> findPathBetween(Graph<V, E> graph, V source, V sink)
    {
        return new BFSShortestPath<>(graph).getPath(source, sink);
    }

}
