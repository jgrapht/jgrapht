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
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test case for {@link EppsteinPathsIterator} class.
 *
 * @author Semen Chudakov
 */
public class EppsteinPathsIteratorTest extends BaseEppsteinKShortestPathsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new EppsteinPathsIterator<>(graph, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new EppsteinPathsIterator<>(graph, 1, 2);
    }

    @Test
    public void testNoPathInGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoPathLeft() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void testSourceEqualsTarget() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        Integer source = 1;
        Integer target = 1;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);
        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 0.0, false);
    }

    @Test
    public void testNoSidetracksInGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge a = Graphs.addEdgeWithVertices(graph, 1, 2, 1.0);
        DefaultWeightedEdge b = Graphs.addEdgeWithVertices(graph, 2, 3, 1.0);
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, 1, 3);
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
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

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
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 13.0, true);
        performAssertion(graph, it, source, target, 15.0, true);
        performAssertion(graph, it, source, target, 21.0, false);
    }

    @Test
    public void testSimpleGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph3);
        Integer source = 5;
        Integer target = 4;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 8.0, true);
        performAssertion(graph, it, source, target, 16.0, true);
        performAssertion(graph, it, source, target, 19.0, true);
        performAssertion(graph, it, source, target, 19.0, true);
        performAssertion(graph, it, source, target, 22.0, true);
        performAssertion(graph, it, source, target, 23.0, true);
        performAssertion(graph, it, source, target, 24.0, true);
        performAssertion(graph, it, source, target, 25.0, true);
        performAssertion(graph, it, source, target, 25.0, true);
        performAssertion(graph, it, source, target, 26.0, true);
    }

    @Test
    public void testCyclicGraph1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Integer source = 1;
        Integer target = 2;
        readGraph(graph, cyclicGraph1);
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 1.0, true);
        performAssertion(graph, it, source, target, 3.0, true);
        performAssertion(graph, it, source, target, 5.0, true);
        performAssertion(graph, it, source, target, 7.0, true);
        performAssertion(graph, it, source, target, 9.0, true);
//         and so on
    }

    @Test
    public void testCyclicGraph2() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph2);
        Integer source = 1;
        Integer target = 6;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        for (int i = 0; i < 2; i++) {
            performAssertion(graph, it, source, target, 4.0, true);
        }

        for (int i = 0; i < 4; i++) {
            performAssertion(graph, it, source, target, 8.0, true);
        }

        for (int i = 0; i < 12; i++) {
            performAssertion(graph, it, source, target, 12.0, true);
        }
//         and so on
    }

    @Test
    public void testCyclicGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        Integer source = 1;
        Integer target = 3;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it =
                new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 2.0, true);
        performAssertion(graph, it, source, target, 4.0, true);
        performAssertion(graph, it, source, target, 6.0, true);
        performAssertion(graph, it, source, target, 6.0, true);
        performAssertion(graph, it, source, target, 8.0, true);
        performAssertion(graph, it, source, target, 8.0, true);
//         and so on
    }

    @Test
    public void testRestHeapGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, restHeapGraph);
        Integer source = 1;
        Integer target = 10;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it
                = new EppsteinPathsIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        performAssertion(graph, it, source, target, 3.0, true);
        performAssertion(graph, it, source, target, 4.0, true);
        performAssertion(graph, it, source, target, 5.0, true);
        performAssertion(graph, it, source, target, 6.0, true);
        performAssertion(graph, it, source, target, 7.0, true);
        performAssertion(graph, it, source, target, 8.0, true);
        performAssertion(graph, it, source, target, 9.0, true);
        performAssertion(graph, it, source, target, 10.0, false);
    }

    @Test
    public void testNotShortestPathEdgesGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, notShortestPathEdgesGraph);
        Integer source = 1;
        Integer target = 2;
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it
                = new EppsteinPathsIterator<>(graph, source, target);

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
        EppsteinPathsIterator<Integer, DefaultWeightedEdge> it
                = new EppsteinPathsIterator<>(graph, source, target);
        if (it.hasNext()) {
            GraphPath<Integer, DefaultWeightedEdge> path = it.next();
            double weight = path.getWeight();
            assertCorrectPath(graph, path, source, target);

            for (int i = 0; i < 10 && it.hasNext(); i++) {
                path = it.next();
                assertCorrectPath(graph, path, source, target);
                assertTrue(weight <= path.getWeight());
                weight = path.getWeight();
            }
        }
    }

    private void performAssertion(Graph<Integer, DefaultWeightedEdge> graph,
                                  EppsteinPathsIterator<Integer, DefaultWeightedEdge> it,
                                  Integer source, Integer target, double expectedWeight, boolean hasNext) {
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(expectedWeight, path.getWeight(), 1e-9);
        assertCorrectPath(graph, path, source, target);
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


    private void getRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p) {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator
                = new GnpRandomGraphGenerator<>(n, p);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, (int) (Math.random() * 10)));
    }
}
