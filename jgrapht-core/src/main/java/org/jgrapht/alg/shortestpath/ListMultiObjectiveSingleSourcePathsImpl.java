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
import org.jgrapht.alg.interfaces.MultiObjectiveShortestPathAlgorithm.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;

/**
 * An implementation of {@link MultiObjectiveSingleSourcePaths} which stores one list of paths per
 * vertex.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class ListMultiObjectiveSingleSourcePathsImpl<V, E>
    implements
    MultiObjectiveSingleSourcePaths<V, E>,
    Serializable
{
    private static final long serialVersionUID = -6213225353391554721L;

    /**
     * The graph
     */
    protected Graph<V, E> graph;

    /**
     * The source vertex of all paths
     */
    protected V source;

    /**
     * One path per vertex
     */
    protected Map<V, List<GraphPath<V, E>>> paths;

    /**
     * Construct a new instance.
     * 
     * @param graph the graph
     * @param source the source vertex
     * @param paths a list of paths per target vertex
     */
    public ListMultiObjectiveSingleSourcePathsImpl(
        Graph<V, E> graph, V source, Map<V, List<GraphPath<V, E>>> paths)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
        this.source = Objects.requireNonNull(source, "Source vertex is null");
        this.paths = Objects.requireNonNull(paths, "Paths are null");
    }

    @Override
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    @Override
    public V getSourceVertex()
    {
        return source;
    }

    @Override
    public List<GraphPath<V, E>> getPaths(V targetVertex)
    {
        List<GraphPath<V, E>> p = paths.get(targetVertex);
        if (p == null) {
            if (source.equals(targetVertex)) {
                return Collections.singletonList(GraphWalk.singletonWalk(graph, source, 0d));
            } else {
                return Collections.emptyList();
            }
        } else {
            return p;
        }
    }

}
