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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jgrapht.alg.AllAlgTests;
import org.jgrapht.alg.util.AllAlgUtilTests;
import org.jgrapht.generate.AllGenerateTests;
import org.jgrapht.graph.AllGraphTests;
import org.jgrapht.traverse.AllTraverseTests;
import org.jgrapht.util.AllUtilTests;

import java.util.Enumeration;


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
        final ExpandableTestSuite suite =
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
        }

        /**
         * @see TestSuite#TestSuite(String)
         */
        public ExpandableTestSuite(final String name)
        {
            super(name);
        }

        /**
         * Adds all the test from the specified suite into this suite.
         *
         * @param suite
         */
        public void addTestSuit(final TestSuite suite)
        {
            for (Enumeration e = suite.tests(); e.hasMoreElements();) {
                final Test t = (Test) e.nextElement();
                addTest(t);
            }
        }
    }
}

// End AllTests.java
