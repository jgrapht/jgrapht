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
/* --------------------
 * GraphMLExportDemo.java
 * --------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 * Contributor(s):   -
 *
 * Changes
 * -------
 * 26-July-2016 : Initial revision (DM); 
 *
 */
package org.jgrapht.demo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.VertexFactory;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.ext.GraphMLExporter.AttributeCategory;
import org.jgrapht.ext.GraphMLExporter.AttributeType;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * This class demonstrates exporting a graph with custom vertex and 
 * edge attributes as GraphML. Vertices of the graph have an attribute
 * called "color" and a "name" attribute. Edges have a "weight" attribute
 * as well as a "name" attribute.
 */
public final class GraphMLExportDemo
{
    // Number of vertices
    private static final int size = 6;

    private static Random generator = new Random(17);

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

    static class GraphVertex
    {
        private String id;
        private Color color;

        public GraphVertex(String id)
        {
            this(id, null);
        }

        public GraphVertex(String id, Color color)
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
            GraphVertex other = (GraphVertex) obj;
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
            return id;
        }
    }

    public static void main(String[] args)
    {
        /*
         * Generate complete graph.
         * 
         * Vertices have random colors and edges have random edge weights.
         */
        DirectedPseudograph<GraphVertex, DefaultWeightedEdge> g = new DirectedPseudograph<>(
            DefaultWeightedEdge.class);
        CompleteGraphGenerator<GraphVertex, DefaultWeightedEdge> completeGenerator = new CompleteGraphGenerator<>(
            size);
        VertexFactory<GraphVertex> vFactory = new VertexFactory<GraphVertex>()
        {
            private int id = 0;

            @Override
            public GraphVertex createVertex()
            {
                GraphVertex v = new GraphVertex(String.valueOf(id++));
                if (generator.nextBoolean()) {
                    v.setColor(Color.BLACK);
                } else {
                    v.setColor(Color.WHITE);
                }
                return v;
            }

        };
        completeGenerator.generateGraph(g, vFactory, null);

        // assign random weights
        for (DefaultWeightedEdge e : g.edgeSet()) {
            g.setEdgeWeight(e, generator.nextInt(100));
        }

        // create GraphML exporter
        GraphMLExporter<GraphVertex, DefaultWeightedEdge> exporter = new GraphMLExporter<>(
            new VertexNameProvider<GraphVertex>()
            {
                @Override
                public String getVertexName(GraphVertex v)
                {
                    return v.id;
                }
            },
            null,
            new IntegerEdgeNameProvider<>(),
            null);

        // set to export the internal edge weights
        exporter.setExportEdgeWeights(true);

        // register additional color attribute for vertices
        exporter.registerAttribute(
            "color",
            AttributeCategory.NODE,
            AttributeType.STRING);

        // register additional name attribute for vertices and edges
        exporter.registerAttribute(
            "name",
            AttributeCategory.ALL,
            AttributeType.STRING);

        // create provider of vertex attributes
        ComponentAttributeProvider<GraphVertex> vertexAttributeProvider = new ComponentAttributeProvider<GraphVertex>()
        {
            @Override
            public Map<String, String> getComponentAttributes(GraphVertex v)
            {
                Map<String, String> m = new HashMap<String, String>();
                if (v.getColor() != null) {
                    m.put("color", v.getColor().toString());
                }
                m.put("name", "node-" + v.id);
                return m;
            }
        };
        exporter.setVertexAttributeProvider(vertexAttributeProvider);

        // create provider of edge attributes
        ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = new ComponentAttributeProvider<DefaultWeightedEdge>()
        {
            @Override
            public Map<String, String> getComponentAttributes(
                DefaultWeightedEdge e)
            {
                Map<String, String> m = new HashMap<String, String>();
                m.put("name", e.toString());
                return m;
            }
        };
        exporter.setEdgeAttributeProvider(edgeAttributeProvider);

        // export
        try {
            Writer writer = new BufferedWriter(
                new OutputStreamWriter(System.out));
            exporter.exportGraph(g, writer);
            writer.flush();
        } catch (ExportException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }

    }

}
