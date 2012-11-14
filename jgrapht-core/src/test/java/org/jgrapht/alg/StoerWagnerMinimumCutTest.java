/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2011, by Barak Naveh and Contributors.
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
/* ----------------
 * StoerWagnerMinimumCutTest.java
 * ----------------
 * (C) Copyright 2011-2011, by Robby McKilliam and Contributors.
 *
 * Original Author:  Robby McKilliam
 * Contributor(s):   -
 *
 * $Id: StoerWagnerMinimumCut.java $
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;


/**
 * @author Robby McKilliam
 */
public class StoerWagnerMinimumCutTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    private final String v1 = "v1";
    private final String v2 = "v2";
    private final String v3 = "v3";
    private final String v4 = "v4";

    //~ Constructors -----------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    /**
     * Test of mergeVertices method, of class StoerWagnerMinimumCut.
     */
    public void testMinCut14()
    {
        final SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 3.0);
        e = g.addEdge(v1, v3);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v1, v4);
        g.setEdgeWeight(e, 4.0);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v3, v4);
        g.setEdgeWeight(e, 1.0);

        final StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut =
            new StoerWagnerMinimumCut<String, DefaultWeightedEdge>(g);

        assertEquals(4.0, mincut.bestcutweight, 0.000001);
    }

    /**
     * Test of mergeVertices method, of class StoerWagnerMinimumCut.
     */
    public void testMinCutDisconnected()
    {
        final SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 3.0);
        e = g.addEdge(v1, v3);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 1.0);

        final StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut =
            new StoerWagnerMinimumCut<String, DefaultWeightedEdge>(g);

        assertEquals(0.0, mincut.bestcutweight, 0.000001);
    }
}

// End StoerWagnerMinimumCutTest.java
