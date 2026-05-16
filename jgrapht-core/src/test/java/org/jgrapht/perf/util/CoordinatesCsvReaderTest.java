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
package org.jgrapht.perf.util;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CoordinatesCsvReader}.
 *
 * @author Shai Eilat
 */
class CoordinatesCsvReaderTest
{

    @Test
    void readsBasicCoordinates() throws IOException
    {
        String csv = "node,lat,lon\n0,42.5,1.5\n1,42.51,1.51\n";
        Map<Integer, double[]> out = new CoordinatesCsvReader<Integer>()
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, out.size());
        assertArrayEquals(new double[] { 42.5, 1.5 }, out.get(0), 0.0);
        assertArrayEquals(new double[] { 42.51, 1.51 }, out.get(1), 0.0);
    }

    @Test
    void readsGzippedStream() throws IOException
    {
        String csv = "node,lat,lon\n0,1.0,2.0\n";
        byte[] gz;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream out = new GZIPOutputStream(baos))
        {
            out.write(csv.getBytes(StandardCharsets.UTF_8));
            out.finish();
            gz = baos.toByteArray();
        }

        Map<Integer, double[]> coords = new CoordinatesCsvReader<Integer>()
            .gzipped(true)
            .read(new ByteArrayInputStream(gz));

        assertEquals(1, coords.size());
        assertArrayEquals(new double[] { 1.0, 2.0 }, coords.get(0), 0.0);
    }

    @Test
    void respectsHasHeaderFalse() throws IOException
    {
        String csv = "0,1.0,2.0\n1,3.0,4.0\n";
        Map<Integer, double[]> out = new CoordinatesCsvReader<Integer>()
            .hasHeader(false)
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, out.size());
    }

    @Test
    void reportsLineNumberOnParseError()
    {
        String csv = "node,lat,lon\n0,1.0,2.0\nbroken-line\n";

        IOException ex = assertThrows(IOException.class, () ->
            new CoordinatesCsvReader<Integer>()
                .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertTrue(ex.getMessage().contains("line 3"), "line number in error");
    }

    @Test
    void readsCustomVertexParser() throws IOException
    {
        String csv = "node,lat,lon\nA,1.0,2.0\nB,3.0,4.0\n";
        Map<String, double[]> out = new CoordinatesCsvReader<String>()
            .vertexParser(String::trim)
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, out.size());
        assertArrayEquals(new double[] { 1.0, 2.0 }, out.get("A"), 0.0);
        assertArrayEquals(new double[] { 3.0, 4.0 }, out.get("B"), 0.0);
    }

    @Test
    void readResourceThrowsForMissingResource()
    {
        assertThrows(
            IOException.class,
            () -> new CoordinatesCsvReader<Integer>()
                .readResource(getClass(), "/does-not-exist.csv"));
    }
}
