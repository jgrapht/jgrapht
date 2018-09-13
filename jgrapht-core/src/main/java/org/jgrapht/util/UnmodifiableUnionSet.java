/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * An unmodifiable live view of the union of two sets.
 *
 * @param <E> the element type
 *
 * @author Dimitrios Michail
 */
public class UnmodifiableUnionSet<E>
    extends AbstractSet<E>
    implements Serializable
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
    public boolean contains(Object o) {
        return first.contains(o) || second.contains(o);
    }

    private class UnionIterator
        implements Iterator<E>
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
