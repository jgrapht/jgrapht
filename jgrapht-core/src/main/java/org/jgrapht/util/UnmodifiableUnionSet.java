/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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

import java.io.*;
import java.util.*;

/**
 * An unmodifiable live view of the union of two sets.
 *
 * @param <E> the element type
 *
 * @author Dimitrios Michail
 */
public class UnmodifiableUnionSet<E>
    extends
    AbstractSet<E>
    implements
    Serializable
{
    private static final long serialVersionUID = -1937327799873331354L;

    private final Set<E> first;
    private final Set<E> second;

    /**
     * Constructs a new set.
     * 
     * @param first the first set
     * @param second the second set
     */
    public UnmodifiableUnionSet(Set<E> first, Set<E> second)
    {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        this.first = first;
        this.second = second;
    }

    @Override
    public Iterator<E> iterator()
    {
        return new UnionIterator(orderSetsBySize());
    }

    /**
     * {@inheritDoc}
     * 
     * Since the view is live, this operation is no longer a constant time operation.
     */
    @Override
    public int size()
    {
        SetSizeOrdering ordering = orderSetsBySize();
        Set<E> bigger = ordering.bigger;
        int count = ordering.biggerSize;
        for (E e : ordering.smaller) {
            if (!bigger.contains(e)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean contains(Object o)
    {
        return first.contains(o) || second.contains(o);
    }

    private SetSizeOrdering orderSetsBySize()
    {
        int firstSize = first.size();
        int secondSize = second.size();
        if (secondSize > firstSize) {
            return new SetSizeOrdering(second, first, secondSize, firstSize);
        } else {
            return new SetSizeOrdering(first, second, firstSize, secondSize);
        }
    }

    // note that these inner classes could be static, but we
    // declare them as non-static to avoid the clutter from
    // duplicating the generic type parameter

    private class SetSizeOrdering
    {
        final Set<E> bigger;
        final Set<E> smaller;
        final int biggerSize;
        final int smallerSize;

        SetSizeOrdering(Set<E> bigger, Set<E> smaller, int biggerSize, int smallerSize)
        {
            this.bigger = bigger;
            this.smaller = smaller;
            this.biggerSize = biggerSize;
            this.smallerSize = smallerSize;
        }
    }

    private class UnionIterator
        implements
        Iterator<E>
    {
        private SetSizeOrdering ordering;
        private boolean inBiggerSet;
        private Iterator<E> iterator;
        private E cur;

        UnionIterator(SetSizeOrdering ordering)
        {
            this.ordering = ordering;
            this.inBiggerSet = true;
            this.iterator = ordering.bigger.iterator();
            this.cur = prefetch();
        }

        @Override
        public boolean hasNext()
        {
            if (cur != null) {
                return true;
            }
            return (cur = prefetch()) != null;
        }

        @Override
        public E next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E result = cur;
            cur = null;
            return result;
        }

        private E prefetch()
        {
            while (true) {
                if (inBiggerSet) {
                    if (iterator.hasNext()) {
                        return iterator.next();
                    } else {
                        inBiggerSet = false;
                        iterator = ordering.smaller.iterator();
                    }
                } else {
                    if (iterator.hasNext()) {
                        E elem = iterator.next();
                        if (!ordering.bigger.contains(elem)) {
                            return elem;
                        }
                    } else {
                        return null;
                    }
                }
            }
        }

    }

}
