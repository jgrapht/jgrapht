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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;

import java.util.*;

/**
 * An algorithm which computes shortest paths from all sources to all targets.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public interface ManyToManyShortestPathsAlgorithm<V, E>
    extends
    ShortestPathAlgorithm<V, E>
{

    /**
     * Computes shortest paths from all vertices in {@code sources} to all vertices in
     * {@code targets}.
     *
     * @param sources list of sources vertices
     * @param targets list of target vertices
     * @return computed shortest paths
     */
    ManyToManyShortestPaths<V, E> getManyToManyPaths(Set<V> sources, Set<V> targets);

    /**
     * A set of paths from all sources vertices to all target vertices.
     *
     * @param <V> the graph vertices type
     * @param <E> the graph edge type
     */
    interface ManyToManyShortestPaths<V, E>
    {

        /**
         * Returns the set of source vertices for which this many-to-many shortest paths were
         * computed.
         *
         * @return the set of source vertices
         */
        Set<V> getSources();

        /**
         * Returns the set of target vertices for which this many-to-many shortest paths were
         * computed.
         *
         * @return the set of target vertices
         */
        Set<V> getTargets();

        /**
         * Return the path from the {@code source} vertex to the {@code target} vertex. If no such
         * path exists, null is returned.
         *
         * @param source source vertex
         * @param target target vertex
         * @return path between {@code source} and {@code target} or null if no such path exists
         */
        GraphPath<V, E> getPath(V source, V target);

        /**
         * Return the weight of the path from the {@code source} vertex to the {@code target}vertex
         * or {@link Double#POSITIVE_INFINITY} if there is no such path in the graph. The weight of
         * the path between a vertex and itself is always zero.
         *
         * @param source source vertex
         * @param target target vertex
         * @return the weight of the path between source and sink vertices or
         *         {@link Double#POSITIVE_INFINITY} in case no such path exists
         */
        double getWeight(V source, V target);
    }

    /**
     * Base class for many-to-many shortest paths implementations.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    abstract class BaseManyToManyShortestPathsImpl<V, E>
        implements
        ManyToManyShortestPaths<V, E>
    {
        /**
         * Set of source vertices.
         */
        private final Set<V> sources;
        /**
         * Set of source vertices.
         */
        private final Set<V> targets;

        @Override
        public Set<V> getSources()
        {
            return sources;
        }

        @Override
        public Set<V> getTargets()
        {
            return targets;
        }

        /**
         * Constructs an instance for the given {@code sources} and {@code targets}.
         *
         * @param sources source vertices
         * @param targets target vertices
         */
        protected BaseManyToManyShortestPathsImpl(Set<V> sources, Set<V> targets)
        {
            this.sources = sources;
            this.targets = targets;
        }

        /**
         * Checks that {@code source} and {@code target} are not null and are present in the
         * {@code graph}.
         *
         * @param source a source vertex
         * @param target a target vertex
         */
        protected void assertCorrectSourceAndTarget(V source, V target)
        {
            Objects.requireNonNull(source, "source should not be null!");
            Objects.requireNonNull(target, "target should not be null!");

            if (!sources.contains(source) || !targets.contains(target)) {
                throw new IllegalArgumentException(
                    "paths between " + source + " and " + target + " is not computed");
            }
        }
    }
}
