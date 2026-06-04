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
import org.jgrapht.alg.interfaces.HamiltonianPathSearchResult.Status;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.jgrapht.alg.tour.HamiltonianPathValidator.assertHamiltonianPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BacktrackingHamiltonianPath#searchWithStateLimit(Graph, long)} and the
 * {@link HamiltonianPathSearchResult} tri-state return.
 */
public class BacktrackingHamiltonianPathBoundedTest
{

    private <V, E> HamiltonianPathSearchResult<V, E> search(Graph<V, E> graph, long maxStates)
    {
        return new BacktrackingHamiltonianPath<V, E>().searchWithStateLimit(graph, maxStates);
    }

    @Test
    public void zeroMaxStatesThrows()
    {
        Graph<Integer, DefaultEdge> graph = path(3);
        assertThrows(IllegalArgumentException.class, () -> search(graph, 0L));
    }

    @Test
    public void negativeMaxStatesThrows()
    {
        Graph<Integer, DefaultEdge> graph = path(3);
        assertThrows(IllegalArgumentException.class, () -> search(graph, -1L));
    }

    @Test
    public void nullGraphThrows()
    {
        assertThrows(
            NullPointerException.class,
            () -> new BacktrackingHamiltonianPath<Integer, DefaultEdge>()
                .searchWithStateLimit(null, 100L));
    }

    @Test
    public void emptyGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        assertThrows(IllegalArgumentException.class, () -> search(graph, 100L));
    }

    @Test
    public void easyPathReturnsFoundUnderHighLimit()
    {
        Graph<Integer, DefaultEdge> graph = path(6);
        HamiltonianPathSearchResult<Integer, DefaultEdge> result = search(graph, 1_000_000L);
        assertHamiltonianPath(graph, result);
        assertTrue(result.getPath().isPresent());
        assertTrue(result.getStatesExpanded() > 0);
    }

    @Test
    public void disconnectedGraphIsProvenAbsentBeforeAnyState()
    {
        // Disconnected: the cheap precheck rejects without entering the DFS, so the state
        // counter stays at 0 and the result is PROVEN_ABSENT, not ABORTED.
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        HamiltonianPathSearchResult<Integer, DefaultEdge> result = search(graph, 10L);
        assertEquals(Status.PROVEN_ABSENT, result.getStatus());
        assertTrue(result.getPath().isEmpty());
        assertEquals(0L, result.getStatesExpanded());
    }

    @Test
    public void smallLimitOnHardGraphReturnsAborted()
    {
        // A 12-vertex complete graph has many Hamiltonian paths, but with a state budget of 2
        // we cannot finish a full DFS branch. The result must be ABORTED, not PROVEN_ABSENT,
        // because no exhaustive proof was performed.
        Graph<Integer, DefaultEdge> graph = complete(12);

        HamiltonianPathSearchResult<Integer, DefaultEdge> result = search(graph, 2L);
        assertEquals(Status.ABORTED, result.getStatus());
        assertTrue(result.getPath().isEmpty());
        // The check-before-increment guard in extend() keeps the counter at most at maxStates;
        // here it should stop exactly at the configured budget.
        assertEquals(2L, result.getStatesExpanded());
    }

    @Test
    public void statesExpandedNeverExceedsMaxStates()
    {
        // Property check on the off-by-one fix: for a range of small budgets, the reported
        // state count must never exceed maxStates.
        Graph<Integer, DefaultEdge> graph = complete(10);
        for (long budget = 1L; budget <= 20L; budget++) {
            HamiltonianPathSearchResult<Integer, DefaultEdge> result = search(graph, budget);
            long limit = budget;
            assertTrue(
                result.getStatesExpanded() <= limit,
                () -> "states " + result.getStatesExpanded() + " > maxStates " + limit);
        }
    }

    @Test
    public void exactlyEnoughBudgetFindsPath()
    {
        // A path graph yields one state per depth level under MRV ordering; budget = n is the
        // tight upper bound for backtracking to succeed.
        Graph<Integer, DefaultEdge> graph = path(8);

        HamiltonianPathSearchResult<Integer, DefaultEdge> result = search(graph, 8L);
        assertHamiltonianPath(graph, result);
    }

    @Test
    public void boundedSearchDoesNotAffectSubsequentUnboundedCall()
    {
        // Reusing the same algorithm instance for an unbounded getPath after a bounded call
        // must not carry over the state limit.
        BacktrackingHamiltonianPath<Integer, DefaultEdge> algo = new BacktrackingHamiltonianPath<>();
        Graph<Integer, DefaultEdge> hard = complete(12);

        HamiltonianPathSearchResult<Integer, DefaultEdge> bounded =
            algo.searchWithStateLimit(hard, 2L);
        assertEquals(Status.ABORTED, bounded.getStatus());

        HamiltonianPathSearchResult<Integer, DefaultEdge> unbounded = algo.getPath(hard);
        assertNotNull(unbounded);
        assertHamiltonianPath(hard, unbounded);
    }

    @Test
    public void factoriesValidateInput()
    {
        assertThrows(
            NullPointerException.class,
            () -> HamiltonianPathSearchResult.found(null, 1L));
        HamiltonianPathSearchResult<Integer, DefaultEdge> proven =
            HamiltonianPathSearchResult.provenAbsent(7L);
        assertEquals(Status.PROVEN_ABSENT, proven.getStatus());
        assertTrue(proven.getPath().isEmpty());
        assertEquals(7L, proven.getStatesExpanded());

        HamiltonianPathSearchResult<Integer, DefaultEdge> aborted =
            HamiltonianPathSearchResult.aborted(42L);
        assertEquals(Status.ABORTED, aborted.getStatus());
        assertTrue(aborted.getPath().isEmpty());
        assertEquals(42L, aborted.getStatesExpanded());
    }

    @Test
    public void factoriesRejectNegativeStateCounts()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> HamiltonianPathSearchResult.provenAbsent(-1L));
        assertThrows(
            IllegalArgumentException.class,
            () -> HamiltonianPathSearchResult.aborted(-1L));
    }

    private static Graph<Integer, DefaultEdge> path(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i < n - 1; i++) {
            g.addEdge(i, i + 1);
        }
        return g;
    }

    private static Graph<Integer, DefaultEdge> complete(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }
}
