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
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import static org.jgrapht.alg.hamiltonian.HamiltonianPathValidator.assertHamiltonianPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Deterministic unit tests for {@link BacktrackingHamiltonianPath}.
 */
public class BacktrackingHamiltonianPathTest
{

    private <V, E> GraphPath<V, E> findPath(Graph<V, E> graph)
    {
        return new BacktrackingHamiltonianPath<V, E>().getPath(graph);
    }

    @Test
    public void emptyGraphThrows()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        assertThrows(IllegalArgumentException.class, () -> findPath(graph));
    }

    @Test
    public void nullGraphThrows()
    {
        assertThrows(NullPointerException.class, () -> findPath(null));
    }

    @Test
    public void singleVertexReturnsSingletonPath()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("only");

        GraphPath<String, DefaultEdge> path = findPath(graph);

        assertNotNull(path);
        assertEquals(1, path.getVertexList().size());
        assertEquals("only", path.getStartVertex());
        assertEquals("only", path.getEndVertex());
        assertEquals(0, path.getEdgeList().size());
        assertEquals(0d, path.getWeight(), 0d);
    }

    @Test
    public void undirectedPathGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            graph.addEdge(i, i + 1);
        }

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void undirectedCycleGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 6; i++) {
            graph.addEdge(i, (i + 1) % 6);
        }

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void undirectedCompleteGraphK4()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                graph.addEdge(i, j);
            }
        }

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void disconnectedUndirectedGraphHasNoPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        assertNull(findPath(graph));
    }

    @Test
    public void starWithThreeLeavesHasNoPath()
    {
        // K_{1,3}: hub plus three leaves -> three degree-1 vertices, more than two endpoints.
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("hub");
        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addEdge("hub", "a");
        graph.addEdge("hub", "b");
        graph.addEdge("hub", "c");

        assertNull(findPath(graph));
    }

    @Test
    public void twoIsolatedVerticesHaveNoPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);

        assertNull(findPath(graph));
    }

    @Test
    public void directedChainHasPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            graph.addEdge(i, i + 1);
        }

        GraphPath<Integer, DefaultEdge> path = findPath(graph);

        assertHamiltonianPath(graph, path);
        assertEquals(0, path.getStartVertex());
        assertEquals(4, path.getEndVertex());
    }

    @Test
    public void directedChainMissingMiddleEdgeHasNoPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        // gap: no 1 -> 2 edge.
        graph.addEdge(2, 3);

        assertNull(findPath(graph));
    }

    @Test
    public void disjointStronglyConnectedComponentsHaveNoPath()
    {
        // Two strongly-connected pairs with no edge between them. The SCC condensation has two
        // vertices and zero edges, so no Hamiltonian projection exists.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);

        assertNull(findPath(graph));
    }

    @Test
    public void chainOfStronglyConnectedComponentsHasPath()
    {
        // SCC A = {0, 1, 2} (triangle); SCC B = {3, 4, 5} (triangle); single bridge 2 -> 3.
        // Condensation is A -> B, a chain DAG, so a Hamiltonian path is possible. Example:
        // 0 -> 1 -> 2 -> 3 -> 4 -> 5.
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 3);
        graph.addEdge(2, 3);

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void directedGraphWithUnreachableVertexHasNoPath()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        // vertex 3 is isolated.

        assertNull(findPath(graph));
    }

    @Test
    public void directedAcyclicGraphWithHamiltonianPath()
    {
        // Topological chain plus a forward shortcut.
        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addVertex("d");
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        graph.addEdge("c", "d");
        graph.addEdge("a", "c"); // shortcut, not needed for the path

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void weightedUndirectedGraphReturnsCorrectWeight()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        graph.setEdgeWeight(graph.addEdge(0, 1), 1.5);
        graph.setEdgeWeight(graph.addEdge(1, 2), 2.5);
        graph.setEdgeWeight(graph.addEdge(2, 3), 3.5);

        GraphPath<Integer, DefaultEdge> path = findPath(graph);

        assertHamiltonianPath(graph, path);
        assertEquals(7.5, path.getWeight(), 1e-9);
    }

    @Test
    public void selfLoopsAreIgnored()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        for (int i = 0; i < 3; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(0, 0);
        graph.addEdge(2, 2);

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void selfLoopOnCutVertexIsIgnored()
    {
        // BiconnectivityInspector may treat each self-loop as a separate biconnected block,
        // which would inflate a cut vertex's block count and trigger a spurious rejection. The
        // cut-vertex precheck must filter self-loops first so the solver's documented
        // "self-loops are ignored" semantics holds even on articulation vertices.
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(1, 1); // self-loop on the cut vertex

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void selfLoopsOnMultipleCutVerticesAreIgnored()
    {
        // Path 0-1-2-3-4 with self-loops on every internal vertex. Every internal vertex is a
        // cut vertex, so a self-loop-counts-as-extra-block bug would push the block count past
        // 2 on each and spuriously reject the graph.
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            graph.addEdge(i, i + 1);
        }
        graph.addEdge(1, 1);
        graph.addEdge(2, 2);
        graph.addEdge(3, 3);

        assertHamiltonianPath(graph, findPath(graph));
    }

    @Test
    public void undirectedGraphWithArticulationStillHasPath()
    {
        // Two triangles joined at a single vertex form an articulation point.
        // 0-1-2-0 and 2-3-4-2: a Hamiltonian path is 0-1-2-3-4.
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);

        assertHamiltonianPath(graph, findPath(graph));
    }
}
