/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.spanning.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link TwoOptHeuristicTSP}.
 * 
 * @author Dimitrios Michail
 */
@Category(SlowTests.class)
public class TwoOptHeuristicTSPTest
{

    @Test
    public void testWikiExampleSymmetric4Cities()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
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

        GraphPath<String, DefaultWeightedEdge> tour =
            new TwoOptHeuristicTSP<String, DefaultWeightedEdge>().getTour(g);
        assertHamiltonian(g, tour);
    }

    @Test
    public void testComplete()
    {
        final int maxSize = 50;

        for (int i = 1; i < maxSize; i++) {
            SimpleGraph<Object, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            CompleteGraphGenerator<Object, DefaultEdge> generator = new CompleteGraphGenerator<>(i);
            generator.generateGraph(g);

            GraphPath<Object, DefaultEdge> tour =
                new TwoOptHeuristicTSP<Object, DefaultEdge>().getTour(g);
            assertHamiltonian(g, tour);
        }
    }

    @Test
    public void testStar()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");
        g.addVertex("6");

        g.setEdgeWeight(g.addEdge("1", "2"), 1d);
        g.setEdgeWeight(g.addEdge("1", "3"), 1d);
        g.setEdgeWeight(g.addEdge("1", "4"), 1d);
        g.setEdgeWeight(g.addEdge("1", "5"), 2d);
        g.setEdgeWeight(g.addEdge("1", "6"), 2d);

        g.setEdgeWeight(g.addEdge("2", "3"), 2d);
        g.setEdgeWeight(g.addEdge("2", "4"), 1d);
        g.setEdgeWeight(g.addEdge("2", "5"), 1d);
        g.setEdgeWeight(g.addEdge("2", "6"), 2d);

        g.setEdgeWeight(g.addEdge("3", "4"), 1d);
        g.setEdgeWeight(g.addEdge("3", "5"), 2d);
        g.setEdgeWeight(g.addEdge("3", "6"), 1d);

        g.setEdgeWeight(g.addEdge("4", "5"), 1d);
        g.setEdgeWeight(g.addEdge("4", "6"), 1d);

        g.setEdgeWeight(g.addEdge("5", "6"), 1d);

        GraphPath<String, DefaultWeightedEdge> tour =
            new TwoOptHeuristicTSP<String, DefaultWeightedEdge>().getTour(g);
        assertHamiltonian(g, tour);

        double mstWeight = new KruskalMinimumSpanningTree<>(g).getSpanningTree().getWeight();
        double tourWeight = tour.getWeight();
        assertTrue(2 * mstWeight >= tourWeight);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstanceDirected()
    {
        new TwoOptHeuristicTSP<String, DefaultEdge>()
            .getTour(new SimpleDirectedGraph<>(DefaultEdge.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstanceNotComplete()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.setEdgeWeight(g.addEdge("A", "B"), 20d);
        g.setEdgeWeight(g.addEdge("A", "C"), 42d);

        new TwoOptHeuristicTSP<String, DefaultWeightedEdge>().getTour(g);
    }

}
