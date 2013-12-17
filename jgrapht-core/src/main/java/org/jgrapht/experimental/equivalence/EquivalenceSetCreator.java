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
 * EquivalenceSetCreator.java
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
 * FIXME Document me.
 *
 * @param <E> the type of the elements in the set
 * @param <C> the type of the context the element is compared against, e.g. a
 * Graph TODO hb 060208: REVIEW: Using an array for aElementsArray causes
 * problems with generics elsewhere - changed to List?
 *
 * @author Assaf
 * @since Jul 21, 2005
 */
public class EquivalenceSetCreator<E, C>
{
    

    private static final EqGroupSizeComparator groupSizeComparator =
        new EqGroupSizeComparator();

    

    /**
     * Checks for equivalance groups in the aElementsArray. Returns an ordered
     * array of them, where the smallest one is the first in the array.
     *
     * @param aElementsArray
     * @param aEqComparator
     *
     * @deprecated To improve type-safety when using generics, use {@link
     * #createEqualityGroupOrderedArray(Collection, EquivalenceComparator,
     * Object)}
     */
    @Deprecated public static <EE, CC> EquivalenceSet []
    createEqualityGroupOrderedArray(
        EE [] aElementsArray,
        EquivalenceComparator<? super EE, ? super CC> aEqComparator,
        CC aContext)
    {
        return (createEqualityGroupOrderedArray(
            Arrays.asList(aElementsArray),
            aEqComparator,
            aContext));
            // ArrayList<EquivalenceSet<? super EE,? super CC>> arrayList = new
            // ArrayList<EquivalenceSet<? super EE,? super CC>>();
            //
            // HashMap<Integer,List<EquivalenceSet<? super EE,? super CC>>> map
            // = createEqualityGroupMap(aElementsArray, aEqComparator,
            // aContext); // each of the map values is a list with one or
            // more groups in it. // Object[] array = map.values().toArray();
            // // for (int i = 0; i < array.length; i++) // { // List list =
            // (List)array[i];
            //
            // for (List<EquivalenceSet<? super EE,? super CC>> list :
            // map.values() ) { for (EquivalenceSet<? super EE,? super CC>
            // eSet : list ) { arrayList.add( eSet ); } }
            //
            //
            // now we got all the eq. groups  in an array list. we need to sort
            // // them EquivalenceSet [] resultArray = new EquivalenceSet
            // [arrayList.size()]; arrayList.toArray(resultArray);
            // Arrays.sort(resultArray, groupSizeComparator); return
            // resultArray;
    }

    /**
     * Checks for equivalance groups in the aElementsArray. Returns an ordered
     * array of them, where the smallest one is the first in the array.
     *
     * @param elements
     * @param aEqComparator TODO hb 060208: Using an array for aElementsArray
     * causes problems with generics elsewhere - change to List?
     */
    public static <EE, CC> EquivalenceSet [] createEqualityGroupOrderedArray(
        Collection<EE> elements,
        EquivalenceComparator<? super EE, ? super CC> aEqComparator,
        CC aContext)
    {
        ArrayList<EquivalenceSet<? super EE, ? super CC>> arrayList =
            new ArrayList<EquivalenceSet<? super EE, ? super CC>>();

        HashMap<Integer, List<EquivalenceSet<? super EE, ? super CC>>> map =
            createEqualityGroupMap(elements, aEqComparator, aContext);
        // each of the map values is a list with one or more groups in it.
        // Object[] array = map.values().toArray();
        // for (int i = 0; i < array.length; i++)
        // {
        // List list = (List)array[i];

        for (List<EquivalenceSet<? super EE, ? super CC>> list : map.values()) {
            for (EquivalenceSet<? super EE, ? super CC> eSet : list) {
                arrayList.add(eSet);
            }
        }

        // now we got all the eq. groups  in an array list. we need to sort
        // them
        EquivalenceSet [] resultArray = new EquivalenceSet[arrayList.size()];
        arrayList.toArray(resultArray);
        Arrays.sort(resultArray, groupSizeComparator);
        return resultArray;
    }

    /**
     * The data structure we use to store groups is a map, where the key is
     * eqGroupHashCode, and the value is list, containing one or more eqGroup
     * which match this hash.
     *
     * @param elements
     * @param aEqComparator
     *
     * @return a hashmap with key=group hashcode , value = list of eq.groups
     * which match that hash. TODO hb 060208: Using an array for aElementsArray
     * causes problems with generics elsewhere - change to List?
     */
    private static <EE, CC> HashMap<Integer,
        List<EquivalenceSet<? super EE, ? super CC>>> createEqualityGroupMap(
        Collection<EE> elements,
        EquivalenceComparator<? super EE, ? super CC> aEqComparator,
        CC aComparatorContext)
    {
        HashMap<Integer, List<EquivalenceSet<? super EE, ? super CC>>> equalityGroupMap =
            new HashMap<Integer, List<EquivalenceSet<? super EE, ? super CC>>>(
                elements.size());

        for (EE curentElement : elements) {
            int hashcode =
                aEqComparator.equivalenceHashcode(
                    curentElement,
                    aComparatorContext);
            List<EquivalenceSet<? super EE, ? super CC>> list =
                equalityGroupMap.get(Integer.valueOf(hashcode));

            // determine the type of value. It can be null(no value yet) ,
            // or a list of EquivalenceSet

            if (list == null) {
                // create list with one element in it
                list = new LinkedList<EquivalenceSet<? super EE, ? super CC>>();
                list.add(
                    new EquivalenceSet<EE, CC>(
                        curentElement,
                        aEqComparator,
                        aComparatorContext));

                // This is the first one .add it to the map , in an eqGroup
                equalityGroupMap.put(Integer.valueOf(hashcode), list);
            } else {
                boolean eqWasFound = false;

                // we need to check the groups in the list. If none are good,
                // create a new one
                for (EquivalenceSet<? super EE, ? super CC> eqGroup : list) {
                    if (eqGroup.equivalentTo(
                            curentElement,
                            aComparatorContext))
                    {
                        // add it to the list and break
                        eqGroup.add(curentElement);
                        eqWasFound = true;
                        break;
                    }
                }

                // if no match was found add it to the list as a new group
                if (!eqWasFound) {
                    list.add(
                        new EquivalenceSet<EE, CC>(
                            curentElement,
                            aEqComparator,
                            aComparatorContext));
                }
            }
        }

        return equalityGroupMap;
    }

    

    /**
     * Functor used to order groups by size (number of elements in the group)
     * from the smallest to the biggest. If they have the same size, uses the
     * hashcode of the group to compare from the smallest to the biggest. Note
     * that it is inconsistent with equals(). See Object.equals() javadoc.
     *
     * @author Assaf
     * @since Jul 22, 2005
     */
    private static class EqGroupSizeComparator
        implements Comparator<EquivalenceSet>
    {
        /**
         * compare by size , then (if size equal) by hashcode
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public int compare(EquivalenceSet arg1, EquivalenceSet arg2)
        {
            int eqGroupSize1 = arg1.size();
            int eqGroupSize2 = arg2.size();
            if (eqGroupSize1 > eqGroupSize2) {
                return 1;
            } else if (eqGroupSize1 < eqGroupSize2) {
                return -1;
            } else { // size equal , compare hashcodes
                int eqGroupHash1 = arg1.hashCode();
                int eqGroupHash2 = arg2.hashCode();
                if (eqGroupHash1 > eqGroupHash2) {
                    return 1;
                } else if (eqGroupHash1 < eqGroupHash2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}

// End EquivalenceSetCreator.java
