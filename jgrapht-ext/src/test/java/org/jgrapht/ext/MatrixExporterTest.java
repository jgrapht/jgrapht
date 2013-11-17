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
 * MatrixExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 12-Dec-2005 : Initial revision (CF);
 *
 */
package org.jgrapht.ext;

import java.io.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Charles Fry
 */
public class MatrixExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String LAPLACIAN =
        "1 1 2" + NL
        + "1 2 -1" + NL
        + "1 3 -1" + NL
        + "2 2 1" + NL
        + "2 1 -1" + NL
        + "3 3 1" + NL
        + "3 1 -1" + NL;

    private static final String NORMALIZED_LAPLACIAN =
        "1 1 1" + NL
        + "1 2 -0.7071067811865475" + NL
        + "1 3 -0.7071067811865475" + NL
        + "2 2 1" + NL
        + "2 1 -0.7071067811865475" + NL
        + "3 3 1" + NL
        + "3 1 -0.7071067811865475" + NL;

    private static final String UNDIRECTED_ADJACENCY =
        "1 2 1" + NL
        + "1 3 1" + NL
        + "1 1 2" + NL
        + "2 1 1" + NL
        + "3 1 1" + NL;

    private static final String DIRECTED_ADJACENCY =
        "1 2 1" + NL
        + "3 1 2" + NL;

    private static final MatrixExporter<String, DefaultEdge> exporter =
        new MatrixExporter<String, DefaultEdge>();

    //~ Methods ----------------------------------------------------------------

    public void testLaplacian()
    {
        UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        StringWriter w = new StringWriter();
        exporter.exportLaplacianMatrix(w, g);
        assertEquals(LAPLACIAN, w.toString());

        w = new StringWriter();
        exporter.exportNormalizedLaplacianMatrix(w, g);
        assertEquals(NORMALIZED_LAPLACIAN, w.toString());
    }

    public void testAdjacencyUndirected()
    {
        UndirectedGraph<String, DefaultEdge> g =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V1, V1);

        StringWriter w = new StringWriter();
        exporter.exportAdjacencyMatrix(w, g);
        assertEquals(UNDIRECTED_ADJACENCY, w.toString());
    }

    public void testAdjacencyDirected()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V3, V1);

        Writer w = new StringWriter();
        exporter.exportAdjacencyMatrix(w, g);
        assertEquals(DIRECTED_ADJACENCY, w.toString());
    }
}

// End MatrixExporterTest.java
