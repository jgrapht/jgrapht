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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

    private UndirectedGraph<String, DefaultEdge> graph;

    //~ Methods ----------------------------------------------------------------

    @Before
    public void setUp() {
        graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        graph.addVertex(V1);
        graph.addVertex(V2);
        graph.addEdge(V1, V2);
        graph.addVertex(V3);
        graph.addEdge(V3, V1);
    }

    @Test
    public void testUndirected() throws Exception {

        StringWriter w = new StringWriter();
        final GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        exporter.export(w, graph);

        String xmlString = w.toString();
        // FIXME: Why does assertXMLValid fail?
        // XMLAssert.assertXMLValid(xmlString);
        XMLAssert.assertXMLEqual(UNDIRECTED, xmlString);
    }

    private static class TestPropertyProvider<T> implements ComponentAttributeProvider<T> {

        @Override
        public Map<String, String> getComponentAttributes(T x) {
            HashMap<String, String> result = new HashMap<String, String>();
            if (x.equals(V1)) {
                result.put("key-1", x.toString() + "-val-1");
            }
            result.put("key-2", x.toString() + "-val-2");
            return result;
        }
    }

    @Ignore("FIXME: Why does assertXMLValid fail?") @Test
    public void undirectedWithProperties() throws Exception {
        StringWriter w = new StringWriter();

        final GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>(
                new IntegerNameProvider<String>(), new TestPropertyProvider<String>(),
                new IntegerEdgeNameProvider<DefaultEdge>(), new TestPropertyProvider<DefaultEdge>());
        exporter.export(w, graph);

        String xmlString = w.toString();

        XMLAssert.assertXMLValid(xmlString);
        XMLAssert.assertXpathExists("//graph/node/data[@key='key-1']", xmlString);
        XMLAssert.assertXpathEvaluatesTo("v1-val-1", "//graph/node[@id='1']", xmlString);
    }

}

// End GraphMLExporterTest.java
