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
 * IntegerPermutationIter.java
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
 * Iterates through permutations of N elements.
 * <li>use getNext() to get the next permutation order, for example(N=4):
 * perm0=[1,2,3,4] perm1=[1,2,4,3] perm2=[1,3,2,4] .
 * <li>use hasNext() or verify by counter<getTotalNumberOfPermutations () that
 * you do not overflow the max number. RunTimeException will be thrown if you
 * do. Getting the next permutation is done in O(N) Note: This class is not
 * thread safe.
 *
 * @author Assaf
 * @since May 20, 2005
 */
public class IntegerPermutationIter
    implements Iterator,
        ArrayPermutationsIter
{
    

    private int [] Value;
    private int N;
    private long permutationCounter;
    private boolean endWasReached = false;
    private boolean wasNextValueCalculatedAlready = false;

    /**
     * Will hold the current value. Will be changed only by getNext()
     */
    private int [] currentValueBackup;

    

    /**
     * Creates an array of size N with elements 1,2,...,n-1 Useful for
     * describing regular permutations. See IntegerPermutationIter(int[] array)
     * for the other kind of permutations; efficency of initiation is O(N)
     *
     * @param N
     */
    public IntegerPermutationIter(int N)
    {
        int [] newArray = new int[N];

        // fill the array with 1,2,3...
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = i;
        }
        init(newArray);
    }

    /**
     * Uses a predefined array (sorted), for example: [3,1,1,2,1]-->[1,1,1,2,3];
     * note that there are much less than 5! premutations here, because of the
     * repetitive 1s.
     *
     * @param array creates a copy of it (so sort / later changes will not
     * matter)
     */
    public IntegerPermutationIter(int [] array)
    {
        int [] newArray = new int[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        Arrays.sort(newArray);
        init(newArray);
    }

    

    private void init(int [] array)
    {
        this.N = array.length;
        this.Value = array;
        this.currentValueBackup = this.Value;
        permutationCounter = 0;
    }

    /**
     * Swaps by array indexes
     *
     * @param i
     * @param j
     */
    private void swap(int i, int j)
    {
        int temp = this.Value[i];
        this.Value[i] = this.Value[j];
        this.Value[j] = temp;
    }

    private int [] arrayClone(int [] sourceArray)
    {
        int [] destArray = new int[sourceArray.length];
        System.arraycopy(sourceArray, 0, destArray, 0, sourceArray.length);
        return destArray;
    }

    private int [] getNextStartingWith2()
    {
        permutationCounter++;
        int i = N - 1;

        if (i <= 0) // may happen only on N<=1

        {
            this.endWasReached = true;
            return null;
        }

        /** while (Value[i-1] >= Value[i])
          {
          i = i-1;
          }*/
        while (Value[i - 1] >= Value[i]) {
            i = i - 1;
            if (i == 0) {
                this.endWasReached = true;
                return null;
            }
        }

        int j = N;

        while (Value[j - 1] <= Value[i - 1]) {
            j = j - 1;
        }

        swap(i - 1, j - 1); // swap values at positions (i-1) and (j-1)

        i++;
        j = N;

        while (i < j) {
            swap(i - 1, j - 1);
            i++;
            j--;
        }
        return this.Value;
    }

    /**
     * Efficiency: O(N) implementation, try to take the next!
     */
    public boolean hasNext()
    {
        if ((this.permutationCounter == 0)
            || (this.wasNextValueCalculatedAlready))
        {
            return true;
        } else if (this.endWasReached) {
            return false;
        }

        boolean result = true;
        // calculate the next value into this.value  save the current result. in
        // the end swap the arrays there is no way to know when to stop , but
        // the out-of-bound
        /*  try
         *  {
         *      this.wasNextValueCalculatedAlready=true;
         *      getNextStartingWith2();
         *  }
         *  catch (ArrayIndexOutOfBoundsException outOfBoundException)
         *  {
         *      endWasReached=true;
         *      result=false;
         *  }*/

        getNextStartingWith2();
        this.wasNextValueCalculatedAlready = true;
        if (endWasReached) {
            return false;
        }

        //////////////////////////////
        return result;
    }

    public Object next()
    {
        return getNext();
    }

    /**
     * Facade. use it with getNext. efficency: O(N)
     *
     * @return a new Array with the permutatation order. for example:
     * perm0=[1,2,3,4] perm1=[1,2,4,3] perm2=[1,3,2,4]
     */
    public int [] getNext()
    {
        if (!hasNext()) {
            throw new RuntimeException(
                "IntegerPermutationIter exceeds the total number of permutaions."
                + " Suggestion: do a check with hasNext() , or count till getTotalNumberOfPermutations"
                + " before using getNext()");
        }

        // if it is the first one , return original
        int [] internalArray;
        if (this.permutationCounter == 0) {
            this.permutationCounter++;
            internalArray = this.Value;
        } else {
            // if hasNext() has precaclulated it , take this value.
            if (this.wasNextValueCalculatedAlready) {
                internalArray = this.Value;
                this.wasNextValueCalculatedAlready = false;
            } else {
                internalArray = getNextStartingWith2();
                if (this.endWasReached) {
                    return null;
                }
            }
        }
        this.currentValueBackup = arrayClone(internalArray);
        return arrayClone(internalArray);
    }

    public int [] getCurrent()
    {
        return arrayClone(this.currentValueBackup);
    }

    /**
     * Utility method to convert the array into a string examples: [] [0]
     * [0,1][1,0]
     *
     * @param array
     */
    public String toString(int [] array)
    {
        if (array.length <= 0) {
            return "[]";
        }
        StringBuffer stBuffer = new StringBuffer("[");
        for (int i = 0; i < (array.length - 1); i++) {
            stBuffer.append(array[i]).append(",");
        }
        stBuffer.append(array[array.length - 1]).append("]");
        return stBuffer.toString();
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
}

// End IntegerPermutationIter.java
