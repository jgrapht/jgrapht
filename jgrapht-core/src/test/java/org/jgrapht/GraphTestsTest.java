/*
 * (C) Copyright 2016-2023, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class GraphTests.
 *
 * @author Dimitrios Michail
 * @author Wissal Chbani
 */
public class GraphTestsTest
{

    @Test
    public void testIsLinear() {
        Graph<Integer, DefaultEdge> graph;

        graph = new SimpleGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        assertTrue(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(1, 2);
        assertTrue(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        assertTrue(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        assertFalse(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        assertFalse(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(3, 4);
        assertFalse(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        assertFalse(GraphTests.isLinear(graph));

        graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        assertFalse(GraphTests.isLinear(graph));

        assertThrows(NullPointerException.class, () -> {
            GraphTests.isLinear(null);
        });

        Graph<Integer, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
        directedGraph.addVertex(1);
        directedGraph.addVertex(2);
        directedGraph.addEdge(1, 2);
        assertThrows(IllegalArgumentException.class, () -> {
            GraphTests.isLinear(directedGraph);
        });
    }

    @Test
    public void testIsChordless()
    {
        // Test 1: Triangle (C3) - chordless by definition
        Graph<Integer, DefaultEdge> triangle = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(triangle, 1, 2);
        Graphs.addEdgeWithVertices(triangle, 2, 3);
        Graphs.addEdgeWithVertices(triangle, 3, 1);
        GraphPath<Integer, DefaultEdge> trianglePath =
                new GraphWalk<>(triangle, Arrays.asList(1, 2, 3, 1), 0);
        assertTrue(GraphTests.isChordless(triangle, trianglePath));

        // Test 2: Square (C4) without chord - chordless
        Graph<Integer, DefaultEdge> square = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(square, 1, 2);
        Graphs.addEdgeWithVertices(square, 2, 3);
        Graphs.addEdgeWithVertices(square, 3, 4);
        Graphs.addEdgeWithVertices(square, 4, 1);
        GraphPath<Integer, DefaultEdge> squarePath =
                new GraphWalk<>(square, Arrays.asList(1, 2, 3, 4, 1), 0);
        assertTrue(GraphTests.isChordless(square, squarePath));

        // Test 3: Square with diagonal - NOT chordless
        Graph<Integer, DefaultEdge> squareWithChord = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(squareWithChord, 1, 2);
        Graphs.addEdgeWithVertices(squareWithChord, 2, 3);
        Graphs.addEdgeWithVertices(squareWithChord, 3, 4);
        Graphs.addEdgeWithVertices(squareWithChord, 4, 1);
        Graphs.addEdgeWithVertices(squareWithChord, 1, 3); // chord
        GraphPath<Integer, DefaultEdge> squareWithChordPath =
                new GraphWalk<>(squareWithChord, Arrays.asList(1, 2, 3, 4, 1), 0);
        assertFalse(GraphTests.isChordless(squareWithChord, squareWithChordPath));

        // Test 4: Pentagon (C5) - chordless
        Graph<Integer, DefaultEdge> pentagon = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(pentagon, 1, 2);
        Graphs.addEdgeWithVertices(pentagon, 2, 3);
        Graphs.addEdgeWithVertices(pentagon, 3, 4);
        Graphs.addEdgeWithVertices(pentagon, 4, 5);
        Graphs.addEdgeWithVertices(pentagon, 5, 1);
        GraphPath<Integer, DefaultEdge> pentagonPath =
                new GraphWalk<>(pentagon, Arrays.asList(1, 2, 3, 4, 5, 1), 0);
        assertTrue(GraphTests.isChordless(pentagon, pentagonPath));

        // Test 5: Pentagon with chord - NOT chordless
        Graphs.addEdgeWithVertices(pentagon, 1, 3); // chord
        assertFalse(GraphTests.isChordless(pentagon, pentagonPath));
    }

    @Test
    public void testIsChordlessNullArguments()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 1);
        GraphPath<Integer, DefaultEdge> path =
                new GraphWalk<>(graph, Arrays.asList(1, 2, 3, 1), 0);

        assertThrows(NullPointerException.class, () -> GraphTests.isChordless(null, path));
        assertThrows(NullPointerException.class, () -> GraphTests.isChordless(graph, null));
    }

    @Test
    public void testIsChordlessInvalidCycle()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);

        // Not a cycle (too short)
        GraphPath<Integer, DefaultEdge> tooShort =
                new GraphWalk<>(graph, Arrays.asList(1, 2, 1), 0);
        assertThrows(IllegalArgumentException.class, () -> GraphTests.isChordless(graph, tooShort));

        // Not a cycle (first != last)
        GraphPath<Integer, DefaultEdge> notCycle =
                new GraphWalk<>(graph, Arrays.asList(1, 2, 3), 0);
        assertThrows(IllegalArgumentException.class, () -> GraphTests.isChordless(graph, notCycle));
    }

    @Test
    public void testIsEmpty()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isEmpty(g));
        g.addVertex(1);
        assertTrue(GraphTests.isEmpty(g));
        g.addVertex(2);
        assertTrue(GraphTests.isEmpty(g));
        DefaultEdge e = g.addEdge(1, 2);
        assertFalse(GraphTests.isEmpty(g));
        g.removeEdge(e);
        assertTrue(GraphTests.isEmpty(g));
    }

    @Test
    public void testIsSimple()
    {
        // test empty
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isSimple(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isSimple(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isSimple(g3));

        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        assertTrue(GraphTests.isSimple(g3));
        DefaultEdge g3e11 = g3.addEdge(1, 1);
        assertFalse(GraphTests.isSimple(g3));
        g3.removeEdge(g3e11);
        assertTrue(GraphTests.isSimple(g3));
        g3.addEdge(2, 1);
        assertFalse(GraphTests.isSimple(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        assertTrue(GraphTests.isSimple(g4));
        DefaultEdge g4e12 = g4.addEdge(1, 2);
        g4.addEdge(2, 1);
        assertFalse(GraphTests.isSimple(g4));
        g4.removeEdge(g4e12);
        assertTrue(GraphTests.isSimple(g4));
        g4.addEdge(1, 1);
        assertFalse(GraphTests.isSimple(g4));
    }

    @Test
    public void testHasSelfLoops()
    {
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasSelfLoops(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasSelfLoops(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasSelfLoops(g3));

        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        assertFalse(GraphTests.hasSelfLoops(g3));
        g3.addEdge(2, 2);
        assertTrue(GraphTests.hasSelfLoops(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        g4.addEdge(1, 2);
        g4.addEdge(2, 1);
        assertFalse(GraphTests.hasSelfLoops(g4));
        g4.addEdge(2, 2);
        assertTrue(GraphTests.hasSelfLoops(g4));
    }

    @Test
    public void testHasMultipleEdges()
    {
        Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasMultipleEdges(g1));

        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasMultipleEdges(g2));

        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        assertFalse(GraphTests.hasMultipleEdges(g3));
        Graphs.addAllVertices(g3, Arrays.asList(1, 2));
        g3.addEdge(1, 2);
        g3.addEdge(2, 1);
        g3.addEdge(1, 1);
        assertFalse(GraphTests.hasMultipleEdges(g3));
        g3.addEdge(2, 2);
        assertFalse(GraphTests.hasMultipleEdges(g3));
        g3.addEdge(2, 1);
        assertTrue(GraphTests.hasMultipleEdges(g3));

        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g4, Arrays.asList(1, 2));
        g4.addEdge(1, 2);
        g4.addEdge(1, 1);
        assertFalse(GraphTests.hasMultipleEdges(g4));
        g4.addEdge(2, 1);
        assertTrue(GraphTests.hasMultipleEdges(g4));

        Graph<Integer, DefaultEdge> g5 = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g5, Arrays.asList(1, 2));
        g5.addEdge(1, 2);
        g5.addEdge(1, 1);
        assertFalse(GraphTests.hasMultipleEdges(g5));
        g5.addEdge(1, 1);
        assertTrue(GraphTests.hasMultipleEdges(g5));

    }

    @Test
    public void testIsCompleteDirected()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(3, 1);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(3, 2);
        assertTrue(GraphTests.isComplete(g));

        // check loops
        Graph<Integer, DefaultEdge> g1 = new DirectedPseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(1);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(2);
        assertFalse(GraphTests.isComplete(g1));
        g1.addEdge(1, 1);
        g1.addEdge(2, 2);
        assertFalse(GraphTests.isComplete(g1));

        // check multiple edges
        Graph<Integer, DefaultEdge> g2 = new DirectedPseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g2));
        Graphs.addAllVertices(g2, Arrays.asList(1, 2, 3));
        assertFalse(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        g2.addEdge(1, 3);
        g2.addEdge(2, 3);
        g2.addEdge(1, 1);
        g2.addEdge(2, 2);
        g2.addEdge(3, 3);
        assertFalse(GraphTests.isComplete(g2));
    }

    @Test
    public void testIsCompleteUndirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(1);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(2);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isComplete(g));
        g.addVertex(3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isComplete(g));

        // check loops
        Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(1);
        assertTrue(GraphTests.isComplete(g1));
        g1.addVertex(2);
        assertFalse(GraphTests.isComplete(g1));
        g1.addEdge(1, 1);
        assertFalse(GraphTests.isComplete(g1));

        // check multiple edges
        Graph<Integer, DefaultEdge> g2 = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isComplete(g2));
        g2.addVertex(1);
        assertTrue(GraphTests.isComplete(g2));
        g2.addVertex(2);
        assertFalse(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        assertTrue(GraphTests.isComplete(g2));
        g2.addEdge(1, 2);
        assertFalse(GraphTests.isComplete(g2));
        g2.addVertex(3);
        g2.addEdge(1, 3);
        assertFalse(GraphTests.isComplete(g2));
    }

    @Test
    public void testIsConnectedUndirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.isConnected(g));
        g.addVertex(1);
        assertTrue(GraphTests.isConnected(g));
        g.addVertex(2);
        assertFalse(GraphTests.isConnected(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isConnected(g));
        g.addVertex(3);
        assertFalse(GraphTests.isConnected(g));
        g.addEdge(1, 3);
        assertTrue(GraphTests.isConnected(g));
    }

    @Test
    public void testIsConnectedDirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addVertex(1);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertTrue(GraphTests.isStronglyConnected(g));
        g.addVertex(2);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addVertex(3);
        assertFalse(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertFalse(GraphTests.isStronglyConnected(g));
        g.addEdge(3, 1);
        assertTrue(GraphTests.isWeaklyConnected(g));
        assertTrue(GraphTests.isStronglyConnected(g));
    }

    @Test
    public void testIsTree()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isTree(g));
        g.addVertex(1);
        assertTrue(GraphTests.isTree(g));
        g.addVertex(2);
        assertFalse(GraphTests.isTree(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isTree(g));
        g.addVertex(3);
        assertFalse(GraphTests.isTree(g));
        g.addEdge(1, 3);
        assertTrue(GraphTests.isTree(g));
        g.addEdge(2, 3);
        assertFalse(GraphTests.isTree(g));

        // disconnected but with correct number of edges
        Graph<Integer, DefaultEdge> g1 = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isTree(g1));
        g1.addVertex(1);
        g1.addVertex(2);
        g.addEdge(1, 1);
        assertFalse(GraphTests.isTree(g1));
    }

    @Test
    public void testIsForest1()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        assertFalse(GraphTests.isForest(g));
        g.addVertex(1);
        assertTrue(GraphTests.isForest(g));
        g.addVertex(2);
        assertTrue(GraphTests.isForest(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isForest(g));
        g.addEdge(1, 2);
        assertFalse(GraphTests.isForest(g));
    }

    @Test
    public void testIsForest2()
    {
        Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
        StarGraphGenerator<Integer, DefaultEdge> gen = new StarGraphGenerator<>(10);
        gen.generateGraph(g);
        gen.generateGraph(g);
        assertTrue(GraphTests.isForest(g));
    }

    @Test
    public void testIsOverfull()
    {
        assertFalse(GraphTests.isOverfull(NamedGraphGenerator.clawGraph()));
        assertTrue(GraphTests.isOverfull(NamedGraphGenerator.doyleGraph()));

        Graph<Integer, DefaultEdge> k6 = GraphTestsUtils.createPseudograph();
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(6);
        gen.generateGraph(k6);
        assertFalse(GraphTests.isOverfull(k6));

        Graph<Integer, DefaultEdge> k7 = GraphTestsUtils.createPseudograph();
        gen = new CompleteGraphGenerator<>(7);
        gen.generateGraph(k7);
        assertTrue(GraphTests.isOverfull(k7));
    }

    @Test
    public void isSplit1()
    {
        assertFalse(GraphTests.isSplit(NamedGraphGenerator.petersenGraph()));
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        assertFalse(GraphTests.isSplit(g));
        g.addVertex(0);
        assertTrue(GraphTests.isSplit(g));
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        // clique
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        // independent set
        g.addEdge(3, 1);
        g.addEdge(3, 2);
        g.addEdge(4, 1);
        assertTrue(GraphTests.isSplit(g));
        g.addEdge(3, 4);
        assertTrue(GraphTests.isSplit(g));
    }

    @Test
    public void isSplit2()
    {
        // Create some random split graphs.
        Random rand = new Random(0);
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(6);

        for (int inst = 0; inst < 5; inst++) {
            // 1. create a clique
            Graph<Integer, DefaultEdge> g = GraphTestsUtils.createSimpleGraph();
            gen.generateGraph(g);

            // 2. add a number of vertices (the independent set) and connect some of these vertices
            // with vertices in the clique.
            for (int j = 6; j < 12; j++) {
                g.addVertex(j);
                for (int i = 0; i < 6; i++)
                    if (rand.nextBoolean())
                        g.addEdge(i, j);
            }
            assertTrue(GraphTests.isSplit(g));
        }
    }

    @Test
    public void testIsCubic()
    {
        assertTrue(GraphTests.isCubic(NamedGraphGenerator.petersenGraph()));
        Graph<Integer, DefaultEdge> triangle = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(triangle, 1, 2);
        Graphs.addEdgeWithVertices(triangle, 2, 3);
        Graphs.addEdgeWithVertices(triangle, 3, 1);
        assertFalse(GraphTests.isCubic(triangle));
    }

    @Test
    public void testIsChordal()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        assertFalse(GraphTests.isChordal(graph));
        Graphs.addEdgeWithVertices(graph, 1, 4);
        assertTrue(GraphTests.isChordal(graph));
    }

    @Test
    public void testIsWeaklyChordal()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 1);
        assertFalse(GraphTests.isWeaklyChordal(graph));
        Graphs.addEdgeWithVertices(graph, 1, 3);
        assertTrue(GraphTests.isWeaklyChordal(graph));
    }

    /**
     * Full graph on 4 vertices (every graph on less that 5 is planar)
     */
    @Test
    public void testIsPlanar1()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 3 }, { 2, 4 }, { 3, 4 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertTrue(GraphTests.isPlanar(graph));
    }

    /**
     * $K_{3,3}$
     */
    @Test
    public void testIsPlanar2()
    {
        int[][] edges = { { 1, 4 }, { 1, 5 }, { 1, 6 }, { 2, 4 }, { 2, 5 }, { 2, 6 }, { 3, 4 },
            { 3, 5 }, { 3, 6 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertFalse(GraphTests.isPlanar(graph));
    }

    /**
     * $K_{5}$
     */
    @Test
    public void testIsPlanar3()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 1, 5 }, { 2, 3 }, { 2, 4 }, { 2, 5 },
            { 3, 4 }, { 3, 5 }, { 4, 5 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertFalse(GraphTests.isPlanar(graph));
    }

    @Test
    public void testIsK33Subdivision1()
    {
        int[][] edges = { { 1, 4 }, { 1, 5 }, { 1, 6 }, { 2, 4 }, { 2, 5 }, { 2, 6 }, { 3, 4 },
            { 3, 5 }, { 3, 6 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertTrue(GraphTests.isKuratowskiSubdivision(graph));
        assertTrue(GraphTests.isK33Subdivision(graph));
    }

    @Test
    public void testIsK33Subdivision2()
    {
        int[][] edges = { { 1, 5 }, { 1, 6 }, { 2, 4 }, { 2, 6 }, { 3, 4 }, { 3, 5 }, { 1, 7 },
            { 7, 4 }, { 2, 8 }, { 8, 5 }, { 3, 9 }, { 9, 6 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertTrue(GraphTests.isKuratowskiSubdivision(graph));
        assertTrue(GraphTests.isK33Subdivision(graph));
    }

    @Test
    public void testIsK5Subdivision1()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 1, 5 }, { 2, 3 }, { 2, 4 }, { 2, 5 },
            { 3, 4 }, { 3, 5 }, { 4, 5 } };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertTrue(GraphTests.isKuratowskiSubdivision(graph));
        assertTrue(GraphTests.isK5Subdivision(graph));
    }

    @Test
    public void testIsK5Subdivision2()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 8 },
            { 8, 9 }, { 9, 10 }, { 10, 1 }, { 1, 5 }, { 1, 7 }, { 3, 7 }, { 3, 9 }, { 5, 9 }, };
        Graph<Integer, DefaultEdge> graph = getGraph(edges);
        assertTrue(GraphTests.isKuratowskiSubdivision(graph));
        assertTrue(GraphTests.isK5Subdivision(graph));
    }

    @Test
    public void failRequireIsWeightedOnUnweightedGraph()
    {
        try {
            Graph<String, DefaultWeightedEdge> graph =
                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
            GraphTests.requireWeighted(graph);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Graph must be weighted"));
        }
    }

    @Test
    public void failRequireIsWeightedOnNull()
    {
        try {
            GraphTests.requireWeighted(null);
            fail("Expected an NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Graph cannot be null"));
        }
    }

    @Test
    public void testRequireIsWeighted()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        assertEquals(graph, GraphTests.requireWeighted(graph));
    }

    /**
     * Creates a graph from the list of its edges
     *
     * @param edges the edge list of a graph
     * @return a graph specified by the {@code edges}
     */
    private Graph<Integer, DefaultEdge> getGraph(int[][] edges)
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        for (int[] edge : edges) {
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        }
        return graph;
    }
}
