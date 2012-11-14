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
    //~ Methods ----------------------------------------------------------------

    public static ArrayPermutationsIter createRegular(final int [] permSourceArray)
    {
        return new IntegerPermutationIter(permSourceArray);
    }

    /**
     * For efficiency, try putting the biggest groups at the beggining of the
     * array.
     *
     * @param groupSizesArray . example [3,2] will create an array (0,1,2)(3,4)
     */
    public static ArrayPermutationsIter createByGroups(
        final int [] groupSizesArray)
    {
        return new CompoundPermutationIter(groupSizesArray);
    }
}

// End PermutationFactory.java
