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
package org.jgrapht.alg.interfaces;

import org.jgrapht.GraphPath;

import java.util.Set;

/**
 * An algorithm which computes shortest paths from all sources to all targets.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public interface ManyToManyShortestPathsAlgorithm<V, E> {

    /**
     * Computes shortest paths from all vertices in {@code sources}
     * to all vertices in {@code targets}.
     *
     * @param sources list if sources vertices
     * @param targets list of target vertices
     * @return computed shortest paths
     */
    ManyToManyShortestPaths<V, E> getManyTwoManyPaths(Set<V> sources, Set<V> targets);

    /**
     * A set of paths from all sources vertices to all target vertices.
     *
     * @param <V> the graph verticex type
     * @param <E> the graph edge type
     */
    interface ManyToManyShortestPaths<V, E> {

        /**
         * Return the path from the {@code source} vertex to the {@code target} vertex.
         * If no such path exists, null is returned.
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
         * {@link Double#POSITIVE_INFINITY} in case no such path exists
         */
        double getWeight(V source, V target);
    }
}
