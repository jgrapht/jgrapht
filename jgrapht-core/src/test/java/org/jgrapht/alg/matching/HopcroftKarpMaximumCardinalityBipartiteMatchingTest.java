/*
 * (C) Copyright 2012-2026, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the HopcroftKarpMaximumCardinalityBipartiteMatching class
 *
 * @author Joris Kinable
 *
 */
public class HopcroftKarpMaximumCardinalityBipartiteMatchingTest extends MaximumCardinalityBipartiteMatchingTest
{
    @Test
    public void testMatchingWeightWithNonUnitEdgeWeights()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 0, 1, -1.5);
        Graphs.addEdgeWithVertices(graph, 2, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 4, 5, 4.25);

        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching =
            new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, Set.of(0, 2, 4), Set.of(1, 3, 5)).getMatching();

        assertEquals(3, matching.getEdges().size());
        assertEquals(graph.edgeSet(), matching.getEdges());
        assertEquals(2.75, matching.getWeight(), 1e-9);
    }


    @Override
    public MatchingAlgorithm<Integer, DefaultEdge> getMatchingAlgorithm(
        Graph<Integer, DefaultEdge> graph, Set<Integer> partition1, Set<Integer> partition2)
    {
        return new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, partition1, partition2);
    }
}
