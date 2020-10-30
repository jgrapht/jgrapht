/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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

import java.util.Comparator;

/**
 * Represents the method of ensuring the existence of a total order of a set of elements.
 * 
 * @author Dimitrios Michail
 * 
 * @param <T> the element type
 */
public class ElementOrderMethod<T>
{
    private Comparator<T> comparator;
    private boolean naturalOrdering;

    private ElementOrderMethod()
    {
    }

    /**
     * Get the natural ordering method
     * 
     * @param <T> the element type
     * @return the natural ordering method
     */
    public static <T> ElementOrderMethod<T> natural()
    {
        ElementOrderMethod<T> m = new ElementOrderMethod<>();
        m.comparator = null;
        m.naturalOrdering = true;
        return m;
    }

    /**
     * Get the internal ordering method. This represents the method of explicitly maintaining a map
     * from the elements to long integers. Thus, it incurs a penalty in space and in lookups.
     * 
     * @param <T> the element type
     * @return the internal ordering method
     */
    public static <T> ElementOrderMethod<T> internal()
    {
        ElementOrderMethod<T> m = new ElementOrderMethod<>();
        m.comparator = null;
        m.naturalOrdering = false;
        return m;
    }

    /**
     * Get the comparator ordering method.
     * 
     * @param comparator the actual comparator
     * @param <T> the element type
     * @return the comparator ordering method
     */
    public static <T> ElementOrderMethod<T> comparator(Comparator<T> comparator)
    {
        ElementOrderMethod<T> m = new ElementOrderMethod<>();
        m.comparator = comparator;
        m.naturalOrdering = false;
        return m;
    }

    /**
     * Get the comparator. Returns null if the method does not use an explicit comparator.
     * 
     * @return the comparator or null if the method does not use an explicit comparator
     */
    public Comparator<T> comparator()
    {
        return comparator;
    }

    /**
     * Get the type
     * 
     * @return the type
     */
    public Type getType()
    {
        if (comparator != null) {
            return Type.COMPARATOR;
        } else if (naturalOrdering) {
            return Type.NATURAL;
        }
        return Type.INTERNAL;
    }

    /**
     * Element order method type
     */
    public enum Type
    {
        /**
         * Usage of an actual comparator instance to order the elements.
         */
        COMPARATOR,
        /**
         * Natural ordering. This method may result in {@link ClassCastException} if the elements
         * are not comparable.
         */
        NATURAL,
        /**
         * An internal numbering scheme backed by a map. This incurs space penalty and additional
         * hashtable lookups on each comparison.
         */
        INTERNAL,
    }

}
