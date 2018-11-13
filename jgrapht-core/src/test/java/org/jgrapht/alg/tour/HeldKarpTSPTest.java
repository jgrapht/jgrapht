/*
 * (C) Copyright 2017-2018, by Alexandru Valeanu and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.util.*;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.*;

/**
 * Tests for {@link HeldKarpTSP}
 *
 * @author Alexandru Valeanu
 *
 */
@Category(SlowTests.class)
public class HeldKarpTSPTest
{
    static Graph<String, DefaultWeightedEdge> directedGraph()
    {
        // Solution exists; cost 26

        Graph<String, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");

        g.setEdgeWeight(g.addEdge("0", "1"), 9d);
        g.setEdgeWeight(g.addEdge("0", "3"), 8d);
        g.setEdgeWeight(g.addEdge("1", "0"), 7d);
        g.setEdgeWeight(g.addEdge("1", "2"), 1d);
        g.setEdgeWeight(g.addEdge("1", "4"), 3d);
        g.setEdgeWeight(g.addEdge("2", "0"), 5d);
        g.setEdgeWeight(g.addEdge("2", "4"), 4d);
        g.setEdgeWeight(g.addEdge("3", "2"), 6d);
        g.setEdgeWeight(g.addEdge("4", "3"), 7d);
        g.setEdgeWeight(g.addEdge("4", "1"), 1d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> directedGraph2()
    {
        // Solution exists; cost 2166782

        Graph<String, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");

        g.setEdgeWeight(g.addEdge("1", "3"), 578985d);
        g.setEdgeWeight(g.addEdge("1", "2"), 316670d);
        g.setEdgeWeight(g.addEdge("2", "3"), 121118d);
        g.setEdgeWeight(g.addEdge("3", "2"), 585978d);
        g.setEdgeWeight(g.addEdge("0", "1"), 220022d);
        g.setEdgeWeight(g.addEdge("2", "1"), 62190d);
        g.setEdgeWeight(g.addEdge("0", "3"), 599952d);
        g.setEdgeWeight(g.addEdge("3", "1"), 540561d);
        g.setEdgeWeight(g.addEdge("0", "2"), 960850d);
        g.setEdgeWeight(g.addEdge("2", "0"), 781797d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> noSolutionDirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");

        g.setEdgeWeight(g.addEdge("2", "1"), 200526d);
        g.setEdgeWeight(g.addEdge("1", "3"), 427820d);
        g.setEdgeWeight(g.addEdge("3", "1"), 375699d);
        g.setEdgeWeight(g.addEdge("3", "2"), 541104d);
        g.setEdgeWeight(g.addEdge("0", "2"), 311063d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> noSolutionUndirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");

        g.setEdgeWeight(g.addEdge("0", "1"), 1d);
        g.setEdgeWeight(g.addEdge("0", "2"), 1d);
        g.setEdgeWeight(g.addEdge("0", "3"), 1d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> undirectedGraph()
    {
        // Solution exists; cost 80

        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");

        g.setEdgeWeight(g.addEdge("1", "2"), 10d);
        g.setEdgeWeight(g.addEdge("1", "3"), 15d);
        g.setEdgeWeight(g.addEdge("1", "4"), 20d);
        g.setEdgeWeight(g.addEdge("2", "3"), 35d);
        g.setEdgeWeight(g.addEdge("2", "4"), 25d);
        g.setEdgeWeight(g.addEdge("3", "4"), 30d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> symmetric4CitiesGraph()
    {
        // Solution exists; cost 97

        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");

        g.setEdgeWeight(g.addEdge("A", "B"), 20d);
        g.setEdgeWeight(g.addEdge("A", "C"), 42d);
        g.setEdgeWeight(g.addEdge("A", "D"), 35d);
        g.setEdgeWeight(g.addEdge("B", "C"), 30d);
        g.setEdgeWeight(g.addEdge("B", "D"), 34d);
        g.setEdgeWeight(g.addEdge("C", "D"), 12d);

        return g;
    }

    static Graph<String, DefaultWeightedEdge> oneVertexGraph()
    {
        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex("A");

        return g;
    }

    @Test
    public void testDirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g = directedGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(tour.getWeight(), 26d, 1e-9);
    }

    @Test
    public void testDirectedGraph2()
    {
        Graph<String, DefaultWeightedEdge> g = directedGraph2();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(tour.getWeight(), 2166782d, 1e-9);
    }

    @Test
    public void testUndirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g = undirectedGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(tour.getWeight(), 80d, 1e-9);
    }

    @Test
    public void testUndirectedGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        int[][] weights = new int[5][];

        weights[0] = new int[] { 0, 8, 7, 5, 6 };
        weights[1] = new int[] { 8, 0, 3, 1, 7 };
        weights[2] = new int[] { 7, 3, 0, 8, 6 };
        weights[3] = new int[] { 5, 1, 8, 0, 1 };
        weights[4] = new int[] { 6, 7, 6, 1, 0 };

        for (int i = 0; i < 5; i++) {
            g.addVertex(i);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                g.addEdge(i, j);
                g.setEdgeWeight(g.getEdge(i, j), weights[i][j]);
            }
        }

        GraphPath<Integer, DefaultWeightedEdge> tour =
            new HeldKarpTSP<Integer, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(tour.getWeight(), 18d, 1e-9);
    }

    @Test
    public void testDirectedWeightedPseudograph()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        int[][] weights = new int[5][];

        weights[0] = new int[] { 0, 9, 3, 3, 7 };
        weights[1] = new int[] { 9, 0, 10, 7, 5 };
        weights[2] = new int[] { 3, 10, 0, 1, 1 };
        weights[3] = new int[] { 3, 7, 1, 0, 10 };
        weights[4] = new int[] { 7, 5, 1, 10, 0 };

        for (int i = 0; i < 5; i++) {
            g.addVertex(i);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                g.addEdge(i, j);
                g.setEdgeWeight(g.getEdge(i, j), weights[i][j]);
            }
        }

        GraphPath<Integer, DefaultWeightedEdge> tour =
            new HeldKarpTSP<Integer, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(19d, tour.getWeight(), 1e-9);
    }

    @Test
    public void testWikiExampleSymmetric4Cities()
    {
        Graph<String, DefaultWeightedEdge> g = symmetric4CitiesGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNotNull(tour);
        assertHamiltonian(g, tour);
        assertEquals(tour.getWeight(), 97d, 1e-9);
    }

    @Test
    public void testNoSolutionDirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g = noSolutionDirectedGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNull(tour);
    }

    @Test
    public void testNoSolutionUndirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> g = noSolutionUndirectedGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertNull(tour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstanceEmpty()
    {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        new HeldKarpTSP<String, DefaultEdge>().getTour(g);
    }

    @Test
    public void testOneVertexGraph()
    {
        Graph<String, DefaultWeightedEdge> g = oneVertexGraph();

        GraphPath<String, DefaultWeightedEdge> tour =
            new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

        assertHamiltonian(g, tour);
    }

    @Test
    public void testRandomGraphs()
    {
        /*
         * Generate 500 'random' directed multigraphs that contain a tour
         */

        final int NUM_TESTS = 500;
        Random random = new Random(123);

        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<String, DefaultWeightedEdge> g =
                new DirectedMultigraph<>(DefaultWeightedEdge.class);

            // Generate n - number of nodes; 2 <= n <= 20
            final int n = 2 + random.nextInt(19);
            for (int i = 0; i < n; i++) {
                g.addVertex(Integer.toString(i));
            }

            // Make sure that there is at least one path
            for (int i = 0; i < n - 1; i++) {
                g.addEdge(Integer.toString(i), Integer.toString(i + 1));
            }

            if (n > 1)
                g.addEdge(Integer.toString(n - 1), Integer.toString(0));

            // Add some extra edges
            final int m = n * (1 + random.nextInt(6));
            for (int i = 0; i < m; i++) {
                int u = random.nextInt(n);
                int v = random.nextInt(n);

                if (u != v)
                    g.addEdge(Integer.toString(u), Integer.toString(v));
            }

            GraphPath<String, DefaultWeightedEdge> tour =
                new HeldKarpTSP<String, DefaultWeightedEdge>().getTour(g);

            assertNotNull(tour);
            assertHamiltonian(g, tour);
        }
    }
}
