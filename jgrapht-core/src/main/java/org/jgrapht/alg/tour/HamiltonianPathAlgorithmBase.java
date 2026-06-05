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
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Base class for {@link HamiltonianPathAlgorithm} implementations.
 *
 * <p>
 * Shares the small amount of plumbing common to the Hamiltonian path solvers in this package:
 * input validation, the singleton-vertex special case, an open-vertex-list to {@link GraphPath}
 * converter, and an adjacency-list builder that collapses parallel edges and skips self-loops.
 *
 * <p>
 * This class is structurally similar to {@link HamiltonianCycleAlgorithmBase} but adapted to
 * the open-path semantics (start and end vertex may differ; no closing edge is appended).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public abstract class HamiltonianPathAlgorithmBase<V, E>
    implements HamiltonianPathAlgorithm<V, E>
{
    /**
     * Constructs a new instance.
     */
    protected HamiltonianPathAlgorithmBase()
    {
    }

    /**
     * Throws {@link IllegalArgumentException} if {@code graph} has no vertices.
     *
     * @param graph the input graph
     * @throws IllegalArgumentException if {@code graph} has no vertices
     */
    protected void requireNotEmpty(Graph<V, E> graph)
    {
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }
    }

    /**
     * Returns the trivial Hamiltonian path of a graph with exactly one vertex: a {@link GraphWalk}
     * whose vertex list contains only that vertex, no edges, and weight {@code 0}.
     *
     * @param graph the input graph, which must contain exactly one vertex
     * @return the single-vertex path
     */
    protected GraphPath<V, E> singletonPath(Graph<V, E> graph)
    {
        assert graph.vertexSet().size() == 1;
        V only = graph.vertexSet().iterator().next();
        return new GraphWalk<>(
            graph, only, only, Collections.singletonList(only), Collections.emptyList(), 0d);
    }

    /**
     * Converts a complete vertex sequence into a {@link GraphPath}. For every consecutive vertex
     * pair {@code (u, v)} the edge returned by {@link Graph#getEdge(Object, Object) graph.getEdge(u, v)}
     * is appended; the path weight is the sum of the chosen edges' {@link Graph#getEdgeWeight
     * graph.getEdgeWeight} values. In multigraphs the chosen edge is an arbitrary representative;
     * this method does not attempt to minimise weight across parallel edges.
     *
     * @param vertices the vertex sequence (must contain at least one vertex)
     * @param graph the input graph
     * @return a {@link GraphPath} corresponding to {@code vertices}
     */
    protected GraphPath<V, E> vertexListToPath(List<V> vertices, Graph<V, E> graph)
    {
        final int n = vertices.size();
        if (n == 1) {
            V only = vertices.get(0);
            return new GraphWalk<>(
                graph, only, only, Collections.singletonList(only), Collections.emptyList(), 0d);
        }
        List<E> edges = new ArrayList<>(n - 1);
        double weight = 0d;
        for (int i = 1; i < n; i++) {
            V u = vertices.get(i - 1);
            V v = vertices.get(i);
            E edge = graph.getEdge(u, v);
            edges.add(edge);
            weight += graph.getEdgeWeight(edge);
        }
        return new GraphWalk<>(
            graph, vertices.get(0), vertices.get(n - 1), vertices, edges, weight);
    }

    /**
     * Builds an integer-indexed adjacency list over the supplied vertex ordering. Self-loops are
     * skipped because they cannot participate in a simple path; parallel edges between the same
     * vertex pair collapse to a single neighbour entry, which is sufficient for the
     * existence-only search performed by the Hamiltonian path solvers.
     *
     * <p>
     * For directed graphs only outgoing edges are recorded. For undirected graphs every
     * incident edge contributes a neighbour entry.
     *
     * @param graph the input graph
     * @param indexToVertex vertex ordering used as the row index of the returned adjacency
     * @param vertexToIndex inverse of {@code indexToVertex} for fast neighbour lookup
     * @param directed {@code true} if {@code graph} is directed
     * @return adjacency list as a jagged {@code int[][]}; row {@code i} lists the neighbours of
     *         {@code indexToVertex.get(i)} as indices into {@code indexToVertex}
     */
    protected int[][] buildAdjacency(
        Graph<V, E> graph, List<V> indexToVertex, Map<V, Integer> vertexToIndex, boolean directed)
    {
        final int n = indexToVertex.size();
        int[][] adjacency = new int[n][];
        for (int u = 0; u < n; u++) {
            V uVertex = indexToVertex.get(u);
            Set<Integer> neighbours = new LinkedHashSet<>();
            Iterable<E> edges = directed ? graph.outgoingEdgesOf(uVertex) : graph.edgesOf(uVertex);
            for (E e : edges) {
                V other = directed
                    ? graph.getEdgeTarget(e) : Graphs.getOppositeVertex(graph, e, uVertex);
                if (other.equals(uVertex)) {
                    continue;
                }
                neighbours.add(vertexToIndex.get(other));
            }
            int[] row = new int[neighbours.size()];
            int idx = 0;
            for (int v : neighbours) {
                row[idx++] = v;
            }
            adjacency[u] = row;
        }
        return adjacency;
    }
}
