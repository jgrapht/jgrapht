/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
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
/* -------------------------
 * EdmondsBlossomShrinkingTest.java
 * -------------------------
 * (C) Copyright 2012-2012, by Alejandro Ramon Lopez del Huerto and Contributors.
 *
 * Original Author:  Alejandro Ramon Lopez del Huerto
 * Contributor(s):
 *
 * Changes
 * -------
 * 24-Jan-2012 : Initial revision (ARLH);
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.Set;

/**
 * .
 * 
 * @author Alejandro R. Lopez del Huerto
 * @since Jan 24, 2012
 */
public final class EdmondsBlossomShrinkingTest extends TestCase
{
    public void testOne()
    {
        // create an undirected graph
        UndirectedGraph<Integer, DefaultEdge> g =
            new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

        Integer v1 = 1;
        Integer v2 = 2;
        Integer v3 = 3;
        Integer v4 = 4;

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultEdge e12 = g.addEdge(v1, v2);
        DefaultEdge e23 = g.addEdge(v2, v3);
        DefaultEdge e24 = g.addEdge(v2, v4);
        DefaultEdge e34 = g.addEdge(v3, v4);

        // compute max match
        EdmondsBlossomShrinking<Integer, DefaultEdge> matcher =
            new EdmondsBlossomShrinking<Integer, DefaultEdge>(g);
        Set<DefaultEdge> match = matcher.getMatching();
        assertEquals(2, match.size());
        assertTrue(match.contains(e12));
        assertTrue(match.contains(e34));
    }
}
