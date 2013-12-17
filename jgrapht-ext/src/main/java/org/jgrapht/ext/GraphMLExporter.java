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
/* ------------------
 * GraphMLExporter.java
 * ------------------
 * (C) Copyright 2006, by Trevor Harmon.
 *
 * Original Author:  Trevor Harmon <trevor@vocaro.com>
 *
 */
package org.jgrapht.ext;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.jgrapht.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * Exports a graph into a GraphML file.
 *
 * <p>For a description of the format see <a
 * href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/GraphML</a>.</p>
 *
 * @author Trevor Harmon
 */
public class GraphMLExporter<V, E>
{
    

    private VertexNameProvider<V> vertexIDProvider;
    private VertexNameProvider<V> vertexLabelProvider;
    private EdgeNameProvider<E> edgeIDProvider;
    private EdgeNameProvider<E> edgeLabelProvider;

    

    /**
     * Constructs a new GraphMLExporter object with integer name providers for
     * the vertex and edge IDs and null providers for the vertex and edge
     * labels.
     */
    public GraphMLExporter()
    {
        this(
            new IntegerNameProvider<V>(),
            null,
            new IntegerEdgeNameProvider<E>(),
            null);
    }

    /**
     * Constructs a new GraphMLExporter object with the given ID and label
     * providers.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     * labels will not be written to the file.
     * @param edgeIDProvider for generating vertex IDs. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     * will not be written to the file.
     */
    public GraphMLExporter(
        VertexNameProvider<V> vertexIDProvider,
        VertexNameProvider<V> vertexLabelProvider,
        EdgeNameProvider<E> edgeIDProvider,
        EdgeNameProvider<E> edgeLabelProvider)
    {
        this.vertexIDProvider = vertexIDProvider;
        this.vertexLabelProvider = vertexLabelProvider;
        this.edgeIDProvider = edgeIDProvider;
        this.edgeLabelProvider = edgeLabelProvider;
    }

    

    /**
     * Exports a graph into a plain text file in GraphML format.
     *
     * @param writer the writer to which the graph to be exported
     * @param g the graph to be exported
     */
    public void export(Writer writer, Graph<V, E> g)
        throws SAXException, TransformerConfigurationException
    {
        // Prepare an XML file to receive the GraphML data
        PrintWriter out = new PrintWriter(writer);
        StreamResult streamResult = new StreamResult(out);
        SAXTransformerFactory factory =
            (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler handler = factory.newTransformerHandler();
        Transformer serializer = handler.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        handler.setResult(streamResult);
        handler.startDocument();
        AttributesImpl attr = new AttributesImpl();

        // <graphml>
        handler.startPrefixMapping(
            "xsi",
            "http://www.w3.org/2001/XMLSchema-instance");

        // FIXME: Is this the proper way to add this attribute?
        attr.addAttribute(
            "",
            "",
            "xsi:schemaLocation",
            "CDATA",
            "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
        handler.startElement(
            "http://graphml.graphdrawing.org/xmlns",
            "",
            "graphml",
            attr);
        handler.endPrefixMapping("xsi");

        if (vertexLabelProvider != null) {
            // <key> for vertex label attribute
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", "vertex_label");
            attr.addAttribute("", "", "for", "CDATA", "node");
            attr.addAttribute("", "", "attr.name", "CDATA", "Vertex Label");
            attr.addAttribute("", "", "attr.type", "CDATA", "string");
            handler.startElement("", "", "key", attr);
            handler.endElement("", "", "key");
        }

        if (edgeLabelProvider != null) {
            // <key> for edge label attribute
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", "edge_label");
            attr.addAttribute("", "", "for", "CDATA", "edge");
            attr.addAttribute("", "", "attr.name", "CDATA", "Edge Label");
            attr.addAttribute("", "", "attr.type", "CDATA", "string");
            handler.startElement("", "", "key", attr);
            handler.endElement("", "", "key");
        }

        // <graph>
        attr.clear();
        attr.addAttribute(
            "",
            "",
            "edgedefault",
            "CDATA",
            (g instanceof DirectedGraph<?, ?>) ? "directed" : "undirected");
        handler.startElement("", "", "graph", attr);

        // Add all the vertices as <node> elements...
        for (V v : g.vertexSet()) {
            // <node>
            attr.clear();
            attr.addAttribute(
                "",
                "",
                "id",
                "CDATA",
                vertexIDProvider.getVertexName(v));
            handler.startElement("", "", "node", attr);

            if (vertexLabelProvider != null) {
                // <data>
                attr.clear();
                attr.addAttribute("", "", "key", "CDATA", "vertex_label");
                handler.startElement("", "", "data", attr);

                // Content for <data>
                String vertexLabel = vertexLabelProvider.getVertexName(v);
                handler.characters(
                    vertexLabel.toCharArray(),
                    0,
                    vertexLabel.length());

                handler.endElement("", "", "data");
            }

            handler.endElement("", "", "node");
        }

        // Add all the edges as <edge> elements...
        for (E e : g.edgeSet()) {
            // <edge>
            attr.clear();
            attr.addAttribute(
                "",
                "",
                "id",
                "CDATA",
                edgeIDProvider.getEdgeName(e));
            attr.addAttribute(
                "",
                "",
                "source",
                "CDATA",
                vertexIDProvider.getVertexName(g.getEdgeSource(e)));
            attr.addAttribute(
                "",
                "",
                "target",
                "CDATA",
                vertexIDProvider.getVertexName(g.getEdgeTarget(e)));
            handler.startElement("", "", "edge", attr);

            if (edgeLabelProvider != null) {
                // <data>
                attr.clear();
                attr.addAttribute("", "", "key", "CDATA", "edge_label");
                handler.startElement("", "", "data", attr);

                // Content for <data>
                String edgeLabel = edgeLabelProvider.getEdgeName(e);
                handler.characters(
                    edgeLabel.toCharArray(),
                    0,
                    edgeLabel.length());
                handler.endElement("", "", "data");
            }

            handler.endElement("", "", "edge");
        }

        handler.endElement("", "", "graph");
        handler.endElement("", "", "graphml");
        handler.endDocument();

        out.flush();
    }
}

// End GraphMLExporter.java
