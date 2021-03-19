/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.dot;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 2nd part of tests for DOTImporter. See also {@link DOTImporter1Test}.
 */
public class DOTImporter2Test
{
    private static final String NL = "\n";

    @Test
    public void testDOT1()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph {" + NL +
                       "  a -- b -- c;" + NL +
                       "  k:1 -- q:a:3 -- d:3;" + NL +
                       "}";
        // @formatter:on

        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();
        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("0", "1");
        expected.addEdge("1", "2");
        expected.addEdge("3", "4");
        expected.addEdge("4", "5");

        assertEquals(expected.toString(), graph.toString());
    }

    @Test
    public void testDOT2()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  subgraph cluster0 { " + NL +
                       "   a0 -> a1 -> a2 -> a3;" + NL +
                       "  }" + NL +
                       "  subgraph cluster1 { " + NL +
                       "   b0 -> b1 -> b2 -> b3;" + NL +
                       "  }" + NL +
                       "  start -> a0;" + NL +
                       "  start -> b0;" + NL +
                       "  a1 -> b3;" + NL +
                       "  b2 -> a3;" + NL +
                       "  a3 -> a0;" + NL +
                       "  a3 -> end;" + NL +
                       "  b3 -> end;" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Graphs
            .addAllVertices(
                expected, Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        expected.addEdge("0", "1");
        expected.addEdge("1", "2");
        expected.addEdge("2", "3");
        expected.addEdge("4", "5");
        expected.addEdge("5", "6");
        expected.addEdge("6", "7");
        expected.addEdge("8", "0");
        expected.addEdge("8", "4");
        expected.addEdge("1", "7");
        expected.addEdge("6", "3");
        expected.addEdge("3", "0");
        expected.addEdge("3", "9");
        expected.addEdge("7", "9");

        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
        assertEquals(attrs.get("2").get("ID").getValue(), "a2");
        assertEquals(attrs.get("3").get("ID").getValue(), "a3");

        assertEquals(attrs.get("4").get("ID").getValue(), "b0");
        assertEquals(attrs.get("5").get("ID").getValue(), "b1");
        assertEquals(attrs.get("6").get("ID").getValue(), "b2");
        assertEquals(attrs.get("7").get("ID").getValue(), "b3");

        assertEquals(attrs.get("8").get("ID").getValue(), "start");
        assertEquals(attrs.get("9").get("ID").getValue(), "end");
    }

    @Test
    public void testDOT3()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  subgraph { " + NL +
                       "   a0 -> a1;" + NL +
                       "  }" + "->" +
                       "  subgraph { " + NL +
                       "   b0 -> b1;" + NL +
                       "  }" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3"));
        expected.addEdge("0", "1");
        expected.addEdge("2", "3");
        expected.addEdge("0", "2");
        expected.addEdge("0", "3");
        expected.addEdge("1", "2");
        expected.addEdge("1", "3");

        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
        assertEquals(attrs.get("2").get("ID").getValue(), "b0");
        assertEquals(attrs.get("3").get("ID").getValue(), "b1");
    }

    @Test
    public void testDOT4()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  subgraph { " + NL +
                       "   a0 -> a1;" + NL +
                       "   subgraph { " + NL +
                       "     a00 -> a11" + NL +
                       "   }" + NL +
                       "  }" + "->" +
                       "  subgraph { " + NL +
                       "   b0 -> b1;" + NL +
                       "   subgraph { " + NL +
                       "     b00 -> b11" + NL +
                       "   }" + NL +
                       "  }" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7"));
        expected.addEdge("0", "1");
        expected.addEdge("2", "3");
        expected.addEdge("4", "5");
        expected.addEdge("6", "7");
        expected.addEdge("0", "4");
        expected.addEdge("0", "5");
        expected.addEdge("0", "6");
        expected.addEdge("0", "7");
        expected.addEdge("1", "4");
        expected.addEdge("1", "5");
        expected.addEdge("1", "6");
        expected.addEdge("1", "7");
        expected.addEdge("2", "4");
        expected.addEdge("2", "5");
        expected.addEdge("2", "6");
        expected.addEdge("2", "7");
        expected.addEdge("3", "4");
        expected.addEdge("3", "5");
        expected.addEdge("3", "6");
        expected.addEdge("3", "7");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
        assertEquals(attrs.get("2").get("ID").getValue(), "a00");
        assertEquals(attrs.get("3").get("ID").getValue(), "a11");
        assertEquals(attrs.get("4").get("ID").getValue(), "b0");
        assertEquals(attrs.get("5").get("ID").getValue(), "b1");
        assertEquals(attrs.get("6").get("ID").getValue(), "b00");
        assertEquals(attrs.get("7").get("ID").getValue(), "b11");
    }

    @Test
    public void testDOT5()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph {" + NL +
                       "  a -- b -- c;" + NL +
                       "  k:1 -- q:a:3 -- d:3;" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        importer.setVertexFactory(id -> id);
        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Graphs.addAllVertices(expected, Arrays.asList("a", "b", "c", "k", "q", "d"));
        expected.addEdge("a", "b");
        expected.addEdge("b", "c");
        expected.addEdge("k", "q");
        expected.addEdge("q", "d");

        assertEquals(expected.toString(), graph.toString());
    }

    @Test
    public void testNodeSubgraphEdges()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 -> { a00 -> a11 } -> b0 -> { b00 -> b11 }" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("1", "2");
        expected.addEdge("4", "5");
        expected.addEdge("0", "1");
        expected.addEdge("0", "2");
        expected.addEdge("1", "3");
        expected.addEdge("2", "3");
        expected.addEdge("3", "4");
        expected.addEdge("3", "5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a00");
        assertEquals(attrs.get("2").get("ID").getValue(), "a11");
        assertEquals(attrs.get("3").get("ID").getValue(), "b0");
        assertEquals(attrs.get("4").get("ID").getValue(), "b00");
        assertEquals(attrs.get("5").get("ID").getValue(), "b11");
    }

    @Test
    public void testNodeSubgraphEdgesUndirected()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 -> { a00 -- a11 } -> b0 -- { b00 -- b11 }" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("1", "2");
        expected.addEdge("4", "5");
        expected.addEdge("0", "1");
        expected.addEdge("0", "2");
        expected.addEdge("1", "3");
        expected.addEdge("2", "3");
        expected.addEdge("3", "4");
        expected.addEdge("3", "5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a00");
        assertEquals(attrs.get("2").get("ID").getValue(), "a11");
        assertEquals(attrs.get("3").get("ID").getValue(), "b0");
        assertEquals(attrs.get("4").get("ID").getValue(), "b00");
        assertEquals(attrs.get("5").get("ID").getValue(), "b11");
    }

    @Test
    public void testNestedSubgraphs()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 -> { { { { a00 -> a11 } } } } -> b0 -> { { b00 -> b11 } }" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("1", "2");
        expected.addEdge("4", "5");
        expected.addEdge("0", "1");
        expected.addEdge("0", "2");
        expected.addEdge("1", "3");
        expected.addEdge("2", "3");
        expected.addEdge("3", "4");
        expected.addEdge("3", "5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a00");
        assertEquals(attrs.get("2").get("ID").getValue(), "a11");
        assertEquals(attrs.get("3").get("ID").getValue(), "b0");
        assertEquals(attrs.get("4").get("ID").getValue(), "b00");
        assertEquals(attrs.get("5").get("ID").getValue(), "b11");
    }

    @Test
    public void testEdgeAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  edge [weight=5.0];" + NL +
                       "  a0 -> a1;" + NL +
                       "  subgraph {" + NL +
                       "    edge [weight=2.0];" + NL +
                       "    a2 -> a3;" + NL +
                       "  };" + NL +
                       "  a4 -> a5 [weight=15.0];" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        Map<DefaultEdge, Map<String, Attribute>> edgeAttrs = new HashMap<>();
        importer.addEdgeAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = edgeAttrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                edgeAttrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("0", "1");
        expected.addEdge("2", "3");
        expected.addEdge("4", "5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
        assertEquals(attrs.get("2").get("ID").getValue(), "a2");
        assertEquals(attrs.get("3").get("ID").getValue(), "a3");
        assertEquals(attrs.get("4").get("ID").getValue(), "a4");
        assertEquals(attrs.get("5").get("ID").getValue(), "a5");

        assertEquals(edgeAttrs.get(graph.getEdge("0", "1")).get("weight").getValue(), "5.0");
        assertEquals(edgeAttrs.get(graph.getEdge("2", "3")).get("weight").getValue(), "2.0");
        assertEquals(edgeAttrs.get(graph.getEdge("4", "5")).get("weight").getValue(), "15.0");
    }

    @Test
    public void testComments()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G { // ignore" + NL +
                       "  /* ignore */ a0 -> a1; /* ignore */" + NL +
                       "# ignore" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1"));
        expected.addEdge("0", "1");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
    }

    @Test
    public void testNodeAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  node [color=gray];" + NL +
                       "  a0 -> a1;" + NL +
                       "  subgraph {" + NL +
                       "    node [color=black];" + NL +
                       "    a2 -> a3;" + NL +
                       "  };" + NL +
                       "  a4 [color=white];" + NL +                       
                       "  a4 -> a5;" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2", "3", "4", "5"));
        expected.addEdge("0", "1");
        expected.addEdge("2", "3");
        expected.addEdge("4", "5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "a0");
        assertEquals(attrs.get("1").get("ID").getValue(), "a1");
        assertEquals(attrs.get("2").get("ID").getValue(), "a2");
        assertEquals(attrs.get("3").get("ID").getValue(), "a3");
        assertEquals(attrs.get("4").get("ID").getValue(), "a4");
        assertEquals(attrs.get("5").get("ID").getValue(), "a5");

        assertEquals("gray", attrs.get("0").get("color").getValue());
        assertEquals("gray", attrs.get("1").get("color").getValue());
        assertEquals("black", attrs.get("2").get("color").getValue());
        assertEquals("black", attrs.get("3").get("color").getValue());
        assertEquals("white", attrs.get("4").get("color").getValue());
        assertEquals("gray", attrs.get("5").get("color").getValue());
    }

    @Test
    public void testUnescape()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0  [name=\"myname\"];" + NL +
                       "  a1  [name=\"name with ; semicolon\"];" + NL +
                       "  a2  [name=myname];" + NL +
                       "  a3  [name=\"name with \\\"internal\\\" quotes\"];" + NL +
                       "  a4  [name=\"my\nname\"];" + NL +
                       "  a5  [name=<<a href=\"http:///www.jgrapht.org\"/>>];" + NL +
                       "  a6  [name=<<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>>];" + NL +
                       "  a7  [name=<<h2>name &#9989;</h2>>];" + NL +
                       "  a8  [name=\"two\\\nlines\"];" + NL +
                       "  a9 [name=\"\"];" + NL +
                       "  a10 [name=\"\\\\\\\\\\\\\\\\\"];" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        assertEquals("myname", attrs.get("0").get("name").getValue());
        assertEquals("name with ; semicolon", attrs.get("1").get("name").getValue());
        assertEquals("myname", attrs.get("2").get("name").getValue());
        assertEquals("name with \"internal\" quotes", attrs.get("3").get("name").getValue());
        assertEquals("my\nname", attrs.get("4").get("name").getValue());
        assertEquals(
            "<a href=\"http:///www.jgrapht.org\"/>", attrs.get("5").get("name").getValue());
        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
            attrs.get("6").get("name").getValue());
        assertEquals("<h2>name \u2705</h2>", attrs.get("7").get("name").getValue());
        assertEquals("two\nlines", attrs.get("8").get("name").getValue());
        assertEquals("", attrs.get("9").get("name").getValue());
        assertEquals("\\\\\\\\", attrs.get("10").get("name").getValue());
    }

    @Test
    public void testNonValidHtmlString()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 [name=< >> >];" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        try {
            importer.importGraph(graph, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testValidHtmlString()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 [name=<<h1/>>];" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        Attribute attr = attrs.get("0").get("name");
        assertEquals("<h1/>", attr.getValue());
        assertEquals(AttributeType.STRING, attr.getType());
    }

    @Test
    public void testLoopError()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 -> a0;" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        try {
            importer.importGraph(graph, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testExampleDOT1()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  subgraph cluster0 {" + NL +
                       "    node [style=filled,color=white];" + NL+
                       "    style=filled;" + NL+
                       "    color=lightgrey;" + NL+
                       "    a0->a1->a2->a3;" + NL+
                       "    label=\"process #1\";" + NL+
                       "  }"+NL+
                       "  subgraph cluster1 {" + NL+
                       "    node [style=filled];"+NL+
                       "    b0->b1->b2->b3;" + NL+
                       "    label=\"process #2\";" + NL+
                       "    color=blue"+NL+
                       "  }"+NL+
                       "  start -> a0;"+NL+
                       "  start -> b0;"+NL+
                       "  a1 -> b3;"+NL+
                       "  b2 -> a3;"+NL+
                       "  a3 -> a0;"+NL+
                       "  a3 -> end;"+NL+
                       "  b3 -> end;"+NL+
                       "  start [shape=Mdiamond];"+NL+
                       "  end [shape=Msquare];" + NL+ 
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "G");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

    }

    @Test
    public void testShapes1()
        throws ImportException
    {
        // @formatter:off
        // input www.graphviz.org/doc/info/html3.gv
        String input = "digraph structs {" + NL +
                       "  node [shape=plaintext];" + NL +
                       "  struct1 [label=<<TABLE>" + NL +
                       "  <TR>" + NL +
                       "    <TD>line 1</TD>" + NL +
                       "    <TD BGCOLOR=\"blue\"><FONT COLOR=\"white\">line2</FONT></TD>" + NL +
                       "    <TD BGCOLOR=\"gray\"><FONT POINT-SIZE=\"24.0\">line3</FONT></TD>" + NL +
                       "    <TD BGCOLOR=\"yellow\"><FONT POINT-SIZE=\"24.0\" FACE=\"ambrosia\">line4</FONT></TD>" + NL +
                       "    <TD>" + NL +
                       "      <TABLE CELLPADDING=\"0\" BORDER=\"0\" CELLSPACING=\"0\">" + NL +
                       "      <TR>" + NL +
                       "        <TD><FONT COLOR=\"green\">Mixed</FONT></TD>" + NL +
                       "        <TD><FONT COLOR=\"red\">fonts</FONT></TD>" + NL +
                       "      </TR>" + NL +
                       "      </TABLE>" + NL +
                       "    </TD>" + NL +
                       "  </TR>" + NL +
                       "  </TABLE>>];" + NL +
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            assertEquals(p, "ID");
            assertEquals(a.getValue(), "structs");
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0"));
        assertEquals(expected.toString(), graph.toString());
    }

    @Test
    public void testEmptyList()
        throws Exception
    {
        // @formatter:off
        String input = "digraph g {" + NL +
                       "  name=\"TEST\";" + NL + 
                       "  graph []" + NL +
                       "  anyNodeWithAttributes [color=\"red\"];" + NL +
                       "  anyNodeWithoutAttributes;" + NL +
                       "  anyNodeWithEmptyAttributes [];" + NL +
                       "}";
        // @formatter:on
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.addGraphAttributeConsumer((p, a) -> {
            if ("ID".equals(p)) {
                assertEquals("g", a.getValue());
            }
            if ("name".equals(p)) {
                assertEquals("TEST", a.getValue());
            }
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("0", "1", "2"));
        assertEquals(expected.toString(), graph.toString());

        assertEquals(attrs.get("0").get("ID").getValue(), "anyNodeWithAttributes");
        assertEquals(attrs.get("1").get("ID").getValue(), "anyNodeWithoutAttributes");
        assertEquals(attrs.get("2").get("ID").getValue(), "anyNodeWithEmptyAttributes");

    }

    @Test
    public void testCreateVerticesWithAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 [color=gray];" + NL +
                       "  a1 [color=green];" + NL +                       
                       "  a0 -> a1;" + NL +
                       "  a2 [color=white];" + NL +                       
                       "}";
        // @formatter:on

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

        importer.setVertexWithAttributesFactory((id, attrs) -> {
            return id + "-" + attrs.get("color").getValue();
        });

        DirectedPseudograph<String, DefaultEdge> graph = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(graph, new StringReader(input));

        assertTrue(graph.containsVertex("a0-gray"));
        assertTrue(graph.containsVertex("a1-green"));
        assertTrue(graph.containsVertex("a2-white"));
    }

    @Test
    public void testCreateEdgesWithAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0 [color=gray];" + NL +
                       "  a1 [color=green];" + NL +                       
                       "  a2 [color=white];" + NL +                       
                       "  a0 -> a1 [label=\"e1\"];" + NL +
                       "  a1 -> a2 [label=\"e2\"];" + NL +                       
                       "}";
        // @formatter:on

        DOTImporter<String, String> importer = new DOTImporter<>();

        importer.setVertexWithAttributesFactory((id, attrs) -> {
            return id + "-" + attrs.get("color").getValue();
        });

        importer.setEdgeWithAttributesFactory((attrs) -> {
            return attrs.get("label").getValue();
        });

        DirectedPseudograph<String, String> graph =
            new DirectedPseudograph<>(SupplierUtil.createStringSupplier(), null, false);
        importer.importGraph(graph, new StringReader(input));

        assertTrue(graph.containsEdge("e1"));
        assertTrue(graph.containsEdge("e2"));
    }

}
