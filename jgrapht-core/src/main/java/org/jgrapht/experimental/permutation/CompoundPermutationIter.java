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
 * CompoundPermutationIter.java
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

import org.jgrapht.util.*;


/**
 * For permutation like this:
 * <li>1,2 are the same eq.group (numbers)
 * <li>a,b are og the same eq.group (letters)
 * <li>'$' is of its own eq. group (signs) Let the order of the group be
 * (arbitrary): signs,numbers,letters (note that for performance reasons, this
 * arbitrary order is the worst! see Performance section below)
 *
 * <p>These are the possible compound perm: [$,1,2,a,b,c]
 *
 * <p>[$,1,2,a,c,b]
 *
 * <p>[$,1,2,b,a,c]
 *
 * <p>[$,1,2,b,c,a]
 *
 * <p>[$,1,2,c,a,b]
 *
 * <p>[$,1,2,c,b,a]
 *
 * <p>[$,2,1,a,b,c]
 *
 * <p>[$,2,1,a,c,b]
 *
 * <p>[$,2,1,b,a,c]
 *
 * <p>[$,2,1,b,c,a]
 *
 * <p>[$,2,1,c,a,b]
 *
 * <p>[$,2,1,c,b,a]
 *
 * <p>The overall number is the product of the factorials of each eq. group
 * size; in our example : (1!)x(2!)x(3!)=1x2x6=12. Using the constructor with
 * eq.group sizes and initial order [1,2,3], the result permutations are
 * retrieved as numbers in an array, where [0,1,2,3,4,5] means [$,1,2,a,b,c]:
 *
 * <p>[0,1,2,3,5,4]
 *
 * <p>[0,1,2,4,3,5]
 *
 * <p>etc. etc., till:
 *
 * <p>[0,2,1,5,4,3] means [$,2,1,c,b,a]
 *
 * <p>
 * <p><i>Performance:</i> The implementation tries to advance each time the
 * group zero, if it does not succeed, it tries the next group (1,2 and so on),
 * so: try to put the largest group as the first groups, UNLIKE the example.
 * Performance-wise it is better to do [a,b,c,1,2,$] .The effect is improvement
 * by constant (for example, by 2)
 *
 * @author Assaf
 * @since May 30, 2005
 */
public class CompoundPermutationIter
    implements ArrayPermutationsIter,
        Iterator
{
    

    IntegerPermutationIter [] permArray;

    /**
     * on the example 1+2+3=6
     */
    private int totalPermArraySize;

    /**
     * The overall number is the product of the factorial of each eq. group
     * size.
     */
    private int max;

    private int iterCounter = 0;

    

    /**
     * For the class example, use [1,2,2]. order matters! (performance-wise too)
     *
     * @param equalityGroupsSizesArray
     */
    public CompoundPermutationIter(int [] equalityGroupsSizesArray)
    {
        init(equalityGroupsSizesArray);
    }

    

    /**
     * Creates an IntegerPermutationIter class per equalityGroup with different
     * integers.
     *
     * @param equalityGroupsSizesArray
     */
    private void init(int [] equalityGroupsSizesArray)
    {
        this.permArray =
            new IntegerPermutationIter[equalityGroupsSizesArray.length];

        int counter = 0;
        this.max = 1; // each time , multiply by factorail(eqGroupSize)
        for (
            int eqGroup = 0;
            eqGroup < equalityGroupsSizesArray.length;
            eqGroup++)
        {
            // create an array of eq.group size filled with values
            // of counter, counter+1, ... counter+size-1
            int currGroupSize = equalityGroupsSizesArray[eqGroup];
            int [] currArray = new int[currGroupSize];
            for (int i = 0; i < currGroupSize; i++) {
                currArray[i] = counter;
                counter++;
            }
            this.permArray[eqGroup] = new IntegerPermutationIter(currArray);
            this.permArray[eqGroup].getNext(); // first iteration return the
                                               // source

            // each time , multiply by factorail(eqGroupSize)
            this.max *= MathUtil.factorial(currGroupSize);
        }
        this.totalPermArraySize = counter;

        // calc max
    }

    public Object next()
    {
        return getNext();
    }

    /**
     * Iteration may be one of these two: 1. the last group advances by one
     * iter, all else stay. 2. the last group cannot advance , so it restarts
     * but telling the group after it to advance (done recursively till some
     * group can advance)
     */
    public int [] getNext()
    {
        if (this.iterCounter == 0) {
            // just return it , without change
            this.iterCounter++;
            return getPermAsArray();
        }

        int firstGroupCapableOfAdvancing = -1;
        int currGroupIndex = 0; //
        while (firstGroupCapableOfAdvancing == -1) {
            IntegerPermutationIter currGroup = this.permArray[currGroupIndex];

            if (currGroup.hasNext()) {
                currGroup.getNext();

                // restart all that we passed on
                for (int i = 0; i < currGroupIndex; i++) {
                    restartPermutationGroup(i);
                }
                firstGroupCapableOfAdvancing = currGroupIndex;
            }

            currGroupIndex++;
            if (currGroupIndex >= this.permArray.length) {
                break;
            }
        }

        this.iterCounter++;

        if (firstGroupCapableOfAdvancing == -1) {
            // nothing found. we finished all iterations
            return null;
        } else {
            int [] tempArray = getPermAsArray();
            return tempArray;
        }
    }

    /**
     * Creates and returns a new array which consists of the eq. group current
     * permutation arrays. For example, in the 10th iter ([$,2,1,b,c,a]) The
     * permutations current statuses is [0] [2,1] [4,5,3] so retrieve
     * [0,2,1,4,5,3]
     */
    public int [] getPermAsArray()
    {
        int [] resultArray = new int[this.totalPermArraySize];
        int counter = 0;
        for (
            int groupIndex = 0;
            groupIndex < this.permArray.length;
            groupIndex++)
        {
            int [] currPermArray = this.permArray[groupIndex].getCurrent();
            System.arraycopy(
                currPermArray,
                0,
                resultArray,
                counter,
                currPermArray.length);
            counter += currPermArray.length;
        }
        return resultArray;
    }

    /**
     * Restarts by creating a new one instead.
     *
     * @param groupIndex
     */
    private void restartPermutationGroup(int groupIndex)
    {
        int [] oldPermArray = this.permArray[groupIndex].getCurrent();
        Arrays.sort(oldPermArray);
        this.permArray[groupIndex] = new IntegerPermutationIter(oldPermArray);
        this.permArray[groupIndex].getNext();
    }

    public boolean hasNext()
    {
        boolean result;
        if (this.iterCounter < this.max) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public int getMax()
    {
        return max;
    }

    /* (non-Javadoc)
     * @see ArrayPermutationsIter#nextPermutation()
     */
    public int [] nextPermutation()
    {
        return (int []) next();
    }

    /* (non-Javadoc)
     * @see ArrayPermutationsIter#hasNextPermutaions()
     */
    public boolean hasNextPermutaions()
    {
        return hasNext();
    }

    /**
     * UNIMPLEMENTED. always throws new UnsupportedOperationException
     *
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}

// End CompoundPermutationIter.java
