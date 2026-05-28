/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Polynomial-time Hamiltonian path algorithm for directed acyclic graphs.
 *
 * <p>
 * Hamiltonian path existence in a directed acyclic graph (DAG) can be decided in linear time by
 * checking whether the longest directed path covers all vertices. This implementation computes
 * a topological order, runs the standard longest-path-in-DAG dynamic program, and reconstructs
 * a Hamiltonian path when the longest path's length equals the vertex count.
 *
 * <p>
 * Total complexity is {@code O(|V| + |E|)} time and {@code O(|V|)} space.
 *
 * <p>
 * This class only accepts directed acyclic graphs. Passing a directed graph that contains a
 * cycle, an undirected graph, or {@code null} causes an {@link IllegalArgumentException}. To
 * solve Hamiltonian path on cyclic directed graphs, use {@link BacktrackingHamiltonianPath} or
 * {@link HeldKarpHamiltonianPath}.
 *
 * <p>
 * In multigraphs, parallel edges between the same vertex pair do not change the result. The
 * returned path uses an arbitrary representative edge selected via {@link Graph#getEdge} and
 * is not weight-optimised across parallel edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public class DagHamiltonianPath<V, E>
    extends HamiltonianPathAlgorithmBase<V, E>
{

    /**
     * Constructs a new instance.
     */
    public DagHamiltonianPath()
    {
    }

    @Override
    public GraphPath<V, E> getPath(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "graph must not be null");
        GraphTests.requireDirected(graph);
        requireNotEmpty(graph);

        final int n = graph.vertexSet().size();
        if (n == 1) {
            return singletonPath(graph);
        }

        if (new CycleDetector<>(graph).detectCycles()) {
            throw new IllegalArgumentException(
                "DagHamiltonianPath requires an acyclic graph; cycle detected. "
                    + "Use BacktrackingHamiltonianPath or HeldKarpHamiltonianPath instead.");
        }

        List<V> topo = new ArrayList<>(n);
        new TopologicalOrderIterator<>(graph).forEachRemaining(topo::add);
        Map<V, Integer> position = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            position.put(topo.get(i), i);
        }

        int[] longest = new int[n];
        int[] predecessor = new int[n];
        Arrays.fill(longest, 1);
        Arrays.fill(predecessor, -1);

        int bestEnd = 0;
        int bestLength = 1;
        for (int i = 0; i < n; i++) {
            V v = topo.get(i);
            for (E e : graph.incomingEdgesOf(v)) {
                V u = graph.getEdgeSource(e);
                if (u.equals(v)) {
                    continue; // self-loop; cannot occur in a DAG but guarded for safety
                }
                int candidate = longest[position.get(u)] + 1;
                if (candidate > longest[i]) {
                    longest[i] = candidate;
                    predecessor[i] = position.get(u);
                }
            }
            if (longest[i] > bestLength) {
                bestLength = longest[i];
                bestEnd = i;
            }
        }

        if (bestLength != n) {
            return null;
        }

        Deque<V> reversed = new ArrayDeque<>(n);
        int cur = bestEnd;
        while (cur != -1) {
            reversed.push(topo.get(cur));
            cur = predecessor[cur];
        }
        return vertexListToPath(new ArrayList<>(reversed), graph);
    }
}
