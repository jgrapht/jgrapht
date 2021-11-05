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
package org.jgrapht.nio.graphml;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class GraphMLImporterTest
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
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL +  
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testVertexFactory()
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

        GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<>();
        importer.setVertexFactory(id -> String.valueOf("node" + id));
        importer.importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("node1"));
        assertTrue(g.containsVertex("node2"));
        assertTrue(g.containsVertex("node3"));
        assertTrue(g.containsEdge("node1", "node2"));
        assertTrue(g.containsEdge("node2", "node3"));
        assertTrue(g.containsEdge("node3", "node1"));
    }

    @Test
    public void testUndirectedUnweightedFromInputStream()
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
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL +  
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testUndirectedUnweightedPseudoGraph()
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
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL + 
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<edge source=\"1\" target=\"1\"/>"+ NL +
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(5, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("1", "1"));
    }

    @Test
    public void testUndirectedUnweightedStringKeys()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph edgedefault=\"undirected\">" + NL + 
            "<node id=\"n1\"/>" + NL +
            "<node id=\"n2\"/>" + NL + 
            "<node id=\"n3\"/>" + NL + 
            "<edge source=\"n1\" target=\"n2\"/>" + NL + 
            "<edge source=\"n2\" target=\"n3\"/>" + NL + 
            "<edge source=\"n3\" target=\"n1\"/>"+ NL +
            "<edge source=\"n1\" target=\"n1\"/>"+ NL +
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("1", "1"));
    }

    @Test
    public void testUndirectedUnweightedWrongOrder()
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
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testDirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph id=\"G\" edgedefault=\"directed\">" + NL + 
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, true, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertFalse(g.containsEdge("2", "1"));
        assertTrue(g.containsEdge("2", "3"));
        assertFalse(g.containsEdge("3", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertFalse(g.containsEdge("1", "3"));
    }

    @Test
    public void testUndirectedUnweightedPrefix()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<gml:graphml xmlns:gml=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<gml:graph id=\"G\" edgedefault=\"undirected\">" + NL + 
            "<gml:node id=\"1\"/>" + NL +
            "<gml:node id=\"2\"/>" + NL + 
            "<gml:node id=\"3\"/>" + NL + 
            "<gml:edge source=\"1\" target=\"2\"/>" + NL + 
            "<gml:edge source=\"2\" target=\"3\"/>" + NL + 
            "<gml:edge source=\"3\" target=\"1\"/>"+ NL + 
            "</gml:graph>" + NL + 
            "</gml:graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testWithAttributes()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL +
            "<default>yellow</default>" + NL +
            "</key>" + NL +
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
            "<data key=\"d1\">1.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
    }

    @Test
    public void testWithMapAttributes()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL +
            "<default>yellow</default>" + NL +
            "</key>" + NL +
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
            "<data key=\"d1\">1.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Map<String, Map<String, Attribute>> vAttributes = new HashMap<>();
        Map<DefaultEdge, Map<String, Attribute>> eAttributes = new HashMap<>();
        Graph<String, DefaultEdge> g =
            readGraph(input, DefaultEdge.class, false, false, vAttributes, eAttributes);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());

        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));

        assertEquals("green", vAttributes.get("1").get("color").getValue());
        assertEquals(AttributeType.STRING, vAttributes.get("1").get("color").getType());
        assertEquals("yellow", vAttributes.get("2").get("color").getValue());
        assertEquals(AttributeType.STRING, vAttributes.get("2").get("color").getType());
        assertEquals("blue", vAttributes.get("3").get("color").getValue());
        assertEquals(AttributeType.STRING, vAttributes.get("3").get("color").getType());
        assertEquals("2.0", eAttributes.get(g.getEdge("1", "3")).get("weight").getValue());
        assertEquals(
            AttributeType.DOUBLE, eAttributes.get(g.getEdge("1", "3")).get("weight").getType());
        assertEquals("1.0", eAttributes.get(g.getEdge("1", "2")).get("weight").getValue());
        assertEquals(
            AttributeType.DOUBLE, eAttributes.get(g.getEdge("1", "2")).get("weight").getType());
        assertNull(eAttributes.get(g.getEdge("2", "3")));
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
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL +
            "<default>yellow</default>" + NL +
            "</key>" + NL +
            "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL +
            "<default>3.0</default>" + NL +
            "</key>" + NL +
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
            "<data key=\"d1\">1.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            readGraph(input, DefaultWeightedEdge.class, true, true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "3")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);
    }

    @Test
    public void testWithAttributesCustomNamedWeightedGraphs()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL +
            "<default>yellow</default>" + NL +
            "</key>" + NL +
            "<key id=\"d1\" for=\"edge\" attr.name=\"myvalue\" attr.type=\"double\">" + NL +
            "<default>3.0</default>" + NL +
            "</key>" + NL +
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
            "<data key=\"d1\">1.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
            SupplierUtil.createStringSupplier(0), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);
        Map<String, Map<String, Attribute>> vAttributes = new HashMap<>();
        Map<DefaultWeightedEdge, Map<String, Attribute>> eAttributes = new HashMap<>();

        GraphMLImporter<String, DefaultWeightedEdge> importer =
            createGraphImporter(vAttributes, eAttributes);

        importer.setEdgeWeightAttributeName("myvalue");
        importer.importGraph(g, new StringReader(input));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("0", "2"));
        assertTrue(g.containsEdge("0", "1"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("0", "2")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("0", "1")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);

        assertEquals(vAttributes.get("0").get("ID").getValue(), "n0");
    }

    @Test
    public void testWithAttributesCustomNamedWeightedForAllGraphs()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL +
            "<default>yellow</default>" + NL +
            "</key>" + NL +
            "<key id=\"d1\" for=\"all\" attr.name=\"myvalue\" attr.type=\"double\">" + NL +
            "<default>3.0</default>" + NL +
            "</key>" + NL +
            "<key id=\"d2\" for=\"all\" attr.name=\"onemore\" attr.type=\"string\"/>" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"n0\">" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "</node>" + NL +
            "<node id=\"n1\">" + NL +
            "<data key=\"d2\">value1</data>" + NL +
            "</node>" + NL +
            "<node id=\"n2\">" + NL +
            "<data key=\"d0\">blue</data>" + NL +
            "</node>" + NL+
            "<edge id=\"e0\" source=\"n0\" target=\"n2\">" + NL +
            "<data key=\"d1\">2.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e1\" source=\"n0\" target=\"n1\">" + NL +
            "<data key=\"d1\">1.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

        Map<String, Map<String, Attribute>> vAttributes = new HashMap<>();
        Map<DefaultWeightedEdge, Map<String, Attribute>> eAttributes = new HashMap<>();

        GraphMLImporter<String, DefaultWeightedEdge> importer =
            createGraphImporter(vAttributes, eAttributes);

        importer.setEdgeWeightAttributeName("myvalue");
        importer.importGraph(g, new StringReader(input));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "3")), 1e-9);
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);

        assertEquals("3.0", vAttributes.get("1").get("myvalue").getValue());
        assertEquals("n0", vAttributes.get("1").get("ID").getValue());
        assertEquals("3.0", vAttributes.get("2").get("myvalue").getValue());
        assertEquals("n1", vAttributes.get("2").get("ID").getValue());
        assertEquals("3.0", vAttributes.get("3").get("myvalue").getValue());
        assertEquals("n2", vAttributes.get("3").get("ID").getValue());

        assertFalse(vAttributes.get("1").containsKey("onemore"));
        assertEquals("value1", vAttributes.get("2").get("onemore").getValue());
        assertFalse(vAttributes.get("3").containsKey("onemore"));
        assertFalse(eAttributes.get(g.getEdge("1", "3")).containsKey("onemore"));
        assertFalse(eAttributes.get(g.getEdge("1", "2")).containsKey("onemore"));
        assertFalse(eAttributes.get(g.getEdge("2", "3")).containsKey("onemore"));
    }

    @Test
    public void testWithHyperEdges()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"n0\"/>" + NL +
            "<node id=\"n1\"/>" + NL +
            "<node id=\"n2\"/>" + NL +
            "<node id=\"n3\">" + NL +
            "  <port name=\"North\"/>" + NL +
            "  <port name=\"South\"/>" + NL +
            "</node>" + NL +
            "<edge source=\"n0\" target=\"n1\"/>" + NL +
            "<edge source=\"n0\" target=\"n3\"/>" + NL +
            "<hyperedge>" + NL +
            "  <endpoint node=\"n0\"/>" + NL +
            "  <endpoint node=\"n1\"/>" + NL +
            "  <endpoint node=\"n2\"/>" + NL +
            "  <endpoint node=\"n3\" port=\"South\"/>" + NL +
            "</hyperedge>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "4"));
    }

    @Test
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

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testValidateNoNodeId()
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
            "<node/>" + NL +
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testValidateDuplicateNode()
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
            "<node id=\"1\"/>" + NL +
            "<node id=\"1\"/>" + NL +
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("No!");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testValidWithXLinkNodeAttrib()
        throws ImportException
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xmlns:xlink=\"http://www.w3.org/1999/xlink\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " +
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"1\" xlink:href=\"http://graphml.graphdrawing.org\" />" + NL +
            "<node id=\"2\"/>" + NL +
            "<node id=\"3\"/>" + NL +
            "<edge source=\"1\" target=\"2\"/>" + NL +
            "<edge source=\"2\" target=\"3\"/>" + NL +
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testExportImport()
        throws Exception
    {
        DirectedPseudograph<String, DefaultEdge> g1 = new DirectedPseudograph<>(DefaultEdge.class);
        g1.addVertex("1");
        g1.addVertex("2");
        g1.addVertex("3");
        g1.addEdge("1", "2");
        g1.addEdge("2", "3");
        g1.addEdge("3", "3");

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g1, os);
        String output = new String(os.toByteArray(), "UTF-8");

        Graph<String, DefaultEdge> g2 = readGraph(output, DefaultEdge.class, true, false);

        assertEquals(3, g2.vertexSet().size());
        assertEquals(3, g2.edgeSet().size());
        assertTrue(g2.containsEdge("1", "2"));
        assertTrue(g2.containsEdge("2", "3"));
        assertTrue(g2.containsEdge("3", "3"));
    }

    @Test
    public void testWithAttributesAtGraphLevel()
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
            "<data key=\"d0\">green</data>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
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

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
    }

    @Test
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

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(7, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsVertex("6"));
        assertTrue(g.containsVertex("7"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("5", "6"));
        assertTrue(g.containsEdge("4", "7"));
    }

    @Test
    public void testUnsupportedGraph()
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
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL + 
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<edge source=\"3\" target=\"1\"/>"+ NL +
            "<edge source=\"1\" target=\"1\"/>"+ NL +
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        final Graph<String, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        try {
            GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<>();
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (Exception e) {
            // nothing
        }
    }

    @Test
    public void testNonValidApoc()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL + 
                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL + 
                 "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>" + NL +
            "<key id=\"id\" for=\"node\" attr.name=\"id\" attr.type=\"long\"/>" + NL +
            "<graph id=\"G\" edgedefault=\"directed\">" + NL +
                "<node id=\"n45\" labels=\":Person:ENTITY\">" + NL + 
                    "<data key=\"labels\">:Person:ENTITY</data>" + NL +
                    "<data key=\"name\">Person1</data>" + NL +
                    "<data key=\"id\">1</data>" + NL +
                "</node>" + NL +
                "<node id=\"n46\" labels=\":Person:ENTITY\">" + NL +
                    "<data key=\"labels\">:Person:ENTITY</data>" + NL +
                    "<data key=\"name\">Person2</data>" + NL +
                    "<data key=\"id\">2</data>" + NL +
                "</node>" + NL +
                "<edge id=\"e34\" source=\"n45\" target=\"n46\" label=\"daughter\">" + NL +
                    "<data key=\"label\">daughter</data>" + NL +
                "</edge>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        HashMap<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        HashMap<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();

        GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<>();

        importer.addVertexAttributeConsumer((k, a) -> {
            String vertex = k.getFirst();
            Map<String, Attribute> attrs = vertexAttributes.get(vertex);
            if (attrs == null) {
                attrs = new HashMap<>();
                vertexAttributes.put(vertex, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        importer.addEdgeAttributeConsumer((k, a) -> {
            DefaultEdge edge = k.getFirst();
            Map<String, Attribute> attrs = edgeAttributes.get(edge);
            if (attrs == null) {
                attrs = new HashMap<>();
                edgeAttributes.put(edge, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        importer.setSchemaValidation(false);

        importer.importGraph(g, new StringReader(input));

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        for (Map<String, Attribute> va : vertexAttributes.values()) {
            assertTrue(va.containsKey("name"));
            assertTrue(va.containsKey("id"));
            assertFalse(va.containsKey("labels"));
        }
        for (Map<String, Attribute> ea : edgeAttributes.values()) {
            assertTrue(ea.isEmpty());
        }
    }

    @Test(expected = ImportException.class)
    public void testNonValidNoVertexId()
        throws ImportException
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL + 
                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL + 
                 "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<graph id=\"G\" edgedefault=\"directed\">" + NL +
                "<node />" + NL + 
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<>();

        importer.setSchemaValidation(false);

        importer.importGraph(g, new StringReader(input));
    }

    public <E> Graph<String, E> readGraph(
        String input, Class<E> edgeClass, boolean directed, boolean weighted)
        throws ImportException
    {
        return readGraph(
            input, edgeClass, directed, weighted, new HashMap<>(),
            new HashMap<E, Map<String, Attribute>>());
    }

    public <E> Graph<String, E> readGraph(
        InputStream input, Class<E> edgeClass, boolean directed, boolean weighted)
        throws ImportException
    {
        return readGraph(
            input, edgeClass, directed, weighted, new HashMap<>(),
            new HashMap<E, Map<String, Attribute>>());
    }

    public <E> Graph<String, E> readGraph(
        String input, Class<E> edgeClass, boolean directed, boolean weighted,
        Map<String, Map<String, Attribute>> vertexAttributes,
        Map<E, Map<String, Attribute>> edgeAttributes)
        throws ImportException
    {
        return readGraph(
            new ByteArrayInputStream(input.getBytes()), edgeClass, directed, weighted,
            vertexAttributes, edgeAttributes);
    }

    public <E> Graph<String, E> readGraph(
        InputStream input, Class<E> edgeClass, boolean directed, boolean weighted,
        Map<String, Map<String, Attribute>> vertexAttributes,
        Map<E, Map<String, Attribute>> edgeAttributes)
        throws ImportException
    {
        Graph<String, E> g;

        if (directed) {
            g = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(weighted)
                .vertexSupplier(SupplierUtil.createStringSupplier(1)).edgeClass(edgeClass)
                .buildGraph();
        } else {
            g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(weighted)
                .vertexSupplier(SupplierUtil.createStringSupplier(1)).edgeClass(edgeClass)
                .buildGraph();
        }

        GraphMLImporter<String, E> importer = new GraphMLImporter<>();

        importer.addVertexAttributeConsumer((k, a) -> {
            String vertex = k.getFirst();
            Map<String, Attribute> attrs = vertexAttributes.get(vertex);
            if (attrs == null) {
                attrs = new HashMap<>();
                vertexAttributes.put(vertex, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        importer.addEdgeAttributeConsumer((k, a) -> {
            E edge = k.getFirst();
            Map<String, Attribute> attrs = edgeAttributes.get(edge);
            if (attrs == null) {
                attrs = new HashMap<>();
                edgeAttributes.put(edge, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        importer.importGraph(g, input);
        return g;
    }

    public <E> GraphMLImporter<String, E> createGraphImporter(
        Map<String, Map<String, Attribute>> vertexAttributes,
        Map<E, Map<String, Attribute>> edgeAttributes)
    {
        GraphMLImporter<String, E> importer = new GraphMLImporter<>();

        importer.addVertexAttributeConsumer((k, a) -> {
            String vertex = k.getFirst();
            Map<String, Attribute> attrs = vertexAttributes.get(vertex);
            if (attrs == null) {
                attrs = new HashMap<>();
                vertexAttributes.put(vertex, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        importer.addEdgeAttributeConsumer((k, a) -> {
            E edge = k.getFirst();
            Map<String, Attribute> attrs = edgeAttributes.get(edge);
            if (attrs == null) {
                attrs = new HashMap<>();
                edgeAttributes.put(edge, attrs);
            }
            attrs.put(k.getSecond(), a);
        });

        return importer;
    }

}
