/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

/**
 * An implementation of <a href="http://mathworld.wolfram.com/DijkstrasAlgorithm.html">Dijkstra's
 * shortest path algorithm</a> using a Fibonacci heap.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 */
public final class DijkstraShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    private final double radius;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     * 
     * @param graph the graph
     */
    public DijkstraShortestPath(Graph<V, E> graph)
    {
        this(graph, Double.POSITIVE_INFINITY);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     * @param radius limit on path length, or Double.POSITIVE_INFINITY for unbounded search
     */
    public DijkstraShortestPath(Graph<V, E> graph, double radius)
    {
        super(graph);
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        if (source.equals(sink)) {
            return createEmptyPath(source, sink);
        }

        DijkstraClosestFirstIterator<V, E> it =
            new DijkstraClosestFirstIterator<>(graph, source, radius);

        while (it.hasNext()) {
            V vertex = it.next();
            if (vertex.equals(sink)) {
                break;
            }
        }

        return it.getPaths().getPath(sink);
    }

    /**
     * {@inheritDoc}
     *
     * Note that in the case of Dijkstra's algorithm it is more efficient to compute all
     * single-source shortest paths using this method than repeatedly invoking
     * {@link #getPath(Object, Object)} for the same source but different sink vertex.
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        DijkstraClosestFirstIterator<V, E> it =
            new DijkstraClosestFirstIterator<>(graph, source, radius);

        while (it.hasNext()) {
            it.next();
        }

        return it.getPaths();
    }

    /**
     * Find a path between two vertices. For a more advanced search (e.g. limited by radius), use
     * the constructor instead.
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
        return new DijkstraShortestPath<>(graph).getPath(source, sink);
    }

}
