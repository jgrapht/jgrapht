/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Dimitrios Michail
 */
public class GreedyMultiplicativeSpannerTest
{

    // ~ Static fields/initializers
    // ---------------------------------------------
    private static final String V0 = "v0";
    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";
    private static final String V8 = "v8";
    private static final String V9 = "v9";
    private static final String V10 = "v10";
    private static final String V11 = "v11";
    private static final String V12 = "v12";
    private static final String V13 = "v13";
    private static final String V14 = "v14";
    private static final String V15 = "v15";

    // ~ Methods
    // ----------------------------------------------------------------
    public void createGraph1(Graph<String, DefaultEdge> g)
    {
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);

        g.addEdge(V0, V1);
        g.addEdge(V1, V2);
        g.addEdge(V0, V5);
        g.addEdge(V1, V5);
        g.addEdge(V1, V4);
        g.addEdge(V2, V4);
        g.addEdge(V2, V3);
        g.addEdge(V5, V4);
        g.addEdge(V4, V3);
        g.addEdge(V5, V6);
        g.addEdge(V4, V6);
        g.addEdge(V3, V6);
        g.addEdge(V3, V7);
        g.addEdge(V6, V7);
    }

    public void createGraph2(Graph<String, DefaultEdge> g)
    {
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);
        g.addVertex(V9);
        g.addVertex(V10);
        g.addVertex(V11);
        g.addVertex(V12);
        g.addVertex(V13);
        g.addVertex(V14);
        g.addVertex(V15);

        g.addEdge(V0, V1);
        g.addEdge(V0, V3);
        g.addEdge(V0, V2);
        g.addEdge(V1, V3);
        g.addEdge(V1, V2);
        g.addEdge(V3, V2);
        g.addEdge(V3, V4);
        g.addEdge(V2, V5);
        g.addEdge(V4, V5);
        g.addEdge(V4, V6);
        g.addEdge(V5, V7);
        g.addEdge(V6, V7);

        g.addEdge(V8, V9);
        g.addEdge(V8, V10);
        g.addEdge(V8, V11);
        g.addEdge(V9, V10);
        g.addEdge(V9, V11);
        g.addEdge(V10, V11);
        g.addEdge(V10, V12);
        g.addEdge(V11, V13);
        g.addEdge(V12, V14);
        g.addEdge(V13, V15);
    }

    // ~ Methods
    // ----------------------------------------------------------------
    public void createGraph3(Graph<String, DefaultWeightedEdge> g)
    {
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);

        g.setEdgeWeight(g.addEdge(V0, V1), 3.0);
        g.setEdgeWeight(g.addEdge(V1, V2), 15.0);
        g.setEdgeWeight(g.addEdge(V0, V5), 5.0);
        g.setEdgeWeight(g.addEdge(V1, V5), 20.0);
        g.setEdgeWeight(g.addEdge(V1, V4), 100.0);
        g.setEdgeWeight(g.addEdge(V2, V4), 5.0);
        g.setEdgeWeight(g.addEdge(V2, V3), 10.0);
        g.setEdgeWeight(g.addEdge(V5, V4), 1.0);
        g.setEdgeWeight(g.addEdge(V4, V3), 100.0);
        g.setEdgeWeight(g.addEdge(V5, V6), 10.0);
        g.setEdgeWeight(g.addEdge(V4, V6), 20.0);
        g.setEdgeWeight(g.addEdge(V3, V6), 100.0);
        g.setEdgeWeight(g.addEdge(V3, V7), 1000.0);
        g.setEdgeWeight(g.addEdge(V6, V7), 5.0);
    }

    private <V, E> void runTest(Graph<V, E> g, int k, Set<E> correct)
    {
        Set<E> result = new GreedyMultiplicativeSpanner<>(g, k).getSpanner();

        assertEquals(correct.size(), result.size());
        for (E e : correct) {
            assertTrue(result.contains(e));
        }
    }

    @Test
    public void testGraph1()
    {
        Graph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        createGraph1(g);

        // test 3-spanner using k = 2
        Set<DefaultEdge> spanner3 = new HashSet<>();
        spanner3.add(g.getEdge(V0, V1));
        spanner3.add(g.getEdge(V1, V2));
        spanner3.add(g.getEdge(V0, V5));
        spanner3.add(g.getEdge(V1, V4));
        spanner3.add(g.getEdge(V2, V3));
        spanner3.add(g.getEdge(V5, V6));
        spanner3.add(g.getEdge(V4, V6));
        spanner3.add(g.getEdge(V3, V6));
        spanner3.add(g.getEdge(V3, V7));

        runTest(g, 2, spanner3);

        // test 5-spanner using k = 3
        Set<DefaultEdge> spanner5 = new HashSet<>();
        spanner5.add(g.getEdge(V0, V1));
        spanner5.add(g.getEdge(V1, V2));
        spanner5.add(g.getEdge(V0, V5));
        spanner5.add(g.getEdge(V1, V4));
        spanner5.add(g.getEdge(V2, V3));
        spanner5.add(g.getEdge(V5, V6));
        spanner5.add(g.getEdge(V3, V7));
        spanner5.add(g.getEdge(V6, V7));

        runTest(g, 3, spanner5);

        // test 7-spanner using k = 4
        Set<DefaultEdge> spanner7 = new HashSet<>();
        spanner7.add(g.getEdge(V0, V1));
        spanner7.add(g.getEdge(V1, V2));
        spanner7.add(g.getEdge(V0, V5));
        spanner7.add(g.getEdge(V1, V4));
        spanner7.add(g.getEdge(V2, V3));
        spanner7.add(g.getEdge(V5, V6));
        spanner7.add(g.getEdge(V3, V7));

        runTest(g, 4, spanner7);

        // test minimum spanning tree using large k
        runTest(g, 100, spanner7);
    }

    @Test
    public void testGraph1WithLoops()
    {
        Graph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        createGraph1(g);

        g.addEdge(V0, V0);
        g.addEdge(V1, V1);
        g.addEdge(V2, V2);

        // test 3-spanner using k = 2
        Set<DefaultEdge> spanner3 = new HashSet<>();
        spanner3.add(g.getEdge(V0, V1));
        spanner3.add(g.getEdge(V1, V2));
        spanner3.add(g.getEdge(V0, V5));
        spanner3.add(g.getEdge(V1, V4));
        spanner3.add(g.getEdge(V2, V3));
        spanner3.add(g.getEdge(V5, V6));
        spanner3.add(g.getEdge(V4, V6));
        spanner3.add(g.getEdge(V3, V6));
        spanner3.add(g.getEdge(V3, V7));

        runTest(g, 2, spanner3);
    }

    @Test
    public void testGraph1WithMultipleEdges()
    {
        Graph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        createGraph1(g);

        g.addEdge(V0, V1);
        g.addEdge(V1, V2);
        g.addEdge(V0, V5);

        // test 3-spanner using k = 2
        Set<DefaultEdge> spanner3 = new HashSet<>();
        spanner3.add(g.getEdge(V0, V1));
        spanner3.add(g.getEdge(V1, V2));
        spanner3.add(g.getEdge(V0, V5));
        spanner3.add(g.getEdge(V1, V4));
        spanner3.add(g.getEdge(V2, V3));
        spanner3.add(g.getEdge(V5, V6));
        spanner3.add(g.getEdge(V4, V6));
        spanner3.add(g.getEdge(V3, V6));
        spanner3.add(g.getEdge(V3, V7));

        runTest(g, 2, spanner3);
    }

    @Test
    public void testGraph2()
    {
        Graph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        createGraph2(g);

        // test 3-spanner using k = 2
        Set<DefaultEdge> spanner3 = new HashSet<>();
        spanner3.add(g.getEdge(V0, V1));
        spanner3.add(g.getEdge(V0, V3));
        spanner3.add(g.getEdge(V0, V2));
        spanner3.add(g.getEdge(V3, V4));
        spanner3.add(g.getEdge(V2, V5));
        spanner3.add(g.getEdge(V4, V5));
        spanner3.add(g.getEdge(V4, V6));
        spanner3.add(g.getEdge(V5, V7));
        spanner3.add(g.getEdge(V8, V9));
        spanner3.add(g.getEdge(V8, V10));
        spanner3.add(g.getEdge(V8, V11));
        spanner3.add(g.getEdge(V10, V12));
        spanner3.add(g.getEdge(V11, V13));
        spanner3.add(g.getEdge(V12, V14));
        spanner3.add(g.getEdge(V13, V15));

        runTest(g, 2, spanner3);

        // test 5-spanner using k = 3
        Set<DefaultEdge> spanner5 = new HashSet<>();
        spanner5.add(g.getEdge(V0, V1));
        spanner5.add(g.getEdge(V0, V3));
        spanner5.add(g.getEdge(V0, V2));
        spanner5.add(g.getEdge(V3, V4));
        spanner5.add(g.getEdge(V2, V5));
        spanner5.add(g.getEdge(V4, V6));
        spanner5.add(g.getEdge(V5, V7));
        spanner5.add(g.getEdge(V6, V7));
        spanner5.add(g.getEdge(V8, V9));
        spanner5.add(g.getEdge(V8, V10));
        spanner5.add(g.getEdge(V8, V11));
        spanner5.add(g.getEdge(V10, V12));
        spanner5.add(g.getEdge(V11, V13));
        spanner5.add(g.getEdge(V12, V14));
        spanner5.add(g.getEdge(V13, V15));

        runTest(g, 3, spanner5);

        // test 7-spanner using k = 4
        Set<DefaultEdge> spanner7 = new HashSet<>();
        spanner7.add(g.getEdge(V0, V1));
        spanner7.add(g.getEdge(V0, V3));
        spanner7.add(g.getEdge(V0, V2));
        spanner7.add(g.getEdge(V3, V4));
        spanner7.add(g.getEdge(V2, V5));
        spanner7.add(g.getEdge(V4, V6));
        spanner7.add(g.getEdge(V5, V7));
        spanner7.add(g.getEdge(V8, V9));
        spanner7.add(g.getEdge(V8, V10));
        spanner7.add(g.getEdge(V8, V11));
        spanner7.add(g.getEdge(V10, V12));
        spanner7.add(g.getEdge(V11, V13));
        spanner7.add(g.getEdge(V12, V14));
        spanner7.add(g.getEdge(V13, V15));

        runTest(g, 4, spanner7);

        // test minimum spanning tree using large k
        runTest(g, 100, spanner7);
    }

    @Test
    public void testGraph3()
    {
        WeightedPseudograph<String, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        createGraph3(g);

        // test 3-spanner using k = 2
        Set<DefaultWeightedEdge> spanner3 = new HashSet<>();
        spanner3.add(g.getEdge(V5, V4));
        spanner3.add(g.getEdge(V0, V1));
        spanner3.add(g.getEdge(V0, V5));
        spanner3.add(g.getEdge(V2, V4));
        spanner3.add(g.getEdge(V2, V3));
        spanner3.add(g.getEdge(V5, V6));
        spanner3.add(g.getEdge(V6, V7));

        runTest(g, 2, spanner3);

        // compute minimum-spanning-tree
        runTest(g, Integer.MAX_VALUE, spanner3);
    }

    @Test
    public void testNegativeWeightsGraph()
    {
        WeightedPseudograph<String, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);

        g.setEdgeWeight(g.addEdge(V0, V1), 1.0);
        g.setEdgeWeight(g.addEdge(V1, V2), -1.0);
        g.setEdgeWeight(g.addEdge(V2, V0), 1.0);

        try {
            new GreedyMultiplicativeSpanner<>(g, 2).getSpanner();
            fail("Negative edge weights not permitted.");
        } catch (IllegalArgumentException e) {
        }
    }

}
