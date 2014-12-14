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
 * OddEvenGroupComparator.java
 * -----------------
 * (C) Copyright 2005-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism.comparators;

import org.jgrapht.experimental.equivalence.*;


/**
 * Comparator which defines two groups of integers. Odds (mod2=1) and
 * Evens(mod2=0). Works only on Integers.
 *
 * @author Assaf
 * @since Jul 22, 2005
 */
public class OddEvenGroupComparator
    implements EquivalenceComparator<Integer, Object>
{
    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean equivalenceCompare(
        Integer arg1,
        Integer arg2,
        Object context1,
        Object context2)
    {
        int int1 = arg1.intValue();
        int int2 = arg2.intValue();

        boolean result = ((int1 % 2) == (int2 % 2));
        return result;
    }

    /* Odd and even must have unique values.
     * @see
     *
     *
     *
     *
     *
     * org.jgrapht.experimental.equivalence.EquivalenceComparator#equivalenceHashcode(java.lang.Object)
     */
    @Override
    public int equivalenceHashcode(Integer arg1, Object context)
    {
        int int1 = arg1.intValue();
        return int1 % 2;
    }
}

// End OddEvenGroupComparator.java
