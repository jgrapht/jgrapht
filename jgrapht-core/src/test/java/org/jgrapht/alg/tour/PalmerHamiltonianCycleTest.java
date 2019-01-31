/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PalmerHamiltonianCycleTest
{

    /**
     * Small graph of 4 nodes.
     */
    @Test
    public void testSmallGraph()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        GraphPath<String, DefaultEdge> tour =
            new PalmerHamiltonianCycle<String, DefaultEdge>().getTour(graph);

        assertNotNull(tour);
        assertHamiltonian(graph, tour);
    }

    /**
     * Test that contains a simple cycle of 10 nodes. The graph has a Hamiltonian cycle but it
     * doesn't meet Ore's condition.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLineGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 0; i < 10; i++) {
            graph.addVertex(i);
        }

        for (int i = 0; i < 10; i++) {
            graph.addEdge(i, (i + 1) % 10);
        }

        GraphPath<Integer, DefaultEdge> tour =
            new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

        assertNotNull(tour);
        assertHamiltonian(graph, tour);
    }

    private void testRandomGraphs(Random random)
    {
        final int NUM_TESTS = 500;
        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            final int n = 3 + random.nextInt(150);

            for (int i = 0; i < n; i++) {
                graph.addVertex(i);
            }

            List<Integer> vertexList = new ArrayList<>(graph.vertexSet());

            while (!GraphTests.hasOreProperty(graph)) {
                Collections.shuffle(vertexList, random);

                search: for (int i = 0; i < vertexList.size(); i++) {
                    for (int j = i + 1; j < vertexList.size(); j++) {
                        int u = vertexList.get(i);
                        int v = vertexList.get(j);

                        if (!graph.containsEdge(u, v)
                            && graph.degreeOf(u) + graph.degreeOf(v) < n)
                        {
                            graph.addEdge(u, v);
                            break search;
                        }
                    }
                }
            }

            GraphPath<Integer, DefaultEdge> tour =
                new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

            assertNotNull(tour);
            assertHamiltonian(graph, tour);
        }
    }

    /**
     * Test with 500 randomly generated graphs. Method of generation: randomly add edges while the
     * graph doesn't have Ore's property
     */
    @Test
    @Category(SlowTests.class)
    public void testRandomGraphs()
    {
        testRandomGraphs(new Random(0xC0FFEE));
    }

    private void testRandomGraphs2(Random random)
    {
        final int NUM_TESTS = 500;
        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            final int n = 3 + random.nextInt(150);

            for (int i = 0; i < n; i++) {
                graph.addVertex(i);
            }

            List<Integer> vertexList = new ArrayList<>(graph.vertexSet());
            boolean changed;

            do {
                changed = false;
                Collections.shuffle(vertexList, random);

                search: for (int v : vertexList) {
                    if (graph.degreeOf(v) < (n + 1) / 2) {
                        for (int u : vertexList) {
                            if (u != v && !graph.containsEdge(u, v)) {
                                graph.addEdge(u, v);
                                changed = true;
                                break search;
                            }
                        }
                    }
                }

            } while (changed);

            GraphPath<Integer, DefaultEdge> tour =
                new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

            assertNotNull(tour);
            assertHamiltonian(graph, tour);
        }
    }

    /**
     * Test with 500 randomly generated graphs (fixed seed). Method of generation: make sure that
     * each node has (n+1)/2 neighbours
     */
    @Test
    @Category(SlowTests.class)
    public void testRandomGraphs2FixedSeed()
    {
        testRandomGraphs2(new Random(0xBEEF));
    }

    private static Graph<Integer, DefaultEdge> bigGraph = new SimpleGraph<>(DefaultEdge.class);

    @BeforeClass
    public static void generateBigGraph()
    {
        Random random = new Random(0xC0FFEE);
        final int n = 1000;

        for (int i = 0; i < n; i++) {
            bigGraph.addVertex(i);
        }

        List<Integer> vertexList = new ArrayList<>(bigGraph.vertexSet());

        while (!GraphTests.hasOreProperty(bigGraph)) {
            Collections.shuffle(vertexList, random);

            search: for (int i = 0; i < vertexList.size(); i++) {
                for (int j = i + 1; j < vertexList.size(); j++) {
                    int u = vertexList.get(i);
                    int v = vertexList.get(j);

                    if (!bigGraph.containsEdge(u, v)
                        && bigGraph.degreeOf(u) + bigGraph.degreeOf(v) < n)
                    {
                        bigGraph.addEdge(u, v);
                        break search;
                    }
                }
            }
        }

        assertTrue(GraphTests.hasOreProperty(bigGraph));
    }

    @Test
    @Category(SlowTests.class)
    public void testBigGraph()
    {
        GraphPath<Integer, DefaultEdge> tour =
            new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(bigGraph);

        assertNotNull(tour);
        assertHamiltonian(bigGraph, tour);
    }
}
