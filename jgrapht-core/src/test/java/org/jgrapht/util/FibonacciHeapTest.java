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
/* -----------------
 * FibonacciHeapTest.java
 * -----------------
 * (C) Copyright 2008-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 20-Apr-2008 : Initial revision (JVS);
 */
package org.jgrapht.util;

import junit.framework.TestCase;

import java.util.Random;


public class FibonacciHeapTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    // in honor of sf.net bug #1845376
    public void testAddRemoveOne()
    {
        final String s = "A";
        final FibonacciHeapNode<String> n = new FibonacciHeapNode<String>(s);
        final FibonacciHeap<String> h = new FibonacciHeap<String>();
        assertTrue(h.isEmpty());
        h.insert(n, 1.0);
        assertFalse(h.isEmpty());
        final FibonacciHeapNode<String> n2 = h.removeMin();
        assertEquals(s, n2.getData());
        assertTrue(h.isEmpty());
    }

    public void testGrowReplaceShrink()
    {
        final Random r = new Random();
        final int k = 10000;
        final String s = "A";
        double t = 0;
        final FibonacciHeap<String> h = new FibonacciHeap<String>();
        for (int i = 0; i < k * 3; ++i) {
            // during first two-thirds, insert
            if (i < k * 2) {
                final double d = r.nextDouble();
                t += d;
                final FibonacciHeapNode<String> n = new FibonacciHeapNode<String>(s);
                h.insert(n, d);
            }

            // during last two-thirds, delete (so during middle
            // third, we'll do both insert and delete, interleaved)
            if (i >= k) {
                final FibonacciHeapNode<String> n2 = h.removeMin();
                t -= n2.getKey();
            }
        }
        assertTrue(h.isEmpty());

        // tally should come back down to zero, or thereabouts (due to roundoff)
        assertEquals(0.0, t, 0.00001);
    }
}

// End FibonacciHeapTest.java
