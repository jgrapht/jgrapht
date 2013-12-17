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
 * EquivalenceComparatorChain.java
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
 * A container of comparators, which are tested in a chain until the first
 * result can be supplied. It implements the EquivalenceComparator, so chains
 * can include other chains. The first check will use the current comparator and
 * not the next one. So, make sure to use the one which has better performance
 * first. (This class follows the "Composite" design-pattern).
 *
 * @param <E> the type of the elements in the set
 * @param <C> the type of the context the element is compared against, e.g. a
 * Graph
 *
 * @author Assaf
 * @since Jul 22, 2005
 */
public interface EquivalenceComparatorChain<E, C>
    extends EquivalenceComparator<E, C>
{
    

    /**
     * Adds a comparator which will also test equivalence. For
     * equivalenceCompare(), the return value is a logical AND of the two
     * comparators. The first check will use the first comparator before the
     * next one. Make sure to put the one which has better performance first.
     * For equivalenceHashcode(), the resulting hashes will be rehashed
     * together. This method may be used multiple times to create a long "chain"
     * of comparators.
     */
    public void appendComparator(EquivalenceComparator<E, C> comparatorAfter);
}

// End EquivalenceComparatorChain.java
