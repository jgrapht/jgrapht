/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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
    //~ Methods ----------------------------------------------------------------

    /**
     * Adds a comparator which will also test equivalence. For
     * equivalenceCompare(), the return value is a logical AND of the two
     * comparators. The first check will use the first comparator before the
     * next one. Make sure to put the one which has better performance first.
     * For equivalenceHashcode(), the resulting hashes will be rehashed
     * together. This method may be used multiple times to create a long "chain"
     * of comparators.
     */
    void appendComparator(EquivalenceComparator<E, C> comparatorAfter);
}

// End EquivalenceComparatorChain.java
