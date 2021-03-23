/*
 * (C) Copyright 2015-2021, by Fabian Späh and Contributors.
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

        int[] v1OutsExpected = { v2o, v3o, v4o };
        int[] v1Outs = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1OutsExpected);
        Arrays.sort(v1Outs);

        int[] v2OutsExpected = { v1o, v4o };
        int[] v2Outs = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2OutsExpected);
        Arrays.sort(v2Outs);

        int[] v3OutsExpected = { v1o };
        int[] v3Outs = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3OutsExpected);
        Arrays.sort(v3Outs);

        int[] v4OutsExpected = { v1o, v2o };
        int[] v4Outs = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4OutsExpected);
        Arrays.sort(v4Outs);

        int[] v5OutsExpected = {};
        int[] v5Outs = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5OutsExpected);
        Arrays.sort(v5Outs);

        assertArrayEquals(v1OutsExpected, v1Outs);
        assertArrayEquals(v2OutsExpected, v2Outs);
        assertArrayEquals(v3OutsExpected, v3Outs);
        assertArrayEquals(v4OutsExpected, v4Outs);
        assertArrayEquals(v5OutsExpected, v5Outs);

        int[] v1InsExpected = { v2o, v3o, v4o };
        int[] v1Ins = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1InsExpected);
        Arrays.sort(v1Ins);

        int[] v2InsExpected = { v1o, v4o };
        int[] v2Ins = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2InsExpected);
        Arrays.sort(v2Ins);

        int[] v3InsExpected = { v1o };
        int[] v3Ins = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3InsExpected);
        Arrays.sort(v3Ins);

        int[] v4InsExpected = { v1o, v2o };
        int[] v4Ins = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4InsExpected);
        Arrays.sort(v4Ins);

        int[] v5InsExpected = {};
        int[] v5Ins = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5InsExpected);
        Arrays.sort(v5Ins);

        assertArrayEquals(v1InsExpected, v1Ins);
        assertArrayEquals(v2InsExpected, v2Ins);
        assertArrayEquals(v3InsExpected, v3Ins);
        assertArrayEquals(v4InsExpected, v4Ins);
        assertArrayEquals(v5InsExpected, v5Ins);

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


        int[] v1OutsExpected = { v2o };
        int[] v1Outs = g1Ordering.getOutEdges(v1o);
        Arrays.sort(v1OutsExpected);
        Arrays.sort(v1Outs);

        int[] v2OutsExpected = { v3o };
        int[] v2Outs = g1Ordering.getOutEdges(v2o);
        Arrays.sort(v2OutsExpected);
        Arrays.sort(v2Outs);

        int[] v3OutsExpected = { v2o, v4o };
        int[] v3Outs = g1Ordering.getOutEdges(v3o);
        Arrays.sort(v3OutsExpected);
        Arrays.sort(v3Outs);

        int[] v4OutsExpected = {};
        int[] v4Outs = g1Ordering.getOutEdges(v4o);
        Arrays.sort(v4OutsExpected);
        Arrays.sort(v4Outs);

        int[] v5OutsExpected = {};
        int[] v5Outs = g1Ordering.getOutEdges(v5o);
        Arrays.sort(v5OutsExpected);
        Arrays.sort(v5Outs);

        assertArrayEquals(v1OutsExpected, v1Outs);
        assertArrayEquals(v2OutsExpected, v2Outs);
        assertArrayEquals(v3OutsExpected, v3Outs);
        assertArrayEquals(v4OutsExpected, v4Outs);
        assertArrayEquals(v5OutsExpected, v5Outs);

        int[] v1InsExpected = {};
        int[] v1Ins = g1Ordering.getInEdges(v1o);
        Arrays.sort(v1InsExpected);
        Arrays.sort(v1Ins);

        int[] v2InsExpected = { v1o, v3o };
        int[] v2Ins = g1Ordering.getInEdges(v2o);
        Arrays.sort(v2InsExpected);
        Arrays.sort(v2Ins);

        int[] v3InsExpected = { v2o };
        int[] v3Ins = g1Ordering.getInEdges(v3o);
        Arrays.sort(v3InsExpected);
        Arrays.sort(v3Ins);

        int[] v4InsExpected = { v3o };
        int[] v4Ins = g1Ordering.getInEdges(v4o);
        Arrays.sort(v4InsExpected);
        Arrays.sort(v4Ins);

        int[] v5InsExpected = {};
        int[] v5Ins = g1Ordering.getInEdges(v5o);
        Arrays.sort(v5InsExpected);
        Arrays.sort(v5Ins);

        assertArrayEquals(v1InsExpected, v1Ins);
        assertArrayEquals(v2InsExpected, v2Ins);
        assertArrayEquals(v3InsExpected, v3Ins);
        assertArrayEquals(v4InsExpected, v4Ins);
        assertArrayEquals(v5InsExpected, v5Ins);

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
