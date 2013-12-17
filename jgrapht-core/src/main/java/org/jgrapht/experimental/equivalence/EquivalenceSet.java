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

import java.util.*;


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
    

    /**
     * The comparator used to define the group
     */
    protected EquivalenceComparator<? super E, ? super C> eqComparator;
    protected C comparatorContext;

    /**
     * Contains the current elements of the group
     */
    protected Set<E> elementsSet;

    

    /**
     * Constructs a new EquivalenceSet, filled with the aElement parameter and a
     * reference to the comparator which is used.
     */
    public EquivalenceSet(
        E aElement,
        EquivalenceComparator<? super E, ? super C> aEqComparator,
        C aComparatorContext)
    {
        this.eqComparator = aEqComparator;
        this.comparatorContext = aComparatorContext;

        this.elementsSet = new HashSet<E>();
        this.elementsSet.add(aElement);
    }

    

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
        return this.comparatorContext;
    }

    public int size()
    {
        return elementsSet.size();
    }

    /**
     * Adds an element to the group. It does not check it for equivalance . You
     * must make sure it does, using equals().
     */
    public void add(E element)
    {
        this.elementsSet.add(element);
    }

    public boolean equivalentTo(E aOther, C aOtherContext)
    {
        boolean result =
            this.eqComparator.equivalenceCompare(
                this.getRepresentative(),
                aOther,
                this.comparatorContext,
                aOtherContext);
        return result;
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
    public boolean equals(Object other)
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

        boolean result =
            this.eqComparator.equivalenceCompare(
                this.getRepresentative(),
                otherRepresentative,
                this.comparatorContext,
                otherContext);
        return result;
    }

    /**
     * Uses a representative to calculate the group hashcode using
     * equivalenceHashcode().
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int result =
            this.eqComparator.equivalenceHashcode(
                this.getRepresentative(),
                this.comparatorContext);
        return result;
    }

    public String toString()
    {
        return "Eq.Group=" + this.elementsSet.toString();
    }

    /**
     * Returns the elements of the group. The order of the elements in the
     * returned array is not guaranteed. In other words, two calls to the same
     * object may return different order.
     */
    public Object [] toArray()
    {
        return this.elementsSet.toArray();
    }
}

// End EquivalenceSet.java
