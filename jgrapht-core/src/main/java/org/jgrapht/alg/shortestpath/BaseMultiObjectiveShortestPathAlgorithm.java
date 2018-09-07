/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * A base implementation of the multi-objective shortest path interface.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
abstract class BaseMultiObjectiveShortestPathAlgorithm<V, E>
    implements
    MultiObjectiveShortestPathAlgorithm<V, E>
{
    /**
     * Error message for reporting that a source vertex is missing.
     */
    static final String GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX =
        "Graph must contain the source vertex!";
    /**
     * Error message for reporting that a sink vertex is missing.
     */
    static final String GRAPH_MUST_CONTAIN_THE_SINK_VERTEX = "Graph must contain the sink vertex!";

    /**
     * The underlying graph.
     */
    protected final Graph<V, E> graph;

    /**
     * Constructs a new instance of the algorithm for a given graph
     * 
     * @param graph the graph
     */
    public BaseMultiObjectiveShortestPathAlgorithm(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
    }

    @Override
    public MultiObjectiveSingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        Map<V, List<GraphPath<V, E>>> paths = new HashMap<>();
        for (V v : graph.vertexSet()) {
            paths.put(v, getPaths(source, v));
        }
        return new ListMultiObjectiveSingleSourcePathsImpl<>(graph, source, paths);
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
