/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.ImportException;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link JsonImporter}.
 * 
 * @author Dimitrios Michail
 */
public class JSONImporterTest
{

    @Test
    public void testUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":\"3\" },\n"
                     + "  { \"id\":\"4\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\" },\n"
                     + "  { \"source\":\"1\", \"target\":\"3\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
    }

    @Test
    public void testVertexFactory()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":\"3\" },\n"
                     + "  { \"id\":\"4\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\" },\n"
                     + "  { \"source\":\"1\", \"target\":\"3\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.setVertexFactory(id -> String.valueOf("node" + id));
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("node1"));
        assertTrue(g.containsVertex("node2"));
        assertTrue(g.containsVertex("node3"));
        assertTrue(g.containsVertex("node4"));
        assertTrue(g.containsEdge("node1", "node2"));
        assertTrue(g.containsEdge("node1", "node3"));
    }

    @Test
    public void testMixedStringAndIntegerIds()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":1 },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":\"3\" },\n"
                     + "  { \"id\":4 }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":1, \"target\":\"2\" },\n"
                     + "  { \"source\":1, \"target\":3 }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
    }

    @Test(expected = ImportException.class)
    public void testDuplicateNodeIds()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":1 },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":1 }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));
    }

    @Test(expected = ImportException.class)
    public void testMissingSourceOnEdge()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":1 },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\" },\n"
                     + "  { \"target\":\"2\" },\n"                     
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));
    }

    @Test(expected = ImportException.class)
    public void testMissingTargetOnEdge()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":1 },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\" },\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));
    }

    @Test
    public void testWeightsOnWeighted()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":\"3\" },\n"
                     + "  { \"id\":\"4\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\", \"weight\": 2.0 },\n"
                     + "  { \"source\":\"1\", \"target\":\"3\", \"weight\": 3.0 },\n"
                     + "  { \"source\":\"2\", \"target\":\"3\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("1", "3")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);
    }

    @Test
    public void testWeightsOnUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { \"id\":\"3\" },\n"
                     + "  { \"id\":\"4\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\", \"weight\": 2.0 },\n"
                     + "  { \"source\":\"1\", \"target\":\"3\", \"weight\": 3.0 },\n"
                     + "  { \"source\":\"2\", \"target\":\"3\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "3")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);
    }

    @Test
    public void testNodeAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\", \"label\": \"Label\", \"int\": 4, \"double\": 0.5, \"boolean\": true, \"boolean1\": false, \"novalue\": null }\n"
                     + "  ],\n"
                     + "  \"edges\": null"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> attrs = vertexAttributes.get(p.getFirst());
            if (attrs == null) {
                attrs = new HashMap<>();
                vertexAttributes.put(p.getFirst(), attrs);
            }
            attrs.put(p.getSecond(), a);
        });

        importer.importGraph(g, new StringReader(input));

        assertEquals(1, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));

        Map<String, Attribute> attributes = vertexAttributes.get("1");
        assertNotNull(attributes);
        assertTrue(attributes.get("label").getType().equals(AttributeType.STRING));
        assertTrue(attributes.get("label").getValue().equals("Label"));
        assertTrue(attributes.get("int").getType().equals(AttributeType.INT));
        assertTrue(attributes.get("int").getValue().equals("4"));
        assertTrue(attributes.get("double").getType().equals(AttributeType.DOUBLE));
        assertTrue(attributes.get("double").getValue().equals("0.5"));
        assertTrue(attributes.get("boolean").getType().equals(AttributeType.BOOLEAN));
        assertTrue(attributes.get("boolean").getValue().equals("true"));
        assertTrue(attributes.get("boolean1").getType().equals(AttributeType.BOOLEAN));
        assertTrue(attributes.get("boolean1").getValue().equals("false"));
        assertTrue(attributes.get("novalue").getType().equals(AttributeType.NULL));
        assertTrue(attributes.get("novalue").getValue().equals("null"));
    }

    @Test
    public void testEdgeAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"
                     + "  { \"source\":\"1\", \"target\": \"1\", \"label\": \"Label\", \"int\": 4, \"double\": 0.5, \"boolean\": true, \"boolean1\": false, \"novalue\": null }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();

        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        importer.addEdgeAttributeConsumer((p, a) -> {
            Map<String, Attribute> attrs = edgeAttributes.get(p.getFirst());
            if (attrs == null) {
                attrs = new HashMap<>();
                edgeAttributes.put(p.getFirst(), attrs);
            }
            attrs.put(p.getSecond(), a);
        });
        importer.importGraph(g, new StringReader(input));

        assertEquals(1, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));

        DefaultEdge edge = g.getEdge("1", "1");
        assertNotNull(edge);
        Map<String, Attribute> attributes = edgeAttributes.get(edge);
        assertNotNull(attributes);
        assertTrue(attributes.get("label").getType().equals(AttributeType.STRING));
        assertTrue(attributes.get("label").getValue().equals("Label"));
        assertTrue(attributes.get("int").getType().equals(AttributeType.INT));
        assertTrue(attributes.get("int").getValue().equals("4"));
        assertTrue(attributes.get("double").getType().equals(AttributeType.DOUBLE));
        assertTrue(attributes.get("double").getValue().equals("0.5"));
        assertTrue(attributes.get("boolean").getType().equals(AttributeType.BOOLEAN));
        assertTrue(attributes.get("boolean").getValue().equals("true"));
        assertTrue(attributes.get("boolean1").getType().equals(AttributeType.BOOLEAN));
        assertTrue(attributes.get("boolean1").getValue().equals("false"));
        assertTrue(attributes.get("novalue").getType().equals(AttributeType.NULL));
        assertTrue(attributes.get("novalue").getValue().equals("null"));
    }

    @Test
    public void testNestedAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"
                     + "  { \"id\":\"1\", \"custom\": { \"pi\": 3.14 } },\n"            
                     + "  { \"id\":\"2\", \"array\": [ { \"obj\": 3.14 } ] }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"
                     + "  { \"source\":\"1\", \"target\": \"2\", \"array\": [ { \"key1\": 1 }, { \"key2\": 2 } ] },\n"
                     + "  { \"source\":\"2\", \"target\": \"1\", \"obj\": { \"key1\": [ { \"key1\": 1 }, { \"key2\": 2 } ] } }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();

        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> attrs = vertexAttributes.get(p.getFirst());
            if (attrs == null) {
                attrs = new HashMap<>();
                vertexAttributes.put(p.getFirst(), attrs);
            }
            attrs.put(p.getSecond(), a);
        });

        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        importer.addEdgeAttributeConsumer((p, a) -> {
            Map<String, Attribute> attrs = edgeAttributes.get(p.getFirst());
            if (attrs == null) {
                attrs = new HashMap<>();
                edgeAttributes.put(p.getFirst(), attrs);
            }
            attrs.put(p.getSecond(), a);
        });
        importer.importGraph(g, new StringReader(input));

        assertEquals(2, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));

        Map<String, Attribute> attributes = vertexAttributes.get("1");
        assertNotNull(attributes);
        assertTrue(attributes.get("custom").getType().equals(AttributeType.UNKNOWN));
        assertTrue(attributes.get("custom").getValue().equals("{\"pi\":3.14}"));

        attributes = vertexAttributes.get("2");
        assertNotNull(attributes);
        assertTrue(attributes.get("array").getType().equals(AttributeType.UNKNOWN));
        assertTrue(attributes.get("array").getValue().equals("[{\"obj\":3.14}]"));

        DefaultEdge edge = g.getEdge("1", "2");
        assertNotNull(edge);
        attributes = edgeAttributes.get(edge);
        assertNotNull(attributes);
        assertTrue(attributes.get("array").getType().equals(AttributeType.UNKNOWN));
        assertTrue(attributes.get("array").getValue().equals("[{\"key1\":1},{\"key2\":2}]"));

        edge = g.getEdge("2", "1");
        assertNotNull(edge);
        attributes = edgeAttributes.get(edge);
        assertNotNull(attributes);
        assertTrue(attributes.get("obj").getType().equals(AttributeType.UNKNOWN));
        assertTrue(
            attributes.get("obj").getValue().equals("{\"key1\":[{\"key1\":1},{\"key2\":2}]}"));
    }

    @Test
    public void testSingletons()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" },\n"
                     + "  { },\n"
                     + "  { }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(4, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());

    }

    @Test
    public void testNegativeIntegerWeights()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"1\" },\n"
                     + "  { \"id\":\"2\" }\n"
                     + "  ],\n"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"1\", \"target\":\"2\", \"weight\": -2 }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
                .vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();
        importer.importGraph(g, new StringReader(input));

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertEquals(-2.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
    }

    @Test
    public void testCreateVerticesWithAttributes()
        throws ImportException
    {
        // @formatter:off
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"a0\", \"color\":\"gray\" },\n"
                     + "  { \"id\":\"a1\", \"color\":\"green\" },\n"
                     + "  { \"id\":\"a2\", \"color\":\"white\" }\n"
                     + "  ],"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"a0\", \"target\":\"a1\" },\n"
                     + "  { \"source\":\"a0\", \"target\":\"a2\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        JSONImporter<String, DefaultEdge> importer = new JSONImporter<>();

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
        String input = "{\n"
                     + "  \"nodes\": [\n"    
                     + "  { \"id\":\"a0\", \"color\":\"gray\" },\n"
                     + "  { \"id\":\"a1\", \"color\":\"green\" },\n"
                     + "  { \"id\":\"a2\", \"color\":\"white\" }\n"
                     + "  ],"
                     + "  \"edges\": [\n"    
                     + "  { \"source\":\"a0\", \"target\":\"a1\", \"label\":\"e1\" },\n"
                     + "  { \"source\":\"a0\", \"target\":\"a2\", \"label\":\"e2\" }\n"
                     + "  ]\n"
                     + "}";
        // @formatter:on

        JSONImporter<String, String> importer = new JSONImporter<>();

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
