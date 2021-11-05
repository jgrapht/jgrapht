/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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

import java.util.*;

/**
 * Implementation of Yen`s algorithm for finding $k$ shortest loopless paths.
 *
 * <p>
 * The time complexity of the algorithm is $O(kn(m + n log n))$, where $n$ is the number of vertices
 * in the graph, $m$ is the number of edges in the graph and $k$ is the number of paths needed.
 *
 * <p>
 * The algorithm is originally described in: Q. V. Martins, Ernesto and M. B. Pascoal, Marta.
 * (2003). A new implementation of Yen’s ranking loopless paths algorithm. Quarterly Journal of the
 * Belgian, French and Italian Operations Research Societies. 1. 121-133. 10.1007/s10288-002-0010-2.
 *
 * <p>
 * The implementation iterates over the existing loopless path between the {@code source} and the
 * {@code sink} and forms the resulting list. It is possible to provide a {@link PathValidator} to
 * filter the resulting path list
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see YenShortestPathIterator
 * @see PathValidator
 */
public class YenKShortestPath<V, E>
    implements
    KShortestPathAlgorithm<V, E>
{
    /**
     * Underlying graph.
     */
    private final Graph<V, E> graph;

    /**
     * Provides validation for the paths which will be computed. If the validator is {@code null},
     * this means that all paths are valid.
     */
    private PathValidator<V, E> pathValidator;

    /**
     * Constructs an instance of the algorithm for the given {@code graph}.
     *
     * @param graph graph
     */
    public YenKShortestPath(Graph<V, E> graph)
    {
        this(graph, null);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code graph} and
     * {@code pathValidator}.
     *
     * @param graph graph
     * @param pathValidator validator for computed paths
     */
    public YenKShortestPath(Graph<V, E> graph, PathValidator<V, E> pathValidator)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
        this.pathValidator = pathValidator;
    }

    /**
     * Computes {@code k} shortest loopless paths between {@code source} and {@code sink}. If the
     * overall number of such paths is denoted by $n$, the method returns $m = min\{k, n\}$ such
     * paths. The paths are produced in sorted order by weights.
     *
     * @param source the source vertex
     * @param sink the target vertex
     * @param k the number of shortest paths to return
     * @return a list of k shortest paths
     */
    @Override
    public List<GraphPath<V, E>> getPaths(V source, V sink, int k)
    {
        if (k < 0) {
            throw new IllegalArgumentException("k should be positive");
        }
        List<GraphPath<V, E>> result = new ArrayList<>();
        YenShortestPathIterator<V, E> iterator =
            new YenShortestPathIterator<>(graph, source, sink, pathValidator);
        for (int i = 0; i < k && iterator.hasNext(); i++) {
            result.add(iterator.next());
        }
        return result;
    }
}
