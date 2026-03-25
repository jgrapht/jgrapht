/*
 * (C) Copyright 2015-2025, by Vera-Licona Research Group and Contributors.
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
import org.jgrapht.GraphType;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.math.BigInteger;
import java.util.*;

/**
 * Counts the number of simple directed paths in a Directed Acyclic Graph (DAG)
 * between a given source and target using dynamic programming in topological order.
 *
 * <p>For large graphs the number of paths can be extremely large; values are returned as
 * {@link java.math.BigInteger}.</p>
 *
 * <p>Usage constraints: the provided graph must be directed and acyclic. An
 * {@link IllegalArgumentException} is thrown otherwise.</p>
 */
public final class DagAllPathsCounter {

    private DagAllPathsCounter() {
        // utility
    }

    /**
     * Count the number of simple directed paths from {@code source} to {@code target}
     * in the given directed acyclic graph.
     *
     * <p>Edge case semantics:</p>
     * <ul>
     *   <li>If {@code source} or {@code target} is not contained in the graph, throws {@link IllegalArgumentException}.</li>
     *   <li>If {@code source.equals(target)}, the empty path (length 0) is counted as one path and paths that leave
     *   {@code source} and return to it cannot exist in a DAG, thus the result is 1.</li>
     *   <li>If there is no path from {@code source} to {@code target}, returns {@link BigInteger#ZERO}.</li>
     * </ul>
     *
     * @param graph  directed acyclic graph
     * @param source source vertex
     * @param target target vertex
     * @param <V> vertex type
     * @param <E> edge type
     * @return number of simple directed paths from {@code source} to {@code target}
     * @throws IllegalArgumentException if the graph is not directed, not acyclic, or vertices are missing
     */
    public static <V, E> BigInteger countAllPaths(Graph<V, E> graph, V source, V target) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        GraphType type = graph.getType();
        if (!type.isDirected()) {
            throw new IllegalArgumentException("countAllPaths requires a directed graph");
        }

        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            throw new IllegalArgumentException("source and target must be vertices of the graph");
        }

        // Validate acyclicity
        CycleDetector<V, E> cycleDetector = new CycleDetector<>(graph);
        if (cycleDetector.detectCycles()) {
            throw new IllegalArgumentException("Graph must be acyclic (DAG)");
        }

        if (source.equals(target)) {
            // Only the empty path exists in a DAG
            return BigInteger.ONE;
        }

        // Compute topological order
        List<V> topo = new ArrayList<>(graph.vertexSet().size());
        TopologicalOrderIterator<V, E> it = new TopologicalOrderIterator<>(graph);
        while (it.hasNext()) {
            topo.add(it.next());
        }

        // Map vertex to index in topo order for quick reachability pruning
        Map<V, Integer> index = new HashMap<>(topo.size() * 2);
        for (int i = 0; i < topo.size(); i++) {
            index.put(topo.get(i), i);
        }

        Integer si = index.get(source);
        Integer ti = index.get(target);
        if (si == null || ti == null) {
            // Should not happen since we validated containsVertex above
            return BigInteger.ZERO;
        }
        if (si > ti) {
            // In a DAG, no path can go backwards in topological order
            return BigInteger.ZERO;
        }

        // DP: ways[v] = number of paths from source to v
        Map<V, BigInteger> ways = new HashMap<>(topo.size() * 2);
        for (V v : topo) {
            ways.put(v, BigInteger.ZERO);
        }
        ways.put(source, BigInteger.ONE);

        for (int i = si; i < topo.size(); i++) {
            V u = topo.get(i);
            BigInteger wu = ways.get(u);
            if (wu.signum() == 0) continue; // unreachable
            // Early stop if we have passed the target in topological order
            if (i > ti) break;
            for (E e : graph.outgoingEdgesOf(u)) {
                V v = graph.getEdgeTarget(e);
                // For directed graphs, some representations may return u as target; handle via check
                if (u.equals(v)) {
                    v = graph.getEdgeSource(e);
                }
                // Since the graph is a DAG and topologically sorted, v should appear after u; no extra checks needed
                ways.put(v, ways.get(v).add(wu));
            }
        }

        return ways.getOrDefault(target, BigInteger.ZERO);
    }
}
