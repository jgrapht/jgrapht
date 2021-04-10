/*
 * (C) Copyright 2018-2018, by Daniel Mock and Contributors.
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

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents an ordering of the given elements.
 * It can be accessed as iterator over the ordering, as comparator to compare to (previously added) elements or as
 * a bijection between the elements and {0,..., size - 1}.
 * The ordering is immutable.
 * Null values are not allowed.
 *
 * @param <E> the element type
 *
 * @author Daniel Mock
 */
public class Ordering<E> implements Comparator<E>, Iterable<E> {
    private List<E> list;
    private HashMap<E, Integer> positionOf;

    /**
     * Creates an ordering imposed by the list. No sanity checks are performed.
     * The user has to make sure that each element is unique and not null.
     * @param list the list defining the ordering.
     */
    public Ordering(List<E> list) {
        this.list = new ArrayList<>(list);
        positionOf = new HashMap<>(list.size());

        int position = 0;
        for (E element: this.list) {
            positionOf.put(element, position++);
        }
    }

    /**
     * Creates an ordering imposed by the collection. The order is imposed by the collection in its current state.
     * The user has to make sure that each element is unique and not null.
     * @param collection the collection
     */
    public Ordering(Collection<E> collection) {
        this(new ArrayList<>(collection));
    }

    /**
     * Creates an ordering imposed by the HashMap. The elements mapped to Integer i are at position i in the
     * new ordering.
     * @param positionOf A HashMap whose elements are {0,..., size - 1}.
     */
    public Ordering(HashMap<E, Integer> positionOf) {
        this.positionOf = new HashMap<>(positionOf);
        list = new ArrayList<>(positionOf.size());
        for (int i = 0; i < positionOf.size(); i++) {
            list.add(null);
        }

        for (E element: positionOf.keySet()) {
            list.set(positionOf.get(element), element);
        }
    }

    /**
     * Returns the index of specified element in this ordering, or -1 if this ordering does not contain the element.
     * @param element element to search for
     * @return the index of specified element in this ordering, or -1 if this ordering does not contain the element.
     */
    public int getPositionOf(E element) {
        Integer result = positionOf.get(element);
        if (result == null) {
            result = -1;
        }
        return result;
    }

    /**
     * Returns the ordering as a list.
     * @return the list which represents this ordering.
     */
    public List<E> getOrderedList() {
        return new ArrayList<>(list);
    }


    /**
     * Returns the element at the specified position in the ordering.
     * @param position index of the element to return
     * @return the element at the specified position in this ordering
     */
    public E getElementAt(int position) {
        return list.get(position);
    }

    /**
     * Returns the number of elements in this ordering.
     * @return the number of elements in this ordering
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if this list contains the specified element.
     * @param element element whose presence in this ordering is to be tested
     * @return true if this ordering contains the specified element
     */
    public boolean contains(E element) {
        return positionOf.containsKey(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(E o1, E o2) {
        Integer position1 = positionOf.get(o1);
        Integer position2 = positionOf.get(o2);

        if (position1 == null || position2 == null) {
            throw new IllegalArgumentException();
        }

        return position1 - position2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Iterator<E> it = list.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }
        };
    }
}
