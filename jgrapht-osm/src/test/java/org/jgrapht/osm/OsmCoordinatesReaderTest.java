/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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
package org.jgrapht.osm;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.jgrapht.osm.TestStreams.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OsmCoordinatesReader}.
 *
 * @author Shai Eilat
 */
class OsmCoordinatesReaderTest
{

    @Test
    void readsHeaderlessCoordinates() throws IOException
    {
        Map<Integer, double[]> out =
            OsmCoordinatesReader.readGzipped(gzipOf("0,42.5,1.5\n1,42.51,1.51\n"));

        assertEquals(2, out.size());
        assertArrayEquals(new double[] { 42.5, 1.5 }, out.get(0), 0.0);
        assertArrayEquals(new double[] { 42.51, 1.51 }, out.get(1), 0.0);
    }

    @Test
    void skipsBlankLines() throws IOException
    {
        Map<Integer, double[]> out = OsmCoordinatesReader.readGzipped(
            gzipOf("0,1.0,2.0\n\n1,3.0,4.0\n\n"));

        assertEquals(2, out.size());
    }

    @Test
    void reportsLineNumberOnParseError() throws IOException
    {
        IOException ex = assertThrows(
            IOException.class,
            () -> OsmCoordinatesReader.readGzipped(
                gzipOf("0,1.0,2.0\nbroken-line\n")));
        assertTrue(ex.getMessage().contains("line 2"), "line number in error");
    }

    @Test
    void throwsOnMissingResource()
    {
        assertThrows(
            IOException.class,
            () -> OsmCoordinatesReader.readGzippedResource(
                getClass(), "/does-not-exist.csv.gz"));
    }

}
