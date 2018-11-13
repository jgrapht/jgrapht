/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * 
 * Tests for the {@link BaseKDisjointShortestPathsAlgorithm} class.
 * 
 * @author Assaf Mizrachi
 */
public abstract class KDisjointShortestPathsTestCase
{

    /**
     * Tests single path
     * 
     * Edges expected in path --------------- {@literal 1 --> 2}
     */
    @Test
    public void testSinglePath()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        DefaultWeightedEdge edge = graph.addEdge(1, 2);
        graph.setEdgeWeight(edge, 8);
        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 2, 5);
        assertEquals(1, pathList.size());
        assertEquals(1, pathList.get(0).getLength());
        assertTrue(pathList.get(0).getEdgeList().contains(edge));
        assertEquals(new Integer(2), pathList.get(0).getEndVertex());
        assertEquals(new Integer(1), pathList.get(0).getStartVertex());
        assertEquals(2, pathList.get(0).getVertexList().size());
        assertTrue(pathList.get(0).getVertexList().contains(1));
        assertTrue(pathList.get(0).getVertexList().contains(2));
        assertEquals(8.0, pathList.get(0).getWeight(), 0.0);
    }

    /**
     * Tests two disjoint paths traversing common vertex.
     * 
     * Expected path 1 --------------- {@literal 1 --> 2 --> 3 --> 4 --> 5}
     * 
     * Expected path 2 --------------- {@literal 1 --> 7 --> 3 --> 6 --> 5}
     * 
     */
    @Test
    public void testTwoDisjointPathsJointNode()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addVertex(7);
        graph.addEdge(1, 2);
        graph.addEdge(1, 7);
        graph.addEdge(2, 3);
        graph.addEdge(7, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 6);
        graph.addEdge(4, 5);
        graph.addEdge(6, 5);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5, 2);

        assertEquals(2, pathList.size());

        assertEquals(4, pathList.get(0).getLength());
        assertEquals(4.0, pathList.get(0).getWeight(), 0.0);

        assertEquals(4, pathList.get(1).getLength());
        assertEquals(4.0, pathList.get(1).getWeight(), 0.0);

        // We have four potential paths all must pass through the joint node #3
        GraphPath<Integer, DefaultWeightedEdge> potentialP1_1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 3, 4, 5), 4);
        GraphPath<Integer, DefaultWeightedEdge> potentialP1_2 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 3, 6, 5), 4);
        GraphPath<Integer, DefaultWeightedEdge> potentialP1_3 =
            new GraphWalk<>(graph, Arrays.asList(1, 7, 3, 4, 5), 4);
        GraphPath<Integer, DefaultWeightedEdge> potentialP1_4 =
            new GraphWalk<>(graph, Arrays.asList(1, 7, 3, 6, 5), 4);

        if (pathList.get(0).equals(potentialP1_1)) {
            assertEquals(potentialP1_4, pathList.get(1));
        } else if (pathList.get(0).equals(potentialP1_2)) {
            assertEquals(potentialP1_3, pathList.get(1));
        } else if (pathList.get(0).equals(potentialP1_3)) {
            assertEquals(potentialP1_2, pathList.get(1));
        } else if (pathList.get(0).equals(potentialP1_4)) {
            assertEquals(potentialP1_1, pathList.get(1));
        } else {
            fail("Unexpected path");
        }

    }

    /**
     * Tests two disjoint paths from 1 to 3
     * 
     * Edges expected in path 1 --------------- {@literal 1 --> 3}
     * 
     * Edges expected in path 2 --------------- {@literal 1 --> 2} {@literal 2 --> 3}
     * 
     */
    @Test
    public void testTwoDisjointPaths()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(1, 3);
        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 3, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 3), 1);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(1, pathList.get(0).getLength());
        assertEquals(1.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 3), 2);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(2.0, pathList.get(1).getWeight(), 0.0);

    }

    @Test
    public void testDisconnectedGraph()
    {

        Graph<Integer, DefaultWeightedEdge> graph = createDisconnectedGraph();

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 3, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 3), 3);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(3.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 3), 4);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(1, pathList.get(1).getLength());
        assertEquals(4.0, pathList.get(1).getWeight(), 0.0);

    }

    private Graph<Integer, DefaultWeightedEdge> createDisconnectedGraph()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);

        DefaultWeightedEdge e;
        e = graph.addEdge(1, 2);
        graph.setEdgeWeight(e, 2.0);
        e = graph.addEdge(2, 1);
        graph.setEdgeWeight(e, 2.0);

        e = graph.addEdge(2, 3);
        graph.setEdgeWeight(e, 1.0);
        e = graph.addEdge(3, 2);
        graph.setEdgeWeight(e, 1.0);

        e = graph.addEdge(3, 1);
        graph.setEdgeWeight(e, 4.0);
        e = graph.addEdge(1, 3);
        graph.setEdgeWeight(e, 4.0);

        e = graph.addEdge(4, 5);
        graph.setEdgeWeight(e, 7.0);
        e = graph.addEdge(5, 6);
        graph.setEdgeWeight(e, 8.0);
        e = graph.addEdge(6, 4);
        graph.setEdgeWeight(e, 9.0);

        return graph;
    }

    /**
     * Tests two joint paths from 1 to 4, merge paths is not required.
     * 
     * Edges expected in path 1 --------------- {@literal 1 --> 2}, w=1 {@literal 2 --> 6}, w=1
     * {@literal 6 --> 4}, w=1
     * 
     * Edges expected in path 2 --------------- {@literal 1 --> 5}, w=2 {@literal 5 --> 3}, w=2
     * {@literal 3 --> 4}, w=2
     * 
     * Edges expected in no path --------------- {@literal 2 --> 3}, w=3
     * 
     */
    @Test
    public void testTwoDisjointPathsNoNeedToMerge()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        // this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);

        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 3);
        graph.setEdgeWeight(e34, 2);
        graph.setEdgeWeight(e15, 2);
        graph.setEdgeWeight(e53, 2);
        graph.setEdgeWeight(e26, 1);
        graph.setEdgeWeight(e64, 1);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 6, 4), 3);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 5, 3, 4), 6);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(6.0, pathList.get(1).getWeight(), 0.0);
    }

    /**
     * Tests two joint paths from 1 to 4, merge paths is required.
     * 
     * Edges expected in path 1 --------------- {@literal 1 --> 2}, w=1 {@literal 2 --> 6}, w=2
     * {@literal 6 --> 4}, w=2
     * 
     * Edges expected in path 2 --------------- {@literal 1 --> 5}, w=1 {@literal 5 --> 3}, w=3
     * {@literal 3 --> 4}, w=3
     * 
     * Edges expected in no path --------------- {@literal 2 --> 3}, w=1
     * 
     */
    @Test
    public void testTwoDisjointPathsNeedToMerge()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        // this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);

        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);
        graph.setEdgeWeight(e15, 3);
        graph.setEdgeWeight(e53, 3);
        graph.setEdgeWeight(e26, 2);
        graph.setEdgeWeight(e64, 2);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 6, 4), 5);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 5, 3, 4), 7);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
    }

    /**
     * Tests two joint paths from 1 to 4, reversed edges already exist in graph so not added when
     * preparing for next phase.
     * 
     * Edges expected in path 1 --------------- {@literal 1 --> 2}, w=1 {@literal 2 --> 6}, w=2
     * {@literal 6 --> 4}, w=2
     * 
     * Edges expected in path 2 --------------- {@literal 1 --> 5}, w=1 {@literal 5 --> 3}, w=3
     * {@literal 3 --> 4}, w=3
     * 
     * Edges expected in no path --------------- {@literal 2 --> 3}, w=1
     * 
     */
    @Test
    public void testTwoDisjointPathsWithReversedEdgesExist()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        // this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);

        DefaultWeightedEdge e21 = graph.addEdge(2, 1);
        // this edge should not be used
        DefaultWeightedEdge e32 = graph.addEdge(3, 2);
        DefaultWeightedEdge e43 = graph.addEdge(4, 3);
        DefaultWeightedEdge e51 = graph.addEdge(5, 1);
        DefaultWeightedEdge e35 = graph.addEdge(3, 5);
        DefaultWeightedEdge e62 = graph.addEdge(6, 2);
        DefaultWeightedEdge e46 = graph.addEdge(4, 6);

        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);
        graph.setEdgeWeight(e15, 3);
        graph.setEdgeWeight(e53, 3);
        graph.setEdgeWeight(e26, 2);
        graph.setEdgeWeight(e64, 2);

        graph.setEdgeWeight(e21, 1);
        graph.setEdgeWeight(e32, 1);
        graph.setEdgeWeight(e43, 1);
        graph.setEdgeWeight(e51, 3);
        graph.setEdgeWeight(e35, 3);
        graph.setEdgeWeight(e62, 2);
        graph.setEdgeWeight(e46, 2);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 6, 4), 5);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 5, 3, 4), 7);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
    }

    /**
     * Tests three joint paths from 1 to 5.
     * <p>
     * Edges expected in path 1 --------------- {@literal 1 --> 4}, w=4 {@literal 4 --> 5}, w=1
     * <p>
     * Edges expected in path 2 --------------- {@literal 1 --> 2}, w=1 {@literal 2 --> 5}, w=6
     * <p>
     * Edges expected in path 3 --------------- {@literal 1 --> 3}, w=4 {@literal 3 --> 5}, w=5
     * <p>
     * Edges expected in no path --------------- {@literal 2 --> 3}, w=1 {@literal 3 --> 4}, w=1
     */
    @Test
    public void testThreeDisjointPaths()
    {
        Graph<Integer, DefaultWeightedEdge> graph = createThreeDisjointPathsGraph();

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5, 5);

        assertEquals(3, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 4, 5), 5);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 5), 7);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP3 =
            new GraphWalk<>(graph, Arrays.asList(1, 3, 5), 9);
        assertEquals(expectedP3, pathList.get(2));
        assertEquals(2, pathList.get(2).getLength());
        assertEquals(9.0, pathList.get(2).getWeight(), 0.0);

    }

    @Test
    public void testThreeDisjointPathsReverseEdgeExist()
    {
        Graph<Integer, DefaultWeightedEdge> graph = createThreeDisjointPathsGraphBidirectional();

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5, 5);

        assertEquals(3, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 4, 5), 5);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 5), 7);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP3 =
            new GraphWalk<>(graph, Arrays.asList(1, 3, 5), 9);
        assertEquals(expectedP3, pathList.get(2));
        assertEquals(2, pathList.get(2).getLength());
        assertEquals(9.0, pathList.get(2).getWeight(), 0.0);

    }

    @Test
    public void testMaximumKPathsAreReturned()
    {
        Graph<Integer, DefaultWeightedEdge> graph = createThreeDisjointPathsGraph();

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5, 1);
        assertEquals(1, pathList.size());

        pathList = alg.getPaths(1, 5, 2);
        assertEquals(2, pathList.size());

        pathList = alg.getPaths(1, 5, 3);
        assertEquals(3, pathList.size());
    }

    /**
     * Tests that sequential calls return the same result.
     */
    @Test
    public void testSequentialCallsSanity()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        DefaultWeightedEdge edge = graph.addEdge(1, 2);
        graph.setEdgeWeight(edge, 8);
        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList_1 = alg.getPaths(1, 2, 5);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList_2 = alg.getPaths(1, 2, 5);

        assertEquals(pathList_1, pathList_2);

    }

    private Graph<Integer, DefaultWeightedEdge> createThreeDisjointPathsGraph()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e25 = graph.addEdge(2, 5);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        DefaultWeightedEdge e35 = graph.addEdge(3, 5);
        DefaultWeightedEdge e14 = graph.addEdge(1, 4);
        DefaultWeightedEdge e45 = graph.addEdge(4, 5);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);

        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e25, 6);
        graph.setEdgeWeight(e13, 4);
        graph.setEdgeWeight(e35, 5);
        graph.setEdgeWeight(e14, 4);
        graph.setEdgeWeight(e45, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);

        return graph;
    }

    private Graph<Integer, DefaultWeightedEdge> createThreeDisjointPathsGraphBidirectional()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e21 = graph.addEdge(2, 1);

        DefaultWeightedEdge e25 = graph.addEdge(2, 5);
        DefaultWeightedEdge e52 = graph.addEdge(5, 2);

        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        DefaultWeightedEdge e31 = graph.addEdge(3, 1);

        DefaultWeightedEdge e35 = graph.addEdge(3, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);

        DefaultWeightedEdge e14 = graph.addEdge(1, 4);
        DefaultWeightedEdge e41 = graph.addEdge(4, 1);

        DefaultWeightedEdge e45 = graph.addEdge(4, 5);
        DefaultWeightedEdge e54 = graph.addEdge(5, 4);

        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e32 = graph.addEdge(3, 2);

        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e43 = graph.addEdge(4, 3);

        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e21, 1);

        graph.setEdgeWeight(e25, 6);
        graph.setEdgeWeight(e52, 6);

        graph.setEdgeWeight(e13, 4);
        graph.setEdgeWeight(e31, 4);

        graph.setEdgeWeight(e35, 5);
        graph.setEdgeWeight(e53, 5);

        graph.setEdgeWeight(e14, 4);
        graph.setEdgeWeight(e41, 4);

        graph.setEdgeWeight(e45, 1);
        graph.setEdgeWeight(e54, 1);

        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e32, 1);

        graph.setEdgeWeight(e34, 1);
        graph.setEdgeWeight(e43, 1);

        return graph;
    }

    private Graph<Integer, DefaultEdge> createUnweightedGraph()
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addVertex(7);
        graph.addVertex(8);

        graph.addEdge(1, 2);
        graph.addEdge(2, 5);
        graph.addEdge(1, 3);
        graph.addEdge(3, 6);
        graph.addEdge(6, 5);
        graph.addEdge(1, 4);
        graph.addEdge(4, 7);
        graph.addEdge(7, 8);
        graph.addEdge(8, 5);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);

        return graph;
    }

    @Test
    public void testThreeDisjointPathsGraphIsNotChanged()
    {
        checkGraphIsNotChanged(createThreeDisjointPathsGraph());
    }

    @Test
    public void testDisconnectedGraphIsNotChanged()
    {
        checkGraphIsNotChanged(createDisconnectedGraph());
    }

    public void checkGraphIsNotChanged(Graph<Integer, DefaultWeightedEdge> source)
    {
        Graph<Integer, DefaultWeightedEdge> destination = new DefaultDirectedWeightedGraph<>(
            source.getVertexSupplier(), source.getEdgeSupplier());
        Graphs.addGraph(destination, source);

        Map<DefaultWeightedEdge, Double> originalWeightMap = new HashMap<>();
        for (DefaultWeightedEdge e : source.edgeSet()) {
            originalWeightMap.put(e, source.getEdgeWeight(e));
        }

        getKShortestPathAlgorithm(source).getPaths(1, 5, 5);

        assertEquals(destination, source);

        Map<DefaultWeightedEdge, Double> weightMap = new HashMap<>();
        for (DefaultWeightedEdge e : source.edgeSet()) {
            weightMap.put(e, source.getEdgeWeight(e));
        }

        assertEquals(originalWeightMap, weightMap);
    }

    @Test
    public void testUnweightedGraphIsNotChanged()
    {
        Graph<Integer, DefaultEdge> source = createUnweightedGraph();
        Graph<Integer, DefaultEdge> destination =
            new DefaultDirectedGraph<>(source.getVertexSupplier(), source.getEdgeSupplier(), false);
        Graphs.addGraph(destination, source);

        Map<DefaultEdge, Double> originalWeightMap = new HashMap<>();
        for (DefaultEdge e : source.edgeSet()) {
            originalWeightMap.put(e, source.getEdgeWeight(e));
        }

        getKShortestPathAlgorithm(source).getPaths(1, 5, 5);

        assertEquals(destination, source);

        Map<DefaultEdge, Double> weightMap = new HashMap<>();
        for (DefaultEdge e : source.edgeSet()) {
            weightMap.put(e, source.getEdgeWeight(e));
        }

        assertEquals(originalWeightMap, weightMap);
    }

    @Test
    public void testUnweightedGraph()
    {
        Graph<Integer, DefaultEdge> graph = createUnweightedGraph();

        KShortestPathAlgorithm<Integer, DefaultEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<Integer, DefaultEdge>> pathList = alg.getPaths(1, 5, 5);

        assertEquals(3, pathList.size());

        GraphPath<Integer, DefaultEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 5), 2);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(2.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 3, 6, 5), 3);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(3.0, pathList.get(1).getWeight(), 0.0);

        GraphPath<Integer, DefaultEdge> expectedP3 =
            new GraphWalk<>(graph, Arrays.asList(1, 4, 7, 8, 5), 4);
        assertEquals(expectedP3, pathList.get(2));
        assertEquals(4, pathList.get(2).getLength());
        assertEquals(4.0, pathList.get(2).getWeight(), 0.0);
    }

    @Test
    public void testWikipediaGraph()
    {
        Graph<String, DefaultWeightedEdge> graph = buildWikipediaGraph();

        KShortestPathAlgorithm<String, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        List<GraphPath<String, DefaultWeightedEdge>> pathList = alg.getPaths("A", "F", 3);

        assertEquals(2, pathList.size());

        GraphPath<String, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList("A", "C", "D", "F"), 5);

        GraphPath<String, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList("A", "B", "E", "F"), 5);

        if (pathList.get(0).equals(expectedP1)) {
            assertEquals(expectedP2, pathList.get(1));
        } else if (pathList.get(0).equals(expectedP2)) {
            assertEquals(expectedP1, pathList.get(1));
        } else {
            fail("Unexpected result");
        }

    }

    private Graph<String, DefaultWeightedEdge> buildWikipediaGraph()
    {
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");

        DefaultWeightedEdge e;
        e = graph.addEdge("A", "B");
        graph.setEdgeWeight(e, 1.0);
        e = graph.addEdge("B", "A");
        graph.setEdgeWeight(e, 1.0);

        e = graph.addEdge("A", "C");
        graph.setEdgeWeight(e, 2.0);
        e = graph.addEdge("C", "A");
        graph.setEdgeWeight(e, 2.0);

        e = graph.addEdge("B", "D");
        graph.setEdgeWeight(e, 1.0);
        e = graph.addEdge("D", "B");
        graph.setEdgeWeight(e, 1.0);

        e = graph.addEdge("B", "E");
        graph.setEdgeWeight(e, 2.0);
        e = graph.addEdge("E", "B");
        graph.setEdgeWeight(e, 2.0);

        e = graph.addEdge("D", "C");
        graph.setEdgeWeight(e, 2.0);
        e = graph.addEdge("C", "D");
        graph.setEdgeWeight(e, 2.0);

        e = graph.addEdge("D", "F");
        graph.setEdgeWeight(e, 1.0);
        e = graph.addEdge("F", "D");
        graph.setEdgeWeight(e, 1.0);

        e = graph.addEdge("E", "F");
        graph.setEdgeWeight(e, 2.0);
        e = graph.addEdge("F", "E");
        graph.setEdgeWeight(e, 2.0);

        return graph;
    }

    /**
     * Only one disjoint path should exist on the line
     */
    @Test
    public void testLinear()
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(
                SupplierUtil.createIntegerSupplier(1),
                SupplierUtil.createDefaultWeightedEdgeSupplier());
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator =
            new LinearGraphGenerator<>(20);
        graphGenerator.generateGraph(graph);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 20, 2);

        assertEquals(1, pathList.size());
        assertEquals(19, pathList.get(0).getLength());
        assertEquals(19.0, pathList.get(0).getWeight(), 0.0);

        for (int i = 1; i < 21; i++) {
            assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }

    /**
     * Exactly one disjoint path should exist on the ring
     */
    @Test
    public void testRing()
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(
                SupplierUtil.createIntegerSupplier(1),
                SupplierUtil.createDefaultWeightedEdgeSupplier());
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator =
            new RingGraphGenerator<>(20);
        graphGenerator.generateGraph(graph);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 10, 2);

        assertEquals(1, pathList.size());
        assertEquals(9, pathList.get(0).getLength());
        assertEquals(9.0, pathList.get(0).getWeight(), 0.0);

        for (int i = 1; i < 10; i++) {
            assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }

    /**
     * Exactly one disjoint path should exist in a clique
     */
    @Test
    public void testClique()
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(
                SupplierUtil.createIntegerSupplier(1),
                SupplierUtil.createDefaultWeightedEdgeSupplier());
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator =
            new CompleteGraphGenerator<>(20);
        graphGenerator.generateGraph(graph);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        for (int i = 2; i < 20; i++) {
            List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, i, 2);
            assertEquals(2, pathList.size());
        }
    }

    /**
     * Exactly one disjoint path should exist is a star graph.
     */
    @Test
    public void testStar()
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(
                SupplierUtil.createIntegerSupplier(1),
                SupplierUtil.createDefaultWeightedEdgeSupplier());
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator =
            new StarGraphGenerator<>(20);
        graphGenerator.generateGraph(graph);

        KShortestPathAlgorithm<Integer, DefaultWeightedEdge> alg = getKShortestPathAlgorithm(graph);

        for (int i = 2; i < 20; i++) {
            List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(i, 1, 2);
            assertEquals(1, pathList.size());
        }
    }

    protected abstract <V,
        E> KShortestPathAlgorithm<V, E> getKShortestPathAlgorithm(Graph<V, E> graph);

}
