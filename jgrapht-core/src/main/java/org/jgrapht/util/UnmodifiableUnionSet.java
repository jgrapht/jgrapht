/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
        if (second.size() > first.size()) {
            // always store largest set first
            this.first = second;
            this.second = first;
        } else {
            this.first = first;
            this.second = second;
        }
    }

    @Override
    public Iterator<E> iterator()
    {
        return new UnionIterator();
    }

    /**
     * {@inheritDoc}
     * 
     * Since the view is live, this operation is no longer a constant time operation.
     */
    @Override
    public int size()
    {
        int count = first.size();
        for (E e : second) {
            if (!first.contains(e)) {
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

    private class UnionIterator
        implements
        Iterator<E>
    {

        private boolean inFirstSet;
        private Iterator<E> iterator;
        private E cur;

        public UnionIterator()
        {
            this.inFirstSet = true;
            this.iterator = first.iterator();
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
                if (inFirstSet) {
                    if (iterator.hasNext()) {
                        return iterator.next();
                    } else {
                        inFirstSet = false;
                        iterator = second.iterator();
                    }
                } else {
                    if (iterator.hasNext()) {
                        E elem = iterator.next();
                        if (!first.contains(elem)) {
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
