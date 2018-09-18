/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
 * Implementation of a weighted, unmodifiable set. This class can for instance be used to store a
 * weighted vertex cover. The {@code hashCode()} and {@code equals()} methods are identical to those
 * of a normal set, i.e. they are independent of the {@code weight} of this class. All methods are
 * delegated to the underlying set.
 *
 * @param <E> element type
 *
 * @author Joris Kinable
 */
public class WeightedUnmodifiableSet<E>
    extends
    AbstractSet<E>
    implements
    Serializable
{

    private static final long serialVersionUID = -5913435131882975869L;

    public final Set<E> backingSet;
    public final double weight;

    /**
     * Constructs a WeightedUnmodifiableSet instance
     * 
     * @param backingSet underlying set
     */
    public WeightedUnmodifiableSet(Set<E> backingSet)
    {
        this.backingSet = backingSet;
        this.weight = backingSet.size();
    }

    /**
     * Constructs a WeightedUnmodifiableSet instance
     * 
     * @param backingSet underlying set
     * @param weight weight of the set
     */
    public WeightedUnmodifiableSet(Set<E> backingSet, double weight)
    {
        this.backingSet = backingSet;
        this.weight = weight;
    }

    /**
     * Returns the weight of the set.
     *
     * @return weight of the set
     */
    public double getWeight()
    {
        return weight;
    }

    @Override
    public int size()
    {
        return backingSet.size();
    }

    @Override
    public boolean isEmpty()
    {
        return backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return backingSet.contains(o);
    }

    @Override
    public Iterator<E> iterator()
    {
        return backingSet.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return backingSet.toArray(a);
    }

    @Override
    public boolean add(E v)
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return backingSet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("This set is unmodifiable");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof WeightedUnmodifiableSet))
            return false;
        @SuppressWarnings("unchecked") WeightedUnmodifiableSet<E> other =
            (WeightedUnmodifiableSet<E>) o;
        return this.backingSet.equals(other.backingSet);
    }

    @Override
    public int hashCode()
    {
        return backingSet.hashCode();
    }
}
