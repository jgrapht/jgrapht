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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;

/**
 * An algorithm solving the <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Hamiltonian
 * path problem</a>.
 *
 * <p>
 * A Hamiltonian path is a path in an undirected or directed graph that visits every vertex
 * exactly once. Unlike a Hamiltonian cycle, a Hamiltonian path is not required to return to the
 * start vertex. The path's endpoints may be any pair of vertices (or a single vertex if the graph
 * has only one vertex).
 *
 * <p>
 * Deciding whether a Hamiltonian path exists in a general graph is NP-complete. Implementations
 * of this interface are therefore expected to use exact, exponential-time algorithms (possibly
 * with pruning) or specialised polynomial-time algorithms for restricted graph classes. Callers
 * should consult each implementation's JavaDoc for complexity and recommended graph sizes.
 *
 * <p>
 * This interface is the path-specific counterpart of
 * {@link HamiltonianCycleAlgorithm}. It is intentionally separate because the path problem does
 * not require a return edge from the last visited vertex to the first, and because the existing
 * cycle / tour APIs often assume tour or complete-graph semantics that are not appropriate for
 * Hamiltonian paths.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public interface HamiltonianPathAlgorithm<V, E>
{

    /**
     * Computes a Hamiltonian path in the given graph.
     *
     * <p>
     * Returns a {@link GraphPath} whose vertex list contains every vertex of the graph exactly
     * once and whose consecutive vertices are connected by an edge of the graph (respecting edge
     * direction in directed graphs). Returns {@code null} when the implementation has proven
     * that no Hamiltonian path exists. Implementations that perform bounded search must
     * distinguish proven absence from search abortion via their own API and must not return
     * {@code null} for an aborted search.
     *
     * @param graph the input graph
     * @return a Hamiltonian path, or {@code null} if no such path exists
     */
    GraphPath<V, E> getPath(Graph<V, E> graph);

}
