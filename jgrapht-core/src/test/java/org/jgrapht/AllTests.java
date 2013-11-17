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
/* -------------
 * AllTests.java
 * -------------
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
package org.jgrapht;

import java.util.*;

import junit.framework.*;

import org.jgrapht.alg.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;


/**
 * Runs all unit tests of the JGraphT library.
 *
 * @author Barak Naveh
 */
public final class AllTests
{
    //~ Constructors -----------------------------------------------------------

    private AllTests()
    {
    } // ensure non-instantiability.

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a test suite that includes all JGraphT tests.
     *
     * @return a test suite that includes all JGraphT tests.
     */
    public static Test suite()
    {
        ExpandableTestSuite suite =
            new ExpandableTestSuite("All tests of JGraphT");

        suite.addTestSuit((TestSuite) AllAlgTests.suite());
        suite.addTestSuit((TestSuite) AllAlgUtilTests.suite());
        suite.addTestSuit((TestSuite) AllGenerateTests.suite());
        suite.addTestSuit((TestSuite) AllGraphTests.suite());
        suite.addTestSuit((TestSuite) AllTraverseTests.suite());
        suite.addTestSuit((TestSuite) AllUtilTests.suite());

        return suite;
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class ExpandableTestSuite
        extends TestSuite
    {
        /**
         * @see TestSuite#TestSuite()
         */
        public ExpandableTestSuite()
        {
            super();
        }

        /**
         * @see TestSuite#TestSuite(java.lang.String)
         */
        public ExpandableTestSuite(String name)
        {
            super(name);
        }

        /**
         * Adds all the test from the specified suite into this suite.
         *
         * @param suite
         */
        public void addTestSuit(TestSuite suite)
        {
            for (Enumeration e = suite.tests(); e.hasMoreElements();) {
                Test t = (Test) e.nextElement();
                this.addTest(t);
            }
        }
    }
}

// End AllTests.java
