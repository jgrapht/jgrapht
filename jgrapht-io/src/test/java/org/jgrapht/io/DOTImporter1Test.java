/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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

import static org.junit.Assert.*;

/**
 * 1st part of tests for DOTImporter. See also {@link DOTImporter2Test}.
 */
public class DOTImporter1Test
{

    @Test
    public void testImportID()
        throws ImportException
    {
        String id = "MyGraph";

        String input = "strict graph " + id + " {\n}\n";

        GraphWithID expected = new GraphWithID();
        expected.id = id;

        GraphWithID result = new GraphWithID();
        assertNull(result.id);

        buildGraphIDImporter().importGraph(result, new StringReader(input));
        assertEquals(expected.toString(), result.toString());
        assertEquals(expected.id, id);
    }

    @Test
    public void testImportWrongID()
        throws ImportException
    {
        String invalidID = "2test";
        String input = "graph " + invalidID + " {\n}\n";

        GraphWithID result = new GraphWithID();

        try {
            buildGraphIDImporter().importGraph(result, new StringReader(input));
            fail("Should not get here");
        } catch (ImportException e) {
            assertEquals(
                "Failed to import DOT graph: line 1:7 extraneous input 'test' expecting '{'",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidHeader()
        throws ImportException
    {
        // testing all cases of missing keywords or wrong order
        for (String invalidInput : new String[] { " {}", "strict {}", "id {}", "strict id {}",
            "id strict {}", "id strict graph {}", "graph strict id {}" })
        {

            GraphWithID result = new GraphWithID();

            try {
                buildGraphIDImporter().importGraph(result, new StringReader(invalidInput));
                fail("Correctly loaded incorrect graph: " + invalidInput);
            } catch (ImportException e) {
                // this is the expected exception
            } catch (Exception e) {
                fail("Expected ImportException but found " + e.getClass().getSimpleName());
            }
        }
    }

    @Test
    public void testImportOnlyGraphKeyword()
        throws ImportException
    {
        String input = "graph {\n}\n";
        GraphWithID result = new GraphWithID();
        buildGraphIDImporter().importGraph(result, new StringReader(input));
        assertNull(result.id);
    }

    @Test
    public void testImportNoID()
        throws ImportException
    {
        String input = "strict graph {\n}\n";
        GraphWithID result = new GraphWithID();
        buildGraphIDImporter().importGraph(result, new StringReader(input));
        assertNull(result.id);
    }

    @Test
    public void testUndirectedWithLabels()
        throws ImportException
    {
        String input = "graph G {\n" + "  1 [ \"label\"=\"abc123\" ];\n"
            + "  2 [ label=\"fred\" ];\n" + "  1 -- 2;\n" + "}";

        Multigraph<String, DefaultEdge> expected = new Multigraph<>(DefaultEdge.class);
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("1", "2");

        GraphImporter<String, DefaultEdge> importer = buildImporter();

        Multigraph<String, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(2, result.vertexSet().size());
        assertEquals(1, result.edgeSet().size());

    }

    @Test
    public void testDirectedNoLabels()
        throws ImportException
    {
        String input =
            "digraph graphname {\r\n" + "     a -> b -> c;\r\n" + "     b -> d;\r\n" + " }";

        DirectedMultigraph<String, DefaultEdge> expected =
            new DirectedMultigraph<>(DefaultEdge.class);
        expected.addVertex("a");
        expected.addVertex("b");
        expected.addVertex("c");
        expected.addVertex("d");
        expected.addEdge("a", "b");
        expected.addEdge("b", "c");
        expected.addEdge("b", "d");

        GraphImporter<String, DefaultEdge> importer = buildImporter();

        DirectedMultigraph<String, DefaultEdge> result =
            new DirectedMultigraph<>(DefaultEdge.class);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(4, result.vertexSet().size());
        assertEquals(3, result.edgeSet().size());

    }

    @Test
    public void testDirectedSameLabels()
        throws ImportException
    {
        String input =
            "digraph sample {\n" + "  a -> b;" + "  b -> c;\n" + "  a [ label=\"Test\"];\n"
                + "  b [ label=\"Test\"];\n" + "  c [ label=\"Test\"];\n" + "}";

        DirectedMultigraph<String, DefaultEdge> expected =
            new DirectedMultigraph<>(DefaultEdge.class);
        expected.addVertex("a");
        expected.addVertex("b");
        expected.addVertex("c");
        expected.addEdge("a", "b");
        expected.addEdge("b", "c");

        VertexProvider<String> vp = (label, attrs) -> label;
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, attrs) -> new DefaultEdge();
        ComponentUpdater<String> cu = (v, attrs) -> {
        };
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>(vp, ep, cu);

        DirectedMultigraph<String, DefaultEdge> result =
            new DirectedMultigraph<>(DefaultEdge.class);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testMultiLinksUndirected()
        throws ImportException
    {
        String input = "graph G {\n" + "  1 [ label=\"bob\" ];\n" + "  2 [ label=\"fred\" ];\n"
        // the extra label will be ignored but not cause any problems.
            + "  1 -- 2 [ label=\"friend\"];\n" + "  1 -- 2;\n" + "}";

        Multigraph<String, DefaultEdge> expected = new Multigraph<>(DefaultEdge.class);
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("1", "2", new DefaultEdge());
        expected.addEdge("1", "2", new DefaultEdge());

        GraphImporter<String, DefaultEdge> importer = buildImporter();

        Multigraph<String, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(2, result.vertexSet().size());
        assertEquals(2, result.edgeSet().size());
    }

    @Test
    public void testExportImportLoop()
        throws ImportException,
        ExportException,
        UnsupportedEncodingException
    {
        DirectedMultigraph<String, DefaultEdge> start = new DirectedMultigraph<>(DefaultEdge.class);
        start.addVertex("a");
        start.addVertex("b");
        start.addVertex("c");
        start.addVertex("d");
        start.addEdge("a", "b");
        start.addEdge("b", "c");
        start.addEdge("b", "d");

        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(
            vertex -> vertex, null, new IntegerComponentNameProvider<DefaultEdge>());

        GraphImporter<String, DefaultEdge> importer = buildImporter();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(start, os);
        String output = new String(os.toByteArray(), "UTF-8");

        DirectedMultigraph<String, DefaultEdge> result =
            new DirectedMultigraph<>(DefaultEdge.class);

        importer.importGraph(result, new StringReader(output));

        assertEquals(start.toString(), result.toString());

        assertEquals(4, result.vertexSet().size());
        assertEquals(3, result.edgeSet().size());

    }

    @Test
    public void testDashLabelVertex()
        throws ImportException
    {
        String input =
            "graph G {\n" + "a [label=\"------this------contains-------dashes------\"]\n" + "}";

        Multigraph<String, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();

        VertexProvider<String> vp = (label, a) -> {
            attrs.put(label, a);
            return label;
        };
        EdgeProvider<String, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();
        ComponentUpdater<String> cu = (v, a) -> {
        };
        DOTImporter<String, DefaultEdge> importer =
            new DOTImporter<String, DefaultEdge>(vp, ep, cu);

        importer.importGraph(result, new StringReader(input));

        assertEquals(1, result.vertexSet().size());
        String v = result.vertexSet().stream().findFirst().get();
        assertEquals("a", v);
        assertEquals(
            "------this------contains-------dashes------", attrs.get("a").get("label").getValue());
    }

    @Test
    public void testAttributesWithNoQuotes()
        throws ImportException
    {
        String input =
            "graph G {\n" + "  1 [ label = \"bob\" \"foo\"=bar ];\n" + "  2 [ label = \"fred\" ];\n"
            // the extra label will be ignored but not cause any problems.
                + "  1 -- 2 [ label = \"friend\" \"foo\" = wibble];\n" + "}";

        Multigraph<TestVertex, TestEdge> result =
            new Multigraph<TestVertex, TestEdge>(TestEdge.class);
        DOTImporter<TestVertex, TestEdge> importer = new DOTImporter<TestVertex, TestEdge>(
            (l, a) -> new TestVertex(l, a), (f, t, l, a) -> new TestEdge(l, a));

        importer.importGraph(result, new StringReader(input));
        assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());

        for (TestVertex v : result.vertexSet()) {
            if ("1".equals(v.getId())) {
                assertEquals("wrong number of attributes", 2, v.getAttributes().size());
                assertEquals(
                    "Wrong attribute values", "bar", v.getAttributes().get("foo").getValue());
                assertEquals(
                    "Wrong attribute values", "bob", v.getAttributes().get("label").getValue());
            } else {
                assertEquals("wrong number of attributes", 1, v.getAttributes().size());
                assertEquals(
                    "Wrong attribute values", "fred", v.getAttributes().get("label").getValue());
            }
        }

        for (TestEdge e : result.edgeSet()) {
            assertEquals("wrong id", "friend", e.getId());
            assertEquals("wrong number of attributes", 2, e.getAttributes().size());
            assertEquals(
                "Wrong attribute value", "wibble", e.getAttributes().get("foo").getValue());
            assertEquals(
                "Wrong attribute value", "friend", e.getAttributes().get("label").getValue());
        }

    }

    @Test
    public void testEmptyString()
    {
        testGarbage(
            "",
            "Failed to import DOT graph: line 1:0 mismatched input '' expecting {STRICT, GRAPH, DIGRAPH}");
    }

    @Test
    public void testGarbageStringEnoughLines()
    {
        String input =
            "jsfhg kjdsf hgkfds\n" + "fdsgfdsgfd\n" + "gfdgfdsgfdsg\n" + "jdhgkjfdshgsjkhl\n";

        testGarbage(
            input,
            "Failed to import DOT graph: line 1:0 mismatched input 'jsfhg' expecting {STRICT, GRAPH, DIGRAPH}");
    }

    @Test
    public void testGarbageStringInvalidFirstLine()
    {
        String input = "jsfhgkjdsfhgkfds\n" + "fdsgfdsgfd\n";

        testGarbage(
            input,
            "Failed to import DOT graph: line 1:0 mismatched input 'jsfhgkjdsfhgkfds' expecting {STRICT, GRAPH, DIGRAPH}");
    }

    @Test
    public void testGarbageStringNotEnoughLines()
    {
        String input = "jsfhgkjdsfhgkfds\n";

        testGarbage(
            input,
            "Failed to import DOT graph: line 1:0 mismatched input 'jsfhgkjdsfhgkfds' expecting {STRICT, GRAPH, DIGRAPH}");
    }

    @Test
    public void testIncompatibleDirectedGraph()
    {
        String input = "digraph G {\n" + "a -- b\n" + "}";

        Multigraph<String, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);

        testGarbageGraph(
            input, "Failed to import DOT graph: Provided graph is not directed", result);
    }

    @Test
    public void testIncompatibleGraph()
    {
        String input = "graph G {\n" + "a -- b\n" + "}";

        DirectedMultigraph<String, DefaultEdge> result =
            new DirectedMultigraph<>(DefaultEdge.class);

        testGarbageGraph(
            input, "Failed to import DOT graph: Provided graph is not undirected", result);
    }

    @Test
    public void testAttributesWithNoValues()
        throws ImportException
    {
        String input =
            "graph G {\n" + "  1 [ label = \"bob\" \"foo\" ];\n" + "  2 [ label = \"fred\" ];\n"
            // the extra label will be ignored but not cause any problems.
                + "  1 -- 2 [ label = friend foo];\n" + "}";

        Multigraph<TestVertex, TestEdge> graph = new Multigraph<>(TestEdge.class);

        VertexProvider<TestVertex> vp = (label, attrs) -> new TestVertex(label, attrs);
        EdgeProvider<TestVertex, TestEdge> ep = (f, t, l, attrs) -> new TestEdge(l, attrs);
        DOTImporter<TestVertex, TestEdge> importer = new DOTImporter<TestVertex, TestEdge>(vp, ep);

        try {
            importer.importGraph(graph, new StringReader(input));
            fail("Failed to import DOT graph: line 2:26 mismatched input ']' expecting '='");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testUpdatingVertex()
        throws ImportException
    {
        String input = "graph G {\n" + "a -- b;\n" + "a [foo=\"bar\"];\n" + "}";
        Multigraph<TestVertex, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);

        VertexProvider<TestVertex> vp = (label, attrs) -> new TestVertex(label, attrs);
        EdgeProvider<TestVertex, DefaultEdge> ep = (f, t, l, attrs) -> new DefaultEdge();
        ComponentUpdater<TestVertex> cu = (v, attrs) -> v.getAttributes().putAll(attrs);
        DOTImporter<TestVertex, DefaultEdge> importer = new DOTImporter<>(vp, ep, cu);

        importer.importGraph(result, new StringReader(input));

        assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());
        for (TestVertex v : result.vertexSet()) {
            if ("a".equals(v.getId())) {
                assertEquals("wrong number of attributes", 1, v.getAttributes().size());
            } else {
                assertEquals("attributes are populated", 0, v.getAttributes().size());
            }
        }

    }

    @Test
    public void testParametersWithSemicolons()
        throws ImportException
    {
        String input = "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];\n}\n";
        Multigraph<TestVertex, DefaultEdge> result =
            new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
        DOTImporter<TestVertex, DefaultEdge> importer = new DOTImporter<TestVertex, DefaultEdge>(
            (l, a) -> new TestVertex(l, a), (f, t, l, a) -> new DefaultEdge());

        importer.importGraph(result, new StringReader(input));
        assertEquals("wrong size of vertexSet", 1, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
    }

    @Test
    public void testLabelsWithEscapedSemicolons()
        throws ImportException
    {
        String escapedLabel = "this \\\"label; \\\"contains an escaped semi colon";
        String input = "graph G {\n node [ label=\"" + escapedLabel + "\" ];\n node0 }\n";
        Multigraph<TestVertex, DefaultEdge> result =
            new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
        DOTImporter<TestVertex, DefaultEdge> importer = new DOTImporter<TestVertex, DefaultEdge>(
            (label, attrs) -> new TestVertex(label, attrs), (f, t, l, a) -> new DefaultEdge());

        importer.importGraph(result, new StringReader(input));
        assertEquals("wrong size of vertexSet", 1, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
        assertEquals(
            "wrong parsing", "node0", ((TestVertex) result.vertexSet().toArray()[0]).getId());
        assertEquals(
            "wrong parsing", "this \"label; \"contains an escaped semi colon",
            ((TestVertex) result.vertexSet().toArray()[0]).getAttributes().get("label").getValue());
    }

    @Test
    public void testNoLineEndBetweenNodes()
        throws ImportException
    {
        String input =
            "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];  2 [ label=\"wibble\" ] \n}\n";
        Multigraph<TestVertex, DefaultEdge> result =
            new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
        DOTImporter<TestVertex, DefaultEdge> importer = new DOTImporter<TestVertex, DefaultEdge>(
            (l, a) -> new TestVertex(l, a), (f, t, l, a) -> new DefaultEdge());

        importer.importGraph(result, new StringReader(input));
        assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
    }

    @Test
    public void testWithReader()
        throws ImportException
    {
        String input = "graph G {\n" + "  1 [ \"label\"=\"abc123\" ];\n"
            + "  2 [ label=\"fred\" ];\n" + "  1 -- 2;\n" + "}";

        Multigraph<String, DefaultEdge> expected = new Multigraph<>(DefaultEdge.class);
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("1", "2");

        GraphImporter<String, DefaultEdge> importer = buildImporter();

        Graph<String, DefaultEdge> result = new Multigraph<>(DefaultEdge.class);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(2, result.vertexSet().size());
        assertEquals(1, result.edgeSet().size());

    }

    private void testGarbage(String input, String expected)
    {
        DirectedMultigraph<String, DefaultEdge> result =
            new DirectedMultigraph<>(DefaultEdge.class);
        testGarbageGraph(input, expected, result);
    }

    private void testGarbageGraph(String input, String expected, Graph<String, DefaultEdge> graph)
    {
        GraphImporter<String, DefaultEdge> importer = buildImporter();
        try {
            importer.importGraph(graph, new StringReader(input));
            fail("Should not get here");
        } catch (ImportException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    private GraphImporter<String, DefaultEdge> buildImporter()
    {
        return new DOTImporter<String, DefaultEdge>(
            (label, attributes) -> label, (from, to, label, attributes) -> new DefaultEdge());
    }

    private static class GraphWithID
        extends
        SimpleGraph<String, DefaultEdge>
    {
        private static final long serialVersionUID = 1L;

        String id = null;

        protected GraphWithID()
        {
            super(DefaultEdge.class);
        }

    }

    private GraphImporter<String, DefaultEdge> buildGraphIDImporter()
    {
        return new DOTImporter<String, DefaultEdge>(
            (label, attributes) -> label, (from, to, label, attributes) -> new DefaultEdge(), null,
            (component, attributes) -> {
                if (component instanceof GraphWithID) {
                    Attribute idAttribute = attributes.get("ID");
                    String id = "G";
                    if (idAttribute != null) {
                        id = idAttribute.getValue();
                    }
                    ((GraphWithID) component).id = id;
                }
            });
    }

    private class TestVertex
    {
        String id;
        Map<String, Attribute> attributes;

        public TestVertex(String id, Map<String, Attribute> attributes)
        {
            this.id = id;
            this.attributes = attributes;
        }

        public String getId()
        {
            return id;
        }

        public Map<String, Attribute> getAttributes()
        {
            return attributes;
        }

        @Override
        public String toString()
        {
            return id + ", " + attributes;
        }

    }

    private class TestEdge
        extends
        DefaultEdge
    {
        private static final long serialVersionUID = 1L;

        String id;
        Map<String, Attribute> attributes;

        public TestEdge(String id, Map<String, Attribute> attributes)
        {
            super();
            this.id = id;
            this.attributes = attributes;
        }

        public String getId()
        {
            return id;
        }

        public Map<String, Attribute> getAttributes()
        {
            return attributes;
        }

        @Override
        public String toString()
        {
            return id + ", " + attributes;
        }
    }
}
