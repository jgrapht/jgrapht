/*
 * (C) Copyright 2021-2021, by Hannes Wellmann and Contributors.
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

/**
 * Utility class to simplify handling of arrays.
 *
 * @author Hannes Wellmann
 *
 */
public class ArrayUtil
{
    private ArrayUtil()
    { // static use only
    }

    /**
     * Reverses the order of the elements in the specified range within the given array.
     *
     * @param <V> the type of elements in the array
     * @param arr the array
     * @param from the index of the first element (inclusive) inside the range to reverse
     * @param to the index of the last element (inclusive) inside the range to reverse
     */
    public static final <V> void reverse(V[] arr, int from, int to)
    {
        for (int i = from, j = to; i < j; ++i, --j) {
            swap(arr, i, j);
        }
    }

    /**
     * Reverses the order of the elements in the specified range within the given array.
     *
     * @param arr the array
     * @param from the index of the first element (inclusive) inside the range to reverse
     * @param to the index of the last element (inclusive) inside the range to reverse
     */
    public static final void reverse(int[] arr, int from, int to)
    {
        for (int i = from, j = to; i < j; ++i, --j) {
            int tmp = arr[j];
            arr[j] = arr[i];
            arr[i] = tmp;
        }
    }

    /**
     * Swaps the two elements at the specified indices in the given array.
     *
     * @param <V> the type of elements in the array
     * @param arr the array
     * @param i the index of the first element
     * @param j the index of the second element
     */
    public static final <V> void swap(V[] arr, int i, int j)
    {
        V tmp = arr[j];
        arr[j] = arr[i];
        arr[i] = tmp;
    }
}
