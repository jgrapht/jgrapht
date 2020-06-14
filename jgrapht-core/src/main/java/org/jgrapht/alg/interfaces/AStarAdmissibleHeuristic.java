/*
 * (C) Copyright 2015-2020, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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

/**
 * Interface for an admissible heuristic used in A* search.
 *
 * @param <V> vertex type
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 */
public interface AStarAdmissibleHeuristic<V>
{
    /**
     * An admissible "heuristic estimate" of the distance from $x$, the sourceVertex, to the goal
     * (usually denoted $h(x)$). This is the good guess function which must never overestimate the
     * distance.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @return an estimate of the distance from the source to the target vertex
     */
    double getCostEstimate(V sourceVertex, V targetVertex);

    /**
     * Returns true if the heuristic is a <i>consistent</i> or <i>monotone</i> heuristic wrt the
     * provided {@code graph}. A heuristic is monotonic if its estimate is always less than or equal
     * to the estimated distance from any neighboring vertex to the goal, plus the step cost of
     * reaching that neighbor. For details, refer to <a href=
     * "https://en.wikipedia.org/wiki/Consistent_heuristic">https://en.wikipedia.org/wiki/Consistent_heuristic</a>.
     * In short, a heuristic is consistent iff <code>h(u)&le; d(u,v)+h(v)</code>, for every edge
     * $(u,v)$, where $d(u,v)$ is the weight of edge $(u,v)$ and $h(u)$ is the estimated cost to
     * reach the target node from vertex u. Most natural admissible heuristics, such as Manhattan or
     * Euclidean distance, are consistent heuristics.
     *
     * @param graph graph to test heuristic on
     * @param <E> graph edges type
     * @return true iff the heuristic is consistent wrt the {@code graph}, false otherwise
     */
    default <E> boolean isConsistent(Graph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null!");
        }
        for (V targetVertex : graph.vertexSet()) {
            for (E e : graph.edgeSet()) {
                double weight = graph.getEdgeWeight(e);
                V edgeSource = graph.getEdgeSource(e);
                V edgeTarget = graph.getEdgeTarget(e);
                double h_x = getCostEstimate(edgeSource, targetVertex);
                double h_y = getCostEstimate(edgeTarget, targetVertex);
                if (h_x > weight + h_y)
                    return false;
            }
        }
        return true;
    }
}
