/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.util;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Test {@link Pair} and {@link UnorderedPair} classes.
 *
 * @author Dimitrios Michail
 */
public class PairTest
{

    private static final String ANOTHER = "another";
    private static final String CUSTOM = "custom";

    @Test
    public void testPair()
    {
        Pair<String, Custom> p = Pair.of(CUSTOM, new Custom(1));

        assertEquals(CUSTOM, p.getFirst());
        assertNotEquals(ANOTHER, p.getFirst());

        assertEquals(new Custom(1), p.getSecond());
        assertNotEquals(new Custom(2), p.getSecond());

        assertTrue(p.hasElement(CUSTOM));
        assertFalse(p.hasElement(ANOTHER));
        assertTrue(p.hasElement(new Custom(1)));
        assertFalse(p.hasElement(new Custom(2)));
        assertFalse(p.hasElement(null));
    }

    @Test
    public void testUnorderedPair()
    {
        Pair<String, Custom> p = UnorderedPair.of(CUSTOM, new Custom(1));

        assertEquals(CUSTOM, p.getFirst());
        assertNotEquals(ANOTHER, p.getFirst());

        assertEquals(new Custom(1), p.getSecond());
        assertNotEquals(new Custom(2), p.getSecond());

        assertTrue(p.hasElement(CUSTOM));
        assertFalse(p.hasElement(ANOTHER));
        assertTrue(p.hasElement(new Custom(1)));
        assertFalse(p.hasElement(new Custom(2)));
        assertFalse(p.hasElement(null));
    }

    @Test
    public void testPairWithNull()
    {
        Pair<String, Custom> p = Pair.of(null, new Custom(1));

        assertNull(p.getFirst());
        assertEquals(new Custom(1), p.getSecond());

        assertTrue(p.hasElement(null));
        assertTrue(p.hasElement(new Custom(1)));
    }

    @Test
    public void testUnorderedPairWithNull()
    {
        Pair<String, Custom> p = UnorderedPair.of(null, new Custom(1));

        assertNull(p.getFirst());
        assertEquals(new Custom(1), p.getSecond());

        assertTrue(p.hasElement(null));
        assertTrue(p.hasElement(new Custom(1)));
    }

    @Test
    public void testPairEquals()
    {
        Pair<String, Custom> p1 = Pair.of(CUSTOM, new Custom(1));
        Pair<String, Custom> p2 = Pair.of(ANOTHER, new Custom(1));
        Pair<String, Custom> p3 = Pair.of(ANOTHER, new Custom(2));
        Pair<String, Custom> p4 = Pair.of(CUSTOM, new Custom(1));
        Pair<String, Custom> p5 = Pair.of(CUSTOM, new Custom(2));
        Pair<Custom, String> p6 = Pair.of(new Custom(1), CUSTOM);

        assertNotEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1, p4);
        assertNotEquals(p1, p5);
        assertNotEquals(p1, p6);
        assertNotEquals(p2, p3);
        assertNotEquals(p2, p4);
        assertNotEquals(p2, p5);
        assertNotEquals(p2, p6);
        assertNotEquals(p3, p4);
        assertNotEquals(p3, p5);
        assertNotEquals(p3, p6);
        assertNotEquals(p4, p5);
        assertNotEquals(p4, p6);
        assertNotEquals(p5, p6);
    }

    @Test
    public void testUnorderedPairEquals()
    {
        Pair<String, Custom> p1 = UnorderedPair.of(CUSTOM, new Custom(1));
        Pair<String, Custom> p2 = UnorderedPair.of(ANOTHER, new Custom(1));
        Pair<String, Custom> p3 = UnorderedPair.of(ANOTHER, new Custom(2));
        Pair<String, Custom> p4 = UnorderedPair.of(CUSTOM, new Custom(1));
        Pair<String, Custom> p5 = UnorderedPair.of(CUSTOM, new Custom(2));
        Pair<Custom, String> p6 = UnorderedPair.of(new Custom(1), CUSTOM);

        assertNotEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1, p4);
        assertNotEquals(p1, p5);
        assertEquals(p1, p6);
        assertNotEquals(p2, p3);
        assertNotEquals(p2, p4);
        assertNotEquals(p2, p5);
        assertNotEquals(p2, p6);
        assertNotEquals(p3, p4);
        assertNotEquals(p3, p5);
        assertNotEquals(p3, p6);
        assertNotEquals(p4, p5);
        assertEquals(p4, p6);
        assertNotEquals(p5, p6);
    }

    @Test
    public void testPairEqualsWithNull()
    {
        Pair<String, Custom> p1 = Pair.of(CUSTOM, null);
        Pair<Custom, String> p2 = Pair.of(null, CUSTOM);
        Pair<String, Custom> p3 = Pair.of(ANOTHER, null);
        Pair<String, Custom> p4 = Pair.of(CUSTOM, null);
        Pair<String, Custom> p5 = Pair.of(CUSTOM, new Custom(1));

        assertNotEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1, p4);
        assertNotEquals(p1, p5);
        assertNotEquals(p2, p3);
        assertNotEquals(p2, p4);
        assertNotEquals(p2, p5);
        assertNotEquals(p3, p4);
        assertNotEquals(p3, p5);
        assertNotEquals(p4, p5);
    }

    @Test
    public void testUnorderedPairEqualsWithNull()
    {
        Pair<String, Custom> p1 = UnorderedPair.of(CUSTOM, null);
        Pair<Custom, String> p2 = UnorderedPair.of(null, CUSTOM);
        Pair<String, Custom> p3 = UnorderedPair.of(ANOTHER, null);
        Pair<String, Custom> p4 = UnorderedPair.of(CUSTOM, null);
        Pair<String, Custom> p5 = UnorderedPair.of(CUSTOM, new Custom(1));

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1, p4);
        assertNotEquals(p1, p5);
        assertNotEquals(p2, p3);
        assertEquals(p2, p4);
        assertNotEquals(p2, p5);
        assertNotEquals(p3, p4);
        assertNotEquals(p3, p5);
        assertNotEquals(p4, p5);
    }

    @Test
    public void testDifferentTypesEqualsWithNull()
    {
        Pair<String, Custom> p1 = Pair.of(null, null);
        Pair<String, Custom> p2 = Pair.of(null, null);
        Pair<Custom, String> p3 = Pair.of(null, null);
        assertEquals(p1, p2);
        assertEquals(p2, p3);
        assertEquals(p3, p1);

        UnorderedPair<String, Custom> p4 = UnorderedPair.of(null, null);
        UnorderedPair<String, Custom> p5 = UnorderedPair.of(null, null);
        UnorderedPair<Custom, String> p6 = UnorderedPair.of(null, null);
        assertEquals(p4, p5);
        assertEquals(p5, p6);
        assertEquals(p6, p4);
    }

    @Test
    public void testUnorderedSameHashCode()
    {
        UnorderedPair<String, Custom> p1 = UnorderedPair.of(CUSTOM, new Custom(1));
        UnorderedPair<Custom, String> p2 = UnorderedPair.of(new Custom(1), CUSTOM);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    class Custom
    {
        private int id;

        public Custom(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Custom other = (Custom) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (id != other.id)
                return false;
            return true;
        }

        private PairTest getOuterType()
        {
            return PairTest.this;
        }
    }

}
