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
import org.jgrapht.util.*;

import java.util.*;

/**
 * Dynamic-programming algorithm for the
 * <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Hamiltonian path problem</a>.
 *
 * <p>
 * The implementation follows the classic Held-Karp subset DP, adapted to find a Hamiltonian
 * <em>path</em> rather than a Hamiltonian cycle. The state {@code dp[subset][v]} is
 * {@code true} when there exists a simple path that visits exactly the vertices in
 * {@code subset} and whose last vertex is {@code v}. The transition extends a path ending at
 * {@code v} by a new vertex {@code u} whenever the edge {@code (v, u)} exists and {@code u} is
 * not yet in the subset. A Hamiltonian path exists if and only if {@code dp[fullMask][v]} is
 * {@code true} for some {@code v}.
 *
 * <p>
 * Complexity is {@code O(n^2 * 2^n)} time and {@code O(n * 2^n)} space, where {@code n} is the
 * number of vertices. Because the running time and memory grow exponentially in {@code n}, this
 * class refuses graphs with more than {@value #MAX_VERTICES} vertices by throwing
 * {@link IllegalArgumentException}. Callers handling larger graphs should use
 * {@link BacktrackingHamiltonianPath} instead and accept the lack of polynomial-bound
 * guarantees.
 *
 * <p>
 * The algorithm is exact and deterministic. It supports directed and undirected graphs and
 * tolerates parallel edges and self-loops (self-loops are ignored because they cannot extend a
 * simple path). In multigraphs, parallel edges between the same vertex pair collapse to a
 * single DP transition; the returned path uses an arbitrary representative edge selected via
 * {@link Graph#getEdge} and is not weight-optimised across parallel edges. It returns
 * {@code null} when no Hamiltonian path exists.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public class HeldKarpHamiltonianPath<V, E>
    extends HamiltonianPathAlgorithmBase<V, E>
{

    /**
     * Maximum supported number of vertices. With {@code n = 20} the DP table requires
     * roughly {@code 20 * 2^20 = ~20M} entries; the constant is chosen to keep memory and
     * runtime within practical limits.
     */
    public static final int MAX_VERTICES = 20;

    private static final byte UNVISITED = -1;
    private static final byte START_SENTINEL = -2;

    /**
     * Constructs a new instance.
     */
    public HeldKarpHamiltonianPath()
    {
    }

    @Override
    public GraphPath<V, E> getPath(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "graph must not be null");
        GraphTests.requireDirectedOrUndirected(graph);
        requireNotEmpty(graph);

        final int n = graph.vertexSet().size();
        if (n == 1) {
            return singletonPath(graph);
        }
        if (n > MAX_VERTICES) {
            throw new IllegalArgumentException(
                "HeldKarpHamiltonianPath supports at most " + MAX_VERTICES
                    + " vertices; got " + n + ". Use BacktrackingHamiltonianPath for larger graphs.");
        }

        VertexToIntegerMapping<V> mapping = Graphs.getVertexToIntegerMapping(graph);
        List<V> indexList = mapping.getIndexList();
        Map<V, Integer> vertexMap = mapping.getVertexMap();
        final boolean directed = graph.getType().isDirected();
        int[][] adjacency = buildAdjacency(graph, indexList, vertexMap, directed);

        final int fullMask = (1 << n) - 1;
        // pred[mask][v] stores the predecessor of v on a path realising dp[mask][v].
        // UNVISITED (-1) means dp[mask][v] is false; START_SENTINEL (-2) marks the start vertex.
        byte[][] pred = new byte[1 << n][n];
        for (byte[] row : pred) {
            Arrays.fill(row, UNVISITED);
        }
        for (int v = 0; v < n; v++) {
            pred[1 << v][v] = START_SENTINEL;
        }

        for (int mask = 1; mask < (1 << n); mask++) {
            for (int v = 0; v < n; v++) {
                if (((mask >> v) & 1) == 0 || pred[mask][v] == UNVISITED) {
                    continue;
                }
                for (int u : adjacency[v]) {
                    if (((mask >> u) & 1) != 0) {
                        continue;
                    }
                    int newMask = mask | (1 << u);
                    if (pred[newMask][u] == UNVISITED) {
                        pred[newMask][u] = (byte) v;
                    }
                }
            }
        }

        int endVertex = -1;
        for (int v = 0; v < n; v++) {
            if (pred[fullMask][v] != UNVISITED) {
                endVertex = v;
                break;
            }
        }
        if (endVertex == -1) {
            return null;
        }

        List<Integer> reversed = new ArrayList<>(n);
        int mask = fullMask;
        int cur = endVertex;
        while (cur != -1) {
            reversed.add(cur);
            byte p = pred[mask][cur];
            mask ^= (1 << cur);
            cur = (p == START_SENTINEL) ? -1 : (p & 0xFF);
        }
        Collections.reverse(reversed);

        List<V> vertices = new ArrayList<>(n);
        for (int idx : reversed) {
            vertices.add(indexList.get(idx));
        }
        return vertexListToPath(vertices, graph);
    }
}
