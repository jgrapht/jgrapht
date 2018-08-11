/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link RadixSort} class.
 *
 * @author Alexandru Valeanu
 */
public class RadixSortTest {

    /**
     * Check if the input list is sorted in ascending order.
     *
     * @param list the input list
     * @return true if the list is sorted in ascending order, false otherwise
     */
    public static boolean isSorted(List<Integer> list){
        for (int i = 0; i < list.size() - 1; i++) {
            if (!(list.get(i) <= list.get(i + 1)))
                return false;
        }

        return true;
    }

    @Test
    public void testNullArray(){
        RadixSort.sort(null);
    }

    @Test
    public void testEmptyArray(){
        List<Integer> list = new ArrayList<>();
        RadixSort.sort(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSmallArray(){
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
    public void testRandomHugeArray(){
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
    public void testRandomArrays(){
        Random random = new Random(0x88);
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