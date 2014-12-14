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
 * DOTExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Trevor Harmon
 *
 */
package org.jgrapht.ext;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Trevor Harmon
 */
public class DOTExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String UNDIRECTED =
        "graph G {" + NL
        + "  1 [ label=\"a\" ];" + NL
        + "  2 [ x=\"y\" ];" + NL
        + "  3;" + NL
        + "  1 -- 2;" + NL
        + "  3 -- 1;" + NL
        + "}" + NL;

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
        ComponentAttributeProvider<String> vertexAttributeProvider =
            new ComponentAttributeProvider<String>() {
                @Override
                public Map<String, String> getComponentAttributes(String v)
                {
                    Map<String, String> map =
                        new LinkedHashMap<String, String>();
                    if (v.equals(V1)) {
                        map.put("label", "a");
                    } else if (v.equals(V2)) {
                        map.put("x", "y");
                    } else {
                        map = null;
                    }
                    return map;
                }
            };

        DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<String, DefaultEdge>(
                new IntegerNameProvider<String>(),
                null,
                null,
                vertexAttributeProvider,
                null);
        exporter.export(w, g);
        assertEquals(UNDIRECTED, w.toString());
    }

    public void testValidNodeIDs()
    {
        DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<String, DefaultEdge>(
                new StringNameProvider<String>(),
                new StringNameProvider<String>(),
                null);

        List<String> validVertices =
            Arrays.asList(
                "-9.78",
                "-.5",
                "12",
                "a",
                "12",
                "abc_78",
                "\"--34asdf\"");
        for (String vertex : validVertices) {
            Graph<String, DefaultEdge> graph =
                new DefaultDirectedGraph<String, DefaultEdge>(
                    DefaultEdge.class);
            graph.addVertex(vertex);

            exporter.export(new StringWriter(), graph);
        }

        List<String> invalidVertices =
            Arrays.asList("2test", "--4", "foo-bar", "", "t:32");
        for (String vertex : invalidVertices) {
            Graph<String, DefaultEdge> graph =
                new DefaultDirectedGraph<String, DefaultEdge>(
                    DefaultEdge.class);
            graph.addVertex(vertex);

            try {
                exporter.export(new StringWriter(), graph);
                Assert.fail(vertex);
            } catch (RuntimeException re) {
                // this is a negative test so exception is expected
            }
        }
    }
}

// End DOTExporterTest.java
