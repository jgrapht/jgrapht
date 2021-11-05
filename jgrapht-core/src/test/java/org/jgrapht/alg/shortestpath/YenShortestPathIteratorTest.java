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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for the {@link YenShortestPathIterator}.
 */
public class YenShortestPathIteratorTest
    extends
    BaseKShortestPathTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new YenShortestPathIterator<>(graph, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new YenShortestPathIterator<>(graph, 1, 2);
    }

    @Test
    public void testNoPathInGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, 1, 2);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoPathLeft()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, 1, 2);
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
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);
        assertTrue(it.hasNext());
        verifyNextPath(it, 0.0, false);
    }

    @Test
    public void testOnlyShortestPathGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testSimpleGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testSimpleGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testSimpleGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testSimpleGraph4()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testCyclicGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Integer source = 1;
        Integer target = 2;
        readGraph(graph, cyclicGraph1);
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, false);
    }

    @Test
    public void testCyclicGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
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
    public void testCyclicGraph3()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        Integer source = 1;
        Integer target = 3;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 2.0, false);
    }

    @Test
    public void testPseudoGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        readGraph(graph, pseudograph1);
        Integer source = 1;
        Integer target = 5;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 2.0, true);
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 4.0, true);
        verifyNextPath(it, 5.0, true);
        verifyNextPath(it, 6.0, true);
        verifyNextPath(it, 7.0, true);
        verifyNextPath(it, 9.0, true);
        verifyNextPath(it, 10.0, true);
        verifyNextPath(it, 11.0, false);
    }

    @Test
    public void testPseudoGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        readGraph(graph, pseudograph2);
        Integer source = 2;
        Integer target = 3;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 6.0, true);
        verifyNextPath(it, 7.0, false);

        source = 1;
        target = 3;
        it = new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 8.0, true);
        verifyNextPath(it, 9.0, true);
        verifyNextPath(it, 9.0, true);
        verifyNextPath(it, 10.0, true);
        verifyNextPath(it, 10.0, true);
        verifyNextPath(it, 11.0, false);

        source = 1;
        target = 4;
        it = new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 17.0, true);
        verifyNextPath(it, 18.0, true);
        verifyNextPath(it, 18.0, true);
        verifyNextPath(it, 18.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 19.0, true);
        verifyNextPath(it, 20.0, true);
        verifyNextPath(it, 20.0, true);
        verifyNextPath(it, 20.0, true);
        verifyNextPath(it, 21.0, false);
    }

    @Test
    public void testPseudoGraph3()
    {
        Graph<String, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);

        graph.addVertex("19");
        graph.addVertex("1e");
        graph.addVertex("1c");
        graph.addVertex("1b");
        graph.addVertex("1d");
        graph.addVertex("1f");
        graph.addVertex("16");
        graph.addVertex("17");
        graph.addVertex("12");
        graph.addVertex("14");
        graph.addVertex("18");
        graph.addVertex("15");
        graph.addVertex("21");

        DefaultEdge e1 = graph.addEdge("19", "1e");
        graph.addEdge("19", "1c");
        graph.addEdge("19", "1b");
        graph.addEdge("19", "1d");
        DefaultEdge e5 = graph.addEdge("19", "1f");
        DefaultEdge e6 = graph.addEdge("19", "16");
        graph.addEdge("12", "17");
        graph.addEdge("12", "14");
        graph.addEdge("12", "15");
        DefaultEdge e10 = graph.addEdge("12", "16");
        DefaultEdge e11 = graph.addEdge("12", "16");
        DefaultEdge e12 = graph.addEdge("12", "18");
        DefaultEdge e13 = graph.addEdge("12", "21");
        DefaultEdge e14 = graph.addEdge("21", "1f");

        KShortestPathAlgorithm<String, DefaultEdge> yen = new YenKShortestPath<>(graph);

        // should contain exactly 3 elements each
        List<GraphPath<String, DefaultEdge>> yenPaths = yen.getPaths("1e", "18", 7);

        List<DefaultEdge> expectedEdgeList1 = Arrays.asList(e1, e6, e10, e12);
        List<DefaultEdge> expectedEdgeList2 = Arrays.asList(e1, e6, e11, e12);
        List<DefaultEdge> expectedEdgeList3 = Arrays.asList(e1, e5, e14, e13, e12);

        boolean option1 = yenPaths.get(0).getEdgeList().equals(expectedEdgeList1)
            && yenPaths.get(1).getEdgeList().equals(expectedEdgeList2);
        boolean option2 = yenPaths.get(0).getEdgeList().equals(expectedEdgeList2)
            && yenPaths.get(1).getEdgeList().equals(expectedEdgeList1);

        assertEquals(3, yenPaths.size());

        assertTrue(option1 ^ option2);
        assertEquals(expectedEdgeList3, yenPaths.get(2).getEdgeList());
    }

    @Test
    public void testNotShortestPathEdgesGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, notShortestPathEdgesGraph);
        Integer source = 1;
        Integer target = 2;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it =
            new YenShortestPathIterator<>(graph, source, target);

        assertTrue(it.hasNext());
        verifyNextPath(it, 1.0, false);
    }

    @Test
    public void testForbidAll()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        readGraph(graph, pseudograph1);
        Integer source = 1;
        Integer target = 5;
        YenShortestPathIterator<Integer, DefaultWeightedEdge> iterator =
            new YenShortestPathIterator<>(graph, source, target, (partialPath, edge) -> false);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNonTrivialPathValidator()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        readGraph(graph, pseudograph3);
        Integer source = 1;
        Integer target = 3;
        PathValidator<Integer, DefaultWeightedEdge> validator = (partialPath, edge) -> {
            if (graph.getEdgeSource(edge).equals(1) && graph.getEdgeTarget(edge).equals(2)
                && graph.getEdgeWeight(edge) == 2.0)
            {
                return false;
            }
            if (graph.getEdgeSource(edge).equals(2) && graph.getEdgeTarget(edge).equals(3)
                && graph.getEdgeWeight(edge) == 4.0)
            {
                return false;
            }
            return true;
        };
        YenShortestPathIterator<Integer, DefaultWeightedEdge> iterator =
            new YenShortestPathIterator<>(graph, source, target, validator);
        verifyNextPath(iterator, 6.0, true);
        verifyNextPath(iterator, 8.0, false);
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
        YenShortestPathIterator<Integer, DefaultWeightedEdge> it, double expectedWeight,
        boolean hasNext)
    {
        GraphPath<Integer, DefaultWeightedEdge> path = it.next();
        assertEquals(expectedWeight, path.getWeight(), 1e-9);
        ((GraphWalk<Integer, DefaultWeightedEdge>) path).verify();
        assertLooplessPath(path);
        assertEquals(it.hasNext(), hasNext);
    }

    /**
     * Asserts that {@code path} is loopless. More formally checks that the {@code path} has no
     * duplicate vertices.
     */
    private void assertLooplessPath(GraphPath<Integer, DefaultWeightedEdge> path)
    {
        Set<Integer> uniqueVertices = new HashSet<>(path.getVertexList());
        assertEquals(path.getVertexList().size(), uniqueVertices.size());
    }
}
