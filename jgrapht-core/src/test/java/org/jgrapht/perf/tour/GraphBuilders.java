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
package org.jgrapht.perf.tour;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.perf.tour.HamiltonianPathPerformanceTest.GraphFamily;

import java.util.*;

/**
 * Deterministic builders for Hamiltonian path benchmark graph families. Each builder produces
 * the same graph for the same {@code (family, n)} pair so JMH measurement iterations and the
 * diagnostic state-count harness measure consistent inputs.
 */
final class GraphBuilders
{
    private GraphBuilders()
    {
    }

    static Graph<Integer, DefaultEdge> build(GraphFamily family, int n)
    {
        Random random = new Random(0xBEEFCAFE12345678L ^ ((long) n << 16) ^ family.ordinal());
        switch (family) {
        case PATH:
            return path(n);
        case CYCLE:
            return cycle(n);
        case COMPLETE:
            return complete(n);
        case SPARSE:
            return sparseErdosRenyi(n, 3.0 / n, random);
        case STAR_NEG:
            return star(n);
        case MODULAR_BRIDGES:
            return modularBridges(n);
        case DAG_POS:
            return dagWithShortcuts(n, random);
        default:
            throw new IllegalArgumentException("unsupported family: " + family);
        }
    }

    private static Graph<Integer, DefaultEdge> path(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int i = 0; i < n - 1; i++) {
            g.addEdge(i, i + 1);
        }
        return g;
    }

    private static Graph<Integer, DefaultEdge> cycle(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int i = 0; i < n; i++) {
            g.addEdge(i, (i + 1) % n);
        }
        return g;
    }

    private static Graph<Integer, DefaultEdge> complete(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }

    private static Graph<Integer, DefaultEdge> sparseErdosRenyi(int n, double p, Random random)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < p) {
                    g.addEdge(i, j);
                }
            }
        }
        return g;
    }

    /**
     * Star K_{1, n-1}: vertex 0 is the hub, vertices 1..n-1 are leaves. For {@code n > 3} there
     * are more than two degree-1 vertices, so no Hamiltonian path exists. The cheap leaf
     * precheck rejects immediately.
     */
    private static Graph<Integer, DefaultEdge> star(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int leaf = 1; leaf < n; leaf++) {
            g.addEdge(0, leaf);
        }
        return g;
    }

    /**
     * Block-bridge graph: split the {@code n} vertices into chunks of size 3 forming triangles,
     * with a single bridge edge between consecutive chunks. A Hamiltonian path traversing each
     * triangle and crossing each bridge always exists, which exercises the cut-vertex degree
     * check on the positive side.
     */
    private static Graph<Integer, DefaultEdge> modularBridges(int n)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        addVertices(g, n);
        int chunk = 3;
        int idx = 0;
        while (idx + chunk <= n) {
            int base = idx;
            g.addEdge(base, base + 1);
            g.addEdge(base + 1, base + 2);
            g.addEdge(base, base + 2);
            if (base + chunk < n) {
                g.addEdge(base + 2, base + chunk); // bridge
            }
            idx += chunk;
        }
        // Tail: any remaining vertices are connected linearly to the previous block tail.
        for (int v = idx; v < n; v++) {
            g.addEdge(v - 1, v);
        }
        return g;
    }

    /**
     * DAG chain {@code 0 -> 1 -> ... -> n-1} plus a handful of forward shortcuts. Has a
     * Hamiltonian path (the chain itself). Exercises directed pruning and the SCC precheck.
     */
    private static Graph<Integer, DefaultEdge> dagWithShortcuts(int n, Random random)
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        addVertices(g, n);
        for (int i = 0; i < n - 1; i++) {
            g.addEdge(i, i + 1);
        }
        int shortcuts = Math.max(1, n / 3);
        for (int s = 0; s < shortcuts; s++) {
            int from = random.nextInt(n - 2);
            int to = from + 2 + random.nextInt(Math.max(1, n - from - 2));
            if (to >= n) {
                to = n - 1;
            }
            g.addEdge(from, to);
        }
        return g;
    }

    private static void addVertices(Graph<Integer, DefaultEdge> g, int n)
    {
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
    }
}
