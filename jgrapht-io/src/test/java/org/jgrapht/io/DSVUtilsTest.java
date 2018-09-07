/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * .
 * 
 * @author Dimitrios Michail
 */
public class DSVUtilsTest
{

    @Test
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
        assertEquals("\"\"\"foo\"\"\n\"\"foo\"\"\"", DSVUtils.escapeDSV(input5, ';'));
    }

    @Test
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
