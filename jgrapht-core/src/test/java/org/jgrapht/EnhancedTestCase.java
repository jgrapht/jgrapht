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

import java.lang.String;


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
    }

    /**
     * @see TestCase#TestCase(String)
     */
    public EnhancedTestCase(final String name)
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
