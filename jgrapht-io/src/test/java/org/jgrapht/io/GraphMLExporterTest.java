/*
 * (C) Copyright 2006-2017, by Trevor Harmon, Dimitrios Michail and Contributors.
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
import org.jgrapht.io.GraphMLExporter.*;
import org.junit.*;
import org.xmlunit.builder.*;
import org.xmlunit.diff.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author Trevor Harmon
 * @author Dimitrios Michail
 */
public class GraphMLExporterTest
{
    // ~ Static fields/initializers
    // ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // ~ Methods
    // ----------------------------------------------------------------

    @Test
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

        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
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
				+ "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
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

        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
        exporter.setExportEdgeWeights(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
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

        Graph<String, DefaultEdge> g =
            new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
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
				+ "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
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

        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
        exporter.setExportEdgeWeights(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
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
				+ "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
				+ "<default>1.0</default>" + NL 
				+ "</key>" + NL 
				+ "<graph edgedefault=\"undirected\">" + NL
				+ "<node id=\"1\"/>" + NL 
				+ "<node id=\"2\"/>" + NL + "<node id=\"3\"/>" + NL
				+ "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
				+ "<data key=\"edge_weight_key\">3.0</data>" + NL
				+ "</edge>" + NL 
				+ "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL 
				+ "</graph>" + NL
				+ "</graphml>" + NL;
		// @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<>();
        exporter.setExportEdgeWeights(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testUndirectedWeightedWithCustomNameWeights()
        throws Exception
    {
        String output =
        // @formatter:off
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
                + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
                + "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"value\" attr.type=\"double\">" + NL
                + "<default>1.0</default>" + NL 
                + "</key>" + NL 
                + "<graph edgedefault=\"undirected\">" + NL
                + "<node id=\"1\"/>" + NL 
                + "<node id=\"2\"/>" + NL 
                + "<node id=\"3\"/>" + NL
                + "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
                + "<data key=\"edge_weight_key\">3.0</data>" + NL
                + "</edge>" + NL 
                + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL 
                + "</graph>" + NL
                + "</graphml>" + NL;
        // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<>();
        exporter.setExportEdgeWeights(true);
        exporter.setEdgeWeightAttributeName("value");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testNoRegisterWeightAttribute()
        throws Exception
    {
        try {
            GraphMLExporter<String, DefaultWeightedEdge> exporter =
                new GraphMLExporter<String, DefaultWeightedEdge>();
            exporter.registerAttribute("weight", AttributeCategory.ALL, AttributeType.STRING);
            fail("Registered reserved attribute");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testRegisterWeightAttribute()
        throws Exception
    {
        try {
            GraphMLExporter<String, DefaultWeightedEdge> exporter =
                new GraphMLExporter<String, DefaultWeightedEdge>();
            exporter.setEdgeWeightAttributeName("anothername");
            exporter.registerAttribute("weight", AttributeCategory.ALL, AttributeType.STRING);
        } catch (IllegalArgumentException e) {
            fail("No!");
        }
    }

    @Test
    public void testNoAlreadyRegisteredAttributeAsWeightName()
        throws Exception
    {
        try {
            GraphMLExporter<String, DefaultWeightedEdge> exporter =
                new GraphMLExporter<String, DefaultWeightedEdge>();
            exporter.registerAttribute("length", AttributeCategory.EDGE, AttributeType.STRING);
            exporter.setEdgeWeightAttributeName("length");
            fail("Registered reserved attribute");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
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
				+ "<key id=\"vertex_label_key\" for=\"node\" attr.name=\"VertexLabel\" attr.type=\"string\"/>" + NL
				+ "<key id=\"edge_label_key\" for=\"edge\" attr.name=\"EdgeLabel\" attr.type=\"string\"/>" + NL
				+ "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
				+ "<default>1.0</default>" + NL 
				+ "</key>" + NL 
				+ "<graph edgedefault=\"undirected\">" + NL
				+ "<node id=\"1\">" + NL 
				+ "<data key=\"vertex_label_key\">v1</data>" + NL 
				+ "</node>" + NL
				+ "<node id=\"2\">" + NL 
				+ "<data key=\"vertex_label_key\">v2</data>" + NL 
				+ "</node>" + NL
				+ "<node id=\"3\">" + NL 
				+ "<data key=\"vertex_label_key\">v3</data>" + NL 
				+ "</node>" + NL
				+ "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
				+ "<data key=\"edge_label_key\">(v1 : v2)</data>" + NL 
				+ "<data key=\"edge_weight_key\">3.0</data>" + NL 
				+ "</edge>" + NL
				+ "<edge id=\"2\" source=\"3\" target=\"1\">" + NL 
				+ "<data key=\"edge_label_key\">(v3 : v1)</data>"+ NL 
				+ "<data key=\"edge_weight_key\">15.0</data>" + NL 
				+ "</edge>" + NL 
				+ "</graph>" + NL
				+ "</graphml>" + NL;
		    // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);
        g.setEdgeWeight(g.getEdge(V3, V1), 15.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<>(
            new IntegerComponentNameProvider<String>(), new ComponentNameProvider<String>()
            {
                @Override
                public String getName(String vertex)
                {
                    return vertex;
                }
            }, new IntegerComponentNameProvider<DefaultWeightedEdge>(),
            new ComponentNameProvider<DefaultWeightedEdge>()
            {
                @Override
                public String getName(DefaultWeightedEdge edge)
                {
                    return edge.toString();
                }

            });
        exporter.setExportEdgeWeights(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testUndirectedWeightedWithWeightsAndLabelsAndCustomNames()
        throws Exception
    {
        String output =
        // @formatter:off
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
                + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
                + "<key id=\"vertex_label_key\" for=\"node\" attr.name=\"custom_vertex_label\" attr.type=\"string\"/>" + NL
                + "<key id=\"edge_label_key\" for=\"edge\" attr.name=\"custom_edge_label\" attr.type=\"string\"/>" + NL
                + "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
                + "<default>1.0</default>" + NL 
                + "</key>" + NL 
                + "<graph edgedefault=\"undirected\">" + NL
                + "<node id=\"1\">" + NL 
                + "<data key=\"vertex_label_key\">myvertex-v1</data>" + NL 
                + "</node>" + NL
                + "<node id=\"2\">" + NL 
                + "<data key=\"vertex_label_key\">myvertex-v2</data>" + NL 
                + "</node>" + NL
                + "<node id=\"3\">" + NL 
                + "<data key=\"vertex_label_key\">myvertex-v3</data>" + NL 
                + "</node>" + NL
                + "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
                + "<data key=\"edge_label_key\">myedge-(v1 : v2)</data>" + NL 
                + "<data key=\"edge_weight_key\">3.0</data>" + NL 
                + "</edge>" + NL
                + "<edge id=\"2\" source=\"3\" target=\"1\">" + NL 
                + "<data key=\"edge_label_key\">myedge-(v3 : v1)</data>"+ NL 
                + "<data key=\"edge_weight_key\">15.0</data>" + NL 
                + "</edge>" + NL 
                + "</graph>" + NL
                + "</graphml>" + NL;
            // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);
        g.setEdgeWeight(g.getEdge(V3, V1), 15.0);

        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<>();
        exporter.setVertexLabelProvider(new ComponentNameProvider<String>()
        {
            @Override
            public String getName(String vertex)
            {
                return "myvertex-" + vertex;
            }
        });
        exporter.setVertexLabelAttributeName("custom_vertex_label");
        exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultWeightedEdge>()
        {
            @Override
            public String getName(DefaultWeightedEdge edge)
            {
                return "myedge-" + edge.toString();
            }

        });
        exporter.setEdgeLabelAttributeName("custom_edge_label");
        exporter.setExportEdgeWeights(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testUndirectedWeightedWithWeightsAndColor()
        throws Exception
    {
        String output =
        // @formatter:off
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
				+ "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
				+ "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
				+ "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
				+ "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
				+ "<default>1.0</default>" + NL 
				+ "</key>" + NL
				+ "<key id=\"key0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL
				+ "<default>yellow</default>" + NL 
                + "</key>" + NL
                + "<key id=\"key1\" for=\"all\" attr.name=\"name\" attr.type=\"string\">" + NL
                + "<default>johndoe</default>" + NL 
                + "</key>" + NL                        
				+ "<graph edgedefault=\"undirected\">" + NL
				+ "<node id=\"1\">" + NL
				+ "<data key=\"key1\">V1</data>" + NL
				+ "</node>" + NL
				+ "<node id=\"2\">" + NL
				+ "<data key=\"key0\">red</data>" + NL
				+ "<data key=\"key1\">V2</data>" + NL
				+ "</node>" + NL
				+ "<node id=\"3\">" + NL
				+ "<data key=\"key1\">V3</data>" + NL
				+ "</node>" + NL
				+ "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
				+ "<data key=\"edge_weight_key\">3.0</data>" + NL
				+ "<data key=\"key1\">e12</data>" + NL
				+ "</edge>" + NL 
				+ "<edge id=\"2\" source=\"3\" target=\"1\">" + NL
				+ "<data key=\"key1\">e31</data>" + NL
                + "</edge>" + NL
				+ "</graph>" + NL
				+ "</graphml>" + NL;
		    // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);

        ComponentAttributeProvider<String> vertexAttributeProvider =
            new ComponentAttributeProvider<String>()
            {
                @Override
                public Map<String, Attribute> getComponentAttributes(String v)
                {
                    Map<String, Attribute> map = new LinkedHashMap<>();
                    switch (v) {
                    case V1:
                        map.put("color", DefaultAttribute.createAttribute("yellow"));
                        map.put("name", DefaultAttribute.createAttribute("V1"));
                        break;
                    case V2:
                        map.put("color", DefaultAttribute.createAttribute("red"));
                        map.put("name", DefaultAttribute.createAttribute("V2"));
                        break;
                    case V3:
                        map.put("name", DefaultAttribute.createAttribute("V3"));
                        break;
                    default:
                        break;
                    }
                    return map;
                }
            };

        ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider =
            new ComponentAttributeProvider<DefaultWeightedEdge>()
            {
                @Override
                public Map<String, Attribute> getComponentAttributes(DefaultWeightedEdge e)
                {
                    Map<String, Attribute> map = new LinkedHashMap<>();
                    if (e.equals(g.getEdge(V1, V2))) {
                        map.put("color", DefaultAttribute.createAttribute("what?"));
                        map.put("name", DefaultAttribute.createAttribute("e12"));
                    } else if (e.equals(g.getEdge(V3, V1))) {
                        map.put("color", DefaultAttribute.createAttribute("I have no color!"));
                        map.put("name", DefaultAttribute.createAttribute("e31"));
                    }
                    return map;
                }
            };

        GraphMLExporter<String,
            DefaultWeightedEdge> exporter = new GraphMLExporter<>(
                new IntegerComponentNameProvider<>(), null, vertexAttributeProvider,
                new IntegerComponentNameProvider<>(), null, edgeAttributeProvider);
        exporter.setExportEdgeWeights(true);
        exporter.registerAttribute(
            "color", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING, "yellow");
        exporter.registerAttribute(
            "name", GraphMLExporter.AttributeCategory.ALL, AttributeType.STRING, "johndoe");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testUndirectedWeightedWithNullComponentProvider()
        throws Exception
    {
        String output =
        // @formatter:off
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
                + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
                + "<key id=\"edge_weight_key\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\">" + NL
                + "<default>1.0</default>" + NL 
                + "</key>" + NL
                + "<key id=\"key0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" + NL
                + "<default>yellow</default>" + NL 
                + "</key>" + NL
                + "<key id=\"key1\" for=\"all\" attr.name=\"name\" attr.type=\"string\">" + NL
                + "<default>johndoe</default>" + NL 
                + "</key>" + NL                        
                + "<graph edgedefault=\"undirected\">" + NL
                + "<node id=\"1\"/>" + NL
                + "<node id=\"2\"/>" + NL
                + "<node id=\"3\"/>" + NL
                + "<edge id=\"1\" source=\"1\" target=\"2\">" + NL 
                + "<data key=\"edge_weight_key\">3.0</data>" + NL
                + "</edge>" + NL 
                + "<edge id=\"2\" source=\"3\" target=\"1\"/>" + NL
                + "</graph>" + NL
                + "</graphml>" + NL;
            // @formatter:on

        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.setEdgeWeight(g.getEdge(V1, V2), 3.0);

        GraphMLExporter<String,
            DefaultWeightedEdge> exporter = new GraphMLExporter<>(
                new IntegerComponentNameProvider<>(), null, v -> null,
                new IntegerComponentNameProvider<>(), null, e -> null);
        exporter.setExportEdgeWeights(true);
        exporter.registerAttribute(
            "color", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING, "yellow");
        exporter.registerAttribute(
            "name", GraphMLExporter.AttributeCategory.ALL, AttributeType.STRING, "johndoe");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

}
