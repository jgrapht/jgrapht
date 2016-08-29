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
/* ------------------------------
 * DSVUtilsTest.java
 * ------------------------------
 * (C) Copyright 2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 * Contributors: -
 *
 * Changes
 * -------
 * 20-Aug-2016 : Initial revision (DM);
 *
 */
package org.jgrapht.ext;

import junit.framework.TestCase;

/**
 * .
 * 
 * @author Dimitrios Michail
 */
public class DSVUtilsTest
    extends TestCase
{

    public void testEscape()
    {
        String input1 = "nothing special in here";
        assertEquals(input1, DSVUtils.escapeDSV(input1, ';'));
        assertEquals(input1, DSVUtils.escapeDSV(input1, ','));

        String input2 = "foo;;;";
        assertEquals(input2, DSVUtils.escapeDSV(input2, ','));
        assertEquals("\"foo;;;\"", DSVUtils.escapeDSV(input2, ';'));

        String input3 = "foo\n";
        assertEquals("\"foo\n\"", DSVUtils.escapeDSV(input3, ';'));

        String input4 = "foo\rfoo";
        assertEquals("\"foo\rfoo\"", DSVUtils.escapeDSV(input4, ';'));

        String input5 = "\"foo\"\n\"foo\"";
        assertEquals(
            "\"\"\"foo\"\"\n\"\"foo\"\"\"",
            DSVUtils.escapeDSV(input5, ';'));
    }

    public void testUnescape()
    {
        String input1 = "nothing special in here";
        assertEquals(input1, DSVUtils.unescapeDSV(input1, ';'));
        assertEquals(input1, DSVUtils.unescapeDSV(input1, ','));

        String input2 = "\"foo;;;\"";
        assertEquals("foo;;;", DSVUtils.unescapeDSV(input2, ';'));
        assertEquals("\"foo;;;\"", DSVUtils.unescapeDSV(input2, ','));

        String input3 = "\"foo\n\"";
        assertEquals("foo\n", DSVUtils.unescapeDSV(input3, ';'));

        String input4 = "\"foo\rfoo\"";
        assertEquals("foo\rfoo", DSVUtils.unescapeDSV(input4, ';'));

        String input5 = "\"\"\"foo\"\"\n\"\"foo\"\"\"";
        assertEquals("\"foo\"\n\"foo\"", DSVUtils.unescapeDSV(input5, ';'));
    }

}

// End DSVUtilsTest.java
