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
 * GraphMLExporterTest.java
 * ------------------------------
 * (C) Copyright 2006-2016, by Trevor Harmon and Contributors.
 *
 * Original Author:  Trevor Harmon
 * Contributors: Dimitrios Michail
 *
 */
package org.jgrapht.ext;

import java.io.*;

import junit.framework.*;

import org.custommonkey.xmlunit.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * @author Trevor Harmon
 * @author Dimitrios Michail
 */
public class GraphMLExporterTest
    extends TestCase
{
    // ~ Static fields/initializers
    // ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // ~ Methods
    // ----------------------------------------------------------------

    public void testUndirected()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<graph edgedefault=\"undirected\">" + NL
            + "<node id=\"1\"/>" + NL
            + "<node id=\"2\"/>" + NL
            + "<node id=\"3\"/>" + NL
            + "<edge id=\"1\" source=\"1\" target=\"2\"/>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        StringWriter w = new StringWriter();
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

    public void testUndirectedWeighted()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<key id=\"edge_weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
            + "<default>1.0</default>" + NL
            + "</key>" + NL
            + "<graph edgedefault=\"undirected\">" + NL
            + "<node id=\"1\"/>" + NL
            + "<node id=\"2\"/>" + NL
            + "<node id=\"3\"/>" + NL
            + "<edge id=\"1\" source=\"1\" target=\"2\"/>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        StringWriter w = new StringWriter();
        exporter.setExportEdgeWeights(true);
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

    public void testDirected()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<graph edgedefault=\"directed\">" + NL
            + "<node id=\"1\"/>" + NL
            + "<node id=\"2\"/>" + NL
            + "<node id=\"3\"/>" + NL
            + "<edge id=\"1\" source=\"1\" target=\"2\"/>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        DirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        StringWriter w = new StringWriter();
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

    public void testUndirectedUnweightedWithWeights()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<key id=\"edge_weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
            + "<default>1.0</default>" + NL
            + "</key>" + NL
            + "<graph edgedefault=\"undirected\">" + NL
            + "<node id=\"1\"/>" + NL
            + "<node id=\"2\"/>" + NL
            + "<node id=\"3\"/>" + NL
            + "<edge id=\"1\" source=\"1\" target=\"2\"/>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(
            DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<String, DefaultEdge>();
        StringWriter w = new StringWriter();
        exporter.setExportEdgeWeights(true);
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

    public void testUndirectedWeightedWithWeights()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<key id=\"edge_weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
            + "<default>1.0</default>" + NL
            + "</key>" + NL
            + "<graph edgedefault=\"undirected\">" + NL
            + "<node id=\"1\"/>" + NL
            + "<node id=\"2\"/>" + NL
            + "<node id=\"3\"/>" + NL
            + "<edge id=\"1\" source=\"1\" target=\"2\">" + NL
            + "<data key=\"edge_weight\">3.0</data>" + NL
            + "</edge>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<String, DefaultWeightedEdge>(
            DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<String, DefaultWeightedEdge>();
        StringWriter w = new StringWriter();
        exporter.setExportEdgeWeights(true);
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

    public void testUndirectedWeightedWithWeightsAndLabels()
        throws Exception
    {
        String output =
            // @formatter:off
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "  
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "  
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "  
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
            + "<key id=\"vertex_label\" for=\"node\" attr.name=\"Vertex Label\" attr.type=\"string\"/>" + NL
            + "<key id=\"edge_label\" for=\"edge\" attr.name=\"Edge Label\" attr.type=\"string\"/>" + NL
            + "<key id=\"edge_weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
            + "<default>1.0</default>" + NL
            + "</key>" + NL
            + "<graph edgedefault=\"undirected\">" + NL
            + "<node id=\"1\">" + NL
            + "<data key=\"vertex_label\">v1</data>" + NL
            + "</node>" + NL
            + "<node id=\"2\">" + NL
            + "<data key=\"vertex_label\">v2</data>" + NL
            + "</node>" + NL            
            + "<node id=\"3\">" + NL
            + "<data key=\"vertex_label\">v3</data>" + NL
            + "</node>" + NL            
            + "<edge id=\"1\" source=\"1\" target=\"2\">" + NL
            + "<data key=\"edge_label\">(v1 : v2)</data>" + NL
            + "<data key=\"edge_weight\">3.0</data>" + NL
            + "</edge>" + NL
            + "<edge id=\"2\" source=\"3\" target=\"1\">" + NL
            + "<data key=\"edge_label\">(v3 : v1)</data>" + NL            
            + "<data key=\"edge_weight\">15.0</data>" + NL            
            + "</edge>" + NL
            + "</graph>" + NL
            + "</graphml>" + NL;
            // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<String, DefaultWeightedEdge>(
            DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);
        g.setEdgeWeight(g.getEdge(V3, V1), 15.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<String, DefaultWeightedEdge>(
            new IntegerNameProvider<String>(),
            new VertexNameProvider<String>()
            {
                @Override
                public String getVertexName(String vertex)
                {
                    return vertex;
                }
            },
            new IntegerEdgeNameProvider<DefaultWeightedEdge>(),
            new EdgeNameProvider<DefaultWeightedEdge>()
            {
                @Override
                public String getEdgeName(DefaultWeightedEdge edge)
                {
                    return edge.toString();
                }

            });
        StringWriter w = new StringWriter();
        exporter.setExportEdgeWeights(true);
        exporter.export(w, g);

        XMLAssert.assertXMLEqual(output, w.toString());
    }

}

// End GraphMLExporterTest.java
