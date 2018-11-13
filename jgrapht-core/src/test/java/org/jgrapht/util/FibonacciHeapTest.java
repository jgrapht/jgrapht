/*
 * (C) Copyright 2008-2018, by John V Sichi and Contributors.
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
package org.jgrapht.util;

import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class FibonacciHeapTest
{
    // ~ Methods ----------------------------------------------------------------

    // in honor of sf.net bug #1845376
    @Test
    public void testAddRemoveOne()
    {
        String s = "A";
        FibonacciHeapNode<String> n = new FibonacciHeapNode<>(s);
        FibonacciHeap<String> h = new FibonacciHeap<>();
        assertTrue(h.isEmpty());
        h.insert(n, 1.0);
        assertFalse(h.isEmpty());
        FibonacciHeapNode<String> n2 = h.removeMin();
        assertEquals(s, n2.getData());
        assertTrue(h.isEmpty());
    }

    @Test
    public void testGrowReplaceShrink()
    {
        Random r = new Random();
        int k = 10000;
        String s = "A";
        double t = 0;
        FibonacciHeap<String> h = new FibonacciHeap<>();
        for (int i = 0; i < (k * 3); ++i) {
            // during first two-thirds, insert
            if (i < (k * 2)) {
                double d = r.nextDouble();
                t += d;
                FibonacciHeapNode<String> n = new FibonacciHeapNode<>(s);
                h.insert(n, d);
            }

            // during last two-thirds, delete (so during middle
            // third, we'll do both insert and delete, interleaved)
            if (i >= k) {
                FibonacciHeapNode<String> n2 = h.removeMin();
                t -= n2.getKey();
            }
        }
        assertTrue(h.isEmpty());

        // tally should come back down to zero, or thereabouts (due to roundoff)
        assertEquals(0.0, t, 0.00001);
    }

    @Test
    public void testValidReinsert()
    {
        FibonacciHeapNode<String> n1 = new FibonacciHeapNode<>("1");
        FibonacciHeapNode<String> n2 = new FibonacciHeapNode<>("2");
        FibonacciHeapNode<String> n3 = new FibonacciHeapNode<>("3");
        FibonacciHeapNode<String> n4 = new FibonacciHeapNode<>("4");

        FibonacciHeap<String> h = new FibonacciHeap<>();
        h.insert(n1, 1d);
        h.insert(n2, 2d);
        h.insert(n3, 3d);
        h.insert(n4, 4d);

        assertEquals("1", h.min().getData());
        h.removeMin();
        h.insert(n1, 5d);
        assertEquals("2", h.min().getData());
        h.removeMin();
        assertEquals("3", h.min().getData());
        h.removeMin();
        assertEquals("4", h.min().getData());
        h.removeMin();
        assertEquals("1", h.min().getData());
        h.removeMin();
        assertTrue(h.isEmpty());
    }

    @Test
    public void testBadReinsert()
    {
        FibonacciHeapNode<String> n1 = new FibonacciHeapNode<>("1");
        FibonacciHeapNode<String> n2 = new FibonacciHeapNode<>("2");
        FibonacciHeapNode<String> n3 = new FibonacciHeapNode<>("3");
        FibonacciHeapNode<String> n4 = new FibonacciHeapNode<>("4");

        FibonacciHeap<String> h = new FibonacciHeap<>();
        h.insert(n1, 1d);
        h.insert(n2, 2d);
        h.insert(n3, 3d);
        h.insert(n4, 4d);

        assertEquals("1", h.min().getData());
        try {
            h.insert(n2, 5d);
            fail("Reinsert allowed!");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void testBadDecreaseKey()
    {
        FibonacciHeapNode<String> n1 = new FibonacciHeapNode<>("1");
        FibonacciHeapNode<String> n2 = new FibonacciHeapNode<>("2");
        FibonacciHeapNode<String> n3 = new FibonacciHeapNode<>("3");
        FibonacciHeapNode<String> n4 = new FibonacciHeapNode<>("4");

        FibonacciHeap<String> h = new FibonacciHeap<>();
        h.insert(n1, 1d);
        h.insert(n2, 2d);
        h.insert(n3, 3d);
        h.insert(n4, 4d);

        assertEquals("1", h.min().getData());
        h.decreaseKey(n4, 0.5);
        assertEquals("4", h.min().getData());
        h.removeMin();
        assertEquals("1", h.min().getData());
        try {
            h.decreaseKey(n4, 0.1);
            fail("Invalid decrease key allowed!");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void testBadDelete()
    {
        FibonacciHeapNode<String> n1 = new FibonacciHeapNode<>("1");
        FibonacciHeapNode<String> n2 = new FibonacciHeapNode<>("2");
        FibonacciHeapNode<String> n3 = new FibonacciHeapNode<>("3");
        FibonacciHeapNode<String> n4 = new FibonacciHeapNode<>("4");

        FibonacciHeap<String> h = new FibonacciHeap<>();
        h.insert(n1, 1d);
        h.insert(n2, 2d);
        h.insert(n3, 3d);
        h.insert(n4, 4d);

        assertEquals("1", h.min().getData());
        h.removeMin();
        assertEquals("2", h.min().getData());
        try {
            h.delete(n1);
            fail("Invalid delete allowed!");
        } catch (IllegalArgumentException e) {
            // ignore
        }
        h.delete(n2);
        h.delete(n3);
        h.delete(n4);
    }

}
