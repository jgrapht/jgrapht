/*
 * (C) Copyright 2003-2017, by Trevor Harmon and Contributors.
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
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

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
        throws UnsupportedEncodingException,
        ExportException
    {
        testUndirected(new SimpleGraph<>(DefaultEdge.class), true);
        testUndirected(new Multigraph<>(DefaultEdge.class), false);
        testUndirectedWithGraphAttributes(new Multigraph<>(DefaultEdge.class), false);
    }

    private void testUndirected(Graph<String, DefaultEdge> g, boolean strict)
        throws UnsupportedEncodingException,
        ExportException
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
        throws UnsupportedEncodingException,
        ExportException
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
    public void testQuotedNodeIDs()
    {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
            new StringComponentNameProvider<>(), new StringComponentNameProvider<>(), null);

        StringWriter outputWriter = new StringWriter();

        String quotedNodeId = "\"abc\"";

        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(quotedNodeId);
        exporter.exportGraph(graph, outputWriter);

        assertThat(outputWriter.toString(), containsString("label=\"\\\"abc\\\"\""));
    }

    @Test
    public void testNodeHtmlLabel()
    {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
            new StringComponentNameProvider<>(), vertex -> "<<b>html label</b>>", null);

        StringWriter outputWriter = new StringWriter();

        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("myVertex");
        exporter.exportGraph(graph, outputWriter);

        assertThat(outputWriter.toString(), containsString("label=\"<<b>html label</b>>\""));
    }

    @Test
    public void testNodeHtmlLabelFromAttribute()
    {
        DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<>(new StringComponentNameProvider<>(), vertex -> null, null, vertex -> {
                final HashMap<String, Attribute> attrs = new HashMap<>();
                attrs.put("label", new DefaultAttribute<>("<b>html label</b>", AttributeType.HTML));
                return attrs;
            }, null);

        StringWriter outputWriter = new StringWriter();

        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("myVertex");
        exporter.exportGraph(graph, outputWriter);

        assertThat(outputWriter.toString(), containsString("label=<<b>html label</b>>"));
    }

    @Test
    public void testDifferentGraphID()
        throws UnsupportedEncodingException,
        ExportException
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
