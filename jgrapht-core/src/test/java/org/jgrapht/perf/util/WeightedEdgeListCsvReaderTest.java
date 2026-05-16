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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.*;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WeightedEdgeListCsvReader}.
 *
 * @author Shai Eilat
 */
class WeightedEdgeListCsvReaderTest
{

    @Test
    void readsBasicEdgeList()
    {
        String csv = "src,dst,weight\n0,1,2.5\n1,2,7.0\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        readPlain(g, csv);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertEquals(2.5, g.getEdgeWeight(g.getEdge(0, 1)), 0.0);
        assertEquals(7.0, g.getEdgeWeight(g.getEdge(1, 2)), 0.0);
    }

    @Test
    void skipsBlankLines()
    {
        String csv = "src,dst,weight\n\n0,1,1.0\n\n1,2,2.0\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        readPlain(g, csv);

        assertEquals(2, g.edgeSet().size());
    }

    @Test
    void respectsNoHeaderFlag() throws IOException
    {
        String csv = "0,1,1.0\n1,2,2.0\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
            .hasHeader(false)
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, g.edgeSet().size());
    }

    @Test
    void readsGzippedStream() throws IOException
    {
        String csv = "src,dst,weight\n0,1,1.0\n1,2,2.0\n";
        byte[] gz;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream out = new GZIPOutputStream(baos))
        {
            out.write(csv.getBytes(StandardCharsets.UTF_8));
            out.finish();
            gz = baos.toByteArray();
        }

        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
            .gzipped(true)
            .read(new ByteArrayInputStream(gz));

        assertEquals(2, g.edgeSet().size());
    }

    @Test
    void honoursAddMissingVerticesFalse()
    {
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        g.addVertex(0);
        g.addVertex(1);
        String csv = "src,dst,weight\n0,1,1.0\n";

        new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
            .addMissingVertices(false)
            .hasHeader(true);
        readPlain(g, csv, false);

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
    }

    @Test
    void rejectsEdgeToMissingVertexWhenNotAddingMissing()
    {
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        g.addVertex(0);
        // vertex 1 deliberately missing
        String csv = "src,dst,weight\n0,1,1.0\n";

        IOException ex = assertThrows(IOException.class, () ->
            new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
                .addMissingVertices(false)
                .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertTrue(ex.getMessage().contains("line 2"), "line number in error");
    }

    @Test
    void reportsLineNumberOnParseError()
    {
        String csv = "src,dst,weight\n0,1,1.0\nbroken-line\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();

        IOException ex = assertThrows(IOException.class, () ->
            new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
                .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertTrue(ex.getMessage().contains("line 3"), "line number in error");
    }

    @Test
    void readsCustomDelimiter() throws IOException
    {
        String csv = "src;dst;weight\n0;1;1.0\n1;2;2.0\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
            .delimiter(';')
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, g.edgeSet().size());
    }

    @Test
    void readsCustomVertexParser() throws IOException
    {
        String csv = "src,dst,weight\nA,B,1.0\nB,C,2.0\n";
        Graph<String, DefaultWeightedEdge> g =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new WeightedEdgeListCsvReader<String, DefaultWeightedEdge>(g)
            .vertexParser(String::trim)
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertNotNull(g.getEdge("A", "B"));
        assertNotNull(g.getEdge("B", "C"));
    }

    @Test
    void readResourceThrowsForMissingResource()
    {
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        assertThrows(
            IOException.class,
            () -> new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
                .readResource(getClass(), "/does-not-exist.csv"));
    }

    @Test
    void readsScientificWeights() throws IOException
    {
        String csv = "src,dst,weight\n0,1,1.5e3\n";
        Graph<Integer, DefaultWeightedEdge> g = newDirected();
        new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
            .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1500.0, g.getEdgeWeight(g.getEdge(0, 1)), 0.0);
    }

    private static void readPlain(Graph<Integer, DefaultWeightedEdge> g, String csv)
    {
        readPlain(g, csv, true);
    }

    private static void readPlain(
        Graph<Integer, DefaultWeightedEdge> g, String csv, boolean addMissing)
    {
        try {
            new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(g)
                .addMissingVertices(addMissing)
                .read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static Graph<Integer, DefaultWeightedEdge> newDirected()
    {
        return new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }
}
