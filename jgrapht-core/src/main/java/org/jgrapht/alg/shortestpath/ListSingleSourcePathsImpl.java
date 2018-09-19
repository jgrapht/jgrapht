/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;

/**
 * An implementation of {@link SingleSourcePaths} which stores one path per vertex.
 * 
 * <p>
 * This is an explicit representation which stores all paths. For a more compact representation see
 * {@link TreeSingleSourcePathsImpl}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class ListSingleSourcePathsImpl<V, E>
    implements
    SingleSourcePaths<V, E>,
    Serializable
{
    private static final long serialVersionUID = -60070018446561686L;

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
    protected Map<V, GraphPath<V, E>> paths;

    /**
     * Construct a new instance.
     * 
     * @param graph the graph
     * @param source the source vertex
     * @param paths one path per target vertex
     */
    public ListSingleSourcePathsImpl(Graph<V, E> graph, V source, Map<V, GraphPath<V, E>> paths)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
        this.source = Objects.requireNonNull(source, "Source vertex is null");
        this.paths = Objects.requireNonNull(paths, "Paths are null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getSourceVertex()
    {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWeight(V targetVertex)
    {
        GraphPath<V, E> p = paths.get(targetVertex);
        if (p == null) {
            if (source.equals(targetVertex)) {
                return 0d;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        } else {
            return p.getWeight();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V targetVertex)
    {
        GraphPath<V, E> p = paths.get(targetVertex);
        if (p == null) {
            if (source.equals(targetVertex)) {
                return GraphWalk.singletonWalk(graph, source, 0d);
            } else {
                return null;
            }
        } else {
            return p;
        }
    }

}
