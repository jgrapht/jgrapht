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
 * GmlExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 23-Dec-2006 : Initial revision (JVS);
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
 * @author John V. Sichi
 */
public class GmlExporterTest
        extends TestCase {
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework
    private static final String UNDIRECTED
            = "Creator \"JGraphT GML Exporter\"" + NL
            + "Version 1" + NL
            + "graph [" + NL
            + "\tlabel \"\"" + NL
            + "\tdirected 0" + NL
            + "\tnode [" + NL
            + "\t\tid 1" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 2" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 3" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 1" + NL
            + "\t\tsource 1" + NL
            + "\t\ttarget 2" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 2" + NL
            + "\t\tsource 3" + NL
            + "\t\ttarget 1" + NL
            + "\t]" + NL
            + "]" + NL;

    private static final String UNDIRECTED_WEIGHTED
            = "Creator \"JGraphT GML Exporter\"" + NL
            + "Version 1" + NL
            + "graph [" + NL
            + "\tlabel \"\"" + NL
            + "\tdirected 0" + NL
            + "\tnode [" + NL
            + "\t\tid 1" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 2" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 3" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 1" + NL
            + "\t\tsource 1" + NL
            + "\t\ttarget 2" + NL
            + "\t\tweight 2.0" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 2" + NL
            + "\t\tsource 3" + NL
            + "\t\ttarget 1" + NL
            + "\t\tweight 5.0" + NL
            + "\t]" + NL
            + "]" + NL;

    private static final String DIRECTED
            = "Creator \"JGraphT GML Exporter\"" + NL
            + "Version 1" + NL
            + "graph [" + NL
            + "\tlabel \"\"" + NL
            + "\tdirected 1" + NL
            + "\tnode [" + NL
            + "\t\tid 1" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 2" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 3" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 4" + NL
            + "\t]" + NL
            + "\tnode [" + NL
            + "\t\tid 5" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 1" + NL
            + "\t\tsource 1" + NL
            + "\t\ttarget 2" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 2" + NL
            + "\t\tsource 3" + NL
            + "\t\ttarget 1" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 3" + NL
            + "\t\tsource 2" + NL
            + "\t\ttarget 3" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 4" + NL
            + "\t\tsource 3" + NL
            + "\t\ttarget 4" + NL
            + "\t]" + NL
            + "\tedge [" + NL
            + "\t\tid 5" + NL
            + "\t\tsource 4" + NL
            + "\t\ttarget 5" + NL
            + "\t]" + NL
            + "]" + NL;

    //~ Methods ----------------------------------------------------------------
    public void testUndirected() {
        UndirectedGraph<String, DefaultEdge> g
                = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        StringWriter w = new StringWriter();

        GmlExporter<String, DefaultEdge> exporter
                = new GmlExporter<String, DefaultEdge>();
        exporter.export(w, g);
        assertEquals(UNDIRECTED, w.toString());
    }

    public void testDirected() {
        DirectedGraph<String, DefaultEdge> g
                = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addEdge(V1, V2);
        g.addEdge(V3, V1);
        g.addEdge(V2, V3);
        g.addEdge(V3, V4);
        g.addEdge(V4, V5);

        StringWriter w = new StringWriter();
        GmlExporter<String, DefaultEdge> exporter
                = new GmlExporter<String, DefaultEdge>();
        exporter.export(w, g);
        assertEquals(DIRECTED, w.toString());
    }

    public void testWeightedUndirected() {
        SimpleGraph<String, DefaultWeightedEdge> g
                = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        DefaultWeightedEdge e1 = g.addEdge(V1, V2);
        g.setEdgeWeight(e1, 2.0);
        DefaultWeightedEdge e2 = g.addEdge(V3, V1);
        g.setEdgeWeight(e2, 5.0);

        StringWriter w = new StringWriter();

        GmlExporter<String, DefaultWeightedEdge> exporter
                = new GmlExporter<String, DefaultWeightedEdge>();
        exporter.setExportEdgeWeights(true);
        exporter.export(w, g);
        assertEquals(UNDIRECTED_WEIGHTED, w.toString());
    }

}

// End GmlExporterTest.java
