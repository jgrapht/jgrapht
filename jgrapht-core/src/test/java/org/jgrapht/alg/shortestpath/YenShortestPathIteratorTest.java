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
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test case for the {@link YenShortestPathIterator}.
 */
public class YenShortestPathIteratorTest extends BaseKShortestPathTest {

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
        performAssertion(graph, it, source, target, 0.0, false);
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
        performAssertion(graph, it, source, target, 55.0, true);
        performAssertion(graph, it, source, target, 58.0, true);
        performAssertion(graph, it, source, target, 59.0, true);
        performAssertion(graph, it, source, target, 61.0, true);
        performAssertion(graph, it, source, target, 62.0, true);
        performAssertion(graph, it, source, target, 64.0, true);
        performAssertion(graph, it, source, target, 65.0, true);
        performAssertion(graph, it, source, target, 68.0, true);
        performAssertion(graph, it, source, target, 68.0, true);
        performAssertion(graph, it, source, target, 71.0, false);
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
        performAssertion(graph, it, source, target, 13.0, true);
        performAssertion(graph, it, source, target, 15.0, true);
        performAssertion(graph, it, source, target, 21.0, false);
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
        performAssertion(graph, it, source, target, 9.0, true);
        performAssertion(graph, it, source, target, 13.0, true);
        performAssertion(graph, it, source, target, 15.0, false);
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
        performAssertion(graph, it, source, target, 13.0, true);
        performAssertion(graph, it, source, target, 15.0, true);
        performAssertion(graph, it, source, target, 21.0, false);
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
        performAssertion(graph, it, source, target, 1.0, false);
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
        performAssertion(graph, it, source, target, 4.0, true);
        performAssertion(graph, it, source, target, 4.0, false);
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
        performAssertion(graph, it, source, target, 2.0, false);
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
        performAssertion(graph, it, source, target, 4.0, true);
        performAssertion(graph, it, source, target, 7.0, true);
        performAssertion(graph, it, source, target, 10.0, false);
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
        performAssertion(graph, it, source, target, 1.0, false);
    }

    @Test
    public void testRandomGraphs() {
        int n = 100;
        double p = 0.5;
        for (int i = 0; i < 1000; i++) {
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph
                    = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            getRandomGraph(graph, n, p);
            Integer source = (int) (Math.random() * n);
            Integer target = (int) (Math.random() * n);
            assertCorrectness(graph, source, target);
        }
    }


    private void assertCorrectness(Graph<Integer, DefaultWeightedEdge> graph,
                                   Integer source, Integer target) {
        int amountOfPathsToIterate = 10;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it
                = new YenShortestPathIterator<>(graph, source, target);
        GraphPath<Integer, DefaultWeightedEdge> path;
        double previousPathWeight = 0.0;
        Set<GraphPath<Integer, DefaultWeightedEdge>> paths = new HashSet<>();
        int i = 0;
        for (; i < amountOfPathsToIterate && it.hasNext(); i++) {
            path = it.next();
            paths.add(path);
            assertCorrectPath(graph, path, source, target);
            assertTrue(previousPathWeight <= path.getWeight());
            previousPathWeight = path.getWeight();
        }
        assertEquals(i, paths.size());
    }

    private void performAssertion(Graph<Integer, DefaultWeightedEdge> graph,
                                  YenShortestPathIterator<Integer, DefaultWeightedEdge> it,
                                  Integer source, Integer target, double expectedWeight, boolean hasNext) {
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(expectedWeight, path.getWeight(), 1e-9);
        assertCorrectPath(graph, path, source, target);
        assertLooplessPath(path);
        assertEquals(it.hasNext(), hasNext);
    }

    private void assertCorrectPath(Graph<Integer, DefaultWeightedEdge> graph,
                                   GraphPath<Integer, DefaultWeightedEdge> path,
                                   Integer source, Integer target) {
        List<DefaultWeightedEdge> edgeList = path.getEdgeList();

        double expectedWeight = path.getWeight();
        double actualWeight = edgeList.stream().mapToDouble(graph::getEdgeWeight).sum();
        assertEquals(expectedWeight, actualWeight, 1e-9);

        if (edgeList.size() == 0) {
            assertEquals(source, target);
        } else {
            assertEquals(graph.getEdgeSource(edgeList.get(0)), source);
            assertEquals(graph.getEdgeTarget(edgeList.get(edgeList.size() - 1)), target);

            if (edgeList.size() >= 2) {
                Iterator<DefaultWeightedEdge> it = edgeList.iterator();
                DefaultWeightedEdge curr = it.next();
                DefaultWeightedEdge next;
                while (it.hasNext()) {
                    next = it.next();
                    assertEquals(graph.getEdgeTarget(curr), graph.getEdgeSource(next));
                    curr = next;
                }
            }
        }
    }

    private void assertLooplessPath(GraphPath<Integer, DefaultWeightedEdge> path) {
        Set<Integer> uniqueVertices = new HashSet<>(path.getVertexList());
        assertEquals(path.getVertexList().size(), uniqueVertices.size());
    }

    private void getRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p) {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator
                = new GnpRandomGraphGenerator<>(n, p);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, Math.random()));
    }
}
