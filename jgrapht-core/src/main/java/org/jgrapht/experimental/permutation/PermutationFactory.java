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
 * PermutationFactory.java
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

/**
 * Factory to create Permutations of several types and use them as Enumerations.
 * Note that callers may use them directly if they need to use special concrete
 * methods.
 *
 * <p>These types are:
 *
 * <p>
 * <li>All elements are different. There are N! possible permutations.
 *
 * <p><i>example:</i> source=[1,2,3]
 * result=[1,2,3][1,3,2][2,1,3][2,3,1][3,1,2][3,2,1]
 *
 * <p>
 * <li>Some of the elements are the same.
 *
 * <p><i>example:</i> source=[1,1,2] result=[1,1,2][1,2,1][2,1,1]
 *
 * <p>
 * <li>There are separate permutations groups, which are connected to one
 * sequence. Permutations are allowed only inside the group. Possible sequences:
 * product of factorial of each group. see example.
 *
 * <p><i>example:</i> assume source=the groups are sizes are : 1,2,2,5 elements
 * will be created: (1),(2,3),(4,5).
 *
 * <p>result=[1,(2,3),(4,5)] [1,(2,3),(5,4)] [1,(3,2),(5,4)] [1,(3,2),(4,5)]. In
 * this example the number of possiblities is 1! x 2! x 2! = 4
 *
 * @author Assaf Lehr
 * @since Jun 3, 2005
 */
public class PermutationFactory
{
    

    public static ArrayPermutationsIter createRegular(int [] permSourceArray)
    {
        IntegerPermutationIter regularPerm =
            new IntegerPermutationIter(permSourceArray);
        return regularPerm;
    }

    /**
     * For efficiency, try putting the biggest groups at the beggining of the
     * array.
     *
     * @param groupSizesArray . example [3,2] will create an array (0,1,2)(3,4)
     */
    public static ArrayPermutationsIter createByGroups(
        int [] groupSizesArray)
    {
        CompoundPermutationIter complexPerm =
            new CompoundPermutationIter(groupSizesArray);
        return complexPerm;
    }
}

// End PermutationFactory.java
