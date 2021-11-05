/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link EppsteinShortestPathIterator}.
 *
 * @author Semen Chudakov
 */
public class EppsteinShortestPathIteratorTest
{

    /**
     * Seed value which is used to generate random graphs by
     * {@code getRandomGraph(Graph, int, double)} method.
     */
    private static final long SEED = 13L;
    /**
     * Number of path to iterate over for each random graph in the
     * {@code testOnRandomGraph(Graph, Integer, Integer)} method.
     */
    private static final int NUMBER_OF_PATH_TO_ITERATE = 10;

    private final int[][] simpleGraph1 =
        { { 1, 2, 2 }, { 2, 3, 20 }, { 3, 4, 14 }, { 1, 5, 13 }, { 2, 6, 27 }, { 3, 7, 14 },
            { 4, 8, 15 }, { 5, 6, 9 }, { 6, 7, 10 }, { 7, 8, 25 }, { 5, 9, 15 }, { 6, 10, 20 },
            { 7, 11, 12 }, { 8, 12, 7 }, { 9, 10, 18 }, { 10, 11, 8 }, { 11, 12, 11 } };
    private final int[][] simpleGraph2 =
        { { 1, 2, 5 }, { 1, 3, 6 }, { 2, 3, 7 }, { 2, 4, 8 }, { 3, 4, 9 } };
    private final int[][] simpleGraph3 = { { 0, 1, 6 }, { 2, 0, 9 }, { 4, 0, 4 }, { 0, 5, 6 },
        { 0, 6, 5 }, { 2, 1, 1 }, { 1, 4, 9 }, { 4, 1, 2 }, { 1, 5, 7 }, { 1, 6, 5 }, { 2, 4, 1 },
        { 2, 5, 0 }, { 3, 4, 4 }, { 4, 3, 4 }, { 4, 5, 6 }, { 5, 4, 8 }, { 4, 6, 3 }, { 6, 5, 0 } };

    private final int[][] cyclicGraph1 = { { 1, 2, 1 }, { 2, 1, 1 } };

    private final int[][] cyclicGraph2 = { { 1, 2, 1 }, { 2, 3, 1 }, { 3, 4, 1 }, { 4, 1, 1 },
        { 1, 5, 2 }, { 5, 6, 2 }, { 6, 7, 2 }, { 7, 1, 2 }, { 3, 6, 2 }, { 6, 3, 2 } };

    private final int[][] cyclicGraph3 = { { 1, 2, 1 }, { 2, 3, 1 }, { 3, 4, 1 }, { 3, 4, 1 },
        { 4, 3, 1 }, { 4, 5, 1 }, { 5, 4, 1 } };

    private final int[][] restHeapGraph = { { 1, 2, 2 }, { 1, 3, 3 }, { 1, 4, 4 }, { 1, 5, 5 },
        { 1, 6, 6 }, { 1, 7, 7 }, { 1, 8, 8 }, { 1, 9, 9 }, { 2, 10, 1 }, { 3, 10, 1 },
        { 4, 10, 1 }, { 5, 10, 1 }, { 6, 10, 1 }, { 7, 10, 1 }, { 8, 10, 1 }, { 9, 10, 1 } };
    private final int[][] notShortestPathEdgesGraph = { { 1, 2, 1 }, { 1, 3, 3 }, { 1, 4, 4 },
        { 1, 5, 5 }, { 1, 6, 6 }, { 1, 7, 7 }, { 1, 8, 8 }, { 1, 9, 9 } };

    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new EppsteinShortestPathIterator<>(graph, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new EppsteinShortestPathIterator<>(graph, 1, 2);
    }

    @Test
    public void testNoPathInGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoPathLeft()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void testSourceEqualsTarget()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        Integer source = 1;
        Integer target = 1;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);
        assertTrue(it.hasNext());
        verifyNextPath(it, 0.0, false);
    }

    @Test
    public void testNoSidetracksInGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge a = Graphs.addEdgeWithVertices(graph, 1, 2, 1.0);
        DefaultWeightedEdge b = Graphs.addEdgeWithVertices(graph, 2, 3, 1.0);
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, 1, 3);
        assertTrue(it.hasNext());
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(2.0, path.getWeight(), 1e-9);
        assertEquals(Arrays.asList(a, b), path.getEdgeList());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSimpleGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph1);
        Integer source = 1;
        Integer target = 12;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 55.0, true);
        verifyNextPath(it, 58.0, true);
        verifyNextPath(it, 59.0, true);
        verifyNextPath(it, 61.0, true);
        verifyNextPath(it, 62.0, true);
        verifyNextPath(it, 64.0, true);
        verifyNextPath(it, 65.0, true);
        verifyNextPath(it, 68.0, true);
        verifyNextPath(it, 68.0, true);
        verifyNextPath(it, 71.0, false);
    }

    @Test
    public void testSimpleGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph2);
        Integer source = 1;
        Integer target = 4;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 13.0, true);
        verifyNextPath(it, 15.0, true);
        verifyNextPath(it, 21.0, false);
    }

    @Test
    public void testSimpleGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph3);
        Integer source = 5;
        Integer target = 4;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 8.0, true);
        verifyNextPath(it, 16.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 22.0, true);
        verifyNextPath(it, 23.0, true);
        verifyNextPath(it, 24.0, true);
        verifyNextPath(it, 25.0, true);
        verifyNextPath(it, 25.0, true);
        verifyNextPath(it, 26.0, true);
    }

    @Test
    public void testCyclicGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Integer source = 1;
        Integer target = 2;
        readGraph(graph, cyclicGraph1);
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, true);
        verifyNextPath(it, 3.0, true);
        verifyNextPath(it, 5.0, true);
        verifyNextPath(it, 7.0, true);
        verifyNextPath(it, 9.0, true);
        // and so on
    }

    @Test
    public void testCyclicGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph2);
        Integer source = 1;
        Integer target = 6;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        for (int i = 0; i < 2; i++) {
            verifyNextPath(it, 4.0, true);
        }

        for (int i = 0; i < 4; i++) {
            verifyNextPath(it, 8.0, true);
        }

        for (int i = 0; i < 12; i++) {
            verifyNextPath(it, 12.0, true);
        }
        // and so on
    }

    @Test
    public void testCyclicGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        Integer source = 1;
        Integer target = 3;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 2.0, true);
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 6.0, true);
        verifyNextPath(it, 6.0, true);
        verifyNextPath(it, 8.0, true);
        verifyNextPath(it, 8.0, true);
        // and so on
    }

    @Test
    public void testRestHeapGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, restHeapGraph);
        Integer source = 1;
        Integer target = 10;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 3.0, true);
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 5.0, true);
        verifyNextPath(it, 6.0, true);
        verifyNextPath(it, 7.0, true);
        verifyNextPath(it, 8.0, true);
        verifyNextPath(it, 9.0, true);
        verifyNextPath(it, 10.0, false);
    }

    @Test
    public void testNotShortestPathEdgesGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, notShortestPathEdgesGraph);
        Integer source = 1;
        Integer target = 2;
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, false);
    }

    @Test
    public void testOnRandomGraphs()
    {
        int n = 100;
        double p = 0.5;
        for (int i = 0; i < 1000; i++) {
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            getRandomGraph(graph, n, p);
            Integer source = (int) (Math.random() * n);
            Integer target = (int) (Math.random() * n);
            testOnRandomGraph(graph, source, target);
        }
    }

    /**
     * If the overall number of paths between {@code source} and {@code target} is denoted by $n$
     * and the value of {@code #NUMBER_OF_PATH_TO_ITERATE} is denoted by $m$ then the method
     * iterates over $p = min\{n, m\}$ such paths and verifies that they are built correctly.
     * Additionally method checks that are returned in the increasing order by weight.
     *
     * @param graph graph the iterator is being tested on
     * @param source source vertex
     * @param target target vertex
     */
    private void testOnRandomGraph(
        Graph<Integer, DefaultWeightedEdge> graph, Integer source, Integer target)
    {
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new EppsteinShortestPathIterator<>(graph, source, target);
        GraphPath<Integer, DefaultWeightedEdge> path;
        double weight = 0.0;
        Set<GraphPath<Integer, DefaultWeightedEdge>> paths = new HashSet<>();
        int i = 0;
        for (; i < NUMBER_OF_PATH_TO_ITERATE && it.hasNext(); i++) {
            path = it.next();
            paths.add(path);
            verifyPath(path);
            assertTrue(weight <= path.getWeight());
            weight = path.getWeight();
        }
        assertEquals(i, paths.size());
    }

    /**
     * Performs assertions to check correctness of the next path which the {@code it} is expected to
     * return.
     *
     * @param it shortest paths iterator
     * @param expectedWeight expected weight of the next path
     * @param hasNext expected return value of the {@link YenShortestPathIterator#hasNext()} method
     */
    private void verifyNextPath(
        EppsteinShortestPathIterator<Integer, DefaultWeightedEdge> it, double expectedWeight,
        boolean hasNext)
    {
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(expectedWeight, path.getWeight(), 1e-9);
        verifyPath(path);
        assertEquals(it.hasNext(), hasNext);
    }

    /**
     * Creates a graph walk of the give {@code graph} and verifies that the path is correct by
     * callig {@link GraphWalk#verify()}.
     *
     * @param path path to verify
     */
    private void verifyPath(GraphPath<Integer, DefaultWeightedEdge> path)
    {
        GraphWalk<Integer,
            DefaultWeightedEdge> walk = new GraphWalk<>(
                path.getGraph(), path.getStartVertex(), path.getEndVertex(), path.getVertexList(),
                path.getEdgeList(), path.getWeight());
        walk.verify();
    }

    /**
     * Generates random graph from the $G(n, p)$ model.
     *
     * @param graph graph instance for the generator
     * @param n the number of nodes
     * @param p the edge probability
     */
    private void getRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p)
    {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnpRandomGraphGenerator<>(n, p, SEED);
        generator.generateGraph(graph);
        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, (int) (Math.random() * 10)));
    }

    private void readGraph(Graph<Integer, DefaultWeightedEdge> graph, int[][] representation)
    {
        for (int[] ints : representation) {
            Graphs.addEdgeWithVertices(graph, ints[0], ints[1], ints[2]);
        }
    }
}
