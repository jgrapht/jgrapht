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
 * CompoundPermutationIterTest.java
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

import junit.framework.TestCase;

import java.util.Arrays;


/**
 * @author Assaf
 * @since May 30, 2005
 */
public class CompoundPermutationIterTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    private CompoundPermutationIter complexPerm;

    //~ Methods ----------------------------------------------------------------

    /**
     * Asserts that the number of permutations is the same as getMax. It also
     * verifies that the number is the same when using different internal order
     * of the permutation components. Note: The prints and timer can be unmarked
     * to see performance results and the permutations array themselves.
     */
    public void testGetNext()
    {
        // System.out.println("testing complex perm {1,1,1,2,2,3,4,5} ");
        // StopperTimer timer = new StopperTimer();
        // timer.start();

        complexPerm =
            new CompoundPermutationIter(
                new int[] {
                    1,
                    1,
                    1,
                    2,
                    2,
                    3,
                    4,
                    5
                });
        final int maxPermNum = complexPerm.getMax();

        // System.out.println(Arrays.toString(this.complexPerm.getPermAsArray()));
        int counter = 0;
        while (complexPerm.hasNext()) {
            final int [] resultArray = complexPerm.getNext();

            if (false) {
                System.out.println(Arrays.toString(resultArray));
            }
            counter++;
        }

        // System.out.println(counter);
        assertEquals(maxPermNum, counter);

        // timer.stopAndReport();

        // timer.start();
        complexPerm =
            new CompoundPermutationIter(
                new int[] {
                    5,
                    4,
                    3,
                    2,
                    2,
                    1,
                    1,
                    1
                });

        // System.out.println("testing complex perm {5,4,3,2,2,1,1,1} ");
        // System.out.println(Arrays.toString(this.complexPerm.getPermAsArray()));
        counter = 0;
        while (complexPerm.hasNext()) {
            final int [] resultArray = complexPerm.getNext();

            if (false) {
                System.out.println(Arrays.toString(resultArray));
            }
            counter++;
        }

        // System.out.println(counter);
        assertEquals(maxPermNum, counter);
        // timer.stopAndReport();
    }
}

// End CompoundPermutationIterTest.java
