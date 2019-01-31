/*
 * (C) Copyright 2008-2018, by Peter Giles and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.generate.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the DirectedAcyclicGraph, a dynamic DAG implementation.
 *
 * @author Peter Giles
 */
public class DirectedAcyclicGraphTest
{
    /**
     * Tests the cycle detection capabilities of DirectedAcyclicGraph by building a parallel
     * SimpleDirectedGraph and using a CycleDetector to check for cycles, and comparing the results.
     */
    @Test
    public void testCycleDetectionInRandomGraphBuild()
    {
        for (int i = 0; i < 50; i++) { // test with 50 random graph
                                       // configurations
            Graph<Long, DefaultEdge> sourceGraph = setUpWithSeed(20, 200, i);

            DirectedAcyclicGraph<Long, DefaultEdge> dag =
                new DirectedAcyclicGraph<>(DefaultEdge.class);
            SimpleDirectedGraph<Long, DefaultEdge> compareGraph =
                new SimpleDirectedGraph<>(DefaultEdge.class);

            for (Long vertex : sourceGraph.vertexSet()) {
                dag.addVertex(vertex);
                compareGraph.addVertex(vertex);
            }

            for (DefaultEdge edge : sourceGraph.edgeSet()) {
                Long edgeSource = sourceGraph.getEdgeSource(edge);
                Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                boolean dagRejectedEdge = false;
                try {
                    dag.addEdge(edgeSource, edgeTarget);
                } catch (IllegalArgumentException e) {
                    // okay, it did't add that edge
                    dagRejectedEdge = true;
                }

                DefaultEdge compareEdge = compareGraph.addEdge(edgeSource, edgeTarget);
                CycleDetector<Long, DefaultEdge> cycleDetector = new CycleDetector<>(compareGraph);

                boolean cycleDetected = cycleDetector.detectCycles();

                assertTrue(dagRejectedEdge == cycleDetected);

                if (cycleDetected) {
                    // remove the edge from the compareGraph so the graphs
                    // remain in sync
                    compareGraph.removeEdge(compareEdge);
                }
            }

            // after all this, our graphs must be equal
            assertEquals(compareGraph.vertexSet(), dag.vertexSet());

            // for some reason comparing vertex sets doesn't work, so doing it
            // the hard way:
            for (Long sourceVertex : compareGraph.vertexSet()) {
                for (DefaultEdge outgoingEdge : compareGraph.outgoingEdgesOf(sourceVertex)) {
                    Long targetVertex = compareGraph.getEdgeTarget(outgoingEdge);
                    assertTrue(dag.containsEdge(sourceVertex, targetVertex));
                }
            }
        }
    }

    /**
     * trivial test of topological order using a linear graph
     */
    @Test
    public void testTopoIterationOrderLinearGraph()
    {
        DirectedAcyclicGraph<Long, DefaultEdge> dag = new DirectedAcyclicGraph<>(
            SupplierUtil.createLongSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        LinearGraphGenerator<Long, DefaultEdge> graphGen = new LinearGraphGenerator<>(100);
        graphGen.generateGraph(dag);

        Iterator<Long> internalTopoIter = dag.iterator();
        TopologicalOrderIterator<Long, DefaultEdge> comparTopoIter =
            new TopologicalOrderIterator<>(dag);

        while (comparTopoIter.hasNext()) {
            Long compareNext = comparTopoIter.next();
            Long myNext = null;

            if (internalTopoIter.hasNext()) {
                myNext = internalTopoIter.next();
            }

            assertSame(compareNext, myNext);
            assertEquals(comparTopoIter.hasNext(), internalTopoIter.hasNext());
        }
    }

    /**
     * more rigorous test of topological iteration order, by assuring that each visited vertex
     * adheres to the definition of topological order, that is that it doesn't have a path leading
     * to any of its predecessors.
     */
    @Test
    public void testTopoIterationOrderComplexGraph()
    {
        for (int seed = 0; seed < 20; seed++) {
            DirectedAcyclicGraph<Long, DefaultEdge> dag = new DirectedAcyclicGraph<>(
                SupplierUtil.createLongSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            RepeatableRandomGraphGenerator<Long, DefaultEdge> graphGen =
                new RepeatableRandomGraphGenerator<>(100, 500, seed);
            graphGen.generateGraph(dag);

            ConnectivityInspector<Long, DefaultEdge> connectivityInspector =
                new ConnectivityInspector<>(dag);

            Iterator<Long> internalTopoIter = dag.iterator();

            List<Long> previousVertices = new ArrayList<>();

            while (internalTopoIter.hasNext()) {
                Long vertex = internalTopoIter.next();

                for (Long previousVertex : previousVertices) {
                    connectivityInspector.pathExists(vertex, previousVertex);
                }

                previousVertices.add(vertex);
            }
        }
    }

    @Test
    public void testIterationBehaviors()
    {
        int vertexCount = 100;

        DirectedAcyclicGraph<Long, DefaultEdge> dag = new DirectedAcyclicGraph<>(
            SupplierUtil.createLongSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        RepeatableRandomGraphGenerator<Long, DefaultEdge> graphGen =
            new RepeatableRandomGraphGenerator<>(vertexCount, 500, 2);
        graphGen.generateGraph(dag);

        Iterator<Long> dagIter = dag.iterator();

        // Scroll through all the elements, then make sure things happen as
        // should when an iterator is all used up

        for (int i = 0; i < vertexCount; i++) {
            assertTrue(dagIter.hasNext());
            dagIter.next();
        }
        assertFalse(dagIter.hasNext());

        try {
            dagIter.next();
            fail();
        } catch (NoSuchElementException e) {
            // good, we already looked at all of the elements
        }

        assertFalse(dagIter.hasNext());

        dagIter = dag.iterator(); // replace dagIter;

        assertNotNull(dagIter.next()); // make sure it works on first element
                                       // even if hasNext() wasn't called

        // Test that ConcurrentModificationExceptionS happen as they should when
        // the topology is modified during iteration

        // remove a random vertex
        dag.removeVertex(dag.vertexSet().iterator().next());

        // now we expect exceptions since the topological order has been
        // modified (albeit trivially)
        try {
            dagIter.next();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }

        try {
            dagIter.hasNext();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }

        try {
            dagIter.remove();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }
    }

    @Test
    public void testWhenVertexIsNotInGraph_Then_ThowException()
    {
        DirectedAcyclicGraph<Long, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);
        try {
            dag.addEdge(1l, 2l);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("No exception 'IllegalArgumentException' catched");
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Expected output when determining ancestors of C
     * (order does not matter):
     *
     * B, A
     */
    //@formatter:on
    @Test
    public void testDetermineAncestors00()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Set<String> expectedAncestors = new HashSet<>();
        expectedAncestors.add("B");
        expectedAncestors.add("A");

        Set<String> ancestors = graph.getAncestors("C");

        assertEquals(expectedAncestors, ancestors);
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Expected output when determining ancestors of A:
     *
     * <empty list>
     */
    //@formatter:on
    @Test
    public void testDetermineAncestors01()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Set<String> expectedAncestors = new HashSet<>();

        Set<String> ancestors = graph.getAncestors("A");

        assertEquals(expectedAncestors, ancestors);
    }

    //@formatter:off
    /**
     * Input:
     *
     * A +--> B +--> C
     * |             ^
     * |             |
     * +-------------+
     *
     * Expected output when determining ancestors of A
     * (order does not matter):
     *
     * B, A
     */
    //@formatter:on
    @Test
    public void testDetermineAncestors02()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(a, c);

        Set<String> expectedAncestors = new HashSet<>();
        expectedAncestors.add("B");
        expectedAncestors.add("A");

        Set<String> ancestors = graph.getAncestors("C");

        assertEquals(expectedAncestors, ancestors);
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Expected output when determining descendents of B
     * (order does not matter):
     *
     * C, D
     */
    //@formatter:on
    @Test
    public void testDetermineDescendants00()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Set<String> expectedDescendents = new HashSet<>();
        expectedDescendents.add("C");
        expectedDescendents.add("D");

        Set<String> ancestors = graph.getDescendants("B");

        assertEquals(expectedDescendents, ancestors);
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Expected output when determining descendents of C:
     *
     * <empty list>
     */
    //@formatter:on
    @Test
    public void testDetermineDescendants01()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Set<String> expectedDescendents = new HashSet<>();

        Set<String> ancestors = graph.getDescendants("C");

        assertEquals(expectedDescendents, ancestors);
    }

    //@formatter:off
    /**
     * Input:
     *
     * A +--> B +--> C
     * |             ^
     * |             |
     * +-------------+
     *
     * Expected output when determining ancestors of A
     * (order does not matter):
     *
     * B, C
     */
    //@formatter:on
    @Test
    public void testDetermineDescendants02()
    {

        DirectedAcyclicGraph<String, TestEdge> graph =
            new DirectedAcyclicGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(a, c);

        Set<String> expectedAncestors = new HashSet<>();
        expectedAncestors.add("B");
        expectedAncestors.add("C");

        Set<String> ancestors = graph.getDescendants("A");

        assertEquals(expectedAncestors, ancestors);
    }

    @Test
    public void testRemoveAllVerticesShouldNotDeleteTopologyIfTheGraphHasVerticesLeft()
    {
        // Given
        DirectedAcyclicGraph<String, TestEdge> dag = new DirectedAcyclicGraph<>(TestEdge.class);

        List<String> vertices = Arrays.asList("a", "b", "c", "d", "e");

        vertices.forEach(dag::addVertex);

        dag.addEdge("e", "a");
        dag.addEdge("e", "b");
        dag.addEdge("a", "d");
        dag.addEdge("b", "c");

        // When
        dag.removeAllVertices(vertices.subList(0, vertices.size() - 2));

        // Then
        assertTrue(dag.iterator().hasNext());
    }

    // ~ Private Methods ----------------------------------------------------------

    private Graph<Long, DefaultEdge> setUpWithSeed(int vertices, int edges, long seed)
    {
        GraphGenerator<Long, DefaultEdge, Long> randomGraphGenerator =
            new RepeatableRandomGraphGenerator<>(vertices, edges, seed);
        Graph<Long, DefaultEdge> sourceGraph = new SimpleDirectedGraph<>(
            SupplierUtil.createLongSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        randomGraphGenerator.generateGraph(sourceGraph);
        return sourceGraph;
    }

    // ~ Inner Classes ----------------------------------------------------------

    // it is nice for tests to be easily repeatable, so we use a graph generator
    // that we can seed for specific configurations
    public static class RepeatableRandomGraphGenerator<V, E>
        implements
        GraphGenerator<V, E, V>
    {
        private Random randomizer;
        private int numOfVertexes;
        private int numOfEdges;

        public RepeatableRandomGraphGenerator(int vertices, int edges, long seed)
        {
            this.numOfVertexes = vertices;
            this.numOfEdges = edges;
            this.randomizer = new Random(seed);
        }

        @Override
        public void generateGraph(Graph<V, E> graph, Map<String, V> namedVerticesMap)
        {
            List<V> vertices = new ArrayList<>(numOfVertexes);
            Set<Integer> edgeGeneratorIds = new HashSet<>();

            for (int i = 0; i < numOfVertexes; i++) {
                vertices.add(graph.addVertex());
            }

            for (int i = 0; i < numOfEdges; i++) {
                Integer edgeGeneratorId;
                do {
                    edgeGeneratorId = randomizer.nextInt(numOfVertexes * (numOfVertexes - 1));
                } while (edgeGeneratorIds.contains(edgeGeneratorId));

                int fromVertexId = edgeGeneratorId / numOfVertexes;
                int toVertexId = edgeGeneratorId % (numOfVertexes - 1);
                if (toVertexId >= fromVertexId) {
                    ++toVertexId;
                }

                try {
                    graph.addEdge(vertices.get(fromVertexId), vertices.get(toVertexId));
                } catch (IllegalArgumentException e) {
                    // okay, that's fine; omit cycle
                }
            }
        }
    }

}
