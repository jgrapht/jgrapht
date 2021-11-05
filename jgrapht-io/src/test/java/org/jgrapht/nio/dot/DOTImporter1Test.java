/*
 * (C) Copyright 2015-2021, by Wil Selwood and Contributors.
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
import static org.junit.Assert.fail;

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

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>();
        importer.addGraphAttributeConsumer((k, a) -> {
            assertEquals(k, "ID");
            assertEquals(a.getValue(), id);
        });
        importer.importGraph(result, new StringReader(input));
    }

    @Test
    public void testImportWrongID()
        throws ImportException
    {
        String invalidID = "2test";
        String input = "graph " + invalidID + " {\n}\n";

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        try {
            DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>();
            importer.importGraph(result, new StringReader(input));
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
            Multigraph<String, DefaultEdge> result = new Multigraph<>(
                SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            try {
                DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>();
                importer.importGraph(result, new StringReader(invalidInput));
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
        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>();
        importer.addGraphAttributeConsumer((k, a) -> {
            fail("Id should not be consumed");
        });
        importer.importGraph(result, new StringReader(input));
    }

    @Test
    public void testImportNoID()
        throws ImportException
    {
        String input = "strict graph {\n}\n";
        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>();
        importer.addGraphAttributeConsumer((k, a) -> {
            fail("Id should not be consumed");
        });
        importer.importGraph(result, new StringReader(input));
    }

    @Test
    public void testUndirectedWithLabels()
        throws ImportException
    {
        String input = "graph G {\n" + "  1 [ \"label\"=\"abc123\" ];\n"
            + "  2 [ label=\"fred\" ];\n" + "  1 -- 2;\n" + "}";

        Multigraph<String, DefaultEdge> expected = new Multigraph<>(DefaultEdge.class);
        expected.addVertex("0");
        expected.addVertex("1");
        expected.addEdge("0", "1");

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

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(2, result.vertexSet().size());
        assertEquals(1, result.edgeSet().size());

        assertEquals(attrs.get("0").get("label").getValue(), "abc123");
        assertEquals(attrs.get("0").get("ID").getValue(), "1");
        assertEquals(attrs.get("1").get("label").getValue(), "fred");
        assertEquals(attrs.get("1").get("ID").getValue(), "2");

    }

    @Test
    public void testDirectedNoLabels()
        throws ImportException
    {
        String input =
            "digraph graphname {\r\n" + "     a -> b -> c;\r\n" + "     b -> d;\r\n" + " }";

        DirectedMultigraph<String, DefaultEdge> expected =
            new DirectedMultigraph<>(DefaultEdge.class);
        expected.addVertex("0");
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addEdge("0", "1");
        expected.addEdge("1", "2");
        expected.addEdge("1", "3");

        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();

        DirectedMultigraph<String, DefaultEdge> result = new DirectedMultigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
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
        expected.addVertex("0");
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("0", "1");
        expected.addEdge("1", "2");

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        DirectedMultigraph<String, DefaultEdge> result = new DirectedMultigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(result, new StringReader(input));

        assertEquals(expected.toString(), result.toString());

        assertEquals(attrs.get("0").get("label").getValue(), "Test");
        assertEquals(attrs.get("0").get("ID").getValue(), "a");
        assertEquals(attrs.get("1").get("label").getValue(), "Test");
        assertEquals(attrs.get("1").get("ID").getValue(), "b");
        assertEquals(attrs.get("2").get("label").getValue(), "Test");
        assertEquals(attrs.get("2").get("ID").getValue(), "c");
    }

    @Test
    public void testMultiLinksUndirected()
        throws ImportException
    {
        String input = "graph G {\n" + "  1 [ label=\"bob\" ];\n" + "  2 [ label=\"fred\" ];\n"
        // the extra label will be ignored but not cause any problems.
            + "  1 -- 2 [ label=\"friend\"];\n" + "  1 -- 2;\n" + "}";

        Multigraph<String, DefaultEdge> expected = new Multigraph<>(DefaultEdge.class);
        expected.addVertex("0");
        expected.addVertex("1");
        expected.addEdge("0", "1", new DefaultEdge());
        expected.addEdge("0", "1", new DefaultEdge());

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();
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
        start.addVertex("0");
        start.addVertex("1");
        start.addVertex("2");
        start.addVertex("3");
        start.addEdge("0", "1");
        start.addEdge("1", "2");
        start.addEdge("1", "3");

        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(v -> v);

        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(start, os);
        String output = new String(os.toByteArray(), "UTF-8");

        DirectedMultigraph<String, DefaultEdge> result = new DirectedMultigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

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

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.importGraph(result, new StringReader(input));

        assertEquals(1, result.vertexSet().size());
        String v = result.vertexSet().stream().findFirst().get();
        assertEquals("0", v);
        assertEquals("a", attrs.get("0").get("ID").getValue());
        assertEquals(
            "------this------contains-------dashes------", attrs.get("0").get("label").getValue());
    }

    @Test
    public void testAttributesWithNoQuotes()
        throws ImportException
    {
        String input =
            "graph G {\n" + "  1 [ label = \"bob\" \"foo\"=bar ];\n" + "  2 [ label = \"fred\" ];\n"
            // the extra label will be ignored but not cause any problems.
                + "  1 -- 2 [ label = \"friend\" \"foo\" = wibble];\n" + "}";

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        Map<DefaultEdge, Map<String, Attribute>> edgeAttrs = new HashMap<>();
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });
        importer.addEdgeAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = edgeAttrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                edgeAttrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(result, new StringReader(input));

        assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());

        assertEquals(attrs.get("0").get("ID").getValue(), "1");
        assertEquals(attrs.get("0").get("label").getValue(), "bob");
        assertEquals(attrs.get("0").get("foo").getValue(), "bar");
        assertEquals(attrs.get("1").get("ID").getValue(), "2");
        assertEquals(attrs.get("1").get("label").getValue(), "fred");

        DefaultEdge edge = result.getEdge("0", "1");
        assertEquals(edgeAttrs.get(edge).get("label").getValue(), "friend");
        assertEquals(edgeAttrs.get(edge).get("foo").getValue(), "wibble");
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
    public void testAttributesWithNoValues()
        throws ImportException
    {
        String input =
            "graph G {\n" + "  1 [ label = \"bob\" \"foo\" ];\n" + "  2 [ label = \"fred\" ];\n"
            // the extra label will be ignored but not cause any problems.
                + "  1 -- 2 [ label = friend foo];\n" + "}";

        Multigraph<String, DefaultEdge> graph = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

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

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        importer.importGraph(result, new StringReader(input));

        assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());
        for (String v : result.vertexSet()) {
            if ("0".equals(v)) {
                assertEquals("wrong number of attributes", 2, attrs.get(v).size());
            } else {
                assertEquals("attributes are populated", 1, attrs.get(v).size());
            }
        }

        assertEquals(attrs.get("0").get("foo").getValue(), "bar");
        assertEquals(attrs.get("0").get("ID").getValue(), "a");
        assertEquals(attrs.get("1").get("ID").getValue(), "b");
    }

    @Test
    public void testParametersWithSemicolons()
        throws ImportException
    {
        String input = "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];\n}\n";

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

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

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

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

        importer.importGraph(result, new StringReader(input));

        assertEquals("wrong size of vertexSet", 1, result.vertexSet().size());
        assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
        assertEquals(attrs.get("0").get("ID").getValue(), "node0");
        assertEquals(
            attrs.get("0").get("label").getValue(),
            "this \"label; \"contains an escaped semi colon");
    }

    @Test
    public void testNoLineEndBetweenNodes()
        throws ImportException
    {
        String input =
            "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];  2 [ label=\"wibble\" ] \n}\n";

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();

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
        expected.addVertex("0");
        expected.addVertex("1");
        expected.addEdge("0", "1");

        Multigraph<String, DefaultEdge> result = new Multigraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();
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
        GraphImporter<String, DefaultEdge> importer = new DOTImporter<>();
        try {
            importer.importGraph(graph, new StringReader(input));
            fail("Should not get here");
        } catch (ImportException e) {
            assertEquals(expected, e.getMessage());
        }
    }

}
