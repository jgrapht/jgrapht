/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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

import org.jgrapht.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link RadixSort} class.
 *
 * @author Alexandru Valeanu
 */
public class RadixSortTest
{

    /**
     * Check if the input list is sorted in ascending order.
     *
     * @param list the input list
     * @return true if the list is sorted in ascending order, false otherwise
     */
    public static boolean isSorted(List<Integer> list)
    {
        for (int i = 0; i < list.size() - 1; i++) {
            if (!(list.get(i) <= list.get(i + 1)))
                return false;
        }

        return true;
    }

    @Test
    public void testNullArray()
    {
        RadixSort.sort(null);
    }

    @Test
    public void testEmptyArray()
    {
        List<Integer> list = new ArrayList<>();
        RadixSort.sort(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSmallArray()
    {
        List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(1);
        list.add(10);
        list.add(2);
        list.add(5);
        list.add(3);
        RadixSort.sort(list);

        assertTrue(isSorted(list));
    }

    @Test
    public void testRandomHugeArray()
    {
        Random random = new Random(0x881);
        final int N = 1_000_000;

        List<Integer> list = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            list.add(random.nextInt(Integer.MAX_VALUE));
        }

        RadixSort.sort(list);
        assertTrue(isSorted(list));
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomArrays()
    {
        testRandomArrays(new Random(0x88));
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomArraysWithNoFixedSeed()
    {
        testRandomArrays(new Random());
    }

    private void testRandomArrays(Random random)
    {
        final int NUM_TESTS = 500_000;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 1 + random.nextInt(100);

            List<Integer> list = new ArrayList<>(N);

            for (int i = 0; i < N; i++) {
                list.add(random.nextInt(Integer.MAX_VALUE));
            }

            RadixSort.sort(list);
            assertTrue(isSorted(list));
        }
    }

}
