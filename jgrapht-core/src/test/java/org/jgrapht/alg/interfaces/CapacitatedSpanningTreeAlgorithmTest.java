/*
 * (C) Copyright 2026-2026, by Jaehong Park and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.CapacitatedSpanningTreeAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for capacitated spanning tree validation.
 *
 * @author Jaehong Park
 */
public class CapacitatedSpanningTreeAlgorithmTest
{
    @ParameterizedTest
    @CsvSource({ "0.75, 1.0", "2147483647.0, 2147483647.0" })
    public void testNonIntegerCapacitySum(double demand, double capacity)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 0, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Map<Integer, Double> demands = Map.of(1, demand, 2, demand);
        CapacitatedSpanningTree<Integer, DefaultEdge> tree = new CapacitatedSpanningTreeImpl<>(
            Map.of(1, 0, 2, 0), Map.of(0, Pair.of(Set.of(1, 2), 2 * demand)),
            graph.edgeSet(), 2.0);

        assertFalse(tree.isCapacitatedSpanningTree(graph, 0, capacity, demands));
        assertTrue(tree.isCapacitatedSpanningTree(graph, 0, 2 * demand, demands));
    }
}
