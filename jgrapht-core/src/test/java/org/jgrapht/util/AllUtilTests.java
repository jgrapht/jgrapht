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
 * AllUtilTests.java
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
package org.jgrapht.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jgrapht.experimental.equivalence.EquivalenceGroupCreatorTest;
import org.jgrapht.experimental.permutation.CompoundPermutationIterTest;


public class AllUtilTests
{
    //~ Methods ----------------------------------------------------------------

    public static Test suite()
    {
        final TestSuite suite = new TestSuite("Test for org.jgrapht.util");

        // $JUnit-BEGIN$
        suite.addTestSuite(FibonacciHeapTest.class);
        suite.addTestSuite(PrefetchIteratorTest.class);
        suite.addTestSuite(CompoundPermutationIterTest.class);
        suite.addTestSuite(EquivalenceGroupCreatorTest.class);

        // $JUnit-END$
        return suite;
    }
}

// End AllUtilTests.java
