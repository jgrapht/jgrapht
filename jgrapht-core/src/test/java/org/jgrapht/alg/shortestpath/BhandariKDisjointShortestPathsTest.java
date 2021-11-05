/*
 * (C) Copyright 2018-2021, by Assaf Mizrachi and Contributors.
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

import static org.junit.Assert.assertEquals;

/**
 * 
 * Tests for the {@link BhandariKDisjointShortestPaths} class.
 * 
 * @author Assaf Mizrachi
 */
public class BhandariKDisjointShortestPathsTest
    extends
    KDisjointShortestPathsTestCase
{

    /**
     * Tests two joint paths from 1 to 4, negative edges exist in path.
     * 
     * Edges expected in path 1 --------------- {@literal 1 --> 2}, w=-1 {@literal 2 --> 6}, w=-3
     * {@literal 6 --> 4}, w= 3
     * 
     * Edges expected in path 2 --------------- {@literal 1 --> 5}, w=-2 {@literal 5 --> 3}, w= 2
     * {@literal 3 --> 4}, w=-1
     * 
     * Edges expected in no path --------------- {@literal 2 --> 3}, w=-1
     * 
     */
    @Test
    public void testTwoDisjointPathsNegative()
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

        graph.setEdgeWeight(e12, -20);
        graph.setEdgeWeight(e23, -1);
        graph.setEdgeWeight(e34, -10);
        graph.setEdgeWeight(e15, -2);
        graph.setEdgeWeight(e53, 2);
        graph.setEdgeWeight(e26, -3);
        graph.setEdgeWeight(e64, 3);

        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg =
            new BhandariKDisjointShortestPaths<>(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4, 5);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 6, 4), -20);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(-20.0, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 5, 3, 4), -10);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(-10.0, pathList.get(1).getWeight(), 0.0);
    }

    @Override
    protected <V, E> KShortestPathAlgorithm<V, E> getKShortestPathAlgorithm(Graph<V, E> graph)
    {
        return new BhandariKDisjointShortestPaths<>(graph);
    }
}
