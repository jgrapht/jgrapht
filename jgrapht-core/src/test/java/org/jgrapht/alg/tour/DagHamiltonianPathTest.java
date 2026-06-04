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
import static org.jgrapht.alg.tour.HamiltonianPathValidator.assertProvenAbsent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link DagHamiltonianPath}.
 */
public class DagHamiltonianPathTest
{

    private <V, E> HamiltonianPathSearchResult<V, E> dag(Graph<V, E> graph)
    {
        return new DagHamiltonianPath<V, E>().getPath(graph);
    }

    @Test
    public void emptyGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        assertThrows(IllegalArgumentException.class, () -> dag(graph));
    }

    @Test
    public void undirectedGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        assertThrows(IllegalArgumentException.class, () -> dag(graph));
    }

    @Test
    public void cyclicDirectedGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        assertThrows(IllegalArgumentException.class, () -> dag(graph));
    }

    @Test
    public void singleVertexReturnsSingletonPath()
    {
        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("only");

        HamiltonianPathSearchResult<String, DefaultEdge> result = dag(graph);

        assertNotNull(result);
        assertEquals(Status.PATH_FOUND, result.getStatus());
        assertEquals(1, result.getPath().orElseThrow().getVertexList().size());
    }

    @Test
    public void linearChainHasPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 5; i++) {
            graph.addEdge(i, i + 1);
        }
        HamiltonianPathSearchResult<Integer, DefaultEdge> result = dag(graph);
        assertHamiltonianPath(graph, result);
        GraphPath<Integer, DefaultEdge> path = result.getPath().orElseThrow();
        assertEquals(0, path.getStartVertex());
        assertEquals(5, path.getEndVertex());
    }

    @Test
    public void chainPlusForwardShortcutsHasPath()
    {
        // 0 -> 1 -> 2 -> 3 -> 4 with shortcuts 0->2 and 1->3
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);

        assertHamiltonianPath(graph, dag(graph));
    }

    @Test
    public void parallelChainsHaveNoPath()
    {
        // Two independent chains of length 3, no edges between them
        // The longest path covers only one chain, so no Hamiltonian path.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        assertProvenAbsent(dag(graph));
    }

    @Test
    public void diamondDagWithoutHamiltonianPath()
    {
        // 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3. Longest path = 3 (0->1->3 or 0->2->3),
        // but n = 4. Missing edge between 1 and 2 (in either direction) prevents covering all
        // vertices in one directed sequence.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        assertProvenAbsent(dag(graph));
    }

    @Test
    public void diamondDagWithCrossEdgeHasPath()
    {
        // Add 1 -> 2 to the diamond: now 0 -> 1 -> 2 -> 3 is a Hamiltonian path.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        assertHamiltonianPath(graph, dag(graph));
    }

    @Test
    public void crossValidationAgainstBacktrackingOnRandomDags()
    {
        Random random = new Random(0xDA6DA6L);
        for (int n = 2; n <= 8; n++) {
            for (double p : new double[] { 0.3, 0.5, 0.8 }) {
                for (int t = 0; t < 25; t++) {
                    Graph<Integer, DefaultEdge> graph = randomDag(n, p, random);
                    HamiltonianPathSearchResult<Integer, DefaultEdge> dagResult = dag(graph);
                    HamiltonianPathSearchResult<Integer, DefaultEdge> btResult =
                        new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
                    boolean dagFound = dagResult.getPath().isPresent();
                    boolean btFound = btResult.getPath().isPresent();
                    assertEquals(
                        btFound, dagFound,
                        () -> "existence disagreement on DAG " + graph);
                    if (dagFound) {
                        assertHamiltonianPath(graph, dagResult);
                    }
                }
            }
        }
    }

    private Graph<Integer, DefaultEdge> randomDag(int n, double p, Random random)
    {
        // Randomise vertex order, only add edges from earlier to later positions to guarantee
        // acyclicity.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
            graph.addVertex(i);
        }
        Collections.shuffle(order, random);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < p) {
                    graph.addEdge(order.get(i), order.get(j));
                }
            }
        }
        return graph;
    }
}
