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
/* ----------------
 * AllTraverseTests.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.traverse;

import junit.framework.*;


/**
 * A TestSuite for all tests in this package.
 *
 * @author Barak Naveh
 */
public final class AllTraverseTests
{
    //~ Constructors -----------------------------------------------------------

    private AllTraverseTests()
    {
    } // ensure non-instantiability.

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a test suite for all tests in this package.
     *
     * @return a test suite for all tests in this package.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // $JUnit-BEGIN$
        suite.addTest(new TestSuite(BreadthFirstIteratorTest.class));
        suite.addTest(new TestSuite(DepthFirstIteratorTest.class));
        suite.addTest(new TestSuite(ClosestFirstIteratorTest.class));
        suite.addTest(new TestSuite(IgnoreDirectionTest.class));
        suite.addTest(new TestSuite(TopologicalOrderIteratorTest.class));

        // $JUnit-END$
        return suite;
    }
}

// End AllTraverseTests.java
