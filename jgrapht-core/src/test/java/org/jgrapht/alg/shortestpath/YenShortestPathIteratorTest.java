/*
 * (C) Copyright 2019-2019, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for the {@link YenShortestPathIterator}.
 */
public class YenShortestPathIteratorTest extends BaseKShortestPathTest {

    /**
     * Seed value which is used to generate random graphs
     * by {@code getRandomGraph(Graph, int, double)} method.
     */
    private static final long SEED = 13l;
    /**
     * Number of path to iterate over for each random graph
     * in the {@code testOnRandomGraph(Graph, Integer, Integer)}
     * method.
     */
    private static final int NUMBER_OF_PATH_TO_ITERATE = 10;


    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new YenShortestPathIterator<>(graph, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new YenShortestPathIterator<>(graph, 1, 2);
    }

    @Test
    public void testNoPathInGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoPathLeft() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void testSourceEqualsTarget() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        Integer source = 1;
        Integer target = 1;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);
        assertTrue(it.hasNext());
        verifyNextPath(it, 0.0, false);
    }

    @Test
    public void testOnlyShortestPathGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge a = Graphs.addEdgeWithVertices(graph, 1, 2, 1.0);
        DefaultWeightedEdge b = Graphs.addEdgeWithVertices(graph, 2, 3, 1.0);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, 1, 3);
        assertTrue(it.hasNext());
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(2.0, path.getWeight(), 1e-9);
        assertEquals(Arrays.asList(a, b), path.getEdgeList());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSimpleGraph1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph1);
        Integer source = 1;
        Integer target = 12;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

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
    public void testSimpleGraph2() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph2);
        Integer source = 1;
        Integer target = 4;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 13.0, true);
        verifyNextPath(it, 15.0, true);
        verifyNextPath(it, 21.0, false);
    }

    @Test
    public void testSimpleGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph3);
        Integer source = 1;
        Integer target = 4;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 9.0, true);
        verifyNextPath(it, 13.0, true);
        verifyNextPath(it, 15.0, false);
    }

    @Test
    public void testSimpleGraph4() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph4);
        Integer source = 1;
        Integer target = 3;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 13.0, true);
        verifyNextPath(it, 15.0, true);
        verifyNextPath(it, 21.0, false);
    }

    @Test
    public void testCyclicGraph1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Integer source = 1;
        Integer target = 2;
        readGraph(graph, cyclicGraph1);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, false);
    }

    @Test
    public void testCyclicGraph2() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph2);
        Integer source = 1;
        Integer target = 6;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 4.0, false);
    }

    @Test
    public void testCyclicGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        Integer source = 1;
        Integer target = 3;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 2.0, false);
    }


    @Test
    public void testPseudoGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        readGraph(graph, multigraph);
        Integer source = 1;
        Integer target = 5;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
                new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 7.0, true);
        verifyNextPath(it, 10.0, false);
    }

    @Test
    public void testNotShortestPathEdgesGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, notShortestPathEdgesGraph);
        Integer source = 1;
        Integer target = 2;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it
                = new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, false);
    }

    @Test
    public void testOnRandomGraphs() {
        int n = 100;
        double p = 0.5;
        for (int i = 0; i < 1000; i++) {
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph
                    = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            getRandomGraph(graph, n, p);
            Integer source = (int) (Math.random() * n);
            Integer target = (int) (Math.random() * n);
            testOnRandomGraph(graph, source, target);
        }
    }

    /**
     * If the overall number of paths between {@code source}
     * and {@code target} is denoted by $n$ and the value of
     * {@code #NUMBER_OF_PATH_TO_ITERATE} is denoted by $m$
     * then the method iterates over $p = min\{n, m\}$ such paths
     * and verifies that they are built correctly. Additionally
     * method checks that are returned in the increasing order by
     * weight.
     *
     * @param graph  graph the iterator is being tested on
     * @param source source vertex
     * @param target target vertex
     */
    private void testOnRandomGraph(Graph<Integer, DefaultWeightedEdge> graph,
                                   Integer source, Integer target) {
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it
                = new YenShortestPathIterator<>(graph, source, target);
        GraphPath<Integer, DefaultWeightedEdge> path;
        double previousPathWeight = 0.0;
        Set<GraphPath<Integer, DefaultWeightedEdge>> paths = new HashSet<>();
        int i = 0;
        for (; i < NUMBER_OF_PATH_TO_ITERATE && it.hasNext(); i++) {
            path = it.next();
            paths.add(path);
            ((GraphWalk<Integer, DefaultWeightedEdge>) path).verify();
            assertTrue(previousPathWeight <= path.getWeight());
            previousPathWeight = path.getWeight();
        }
        assertEquals(i, paths.size());
    }

    /**
     * Performs assertions to check correctness of the next
     * path which the {@code it} is expected to return.
     *
     * @param it             shortest paths iterator
     * @param expectedWeight expected weight of the next path
     * @param hasNext        expected return value of the
     *                       {@link YenShortestPathIterator#hasNext()} method
     */
    private void verifyNextPath(YenShortestPathIterator<Integer, DefaultWeightedEdge> it,
                                double expectedWeight, boolean hasNext) {
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(expectedWeight, path.getWeight(), 1e-9);
        ((GraphWalk<Integer, DefaultWeightedEdge>) path).verify();
        assertLooplessPath(path);
        assertEquals(it.hasNext(), hasNext);
    }

    /**
     * Asserts that {@code path} is loopless. More formally
     * checks that the {@code path} has no duplicate vertices.
     */
    private void assertLooplessPath(GraphPath<Integer, DefaultWeightedEdge> path) {
        Set<Integer> uniqueVertices = new HashSet<>(path.getVertexList());
        assertEquals(path.getVertexList().size(), uniqueVertices.size());
    }

    /**
     * Generates random graph from the $G(n, p)$ model.
     *
     * @param graph graph instance for the generator
     * @param n     the number of nodes
     * @param p     the edge probability
     */
    private void getRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p) {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator
                = new GnpRandomGraphGenerator<>(n, p, SEED);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, Math.random()));
    }
}
