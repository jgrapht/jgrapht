/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.io;

import org.junit.*;

import static org.junit.Assert.assertEquals;

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
