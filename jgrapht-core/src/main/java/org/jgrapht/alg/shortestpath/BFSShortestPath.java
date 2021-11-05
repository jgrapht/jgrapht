/*
 * (C) Copyright 2018-2021, by Karri Sai Satish Kumar Reddy and Contributors.
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
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * The BFS Shortest Path algorithm.
 *
 * <p>
 * An implementation of <a href="https://en.wikipedia.org/wiki/Breadth-first_search">BFS shortest
 * path algorithm</a> to compute shortest paths from a single source vertex to all other vertices in
 * an unweighted graph.
 *
 * <p>
 * The running time is $O(|V|+|E|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Karri Sai Satish Kumar Reddy
 */
public class BFSShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     */
    public BFSShortestPath(Graph<V, E> graph)
    {
        super(graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        /*
         * Initialize distanceAndPredecessorMap
         */
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = new HashMap<>();
        distanceAndPredecessorMap.put(source, Pair.of(0d, null));

        /*
         * Declaring queue
         */
        Deque<V> queue = new ArrayDeque<>();
        queue.add(source);

        /*
         * Take the top most vertex from the queue, relax its outgoing edges, update the distance of
         * the neighbouring vertices and push them into the queue
         */
        while (!queue.isEmpty()) {
            V v = queue.poll();
            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (!distanceAndPredecessorMap.containsKey(u)) {
                    queue.add(u);
                    double newDist = distanceAndPredecessorMap.get(v).getFirst() + 1.0;
                    distanceAndPredecessorMap.put(u, Pair.of(newDist, e));
                }
            }
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
