/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.util;

import java.io.*;
import java.util.*;

/**
 * Generic pair.
 * 
 * <p>
 * Although the instances of this class are immutable, it is impossible to ensure that the
 * references passed to the constructor will not be modified by the caller.
 * 
 * @param <A> the first element type
 * @param <B> the second element type
 * 
 */
public class Pair<A, B>
    implements
    Serializable
{
    private static final long serialVersionUID = 8176288675989092842L;

    /**
     * The first pair element
     */
    protected final A first;

    /**
     * The second pair element
     */
    protected final B second;

    /**
     * Create a new pair
     * 
     * @param a the first element
     * @param b the second element
     */
    public Pair(A a, B b)
    {
        this.first = a;
        this.second = b;
    }

    /**
     * Get the first element of the pair
     * 
     * @return the first element of the pair
     */
    public A getFirst()
    {
        return first;
    }

    /**
     * Get the second element of the pair
     * 
     * @return the second element of the pair
     */
    public B getSecond()
    {
        return second;
    }

    /**
     * Assess if this pair contains an element.
     *
     * @param e The element in question
     *
     * @return true if contains the element, false otherwise
     * 
     * @param <E> the element type
     */
    public <E> boolean hasElement(E e)
    {
        if (e == null) {
            return first == null || second == null;
        } else {
            return e.equals(first) || e.equals(second);
        }
    }

    @Override
    public String toString()
    {
        return "(" + first + "," + second + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof Pair))
            return false;

        @SuppressWarnings("unchecked") Pair<A, B> other = (Pair<A, B>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second);
    }

    /**
     * Creates new pair of elements pulling of the necessity to provide corresponding types of the
     * elements supplied.
     *
     * @param a first element
     * @param b second element
     * @param <A> the first element type
     * @param <B> the second element type
     * @return new pair
     */
    public static <A, B> Pair<A, B> of(A a, B b)
    {
        return new Pair<>(a, b);
    }
}
