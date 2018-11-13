/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link DegeneracyOrderingIterator}.
 *
 * @author Dimitrios Michail
 */
public class DegeneracyOrderingIteratorTest
{
    @Test
    public void testGraph1()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");
        g.addVertex("v5");
        g.addVertex("v6");
        g.addVertex("v7");
        g.addVertex("v8");
        g.addVertex("v9");
        g.addVertex("v10");

        // biggest clique: { V1, V2, V3, V4 }
        g.addEdge("v1", "v2");
        g.addEdge("v1", "v3");
        g.addEdge("v1", "v4");
        g.addEdge("v2", "v3");
        g.addEdge("v2", "v4");
        g.addEdge("v3", "v4");

        // smaller clique: { V5, V6, V7 }
        g.addEdge("v5", "v6");
        g.addEdge("v5", "v7");
        g.addEdge("v6", "v7");

        // for fun, add an overlapping clique { V3, V4, V5 }
        g.addEdge("v3", "v5");
        g.addEdge("v4", "v5");

        // make V8 less lonely
        g.addEdge("v7", "v8");

        // add one more maximal which is also the biggest { V1, V2, V9, V10 }
        g.addEdge("v1", "v9");
        g.addEdge("v1", "v10");
        g.addEdge("v2", "v9");
        g.addEdge("v2", "v10");
        g.addEdge("v9", "v10");

        StringBuilder sb = new StringBuilder();
        DegeneracyOrderingIterator<String, DefaultEdge> it = new DegeneracyOrderingIterator<>(g);
        while (it.hasNext()) {
            String v = it.next();
            sb.append("," + v);
        }
        assertEquals(",v8,v6,v7,v5,v9,v10,v1,v2,v3,v4", sb.toString());
    }

    @Test
    public void testGraphWithListener()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(
            g, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"));
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("c", "e");
        g.addEdge("e", "f");
        g.addEdge("e", "g");
        g.addEdge("e", "h");
        g.addEdge("f", "g");
        g.addEdge("f", "h");
        g.addEdge("f", "i");
        g.addEdge("g", "h");
        g.addEdge("i", "j");
        g.addEdge("i", "k");
        g.addEdge("j", "k");

        DegeneracyOrderingIterator<String, DefaultEdge> it = new DegeneracyOrderingIterator<>(g);
        TraversalListener<String, DefaultEdge> listener = new TestTraversalListener<>();
        it.addTraversalListener(listener);

        while (it.hasNext()) {
            it.next();
        }
        assertEquals(
            ",s_a,f_a,s_b,f_b,s_d,f_d,s_c,f_c,s_j,f_j,s_i,f_i,s_k,f_k,s_e,f_e,s_f,f_f,s_g,f_g,s_h,f_h",
            listener.toString());
    }

    private static class TestTraversalListener<V, E>
        implements
        TraversalListener<V, E>
    {

        private StringBuilder sb = new StringBuilder();

        @Override
        public void connectedComponentFinished(ConnectedComponentTraversalEvent e)
        {
            fail("Should not be called");
        }

        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
        {
            fail("Should not be called");
        }

        @Override
        public void edgeTraversed(EdgeTraversalEvent<E> e)
        {
            fail("Should not be called");
        }

        @Override
        public void vertexTraversed(VertexTraversalEvent<V> e)
        {
            sb.append(",s_" + e.getVertex());
        }

        @Override
        public void vertexFinished(VertexTraversalEvent<V> e)
        {
            sb.append(",f_" + e.getVertex());
        }

        public String toString()
        {
            return sb.toString();
        }

    }

}
