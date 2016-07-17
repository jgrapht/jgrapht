/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------------------
 * GmlImporterTest.java
 * ------------------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author: Dimitrios Michail
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-July-2016 : Initial revision (DM);
 *
 */
package org.jgrapht.ext;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.WeightedPseudograph;

import junit.framework.TestCase;

/**
 * @author Dimitrios Michail
 */
public class GraphMLImporterTest
    extends TestCase
{

    private static final String NL = System.getProperty("line.separator");

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("n1"));
        assertTrue(g.containsVertex("n2"));
        assertTrue(g.containsVertex("n3"));
        assertTrue(g.containsEdge("n1", "n2"));
        assertTrue(g.containsEdge("n2", "n3"));
        assertTrue(g.containsEdge("n3", "n1"));
        assertTrue(g.containsEdge("n1", "n1"));
    }

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            true,
            false);

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("n0"));
        assertTrue(g.containsVertex("n1"));
        assertTrue(g.containsVertex("n2"));
        assertTrue(g.containsEdge("n0", "n2"));
        assertTrue(g.containsEdge("n0", "n1"));
        assertTrue(g.containsEdge("n1", "n2"));
    }

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

        Map<String, Map<String, String>> vAttributes = new HashMap<String, Map<String, String>>();
        Map<DefaultEdge, Map<String, String>> eAttributes = new HashMap<DefaultEdge, Map<String, String>>();
        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false,
            vAttributes,
            eAttributes);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("n0"));
        assertTrue(g.containsVertex("n1"));
        assertTrue(g.containsVertex("n2"));
        assertTrue(g.containsEdge("n0", "n2"));
        assertTrue(g.containsEdge("n0", "n1"));
        assertTrue(g.containsEdge("n1", "n2"));
        assertEquals("green", vAttributes.get("n0").get("color"));
        assertEquals("yellow", vAttributes.get("n1").get("color"));
        assertEquals("blue", vAttributes.get("n2").get("color"));
        assertEquals(
            "2.0",
            eAttributes.get(g.getEdge("n0", "n2")).get("weight"));
        assertEquals(
            "1.0",
            eAttributes.get(g.getEdge("n0", "n1")).get("weight"));
        assertFalse(
            eAttributes.get(g.getEdge("n1", "n2")).containsKey("weight"));
    }

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

        Graph<String, DefaultWeightedEdge> g = readGraph(
            input,
            DefaultWeightedEdge.class,
            true,
            true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("n0"));
        assertTrue(g.containsVertex("n1"));
        assertTrue(g.containsVertex("n2"));
        assertTrue(g.containsEdge("n0", "n2"));
        assertTrue(g.containsEdge("n0", "n1"));
        assertTrue(g.containsEdge("n1", "n2"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("n0", "n2")));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("n0", "n1")));
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("n1", "n2")));
    }

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

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsVertex("n0"));
        assertTrue(g.containsVertex("n1"));
        assertTrue(g.containsVertex("n2"));
        assertTrue(g.containsVertex("n3"));
        assertTrue(g.containsEdge("n0", "n1"));
        assertTrue(g.containsEdge("n0", "n3"));
    }

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

    public void testExportImport()
        throws Exception
    {
        DirectedPseudograph<String, DefaultEdge> g1 = new DirectedPseudograph<String, DefaultEdge>(
            DefaultEdge.class);
        g1.addVertex("1");
        g1.addVertex("2");
        g1.addVertex("3");
        g1.addEdge("1", "2");
        g1.addEdge("2", "3");
        g1.addEdge("3", "3");

        StringWriter sw = new StringWriter();
        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        exporter.export(sw, g1);
        String output = sw.toString();

        Graph<String, DefaultEdge> g2 = readGraph(
            output,
            DefaultEdge.class,
            true,
            false);

        assertEquals(3, g2.vertexSet().size());
        assertEquals(3, g2.edgeSet().size());
        assertTrue(g2.containsEdge("1", "2"));
        assertTrue(g2.containsEdge("2", "3"));
        assertTrue(g2.containsEdge("3", "3"));
    }

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

        final Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(
            DefaultEdge.class);

        VertexProvider<String> vp = new VertexProvider<String>()
        {
            @Override
            public String buildVertex(
                String label,
                Map<String, String> attributes)
            {
                return label;
            }
        };

        EdgeProvider<String, DefaultEdge> ep = new EdgeProvider<String, DefaultEdge>()
        {

            @Override
            public DefaultEdge buildEdge(
                String from,
                String to,
                String label,
                Map<String, String> attributes)
            {
                return g.getEdgeFactory().createEdge(from, to);
            }

        };

        try {
            GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<String, DefaultEdge>(
                vp,
                ep);
            importer.read(new StringReader(input), g);
            fail("No!");
        } catch (Exception e) {
            // nothing
        }
    }

    public <E> Graph<String, E> readGraph(
        String input,
        Class<? extends E> edgeClass,
        boolean directed,
        boolean weighted)
        throws ImportException
    {
        return readGraph(
            input,
            edgeClass,
            directed,
            weighted,
            new HashMap<String, Map<String, String>>(),
            new HashMap<E, Map<String, String>>());
    }

    public <E> Graph<String, E> readGraph(
        String input,
        Graph<String, E> g,
        VertexProvider<String> vp,
        EdgeProvider<String, E> ep)
        throws ImportException
    {
        GraphMLImporter<String, E> importer = new GraphMLImporter<String, E>(
            vp,
            ep);
        importer.read(new StringReader(input), g);

        return g;
    }

    public <E> Graph<String, E> readGraph(
        String input,
        Class<? extends E> edgeClass,
        boolean directed,
        boolean weighted,
        Map<String, Map<String, String>> vertexAttributes,
        Map<E, Map<String, String>> edgeAttributes)
        throws ImportException
    {
        Graph<String, E> g;
        if (directed) {
            if (weighted) {
                g = new DirectedWeightedPseudograph<String, E>(edgeClass);
            } else {
                g = new DirectedPseudograph<String, E>(edgeClass);
            }
        } else {
            if (weighted) {
                g = new WeightedPseudograph<String, E>(edgeClass);
            } else {
                g = new Pseudograph<String, E>(edgeClass);
            }
        }

        VertexProvider<String> vp = new VertexProvider<String>()
        {
            @Override
            public String buildVertex(
                String label,
                Map<String, String> attributes)
            {
                vertexAttributes.put(label, attributes);
                return label;
            }
        };

        EdgeProvider<String, E> ep = new EdgeProvider<String, E>()
        {

            @Override
            public E buildEdge(
                String from,
                String to,
                String label,
                Map<String, String> attributes)
            {
                E e = g.getEdgeFactory().createEdge(from, to);
                edgeAttributes.put(e, attributes);
                return e;
            }

        };

        return readGraph(input, g, vp, ep);
    }

}
