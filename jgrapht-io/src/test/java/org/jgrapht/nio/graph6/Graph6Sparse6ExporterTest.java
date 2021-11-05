/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.nio.graph6;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Graph6Sparse6Exporter
 *
 * @author Joris Kinable
 */
public class Graph6Sparse6ExporterTest
{

    // -------------------Sparse6 tests--------------------

    @Test
    public void testEmptyGraph()
        throws UnsupportedEncodingException,
        ExportException
    {
        Graph<Integer, DefaultEdge> orig = new SimpleGraph<>(DefaultEdge.class);
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        assertEquals(":?", res);
    }

    @Test
    public void testGraphWithoutEdges()
        throws UnsupportedEncodingException,
        ExportException
    {
        Graph<Integer, DefaultEdge> orig = new SimpleGraph<>(DefaultEdge.class);
        orig.addVertex(0);
        orig.addVertex(1);
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        assertEquals(":A", res);
    }

    @Test
    public void testExampleGraph()
        throws UnsupportedEncodingException,
        ExportException
    {
        Graph<Integer, DefaultEdge> orig = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(orig, Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        orig.addEdge(0, 1);
        orig.addEdge(0, 2);
        orig.addEdge(1, 2);
        orig.addEdge(5, 6);
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        assertEquals(":Fa@x^", res);
    }

    @Test
    public void testGraph1a()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.petersenGraph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testGraph2a()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.ellinghamHorton78Graph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testGraph3a()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.klein3RegularGraph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testPseudoGraph()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(orig, Arrays.asList(0, 1, 2));
        orig.addEdge(0, 1);
        orig.addEdge(0, 1);
        orig.addEdge(1, 2);
        orig.addEdge(2, 0);
        orig.addEdge(2, 2);

        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);

        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testRandomGraphsS6()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        GnpRandomGraphGenerator<Integer, DefaultEdge> gnp =
            new GnpRandomGraphGenerator<>(40, .55, 0, true);
        for (int i = 0; i < 20; i++) {
            Graph<Integer,
                DefaultEdge> orig = new Pseudograph<>(
                    SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                    false);
            gnp.generateGraph(orig);

            String res = exportGraph(orig, Graph6Sparse6Exporter.Format.SPARSE6);

            Graph<Integer, DefaultEdge> g = importGraph(res);
            this.compare(orig, g);
        }
    }

    // -------------------Graph6 tests--------------------

    @Test
    public void testGraph1b()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.petersenGraph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.GRAPH6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testGraph2b()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.ellinghamHorton78Graph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.GRAPH6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testGraph3b()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        Graph<Integer, DefaultEdge> orig = NamedGraphGenerator.klein3RegularGraph();
        String res = exportGraph(orig, Graph6Sparse6Exporter.Format.GRAPH6);
        Graph<Integer, DefaultEdge> g = importGraph(res);
        this.compare(orig, g);
    }

    @Test
    public void testRandomGraphsG6()
        throws UnsupportedEncodingException,
        ExportException,
        ImportException
    {
        GnpRandomGraphGenerator<Integer, DefaultEdge> gnp =
            new GnpRandomGraphGenerator<>(40, .55, 0);
        for (int i = 0; i < 20; i++) {
            Graph<Integer,
                DefaultEdge> orig = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                    false);
            gnp.generateGraph(orig);

            String res = exportGraph(orig, Graph6Sparse6Exporter.Format.GRAPH6);

            Graph<Integer, DefaultEdge> g = importGraph(res);
            this.compare(orig, g);
        }
    }

    // -------------------helper methods--------------------

    private <V, E> String exportGraph(Graph<V, E> g, Graph6Sparse6Exporter.Format format)
        throws UnsupportedEncodingException,
        ExportException
    {
        Graph6Sparse6Exporter<V, E> exporter = new Graph6Sparse6Exporter<>(format);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        return new String(os.toByteArray(), "UTF-8");
    }

    private Graph<Integer, DefaultEdge> importGraph(String g6)
        throws ImportException
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Graph6Sparse6Importer<Integer, DefaultEdge> importer = new Graph6Sparse6Importer<>();
        importer.importGraph(g, new ByteArrayInputStream(g6.getBytes(StandardCharsets.UTF_8)));
        return g;
    }

    private <V, E> void compare(Graph<V, E> orig, Graph<V, E> g)
    {
        assertEquals(orig.vertexSet().size(), g.vertexSet().size());
        assertEquals(orig.edgeSet().size(), g.edgeSet().size());

        // The original and output graph cannot be compared 1:1 since sparse6/graph6 encodings do
        // not preserve vertex labels
        // Testing for graph isomorphism is hard, so we compare characteristics.
        int[] degeeVectorOrig = new int[orig.edgeSet().size()];
        for (V v : orig.vertexSet())
            degeeVectorOrig[orig.degreeOf(v)]++;

        int[] degeeVectorG = new int[g.edgeSet().size()];
        for (V v : g.vertexSet())
            degeeVectorG[g.degreeOf(v)]++;

        assertTrue(Arrays.equals(degeeVectorOrig, degeeVectorG));

        assertEquals(GraphMetrics.getRadius(orig), GraphMetrics.getRadius(g), 0.00000001);
        assertEquals(GraphMetrics.getDiameter(orig), GraphMetrics.getDiameter(g), 0.00000001);
        assertEquals(GraphMetrics.getGirth(orig), GraphMetrics.getGirth(g), 0.00000001);
    }
}
