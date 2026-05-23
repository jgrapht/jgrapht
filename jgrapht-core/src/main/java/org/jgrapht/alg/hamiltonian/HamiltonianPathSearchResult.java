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
package org.jgrapht.alg.hamiltonian;

import org.jgrapht.*;

import java.util.*;

/**
 * Tri-state result of a bounded Hamiltonian path search.
 *
 * <p>
 * A bounded search can terminate in three distinct ways:
 * <ul>
 * <li>{@link Status#PATH_FOUND} — a Hamiltonian path was discovered. The path is available via
 * {@link #getPath()}.</li>
 * <li>{@link Status#PROVEN_ABSENT} — the search ran to completion (or was rejected by a sound
 * precheck) and proved that no Hamiltonian path exists.</li>
 * <li>{@link Status#ABORTED} — the search hit its configured limit before completing. Whether
 * a Hamiltonian path exists in the input graph is unknown.</li>
 * </ul>
 *
 * <p>
 * This object is immutable. Callers must check {@link #getStatus()} before interpreting a
 * {@link #getPath()} of {@link Optional#empty()}: an empty path may mean either
 * {@link Status#PROVEN_ABSENT} or {@link Status#ABORTED}, and the two have very different
 * semantic implications.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public final class HamiltonianPathSearchResult<V, E>
{
    /**
     * Termination state of a bounded Hamiltonian path search.
     */
    public enum Status
    {
        /** A Hamiltonian path was found. */
        PATH_FOUND,
        /** The exhaustive search terminated without finding a Hamiltonian path. */
        PROVEN_ABSENT,
        /** The search was stopped by a state limit before completing. */
        ABORTED
    }

    private final Status status;
    private final GraphPath<V, E> path;
    private final long statesExpanded;

    private HamiltonianPathSearchResult(Status status, GraphPath<V, E> path, long statesExpanded)
    {
        this.status = status;
        this.path = path;
        this.statesExpanded = statesExpanded;
    }

    private static void requireNonNegativeStates(long statesExpanded)
    {
        if (statesExpanded < 0L) {
            throw new IllegalArgumentException(
                "statesExpanded must be non-negative, got " + statesExpanded);
        }
    }

    /**
     * Creates a result for a successful search.
     *
     * @param path the Hamiltonian path that was found (must not be {@code null})
     * @param statesExpanded the number of DFS states the search explored
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a {@link Status#PATH_FOUND} result holding the given path
     */
    public static <V, E> HamiltonianPathSearchResult<V, E> found(
        GraphPath<V, E> path, long statesExpanded)
    {
        Objects.requireNonNull(path, "path must not be null");
        requireNonNegativeStates(statesExpanded);
        return new HamiltonianPathSearchResult<>(Status.PATH_FOUND, path, statesExpanded);
    }

    /**
     * Creates a result for an exhaustive search that proved no Hamiltonian path exists.
     *
     * @param statesExpanded the number of DFS states the search explored before concluding
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a {@link Status#PROVEN_ABSENT} result with no path
     */
    public static <V, E> HamiltonianPathSearchResult<V, E> provenAbsent(long statesExpanded)
    {
        requireNonNegativeStates(statesExpanded);
        return new HamiltonianPathSearchResult<>(Status.PROVEN_ABSENT, null, statesExpanded);
    }

    /**
     * Creates a result for a search that was stopped by a state limit before completing.
     *
     * @param statesExpanded the number of DFS states the search explored before aborting
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return an {@link Status#ABORTED} result with no path
     */
    public static <V, E> HamiltonianPathSearchResult<V, E> aborted(long statesExpanded)
    {
        requireNonNegativeStates(statesExpanded);
        return new HamiltonianPathSearchResult<>(Status.ABORTED, null, statesExpanded);
    }

    /**
     * Returns the termination status of this search.
     *
     * @return the search status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * Returns the Hamiltonian path if the search found one.
     *
     * @return the discovered path, or {@link Optional#empty()} for
     *         {@link Status#PROVEN_ABSENT} and {@link Status#ABORTED}
     */
    public Optional<GraphPath<V, E>> getPath()
    {
        return Optional.ofNullable(path);
    }

    /**
     * Returns the number of DFS states the search explored. See
     * {@link BacktrackingHamiltonianPath#getStatesExpanded()} for the counting semantics.
     *
     * @return DFS states explored during this search
     */
    public long getStatesExpanded()
    {
        return statesExpanded;
    }

    @Override
    public String toString()
    {
        return "HamiltonianPathSearchResult{status=" + status
            + ", statesExpanded=" + statesExpanded + "}";
    }
}
