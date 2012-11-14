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
 * EqualsAndHashCodeTest.java
 * --------------------------
 * (C) Copyright 2012, by Vladimir Kostyukov and Contributors.
 *
 * Original Author:  Vladimir Kostyukov
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 14-Jun-2012 : Initial revision (VK);
 *
 */

package org.jgrapht.graph;

import com.google.common.collect.Maps;
import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;

import java.util.Map;

public class EqualsAndHashCodeTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    private final String v1 = "v1";
    private final String v2 = "v2";
    private final String v3 = "v3";
    private final String v4 = "v4";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public EqualsAndHashCodeTest(final String name)
    {
        super(name);
    }

    /**
     * Tests equals/hashCode methods for directed graphs.
     */
    public void testDefaultDirectedGraph()
    {
        final DirectedGraph<String, DefaultEdge> g1 =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        final DefaultEdge e12 = g1.addEdge(v1, v2);
        final DefaultEdge e23 = g1.addEdge(v2, v3);
        final DefaultEdge e31 = g1.addEdge(v3, v1);

        final DirectedGraph<String, DefaultEdge> g2 =
             new DefaultDirectedGraph<String, DefaultEdge>(
                 DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e12);

        final DirectedGraph<String, DefaultEdge> g3 =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g3.addVertex(v4);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g2.equals(g1));
        assertTrue(!g3.equals(g2));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for undirected graphs.
     */
    public void testSimpleGraph()
    {
        final UndirectedGraph<String, DefaultEdge> g1 =
            new SimpleGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        final DefaultEdge e12 = g1.addEdge(v1, v2);
        final DefaultEdge e23 = g1.addEdge(v2, v3);
        final DefaultEdge e31 = g1.addEdge(v3, v1);

        final UndirectedGraph<String, DefaultEdge> g2 =
             new SimpleGraph<String, DefaultEdge>(
                 DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e12);

        final UndirectedGraph<String, DefaultEdge> g3 =
            new SimpleGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g3.addVertex(v4);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g2.equals(g1));
        assertTrue(!g3.equals(g2));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with non-Intrusive edges.
     */
    public void testGraphsWithNonIntrusiveEdge()
    {
        final DirectedGraph<String, String> g1 =
            new DefaultDirectedGraph<String, String>(
                String.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, v1 + v2);
        g1.addEdge(v3, v1, v3 + v1);

        final DirectedGraph<String, String> g2 =
             new DefaultDirectedGraph<String, String>(
                 String.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, v3 + v1);
        g2.addEdge(v1, v2, v1 + v2);

        final DirectedGraph<String, String> g3 =
            new DefaultDirectedGraph<String, String>(
                String.class);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, v3 + v1);
        g3.addEdge(v1, v2, v1 + v2);
        g3.addEdge(v2, v3, v2 + v3);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with multiple edges and loops.
     */
    public void testPseudograph()
    {
        final UndirectedGraph<String, DefaultEdge> g1 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        final DefaultEdge e121 = g1.addEdge(v1, v2);
        final DefaultEdge e23 = g1.addEdge(v2, v3);
        final DefaultEdge e31 = g1.addEdge(v3, v1);
        final DefaultEdge e122 = g1.addEdge(v1, v2);
        final DefaultEdge e11 = g1.addEdge(v1, v1);

        final UndirectedGraph<String, DefaultEdge> g2 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v1, v1, e11);
        g2.addEdge(v1, v2, e121);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e122);

        final UndirectedGraph<String, DefaultEdge> g3 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v1, v1, e11);
        g3.addEdge(v1, v2, e121);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with custom edges.
     */
    public void testGrapshWithCustomEdges()
    {
        final UndirectedGraph<String, CustomEdge> g1 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g1.addEdge(v3, v1, new CustomEdge("v3-v1"));

        final UndirectedGraph<String, CustomEdge> g2 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addVertex(v3);
        g2.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g2.addEdge(v3, v1, new CustomEdge("v3-v1"));

        final UndirectedGraph<String, CustomEdge> g3 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g3.addVertex(v1);
        g3.addVertex(v2);
        g3.addVertex(v3);
        g3.addEdge(v1, v2, new CustomEdge("v1::v2"));
        g3.addEdge(v3, v1, new CustomEdge("v3-v1"));

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode for graphs transformed to weighted.
     */
    public void testAsWeightedGraphs() {
        final UndirectedGraph<String, DefaultEdge> g1 =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        final DefaultEdge e12 = g1.addEdge(v1, v2);
        final DefaultEdge e23 = g1.addEdge(v2, v3);
        final DefaultEdge e31 = g1.addEdge(v3, v1);

        final UndirectedGraph<String, DefaultEdge> g2 =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addVertex(v3);
        g2.addEdge(v1, v2, e12);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v3, v1, e31);

        final Map<DefaultEdge, Double> weightMap1 = Maps.newHashMap();

        weightMap1.put(e12, 10.0);
        weightMap1.put(e23, 20.0);
        weightMap1.put(e31, 30.0);

        final WeightedGraph<String, DefaultEdge> g3 =
            new AsWeightedGraph<String, DefaultEdge>(
                g1, weightMap1);

        final Map<DefaultEdge, Double> weightMap2 = Maps.newHashMap();

        weightMap2.put(e12, 10.0);
        weightMap2.put(e23, 20.0);
        weightMap2.put(e31, 30.0);

        final WeightedGraph<String, DefaultEdge> g4 =
            new AsWeightedGraph<String, DefaultEdge>(
                g2, weightMap2);

        final Map<DefaultEdge, Double> weightMap3 = Maps.newHashMap();

        weightMap3.put(e12, 100.0);
        weightMap3.put(e23, 200.0);
        weightMap3.put(e31, 300.0);

        final WeightedGraph<String, DefaultEdge> g5 =
            new AsWeightedGraph<String, DefaultEdge>(
                g2, weightMap3);

        assertTrue(g1.equals(g2));
        assertEquals(g2.hashCode(), g1.hashCode());

        assertTrue(g3.equals(g4));
        assertEquals(g4.hashCode(), g3.hashCode());

        assertTrue(!g4.equals(g5));
    }

    /**
     * Simple custom edge class.
     */
    public static class CustomEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = 1L;
        private final String label;

        public CustomEdge(final String label)
        {
            this.label = label; 
        }

        public int hashCode()
        {
            return label.hashCode();
        }

        public boolean equals(final Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CustomEdge)) {
                return false;
            }

            final CustomEdge edge = (CustomEdge) obj;
            return label.equals(edge.label);
        }
    }
}
