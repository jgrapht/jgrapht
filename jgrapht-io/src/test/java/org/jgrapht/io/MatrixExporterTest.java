/*
 * (C) Copyright 2003-2017, by Charles Fry and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author Charles Fry
 */
public class MatrixExporterTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    private static final String LAPLACIAN = "1 1 2" + NL + "1 2 -1" + NL + "1 3 -1" + NL + "2 2 1"
        + NL + "2 1 -1" + NL + "3 3 1" + NL + "3 1 -1" + NL;

    private static final String NORMALIZED_LAPLACIAN =
        "1 1 1" + NL + "1 2 -0.7071067811865475" + NL + "1 3 -0.7071067811865475" + NL + "2 2 1"
            + NL + "2 1 -0.7071067811865475" + NL + "3 3 1" + NL + "3 1 -0.7071067811865475" + NL;

    private static final String UNDIRECTED_ADJACENCY =
        "1 2 1" + NL + "1 3 1" + NL + "1 1 2" + NL + "2 1 1" + NL + "3 1 1" + NL;

    private static final String DIRECTED_ADJACENCY = "1 2 1" + NL + "3 1 2" + NL;

    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testLaplacian()
        throws ExportException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphExporter<String, DefaultEdge> exporter1 =
            new MatrixExporter<>(MatrixExporter.Format.SPARSE_LAPLACIAN_MATRIX);
        StringWriter w1 = new StringWriter();
        exporter1.exportGraph(g, w1);
        assertEquals(LAPLACIAN, w1.toString());

        GraphExporter<String, DefaultEdge> exporter2 =
            new MatrixExporter<>(MatrixExporter.Format.SPARSE_NORMALIZED_LAPLACIAN_MATRIX);
        StringWriter w2 = new StringWriter();
        exporter2.exportGraph(g, w2);
        assertEquals(NORMALIZED_LAPLACIAN, w2.toString());
    }

    @Test
    public void testAdjacencyUndirected()
        throws ExportException
    {
        Graph<String, DefaultEdge> g = new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V1, V1);

        GraphExporter<String, DefaultEdge> exporter = new MatrixExporter<>();
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(UNDIRECTED_ADJACENCY, w.toString());
    }

    @Test
    public void testAdjacencyDirected()
        throws ExportException
    {
        Graph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V3, V1);

        GraphExporter<String, DefaultEdge> exporter = new MatrixExporter<>();
        Writer w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_ADJACENCY, w.toString());
    }
}

// End MatrixExporterTest.java
