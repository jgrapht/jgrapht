/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.gexf;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class SimpleGEXFImporterTest
{

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <nodes>" + NL
            + "      <node id=\"1\" label=\"1\"/>" + NL 
            + "      <node id=\"2\" label=\"2\"/>" + NL
            + "      <node id=\"3\" label=\"3\"/>" + NL 
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"2\" target=\"3\" />" + NL            
            + "      <edge id=\"0\" source=\"1\" target=\"2\" />" + NL
            + "      <edge id=\"2\" source=\"3\" target=\"1\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGEXFImporter<String, DefaultEdge>()
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
    public void testUndirectedUnweightedWithMeta()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <meta lastmodifieddate=\"2019-10-26\">" + NL
            + "    <creator>JGraphT</creator>" + NL
            + "    <description>JGraphT test</description>" + NL
            + "    <keywords>graph, jgrapht</keywords>" + NL
            + "  </meta>" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <nodes>" + NL
            + "      <node id=\"1\" label=\"1\"/>" + NL 
            + "      <node id=\"2\" label=\"2\"/>" + NL
            + "      <node id=\"3\" label=\"3\"/>" + NL 
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"2\" target=\"3\" />" + NL            
            + "      <edge id=\"0\" source=\"1\" target=\"2\" />" + NL
            + "      <edge id=\"2\" source=\"3\" target=\"1\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGEXFImporter<String, DefaultEdge>()
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
    public void testVertexFactory()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <nodes>" + NL
            + "      <node id=\"1\" label=\"1\"/>" + NL 
            + "      <node id=\"2\" label=\"2\"/>" + NL
            + "      <node id=\"3\" label=\"3\"/>" + NL 
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"2\" target=\"3\" />" + NL            
            + "      <edge id=\"0\" source=\"1\" target=\"2\" />" + NL
            + "      <edge id=\"1\" source=\"3\" target=\"1\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        SimpleGEXFImporter<String, DefaultEdge> importer =
            new SimpleGEXFImporter<String, DefaultEdge>();
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
    public void testUndirectedUnweightedWithConsumers()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"v\" target=\"x\" />" + NL
            + "    </edges>" + NL
            + "    <nodes>" + NL
            + "      <node id=\"u\" label=\"node-u\"/>" + NL 
            + "      <node id=\"v\" label=\"node-v\"/>" + NL
            + "      <node id=\"x\" label=\"node-x\"/>" + NL 
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"2\" source=\"u\" target=\"v\" />" + NL            
            + "      <edge id=\"3\" source=\"x\" target=\"u\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        Map<Pair<String, String>, Attribute> vertexAttrs = new HashMap<>();
        Map<Pair<DefaultEdge, String>, Attribute> edgeAttrs = new HashMap<>();
        Map<String, Attribute> graphAttrs = new HashMap<>();

        SimpleGEXFImporter<String, DefaultEdge> importer =
            new SimpleGEXFImporter<String, DefaultEdge>();
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
        assertEquals(vertexAttrs.get(Pair.of("0", "ID")), DefaultAttribute.createAttribute("v"));
        assertEquals(
            vertexAttrs.get(Pair.of("0", "label")), DefaultAttribute.createAttribute("node-v"));
        assertEquals(vertexAttrs.get(Pair.of("1", "ID")), DefaultAttribute.createAttribute("x"));
        assertEquals(
            vertexAttrs.get(Pair.of("1", "label")), DefaultAttribute.createAttribute("node-x"));
        assertEquals(vertexAttrs.get(Pair.of("2", "ID")), DefaultAttribute.createAttribute("u"));
        assertEquals(
            vertexAttrs.get(Pair.of("2", "label")), DefaultAttribute.createAttribute("node-u"));
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
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"directed\">" + NL
            + "    <attributes class=\"node\">" + NL
            + "      <attribute id=\"0\" title=\"color\"  type=\"string\"/>" + NL
            + "      <attribute id=\"1\" title=\"city\"   type=\"liststring\"/>" + NL
            + "      <attribute id=\"2\" title=\"weight\" type=\"double\"/>" + NL
            + "    </attributes>" + NL                
            + "    <attributes class=\"edge\">" + NL
            + "      <attribute id=\"0\" title=\"length\" type=\"double\"/>" + NL
            + "    </attributes>" + NL            
            + "    <nodes>" + NL
            + "      <node id=\"1\">" + NL
            + "        <attvalues>" + NL
            + "          <attvalue for=\"0\" value=\"Red\"/>" + NL
            + "          <attvalue for=\"1\" value=\"Paris\"/>" + NL
            + "          <attvalue for=\"2\" value=\"100.0\"/>" + NL
            + "        </attvalues>" + NL
            + "      </node>" + NL
            + "      <node id=\"2\" label=\"2\"/>" + NL
            + "      <node id=\"3\">" + NL
            + "        <attvalues>" + NL
            + "          <attvalue for=\"0\" value=\"Black\"/>" + NL
            + "          <attvalue for=\"1\" value=\"NY\"/>" + NL
            + "          <attvalue for=\"2\" value=\"200.0\"/>" + NL
            + "        </attvalues>" + NL
            + "      </node>" + NL            
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"2\" target=\"3\" weight=\"1.2\" />" + NL            
            + "      <edge id=\"0\" source=\"1\" target=\"2\" weight=\"0.1\">" + NL
            + "        <attvalues>" + NL
            + "          <attvalue for=\"0\" value=\"333.0\"/>" + NL
            + "        </attvalues>" + NL
            + "      </edge>" + NL
            + "      <edge id=\"1\" source=\"3\" target=\"1\" weight=\"2.0\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .directed().weighted(true).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        Map<Pair<String, String>, Attribute> vertexAttrs = new HashMap<>();
        Map<Pair<DefaultEdge, String>, Attribute> edgeAttrs = new HashMap<>();
        Map<String, Attribute> graphAttrs = new HashMap<>();

        SimpleGEXFImporter<String, DefaultEdge> importer =
            new SimpleGEXFImporter<String, DefaultEdge>();
        importer.addVertexAttributeConsumer((k, v) -> vertexAttrs.put(k, v));
        importer.addEdgeAttributeConsumer((k, v) -> edgeAttrs.put(k, v));
        importer.addGraphAttributeConsumer((k, v) -> graphAttrs.put(k, v));
        importer.importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("0"));
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("0", "1"));
        assertTrue(g.containsEdge("2", "0"));
        assertEquals(1.2, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(0.1, g.getEdgeWeight(g.getEdge("0", "1")), 1e-9);
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("2", "0")), 1e-9);

        assertEquals(vertexAttrs.get(Pair.of("0", "ID")), DefaultAttribute.createAttribute("1"));
        assertEquals(
            vertexAttrs.get(Pair.of("0", "color")), DefaultAttribute.createAttribute("Red"));
        assertEquals(
            vertexAttrs.get(Pair.of("0", "city")), DefaultAttribute.createAttribute("Paris"));
        assertEquals(
            vertexAttrs.get(Pair.of("0", "weight")),
            new DefaultAttribute<>("100.0", AttributeType.DOUBLE));

        assertEquals(
            edgeAttrs.get(Pair.of(g.getEdge("0", "1"), "length")),
            new DefaultAttribute<>("333.0", AttributeType.DOUBLE));
    }

    @Test(expected = ImportException.class)
    public void testValidate()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <node id=\"1\" label=\"1\"/>" + NL 
            + "    <node id=\"2\" label=\"2\"/>" + NL
            + "    <node id=\"3\" label=\"3\"/>" + NL 
            + "    <edges>" + NL
            + "      <edge id=\"1\" source=\"2\" target=\"3\" />" + NL            
            + "      <edge id=\"0\" source=\"1\" target=\"2\" />" + NL
            + "      <edge id=\"1\" source=\"3\" target=\"1\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        new SimpleGEXFImporter<String, DefaultEdge>()
            .importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testIgnoringNested()
        throws ImportException
    {
        // @formatter:off
        String input = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
            + "      version=\"1.2\" "
            + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
            + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "  <graph defaultedgetype=\"undirected\">" + NL
            + "    <nodes>" + NL
            + "      <node id=\"1\" label=\"1\">" + NL
            + "        <nodes>" + NL
            + "          <node id=\"4\" label=\"4\"/>" + NL
            + "        </nodes>" + NL
            + "        <edges>" + NL
            + "          <edge id=\"3\" source=\"3\" target=\"4\" />" + NL            
            + "        </edges>" + NL
            + "      </node>"
            + "      <node id=\"2\" label=\"2\"/>" + NL
            + "      <node id=\"3\" label=\"3\"/>" + NL 
            + "    </nodes>" + NL
            + "    <edges>" + NL
            + "      <edge id=\"0\" source=\"2\" target=\"3\" />" + NL            
            + "      <edge id=\"1\" source=\"1\" target=\"2\" />" + NL
            + "      <edge id=\"2\" source=\"3\" target=\"1\" />" + NL
            + "    </edges>" + NL
            + "  </graph>" + NL 
            + "</gexf>";
        // @formatter:on

        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        SimpleGEXFImporter<String, DefaultEdge> simpleGEXFImporter =
            new SimpleGEXFImporter<String, DefaultEdge>();
        simpleGEXFImporter.setVertexFactory(id -> id);
        simpleGEXFImporter
            .importGraph(g, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
    }

}
