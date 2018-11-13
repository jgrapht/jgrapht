/*
 * (C) Copyright 2016-2018, by Vera-Licona Research Group and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test cases for the AllDirectedPaths algorithm.
 *
 * @author Andrew Gainer-Dewar, Google LLC
 **/

public class AllDirectedPathsTest
{
    private static final String I1 = "I1";
    private static final String I2 = "I2";
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";
    private static final String F = "F";
    private static final String O1 = "O1";
    private static final String O2 = "O2";

    @Test
    public void testSmallExampleGraph()
    {
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = new HashSet<>();
        sources.add(I1);
        sources.add(I2);

        Set<String> targets = new HashSet<>();
        targets.add(O1);
        targets.add(O2);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, null);

        assertEquals("Toy network should have correct number of simple paths", 7, allPaths.size());
    }

    @Test
    public void testTrivialPaths()
    {
        // Verify fix for http://github.com/jgrapht/jgrapht/issues/234.
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = new HashSet<>();
        sources.add(I1);

        Set<String> targets = new HashSet<>();
        targets.add(I1);
        targets.add(A);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, 1);

        assertEquals(
            "Toy network should have correct number of trivial simple paths", 2, allPaths.size());
        assertEquals(Arrays.asList(I1), allPaths.get(0).getVertexList());
        assertEquals(Arrays.asList(I1, A), allPaths.get(1).getVertexList());
    }

    @Test
    public void testLengthOnePaths()
    {
        // Verify fix for http://github.com/jgrapht/jgrapht/issues/441.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("B", "A");

        AllDirectedPaths<String, DefaultEdge> all = new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultEdge>> allPaths =
            all.getAllPaths(graph.vertexSet(), graph.vertexSet(), true, graph.edgeSet().size());

        assertEquals(3, allPaths.size());
        assertEquals(Arrays.asList("A"), allPaths.get(0).getVertexList());
        assertEquals(Arrays.asList("B"), allPaths.get(1).getVertexList());
        assertEquals(Arrays.asList("B", "A"), allPaths.get(2).getVertexList());
    }

    @Test
    public void testPathWeights()
    {
        // Verify fix for https://github.com/jgrapht/jgrapht/issues/617.
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.setEdgeWeight(graph.addEdge("A", "B"), 1.2);
        graph.setEdgeWeight(graph.addEdge("A", "C"), 0);
        graph.setEdgeWeight(graph.addEdge("A", "D"), -1);
        graph.setEdgeWeight(graph.addEdge("B", "C"), 2);
        graph.setEdgeWeight(graph.addEdge("B", "D"), 1);
        graph.setEdgeWeight(graph.addEdge("C", "D"), 0.5);

        AllDirectedPaths<String, DefaultWeightedEdge> all = new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultWeightedEdge>> allPaths = all.getAllPaths("A", "D", true, 2);
        allPaths.sort(Comparator.comparing(GraphPath::getWeight));

        assertEquals(
            "Example weighted graph has 3 paths of length no greater than 2", 3, allPaths.size());
        ;

        assertEquals(Arrays.asList("A", "D"), allPaths.get(0).getVertexList());
        assertEquals(-1, allPaths.get(0).getWeight(), 0);

        assertEquals(Arrays.asList("A", "C", "D"), allPaths.get(1).getVertexList());
        assertEquals(0.5, allPaths.get(1).getWeight(), 0);

        assertEquals(Arrays.asList("A", "B", "D"), allPaths.get(2).getVertexList());
        assertEquals(2.2, allPaths.get(2).getWeight(), 0);
    }

    @Test
    public void testCycleBehavior()
    {
        Graph<String, DefaultEdge> toyGraph = toyGraph();
        toyGraph.addEdge(D, A);

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph);

        Set<String> sources = new HashSet<>();
        sources.add(I1);
        sources.add(I2);

        Set<String> targets = new HashSet<>();
        targets.add(O1);
        targets.add(O2);

        List<GraphPath<String, DefaultEdge>> allPathsWithoutCycle =
            pathFindingAlg.getAllPaths(sources, targets, true, 8);

        List<GraphPath<String, DefaultEdge>> allPathsWithCycle =
            pathFindingAlg.getAllPaths(sources, targets, false, 8);

        assertEquals(
            "Toy network with cycle should have correct number of paths with cycle", 13,
            allPathsWithCycle.size());
        assertEquals(
            "Toy network with cycle should have correct number of simple paths", 7,
            allPathsWithoutCycle.size());
    }

    @Test
    public void testMustBoundIfNonSimplePaths()
    {
        // Goofy hack to test for an exception

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = Collections.singleton(I1);
        Set<String> targets = Collections.singleton(O1);

        try {
            pathFindingAlg.getAllPaths(sources, targets, false, null);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // This is the expected outcome, so the test passes
        }
    }

    @Test
    public void testZeroLengthPaths()
    {
        // Verify fix for https://github.com/jgrapht/jgrapht/issues/640.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);

        graph.addVertex("a");
        graph.addVertex("b");
        graph.addEdge("a", "b");

        List<GraphPath<String, DefaultEdge>> paths = new AllDirectedPaths<>(graph)
            .getAllPaths(graph.vertexSet(), graph.vertexSet(), false, 0);

        Assert.assertFalse("We should find at least some paths!", paths.isEmpty());

        paths.forEach(
            path -> Assert.assertEquals(
                String.format(
                    "The path %s has length %d even though we requested only paths of length 0",
                    path, path.getLength()),
                0, path.getLength()));
    }

    private static Graph<String, DefaultEdge> toyGraph()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(I1);
        graph.addVertex(I2);
        graph.addVertex(A);
        graph.addVertex(B);
        graph.addVertex(C);
        graph.addVertex(D);
        graph.addVertex(E);
        graph.addVertex(F);
        graph.addVertex(O1);
        graph.addVertex(O2);

        graph.addEdge(I1, A);
        graph.addEdge(I1, B);

        graph.addEdge(I2, B);
        graph.addEdge(I2, C);

        graph.addEdge(A, B);
        graph.addEdge(A, D);
        graph.addEdge(A, E);

        graph.addEdge(B, E);

        graph.addEdge(C, B);
        graph.addEdge(C, F);

        graph.addEdge(D, E);

        graph.addEdge(E, O1);

        graph.addEdge(F, O2);

        return graph;
    }
}
