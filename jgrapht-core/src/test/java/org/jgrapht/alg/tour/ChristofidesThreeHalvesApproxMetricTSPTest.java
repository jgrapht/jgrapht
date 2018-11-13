/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link ChristofidesThreeHalvesApproxMetricTSP}
 *
 * @author Timofey Chudakov
 */
public class ChristofidesThreeHalvesApproxMetricTSPTest
{

    /**
     * Directed graph
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTour0()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        approxMetricTSP.getTour(graph);
    }

    /**
     * Empty graph
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTour1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        approxMetricTSP.getTour(graph);
    }

    /**
     * Not complete
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTour2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        approxMetricTSP.getTour(graph);
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        GraphPath<Integer, DefaultWeightedEdge> tour = approxMetricTSP.getTour(graph);
        assertHamiltonian(graph, tour);
        assertEquals(10, tour.getWeight(), 1e-9);
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        Graphs.addEdgeWithVertices(graph, 1, 3, 5);
        Graphs.addEdgeWithVertices(graph, 2, 3, 9);
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        GraphPath<Integer, DefaultWeightedEdge> tour = approxMetricTSP.getTour(graph);
        assertHamiltonian(graph, tour);
        assertEquals(19, tour.getWeight(), 1e-9);
    }

    @Test
    public void testGetTour5()
    {
        int[][] edges = new int[][] { { 1, 0, 2 }, { 2, 0, 5 }, { 2, 1, 6 }, { 3, 0, 2 },
            { 3, 1, 4 }, { 3, 2, 5 } };
        testOnInstance(edges, 15);
    }

    @Test
    public void testGetTour6()
    {
        int[][] edges = new int[][] { { 1, 0, 8 }, { 2, 0, 4 }, { 2, 1, 4 }, { 3, 0, 5 },
            { 3, 1, 8 }, { 3, 2, 6 }, { 4, 0, 7 }, { 4, 1, 7 }, { 4, 2, 5 }, { 4, 3, 6 } };
        testOnInstance(edges, 26);
    }

    @Test
    public void testGetTour7()
    {
        int[][] edges = new int[][] { { 1, 0, 3 }, { 2, 0, 6 }, { 2, 1, 7 }, { 3, 0, 6 },
            { 3, 1, 7 }, { 3, 2, 7 }, { 4, 0, 5 }, { 4, 1, 6 }, { 4, 2, 9 }, { 4, 3, 9 },
            { 5, 0, 3 }, { 5, 1, 2 }, { 5, 2, 10 }, { 5, 3, 10 }, { 5, 4, 9 } };
        testOnInstance(edges, 33);
    }

    @Test
    public void testGetTour8()
    {
        int[][] edges = new int[][] { { 1, 0, 6 }, { 2, 0, 2 }, { 2, 1, 9 }, { 3, 0, 7 },
            { 3, 1, 1 }, { 3, 2, 8 }, { 4, 0, 2 }, { 4, 1, 7 }, { 4, 2, 3 }, { 4, 3, 8 },
            { 5, 0, 5 }, { 5, 1, 5 }, { 5, 2, 6 }, { 5, 3, 6 }, { 5, 4, 3 }, { 6, 0, 4 },
            { 6, 1, 5 }, { 6, 2, 5 }, { 6, 3, 6 }, { 6, 4, 2 }, { 6, 5, 5 } };
        testOnInstance(edges, 24);
    }

    @Test
    public void testGetTour9()
    {
        int[][] edges = new int[][] { { 1, 0, 1 }, { 2, 0, 3 }, { 2, 1, 2 }, { 3, 0, 5 },
            { 3, 1, 6 }, { 3, 2, 8 }, { 4, 0, 4 }, { 4, 1, 5 }, { 4, 2, 7 }, { 4, 3, 4 },
            { 5, 0, 6 }, { 5, 1, 7 }, { 5, 2, 9 }, { 5, 3, 6 }, { 5, 4, 8 }, { 6, 0, 6 },
            { 6, 1, 7 }, { 6, 2, 9 }, { 6, 3, 6 }, { 6, 4, 8 }, { 6, 5, 9 }, { 7, 0, 4 },
            { 7, 1, 5 }, { 7, 2, 7 }, { 7, 3, 4 }, { 7, 4, 6 }, { 7, 5, 7 }, { 7, 6, 6 } };
        testOnInstance(edges, 39);
    }

    @Test
    public void testGetTour10()
    {
        int[][] edges = new int[][] { { 1, 0, 5 }, { 2, 0, 4 }, { 2, 1, 5 }, { 3, 0, 3 },
            { 3, 1, 7 }, { 3, 2, 6 }, { 4, 0, 5 }, { 4, 1, 7 }, { 4, 2, 6 }, { 4, 3, 5 },
            { 5, 0, 5 }, { 5, 1, 8 }, { 5, 2, 7 }, { 5, 3, 6 }, { 5, 4, 8 }, { 6, 0, 5 },
            { 6, 1, 8 }, { 6, 2, 7 }, { 6, 3, 6 }, { 6, 4, 8 }, { 6, 5, 7 }, { 7, 0, 5 },
            { 7, 1, 7 }, { 7, 2, 6 }, { 7, 3, 5 }, { 7, 4, 7 }, { 7, 5, 6 }, { 7, 6, 8 },
            { 8, 0, 5 }, { 8, 1, 6 }, { 8, 2, 5 }, { 8, 3, 4 }, { 8, 4, 6 }, { 8, 5, 5 },
            { 8, 6, 8 }, { 8, 7, 7 }, { 9, 0, 5 }, { 9, 1, 5 }, { 9, 2, 4 }, { 9, 3, 3 },
            { 9, 4, 5 }, { 9, 5, 4 }, { 9, 6, 8 }, { 9, 7, 7 }, { 9, 8, 6 } };
        testOnInstance(edges, 52);
    }

    private void testOnInstance(int[][] edges, double optWeight)
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1], edge[2]);
        }
        ChristofidesThreeHalvesApproxMetricTSP<Integer, DefaultWeightedEdge> approxMetricTSP =
            new ChristofidesThreeHalvesApproxMetricTSP<>();
        GraphPath<Integer, DefaultWeightedEdge> path = approxMetricTSP.getTour(graph);
        assertHamiltonian(graph, path);
        assertTrue(path.getWeight() <= 1.5 * optWeight);
    }
}
