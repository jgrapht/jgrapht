/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic Quadruple (4-tuple).
 * 
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 * @param <D> the fourth element type
 * 
 * @author Dimitrios Michail
 */
public class Quadruple<A, B, C, D>
    implements
    Serializable
{
    private static final long serialVersionUID = 6518285748557215795L;

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
     * The fourth element
     */
    protected D fourth;

    /**
     * Create a new quadruple
     * 
     * @param a the first element
     * @param b the second element
     * @param c the third element
     * @param d the fourth element
     */
    public Quadruple(A a, B b, C c, D d)
    {
        this.first = a;
        this.second = b;
        this.third = c;
        this.fourth = d;
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
     * Get the fourth element
     * 
     * @return the fourth element
     */
    public D getFourth()
    {
        return fourth;
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
     * Set the fourth element
     *
     * @param d the element to be assigned
     */
    public void setFourth(D d)
    {
        fourth = d;
    }

    /**
     * Assess if this quadruple contains an element.
     *
     * @param e The element in question
     * @return true if contains the element, false otherwise
     * @param <E> the element type
     */
    @SuppressWarnings("unlikely-arg-type")
    public <E> boolean hasElement(E e)
    {
        if (e == null) {
            return first == null || second == null || third == null || fourth == null;
        } else {
            return e.equals(first) || e.equals(second) || e.equals(third) || e.equals(fourth);
        }
    }

    @Override
    public String toString()
    {
        return "(" + first + "," + second + "," + third + "," + fourth + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof Quadruple))
            return false;

        @SuppressWarnings("unchecked") Quadruple<A, B, C, D> other = (Quadruple<A, B, C, D>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second)
            && Objects.equals(third, other.third) && Objects.equals(fourth, other.fourth);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second, third, fourth);
    }

    /**
     * Create a new quadruple.
     *
     * @param a first element
     * @param b second element
     * @param c third element
     * @param d fourth element
     * @param <A> the first element type
     * @param <B> the second element type
     * @param <C> the third element type
     * @param <D> the fourth element type
     * @return new triple
     */
    public static <A, B, C, D> Quadruple<A, B, C, D> of(A a, B b, C c, D d)
    {
        return new Quadruple<>(a, b, c, d);
    }
}
