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

import java.util.*;

/**
 * Tri-state result of a {@link HamiltonianPathAlgorithm} search.
 *
 * <p>
 * A Hamiltonian path search can terminate in three distinct ways:
 * <ul>
 * <li>{@link Status#PATH_FOUND} — a Hamiltonian path was discovered. The path is available via
 * {@link #getPath()}.</li>
 * <li>{@link Status#PROVEN_ABSENT} — the search ran to completion (or was rejected by a sound
 * precheck) and proved that no Hamiltonian path exists.</li>
 * <li>{@link Status#ABORTED} — the search hit a configured limit before completing. Whether a
 * Hamiltonian path exists in the input graph is unknown.</li>
 * </ul>
 *
 * <p>
 * Callers must consult {@link #getStatus()} before interpreting an empty {@link #getPath()}:
 * an empty path may mean either {@link Status#PROVEN_ABSENT} or {@link Status#ABORTED}, and
 * the two have very different semantic implications.
 *
 * <p>
 * Instances are obtained either from a {@link HamiltonianPathAlgorithm} implementation or via
 * the {@link #found(GraphPath, long)}, {@link #provenAbsent(long)}, and
 * {@link #aborted(long)} static factory methods on this interface, which return a default
 * immutable implementation.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public interface HamiltonianPathSearchResult<V, E>
{
    /**
     * Termination state of a Hamiltonian path search.
     */
    enum Status
    {
        /** A Hamiltonian path was found. */
        PATH_FOUND,
        /** The exhaustive search terminated without finding a Hamiltonian path. */
        PROVEN_ABSENT,
        /** The search was stopped by a state limit before completing. */
        ABORTED
    }

    /**
     * Returns the termination status of this search.
     *
     * @return the search status
     */
    Status getStatus();

    /**
     * Returns the Hamiltonian path if the search found one.
     *
     * @return the discovered path, or {@link Optional#empty()} for
     *         {@link Status#PROVEN_ABSENT} and {@link Status#ABORTED}
     */
    Optional<GraphPath<V, E>> getPath();

    /**
     * Returns the number of DFS / DP states the search explored. The exact counting semantics
     * are implementation-defined; consult the JavaDoc of the producing algorithm for details.
     *
     * @return states explored during this search
     */
    long getStatesExpanded();

    /**
     * Creates a result for a successful search.
     *
     * @param path the Hamiltonian path that was found (must not be {@code null})
     * @param statesExpanded the number of states the search explored
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a {@link Status#PATH_FOUND} result holding the given path
     */
    static <V, E> HamiltonianPathSearchResult<V, E> found(
        GraphPath<V, E> path, long statesExpanded)
    {
        Objects.requireNonNull(path, "path must not be null");
        requireNonNegativeStates(statesExpanded);
        return new DefaultHamiltonianPathSearchResult<>(Status.PATH_FOUND, path, statesExpanded);
    }

    /**
     * Creates a result for an exhaustive search that proved no Hamiltonian path exists.
     *
     * @param statesExpanded the number of states the search explored before concluding
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a {@link Status#PROVEN_ABSENT} result with no path
     */
    static <V, E> HamiltonianPathSearchResult<V, E> provenAbsent(long statesExpanded)
    {
        requireNonNegativeStates(statesExpanded);
        return new DefaultHamiltonianPathSearchResult<>(Status.PROVEN_ABSENT, null, statesExpanded);
    }

    /**
     * Creates a result for a search that was stopped by a state limit before completing.
     *
     * @param statesExpanded the number of states the search explored before aborting
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return an {@link Status#ABORTED} result with no path
     */
    static <V, E> HamiltonianPathSearchResult<V, E> aborted(long statesExpanded)
    {
        requireNonNegativeStates(statesExpanded);
        return new DefaultHamiltonianPathSearchResult<>(Status.ABORTED, null, statesExpanded);
    }

    private static void requireNonNegativeStates(long statesExpanded)
    {
        if (statesExpanded < 0L) {
            throw new IllegalArgumentException(
                "statesExpanded must be non-negative, got " + statesExpanded);
        }
    }
}
