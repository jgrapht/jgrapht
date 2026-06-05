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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Shared assertion helper for Hamiltonian path tests.
 *
 * <p>
 * A returned {@link GraphPath} is considered a valid Hamiltonian path on a given graph if:
 * <ul>
 * <li>it is non-null,</li>
 * <li>its vertex list visits every vertex of the graph exactly once,</li>
 * <li>each consecutive vertex pair corresponds to an edge of the graph in the appropriate
 * direction,</li>
 * <li>the path's edge list matches the vertex list and is consistent with the graph,</li>
 * <li>the reported weight equals the sum of edge weights along the path.</li>
 * </ul>
 */
final class HamiltonianPathValidator
{
    private HamiltonianPathValidator()
    {
    }

    /**
     * Asserts that {@code result} reports {@link Status#PATH_FOUND} and that the contained
     * path is a structurally valid Hamiltonian path on {@code graph}.
     */
    static <V, E> void assertHamiltonianPath(
        Graph<V, E> graph, HamiltonianPathSearchResult<V, E> result)
    {
        assertNotNull(result, "expected a HamiltonianPathSearchResult but got null");
        assertEquals(Status.PATH_FOUND, result.getStatus(),
            () -> "expected PATH_FOUND but got " + result.getStatus());
        GraphPath<V, E> path = result.getPath().orElse(null);
        assertHamiltonianPath(graph, path);
    }

    /**
     * Asserts that {@code result} reports {@link Status#PROVEN_ABSENT} and carries no path.
     */
    static <V, E> void assertProvenAbsent(HamiltonianPathSearchResult<V, E> result)
    {
        assertNotNull(result, "expected a HamiltonianPathSearchResult but got null");
        assertEquals(Status.PROVEN_ABSENT, result.getStatus(),
            () -> "expected PROVEN_ABSENT but got " + result.getStatus());
        assertTrue(result.getPath().isEmpty(), "PROVEN_ABSENT result must carry no path");
    }

    static <V, E> void assertHamiltonianPath(Graph<V, E> graph, GraphPath<V, E> path)
    {
        assertNotNull(path, "expected a Hamiltonian path but got null");

        List<V> vertices = path.getVertexList();
        List<E> edges = path.getEdgeList();

        int n = graph.vertexSet().size();
        assertEquals(n, vertices.size(), "vertex list size mismatches |V|");
        assertEquals(Math.max(0, n - 1), edges.size(), "edge list size mismatches |V| - 1");

        Set<V> seen = new HashSet<>(vertices);
        assertEquals(n, seen.size(), "vertex list contains a duplicate");
        if (!seen.equals(graph.vertexSet())) {
            fail("vertex list does not cover every vertex of the graph");
        }

        assertSame(graph, path.getGraph(), "path graph reference must equal input graph");
        assertEquals(vertices.get(0), path.getStartVertex(), "startVertex mismatch");
        assertEquals(vertices.get(n - 1), path.getEndVertex(), "endVertex mismatch");

        double weight = 0d;
        for (int i = 0; i < edges.size(); i++) {
            V u = vertices.get(i);
            V v = vertices.get(i + 1);
            E e = edges.get(i);
            assertTrue(graph.containsEdge(e), "edge " + e + " is not in graph");
            if (graph.getType().isDirected()) {
                assertEquals(u, graph.getEdgeSource(e), "edge source mismatch at index " + i);
                assertEquals(v, graph.getEdgeTarget(e), "edge target mismatch at index " + i);
            } else {
                V src = graph.getEdgeSource(e);
                V tgt = graph.getEdgeTarget(e);
                boolean forward = u.equals(src) && v.equals(tgt);
                boolean backward = u.equals(tgt) && v.equals(src);
                assertTrue(
                    forward || backward,
                    "edge " + e + " does not connect " + u + " and " + v);
            }
            weight += graph.getEdgeWeight(e);
        }
        assertEquals(weight, path.getWeight(), 1e-9, "reported weight mismatches sum of edge weights");
    }
}
