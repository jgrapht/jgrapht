/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dimitrios Michail
 */
public class SimpleGraphMLImporterTest
{

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL +  
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGraphMLImporter<String, DefaultEdge>()
            .importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("0", "1"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "0"));
    }

    @Test
    public void testUndirectedUnweightedWithConsumers()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL + 
            "<edge source=\"v\" target=\"x\"/>" + NL + 
            "<node id=\"u\"/>" + NL +
            "<node id=\"v\"/>" + NL + 
            "<node id=\"x\"/>" + NL +  
            "<edge source=\"u\" target=\"v\"/>" + NL + 
            "<edge source=\"x\" target=\"u\"/>"+ NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        Map<Pair<String, String>, Attribute> vertexAttrs = new HashMap<>();
        Map<Pair<DefaultEdge, String>, Attribute> edgeAttrs = new HashMap<>();
        Map<String, Attribute> graphAttrs = new HashMap<>();

        SimpleGraphMLImporter<String, DefaultEdge> importer =
            new SimpleGraphMLImporter<String, DefaultEdge>();
        importer.addVertexAttributeConsumer((k, v) -> vertexAttrs.put(k, v));
        importer.addEdgeAttributeConsumer((k, v) -> edgeAttrs.put(k, v));
        importer.addGraphAttributeConsumer((k, v) -> graphAttrs.put(k, v));
        importer.importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        // check graph
        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("0", "1"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "0"));

        // check collected attributes
        assertEquals(vertexAttrs.get(Pair.of("0", "id")), DefaultAttribute.createAttribute("v"));
        assertEquals(vertexAttrs.get(Pair.of("1", "id")), DefaultAttribute.createAttribute("x"));
        assertEquals(vertexAttrs.get(Pair.of("2", "id")), DefaultAttribute.createAttribute("u"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "source")),
            DefaultAttribute.createAttribute("v"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "target")),
            DefaultAttribute.createAttribute("x"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("1", "2"), "source")),
            DefaultAttribute.createAttribute("x"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("1", "2"), "target")),
            DefaultAttribute.createAttribute("u"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("2", "0"), "source")),
            DefaultAttribute.createAttribute("u"));
        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("2", "0"), "target")),
            DefaultAttribute.createAttribute("v"));
    }

    @Test
    public void testWithAttributesWeightedGraphs()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"n0\">" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "</node>" + NL +
            "<node id=\"n1\"/>" + NL +
            "<node id=\"n2\">" + NL +
            "<data key=\"d0\">blue</data>" + NL +
            "</node>" + NL+
            "<edge id=\"e0\" source=\"n0\" target=\"n2\">" + NL +
            "<data key=\"d1\">2.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e1\" source=\"n0\" target=\"n1\">" + NL +
            "<data key=\"d1\">3.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(true).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        Map<Pair<String, String>, Attribute> vertexAttrs = new HashMap<>();
        Map<Pair<DefaultEdge, String>, Attribute> edgeAttrs = new HashMap<>();
        Map<String, Attribute> graphAttrs = new HashMap<>();

        SimpleGraphMLImporter<String, DefaultEdge> importer =
            new SimpleGraphMLImporter<String, DefaultEdge>();
        importer.addVertexAttributeConsumer((k, v) -> vertexAttrs.put(k, v));
        importer.addEdgeAttributeConsumer((k, v) -> edgeAttrs.put(k, v));
        importer.addGraphAttributeConsumer((k, v) -> graphAttrs.put(k, v));
        importer.importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("0", "2"));
        assertTrue(g.containsEdge("0", "1"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("0", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("0", "1")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
    }

    @Test(expected = ImportException.class)
    public void testValidate()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL + 
            "<nOde id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<myedge source=\"1\" target=\"2\"/>" + NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGraphMLImporter<String, DefaultEdge>()
            .importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test(expected = ImportException.class)
    public void testNestedGraphs()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " +
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"all\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<key id=\"d1\" for=\"all\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<key id=\"d2\" for=\"all\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "<node id=\"n0\"/>" + NL +
            "<node id=\"n1\">" + NL +
            "  <graph id=\"n1:\" edgedefault=\"undirected\">" + NL +
            "    <node id=\"n1:n0\"/>" + NL +
            "    <node id=\"n1:n1\"/>" + NL +
            "    <data key=\"d0\">green</data>" + NL +
            "    <edge source=\"n1:n0\" target=\"n1:n1\"/>" + NL +
            "  </graph>" + NL +
            "</node>" + NL +
            "<node id=\"n2\">" + NL +
            "  <graph id=\"n2:\" edgedefault=\"undirected\">" + NL +
            "    <node id=\"n2:n0\"/>" + NL +
            "    <node id=\"n2:n1\"/>" + NL +
            "    <data key=\"d0\">green</data>" + NL +
            "    <edge source=\"n2:n0\" target=\"n2:n1\"/>" + NL +
            "  </graph>" + NL +
            "</node>" + NL +
            "<edge id=\"e1\" source=\"n1\" target=\"n2\"/>" + NL +
            "<data key=\"d1\">green</data>" + NL +
            "</graph>" + NL +
            "<data key=\"d2\">green</data>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGraphMLImporter<String, DefaultEdge>()
            .importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testWithAttributesAtGraphMLLevel()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " +
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"graph\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"n0\"/>" + NL +
            "<node id=\"n1\"/>" + NL +
            "<edge id=\"e1\" source=\"n0\" target=\"n1\"/>" + NL +
            "</graph>" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        Map<Pair<String, String>, Attribute> vertexAttrs = new HashMap<>();
        Map<Pair<DefaultEdge, String>, Attribute> edgeAttrs = new HashMap<>();
        Map<String, Attribute> graphAttrs = new HashMap<>();

        SimpleGraphMLImporter<String, DefaultEdge> importer =
            new SimpleGraphMLImporter<String, DefaultEdge>();
        importer.addVertexAttributeConsumer((k, v) -> vertexAttrs.put(k, v));
        importer.addEdgeAttributeConsumer((k, v) -> edgeAttrs.put(k, v));
        importer.addGraphAttributeConsumer((k, v) -> graphAttrs.put(k, v));
        importer.importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsEdge("0", "1"));

        assertEquals(DefaultAttribute.createAttribute("green"), graphAttrs.get("color"));
        assertEquals(DefaultAttribute.createAttribute("undirected"), graphAttrs.get("edgedefault"));
        assertEquals(DefaultAttribute.createAttribute("n0"), vertexAttrs.get(Pair.of("0", "id")));
        assertEquals(DefaultAttribute.createAttribute("n1"), vertexAttrs.get(Pair.of("1", "id")));
        assertEquals(
            DefaultAttribute.createAttribute("e1"),
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "id")));
        assertEquals(
            DefaultAttribute.createAttribute("n0"),
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "source")));
        assertEquals(
            DefaultAttribute.createAttribute("n1"),
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "target")));
    }

}
