/*
 * (C) Copyright 2003-2017, by Trevor Harmon and Contributors.
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

import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import junit.framework.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * .
 *
 * @author Trevor Harmon
 */
public class DOTExporterTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    private static final String UNDIRECTED = "graph G {" + NL + "  1 [ label=\"a\" ];" + NL
        + "  2 [ x=\"y\" ];" + NL + "  3;" + NL + "  1 -- 2;" + NL + "  3 -- 1;" + NL + "}" + NL;

    // @formatter:off
    private static final String UNDIRECTED_WITH_GRAPH_ATTRIBUTES =
        "graph G {" + NL +
        "  overlap=false;" + NL +
        "  splines=true;" + NL +
        "  1;" + NL +
        "  2;" + NL +
        "  3;" + NL +
        "  1 -- 2;" + NL +
        "  3 -- 1;" + NL +
        "}" + NL;
    // @formatter:on

    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testUndirected()
        throws UnsupportedEncodingException, ExportException
    {
        testUndirected(new SimpleGraph<>(DefaultEdge.class), true);
        testUndirected(new Multigraph<>(DefaultEdge.class), false);
        testUndirectedWithGraphAttributes(new Multigraph<>(DefaultEdge.class), false);
    }

    private void testUndirected(Graph<String, DefaultEdge> g, boolean strict)
        throws UnsupportedEncodingException, ExportException
    {
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        ComponentAttributeProvider<String> vertexAttributeProvider =
            new ComponentAttributeProvider<String>()
            {
                @Override
                public Map<String, Attribute> getComponentAttributes(String v)
                {
                    Map<String, Attribute> map = new LinkedHashMap<>();
                    switch (v) {
                    case V1:
                        map.put("label", DefaultAttribute.createAttribute("a"));
                        break;
                    case V2:
                        map.put("x", DefaultAttribute.createAttribute("y"));
                        break;
                    default:
                        map = null;
                        break;
                    }
                    return map;
                }
            };

        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
            new IntegerComponentNameProvider<>(), null, null, vertexAttributeProvider, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals((strict) ? "strict " + UNDIRECTED : UNDIRECTED, res);
    }

    private void testUndirectedWithGraphAttributes(Graph<String, DefaultEdge> g, boolean strict)
        throws UnsupportedEncodingException, ExportException
    {
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<>(new IntegerComponentNameProvider<>(), null, null, null, null);

        exporter.putGraphAttribute("overlap", "false");
        exporter.putGraphAttribute("splines", "true");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(
            (strict) ? "strict " + UNDIRECTED_WITH_GRAPH_ATTRIBUTES
                : UNDIRECTED_WITH_GRAPH_ATTRIBUTES,
            res);
    }

    @Test
    public void testValidNodeIDs()
        throws ExportException
    {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
            new StringComponentNameProvider<>(), new StringComponentNameProvider<>(), null);

        List<String> validVertices =
            Arrays.asList("-9.78", "-.5", "12", "a", "12", "abc_78", "\"--34asdf\"");
        for (String vertex : validVertices) {
            Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
            graph.addVertex(vertex);
            exporter.exportGraph(graph, new ByteArrayOutputStream());
        }

        List<String> invalidVertices = Arrays.asList("2test", "--4", "foo-bar", "", "t:32");
        for (String vertex : invalidVertices) {
            Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
            graph.addVertex(vertex);

            try {
                exporter.exportGraph(graph, new ByteArrayOutputStream());
                fail(vertex);
            } catch (RuntimeException re) {
                // this is a negative test so exception is expected
            }
        }
    }

    @Test
    public void testDifferentGraphID()
        throws UnsupportedEncodingException, ExportException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        final String customID = "MyGraph";

        DOTExporter<String,
            DefaultEdge> exporter = new DOTExporter<>(
                new IntegerComponentNameProvider<>(), null, null, null, null,
                new ComponentNameProvider<Graph<String, DefaultEdge>>()
                {
                    @Override
                    public String getName(Graph<String, DefaultEdge> component)
                    {
                        return customID;
                    }
                });

        final String correctResult = "strict graph " + customID + " {" + NL + "}" + NL;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(correctResult, res);
    }

}

// End DOTExporterTest.java
