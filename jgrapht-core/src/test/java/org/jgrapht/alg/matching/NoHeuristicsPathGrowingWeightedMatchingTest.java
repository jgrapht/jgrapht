/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the PathGrowingWeightedMatching without heuristics algorithm
 * 
 * @author Dimitrios Michail
 */
public class NoHeuristicsPathGrowingWeightedMatchingTest
    extends
    BasePathGrowingWeightedMatchingTest
{

    @Override
    public MatchingAlgorithm<Integer, DefaultWeightedEdge> getApproximationAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new PathGrowingWeightedMatching<>(graph, false);
    };

    @Override
    @Test
    public void testGraph1()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, IntStream.range(0, 15).boxed().collect(Collectors.toList()));
        Graphs.addEdge(g, 0, 1, 5.0);
        Graphs.addEdge(g, 1, 2, 2.5);
        Graphs.addEdge(g, 2, 3, 5.0);
        Graphs.addEdge(g, 3, 4, 2.5);
        Graphs.addEdge(g, 4, 0, 2.5);
        Graphs.addEdge(g, 0, 13, 2.5);
        Graphs.addEdge(g, 13, 14, 5.0);
        Graphs.addEdge(g, 1, 11, 2.5);
        Graphs.addEdge(g, 11, 12, 5.0);
        Graphs.addEdge(g, 2, 9, 2.5);
        Graphs.addEdge(g, 9, 10, 5.0);
        Graphs.addEdge(g, 3, 7, 2.5);
        Graphs.addEdge(g, 7, 8, 5.0);
        Graphs.addEdge(g, 4, 5, 2.5);
        Graphs.addEdge(g, 5, 6, 5.0);

        MatchingAlgorithm<Integer, DefaultWeightedEdge> mm = getApproximationAlgorithm(g);
        Matching<Integer, DefaultWeightedEdge> m = mm.getMatching();

        assertEquals(5, m.getEdges().size());
        assertEquals(22.5, m.getWeight(), MatchingAlgorithm.DEFAULT_EPSILON);
        assertTrue(isMatching(g, m));
    }

    @Override
    @Test
    public void testSelfLoops()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));
        Graphs.addEdge(g, 0, 1, 1.0);
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 3, 1.0);
        Graphs.addEdge(g, 3, 0, 1.0);
        Graphs.addAllVertices(g, Arrays.asList(4, 5, 6, 7));
        Graphs.addEdge(g, 4, 5, 1.0);
        Graphs.addEdge(g, 5, 6, 1.0);
        Graphs.addEdge(g, 6, 7, 1.0);
        Graphs.addEdge(g, 7, 4, 1.0);

        // add self loops
        Graphs.addEdge(g, 0, 0, 100.0);
        Graphs.addEdge(g, 1, 1, 200.0);
        Graphs.addEdge(g, 2, 2, -200.0);
        Graphs.addEdge(g, 3, 3, -100.0);
        Graphs.addEdge(g, 4, 4, 0.0);

        MatchingAlgorithm<Integer, DefaultWeightedEdge> mm = getApproximationAlgorithm(g);
        Matching<Integer, DefaultWeightedEdge> m = mm.getMatching();

        assertEquals(3, m.getEdges().size());
        assertEquals(3.0, m.getWeight(), MatchingAlgorithm.DEFAULT_EPSILON);
        assertTrue(isMatching(g, m));
    }

    @Override
    @Test
    public void test3over4Approximation()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));
        Graphs.addEdge(g, 0, 1, 1.0);
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 3, 1.0);
        Graphs.addEdge(g, 3, 0, 1.0);
        Graphs.addAllVertices(g, Arrays.asList(4, 5, 6, 7));
        Graphs.addEdge(g, 4, 5, 1.0);
        Graphs.addEdge(g, 5, 6, 1.0);
        Graphs.addEdge(g, 6, 7, 1.0);
        Graphs.addEdge(g, 7, 4, 1.0);

        MatchingAlgorithm<Integer, DefaultWeightedEdge> mm =
            new PathGrowingWeightedMatching<>(g, false);
        Matching<Integer, DefaultWeightedEdge> m = mm.getMatching();

        // maximum here is 4.0
        // path growing algorithm gets 3.0
        assertEquals(3, m.getEdges().size());
        assertEquals(3.0, m.getWeight(), MatchingAlgorithm.DEFAULT_EPSILON);
        assertTrue(isMatching(g, m));
    }

    @Override
    @Test
    public void testMultiGraph()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));
        Graphs.addEdge(g, 0, 1, 1.0);
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 3, 1.0);
        Graphs.addEdge(g, 3, 0, 1.0);
        Graphs.addAllVertices(g, Arrays.asList(4, 5, 6, 7));
        Graphs.addEdge(g, 4, 5, 1.0);
        Graphs.addEdge(g, 5, 6, 1.0);
        Graphs.addEdge(g, 6, 7, 1.0);
        Graphs.addEdge(g, 7, 4, 1.0);

        // add multiple edges
        Graphs.addEdge(g, 0, 1, 2.0);
        Graphs.addEdge(g, 1, 2, 2.0);
        Graphs.addEdge(g, 2, 3, 2.0);
        Graphs.addEdge(g, 3, 0, 2.0);
        Graphs.addEdge(g, 4, 5, 2.0);
        Graphs.addEdge(g, 5, 6, 2.0);
        Graphs.addEdge(g, 6, 7, 2.0);
        Graphs.addEdge(g, 7, 4, 2.0);

        MatchingAlgorithm<Integer, DefaultWeightedEdge> mm =
            new PathGrowingWeightedMatching<>(g, false);
        Matching<Integer, DefaultWeightedEdge> m = mm.getMatching();

        // maximum here is 8.0
        // path growing algorithm gets 6.0
        assertEquals(3, m.getEdges().size());
        assertEquals(6.0, m.getWeight(), MatchingAlgorithm.DEFAULT_EPSILON);
        assertTrue(isMatching(g, m));
    }

    @Override
    @Test
    public void testDirected()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));
        Graphs.addEdge(g, 0, 1, 1.0);
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 3, 1.0);
        Graphs.addEdge(g, 3, 0, 1.0);
        Graphs.addAllVertices(g, Arrays.asList(4, 5, 6, 7));
        Graphs.addEdge(g, 4, 5, 1.0);
        Graphs.addEdge(g, 5, 6, 1.0);
        Graphs.addEdge(g, 6, 7, 1.0);
        Graphs.addEdge(g, 7, 4, 1.0);

        // add multiple edges
        Graphs.addEdge(g, 0, 1, 2.0);
        Graphs.addEdge(g, 1, 2, 2.0);
        Graphs.addEdge(g, 2, 3, 2.0);
        Graphs.addEdge(g, 3, 0, 2.0);
        Graphs.addEdge(g, 4, 5, 2.0);
        Graphs.addEdge(g, 5, 6, 2.0);
        Graphs.addEdge(g, 6, 7, 2.0);
        Graphs.addEdge(g, 7, 4, 2.0);

        MatchingAlgorithm<Integer, DefaultWeightedEdge> mm =
            new PathGrowingWeightedMatching<>(g, false);
        Matching<Integer, DefaultWeightedEdge> m = mm.getMatching();

        // maximum here is 8.0
        // path growing algorithm gets 6.0
        assertEquals(3, m.getEdges().size());
        assertEquals(6.0, m.getWeight(), MatchingAlgorithm.DEFAULT_EPSILON);
        assertTrue(isMatching(g, m));
    }

}
