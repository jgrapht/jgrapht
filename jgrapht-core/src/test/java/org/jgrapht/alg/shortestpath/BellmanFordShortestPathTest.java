/*
 * (C) Copyright 2006-2018, by John V Sichi and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class BellmanFordShortestPathTest
    extends
    ShortestPathTestCase
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testUndirected()
    {
        SingleSourcePaths<String, DefaultWeightedEdge> tree;
        Graph<String, DefaultWeightedEdge> g = create();

        tree = new BellmanFordShortestPath<>(g).getPaths(V3);

        // find best path
        assertEquals(
            Arrays.asList(new DefaultWeightedEdge[] { e13, e12, e24, e45 }),
            tree.getPath(V5).getEdgeList());
        assertEquals(3.0, tree.getPath(V1).getWeight(), 1e-9);
        assertEquals(5.0, tree.getPath(V2).getWeight(), 1e-9);
        assertEquals(0.0, tree.getPath(V3).getWeight(), 1e-9);
        assertEquals(10.0, tree.getPath(V4).getWeight(), 1e-9);
        assertEquals(15.0, tree.getPath(V5).getWeight(), 1e-9);
    }

    @Override
    protected List<DefaultWeightedEdge> findPathBetween(
        Graph<String, DefaultWeightedEdge> g, String src, String dest)
    {
        return new BellmanFordShortestPath<>(g).getPaths(src).getPath(dest).getEdgeList();
    }

    @Test
    public void testWithNegativeEdges()
    {
        Graph<String, DefaultWeightedEdge> g = createWithBias(true);

        List<DefaultWeightedEdge> path;

        path = findPathBetween(g, V1, V4);
        assertEquals(Arrays.asList(e13, e34), path);

        path = findPathBetween(g, V1, V5);
        assertEquals(Arrays.asList(e15), path);
    }

    @Test
    public void testNoPath()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("a");
        g.addVertex("b");

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        SingleSourcePaths<String, DefaultWeightedEdge> paths = alg.getPaths("a");
        assertEquals(paths.getWeight("b"), Double.POSITIVE_INFINITY, 0);
        assertNull(paths.getPath("b"));
    }

    @Test
    public void testWikipediaExampleBellmanFord()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.addVertex("z");
        g.addVertex("s");
        g.setEdgeWeight(g.addEdge("w", "z"), 2);
        g.setEdgeWeight(g.addEdge("y", "w"), 4);
        g.setEdgeWeight(g.addEdge("x", "w"), 6);
        g.setEdgeWeight(g.addEdge("x", "y"), 3);
        g.setEdgeWeight(g.addEdge("z", "x"), -7);
        g.setEdgeWeight(g.addEdge("y", "z"), 5);
        g.setEdgeWeight(g.addEdge("z", "y"), -3);
        g.setEdgeWeight(g.addEdge("s", "w"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "y"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "x"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "z"), 0.0);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        SingleSourcePaths<String, DefaultWeightedEdge> paths = alg.getPaths("s");
        assertEquals(0d, paths.getPath("s").getWeight(), 1e-9);
        assertEquals(-1d, paths.getPath("w").getWeight(), 1e-9);
        assertEquals(-4d, paths.getPath("y").getWeight(), 1e-9);
        assertEquals(-7d, paths.getPath("x").getWeight(), 1e-9);
        assertEquals(0d, paths.getPath("z").getWeight(), 1e-9);
    }

    @Test
    public void testNegativeCycleDetection()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.addVertex("z");
        g.addVertex("s");
        g.setEdgeWeight(g.addEdge("w", "z"), 2);
        g.setEdgeWeight(g.addEdge("y", "w"), 4);
        g.setEdgeWeight(g.addEdge("x", "w"), 6);
        g.setEdgeWeight(g.addEdge("x", "y"), 3);
        g.setEdgeWeight(g.addEdge("z", "x"), -7);
        g.setEdgeWeight(g.addEdge("y", "z"), 3);
        g.setEdgeWeight(g.addEdge("z", "y"), -3);
        g.setEdgeWeight(g.addEdge("s", "w"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "y"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "x"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "z"), 0.0);

        try {
            new BellmanFordShortestPath<>(g).getPaths("s");
            fail("Negative-weight cycle not detected");
        } catch (RuntimeException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());
        }
    }

    @Test
    public void testNegativeCycleDetectionActualCycle()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.addVertex("z");
        g.addVertex("s");
        g.setEdgeWeight(g.addEdge("w", "z"), 2);
        g.setEdgeWeight(g.addEdge("y", "w"), 4);
        g.setEdgeWeight(g.addEdge("x", "w"), 6);
        g.setEdgeWeight(g.addEdge("x", "y"), 3);
        g.setEdgeWeight(g.addEdge("z", "x"), -7);
        g.setEdgeWeight(g.addEdge("y", "z"), 3);
        g.setEdgeWeight(g.addEdge("z", "y"), -3);
        g.setEdgeWeight(g.addEdge("s", "w"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "y"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "x"), 0.0);
        g.setEdgeWeight(g.addEdge("s", "z"), 0.0);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        try {
            alg.getPaths("s");
            fail("Negative-weight cycle not detected");
        } catch (NegativeCycleDetectedException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());

            @SuppressWarnings("unchecked") GraphPath<String, DefaultWeightedEdge> cycle =
                (GraphPath<String, DefaultWeightedEdge>) e.getCycle();
            assertEquals("x", cycle.getStartVertex());
            assertEquals("x", cycle.getEndVertex());
            assertEquals(-1.0d, cycle.getWeight(), 1e-9);
            assertEquals(3, cycle.getLength());
        }
    }

    @Test
    public void testNegativeEdgeUndirectedGraph()
    {
        WeightedPseudograph<String, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.setEdgeWeight(g.addEdge("w", "y"), 1);
        g.setEdgeWeight(g.addEdge("y", "x"), 1);
        g.setEdgeWeight(g.addEdge("y", "x"), -1);
        try {
            new BellmanFordShortestPath<>(g).getPaths("w");
            fail("Negative-weight cycle not detected");
        } catch (RuntimeException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());
        }
    }

    @Test
    public void testNegativeEdgeUndirectedGraphActualCycle()
    {
        WeightedPseudograph<String, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.setEdgeWeight(g.addEdge("w", "y"), 1);
        g.setEdgeWeight(g.addEdge("y", "x"), 1);
        g.setEdgeWeight(g.addEdge("y", "x"), -1);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        try {
            alg.getPaths("w");
            fail("Negative-weight cycle not detected");
        } catch (NegativeCycleDetectedException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());

            @SuppressWarnings("unchecked") GraphPath<String, DefaultWeightedEdge> cycle =
                (GraphPath<String, DefaultWeightedEdge>) e.getCycle();
            assertEquals("x", cycle.getStartVertex());
            assertEquals("x", cycle.getEndVertex());
            assertEquals(-2.0d, cycle.getWeight(), 1e-9);
            assertEquals(2, cycle.getLength());
        }
    }

    @Test
    public void testDoNotDetectNonReachableNegativeCycle()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");
        g.addVertex("6");
        g.addVertex("7");
        g.setEdgeWeight(g.addEdge("1", "2"), 1);
        g.setEdgeWeight(g.addEdge("2", "3"), 1);
        g.setEdgeWeight(g.addEdge("3", "4"), 1);

        g.setEdgeWeight(g.addEdge("5", "4"), 1);
        g.setEdgeWeight(g.addEdge("5", "6"), -1);
        g.setEdgeWeight(g.addEdge("6", "7"), -1);
        g.setEdgeWeight(g.addEdge("7", "5"), -1);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        alg.getPaths("1");
    }

    @Test
    public void testNegativeCycle()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");
        g.addVertex("6");
        g.addVertex("7");
        g.addVertex("8");
        g.addVertex("9");
        g.addVertex("x");

        g.setEdgeWeight(g.addEdge("1", "2"), 1);
        g.setEdgeWeight(g.addEdge("2", "3"), 1);
        g.setEdgeWeight(g.addEdge("3", "4"), 1);
        g.setEdgeWeight(g.addEdge("4", "5"), 1);
        g.setEdgeWeight(g.addEdge("5", "6"), 1);
        g.setEdgeWeight(g.addEdge("6", "7"), 1);
        g.setEdgeWeight(g.addEdge("7", "8"), 1);
        g.setEdgeWeight(g.addEdge("8", "9"), 1);

        g.setEdgeWeight(g.addEdge("7", "x"), -3);
        g.setEdgeWeight(g.addEdge("x", "4"), -3);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg = new BellmanFordShortestPath<>(g);
        try {
            alg.getPaths("1");
            fail("Negative-weight cycle not detected");
        } catch (NegativeCycleDetectedException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());

            @SuppressWarnings("unchecked") GraphPath<String, DefaultWeightedEdge> cycle =
                (GraphPath<String, DefaultWeightedEdge>) e.getCycle();

            assertEquals("6", cycle.getStartVertex());
            assertEquals("6", cycle.getEndVertex());
            assertEquals(-3.0d, cycle.getWeight(), 1e-9);
            assertEquals(5, cycle.getLength());
        }
    }

    @Test
    public void testNegativeCycleWithMaxHops()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");

        g.setEdgeWeight(g.addEdge("1", "2"), 1);
        g.setEdgeWeight(g.addEdge("2", "3"), 1);
        g.setEdgeWeight(g.addEdge("3", "4"), 1);
        g.setEdgeWeight(g.addEdge("4", "1"), -5);

        int maxHops = 3;
        BellmanFordShortestPath<String, DefaultWeightedEdge> alg =
            new BellmanFordShortestPath<>(g, 1e-16, maxHops);
        GraphPath<String, DefaultWeightedEdge> path1 = alg.getPaths("1").getPath("3");
        assertEquals(2.0d, path1.getWeight(), 1e-9);

        BellmanFordShortestPath<String, DefaultWeightedEdge> alg1 =
            new BellmanFordShortestPath<>(g, 1e-16, maxHops + 1);
        try {
            alg1.getPaths("1");
            fail("Negative-weight cycle not detected");
        } catch (NegativeCycleDetectedException e) {
            assertEquals("Graph contains a negative-weight cycle", e.getMessage());

            @SuppressWarnings("unchecked") GraphPath<String, DefaultWeightedEdge> cycle =
                (GraphPath<String, DefaultWeightedEdge>) e.getCycle();

            assertEquals("1", cycle.getStartVertex());
            assertEquals("1", cycle.getEndVertex());
            assertEquals(-2.0d, cycle.getWeight(), 1e-9);
            assertEquals(4, cycle.getLength());
        }
    }

}
