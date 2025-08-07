/*
 * (C) Copyright 2018-2023, by Assaf Mizrachi and Contributors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

/**
 * 
 * Tests for the {@link SuurballeKDisjointShortestPaths} class.
 * 
 * @author Assaf Mizrachi
 */
public class SuurballeKDisjointShortestPathsTest
    extends KDisjointShortestPathsTestCase
{

    @Override
    protected <V, E> KShortestPathAlgorithm<V, E> getKShortestPathAlgorithm(Graph<V, E> graph)
    {
        return new SuurballeKDisjointShortestPaths<>(graph);
    }

    @Test
    public void testTwoDisjointPathsInGraphContainingFractionalDoublelEdgeWeight()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);

        graph.setEdgeWeight(e12, 1.0);
        graph.setEdgeWeight(e13, 2.0);
        graph.setEdgeWeight(e23, 0.9);

        SuurballeKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg =
            new SuurballeKDisjointShortestPaths<Integer, DefaultWeightedEdge>(graph);

        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 3, 2);

        assertEquals(2, pathList.size());

        GraphPath<Integer, DefaultWeightedEdge> expectedP1 =
            new GraphWalk<>(graph, Arrays.asList(1, 2, 3), 1);
        assertEquals(expectedP1, pathList.get(0));
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(1.9, pathList.get(0).getWeight(), 0.0);

        GraphPath<Integer, DefaultWeightedEdge> expectedP2 =
            new GraphWalk<>(graph, Arrays.asList(1, 3), 2);
        assertEquals(expectedP2, pathList.get(1));
        assertEquals(1, pathList.get(1).getLength());
        assertEquals(2.0, pathList.get(1).getWeight(), 0.0);
    }

}
