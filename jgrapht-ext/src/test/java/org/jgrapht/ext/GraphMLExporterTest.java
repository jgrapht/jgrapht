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
 * GraphMLExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Trevor Harmon
 *
 */
package org.jgrapht.ext;

import java.io.*;

import junit.framework.*;

import org.custommonkey.xmlunit.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Trevor Harmon
 */
public class GraphMLExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String UNDIRECTED =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
        + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + NL
        + "<graph edgedefault=\"undirected\">" + NL
        + "<node id=\"1\"/>" + NL
        + "<node id=\"2\"/>" + NL
        + "<node id=\"3\"/>" + NL
        + "<edge id=\"1\" source=\"1\" target=\"2\"/>" + NL
        + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
        + "</graph>" + NL
        + "</graphml>" + NL;

    private static final GraphMLExporter<String, DefaultEdge> exporter =
        new GraphMLExporter<String, DefaultEdge>();

    //~ Methods ----------------------------------------------------------------

    public void testUndirected()
        throws Exception
    {
        UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        StringWriter w = new StringWriter();
        exporter.export(w, g);

        if (System.getProperty("java.vm.version").startsWith("1.4")) {
            // NOTE jvs 16-Mar-2007:  XML prefix mapping comes out
            // with missing info on 1.4, so skip the verification part
            // of the test.
            return;
        }

        XMLAssert.assertXMLEqual(UNDIRECTED, w.toString());
    }
}

// End GraphMLExporterTest.java
