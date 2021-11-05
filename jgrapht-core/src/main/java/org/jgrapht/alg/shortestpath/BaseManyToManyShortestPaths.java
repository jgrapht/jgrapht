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
 * Base class for many-to-many shortest paths algorithms. Currently extended by
 * {@link CHManyToManyShortestPaths} and {@link DijkstraManyToManyShortestPaths}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @author Dimitrios Michail
 */
abstract class BaseManyToManyShortestPaths<V, E>
    implements
    ManyToManyShortestPathsAlgorithm<V, E>
{

    protected final Graph<V, E> graph;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     */
    public BaseManyToManyShortestPaths(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
        return getManyToManyPaths(Collections.singleton(source), Collections.singleton(sink))
            .getPath(source, sink);
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
     * Computes shortest paths tree starting at {@code source} and stopping as soon as all of the
     * {@code targets} are reached. Here the {@link DijkstraClosestFirstIterator} is used.
     *
     * @param graph a graph
     * @param source source vertex
     * @param targets target vertices
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return shortest paths starting from {@code source} and reaching all {@code targets}
     */
    protected static <V, E> ShortestPathAlgorithm.SingleSourcePaths<V, E> getShortestPathsTree(
        Graph<V, E> graph, V source, Set<V> targets)
    {
        DijkstraClosestFirstIterator<V, E> iterator =
            new DijkstraClosestFirstIterator<>(graph, source);

        int reachedTargets = 0;
        while (iterator.hasNext() && reachedTargets < targets.size()) {
            if (targets.contains(iterator.next())) {
                ++reachedTargets;
            }
        }

        return iterator.getPaths();
    }
}
