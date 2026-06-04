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
package org.jgrapht.demo;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.tour.*;
import org.jgrapht.graph.*;

/**
 * Hamiltonian path demos for both directed and undirected graphs.
 *
 * <p>
 * Hamiltonian path existence is NP-complete in general. The algorithms shown here are exact
 * exponential-time solvers and are intended for relatively small graphs. For large graphs no
 * polynomial-time algorithm is known.
 *
 * <p>
 * The demo prints the path (a vertex sequence and its weight) when one exists, and a message
 * otherwise.
 *
 * @author seilat
 */
public final class HamiltonianPathDemo
{
    private HamiltonianPathDemo()
    {
    }

    public static void main(String[] args)
    {
        runUndirectedExample();
        System.out.println();
        runDirectedDagExample();
        System.out.println();
        runHeldKarpExample();
    }

    private static void runUndirectedExample()
    {
        System.out.println("== Undirected backtracking solver ==");
        // Five-vertex graph: a path 0 - 1 - 2 - 3 - 4 with one extra triangle edge.
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(2, 4);

        printResult(new BacktrackingHamiltonianPath<Integer, DefaultEdge>().getPath(graph));
    }

    private static void runDirectedDagExample()
    {
        System.out.println("== Directed acyclic special case ==");
        Graph<String, DefaultEdge> dag = new SimpleDirectedGraph<>(DefaultEdge.class);
        dag.addVertex("plan");
        dag.addVertex("design");
        dag.addVertex("build");
        dag.addVertex("test");
        dag.addVertex("ship");
        dag.addEdge("plan", "design");
        dag.addEdge("design", "build");
        dag.addEdge("build", "test");
        dag.addEdge("test", "ship");
        dag.addEdge("design", "test"); // forward shortcut

        printResult(new DagHamiltonianPath<String, DefaultEdge>().getPath(dag));
    }

    private static void runHeldKarpExample()
    {
        System.out.println("== Held-Karp dynamic programming (small graphs) ==");
        // A six-vertex cycle, which trivially has a Hamiltonian path.
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 6; i++) {
            graph.addEdge(i, (i + 1) % 6);
        }

        printResult(new HeldKarpHamiltonianPath<Integer, DefaultEdge>().getPath(graph));
    }

    private static <V, E> void printResult(HamiltonianPathSearchResult<V, E> result)
    {
        if (result.getPath().isEmpty()) {
            System.out.println("No Hamiltonian path exists (status=" + result.getStatus() + ").");
            return;
        }
        GraphPath<V, E> path = result.getPath().orElseThrow();
        System.out.println("Hamiltonian path: " + path.getVertexList());
        System.out.println("Weight: " + path.getWeight());
    }
}
