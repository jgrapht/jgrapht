/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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

import java.util.Map;
import java.util.Set;

/**
 * Abstract subroutine for the spur shortest-path computations performed by
 * {@link BoundedPrunedYenKShortestPath}.
 *
 * <p>
 * Implementations must return an exact non-negative-weight shortest path from {@code source} to
 * {@code sink} on the input {@code graph} after temporarily removing the supplied
 * {@code bannedVertices} and {@code bannedEdges}. Implementations may use the optional
 * {@code reverseDistancesToSink} hint (computed once on the original graph by the caller) for
 * admissible heuristic / pruning purposes.
 *
 * <p>
 * This is a pluggable abstraction so that different shortest-path back-ends can be benchmarked
 * inside the same Yen variant: classical Dijkstra and A* with reverse-distance heuristic. The
 * engine API is intentionally narrow so swapping engines does not change Yen-level behaviour.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public interface SpurShortestPathEngine<V, E>
{
    /**
     * Compute an exact shortest path from {@code source} to {@code sink} on {@code graph} ignoring
     * vertices in {@code bannedVertices} and edges in {@code bannedEdges}.
     *
     * @param graph original graph (engines should not mutate it)
     * @param source source vertex of the spur query
     * @param sink target vertex of the spur query
     * @param bannedVertices vertices to ignore (must not include {@code source} or {@code sink})
     * @param bannedEdges edges to ignore
     * @param reverseDistancesToSink optional hint, lower-bound distances from each vertex to
     *        {@code sink} computed on the original graph; may be {@code null} for engines that do
     *        not need it
     * @return the shortest path or {@code null} if no path exists in the masked graph
     */
    GraphPath<V, E> findPath(
        Graph<V, E> graph, V source, V sink, Set<V> bannedVertices, Set<E> bannedEdges,
        Map<V, Double> reverseDistancesToSink);

    /**
     * Number of vertices the engine has expanded (popped from its open set) since the last
     * {@link #resetCounters()} call. Best-effort metric used by benchmarks; engines that do not
     * track expansion (e.g. those delegating to a back-end without a public expansion counter)
     * may return {@code 0}.
     *
     * @return number of vertices popped from the engine's open set since the last reset, or
     *         {@code 0} when not tracked
     */
    long expandedVertices();

    /**
     * Number of shortest-path queries answered since the last {@link #resetCounters()} call.
     *
     * @return number of shortest-path queries answered since the last reset
     */
    long pathQueries();

    /**
     * Reset internal counters. Called by {@link BoundedPrunedYenKShortestPath} between independent
     * runs so that counters report per-run statistics.
     */
    void resetCounters();

    /**
     * Symbolic name of the engine, used in benchmark reports.
     *
     * @return a short human-readable identifier for this engine
     */
    String name();
}
