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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
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

    /**
     * Seed value which is used to generate random graphs by
     * {@code getRandomGraph(Graph, int, double)} method.
     */
    private static final long SEED = 13l;
    /**
     * Number of path to iterate over for each random graph in the
     * {@code testOnRandomGraph(Graph, Integer, Integer)} method.
     */
    private static final int NUMBER_OF_PATH_TO_ITERATE = 100;

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
    public void testPseudoGraph3() {
        Graph<String,DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);

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

        graph.addEdge("19","1e");
        graph.addEdge("19","1c");
        graph.addEdge("19","1b");
        graph.addEdge("19","1d");
        graph.addEdge("19","1f");
        graph.addEdge("19","16");
        graph.addEdge("12","17");
        graph.addEdge("12","14");
        graph.addEdge("12","15");
        graph.addEdge("12","16");
        graph.addEdge("12","16");
        graph.addEdge("12","18");
        graph.addEdge("12","21");
        graph.addEdge("21","1f");

        KShortestPathAlgorithm<String,DefaultEdge> yen = new YenKShortestPath<>(graph);
        KShortestPathAlgorithm<String,DefaultEdge> simple = new KShortestSimplePaths<>(graph);

        // should contain exactly 3 elements each
        List<GraphPath<String,DefaultEdge>> yenPaths = yen.getPaths("1e","18",7);
        List<GraphPath<String,DefaultEdge>> kSimplePaths = simple.getPaths("1e","18",7);

        yenPaths.sort(Comparator.comparingDouble(GraphPath::getWeight));
        kSimplePaths.sort(Comparator.comparingDouble(GraphPath::getWeight));

        assertEquals(3, yenPaths.size());
        assertEquals(3, kSimplePaths.size());

        boolean option1 = yenPaths.get(0).equals(kSimplePaths.get(0)) && yenPaths.get(1).equals(kSimplePaths.get(1));
        boolean option2 = yenPaths.get(0).equals(kSimplePaths.get(1)) && yenPaths.get(1).equals(kSimplePaths.get(0));

        assertTrue(option1 ^ option2);
        assertEquals(kSimplePaths.get(2), yenPaths.get(2));
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
    public void testOnRandomGraphs()
    {
        Random random = new Random(SEED);
        int n = 50;
        double p = 0.05;
        for (int i = 0; i < 10; i++) {
            DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
                new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            getRandomGraph(graph, n, p);
            Integer source = (int) (random.nextDouble() * n);
            Integer target = (int) (random.nextDouble() * n);
            testOnRandomGraph(graph, source, target);
        }
    }

    /**
     * If the overall number of paths between {@code source} and {@code target} is denoted by $n$
     * and the value of {@code #NUMBER_OF_PATH_TO_ITERATE} is denoted by $m$ then the method
     * iterates over $p = min\{n, m\}$ such paths and verifies that they are built correctly.
     * The method uses the {@link KShortestSimplePaths} implementation to verify the order
     * of paths returned by {@link YenShortestPathIterator}. Additionally it is checked that
     * all paths returned by the iterator are unique.
     *
     * @param graph graph the iterator is being tested on
     * @param source source vertex
     * @param target target vertex
     */
    private void testOnRandomGraph(
        Graph<Integer, DefaultWeightedEdge> graph, Integer source, Integer target)
    {

        Set<GraphPath<Integer, DefaultWeightedEdge>> paths = new HashSet<>();
        List<GraphPath<Integer, DefaultWeightedEdge>> expectedPaths
                = new KShortestSimplePaths<>(graph).getPaths(source, target, NUMBER_OF_PATH_TO_ITERATE);
        Iterator<GraphPath<Integer, DefaultWeightedEdge>> expectedPathsIterator  = expectedPaths.iterator();
        YenShortestPathIterator<Integer, DefaultWeightedEdge> yenPathIterator =
                new YenShortestPathIterator<>(graph, source, target);

        for (int i = 0; i < NUMBER_OF_PATH_TO_ITERATE && yenPathIterator.hasNext() && expectedPathsIterator.hasNext(); ++i) {
            GraphPath<Integer, DefaultWeightedEdge> expected = expectedPathsIterator.next();
            GraphPath<Integer, DefaultWeightedEdge> actual = yenPathIterator.next();

            assertEquals(expected.getWeight(), actual.getWeight(), 1e-9);
            ((GraphWalk<Integer, DefaultWeightedEdge>) actual).verify();
            paths.add(actual);
        }

        assertEquals(expectedPaths.size(), paths.size());
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

    /**
     * Generates random graph from the $G(n, p)$ model.
     *
     * @param graph graph instance for the generator
     * @param n the number of nodes
     * @param p the edge probability
     */
    private void getRandomGraph(Graph<Integer, DefaultWeightedEdge> graph, int n, double p)
    {
        Random random = new Random(SEED);
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnpRandomGraphGenerator<>(n, p, SEED);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, random.nextDouble()));
    }
}
