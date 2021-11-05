/*
 * (C) Copyright 2020-2021, by Hannes Wellmann and Contributors.
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

import java.util.*;

/**
 * Utility class to create {@link Collection} instances.
 *
 * @author Hannes Wellmann
 *
 */
public class CollectionUtil
{
    private CollectionUtil()
    { // static use only
    }

    /**
     * Returns a {@link HashMap} with an initial capacity that is sufficient to hold
     * {@code expectedSize} mappings without rehashing its internal backing storage.
     * <p>
     * The returned {@code HashMap} has a capacity that is the specified expected size divided by
     * the load factor of the Map, which is sufficient to hold {@code expectedSize} mappings without
     * rehashing. As the Javadoc of {@link HashMap} states: "If the initial capacity is greater than
     * the maximum number of entries divided by the load factor, no rehash operations will ever
     * occur".
     * </p>
     *
     * @param <K> the type of keys in the returned {@code HashMap}
     * @param <V> the type of values in the returned {@code HashMap}
     * @param expectedSize of mappings that will be put into the returned {@code HashMap}
     * @return an empty {@code HashMap} with sufficient capacity to hold expectedSize mappings
     * @see HashMap
     */
    public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize)
    {
        return new HashMap<>(capacityForSize(expectedSize));
    }

    /**
     * Returns a {@link LinkedHashMap} with an initial capacity that is sufficient to hold
     * {@code expectedSize} mappings without rehashing its internal backing storage.
     * <p>
     * Because {@code LinkedHashMap} extends {@link HashMap} it inherits the issue that the capacity
     * is not equivalent to the number of mappings it can hold without rehashing. See
     * {@link #newHashMapWithExpectedSize(int)} for details.
     * </p>
     *
     * @param <K> the type of keys in the returned {@code LinkedHashMap}
     * @param <V> the type of values in the returned {@code LinkedHashMap}
     * @param expectedSize of mappings that will be put into the returned {@code LinkedHashMap}
     * @return an empty {@code LinkedHashMap} with sufficient capacity to hold expectedSize mappings
     * @see HashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize)
    {
        return new LinkedHashMap<>(capacityForSize(expectedSize));
    }

    /**
     * Returns a {@link HashSet} with an initial capacity that is sufficient to hold
     * {@code expectedSize} elements without rehashing its internal backing storage.
     * <p>
     * Because a {@code HashSet} is backed by a {@link HashMap} it inherits the issue that the
     * capacity is not equivalent to the number of elements it can hold without rehashing. See
     * {@link #newHashMapWithExpectedSize(int)} for details.
     * </p>
     *
     * @param <E> the type of elements in the returned {@code HashSet}
     * @param expectedSize of elements that will be add to the returned {@code HashSet}
     * @return an empty {@code HashSet} with sufficient capacity to hold expectedSize elements
     * @see HashMap
     */
    public static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize)
    {
        return new HashSet<>(capacityForSize(expectedSize));
    }

    /**
     * Returns a {@link LinkedHashSet} with an initial capacity that is sufficient to hold
     * {@code expectedSize} elements without rehashing its internal backing storage.
     * <p>
     * Because a {@code LinkedHashSet} is backed by a {@link HashMap} it inherits the issue that the
     * capacity is not equivalent to the number of elements it can hold without rehashing. See
     * {@link #newHashMapWithExpectedSize(int)} for details.
     * </p>
     *
     * @param <E> the type of elements in the returned {@code LinkedHashSet}
     * @param expectedSize of elements that will be add to the returned {@code LinkedHashSet}
     * @return an empty {@code LinkedHashSet} with sufficient capacity to hold expectedSize elements
     * @see HashMap
     */
    public static <E> LinkedHashSet<E> newLinkedHashSetWithExpectedSize(int expectedSize)
    {
        return new LinkedHashSet<>(capacityForSize(expectedSize));
    }

    private static int capacityForSize(int size)
    { // consider default load factor 0.75f of (Linked)HashMap
        return (int) (size / 0.75f + 1.0f); // let (Linked)HashMap limit it if it's too large
    }

    /**
     * Returns from the given {@code Iterable} the element with the given {@code index}.
     * <p>
     * The order to which the index applies is that defined by the {@link Iterable#iterator()}.
     * </p>
     *
     * @param <E> the type of elements in the {@code Iterable}
     * @param iterable the Iterable from which the element at {@code index} is returned
     * @param index the index of the returned element
     * @return the element with {@code index} in the {@code iterable}
     */
    public static <E> E getElement(Iterable<E> iterable, int index)
    {
        if (iterable instanceof List) {
            return ((List<E>) iterable).get(index);
        }
        Iterator<E> it = iterable.iterator();
        for (int i = 0; i < index && it.hasNext(); i++) {
            it.next();
        }
        if (it.hasNext()) {
            return it.next();
        } else {
            throw new IndexOutOfBoundsException(index);
        }
    }
}
