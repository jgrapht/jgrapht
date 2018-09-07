/*
 * (C) Copyright 2006-2018, by John V Sichi and Contributors.
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

import java.util.*;

/**
 * Helper for efficiently representing small sets whose elements are known to be unique by
 * construction, implying we don't need to enforce the uniqueness property in the data structure
 * itself. Use with caution.
 *
 * <p>
 * Note that for equals/hashCode, the class implements the Set behavior (unordered), not the list
 * behavior (ordered); the fact that it subclasses ArrayList should be considered an implementation
 * detail.
 * 
 * @param <E> the element type
 *
 * @author John V. Sichi
 */
public class ArrayUnenforcedSet<E>
    extends
    ArrayList<E>
    implements
    Set<E>
{
    private static final long serialVersionUID = -7413250161201811238L;

    /**
     * Constructs a new empty set
     */
    public ArrayUnenforcedSet()
    {
        super();
    }

    /**
     * Constructs a set containing the elements of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    public ArrayUnenforcedSet(Collection<? extends E> c)
    {
        super(c);
    }

    /**
     * Constructs an empty set with the specified initial capacity.
     *
     * @param n the initial capacity of the set
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public ArrayUnenforcedSet(int n)
    {
        super(n);
    }

    @Override
    public boolean equals(Object o)
    {
        return new SetForEquality().equals(o);
    }

    @Override
    public int hashCode()
    {
        return new SetForEquality().hashCode();
    }

    /**
     * Multiple inheritance helper.
     */
    private class SetForEquality
        extends
        AbstractSet<E>
    {
        @Override
        public Iterator<E> iterator()
        {
            return ArrayUnenforcedSet.this.iterator();
        }

        @Override
        public int size()
        {
            return ArrayUnenforcedSet.this.size();
        }
    }
}

// End ArrayUnenforcedSet.java
