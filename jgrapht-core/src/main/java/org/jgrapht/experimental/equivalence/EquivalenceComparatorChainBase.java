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
 * EquivalenceComparatorChainBase.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: EquivalenceComparatorChainBase.java 485 2006-06-26 09:12:14Z perfecthash
 * $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.equivalence;

import java.util.*;


/**
 * This class implements comparator chaining.
 *
 * <p>Usage examples:
 * <li> <i>graph-theory, node equivalence:</i> You can create a comparator for
 * the inDegree of a node, another for the total weight of outDegree edges, and
 * a third which checks the business content of the node. You know that the
 * first topological comparators has dozens of different groups, but the
 * buisness comparator has only two, and they are hard to check . The best
 * performance will be gained by:
 *
 * <blockquote><code>
 * <p>EquivalenceComparatorChainBase eqChain = new
 * EquivalenceComparatorChainBase(fastNodesDegreeComparator);
 *
 * <p>eqChain.addComparatorAfter(ABitSlowerEdgeWeightComparator);
 *
 * <p>eqChain.addComparatorAfter(slowestBuisnessContentsComparator);</code>
 * </blockquote>
 *
 * @param <E> the type of the elements in the set
 * @param <C> the type of the context the element is compared against, e.g. a
 * Graph
 *
 * @author Assaf
 * @since Jul 22, 2005
 */
public class EquivalenceComparatorChainBase<E, C>
    implements EquivalenceComparatorChain<E, C>
{
    //~ Instance fields --------------------------------------------------------

    private final List<EquivalenceComparator<? super E, ? super C>> chain;

    //~ Constructors -----------------------------------------------------------

    /**
     */
    public EquivalenceComparatorChainBase(
        final EquivalenceComparator<E, C> firstComaparator)
    {
        chain =
            new LinkedList<EquivalenceComparator<? super E, ? super C>>();
        chain.add(firstComaparator);
    }

    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see
     *
     *
     *
     *
     *
     * org.jgrapht.experimental.equivalence.EquivalenceComparatorChain#addComparatorAfter(org.jgrapht.experimental.equivalence.EquivalenceComparator)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void appendComparator(final EquivalenceComparator comparatorAfter)
    {
        if (comparatorAfter != null) {
            chain.add(comparatorAfter);
        }
    }

    /**
     * Implements logical AND between the comparators results. Iterates through
     * the comparators chain until one of them returns false. If none returns
     * false, this method returns true.
     *
     * @see EquivalenceComparator#equivalenceCompare(Object, Object, Object,
     * Object)
     */
    @Override
    public boolean equivalenceCompare(
        final E arg1,
        final E arg2,
        final C context1,
        final C context2)
    {
        for (
            final EquivalenceComparator<? super E, ? super C> currentComparator
            : chain)
        {
            if (!currentComparator.equivalenceCompare(
                    arg1,
                    arg2,
                    context1,
                    context2))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Rehashes the concatenation of the results of all single hashcodes.
     *
     * @see EquivalenceComparator#equivalenceHashcode(Object, Object)
     */
    @Override
    public int equivalenceHashcode(final E arg1, final C context)
    {
        final StringBuffer hashStringBuffer = new StringBuffer();
        for (
            ListIterator<EquivalenceComparator<? super E, ? super C>> iter = chain
                .listIterator();
            iter.hasNext();)
        {
            final EquivalenceComparator<? super E, ? super C> currentComparator =
                iter.next();
            final int currentHashCode =
                currentComparator.equivalenceHashcode(arg1, context);
            hashStringBuffer.append(currentHashCode);

            // add a delimeter only if needed for next
            if (iter.hasNext()) {
                hashStringBuffer.append('+');
            }
        }
        return hashStringBuffer.toString().hashCode();
    }
}

// End EquivalenceComparatorChainBase.java
