/*
 * (C) Copyright 2019-2019, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the Eppstein`s algorithm for finding
 * $k$ shortest path between two vertices in a graph.
 *
 * <p>
 * This implementation can only be used for directed simple graphs.
 *
 * <p>
 * The algorithm is originally described in
 * <a href="https://www.ics.uci.edu/~eppstein/pubs/Epp-SJC-98.pdf">this paper</a>.
 * It achieves the state-of-the-art complexity of $O(m + n\log n + k\log k)$ if the paths
 * are produced in sorted order, where $m$ is the amount of edges in the graph, $n$ is the
 * amount of vertices in the graph and $k$ is the amount of paths needed.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see EppsteinPathsIterator
 */
public class EppsteinKShortestPaths<V, E> implements KShortestPathAlgorithm<V, E> {
    /**
     * Underlying graph.
     */
    private final Graph<V, E> graph;

    /**
     * Constructs the algorithm instance for the given {@code graph}.
     *
     * @param graph graph
     */
    public EppsteinKShortestPaths(Graph<V, E> graph) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
    }

    /**
     * Computes {@code k} shortest paths between {@code source}
     * and {@code sink}. If the amount of paths is denoted by $n$,
     * the method returns $m = min{k, n}$ such paths. The paths are
     * produced in sorted order by weights.
     *
     * @param source the source vertex
     * @param sink   the target vertex
     * @param k      the number of shortest paths to return
     * @return a list of k shortest paths
     */
    @Override
    public List<GraphPath<V, E>> getPaths(V source, V sink, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("k should be positive");
        }
        List<GraphPath<V, E>> result = new ArrayList<>();
        EppsteinPathsIterator<V, E> iterator =
                new EppsteinPathsIterator<>(graph, source, sink);
        for (int i = 0; i < k && iterator.hasNext(); i++) {
            result.add(iterator.next());
        }
        return result;
    }
}
