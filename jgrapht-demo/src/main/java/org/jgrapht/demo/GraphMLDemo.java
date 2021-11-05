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
package org.jgrapht.demo;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.graphml.*;
import org.jgrapht.nio.graphml.GraphMLExporter.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * This class demonstrates exporting and importing a graph with custom vertex and edge attributes in
 * GraphML. Vertices of the graph have an attribute called "color" and a "name" attribute. Edges
 * have a "weight" attribute as well as a "name" attribute.
 *
 * The demo constructs a complete graph with random edge weights and exports it as GraphML. The
 * output is then re-imported into a second graph.
 */
public final class GraphMLDemo
{
    // number of vertices
    private static final int SIZE = 6;

    // random number generator
    private static final Random GENERATOR = new Random(17);

    /**
     * Color
     */
    enum Color
    {
        BLACK("black"),
        WHITE("white");

        private final String value;

        private Color(String value)
        {
            this.value = value;
        }

        public String toString()
        {
            return value;
        }

    }

    /**
     * A custom graph vertex.
     */
    static class CustomVertex
    {
        private String id;
        private Color color;

        public CustomVertex(String id)
        {
            this(id, null);
        }

        public CustomVertex(String id, Color color)
        {
            this.id = id;
            this.color = color;
        }

        @Override
        public int hashCode()
        {
            return (id == null) ? 0 : id.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomVertex other = (CustomVertex) obj;
            if (id == null) {
                return other.id == null;
            } else {
                return id.equals(other.id);
            }
        }

        public Color getColor()
        {
            return color;
        }

        public void setColor(Color color)
        {
            this.color = color;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(id);
            if (color != null) {
                sb.append(",").append(color);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    /**
     * A custom vertex supplier which gives each vertex a random color.
     */
    static class CustomVertexSupplier
        implements
        Supplier<CustomVertex>
    {

        private int id = 0;

        @Override
        public CustomVertex get()
        {
            return new CustomVertex(
                String.valueOf(id++), GENERATOR.nextBoolean() ? Color.BLACK : Color.WHITE);
        }

    }

    /**
     * Create exporter
     */
    private static GraphExporter<CustomVertex, DefaultWeightedEdge> createExporter()
    {

        /*
         * Create the exporter. The constructor parameter is a function which generates for each
         * vertex a unique identifier.
         */
        GraphMLExporter<CustomVertex, DefaultWeightedEdge> exporter =
            new GraphMLExporter<>(v -> v.id);

        /*
         * Set to export the internal edge weights
         */
        exporter.setExportEdgeWeights(true);

        /*
         * The exporter may need to generate for each vertex a set of attributes.
         */
        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> m = new HashMap<>();
            if (v.getColor() != null) {
                m.put("color", DefaultAttribute.createAttribute(v.getColor().toString()));
            }
            m.put("name", DefaultAttribute.createAttribute("node-" + v.id));
            return m;
        });

        /*
         * Set the edge id provider.
         *
         * The exporter needs to generate for each edge a unique identifier.
         */
        exporter.setEdgeIdProvider(new IntegerIdProvider<DefaultWeightedEdge>(0));

        /*
         * The exporter may need to generate for each edge a set of attributes.
         */
        exporter.setEdgeAttributeProvider(e -> {
            Map<String, Attribute> m = new HashMap<>();
            m.put("name", DefaultAttribute.createAttribute(e.toString()));
            return m;
        });

        /*
         * Register additional color attribute for vertices
         */
        exporter.registerAttribute("color", AttributeCategory.NODE, AttributeType.STRING);

        /*
         * Register additional name attribute for vertices and edges
         */
        exporter.registerAttribute("name", AttributeCategory.ALL, AttributeType.STRING);

        return exporter;
    }

    /**
     * Create the importer
     */
    private static GraphImporter<CustomVertex, DefaultWeightedEdge> createImporter()
    {
        /*
         * Create the graph importer.
         */
        GraphMLImporter<CustomVertex, DefaultWeightedEdge> importer = new GraphMLImporter<>();

        /*
         * Add vertex attribute consumer to read back vertex color from file.
         */
        importer.addVertexAttributeConsumer((p, attrValue) -> {
            CustomVertex v = p.getFirst();
            String attrName = p.getSecond();

            if (attrName.equals("color")) {
                String color = attrValue.getValue();
                switch (color) {
                case "black":
                    v.setColor(Color.BLACK);
                    break;
                case "white":
                    v.setColor(Color.WHITE);
                    break;
                }
            }
        });

        return importer;
    }

    /**
     * Main demo method
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {

        /*
         * Generate the complete graph. Vertices have random colors and edges have random edge
         * weights.
         */
        Graph<CustomVertex,
            DefaultWeightedEdge> graph1 = GraphTypeBuilder
                .directed().weighted(true).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(new CustomVertexSupplier())
                .edgeSupplier(SupplierUtil.createDefaultWeightedEdgeSupplier()).buildGraph();

        CompleteGraphGenerator<CustomVertex, DefaultWeightedEdge> completeGenerator =
            new CompleteGraphGenerator<>(SIZE);

        System.out.println("-- Generating complete graph");
        completeGenerator.generateGraph(graph1);

        /*
         * Assign random weights to the graph
         */
        for (var e : graph1.edgeSet()) {
            graph1.setEdgeWeight(e, GENERATOR.nextInt(100));
        }

        // now export and import back again
        System.out.println("-- Exporting graph as GraphML");
        GraphExporter<CustomVertex, DefaultWeightedEdge> exporter = createExporter();
        // export as string
        Writer writer = new StringWriter();
        exporter.exportGraph(graph1, writer);
        String graph1AsGraphML = writer.toString();

        // display
        System.out.println(graph1AsGraphML);

        // import it back
        System.out.println("-- Importing graph back from GraphML");
        Graph<CustomVertex,
            DefaultWeightedEdge> graph2 = GraphTypeBuilder
                .directed().weighted(true).allowingMultipleEdges(true).allowingSelfLoops(true)
                .vertexSupplier(new CustomVertexSupplier())
                .edgeSupplier(SupplierUtil.createDefaultWeightedEdgeSupplier()).buildGraph();

        GraphImporter<CustomVertex, DefaultWeightedEdge> importer = createImporter();
        importer.importGraph(graph2, new StringReader(graph1AsGraphML));

    }

}
