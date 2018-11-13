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

import static org.junit.Assert.*;

/**
 * Tests for {@link GmlImporter}.
 * 
 * @author Dimitrios Michail
 */
public class GmlImporterTest
{

    @Test
    public void testUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testIgnoreWeightsUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    weight 2.0\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2.0\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3.3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
    }

    @Test
    public void testNoGraph()
        throws ImportException
    {
        // @formatter:off
        String input = "GRAPH [\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g1 = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(0, g1.vertexSet().size());
        assertEquals(0, g1.edgeSet().size());

        Graph<String, DefaultEdge> g2 =
            readGraph(input.toLowerCase(), DefaultEdge.class, false, false);

        assertEquals(0, g2.vertexSet().size());
        assertEquals(0, g2.edgeSet().size());
    }

    @Test
    public void testIgnore()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  ignore [\n"
                     + "     node [\n"
                     + "       id 5\n"
                     + "     ]"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "    label \"3\""
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  ignore [\n"
                     + "     edge [\n"
                     + "       source 5\n"
                     + "       target 1\n"
                     + "       label \"edge51\""
                     + "     ]"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "    label \"23\""                     
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]"
                     + "node [\n"
                     + "  id 6\n"
                     + "]\n"
                     ;
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testUndirectedUnweightedWrongOrder()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, false, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testDirectedPseudographUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 2\n"
                     + "  ]\n"                     
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(input, DefaultEdge.class, true, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(7, g.edgeSet().size());
    }

    @Test
    public void testDirectedWeighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    weight 2.0\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "    weight 3.0\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            readGraph(input, DefaultWeightedEdge.class, true, true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")), 1e-9);
    }

    @Test
    public void testDirectedWeightedWithComments()
        throws ImportException
    {
        // @formatter:off
            String input = "# A comment line\n" 
            		     + "graph [\n"
                         + "  comment \"Sample Graph\"\n"
                         + "  directed 1\n"
                         + "  node [\n"
                         + "    id 1\n"
                         + "  ]\n"
                         + "  node [\n"
                         + "    id 2\n"
                         + "  ]\n"
                         + "# Another comment line\n"
                         + "  node [\n"
                         + "    id 3\n"
                         + "  ]\n"
                         + "  edge [\n"
                         + "    source 1\n"
                         + "    target 2\n"
                         + "    weight 2.0\n"
                         + "  ]\n"
                         + "  edge [\n"
                         + "    source 3\n"
                         + "    target 1\n"
                         + "    weight 3.0\n"
                         + "  ]\n"
                         + "]";
            // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            readGraph(input, DefaultWeightedEdge.class, true, true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")), 1e-9);
    }

    @Test
    public void testDirectedWeightedSingleLine()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [ node [ id 1 ] node [ id 2 ] node [ id 3 ] " + 
                       "edge [ source 1 target 2 weight 2.0 ] " + 
                       "edge [ source 3 target 1 weight 3.0 ] ]"; 
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            readGraph(input, DefaultWeightedEdge.class, true, true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")), 1e-9);
    }

    @Test
    public void testParserError()
    {
        // @formatter:off
        String input = "graph [ [ node ] ]";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("Managed to parse wrong input");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testMissingVertices()
    {
        // @formatter:off
        String input = "graph [ edge [ source 1 target 2 ] ]";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("Node is missing?");
        } catch (ImportException e) {
        }
    }

    @Test
    public void testExportImport()
        throws ImportException,
        ExportException,
        UnsupportedEncodingException
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g1 =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g1.addVertex("1");
        g1.addVertex("2");
        g1.addVertex("3");
        g1.setEdgeWeight(g1.addEdge("1", "2"), 2.0);
        g1.setEdgeWeight(g1.addEdge("2", "3"), 3.0);
        g1.setEdgeWeight(g1.addEdge("3", "3"), 5.0);

        GmlExporter<String, DefaultWeightedEdge> exporter = new GmlExporter<>();
        exporter.setParameter(GmlExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g1, os);
        String output = new String(os.toByteArray(), "UTF-8");

        Graph<String, DefaultWeightedEdge> g2 =
            readGraph(output, DefaultWeightedEdge.class, true, true);

        assertEquals(3, g2.vertexSet().size());
        assertEquals(3, g2.edgeSet().size());
        assertTrue(g2.containsEdge("1", "2"));
        assertTrue(g2.containsEdge("2", "3"));
        assertTrue(g2.containsEdge("3", "3"));
        assertEquals(2.0, g2.getEdgeWeight(g2.getEdge("1", "2")), 1e-9);
        assertEquals(3.0, g2.getEdgeWeight(g2.getEdge("2", "3")), 1e-9);
        assertEquals(5.0, g2.getEdgeWeight(g2.getEdge("3", "3")), 1e-9);
    }

    @Test
    public void testNotSupportedGraph()
    {
        // @formatter:off
        String input = "graph [ node [ id 1 ] " + 
                       "edge [ source 1 target 1 ] ]"; 
        // @formatter:on

        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        VertexProvider<String> vp = (label, attributes) -> label;
        EdgeProvider<String, DefaultEdge> ep =
            (from, to, label, attributes) -> g.getEdgeSupplier().get();

        try {
            GmlImporter<String, DefaultEdge> importer = new GmlImporter<>(vp, ep);
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
        }

    }

    @Test(expected = ImportException.class)
    public void testNodeIdBadType()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id \"1\"\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on
        readGraph(input, DefaultEdge.class, false, false);
    }

    @Test(expected = ImportException.class)
    public void testEdgeSourceBadType()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source \"1\"\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on
        readGraph(input, DefaultEdge.class, false, false);
    }

    @Test(expected = ImportException.class)
    public void testEdgeTargetBadType()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target \"2\"\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on
        readGraph(input, DefaultEdge.class, false, false);
    }

    @Test(expected = ImportException.class)
    public void testEdgeWeightBadType()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    weight \"2.0\"\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on
        readGraph(input, DefaultEdge.class, false, false);
    }

    @Test
    public void testAttributesSupport()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    label \"Edge 1-2\""
                     + "    name \"Name 12\""
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "    label \"Edge 3-1\""
                     + "    name \"Name 31\""
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "    label \"Edge 2-3\""
                     + "    name \"Name 23\""
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "    label \"Node\t\t1\""
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "    label \"Node\t\t2\""
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "    label \"Node\t\t3\""
                     + "  ]\n"
                     + "  node [\n"
                     + "    label \"Node\t\t?\""
                     + "  ]\n"
                     + "  node [\n"
                     + "    label \"Node\t\t?\""
                     + "  ]\n"                     
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedWeightedPseudograph<>(DefaultEdge.class);

        VertexProvider<String> vp = (id, attributes) -> {
            assertNotNull(attributes);
            if (Integer.parseInt(id) >= 4) {
                assertEquals("Node\t\t?", attributes.get("label").getValue());
            } else {
                assertEquals("Node\t\t" + id, attributes.get("label").getValue());
            }
            return id;
        };
        EdgeProvider<String, DefaultEdge> ep = (from, to, label, attributes) -> {
            assertNotNull(attributes);
            assertEquals("Edge " + from + "-" + to, attributes.get("label").getValue());
            assertEquals("Name " + from + to, attributes.get("name").getValue());
            return g.getEdgeSupplier().get();
        };

        GmlImporter<String, DefaultEdge> importer = new GmlImporter<>(vp, ep);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    @Test
    public void testCustomNumberAttributesSupport()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    frequency 6\n"
                     + "    customweight 7.5\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "    frequency 2\n"
                     + "    customweight 1.2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "    frequency 3\n"
                     + "    customweight 2.1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    frequency 5\n"
                     + "    customweight 5.5\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedWeightedPseudograph<>(DefaultEdge.class);

        VertexProvider<String> vp = (id, attributes) -> {
            assertNotNull(attributes);
            if (Integer.parseInt(id) == 1) {
                assertEquals("2", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("1.2", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            } else if (Integer.parseInt(id) == 2) {
                assertEquals("3", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("2.1", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            } else {
                assertEquals("5", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("5.5", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            }
            return id;
        };
        EdgeProvider<String, DefaultEdge> ep = (from, to, label, attributes) -> {
            assertNotNull(attributes);
            assertEquals("6", attributes.get("frequency").getValue());
            assertEquals(AttributeType.INT, attributes.get("frequency").getType());
            assertEquals("7.5", attributes.get("customweight").getValue());
            assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            return g.getEdgeSupplier().get();
        };

        GmlImporter<String, DefaultEdge> importer = new GmlImporter<>(vp, ep);
        importer.importGraph(g, new StringReader(input));

        assertEquals(3, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
    }

    @Test
    public void testNestedStructure()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    frequency 6\n"
                     + "    customweight 7.5\n"
                     + "    points [ x 1.0 y 2.0 ]\n"
                     + "    deep [ one [ one 1 two 2 ] two [ one 1 two 2 ] ]\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "    frequency 2\n"
                     + "    customweight 1.2\n"
                     + "    deep [ one [ one 1.0 two 2.0 ] two [ one \"1\" two \"2\" ] ]\n"                     
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "    frequency 3\n"
                     + "    customweight 2.1\n"
                     + "    points [ x 1.0 y 2.0 z 3.0 ]\n"
                     + "    deep [ one [ one 1 two 2 ] two [ one 1 two 2 ] ]\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    frequency 5\n"
                     + "    customweight 5.5\n"
                     + "  ]\n"
                     + "  deepignored [\n"
                     + "    deep1 [ deep2 [ deep3 [ deep4 [ deep 5 ] ] ] ]\n"
                     + "  ]\n"
                     + "]\n"
                     + "deepignored [\n"
                     + "  deep1 [ deep2 [ deep3 [ deep4 [ deep 5 ] ] ] ]\n"
                     + "]\n";
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedWeightedPseudograph<>(DefaultEdge.class);

        VertexProvider<String> vp = (id, attributes) -> {
            assertNotNull(attributes);
            if (Integer.parseInt(id) == 1) {
                assertEquals("2", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("1.2", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
                assertEquals(AttributeType.UNKNOWN, attributes.get("deep").getType());
                assertEquals(
                    "[ one [ one 1.0 two 2.0 ] two [ one \"1\" two \"2\" ] ]",
                    attributes.get("deep").getValue());
            } else if (Integer.parseInt(id) == 2) {
                assertEquals("3", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("2.1", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
                assertEquals(AttributeType.UNKNOWN, attributes.get("points").getType());
                assertEquals("[ x 1.0 y 2.0 z 3.0 ]", attributes.get("points").getValue());
                assertEquals(AttributeType.UNKNOWN, attributes.get("deep").getType());
                assertEquals(
                    "[ one [ one 1 two 2 ] two [ one 1 two 2 ] ]",
                    attributes.get("deep").getValue());
            } else {
                assertEquals("5", attributes.get("frequency").getValue());
                assertEquals(AttributeType.INT, attributes.get("frequency").getType());
                assertEquals("5.5", attributes.get("customweight").getValue());
                assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            }
            return id;
        };
        EdgeProvider<String, DefaultEdge> ep = (from, to, label, attributes) -> {
            assertNotNull(attributes);
            assertEquals("6", attributes.get("frequency").getValue());
            assertEquals(AttributeType.INT, attributes.get("frequency").getType());
            assertEquals("7.5", attributes.get("customweight").getValue());
            assertEquals(AttributeType.DOUBLE, attributes.get("customweight").getType());
            assertEquals(AttributeType.UNKNOWN, attributes.get("points").getType());
            assertEquals("[ x 1.0 y 2.0 ]", attributes.get("points").getValue());
            assertEquals(AttributeType.UNKNOWN, attributes.get("deep").getType());
            assertEquals(
                "[ one [ one 1 two 2 ] two [ one 1 two 2 ] ]", attributes.get("deep").getValue());
            return g.getEdgeSupplier().get();
        };

        GmlImporter<String, DefaultEdge> importer = new GmlImporter<>(vp, ep);
        importer.importGraph(g, new StringReader(input));

        assertEquals(3, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
    }

    // ~ Private Methods ------------------------------------------------------

    private <E> Graph<String, E> readGraph(
        String input, Class<? extends E> edgeClass, boolean directed, boolean weighted)
        throws ImportException
    {
        Graph<String, E> g;
        if (directed) {
            if (weighted) {
                g = new DirectedWeightedPseudograph<>(edgeClass);
            } else {
                g = new DirectedPseudograph<>(edgeClass);
            }
        } else {
            if (weighted) {
                g = new WeightedPseudograph<>(edgeClass);
            } else {
                g = new Pseudograph<>(edgeClass);
            }
        }

        GmlImporter<String, E> importer =
            new GmlImporter<>((l, a) -> l, (f, t, l, a) -> g.getEdgeSupplier().get());
        importer.importGraph(g, new StringReader(input));

        return g;
    }

}
