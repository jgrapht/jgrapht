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
/* ---------------------
 * EnhancedTestCase.java
 * ---------------------
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

import junit.framework.*;


/**
 * A little extension to JUnit's TestCase.
 *
 * @author Barak Naveh
 * @since Jul 25, 2003
 */
public abstract class EnhancedTestCase
    extends TestCase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase()
     */
    public EnhancedTestCase()
    {
        super();
    }

    /**
     * @see TestCase#TestCase(java.lang.String)
     */
    public EnhancedTestCase(String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * It means: it's wrong that we got here.
     */
    public void assertFalse()
    {
        assertTrue(false);
    }

    /**
     * It means: it's right that we got here.
     */
    public void assertTrue()
    {
        assertTrue(true);
    }
}

// End EnhancedTestCase.java
