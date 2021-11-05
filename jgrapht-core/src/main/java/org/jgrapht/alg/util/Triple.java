/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
 * Generic triple (3-tuple).
 * 
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 * 
 * @author Dimitrios Michail
 */
public class Triple<A, B, C>
    implements
    Serializable
{
    private static final long serialVersionUID = -7076291895521537427L;

    /**
     * The first element
     */
    protected A first;

    /**
     * The second element
     */
    protected B second;

    /**
     * The third element
     */
    protected C third;

    /**
     * Create a new triple
     * 
     * @param a the first element
     * @param b the second element
     * @param c the third element
     */
    public Triple(A a, B b, C c)
    {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    /**
     * Get the first element
     * 
     * @return the first element
     */
    public A getFirst()
    {
        return first;
    }

    /**
     * Get the second element
     * 
     * @return the second element
     */
    public B getSecond()
    {
        return second;
    }

    /**
     * Get the third element
     * 
     * @return the third element
     */
    public C getThird()
    {
        return third;
    }

    /**
     * Set the first element
     *
     * @param a the element to be assigned
     */
    public void setFirst(A a)
    {
        first = a;
    }

    /**
     * Set the second element
     *
     * @param b the element to be assigned
     */
    public void setSecond(B b)
    {
        second = b;
    }

    /**
     * Set the third element
     *
     * @param c the element to be assigned
     */
    public void setThird(C c)
    {
        third = c;
    }

    /**
     * Assess if this triple contains an element.
     *
     * @param e The element in question
     * @return true if contains the element, false otherwise
     * @param <E> the element type
     */
    @SuppressWarnings("unlikely-arg-type")
    public <E> boolean hasElement(E e)
    {
        if (e == null) {
            return first == null || second == null || third == null;
        } else {
            return e.equals(first) || e.equals(second) || e.equals(third);
        }
    }

    @Override
    public String toString()
    {
        return "(" + first + "," + second + "," + third + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof Triple))
            return false;

        @SuppressWarnings("unchecked") Triple<A, B, C> other = (Triple<A, B, C>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second)
            && Objects.equals(third, other.third);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second, third);
    }

    /**
     * Create a new triple.
     *
     * @param a first element
     * @param b second element
     * @param c third element
     * @param <A> the first element type
     * @param <B> the second element type
     * @param <C> the third element type
     * @return new triple
     */
    public static <A, B, C> Triple<A, B, C> of(A a, B b, C c)
    {
        return new Triple<>(a, b, c);
    }
}
