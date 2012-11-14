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
/* --------------
 * CloneTest.java
 * --------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Oct-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import junit.framework.TestCase;
import org.jgrapht.*;

import java.lang.String;


/**
 * A unit test for a cloning bug, adapted from a forum entry from Linda Buisman.
 *
 * @author John V. Sichi
 * @since Oct 6, 2003
 */
public class CloneTest
    extends EnhancedTestCase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public CloneTest(final String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Test graph cloning.
     */
    @SuppressWarnings("unchecked")
    public void testCloneSpecificsBug()
    {
        final SimpleGraph<String, DefaultEdge> g1 =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        final String one = "1";
        final String two = "2";
        final String three = "3";
        g1.addVertex(one);
        g1.addVertex(two);
        g1.addVertex(three);
        g1.addEdge(one, two);
        g1.addEdge(two, three);

        final SimpleGraph<String, DefaultEdge> g2 =
            (SimpleGraph<String, DefaultEdge>) g1.clone(); // Type-safty
                                                           // warning OK with
                                                           // clone
        assertEquals(2, g2.edgeSet().size());
        assertNotNull(g2.getEdge(one, two));
        assertTrue(g2.removeEdge(g2.getEdge(one, two)));
        assertNotNull(g2.removeEdge("2", "3"));
        assertTrue(g2.edgeSet().isEmpty());
    }

    /**
     * Tests usage of {@link ParanoidGraph} for detecting broken vertex
     * implementations.
     */
    public void testParanoidGraph()
    {
        final BrokenVertex v1 = new BrokenVertex(1);
        final BrokenVertex v2 = new BrokenVertex(2);
        final BrokenVertex v3 = new BrokenVertex(1);

        final SimpleGraph<BrokenVertex, DefaultEdge> g =
            new SimpleGraph<BrokenVertex, DefaultEdge>(DefaultEdge.class);
        final ParanoidGraph<BrokenVertex, DefaultEdge> pg =
            new ParanoidGraph<BrokenVertex, DefaultEdge>(g);
        pg.addVertex(v1);
        pg.addVertex(v2);
        try {
            pg.addVertex(v3);

            // should not get here
            assertFalse();
        } catch (IllegalArgumentException ex) {
            // expected, swallow
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    private class BrokenVertex
    {
        private final int x;

        BrokenVertex(final int x)
        {
            this.x = x;
        }

        public boolean equals(final Object other)
        {
            if (!(other instanceof BrokenVertex)) {
                return false;
            }
            return x == ((BrokenVertex) other).x;
        }
    }
}

// End CloneTest.java
