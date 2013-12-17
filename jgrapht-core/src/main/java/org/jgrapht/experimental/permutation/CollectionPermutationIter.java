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
 * CollectionPermutationIter.java
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
package org.jgrapht.experimental.permutation;

import java.util.*;


/**
 * Given a container with elements (Collection,Enumeration,array) defines a
 * permutation iterator which returns, on each iteration, a differnt permutation
 * of the source container. You may choose a different container
 * type(Collection/Array/etc) for each next call. It will continue as if they
 * were the same iterator.
 *
 * @author Assaf
 * @since May 20, 2005
 */
public class CollectionPermutationIter<E>
{
    

    private ArrayPermutationsIter permOrder;
    private List<E> sourceArray;

    /**
     * change everry calculation.can be retrieved publicly
     */
    private int [] currPermutationArray;

    

    /**
     * Note: the Set interface does not guarantee iteration order. This method
     * iterates on the set to get the initial order and after that the data will
     * be saved internally in another (ordered) container. So, remeber that the
     * Initial order can be different from the objectSet.toString() method. If
     * you want it to be the same, use a LinkedHashSet , or use the array
     * constructor.
     *
     * @param objectsSet
     */
    public CollectionPermutationIter(Set<E> objectsSet)
    {
        this(
            new ArrayList<E>(objectsSet),
            new IntegerPermutationIter(objectsSet.size()));
    }

    /**
     * Uses a permArray like [1,1,1,2] where some of the permutations are not
     * relevant. Here there will be 4 permutations (only the '2' position is
     * important)
     *
     * @param objectsArray
     */
    public CollectionPermutationIter(List<E> objectsArray)
    {
        this(
            objectsArray,
            new IntegerPermutationIter(objectsArray.size()));
    }

    public CollectionPermutationIter(
        List<E> objectsArray,
        ArrayPermutationsIter permuter)
    {
        this.permOrder = permuter;
        this.sourceArray = objectsArray;
    }

    

    public boolean hasNext()
    {
        return this.permOrder.hasNextPermutaions();
    }

    /**
     * On first call, returns the source as an array; on any other call
     * thereafter, a new permutation
     *
     * @return null if we overflowed! the array otherwise
     */
    public List<E> getNextArray()
    {
        List<E> permutationResult; // will hold the array result
        if (this.permOrder.hasNextPermutaions()) {
            this.currPermutationArray = this.permOrder.nextPermutation();
            permutationResult = applyPermutation();
        } else {
            permutationResult = null;
        }

        return permutationResult;
    }

    private List<E> applyPermutation()
    {
        ArrayList<E> output = new ArrayList<E>(sourceArray);

        // Example : this.sourceArray = ["A","B","C","D"]
        // perOrder:                  = [ 1 , 0 , 3 , 2 ]
        // result  :                  = ["B","A","D","C"]
        for (int i = 0; i < output.size(); i++) {
            output.set(
                i,
                this.sourceArray.get(this.currPermutationArray[i]));
        }
        return output;
    }

    /**
     * Wrap result to a Set.
     *
     * @return null if we overflowed! the set otherwise
     */
    public Set<E> getNextSet()
    {
        List<E> result = getNextArray();
        if (result == null) {
            return null;
        } else // wrap in a SET
        {
            Set<E> resultSet = new LinkedHashSet<E>(result);
            return resultSet;
        }
    }

    public int [] getCurrentPermutationArray()
    {
        return this.currPermutationArray;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Permutation int[]=");
        sb.append(Arrays.toString(getCurrentPermutationArray()));

        List<E> permutationResult = applyPermutation();
        sb.append("\nPermutationSet Source Object[]=");
        sb.append(this.sourceArray.toString());
        sb.append("\nPermutationSet Result Object[]=");
        sb.append(permutationResult.toString());
        return sb.toString();
    }
}

// End CollectionPermutationIter.java
