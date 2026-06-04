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
     * Computes a Hamiltonian path in the given graph and returns a tri-state
     * {@link HamiltonianPathSearchResult}.
     *
     * <p>
     * The return value distinguishes:
     * <ul>
     * <li>{@link HamiltonianPathSearchResult.Status#PATH_FOUND} — the algorithm produced a
     * Hamiltonian path; it is accessible via
     * {@link HamiltonianPathSearchResult#getPath()}.</li>
     * <li>{@link HamiltonianPathSearchResult.Status#PROVEN_ABSENT} — the algorithm ran to
     * completion (or was rejected by a sound precheck) and proved that no Hamiltonian path
     * exists.</li>
     * <li>{@link HamiltonianPathSearchResult.Status#ABORTED} — the algorithm performed a bounded
     * search that hit its limit before completing. Whether a Hamiltonian path exists in the
     * graph is unknown.</li>
     * </ul>
     *
     * <p>
     * Unbounded implementations only return {@code PATH_FOUND} or {@code PROVEN_ABSENT}.
     * Implementations that accept an execution budget (e.g. a maximum state count) may also
     * return {@code ABORTED}; such bounded variants must never report {@code PROVEN_ABSENT}
     * for a search that was stopped early.
     *
     * @param graph the input graph
     * @return a {@link HamiltonianPathSearchResult} describing the search outcome
     */
    HamiltonianPathSearchResult<V, E> getPath(Graph<V, E> graph);

}
