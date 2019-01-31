/*
 * (C) Copyright 2006-2018, by HartmutBenz and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * A unit test for graph generic vertex/edge parameters.
 *
 * @author Hartmut Benz
 */
public class GenericGraphsTest
{
    // ~ Instance fields --------------------------------------------------------

    Graph<Object, ? extends DefaultEdge> objectGraph;
    Graph<FooVertex, FooEdge> fooFooGraph;
    Graph<BarVertex, BarEdge> barBarGraph;

    @Test
    public void testLegalInsertStringGraph()
    {
        String v1 = "Vertex1";
        Object v2 = "Vertex2";
        objectGraph.addVertex(v1);
        objectGraph.addVertex(v2);
        objectGraph.addEdge(v1, v2);
    }

    @Test
    public void testLegalInsertFooGraph()
    {
        FooVertex v1 = new FooVertex();
        FooVertex v2 = new FooVertex();
        BarVertex vb1 = new BarVertex();
        BarVertex vb2 = new BarVertex();
        fooFooGraph.addVertex(v1);
        fooFooGraph.addVertex(v2);
        fooFooGraph.addVertex(vb1);
        fooFooGraph.addVertex(vb2);
        fooFooGraph.addEdge(v1, v2);
        fooFooGraph.addEdge(vb1, vb2);
        fooFooGraph.addEdge(v1, vb2);
        fooFooGraph.addEdge(v1, v2, new BarEdge());
        fooFooGraph.addEdge(v1, vb2, new BarEdge());
        fooFooGraph.addEdge(vb1, vb2, new BarEdge());
    }

    @Test
    public void testLegalInsertBarGraph()
    {
        BarVertex v1 = new BarVertex();
        BarVertex v2 = new BarVertex();
        barBarGraph.addVertex(v1);
        barBarGraph.addVertex(v2);
        barBarGraph.addEdge(v1, v2);
    }

    @Test
    public void testLegalInsertFooBarGraph()
    {
        FooVertex v1 = new FooVertex();
        FooVertex v2 = new FooVertex();
        BarVertex vb1 = new BarVertex();
        BarVertex vb2 = new BarVertex();
        fooFooGraph.addVertex(v1);
        fooFooGraph.addVertex(v2);
        fooFooGraph.addVertex(vb1);
        fooFooGraph.addVertex(vb2);
        fooFooGraph.addEdge(v1, v2);
        fooFooGraph.addEdge(vb1, vb2);
        fooFooGraph.addEdge(v1, vb2);
    }

    @Test
    public void testAlissaHacker()
    {
        Graph<String, CustomEdge> g = new DefaultDirectedGraph<>(CustomEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addEdge("a", "b");
        CustomEdge custom = g.getEdge("a", "b");
        String s = custom.toString();
        assertEquals("Alissa P. Hacker approves the edge from a to b", s);
    }

    @Test
    public void testEqualButNotSameVertex()
    {
        EquivVertex v1 = new EquivVertex();
        EquivVertex v2 = new EquivVertex();
        EquivGraph g = new EquivGraph();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addEdge(v1, v2, new DefaultEdge());
        assertEquals(2, g.degreeOf(v1));
        assertEquals(2, g.degreeOf(v2));
    }

    /**
     * .
     */
    @Before
    public void setUp()
    {
        objectGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        fooFooGraph = new SimpleGraph<>(FooEdge.class);
        barBarGraph = new SimpleGraph<>(BarEdge.class);
    }

    // ~ Inner Classes ----------------------------------------------------------

    public static class CustomEdge
        extends
        DefaultEdge
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString()
        {
            return "Alissa P. Hacker approves the edge from " + getSource() + " to " + getTarget();
        }
    }

    public static class EquivVertex
    {
        @Override
        public boolean equals(Object o)
        {
            return true;
        }

        @Override
        public int hashCode()
        {
            return 1;
        }
    }

    public static class EquivGraph
        extends
        AbstractBaseGraph<EquivVertex, DefaultEdge>
    {
        private static final long serialVersionUID = 8647217182401022498L;

        public EquivGraph()
        {
            super(
                SupplierUtil.createSupplier(EquivVertex.class),
                SupplierUtil.createSupplier(DefaultEdge.class),
                DefaultGraphType.directedPseudograph().asUnweighted());
        }
    }

    public static class FooEdge
        extends
        DefaultEdge
    {
        private static final long serialVersionUID = 1L;
    }

    private class FooVertex
    {
        String str;

        public FooVertex()
        {
            super();
            str = "empty foo";
        }

        public FooVertex(String s)
        {
            str = s;
        }

        public String toString()
        {
            return str;
        }
    }

    public static class BarEdge
        extends
        FooEdge
    {
        private static final long serialVersionUID = 1L;
    }

    private class BarVertex
        extends
        FooVertex
    {
        public BarVertex()
        {
            super("empty bar");
        }

    }
}
