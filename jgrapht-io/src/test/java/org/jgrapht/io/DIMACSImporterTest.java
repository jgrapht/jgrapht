/*
 * (C) Copyright 2016-2017, by Joris Kinable, Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author Joris Kinable
 * @author Dimitrios Michail
 */
public class DIMACSImporterTest
{

    public <E> Graph<Integer, E> readGraph(
        InputStream in, Class<? extends E> edgeClass, boolean weighted)
        throws ImportException
    {
        Graph<Integer, E> g;
        if (weighted) {
            g = new DirectedWeightedPseudograph<Integer, E>(edgeClass);
        } else {
            g = new DirectedPseudograph<Integer, E>(edgeClass);
        }

        DIMACSImporter<Integer, E> importer = new DIMACSImporter<>(
            (l, a) -> Integer.parseInt(l), (f, t, l, a) -> g.getEdgeSupplier().get());
        try {
            importer.importGraph(g, new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // cannot happen
        }

        return g;
    }

    /**
     * Read and parse an actual instance
     */
    @Test
    public void testReadDIMACSInstance()
        throws ImportException
    {
        InputStream fstream = getClass().getClassLoader().getResourceAsStream("myciel3.col");
        Graph<Integer, DefaultEdge> graph = readGraph(fstream, DefaultEdge.class, false);

        assertEquals(graph.vertexSet().size(), 11);
        assertEquals(graph.edgeSet().size(), 20);

        int[][] edges = { { 1, 2 }, { 1, 4 }, { 1, 7 }, { 1, 9 }, { 2, 3 }, { 2, 6 }, { 2, 8 },
            { 3, 5 }, { 3, 7 }, { 3, 10 }, { 4, 5 }, { 4, 6 }, { 4, 10 }, { 5, 8 }, { 5, 9 },
            { 6, 11 }, { 7, 11 }, { 8, 11 }, { 9, 11 }, { 10, 11 } };
        for (int[] edge : edges)
            assertTrue(graph.containsEdge(edge[0], edge[1]));
    }

    /**
     * Read and parse an weighted instance
     */
    @Test
    public void testReadWeightedDIMACSInstance()
        throws ImportException
    {
        InputStream fstream =
            getClass().getClassLoader().getResourceAsStream("myciel3_weighted.col");
        Graph<Integer, DefaultWeightedEdge> graph =
            readGraph(fstream, DefaultWeightedEdge.class, true);

        assertEquals(graph.vertexSet().size(), 11);
        assertEquals(graph.edgeSet().size(), 20);

        int[][] edges = { { 1, 2, 1 }, { 1, 4, 2 }, { 1, 7, 3 }, { 1, 9, 4 }, { 2, 3, 5 },
            { 2, 6, 6 }, { 2, 8, 7 }, { 3, 5, 8 }, { 3, 7, 9 }, { 3, 10, 10 }, { 4, 5, 11 },
            { 4, 6, 12 }, { 4, 10, 13 }, { 5, 8, 14 }, { 5, 9, 15 }, { 6, 11, 16 }, { 7, 11, 17 },
            { 8, 11, 18 }, { 9, 11, 19 }, { 10, 11, 20 } };

        for (int[] edge : edges) {
            assertTrue(graph.containsEdge(edge[0], edge[1]));
            DefaultWeightedEdge e = graph.getEdge(edge[0], edge[1]);
            assertEquals((int) graph.getEdgeWeight(e), edge[2]);
        }
    }

    @Test
    public void testReadDIMACSShortestPathFormat()
        throws ImportException
    {
        // @formatter:off
        String input = "p sp 3 3\n" +
                       "a 1 2\n" +
                       "a 2 1\n" +
                       "a 2 3\n";
        // @formatter:on

        Graph<Integer,
            DefaultWeightedEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                DefaultWeightedEdge.class, false);

        assertEquals(3, graph.vertexSet().size());
        assertEquals(3, graph.edgeSet().size());

        int[][] edges = { { 1, 2, 1 }, { 2, 1, 1 }, { 2, 3, 1 } };
        for (int[] edge : edges) {
            assertTrue(graph.containsEdge(edge[0], edge[1]));
            DefaultWeightedEdge e = graph.getEdge(edge[0], edge[1]);
            assertEquals((int) graph.getEdgeWeight(e), edge[2]);
        }
    }

    @Test
    public void testWrongDIMACSInstance1()
        throws ImportException
    {
        // @formatter:off
        String input = "p edge ERROR 5\n"
                     + "e 1 2\n"
                     + "e 1 4\n";
        // @formatter:on

        try {
            readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testWrongDIMACSInstance2()
        throws ImportException
    {
        // @formatter:off
        String input = "p edge -10 5\n"
                     + "e 1 2\n"
                     + "e 1 4\n";
        // @formatter:on

        try {
            readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testWrongDIMACSInstance3()
        throws ImportException
    {
        // @formatter:off
        String input = "p edge 2 5\n"
                     + "e 1 2\n"
                     + "e 1 4\n";
        // @formatter:on

        try {
            readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testWrongDIMACSInstance4()
        throws ImportException
    {
        // @formatter:off
        String input = "p edge 2 2\n"
                     + "e 2\n"
                     + "e 1 2\n";
        // @formatter:on

        try {
            readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

}
