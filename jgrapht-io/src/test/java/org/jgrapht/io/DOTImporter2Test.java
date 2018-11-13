/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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

import static org.junit.Assert.assertEquals;
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a", "b", "c", "k", "q", "d"));
        expected.addEdge("a", "b");
        expected.addEdge("b", "c");
        expected.addEdge("k", "q");
        expected.addEdge("q", "d");

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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(
            expected,
            Arrays.asList("a0", "a1", "a2", "a3", "b0", "b1", "b2", "b3", "start", "end"));
        expected.addEdge("a0", "a1");
        expected.addEdge("a1", "a2");
        expected.addEdge("a2", "a3");
        expected.addEdge("b0", "b1");
        expected.addEdge("b1", "b2");
        expected.addEdge("b2", "b3");
        expected.addEdge("start", "a0");
        expected.addEdge("start", "b0");
        expected.addEdge("a1", "b3");
        expected.addEdge("b2", "a3");
        expected.addEdge("a3", "a0");
        expected.addEdge("a3", "end");
        expected.addEdge("b3", "end");

        assertEquals(expected.toString(), graph.toString());
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a1", "b0", "b1"));
        expected.addEdge("a0", "a1");
        expected.addEdge("b0", "b1");
        expected.addEdge("a0", "b0");
        expected.addEdge("a0", "b1");
        expected.addEdge("a1", "b0");
        expected.addEdge("a1", "b1");

        assertEquals(expected.toString(), graph.toString());
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(
            expected, Arrays.asList("a0", "a1", "a00", "a11", "b0", "b1", "b00", "b11"));
        expected.addEdge("a0", "a1");
        expected.addEdge("a00", "a11");
        expected.addEdge("b0", "b1");
        expected.addEdge("b00", "b11");
        expected.addEdge("a0", "b0");
        expected.addEdge("a0", "b1");
        expected.addEdge("a0", "b00");
        expected.addEdge("a0", "b11");
        expected.addEdge("a1", "b0");
        expected.addEdge("a1", "b1");
        expected.addEdge("a1", "b00");
        expected.addEdge("a1", "b11");
        expected.addEdge("a00", "b0");
        expected.addEdge("a00", "b1");
        expected.addEdge("a00", "b00");
        expected.addEdge("a00", "b11");
        expected.addEdge("a11", "b0");
        expected.addEdge("a11", "b1");
        expected.addEdge("a11", "b00");
        expected.addEdge("a11", "b11");
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a00", "a11", "b0", "b00", "b11"));
        expected.addEdge("a00", "a11");
        expected.addEdge("b00", "b11");
        expected.addEdge("a0", "a00");
        expected.addEdge("a0", "a11");
        expected.addEdge("a00", "b0");
        expected.addEdge("a11", "b0");
        expected.addEdge("b0", "b00");
        expected.addEdge("b0", "b11");
        assertEquals(expected.toString(), graph.toString());
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a00", "a11", "b0", "b00", "b11"));
        expected.addEdge("a00", "a11");
        expected.addEdge("b00", "b11");
        expected.addEdge("a0", "a00");
        expected.addEdge("a0", "a11");
        expected.addEdge("a00", "b0");
        expected.addEdge("a11", "b0");
        expected.addEdge("b0", "b00");
        expected.addEdge("b0", "b11");
        assertEquals(expected.toString(), graph.toString());
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a00", "a11", "b0", "b00", "b11"));
        expected.addEdge("a00", "a11");
        expected.addEdge("b00", "b11");
        expected.addEdge("a0", "a00");
        expected.addEdge("a0", "a11");
        expected.addEdge("a00", "b0");
        expected.addEdge("a11", "b0");
        expected.addEdge("b0", "b00");
        expected.addEdge("b0", "b11");
        assertEquals(expected.toString(), graph.toString());
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

        VertexProvider<String> vp = (a, b) -> a;
        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> {
            DefaultEdge e = new DefaultEdge();
            edgeAttributes.put(e, a);
            return e;
        };
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a1", "a2", "a3", "a4", "a5"));
        expected.addEdge("a0", "a1");
        expected.addEdge("a2", "a3");
        expected.addEdge("a4", "a5");
        assertEquals(expected.toString(), graph.toString());

        for (DefaultEdge e : edgeAttributes.keySet()) {
            String f = graph.getEdgeSource(e);
            String t = graph.getEdgeSource(e);
            Map<String, Attribute> attrs = edgeAttributes.get(e);
            if (f.equals("a0") && t.equals("a1")) {
                assertEquals("5.0", attrs.get("weight"));
            } else if (f.equals("a2") && t.equals("a3")) {
                assertEquals("5.0", attrs.get("weight"));
            } else if (f.equals("a4") && t.equals("a5")) {
                assertEquals("15.0", attrs.get("weight"));
            }
        }
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a1"));
        expected.addEdge("a0", "a1");
        assertEquals(expected.toString(), graph.toString());
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

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        VertexProvider<String> vp = (label, attrs) -> {
            vertexAttributes.put(label, attrs);
            return label;
        };
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("a0", "a1", "a2", "a3", "a4", "a5"));
        expected.addEdge("a0", "a1");
        expected.addEdge("a2", "a3");
        expected.addEdge("a4", "a5");
        assertEquals(expected.toString(), graph.toString());

        assertEquals("gray", vertexAttributes.get("a0").get("color").getValue());
        assertEquals("gray", vertexAttributes.get("a1").get("color").getValue());
        assertEquals("black", vertexAttributes.get("a2").get("color").getValue());
        assertEquals("black", vertexAttributes.get("a3").get("color").getValue());
        assertEquals("white", vertexAttributes.get("a4").get("color").getValue());
        assertEquals("gray", vertexAttributes.get("a5").get("color").getValue());
    }

    @Test
    public void testUnescape()
        throws ImportException
    {
        // @formatter:off
        String input = "digraph G {" + NL +
                       "  a0  [name=\"myname\"];" + NL +
                       "  a1  [name=\"name with ; semicolon\"];" + NL +
                       "  a3  [name=myname];" + NL +
                       "  a4  [name=\"name with \\\"internal\\\" quotes\"];" + NL +
                       "  a5  [name=\"my\nname\"];" + NL +
                       "  a6  [name=<<a href=\"http:///www.jgrapht.org\"/>>];" + NL +
                       "  a7  [name=<<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>>];" + NL +
                       "  a8  [name=<<h2>name &#9989;</h2>>];" + NL +
                       "  a9  [name=\"two\\\nlines\"];" + NL +
                       "  a10 [name=\"\"];" + NL +
                       "  a11 [name=\"\\\\\\\\\\\\\\\\\"];" + NL +
                       "}";
        // @formatter:on

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        VertexProvider<String> vp = (label, attrs) -> {
            vertexAttributes.put(label, attrs);
            return label;
        };
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        assertEquals("myname", vertexAttributes.get("a0").get("name").getValue());
        assertEquals("name with ; semicolon", vertexAttributes.get("a1").get("name").getValue());
        assertEquals("myname", vertexAttributes.get("a3").get("name").getValue());
        assertEquals(
            "name with \"internal\" quotes", vertexAttributes.get("a4").get("name").getValue());
        assertEquals("my\nname", vertexAttributes.get("a5").get("name").getValue());
        assertEquals(
            "<a href=\"http:///www.jgrapht.org\"/>",
            vertexAttributes.get("a6").get("name").getValue());
        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
            vertexAttributes.get("a7").get("name").getValue());
        assertEquals("<h2>name \u2705</h2>", vertexAttributes.get("a8").get("name").getValue());
        assertEquals("two\nlines", vertexAttributes.get("a9").get("name").getValue());
        assertEquals("", vertexAttributes.get("a10").get("name").getValue());
        assertEquals("\\\\\\\\", vertexAttributes.get("a11").get("name").getValue());
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

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        VertexProvider<String> vp = (label, attrs) -> {
            vertexAttributes.put(label, attrs);
            return label;
        };
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
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

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        VertexProvider<String> vp = (label, attrs) -> {
            vertexAttributes.put(label, attrs);
            return label;
        };
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        Attribute attr = vertexAttributes.get("a0").get("name");
        assertEquals("<h1/>", attr.getValue());
        assertEquals(AttributeType.STRING, attr.getType());
    }

    @Test
    public void testError()
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

        VertexProvider<String> vp = (a, b) -> a;
        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> {
            DefaultEdge e = new DefaultEdge();
            if (f.equals("a2") && t.equals("a3")) {
                throw new Error("Error");
            }
            edgeAttributes.put(e, a);
            return e;
        };
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        try {
            importer.importGraph(graph, new StringReader(input));
            fail("No!");
        } catch (Error e) {
            assertEquals("Error", e.getMessage());
        }
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedMultigraph<String, DefaultEdge> graph =
            new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();

        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);

        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);

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

        VertexProvider<String> vp = (a, b) -> a;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(vp, ep);
        DirectedPseudograph<String, DefaultEdge> graph =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(input));

        DirectedPseudograph<String, DefaultEdge> expected =
            new DirectedPseudograph<String, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(expected, Arrays.asList("struct1"));
        assertEquals(expected.toString(), graph.toString());
    }

}
