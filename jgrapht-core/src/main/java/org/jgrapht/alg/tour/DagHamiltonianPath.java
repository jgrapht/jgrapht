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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Polynomial-time Hamiltonian path algorithm for directed acyclic graphs.
 *
 * <p>
 * Hamiltonian path existence in a directed acyclic graph (DAG) reduces to deciding whether the
 * longest directed path covers all vertices. This implementation computes a topological order,
 * runs the standard longest-path-in-DAG dynamic program over that order, and reconstructs a
 * Hamiltonian path when the longest path's length equals the vertex count.
 *
 * <p>
 * The longest-path-in-DAG DP is a textbook reduction; see for example CLRS, "Introduction to
 * Algorithms" (4th ed.), section on single-source shortest paths in DAGs, which dualises to
 * longest-path by negating edge weights, and the MIT 6.s078 lecture notes
 * (<a href="https://people.csail.mit.edu/virgi/6.s078/lecture17.pdf">lecture 17 on dynamic
 * programming, MIT 6.s078, Spring 2022</a>) for the Hamiltonian-path framing.
 *
 * <p>
 * Total complexity is {@code O(|V| + |E|)} time and {@code O(|V|)} space.
 *
 * <p>
 * This class only accepts directed acyclic graphs. Passing a directed graph that contains a
 * cycle causes an {@link IllegalArgumentException} wrapping the
 * {@link org.jgrapht.traverse.NotDirectedAcyclicGraphException} thrown by
 * {@link TopologicalOrderIterator} when the topological pass discovers the cycle; passing an
 * undirected graph or {@code null} also fails with an {@link IllegalArgumentException}
 * (or {@link NullPointerException} respectively). To solve the Hamiltonian path problem on
 * cyclic directed graphs, use {@link BacktrackingHamiltonianPath} or
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
    public HamiltonianPathSearchResult<V, E> getPath(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "graph must not be null");
        GraphTests.requireDirected(graph);
        requireNotEmpty(graph);

        final int n = graph.vertexSet().size();
        if (n == 1) {
            return HamiltonianPathSearchResult.found(singletonPath(graph), 0L);
        }

        List<V> topo = new ArrayList<>(n);
        try {
            new TopologicalOrderIterator<>(graph).forEachRemaining(topo::add);
        } catch (NotDirectedAcyclicGraphException ex) {
            throw new IllegalArgumentException(
                "DagHamiltonianPath requires an acyclic graph; cycle detected. "
                    + "Use BacktrackingHamiltonianPath or HeldKarpHamiltonianPath instead.",
                ex);
        }
        Map<V, Integer> position = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            position.put(topo.get(i), i);
        }

        int[] longest = new int[n];
        int[] predecessor = new int[n];
        Arrays.fill(longest, 1);
        Arrays.fill(predecessor, -1);

        // No self-loop check is needed here: TopologicalOrderIterator throws
        // NotDirectedAcyclicGraphException on any self-loop (an edge v->v is a length-1 cycle),
        // so by this point every incoming edge has a distinct source.
        int bestEnd = 0;
        int bestLength = 1;
        for (int i = 0; i < n; i++) {
            V v = topo.get(i);
            for (E e : graph.incomingEdgesOf(v)) {
                int u = position.get(graph.getEdgeSource(e));
                if (longest[u] + 1 > longest[i]) {
                    longest[i] = longest[u] + 1;
                    predecessor[i] = u;
                }
            }
            if (longest[i] > bestLength) {
                bestLength = longest[i];
                bestEnd = i;
            }
        }

        if (bestLength != n) {
            return HamiltonianPathSearchResult.provenAbsent(n);
        }

        Deque<V> reversed = new ArrayDeque<>(n);
        int cur = bestEnd;
        while (cur != -1) {
            reversed.push(topo.get(cur));
            cur = predecessor[cur];
        }
        return HamiltonianPathSearchResult.found(
            vertexListToPath(new ArrayList<>(reversed), graph), n);
    }
}
