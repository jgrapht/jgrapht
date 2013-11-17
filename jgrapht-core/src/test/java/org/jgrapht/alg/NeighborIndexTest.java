/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------------------
 * NeighborIndexTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 12-Dec-2005 : Initial revision (CF);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Charles Fry
 */
public class NeighborIndexTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    //~ Methods ----------------------------------------------------------------

    public void testNeighborSet()
    {
        // We use Object instead of DefaultEdge for the edge type
        // in order to cover the case in
        // https://sourceforge.net/tracker/index.php?func=detail&aid=3486775&group_id=86459&atid=579687
        ListenableUndirectedGraph<String, Object> g =
            new ListenableUndirectedGraph<String, Object>(
                Object.class);
        g.addVertex(V1);
        g.addVertex(V2);

        g.addEdge(V1, V2);

        NeighborIndex<String, Object> index =
            new NeighborIndex<String, Object>(g);
        g.addGraphListener(index);

        Set neighbors1 = index.neighborsOf(V1);

        assertEquals(1, neighbors1.size());
        assertEquals(true, neighbors1.contains(V2));

        g.addVertex(V3);
        g.addEdge(V3, V1);

        Set neighbors3 = index.neighborsOf(V3);

        assertEquals(2, neighbors1.size());
        assertEquals(true, neighbors1.contains(V3));

        assertEquals(1, neighbors3.size());
        assertEquals(true, neighbors3.contains(V1));

        g.removeEdge(V3, V1);

        assertEquals(1, neighbors1.size());
        assertEquals(false, neighbors1.contains(V3));

        assertEquals(0, neighbors3.size());

        g.removeVertex(V2);

        assertEquals(0, neighbors1.size());
    }

    public void testDirectedNeighborSet()
    {
        ListenableDirectedGraph<String, Object> g =
            new ListenableDirectedGraph<String, Object>(
                Object.class);
        g.addVertex(V1);
        g.addVertex(V2);

        g.addEdge(V1, V2);

        DirectedNeighborIndex<String, Object> index =
            new DirectedNeighborIndex<String, Object>(g);
        g.addGraphListener(index);

        Set p = index.predecessorsOf(V1);
        Set s = index.successorsOf(V1);

        assertEquals(0, p.size());
        assertEquals(1, s.size());
        assertEquals(true, s.contains(V2));

        g.addVertex(V3);
        g.addEdge(V3, V1);

        Set q = index.successorsOf(V3);

        assertEquals(1, p.size());
        assertEquals(1, s.size());
        assertEquals(true, p.contains(V3));

        assertEquals(1, q.size());
        assertEquals(true, q.contains(V1));

        g.removeEdge(V3, V1);

        assertEquals(0, q.size());
        assertEquals(0, p.size());

        g.removeVertex(V2);

        assertEquals(0, s.size());
    }
}

// End NeighborIndexTest.java
