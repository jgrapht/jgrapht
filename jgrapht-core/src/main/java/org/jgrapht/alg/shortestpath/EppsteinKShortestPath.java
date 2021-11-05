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
 * Implementation of the Eppstein`s algorithm for finding $k$ shortest path between two vertices in
 * a graph.
 *
 * <p>
 * The algorithm is originally described in: David Eppstein. 1999. Finding the k Shortest Paths.
 * SIAM J. Comput. 28, 2 (February 1999), 652-673. DOI=http://dx.doi.org/10.1137/S0097539795290477.
 *
 * <p>
 * The main advantage ot this algorithm is that it achieves the complexity of $O(m + n\log n + k\log
 * k)$ while guaranteeing that the paths are produced in sorted order by weight, where $m$ is the
 * number of edges in the graph, $n$ is the number of vertices in the graph and $k$ is the number of
 * paths needed.
 *
 * <p>
 * This implementation can only be used for directed simple graphs.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see EppsteinShortestPathIterator
 */
public class EppsteinKShortestPath<V, E>
    implements
    KShortestPathAlgorithm<V, E>
{
    /**
     * Underlying graph.
     */
    private final Graph<V, E> graph;

    /**
     * Constructs the algorithm instance for the given {@code graph}.
     *
     * @param graph graph
     */
    public EppsteinKShortestPath(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
    }

    /**
     * Computes {@code k} shortest paths between {@code source} and {@code sink}. If the number of
     * paths is denoted by $n$, the method returns $m = min\{k, n\}$ such paths. The paths are
     * produced in sorted order by weights.
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
            throw new IllegalArgumentException("k must be non-negative");
        }
        List<GraphPath<V, E>> result = new ArrayList<>();
        EppsteinShortestPathIterator<V, E> iterator =
            new EppsteinShortestPathIterator<>(graph, source, sink);
        for (int i = 0; i < k && iterator.hasNext(); i++) {
            result.add(iterator.next());
        }
        return result;
    }
}
