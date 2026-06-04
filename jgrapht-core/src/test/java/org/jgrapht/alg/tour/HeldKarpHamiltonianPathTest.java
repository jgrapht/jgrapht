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
 * Tests for {@link HeldKarpHamiltonianPath}. Combines deterministic positive/negative cases
 * with a mutual-consistency cross-check against {@link BacktrackingHamiltonianPath} on small
 * random graphs.
 */
public class HeldKarpHamiltonianPathTest
{

    private <V, E> HamiltonianPathSearchResult<V, E> dp(Graph<V, E> graph)
    {
        return new HeldKarpHamiltonianPath<V, E>().getPath(graph);
    }

    @Test
    public void emptyGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        assertThrows(IllegalArgumentException.class, () -> dp(graph));
    }

    @Test
    public void nullGraphThrows()
    {
        assertThrows(NullPointerException.class, () -> dp(null));
    }

    @Test
    public void singleVertexReturnsSingletonPath()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("only");

        HamiltonianPathSearchResult<String, DefaultEdge> result = dp(graph);

        assertNotNull(result);
        assertEquals(Status.PATH_FOUND, result.getStatus());
        GraphPath<String, DefaultEdge> path = result.getPath().orElseThrow();
        assertEquals(1, path.getVertexList().size());
        assertEquals("only", path.getStartVertex());
        assertEquals("only", path.getEndVertex());
        assertEquals(0, path.getEdgeList().size());
    }

    @Test
    public void tooManyVerticesThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i <= HeldKarpHamiltonianPath.DEFAULT_MAX_VERTICES; i++) {
            graph.addVertex(i);
        }
        assertThrows(IllegalArgumentException.class, () -> dp(graph));
    }

    @Test
    public void customMaxVerticesAccepted()
    {
        // With an explicit ceiling of 4, a 4-vertex graph must succeed and a 5-vertex graph
        // must be rejected.
        HeldKarpHamiltonianPath<Integer, DefaultEdge> small =
            new HeldKarpHamiltonianPath<>(4);
        Graph<Integer, DefaultEdge> ok = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            ok.addVertex(i);
        }
        for (int i = 0; i < 3; i++) {
            ok.addEdge(i, i + 1);
        }
        assertHamiltonianPath(ok, small.getPath(ok));

        Graph<Integer, DefaultEdge> tooBig = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            tooBig.addVertex(i);
        }
        assertThrows(IllegalArgumentException.class, () -> small.getPath(tooBig));
    }

    @Test
    public void constructorRejectsOutOfRangeMaxVertices()
    {
        assertThrows(IllegalArgumentException.class, () -> new HeldKarpHamiltonianPath<>(0));
        assertThrows(IllegalArgumentException.class, () -> new HeldKarpHamiltonianPath<>(-1));
        assertThrows(
            IllegalArgumentException.class,
            () -> new HeldKarpHamiltonianPath<>(HeldKarpHamiltonianPath.HARD_MAX_VERTICES + 1));
    }

    @Test
    public void undirectedPathGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 5; i++) {
            graph.addEdge(i, i + 1);
        }

        assertHamiltonianPath(graph, dp(graph));
    }

    @Test
    public void undirectedCompleteGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                graph.addEdge(i, j);
            }
        }
        assertHamiltonianPath(graph, dp(graph));
    }

    @Test
    public void directedChainHasPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 5; i++) {
            graph.addEdge(i, i + 1);
        }

        HamiltonianPathSearchResult<Integer, DefaultEdge> result = dp(graph);

        assertHamiltonianPath(graph, result);
        GraphPath<Integer, DefaultEdge> path = result.getPath().orElseThrow();
        assertEquals(0, path.getStartVertex());
        assertEquals(5, path.getEndVertex());
    }

    @Test
    public void starWithThreeLeavesHasNoPath()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("hub");
        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addEdge("hub", "a");
        graph.addEdge("hub", "b");
        graph.addEdge("hub", "c");

        assertProvenAbsent(dp(graph));
    }

    @Test
    public void disconnectedHasNoPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        assertProvenAbsent(dp(graph));
    }

    @Test
    public void selfLoopsAndParallelEdgesAreHandled()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        for (int i = 0; i < 3; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(0, 0);
        graph.addEdge(1, 1);
        graph.addEdge(0, 1); // parallel edge

        assertHamiltonianPath(graph, dp(graph));
    }

    @Test
    public void weightIsSumOfEdgeWeights()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.setEdgeWeight(graph.addEdge(0, 1), 1d);
        graph.setEdgeWeight(graph.addEdge(1, 2), 2d);
        graph.setEdgeWeight(graph.addEdge(2, 3), 3d);

        HamiltonianPathSearchResult<Integer, DefaultEdge> result = dp(graph);
        assertHamiltonianPath(graph, result);
        assertEquals(6d, result.getPath().orElseThrow().getWeight(), 1e-9);
    }

    @Test
    public void crossValidationAgainstBacktrackingUndirected()
    {
        Random random = new Random(0xBEEFFACE12345678L);
        for (int n = 2; n <= 8; n++) {
            for (double p : new double[] { 0.3, 0.5, 0.8 }) {
                for (int t = 0; t < 25; t++) {
                    Graph<Integer, DefaultEdge> graph = randomUndirected(n, p, random);
                    crossCheck(graph);
                }
            }
        }
    }

    @Test
    public void crossValidationAgainstBacktrackingDirected()
    {
        Random random = new Random(0xFEEDBEEF87654321L);
        for (int n = 2; n <= 8; n++) {
            for (double p : new double[] { 0.3, 0.5, 0.8 }) {
                for (int t = 0; t < 25; t++) {
                    Graph<Integer, DefaultEdge> graph = randomDirected(n, p, random);
                    crossCheck(graph);
                }
            }
        }
    }

    private void crossCheck(Graph<Integer, DefaultEdge> graph)
    {
        HamiltonianPathSearchResult<Integer, DefaultEdge> dpResult = dp(graph);
        HamiltonianPathSearchResult<Integer, DefaultEdge> btResult =
            new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
        boolean dpFound = dpResult.getPath().isPresent();
        boolean btFound = btResult.getPath().isPresent();
        assertEquals(btFound, dpFound, () -> "existence disagreement on graph " + graph);
        if (dpFound) {
            assertHamiltonianPath(graph, dpResult);
        }
        if (btFound) {
            assertHamiltonianPath(graph, btResult);
        }
    }

    private Graph<Integer, DefaultEdge> randomUndirected(int n, double p, Random random)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < p) {
                    graph.addEdge(i, j);
                }
            }
        }
        return graph;
    }

    private Graph<Integer, DefaultEdge> randomDirected(int n, double p, Random random)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && random.nextDouble() < p) {
                    graph.addEdge(i, j);
                }
            }
        }
        return graph;
    }
}
