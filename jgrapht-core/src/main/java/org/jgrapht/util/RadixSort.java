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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Sorts the specified list of integers into ascending order using the Radix Sort method.
 *
 * This algorithms runs in $O(N + V)$ time and uses $O(N + V)$ extra memory, where $V = 256$.
 *
 * If $N \leq RadixSort.CUT\_OFF$ then the standard Java sorting algorithm is used.
 *
 * The specified list must be modifiable, but need not be resizable.
 */
public class RadixSort {

    public static int CUT_OFF = 40;

    private static final int MAX_DIGITS = 32;
    private static final int MAX_D = 4;
    private static final int SIZE_RADIX = 1 << (MAX_DIGITS / MAX_D);
    private static final int MASK = SIZE_RADIX - 1;

    private static int[] count = new int[SIZE_RADIX];

    // Suppresses default constructor, ensuring non-instantiability.
    private RadixSort(){
    }

    private static void radixSort(int array[], int n, int tempArray[], int cnt[]) {
        for (int d = 0, shift = 0; d < MAX_D; d++, shift += (MAX_DIGITS / MAX_D)) {
            Arrays.fill(cnt, 0);

            for (int i = 0; i < n; ++i)
                ++cnt[(array[i] >> shift) & MASK];

            for (int i = 1; i < SIZE_RADIX; ++i)
                cnt[i] += cnt[i - 1];

            for (int i = n - 1; i >= 0; i--)
                tempArray[--cnt[(array[i] >> shift) & MASK]] = array[i];

            System.arraycopy(tempArray, 0, array, 0, n);
        }
    }

    /**
     * Sort the given list in ascending order.
     *
     * @param list the input list of integers
     */
    public static void sort(List<Integer> list){
        if (list == null){
            return;
        }

        final int n = list.size();

        if (n <= CUT_OFF){
            list.sort(null);
            return;
        }

        int[] array = new int[n];

        ListIterator<Integer> listIterator = list.listIterator();

        while (listIterator.hasNext()){
            array[listIterator.nextIndex()] = listIterator.next();
        }
        radixSort(array, n, new int[n], count);

        listIterator = list.listIterator();

        while (listIterator.hasNext()){
            listIterator.next();
            listIterator.set(array[listIterator.previousIndex()]);
        }
    }
}
