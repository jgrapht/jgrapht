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
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Spur shortest-path engine that delegates to JGraphT's {@link AStarShortestPath}, using the
 * {@code reverseDistancesToSink} hint as an admissible heuristic. Bans are applied by wrapping the
 * input graph in a {@link MaskSubgraph} so we do not duplicate or modify the underlying A*
 * implementation.
 *
 * <p>
 * Correctness: distances on the original graph are a valid lower bound for distances on any
 * subgraph obtained by removing vertices/edges (Dijkstra never gets shorter when the graph
 * shrinks). So {@code h(v) = reverseDistancesToSink[v]} is admissible for the spur subproblem on
 * the masked graph. If the heuristic map is {@code null} or missing for some vertex, the engine
 * falls back to {@code 0.0} for that vertex (still admissible, equivalent to running A* with a
 * trivial heuristic, i.e. Dijkstra).
 *
 * <p>
 * Edge weights must be non-negative.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class AStarSpurEngine<V, E>
    implements SpurShortestPathEngine<V, E>
{
    private long expandedVertices;
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

        AStarAdmissibleHeuristic<V> heuristic = (sourceVertex, targetVertex) -> {
            if (reverseDistancesToSink == null) {
                return 0.0;
            }
            Double d = reverseDistancesToSink.get(sourceVertex);
            // Missing entry => unreachable to sink on the original graph; 0.0 is still admissible.
            return d == null || Double.isInfinite(d) ? 0.0 : d;
        };

        CountingAStar<V, E> astar = new CountingAStar<>(masked, heuristic);
        GraphPath<V, E> path = astar.getPath(source, sink);
        expandedVertices += astar.expanded();
        return path;
    }

    @Override
    public long expandedVertices()
    {
        return expandedVertices;
    }

    @Override
    public long pathQueries()
    {
        return pathQueries;
    }

    @Override
    public void resetCounters()
    {
        expandedVertices = 0;
        pathQueries = 0;
    }

    @Override
    public String name()
    {
        return "AStar";
    }

    /**
     * Tiny subclass that exposes the protected {@code numberOfExpandedNodes} counter so the
     * engine can report per-call expansion counts to the benchmark. Behaviour is otherwise
     * identical to {@link AStarShortestPath}.
     */
    private static final class CountingAStar<V, E>
        extends AStarShortestPath<V, E>
    {
        CountingAStar(Graph<V, E> graph, AStarAdmissibleHeuristic<V> heuristic)
        {
            super(graph, heuristic);
        }

        int expanded()
        {
            return numberOfExpandedNodes;
        }
    }
}
