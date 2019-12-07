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
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Naive algorithm for many-to-many shortest paths problem.
 *
 * <p>
 * Complexity of the algorithm is $O(|S|)(V\log V + E)$, where $S$ is the set of source vertices,
 * $V$ is the set of graph vertices and $E$ is the set of graph edges of the graph.
 *
 * <p>
 * For each source vertex a single source shortest paths search is performed, which is stopped
 * as soon as all target vertices are reached. Shortest paths trees is constructed using
 * {@link DijkstraClosestFirstIterator}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public class DefaultManyToManyShortestPaths<V, E> extends BaseManyToManyShortestPaths<V, E> {
    /**
     * The underlying graph.
     */
    private final Graph<V, E> graph;

    /**
     * Constructs an instance of the algorithm for a given {@code graph}.
     *
     * @param graph underlying graph
     */
    public DefaultManyToManyShortestPaths(Graph<V, E> graph) {
        this.graph = graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManyToManyShortestPaths<V, E> getManyTwoManyPaths(Set<V> sources, Set<V> targets) {
        Objects.requireNonNull(sources, "sources cannot be null!");
        Objects.requireNonNull(targets, "targets cannot be null!");

        Map<V, ShortestPathAlgorithm.SingleSourcePaths<V, E>> searchSpaces = new HashMap<>();

        Set<V> targetsSet = new HashSet<>(targets);

        for (V source : sources) {
            searchSpaces.put(source, getShortestPathsTree(graph, source, targetsSet));
        }

        return new DefaultManyToManyShortestPathsImpl(sources, targets, searchSpaces);
    }

    /**
     * Implementation of the
     * {@link org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths}.
     * For each source vertex a single source shortest paths tree is stored. It is used to retrieve
     * both actual paths and theirs weights.
     */
    private class DefaultManyToManyShortestPathsImpl implements ManyToManyShortestPaths<V, E> {

        private final Set<V> sources;
        private final Set<V> targets;

        /**
         * Map from source vertices to corresponding single source shortest path trees.
         */
        private final Map<V, ShortestPathAlgorithm.SingleSourcePaths<V, E>> searchSpaces;

        /**
         * Constructs an instance of the algorithm for the given {@code searchSpaces}.
         *
         * @param searchSpaces single source shortest paths trees map
         */
        private DefaultManyToManyShortestPathsImpl(Set<V> sources, Set<V> targets,
                                                   Map<V, ShortestPathAlgorithm.SingleSourcePaths<V, E>> searchSpaces) {
            this.sources = sources;
            this.targets = targets;
            this.searchSpaces = searchSpaces;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GraphPath<V, E> getPath(V source, V target) {
            Objects.requireNonNull(source, "source should not be null!");
            Objects.requireNonNull(target, "target should not be null!");

            if (!sources.contains(source) || !targets.contains(target)) {
                throw new IllegalArgumentException("paths between " + source + " and " + target + " not computed");
            }
            return searchSpaces.get(source).getPath(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight(V source, V target) {
            Objects.requireNonNull(source, "source should not be null!");
            Objects.requireNonNull(target, "target should not be null!");

            if (!sources.contains(source) || !targets.contains(target)) {
                throw new IllegalArgumentException("paths between " + source + " and " + target + " not computed");
            }
            return searchSpaces.get(source).getWeight(target);
        }
    }
}
