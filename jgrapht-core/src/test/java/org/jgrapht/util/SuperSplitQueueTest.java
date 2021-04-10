/*
 * (C) Copyright 2018-2018, by Oliver Feith, Daniel Mock and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SuperSplitQueueTest {
    @Test
    public void contains() {
        SubSplitQueue q = SubSplitQueue.subSplitQueueFactory(10);
        for (int i = 0; i < 10; i++) {
            assertTrue(q.contains(i));
        }

        assertFalse(q.contains(10));
    }

    @Test
    public void split() {
        SubSplitQueue q = SubSplitQueue.subSplitQueueFactory(10);
        assertTrue(q.contains(2));

        SubSplitQueue split = q.split(new int[] {2,5,9});

        assertTrue(split.contains(2));
        assertTrue(split.contains(5));
        assertTrue(split.contains(9));
        assertFalse(split.contains(1));

        assertFalse(q.contains(2));
        assertFalse(q.contains(5));
        assertFalse(q.contains(9));

        SubSplitQueue splitsplit = split.split(new int[] {1,2});

        assertTrue(splitsplit.contains(2));
        assertFalse(splitsplit.contains(1));
    }

    @Test
    public void split2() {
        SubSplitQueue q = SubSplitQueue.subSplitQueueFactory(10);
        assertEquals(10, q.getSize());
        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},q.asArray());

        SubSplitQueue split = q.split(new int[] {2,5,9});
        assertArrayEquals(new int[] {0, 1, 3, 4, 6, 7, 8},q.asArray());
        assertArrayEquals(new int[] {2,5,9},split.asArray());

        SubSplitQueue splitsplit = split.split(new int[] {2});
        assertEquals(7, q.getSize());
        assertEquals(2, split.getSize());
        assertEquals(1, splitsplit.getSize());

        assertArrayEquals(new int[] {0, 1, 3, 4, 6, 7, 8}, q.asArray());
        assertArrayEquals(new int[] {5, 9}, split.asArray());
        assertArrayEquals(new int[] {2}, splitsplit.asArray());
    }

    @Test
    public void iterator() {
        SubSplitQueue q = SubSplitQueue.subSplitQueueFactory(10);
        Iterator<Integer> it = q.iterator();
        for (int i = 0; i < 10; i++) {
            int element =  it.next();
            assertEquals(i, element);
        }
    }
}
