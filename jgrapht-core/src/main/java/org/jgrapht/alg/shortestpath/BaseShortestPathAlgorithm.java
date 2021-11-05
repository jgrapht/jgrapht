/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * A base implementation of the shortest path interface.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
abstract class BaseShortestPathAlgorithm<V, E>
    implements
    ShortestPathAlgorithm<V, E>
{
    /**
     * Error message for reporting the existence of a negative-weight cycle.
     */
    protected static final String GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE =
        "Graph contains a negative-weight cycle";
    /**
     * Error message for reporting that a source vertex is missing.
     */
    protected static final String GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX =
        "Graph must contain the source vertex!";
    /**
     * Error message for reporting that a sink vertex is missing.
     */
    protected static final String GRAPH_MUST_CONTAIN_THE_SINK_VERTEX =
        "Graph must contain the sink vertex!";

    /**
     * The underlying graph.
     */
    protected final Graph<V, E> graph;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     * 
     * @param graph the graph
     */
    public BaseShortestPathAlgorithm(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("graph must contain the source vertex");
        }

        Map<V, GraphPath<V, E>> paths = new HashMap<>();
        for (V v : graph.vertexSet()) {
            paths.put(v, getPath(source, v));
        }
        return new ListSingleSourcePathsImpl<>(graph, source, paths);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPathWeight(V source, V sink)
    {
        GraphPath<V, E> p = getPath(source, sink);
        if (p == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return p.getWeight();
        }
    }

    /**
     * Create an empty path. Returns null if the source vertex is different than the target vertex.
     * 
     * @param source the source vertex
     * @param sink the sink vertex
     * @return an empty path or null null if the source vertex is different than the target vertex
     */
    protected final GraphPath<V, E> createEmptyPath(V source, V sink)
    {
        if (source.equals(sink)) {
            return GraphWalk.singletonWalk(graph, source, 0d);
        } else {
            return null;
        }
    }

}
