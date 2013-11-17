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
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String UNDIRECTED =
        "Creator \"JGraphT GML Exporter\"" + NL
        + "Version 1" + NL
        + "graph" + NL
        + "[" + NL
        + "\tlabel \"\"" + NL
        + "\tdirected 0" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 1" + NL
        + "\t]" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 2" + NL
        + "\t]" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 3" + NL
        + "\t]" + NL
        + "\tedge" + NL
        + "\t[" + NL
        + "\t\tid 1" + NL
        + "\t\tsource 1" + NL
        + "\t\ttarget 2" + NL
        + "\t]" + NL
        + "\tedge" + NL
        + "\t[" + NL
        + "\t\tid 2" + NL
        + "\t\tsource 3" + NL
        + "\t\ttarget 1" + NL
        + "\t]" + NL
        + "]" + NL;

    private static final GmlExporter<String, DefaultEdge> exporter =
        new GmlExporter<String, DefaultEdge>();

    //~ Methods ----------------------------------------------------------------

    public void testUndirected()
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
        assertEquals(UNDIRECTED, w.toString());
    }
}

// End GmlExporterTest.java
