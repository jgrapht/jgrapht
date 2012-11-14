/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
/* ------------------------------
 * KruskalMinimumSpanningTreeTest.java
 * ------------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):   -
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


public class KruskalMinimumSpanningTreeTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";

    //~ Instance fields --------------------------------------------------------

    DefaultWeightedEdge e12;
    DefaultWeightedEdge e13;
    DefaultWeightedEdge e15;
    DefaultWeightedEdge e24;
    DefaultWeightedEdge e34;
    DefaultWeightedEdge e45;

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testMinimumSpanningTree()
    {
        final Graph<String, DefaultWeightedEdge> graph = createWeighted();

        final KruskalMinimumSpanningTree<String, DefaultWeightedEdge> mst =
            new KruskalMinimumSpanningTree<String, DefaultWeightedEdge>(graph);

        assertEquals(15.0, mst.getSpanningTreeCost());

        final Set<DefaultWeightedEdge> edges = mst.getEdgeSet();
        for (final DefaultWeightedEdge edge : edges) {
            assertTrue(
                edge.equals(e12) || edge.equals(e13) || edge.equals(e24)
                || edge.equals(e45));
        }
    }

    protected Graph<String, DefaultWeightedEdge> createWeighted()
    {
        final Graph<String, DefaultWeightedEdge> g;
        final double bias = 1;

        g = new SimpleWeightedGraph<String, DefaultWeightedEdge>(
            DefaultWeightedEdge.class);

        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        e12 = Graphs.addEdge(g, V1, V2, bias * 2);

        e13 = Graphs.addEdge(g, V1, V3, bias * 3);

        e24 = Graphs.addEdge(g, V2, V4, bias * 5);

        e34 = Graphs.addEdge(g, V3, V4, bias * 20);

        e45 = Graphs.addEdge(g, V4, V5, bias * 5);

        e15 = Graphs.addEdge(g, V1, V5, bias * 100);

        return g;
    }
}

// End KruskalMinimumSpanningTreeTest.java
