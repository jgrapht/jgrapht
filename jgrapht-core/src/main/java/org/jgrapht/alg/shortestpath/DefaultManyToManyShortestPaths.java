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
import java.util.function.*;

/**
 * Naive algorithm for many-to-many shortest paths problem using.
 *
 * <p>
 * Time complexity of the algorithm is $O(|S||T|C)$, where $S$ is the set of source vertices, $T$ is
 * the set of target vertices and $C$ is the complexity of the
 * {@link ShortestPathAlgorithm#getPath(Object, Object)} method of the provided implementation.
 *
 * <p>
 * For every pair of {@code source} and {@code target} vertices computes a shortest path between
 * them using provided implementation of {@link ShortestPathAlgorithm}. By default this
 * implementation uses {@link BidirectionalDijkstraShortestPath}. If desired, a different
 * implementation can be provided via the {@code function} constructor parameter.
 *
 * <p>
 * The computation complexity of the algorithm consists of two main components - the $|S||T|$
 * multiplier and the $C$ multiplier. This yields two bottlenecks for the algorithm. First of them
 * is the situation when the total number calls to
 * {@link ShortestPathAlgorithm#getPath(Object, Object)} is large. The second situation is when the
 * complexity of the individual call to {@link ShortestPathAlgorithm#getPath(Object, Object)} takes
 * a lot of time. Therefore the ideal use cases for this algorithm are small graphs or large graphs
 * with low total number of source and target vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see DijkstraManyToManyShortestPaths
 * @see CHManyToManyShortestPaths
 */
public class DefaultManyToManyShortestPaths<V, E>
    extends
    BaseManyToManyShortestPaths<V, E>
{

    /**
     * Provides implementation of {@link ShortestPathAlgorithm} for a given graph.
     */
    private final Function<Graph<V, E>, ShortestPathAlgorithm<V, E>> function;

    /**
     * Constructs a new instance of the algorithm for a given {@code graph}. The {@code function} is
     * defaulted to returning {@link BidirectionalDijkstraShortestPath}.
     *
     * @param graph a graph
     */
    public DefaultManyToManyShortestPaths(Graph<V, E> graph)
    {
        this(graph, g -> new BidirectionalDijkstraShortestPath<>(g));
    }

    /**
     * Constructs a new instance of the algorithm for a given {@code graph} and {@code function}.
     *
     * @param graph a graph
     * @param function provides implementation of {@link ShortestPathAlgorithm}
     */
    public DefaultManyToManyShortestPaths(
        Graph<V, E> graph, Function<Graph<V, E>, ShortestPathAlgorithm<V, E>> function)
    {
        super(graph);
        this.function = function;
    }

    @Override
    public ManyToManyShortestPaths<V, E> getManyToManyPaths(Set<V> sources, Set<V> targets)
    {
        Objects.requireNonNull(sources, "sources cannot be null!");
        Objects.requireNonNull(targets, "targets cannot be null!");

        ShortestPathAlgorithm<V, E> algorithm = function.apply(graph);
        Map<V, Map<V, GraphPath<V, E>>> pathMap = new HashMap<>();

        for (V source : sources) {
            pathMap.put(source, new HashMap<>());
        }

        for (V source : sources) {
            for (V target : targets) {
                pathMap.get(source).put(target, algorithm.getPath(source, target));
            }
        }

        return new DefaultManyToManyShortestPathsImpl<>(sources, targets, pathMap);
    }

    /**
     * Implementation of the
     * {@link org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths}.
     * For each pair of source and target vertices stores a corresponding path between them.
     */
    static class DefaultManyToManyShortestPathsImpl<V, E>
        extends
        BaseManyToManyShortestPathsImpl<V, E>
    {

        /**
         * Map with paths between sources and targets.
         */
        private final Map<V, Map<V, GraphPath<V, E>>> pathsMap;

        /**
         * Constructs an instance of the algorithm for the given {@code sources}, {@code targets}
         * and {@code pathsMap}.
         *
         * @param sources source vertices
         * @param targets target vertices
         * @param pathsMap map with paths between sources and targets
         */
        DefaultManyToManyShortestPathsImpl(
            Set<V> sources, Set<V> targets, Map<V, Map<V, GraphPath<V, E>>> pathsMap)
        {
            super(sources, targets);
            this.pathsMap = pathsMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GraphPath<V, E> getPath(V source, V target)
        {
            assertCorrectSourceAndTarget(source, target);
            return pathsMap.get(source).get(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight(V source, V target)
        {
            assertCorrectSourceAndTarget(source, target);

            GraphPath<V, E> path = pathsMap.get(source).get(target);
            if (path == null) {
                return Double.POSITIVE_INFINITY;
            }
            return path.getWeight();
        }
    }
}
