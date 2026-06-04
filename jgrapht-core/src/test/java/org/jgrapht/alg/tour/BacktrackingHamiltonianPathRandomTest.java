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
import org.junit.jupiter.api.*;

import java.util.*;

import static org.jgrapht.alg.tour.HamiltonianPathValidator.assertHamiltonianPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cross-validates {@link BacktrackingHamiltonianPath} against a permutation brute-force oracle
 * on small random graphs. Guards against pruning-induced false negatives by independently
 * deciding existence and, when a path is reported, structurally validating it. The oracle is
 * exponential by construction and limited to {@code n <= 6} so the suite stays in the fast
 * test budget.
 */
public class BacktrackingHamiltonianPathRandomTest
{

    private static final long SEED = 0xC0FFEE0123456789L; // deterministic seed
    private static final int GRAPHS_PER_CONFIG = 40;

    @Test
    public void undirectedRandomGraphsMatchOracle()
    {
        Random random = new Random(SEED);
        for (int n = 2; n <= 6; n++) {
            for (double p : new double[] { 0.3, 0.5, 0.8 }) {
                for (int t = 0; t < GRAPHS_PER_CONFIG; t++) {
                    Graph<Integer, DefaultEdge> graph =
                        randomUndirected(n, p, random);
                    runOne(graph);
                }
            }
        }
    }

    @Test
    public void directedRandomGraphsMatchOracle()
    {
        Random random = new Random(SEED ^ 0x5A5A5A5AL);
        for (int n = 2; n <= 6; n++) {
            for (double p : new double[] { 0.3, 0.5, 0.8 }) {
                for (int t = 0; t < GRAPHS_PER_CONFIG; t++) {
                    Graph<Integer, DefaultEdge> graph = randomDirected(n, p, random);
                    runOne(graph);
                }
            }
        }
    }

    private void runOne(Graph<Integer, DefaultEdge> graph)
    {
        boolean oracle = bruteForceExists(graph);
        HamiltonianPathSearchResult<Integer, DefaultEdge> result =
            new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph);
        boolean found = result.getPath().isPresent();
        assertEquals(oracle, found, () -> "disagreement on graph " + graph);
        if (found) {
            assertHamiltonianPath(graph, result);
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

    /**
     * Brute-force oracle: returns true iff some permutation of the vertices is a valid path in
     * {@code graph}. The graph must have at least one vertex.
     */
    private boolean bruteForceExists(Graph<Integer, DefaultEdge> graph)
    {
        List<Integer> vertices = new ArrayList<>(graph.vertexSet());
        if (vertices.size() == 1) {
            return true;
        }
        return permute(graph, vertices, 0);
    }

    private boolean permute(Graph<Integer, DefaultEdge> graph, List<Integer> v, int from)
    {
        if (from == v.size()) {
            for (int i = 1; i < v.size(); i++) {
                if (!graph.containsEdge(v.get(i - 1), v.get(i))) {
                    return false;
                }
            }
            return true;
        }
        for (int i = from; i < v.size(); i++) {
            Collections.swap(v, from, i);
            if (permute(graph, v, from + 1)) {
                return true;
            }
            Collections.swap(v, from, i);
        }
        return false;
    }
}
