/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Testing the class GraphOrdering
 */
public class GraphOrderingTest
{

    @Test
    public void testUndirectedGraph()
    {
        /*
         * v1--v2 |\ | v5 | \ | v3 v4
         *
         */
        Graph<String, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);

        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4", v5 = "v5";

        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        g1.addVertex(v5);

        g1.addEdge(v1, v2);
        g1.addEdge(v1, v3);
        g1.addEdge(v1, v4);
        g1.addEdge(v2, v4);

        GraphOrdering<String, DefaultEdge> g1Ordering = new GraphOrdering<>(g1);

        assertEquals(5, g1Ordering.getVertexCount());

        int v1o = g1Ordering.getVertexNumber(v1), v2o = g1Ordering.getVertexNumber(v2),
            v3o = g1Ordering.getVertexNumber(v3), v4o = g1Ordering.getVertexNumber(v4),
            v5o = g1Ordering.getVertexNumber(v5);

        int[] v1Outs = { v2o, v3o, v4o };
        int[] v1Outs_ = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1Outs);
        Arrays.sort(v1Outs_);

        int[] v2Outs = { v1o, v4o };
        int[] v2Outs_ = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2Outs);
        Arrays.sort(v2Outs_);

        int[] v3Outs = { v1o };
        int[] v3Outs_ = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3Outs);
        Arrays.sort(v3Outs_);

        int[] v4Outs = { v1o, v2o };
        int[] v4Outs_ = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4Outs);
        Arrays.sort(v4Outs_);

        int[] v5Outs = {};
        int[] v5Outs_ = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5Outs);
        Arrays.sort(v5Outs_);

        assertArrayEquals(v1Outs, v1Outs_);
        assertArrayEquals(v2Outs, v2Outs_);
        assertArrayEquals(v3Outs, v3Outs_);
        assertArrayEquals(v4Outs, v4Outs_);
        assertArrayEquals(v5Outs, v5Outs_);

        int[] v1Ins = { v2o, v3o, v4o };
        int[] v1Ins_ = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1Ins);
        Arrays.sort(v1Ins_);

        int[] v2Ins = { v1o, v4o };
        int[] v2Ins_ = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2Ins);
        Arrays.sort(v2Ins_);

        int[] v3Ins = { v1o };
        int[] v3Ins_ = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3Ins);
        Arrays.sort(v3Ins_);

        int[] v4Ins = { v1o, v2o };
        int[] v4Ins_ = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4Ins);
        Arrays.sort(v4Ins_);

        int[] v5Ins = {};
        int[] v5Ins_ = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5Ins);
        Arrays.sort(v5Ins_);

        assertArrayEquals(v1Ins, v1Ins_);
        assertArrayEquals(v2Ins, v2Ins_);
        assertArrayEquals(v3Ins, v3Ins_);
        assertArrayEquals(v4Ins, v4Ins_);
        assertArrayEquals(v5Ins, v5Ins_);

        assertEquals(false, g1Ordering.hasEdge(v1o, v1o));
        assertEquals(true, g1Ordering.hasEdge(v1o, v2o));
        assertEquals(true, g1Ordering.hasEdge(v1o, v3o));
        assertEquals(true, g1Ordering.hasEdge(v1o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v1o, v5o));
        assertEquals(true, g1Ordering.hasEdge(v2o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v3o));
        assertEquals(true, g1Ordering.hasEdge(v2o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v5o));
        assertEquals(true, g1Ordering.hasEdge(v3o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v5o));
        assertEquals(true, g1Ordering.hasEdge(v4o, v1o));
        assertEquals(true, g1Ordering.hasEdge(v4o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v5o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v5o));
    }

    @Test
    public void testDirectedGraph()
    {
        /*
         * v1 ---> v2 <---> v3 ---> v4 v5
         *
         */
        Graph<String, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);

        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4", v5 = "v5";

        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        g1.addVertex(v5);

        g1.addEdge(v1, v2);
        g1.addEdge(v2, v3);
        g1.addEdge(v3, v2);
        g1.addEdge(v3, v4);

        GraphOrdering<String, DefaultEdge> g1Ordering = new GraphOrdering<>(g1);

        assertEquals(5, g1Ordering.getVertexCount());

        int v1o = g1Ordering.getVertexNumber(v1), v2o = g1Ordering.getVertexNumber(v2),
            v3o = g1Ordering.getVertexNumber(v3), v4o = g1Ordering.getVertexNumber(v4),
            v5o = g1Ordering.getVertexNumber(v5);

        int[] v1Outs = { v2o };
        int[] v1Outs_ = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1Outs);
        Arrays.sort(v1Outs_);

        int[] v2Outs = { v3o };
        int[] v2Outs_ = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2Outs);
        Arrays.sort(v2Outs_);

        int[] v3Outs = { v2o, v4o };
        int[] v3Outs_ = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3Outs);
        Arrays.sort(v3Outs_);

        int[] v4Outs = {};
        int[] v4Outs_ = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4Outs);
        Arrays.sort(v4Outs_);

        int[] v5Outs = {};
        int[] v5Outs_ = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5Outs);
        Arrays.sort(v5Outs_);

        assertArrayEquals(v1Outs, v1Outs_);
        assertArrayEquals(v2Outs, v2Outs_);
        assertArrayEquals(v3Outs, v3Outs_);
        assertArrayEquals(v4Outs, v4Outs_);
        assertArrayEquals(v5Outs, v5Outs_);

        int[] v1Ins = {};
        int[] v1Ins_ = g1Ordering.getInEdges(v1o);
        Arrays.sort(v1Ins);
        Arrays.sort(v1Ins_);

        int[] v2Ins = { v1o, v3o };
        int[] v2Ins_ = g1Ordering.getInEdges(v2o);
        Arrays.sort(v2Ins);
        Arrays.sort(v2Ins_);

        int[] v3Ins = { v2o };
        int[] v3Ins_ = g1Ordering.getInEdges(v3o);
        Arrays.sort(v3Ins);
        Arrays.sort(v3Ins_);

        int[] v4Ins = { v3o };
        int[] v4Ins_ = g1Ordering.getInEdges(v4o);
        Arrays.sort(v4Ins);
        Arrays.sort(v4Ins_);

        int[] v5Ins = {};
        int[] v5Ins_ = g1Ordering.getInEdges(v5o);
        Arrays.sort(v5Ins);
        Arrays.sort(v5Ins_);

        assertArrayEquals(v1Ins, v1Ins_);
        assertArrayEquals(v2Ins, v2Ins_);
        assertArrayEquals(v3Ins, v3Ins_);
        assertArrayEquals(v4Ins, v4Ins_);
        assertArrayEquals(v5Ins, v5Ins_);

        assertEquals(false, g1Ordering.hasEdge(v1o, v1o));
        assertEquals(true, g1Ordering.hasEdge(v1o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v1o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v1o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v1o, v5o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v2o));
        assertEquals(true, g1Ordering.hasEdge(v2o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v2o, v5o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v1o));
        assertEquals(true, g1Ordering.hasEdge(v3o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v3o));
        assertEquals(true, g1Ordering.hasEdge(v3o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v3o, v5o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v4o, v5o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v1o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v2o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v3o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v4o));
        assertEquals(false, g1Ordering.hasEdge(v5o, v5o));
    }
}
