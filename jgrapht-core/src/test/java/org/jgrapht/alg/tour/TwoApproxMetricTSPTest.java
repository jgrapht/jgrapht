/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dimitrios Michail
 */
public class TwoApproxMetricTSPTest
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
            new TwoApproxMetricTSP<String, DefaultWeightedEdge>().getTour(g);
        assertHamiltonian(g, tour);
        assertTrue(
            2 * new KruskalMinimumSpanningTree<>(g).getSpanningTree().getWeight() >= tour
                .getWeight());
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
                new TwoApproxMetricTSP<Object, DefaultEdge>().getTour(g);
            assertHamiltonian(g, tour);

            double mstWeight = new KruskalMinimumSpanningTree<>(g).getSpanningTree().getWeight();
            double tourWeight = tour.getWeight();
            assertTrue(2 * mstWeight >= tourWeight);
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
            new TwoApproxMetricTSP<String, DefaultWeightedEdge>().getTour(g);
        assertHamiltonian(g, tour);

        double mstWeight = new KruskalMinimumSpanningTree<>(g).getSpanningTree().getWeight();
        double tourWeight = tour.getWeight();
        assertTrue(2 * mstWeight >= tourWeight);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstanceDirected()
    {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex("A");

        new TwoApproxMetricTSP<String, DefaultEdge>().getTour(g);
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

        new TwoApproxMetricTSP<String, DefaultWeightedEdge>().getTour(g);
    }

    static <V, E> void assertHamiltonian(Graph<V, E> g, GraphPath<V, E> path)
    {
        List<V> tourVertices = path.getVertexList();
        List<E> tourEdges = path.getEdgeList();

        // check tour length, beginning and end of the tour
        assertEquals(path.getStartVertex(), path.getEndVertex());
        assertEquals(path.getStartVertex(), tourVertices.get(0));
        if (g.vertexSet().size() == 1) {
            assertTrue(tourEdges.isEmpty());
            assertEquals(path.getWeight(), 0.0, 1e-9);
            return;
        }

        assertEquals(g.vertexSet().size(), tourVertices.size() - 1);
        assertEquals(g.vertexSet().size(), tourEdges.size());

        // check tour with edges
        double weight = 0d;
        V v = path.getStartVertex();
        Set<V> visited = new HashSet<>();
        for (E e : tourEdges) {
            v = Graphs.getOppositeVertex(g, e, v);
            assertTrue(visited.add(v));
            weight += g.getEdgeWeight(e);
        }
        assertEquals(path.getWeight(), weight, 1e-9);
        assertEquals(visited.size(), g.vertexSet().size());

        // check tour with vertices
        visited.clear();
        Iterator<V> vIt = tourVertices.iterator();
        V start = vIt.next();
        visited.add(start);
        while (vIt.hasNext()) {
            v = vIt.next();
            if (!vIt.hasNext()) {
                assertTrue(v.equals(start));
            } else {
                assertTrue(visited.add(v));
            }
        }
        assertEquals(visited.size(), g.vertexSet().size());
    }

}
