/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* --------------------------
 * GenericGraphsTest.java
 * --------------------------
 * (C) Copyright 2006-2008, by HartmutBenz and Contributors.
 *
 * Original Author:  Hartmut Benz
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * ??-???-2006 : Initial revision (HB);
 *
 */
package org.jgrapht.graph;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;


/**
 * A unit test for graph generic vertex/edge parameters.
 *
 * @author Hartmut Benz
 */
public class GenericGraphsTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    Graph<Object, ? extends DefaultEdge> objectGraph;
    Graph<FooVertex, FooEdge> fooFooGraph;
    Graph<BarVertex, BarEdge> barBarGraph;
    Graph<FooVertex, BarEdge> fooBarGraph;

    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public GenericGraphsTest(final String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    // ~ Methods ---------------------------------------------------------------

    public void testLegalInsertStringGraph()
    {
        final String v1 = "Vertex1";
        final Object v2 = "Vertex2";
        objectGraph.addVertex(v1);
        objectGraph.addVertex(v2);
        objectGraph.addEdge(v1, v2);
    }

    public void testLegalInsertFooGraph()
    {
        final FooVertex v1 = new FooVertex();
        final FooVertex v2 = new FooVertex();
        final BarVertex vb1 = new BarVertex();
        final BarVertex vb2 = new BarVertex();
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

    public void testLegalInsertBarGraph()
    {
        final BarVertex v1 = new BarVertex();
        final BarVertex v2 = new BarVertex();
        barBarGraph.addVertex(v1);
        barBarGraph.addVertex(v2);
        barBarGraph.addEdge(v1, v2);
    }

    public void testLegalInsertFooBarGraph()
    {
        final FooVertex v1 = new FooVertex();
        final FooVertex v2 = new FooVertex();
        final BarVertex vb1 = new BarVertex();
        final BarVertex vb2 = new BarVertex();
        fooFooGraph.addVertex(v1);
        fooFooGraph.addVertex(v2);
        fooFooGraph.addVertex(vb1);
        fooFooGraph.addVertex(vb2);
        fooFooGraph.addEdge(v1, v2);
        fooFooGraph.addEdge(vb1, vb2);
        fooFooGraph.addEdge(v1, vb2);
    }

    public void testAlissaHacker()
    {
        final DirectedGraph<String, CustomEdge> g =
            new DefaultDirectedGraph<String, CustomEdge>(CustomEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addEdge("a", "b");
        final CustomEdge custom = g.getEdge("a", "b");
        final String s = custom.toString();
        assertEquals("Alissa P. Hacker approves the edge from a to b", s);
    }

    public void testEqualButNotSameVertex()
    {
        final EquivVertex v1 = new EquivVertex();
        final EquivVertex v2 = new EquivVertex();
        final EquivGraph g = new EquivGraph();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addEdge(v1, v2, new DefaultEdge());
        assertEquals(2, g.degreeOf(v1));
        assertEquals(2, g.degreeOf(v2));
    }

    /**
     * .
     */
    @Override
    protected void setUp()
    {
        objectGraph =
            new DefaultDirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        fooFooGraph = new SimpleGraph<FooVertex, FooEdge>(FooEdge.class);
        barBarGraph = new SimpleGraph<BarVertex, BarEdge>(BarEdge.class);
    }

    //~ Inner Classes ----------------------------------------------------------

    public static class CustomEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = 1L;

        public String toString()
        {
            return "Alissa P. Hacker approves the edge from " + getSource()
                + " to " + getTarget();
        }
    }

    public static class EquivVertex
    {
        public boolean equals(final Object o)
        {
            return true;
        }

        public int hashCode()
        {
            return 1;
        }
    }

    public static class EquivGraph
        extends AbstractBaseGraph<EquivVertex, DefaultEdge>
        implements UndirectedGraph<EquivVertex, DefaultEdge>
    {
        /**
         */
        private static final long serialVersionUID = 8647217182401022498L;

        public EquivGraph()
        {
            super(
                new ClassBasedEdgeFactory<EquivVertex, DefaultEdge>(
                    DefaultEdge.class),
                true,
                true);
        }
    }

    public static class FooEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = 1L;
    }

    private static class FooVertex
    {
        final String str;

        public FooVertex()
        {
            str = "empty foo";
        }

        public FooVertex(final String s)
        {
            str = s;
        }
    }

    public static class BarEdge
        extends FooEdge
    {
        private static final long serialVersionUID = 1L;
    }

    private class BarVertex
        extends FooVertex
    {
        public BarVertex()
        {
            super("empty bar");
        }

        public BarVertex(final String s)
        {
            super(s);
        }
    }
}

// End GenericGraphsTest.java
