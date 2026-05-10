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
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Spur shortest-path engine that delegates to JGraphT's {@link DijkstraShortestPath}. Bans are
 * applied by wrapping the input graph in a {@link MaskSubgraph} so we do not duplicate or modify
 * the underlying Dijkstra implementation. The {@code reverseDistancesToSink} hint is unused
 * here; it is consumed by the bounded-pruned Yen driver itself, not by this engine.
 *
 * <p>
 * Edge weights must be non-negative (a Dijkstra precondition).
 *
 * <p>
 * The {@link #expandedVertices()} counter is reported as {@code 0} because
 * {@code DijkstraShortestPath} does not expose a per-call expansion count. The metric is
 * documented as best-effort on {@link SpurShortestPathEngine}.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class DijkstraSpurEngine<V, E>
    implements SpurShortestPathEngine<V, E>
{
    private long pathQueries;

    @Override
    public GraphPath<V, E> findPath(
        Graph<V, E> graph, V source, V sink, Set<V> bannedVertices, Set<E> bannedEdges,
        Map<V, Double> reverseDistancesToSink)
    {
        pathQueries++;
        if (source.equals(sink)) {
            // Trivial zero-length walk; matches YenKShortestPath behaviour for source==sink.
            return new GraphWalk<>(
                graph, source, sink, Collections.singletonList(source), Collections.emptyList(),
                0.0);
        }
        Set<V> banV = bannedVertices == null ? Collections.emptySet() : bannedVertices;
        Set<E> banE = bannedEdges == null ? Collections.emptySet() : bannedEdges;
        Graph<V, E> masked = (banV.isEmpty() && banE.isEmpty())
            ? graph
            : new MaskSubgraph<>(graph, banV::contains, banE::contains);
        return new DijkstraShortestPath<>(masked).getPath(source, sink);
    }

    @Override
    public long expandedVertices()
    {
        return 0L;
    }

    @Override
    public long pathQueries()
    {
        return pathQueries;
    }

    @Override
    public void resetCounters()
    {
        pathQueries = 0;
    }

    @Override
    public String name()
    {
        return "Dijkstra";
    }
}
