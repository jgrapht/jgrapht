/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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
import java.util.stream.*;

/**
 * Deprecated. Should instead use BellmanFordKShortestSimplePaths.
 * 
 * The algorithm determines the k shortest simple paths in increasing order of weight. Weights can
 * be negative (but no negative cycle is allowed), and paths can be constrained by a maximum number
 * of edges. Graphs with multiple (parallel) edges are allowed.
 *
 * <p>
 * The algorithm is a variant of the Bellman-Ford algorithm but instead of only storing the best
 * path it stores the "k" best paths at each pass, yielding a complexity of $O(k \cdot n \cdot
 * (m^2))$ where $m$ is the number of edges and $n$ is the number of vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *           
 * @deprecated Moved/renamed the class to new name BellmanFordKShortestSimplePaths
 * @see BellmanFordKShortestSimplePaths
 *
 */
@Deprecated
public class KShortestSimplePaths<V, E>
    implements
    KShortestPathAlgorithm<V, E>
{
    private final BellmanFordKShortestSimplePaths<V, E> bellmanFordKShortestSimplePaths;
    
    /**
     * Graph on which shortest paths are searched.
     */
    private Graph<V, E> graph;

    private int nMaxHops;

    private PathValidator<V, E> pathValidator;

    /**
     * Constructs an object to compute ranking shortest paths in a graph.
     *
     * @param graph graph on which shortest paths are searched
     */
    public KShortestSimplePaths(Graph<V, E> graph)
    {
        bellmanFordKShortestSimplePaths = new BellmanFordKShortestSimplePaths<>(graph);
    }

    /**
     * Constructs an object to compute ranking shortest paths in a graph. A non-null path validator
     * may be used to accept/deny paths according to some external logic. These validations will be
     * used in addition to the basic path validations which are that the path is from start to
     * target with no loops.
     *
     * @param graph graph on which shortest paths are searched.
     * @param pathValidator the path validator to use
     */
    public KShortestSimplePaths(Graph<V, E> graph, PathValidator<V, E> pathValidator)
    {
        bellmanFordKShortestSimplePaths = new BellmanFordKShortestSimplePaths<>(graph, pathValidator);
    }

    /**
     * Constructs an object to calculate ranking shortest paths in a graph.
     *
     * @param graph graph on which shortest paths are searched
     * @param nMaxHops maximum number of edges of the calculated paths
     *
     * @throws IllegalArgumentException if nMaxHops is negative or 0.
     */
    public KShortestSimplePaths(Graph<V, E> graph, int nMaxHops)
    {
        bellmanFordKShortestSimplePaths = new BellmanFordKShortestSimplePaths<>(graph, nMaxHops);
    }

    /**
     * Constructs an object to calculate ranking shortest paths in a graph. A non-null path
     * validator may be used to accept/deny paths according to some external logic. These
     * validations will be used in addition to the basic path validations which are that the path is
     * from start to target with no loops.
     *
     * @param graph graph on which shortest paths are searched
     * @param nMaxHops maximum number of edges of the calculated paths
     * @param pathValidator the path validator to use
     *
     * @throws IllegalArgumentException if nMaxHops is negative or 0.
     */
    public KShortestSimplePaths(Graph<V, E> graph, int nMaxHops, PathValidator<V, E> pathValidator)
    {
        bellmanFordKShortestSimplePaths = new BellmanFordKShortestSimplePaths<>(graph, nMaxHops, pathValidator);
    }

    /**
     * Returns a list of the $k$ shortest simple paths in increasing order of weight.
     *
     * @param startVertex source vertex of the calculated paths.
     * @param endVertex target vertex of the calculated paths.
     *
     * @return an iterator of paths between the start vertex and the end vertex
     * @throws IllegalArgumentException if the graph does not contain the startVertex or the
     *         endVertex
     * @throws IllegalArgumentException if the startVertex and the endVertex are the same vertices
     * @throws IllegalArgumentException if k is negative or zero
     */
    @Override
    public List<GraphPath<V, E>> getPaths(V startVertex, V endVertex, int k)
    {
        return bellmanFordKShortestSimplePaths.getPaths(startVertex, endVertex, k);
    }

}
