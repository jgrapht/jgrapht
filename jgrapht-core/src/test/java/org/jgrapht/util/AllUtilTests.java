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

import junit.framework.*;

import org.jgrapht.experimental.equivalence.*;
import org.jgrapht.experimental.permutation.*;


public class AllUtilTests
{
    //~ Methods ----------------------------------------------------------------

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for org.jgrapht.util");

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
