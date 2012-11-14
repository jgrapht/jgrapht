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
 * EquivalenceSet.java
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

import java.util.HashSet;
import java.util.Set;


/**
 * EquivalenceSet is a Set of elements which have been determined to be
 * equivalent using EquivalenceComparator. The class makes sure the set size
 * will be one or more.
 * <li>The group can only be created using the factory method
 * createGroupWithElement().
 * <li>The equals and hashcode of a group uses the EquivalenceComparator on one
 * of the group members, thus it is actually checking whether the "other" is in
 * the same group.
 *
 * @param <E> the type of the elements in the set
 * @param <C> the type of the context the element is compared against, e.g. a
 * Graph
 *
 * @author Assaf
 * @since Jul 21, 2005
 */
public class EquivalenceSet<E, C>
{
    //~ Instance fields --------------------------------------------------------

    /**
     * The comparator used to define the group
     */
    protected EquivalenceComparator<? super E, ? super C> eqComparator;
    protected C comparatorContext;

    /**
     * Contains the current elements of the group
     */
    protected Set<E> elementsSet;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new EquivalenceSet, filled with the aElement parameter and a
     * reference to the comparator which is used.
     */
    public EquivalenceSet(
        final E aElement,
        final EquivalenceComparator<? super E, ? super C> aEqComparator,
        final C aComparatorContext)
    {
        eqComparator = aEqComparator;
        comparatorContext = aComparatorContext;

        elementsSet = new HashSet<E>();
        elementsSet.add(aElement);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns an arbitrary object from the group. There is no guarantee as to
     * which will be returned, and whether the same will be returned on the next
     * call.
     */
    public E getRepresentative()
    {
        return elementsSet.iterator().next();
    }

    public C getContext()
    {
        return comparatorContext;
    }

    public int size()
    {
        return elementsSet.size();
    }

    /**
     * Adds an element to the group. It does not check it for equivalance . You
     * must make sure it does, using equals().
     */
    public void add(final E element)
    {
        elementsSet.add(element);
    }

    public boolean equivalentTo(final E aOther, final C aOtherContext)
    {
        return eqComparator.equivalenceCompare(getRepresentative(), aOther,
            comparatorContext, aOtherContext);
    }

    /**
     * Uses the equivalenceCompare() of the comparator to compare a
     * representation of this group, taken using this.getRepresentative(), and a
     * representation of the other object, which may be the object itself, or,
     * if it is an equivalence group too, other.getRepresentative()
     */
    // FIXME REVIEW hb 26-Jan-2006: I think throwing the exception is kind of
    // odd,
    // - it feels like violating the contract of Object.equals()
    // From what I understand, comparing any object to any other object should
    // be
    // possible at all times and simply return false if they are not equal.
    // Uncomparable objects beeing unequal.
    // Suggestion: remove the exception, at best, test on this specific class
    // and
    // write a warning or some such.

    @SuppressWarnings("unchecked")
    public boolean equals(final Object other)
    {
        E otherRepresentative = null;
        C otherContext = null;
        if (other instanceof EquivalenceSet) {
            otherRepresentative =
                ((EquivalenceSet<E, C>) other).getRepresentative();
            otherContext = ((EquivalenceSet<E, C>) other).getContext();
        } else {
            throw new ClassCastException(
                "can check equal() only of EqualityGroup");
        }

        return eqComparator.equivalenceCompare(getRepresentative(),
            otherRepresentative, comparatorContext, otherContext);
    }

    /**
     * Uses a representative to calculate the group hashcode using
     * equivalenceHashcode().
     *
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return eqComparator.equivalenceHashcode(getRepresentative(),
            comparatorContext);
    }

    public String toString()
    {
        return "Eq.Group=" + elementsSet.toString();
    }

    /**
     * Returns the elements of the group. The order of the elements in the
     * returned array is not guaranteed. In other words, two calls to the same
     * object may return different order.
     */
    public Object [] toArray()
    {
        return elementsSet.toArray();
    }
}

// End EquivalenceSet.java
