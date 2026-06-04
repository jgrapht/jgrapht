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
 * The subset-DP formulation is due to Held and Karp,
 * <a href="https://www.jstor.org/stable/2098806">"A Dynamic Programming Approach to Sequencing
 * Problems", J. SIAM 10(1), 1962</a>, and independently to Bellman, "Dynamic Programming
 * Treatment of the Travelling Salesman Problem", J. ACM 9(1), 1962. The Hamiltonian-path
 * variant (where the DP does not need a closing edge back to the start) is a standard textbook
 * adaptation; see for example CLRS, "Introduction to Algorithms" (4th ed.), the
 * dynamic-programming chapter, and the MIT 6.s078 lecture notes on advanced algorithms
 * (lecture 17).
 *
 * <p>
 * Complexity is {@code O(n^2 * 2^n)} time and {@code O(n * 2^n)} space, where {@code n} is the
 * number of vertices. Because the running time and memory grow exponentially in {@code n},
 * this class refuses graphs with more than {@link #getMaxVertices()} vertices by throwing
 * {@link IllegalArgumentException}. The default ceiling is {@link #DEFAULT_MAX_VERTICES}; the
 * single-argument constructor lets callers opt in to a higher ceiling up to
 * {@link #HARD_MAX_VERTICES} when they have the memory headroom. Callers handling larger
 * graphs should use {@link BacktrackingHamiltonianPath} instead and accept the lack of
 * polynomial-bound guarantees.
 *
 * <p>
 * The algorithm is exact and deterministic. It supports directed and undirected graphs and
 * tolerates parallel edges and self-loops (self-loops are ignored because they cannot extend a
 * simple path). In multigraphs, parallel edges between the same vertex pair collapse to a
 * single DP transition; the returned path uses an arbitrary representative edge selected via
 * {@link Graph#getEdge} and is not weight-optimised across parallel edges.
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
     * Default ceiling on the number of vertices. With {@code n = 20} the DP table requires
     * roughly {@code 20 * 2^20 = ~20M} entries; the constant is chosen to keep memory and
     * runtime within practical limits.
     */
    public static final int DEFAULT_MAX_VERTICES = 20;

    /**
     * Hard upper bound on the number of vertices, fixed by the algorithm's use of a {@code int}
     * subset bitmask. Above {@code n = 30} the bitmask space overflows {@link Integer#MAX_VALUE};
     * in practice memory is the dominant constraint well before this ceiling.
     */
    public static final int HARD_MAX_VERTICES = 30;

    private static final byte UNVISITED = -1;
    private static final byte START_SENTINEL = -2;

    private final int maxVertices;
    private long statesExpanded;

    /**
     * Constructs a new instance with the default vertex ceiling
     * ({@link #DEFAULT_MAX_VERTICES}).
     */
    public HeldKarpHamiltonianPath()
    {
        this(DEFAULT_MAX_VERTICES);
    }

    /**
     * Constructs a new instance that accepts graphs with up to {@code maxVertices} vertices.
     *
     * @param maxVertices upper bound on the number of vertices the algorithm will accept; must
     *        be at least 1 and at most {@link #HARD_MAX_VERTICES}
     * @throws IllegalArgumentException if {@code maxVertices} is outside the allowed range
     */
    public HeldKarpHamiltonianPath(int maxVertices)
    {
        if (maxVertices < 1) {
            throw new IllegalArgumentException(
                "maxVertices must be at least 1, got " + maxVertices);
        }
        if (maxVertices > HARD_MAX_VERTICES) {
            throw new IllegalArgumentException(
                "maxVertices must be at most " + HARD_MAX_VERTICES
                    + " (the int-bitmask hard limit); got " + maxVertices);
        }
        this.maxVertices = maxVertices;
    }

    /**
     * Returns the maximum number of vertices this instance will accept.
     *
     * @return configured vertex ceiling
     */
    public int getMaxVertices()
    {
        return maxVertices;
    }

    /**
     * Returns the number of DP states (predecessor cells initialised) the algorithm filled
     * during the most recent call to {@link #getPath(Graph)}. Intended for diagnostics; the
     * exact counting semantics may change if the implementation changes.
     *
     * @return DP states filled during the last search
     */
    public long getStatesExpanded()
    {
        return statesExpanded;
    }

    @Override
    public HamiltonianPathSearchResult<V, E> getPath(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "graph must not be null");
        GraphTests.requireDirectedOrUndirected(graph);
        statesExpanded = 0L;
        requireNotEmpty(graph);

        final int n = graph.vertexSet().size();
        if (n == 1) {
            return HamiltonianPathSearchResult.found(singletonPath(graph), 0L);
        }
        if (n > maxVertices) {
            throw new IllegalArgumentException(
                "HeldKarpHamiltonianPath supports at most " + maxVertices
                    + " vertices; got " + n
                    + ". Use BacktrackingHamiltonianPath for larger graphs or construct this"
                    + " class with a higher maxVertices ceiling.");
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
            statesExpanded++;
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
                        statesExpanded++;
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
            return HamiltonianPathSearchResult.provenAbsent(statesExpanded);
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
        return HamiltonianPathSearchResult.found(vertexListToPath(vertices, graph), statesExpanded);
    }
}
