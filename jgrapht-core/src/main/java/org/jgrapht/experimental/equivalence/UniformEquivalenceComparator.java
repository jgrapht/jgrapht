/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * UniformEquivalenceComparator.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.equivalence;

/**
 * This Equivalence comparator acts as if all elements are in one big global
 * equivalence class. Useful when a comparator is needed, but there is no
 * important difference between the elements. equivalenceCompare() always return
 * true; equivalenceHashcode() always returns 0.
 *
 * @author Assaf
 * @since Jul 21, 2005
 */
public class UniformEquivalenceComparator<E, C>
    implements EquivalenceComparator<E, C>
{
    

    /**
     * Always returns true.
     *
     * @see EquivalenceComparator#equivalenceCompare(Object, Object, Object,
     * Object)
     */
    public boolean equivalenceCompare(
        E arg1,
        E arg2,
        C context1,
        C context2)
    {
        return true;
    }

    /**
     * Always returns 0.
     *
     * @see EquivalenceComparator#equivalenceHashcode(Object, Object)
     */
    public int equivalenceHashcode(E arg1, C context)
    {
        return 0;
    }
}

// End UniformEquivalenceComparator.java
