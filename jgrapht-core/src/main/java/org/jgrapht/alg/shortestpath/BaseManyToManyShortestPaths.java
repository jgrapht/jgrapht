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
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.util.Set;

/**
 * Base class for many-to-many shortest paths algorithms. Currently extended by
 * {@link CHManyToManyShortestPaths} and {@link DefaultManyToManyShortestPaths}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public abstract class BaseManyToManyShortestPaths<V, E> implements ManyToManyShortestPathsAlgorithm<V, E> {

    /**
     * Computes shortest paths tree starting at {@code source} and stopping as
     * soon as all of the {@code targets} are reached. {@link DijkstraClosestFirstIterator}
     * is used.
     *
     * @param graph   a graph
     * @param source  source vertex
     * @param targets target vertices
     * @param <V>     the graph vertex type
     * @param <E>     the  graph edge type
     * @return shortest paths starting from {@code source} and reaching all {@code targets}
     */
    protected static <V, E> ShortestPathAlgorithm.SingleSourcePaths<V, E>
    getShortestPathsTree(Graph<V, E> graph, V source, Set<V> targets) {
        DijkstraClosestFirstIterator<V, E> iterator = new DijkstraClosestFirstIterator<>(graph, source);

        int reachedTargets = 0;
        while (iterator.hasNext() && reachedTargets < targets.size()) {
            if (targets.contains(iterator.next())) {
                ++reachedTargets;
            }
        }

        return iterator.getPaths();
    }
}
