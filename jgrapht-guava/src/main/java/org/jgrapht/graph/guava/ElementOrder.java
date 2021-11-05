/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph.guava;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to maintain a total order for a set of elements.
 * 
 * <p>
 * The user can choose between using a comparator, using the natural ordering of the elements or
 * maintaining internally a mapping to long integers. In the latter case the user is also
 * responsible for notifying this class whenever elements are removed, in order to cleanup any
 * internal state. Construction of elements is performed in a lazy manner.
 * 
 * @author Dimitrios Michail
 */
class ElementOrder<V>
    implements
    Serializable
{
    private static final long serialVersionUID = -3732847114940656189L;

    private Comparator<V> comparator;
    private Map<V, Long> indices;
    private long nextId;

    /**
     * Create a new element order.
     * 
     * @param comparator the comparator to use
     * @param indices internal map from elements to long indices
     */
    private ElementOrder(Comparator<V> comparator, Map<V, Long> indices)
    {
        this.indices = indices;
        this.comparator = comparator;
        this.nextId = 0;
    }

    /**
     * Create an element order with a comparator
     * 
     * @param <V> the element type
     * @param comparator the comparator
     * @return the element order
     */
    public static <V> ElementOrder<V> comparator(Comparator<V> comparator)
    {
        return new ElementOrder<>(comparator, null);
    }

    /**
     * Create an element order with the natural ordering
     * 
     * @param <V> the element type
     * @return the element order
     */
    public static <V> ElementOrder<V> natural()
    {
        return new ElementOrder<>(null, null);
    }

    /**
     * Create an internal element order which maintains a map from elements to long values.
     * 
     * @param <V> the element type
     * @return the element order
     */
    public static <V> ElementOrder<V> internal()
    {
        return new ElementOrder<>(null, new HashMap<>());
    }

    /**
     * Compare two elements
     * 
     * @param v first element
     * @param u second element
     * @return the value {@code 0} if {@code v} is equal to {@code u}; a value less than {@code 0}
     *         if {@code v} is less than {@code u}; and a value greater than {@code 0} if {@code v}
     *         is greater than {@code u}.
     */
    @SuppressWarnings("unchecked")
    public int compare(V v, V u)
    {
        if (comparator != null) {
            return comparator.compare(v, u);
        }
        if (indices != null) {
            long vid = indices.computeIfAbsent(v, this::computeNextId);
            long uid = indices.computeIfAbsent(u, this::computeNextId);
            return Long.compare(vid, uid);
        }
        return ((Comparable<? super V>) v).compareTo(u);
    }

    /**
     * Get the minimum of two elements.
     * 
     * @param v first element
     * @param u second element
     * @return the minimum of two elements
     */
    public V min(V v, V u)
    {
        return compare(v, u) <= 0 ? v : u;
    }

    /**
     * Notify about a new element.
     * 
     * @param v the element
     */
    public void notifyAddition(V v)
    {
        if (indices != null) {
            indices.computeIfAbsent(v, this::computeNextId);
        }
    }

    /**
     * Notify about an element being removed. This method only affects the case that an internal map
     * to long integers is maintained.
     * 
     * @param v the element
     */
    public void notifyRemoval(V v)
    {
        if (indices != null) {
            indices.remove(v);
        }
    }

    private long computeNextId(V vertex)
    {
        return nextId++;
    }

}
