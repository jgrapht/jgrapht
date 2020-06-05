/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.gexf.GEXFExporter.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.xmlunit.builder.*;
import org.xmlunit.diff.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertFalse;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class GEXFExporterTest
{
    private static final String NL = System.getProperty("line.separator");

    // ~ Methods
    // ----------------------------------------------------------------

    @Test
    public void testDirected()
        throws UnsupportedEncodingException
    {
        String output =
        // @formatter:off
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
				+ "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
				+ "      version=\"1.2\" "
				+ "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
				+ "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
				+ "<meta>" + NL
				+ "  <creator>The JGraphT Library</creator>" + NL
				+ "</meta>" + NL
				+ "<graph defaultedgetype=\"directed\">" + NL 
				+ "  <attributes class=\"node\">" + NL
				+ "    <attribute id=\"0\" title=\"color\" type=\"string\"/>" + NL
				+ "    <attribute id=\"1\" title=\"city\" type=\"string\"/>" + NL
				+ "    <attribute id=\"2\" title=\"hobby\" type=\"string\">" + NL
				+ "      <options>ski|dance|photo</options>"
                + "    </attribute>" + NL
                + "  </attributes>" + NL				
                + "  <attributes class=\"edge\">" + NL
                + "    <attribute id=\"0\" title=\"length\" type=\"double\"/>" + NL
                + "  </attributes>" + NL
                + "  <nodes>" + NL
				+ "    <node id=\"0\" label=\"0\">" + NL 
				+ "      <attvalues>" + NL
                + "        <attvalue for=\"0\" value=\"Red\"/>" + NL
                + "        <attvalue for=\"1\" value=\"Paris\"/>" + NL                
                + "      </attvalues>" + NL				
				+ "    </node>" + NL
				+ "    <node id=\"1\" label=\"1\"/>" + NL
				+ "    <node id=\"2\" label=\"2\"/>" + NL 
				+ "  </nodes>" + NL
                + "  <edges>" + NL				
				+ "    <edge id=\"0\" source=\"0\" target=\"1\" type=\"directed\" weight=\"1.0\" label=\"Edge from node 1 to node 2\">" + NL
                + "      <attvalues>" + NL
                + "        <attvalue for=\"0\" value=\"100.0\"/>" + NL
                + "      </attvalues>" + NL             				
                + "    </edge>" + NL
				+ "    <edge id=\"1\" source=\"2\" target=\"0\" type=\"directed\" weight=\"1.0\"/>" + NL 
                + "  </edges>" + NL				
				+ "</graph>" + NL 
				+ "</gexf>" + NL;
		    // @formatter:on

        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createStringSupplier()).allowingMultipleEdges(true)
                .allowingSelfLoops(true).buildGraph();

        graph.addVertex("v1");
        graph.addVertex("v2");
        DefaultEdge e_1_2 = graph.addEdge("v1", "v2");
        graph.addVertex("v3");
        graph.addEdge("v3", "v1");

        GEXFExporter<String, DefaultEdge> exporter = new GEXFExporter<>();
        exporter.setParameter(GEXFExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        exporter.setParameter(GEXFExporter.Parameter.EXPORT_EDGE_TYPES, true);
        exporter.setParameter(GEXFExporter.Parameter.EXPORT_EDGE_LABELS, true);

        exporter.registerAttribute("color", AttributeCategory.NODE, GEXFAttributeType.STRING, null);
        exporter.registerAttribute("city", AttributeCategory.NODE, GEXFAttributeType.STRING, null);
        exporter
            .registerAttribute(
                "hobby", AttributeCategory.NODE, GEXFAttributeType.STRING, null, "ski|dance|photo");

        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> map = new HashMap<String, Attribute>();
            if ("v1".equals(v)) {
                map.put("color", DefaultAttribute.createAttribute("Red"));
                map.put("city", DefaultAttribute.createAttribute("Paris"));
            }
            return map;
        });

        exporter
            .registerAttribute("length", AttributeCategory.EDGE, GEXFAttributeType.DOUBLE, null);

        exporter.setEdgeAttributeProvider(e -> {
            Map<String, Attribute> map = new HashMap<String, Attribute>();
            if (e == e_1_2) {
                map.put("label", DefaultAttribute.createAttribute("Edge from node 1 to node 2"));
                map.put("length", DefaultAttribute.createAttribute("100.0"));
            }
            return map;
        });

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testUndirected()
        throws UnsupportedEncodingException
    {
        String output =
        // @formatter:off
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" "
                + "      version=\"1.2\" "
                + "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\" "
                + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + NL
                + "<meta>" + NL
                + "  <creator>The JGraphT Library</creator>" + NL
                + "  <description>Test</description>" + NL
                + "</meta>" + NL                
                + "<graph defaultedgetype=\"undirected\">" + NL 
                + "  <attributes class=\"node\">" + NL
                + "    <attribute id=\"0\" title=\"color\" type=\"string\"/>" + NL
                + "    <attribute id=\"1\" title=\"city\" type=\"string\"/>" + NL
                + "  </attributes>" + NL                
                + "  <attributes class=\"edge\">" + NL
                + "    <attribute id=\"0\" title=\"length\" type=\"double\"/>" + NL
                + "  </attributes>" + NL
                + "  <nodes>" + NL
                + "    <node id=\"0\" label=\"0\">" + NL 
                + "      <attvalues>" + NL
                + "        <attvalue for=\"0\" value=\"Red\"/>" + NL
                + "        <attvalue for=\"1\" value=\"Paris\"/>" + NL                
                + "      </attvalues>" + NL             
                + "    </node>" + NL
                + "    <node id=\"1\" label=\"1\"/>" + NL
                + "    <node id=\"2\" label=\"2\"/>" + NL 
                + "  </nodes>" + NL
                + "  <edges>" + NL              
                + "    <edge id=\"0\" source=\"0\" target=\"1\" type=\"undirected\" weight=\"1.0\">" + NL
                + "      <attvalues>" + NL
                + "        <attvalue for=\"0\" value=\"100.0\"/>" + NL
                + "      </attvalues>" + NL                             
                + "    </edge>" + NL
                + "    <edge id=\"1\" source=\"2\" target=\"0\" type=\"undirected\" weight=\"13.5\">" + NL 
                + "      <attvalues>" + NL
                + "        <attvalue for=\"0\" value=\"30.0\"/>" + NL
                + "      </attvalues>" + NL                             
                + "    </edge>" + NL                
                + "  </edges>" + NL             
                + "</graph>" + NL 
                + "</gexf>" + NL;
            // @formatter:on

        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().weighted(true).edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createStringSupplier()).allowingMultipleEdges(true)
                .allowingSelfLoops(true).buildGraph();

        graph.addVertex("v1");
        graph.addVertex("v2");
        DefaultEdge e_1_2 = graph.addEdge("v1", "v2");
        graph.addVertex("v3");
        DefaultEdge e_3_1 = graph.addEdge("v3", "v1");
        graph.setEdgeWeight(e_3_1, 13.5d);

        GEXFExporter<String, DefaultEdge> exporter = new GEXFExporter<>();
        exporter.setParameter(GEXFExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        exporter.setParameter(GEXFExporter.Parameter.EXPORT_EDGE_TYPES, true);
        exporter.setDescription("Test");

        exporter.registerAttribute("color", AttributeCategory.NODE, GEXFAttributeType.STRING, null);
        exporter.registerAttribute("city", AttributeCategory.NODE, GEXFAttributeType.STRING, null);

        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> map = new HashMap<String, Attribute>();
            if ("v1".equals(v)) {
                map.put("color", DefaultAttribute.createAttribute("Red"));
                map.put("city", DefaultAttribute.createAttribute("Paris"));
            }
            return map;
        });

        exporter
            .registerAttribute("length", AttributeCategory.EDGE, GEXFAttributeType.DOUBLE, null);

        exporter.setEdgeAttributeProvider(e -> {
            Map<String, Attribute> map = new HashMap<String, Attribute>();
            if (e == e_1_2) {
                map.put("label", DefaultAttribute.createAttribute("Edge from node 1 to node 2"));
                map.put("length", DefaultAttribute.createAttribute("100.0"));
            }
            if (e == e_3_1) {
                map.put("length", DefaultAttribute.createAttribute("30.0"));
            }
            return map;
        });

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
        String res = new String(os.toByteArray(), "UTF-8");

        Diff diff = DiffBuilder
            .compare(res).withTest(output).ignoreWhitespace().checkForIdentical().build();
        assertFalse("XML identical " + diff.toString(), diff.hasDifferences());
    }

}
