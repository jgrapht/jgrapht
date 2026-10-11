/*
 * (C) Copyright 2026-2026, by curforever and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.time.*;
import java.util.*;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for rounding of edge-pair changes in 2-opt moves.
 */
public class TwoOptHeuristicTSPFloatingPointTest
{
    @ParameterizedTest
    @CsvSource({ "1, false", "1e16, false", "1e30, false", "1.8e307, false", "1, true",
        "1e16, true", "1e30, true", "1.8e307, true" })
    public void testEquivalentMovesDoNotCycle(double largeWeight, boolean constructTour)
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < 4; i++) {
            graph.addVertex(i);
        }
        double[][] weights =
            { { 0, largeWeight, largeWeight, 0 }, { largeWeight, 0, largeWeight, 1 },
                { largeWeight, largeWeight, 0, 1 }, { 0, 1, 1, 0 } };
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                graph.setEdgeWeight(graph.addEdge(i, j), weights[i][j]);
            }
        }
        GraphPath<Integer, DefaultWeightedEdge> initial =
            new GraphWalk<>(graph, List.of(0, 1, 2, 3, 0), 2 * largeWeight + 1);
        GraphPath<Integer, DefaultWeightedEdge> improved =
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                TwoOptHeuristicTSP<Integer, DefaultWeightedEdge> algorithm =
                    new TwoOptHeuristicTSP<>(g -> initial);
                return constructTour ? algorithm.getTour(graph) : algorithm.improveTour(initial);
            });
        assertHamiltonian(graph, improved);
        assertEquals(initial.getWeight(), improved.getWeight());
    }
}
