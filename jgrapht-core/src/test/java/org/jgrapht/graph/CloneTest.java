/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

import org.junit.*;

import static org.junit.Assert.*;

/**
 * A unit test for a cloning bug, adapted from a forum entry from Linda Buisman.
 *
 * @author John V. Sichi
 */
public class CloneTest
{
    /**
     * Test graph cloning.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCloneSpecificsBug()
    {
        SimpleGraph<String, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        String one = "1";
        String two = "2";
        String three = "3";
        g1.addVertex(one);
        g1.addVertex(two);
        g1.addVertex(three);
        g1.addEdge(one, two);
        g1.addEdge(two, three);

        SimpleGraph<String, DefaultEdge> g2 = (SimpleGraph<String, DefaultEdge>) g1.clone(); // Type-safty
                                                                                             // warning
                                                                                             // OK
                                                                                             // with
                                                                                             // clone
        assertEquals(2, g2.edgeSet().size());
        assertNotNull(g2.getEdge(one, two));
        assertTrue(g2.removeEdge(g2.getEdge(one, two)));
        assertNotNull(g2.removeEdge("2", "3"));
        assertTrue(g2.edgeSet().isEmpty());
    }

    /**
     * Tests usage of {@link ParanoidGraph} for detecting broken vertex implementations.
     */
    @Test
    public void testParanoidGraph()
    {
        BrokenVertex v1 = new BrokenVertex(1);
        BrokenVertex v2 = new BrokenVertex(2);
        BrokenVertex v3 = new BrokenVertex(1);

        SimpleGraph<BrokenVertex, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        ParanoidGraph<BrokenVertex, DefaultEdge> pg = new ParanoidGraph<>(g);
        pg.addVertex(v1);
        pg.addVertex(v2);
        try {
            pg.addVertex(v3);

            Assert.fail(); // should not get here
        } catch (IllegalArgumentException ex) {
            // expected, swallow
        }
    }

    // ~ Inner Classes ----------------------------------------------------------

    private class BrokenVertex
    {
        private int x;

        BrokenVertex(int x)
        {
            this.x = x;
        }

        @Override
        public boolean equals(Object other)
        {
            return other instanceof BrokenVertex && x == ((BrokenVertex) other).x;
        }
    }
}
