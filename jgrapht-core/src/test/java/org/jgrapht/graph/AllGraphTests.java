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
 * AllGraphTests.java
 * -----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Aug-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.graph;

import junit.framework.*;


/**
 * A TestSuite for all tests in this package.
 *
 * @author Barak Naveh
 * @since Aug 3, 2003
 */
public final class AllGraphTests
{
    //~ Constructors -----------------------------------------------------------

    private AllGraphTests()
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
        suite.addTest(new TestSuite(DefaultDirectedGraphTest.class));
        suite.addTest(new TestSuite(ListenableGraphTest.class));
        suite.addTest(new TestSuite(SimpleDirectedGraphTest.class));
        suite.addTest(new TestSuite(AsUndirectedGraphTest.class));
        suite.addTest(new TestSuite(AsUnweightedGraphTest.class));
        suite.addTest(new TestSuite(CloneTest.class));
        suite.addTest(new TestSuite(SerializationTest.class));
        suite.addTest(new TestSuite(GenericGraphsTest.class));
        suite.addTest(new TestSuite(EqualsAndHashCodeTest.class));
        // $JUnit-END$
        return suite;
    }
}

// End AllGraphTests.java
