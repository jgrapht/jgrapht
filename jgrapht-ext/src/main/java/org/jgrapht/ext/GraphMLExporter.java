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
 * (C) Copyright 2006-2016, by Trevor Harmon and Contributors.
 *
 * Original Author:  Trevor Harmon <trevor@vocaro.com>
 * Contributors: Dimitrios Michail
 *
 */
package org.jgrapht.ext;

import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Exports a graph into a GraphML file.
 *
 * <p>
 * For a description of the format see
 * <a href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/GraphML</a>.
 * </p>
 *
 * @author Trevor Harmon
 * @author Dimitrios Michail
 */
public class GraphMLExporter<V, E>
{
    private VertexNameProvider<V> vertexIDProvider;
    private VertexNameProvider<V> vertexLabelProvider;
    private EdgeNameProvider<E> edgeIDProvider;
    private EdgeNameProvider<E> edgeLabelProvider;

    /**
     * Whether to print edge weights in case the graph is weighted.
     */
    private boolean exportEdgeWeights = false;

    /**
     * Constructs a new GraphMLExporter object with integer name providers for
     * the vertex and edge IDs and null providers for the vertex and edge
     * labels.
     */
    public GraphMLExporter()
    {
        this(
            new IntegerNameProvider<>(),
            null,
            new IntegerEdgeNameProvider<>(),
            null);
    }

    /**
     * Constructs a new GraphMLExporter object with the given ID and label
     * providers.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     *        labels will not be written to the file.
     * @param edgeIDProvider for generating vertex IDs. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     *        will not be written to the file.
     */
    public GraphMLExporter(
        VertexNameProvider<V> vertexIDProvider,
        VertexNameProvider<V> vertexLabelProvider,
        EdgeNameProvider<E> edgeIDProvider,
        EdgeNameProvider<E> edgeLabelProvider)
    {
        if (vertexIDProvider == null) {
            throw new IllegalArgumentException(
                "Vertex ID provider must not be null");
        }
        this.vertexIDProvider = vertexIDProvider;
        this.vertexLabelProvider = vertexLabelProvider;
        if (edgeIDProvider == null) {
            throw new IllegalArgumentException(
                "Edge ID provider must not be null");
        }
        this.edgeIDProvider = edgeIDProvider;
        this.edgeLabelProvider = edgeLabelProvider;
    }

    /**
     * Whether the exporter will print edge weights.
     *
     * @return {@code true} if the exporter prints edge weights, {@code false}
     *         otherwise
     */
    public boolean isExportEdgeWeights()
    {
        return exportEdgeWeights;
    }

    /**
     * Set whether the exporter will print edge weights.
     *
     * @param exportEdgeWeights value to set
     */
    public void setExportEdgeWeights(boolean exportEdgeWeights)
    {
        this.exportEdgeWeights = exportEdgeWeights;
    }

    private void writeHeader(TransformerHandler handler)
        throws SAXException
    {
        handler.startPrefixMapping(
            "xsi",
            "http://www.w3.org/2001/XMLSchema-instance");
        handler.endPrefixMapping("xsi");

        AttributesImpl attr = new AttributesImpl();
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
    }

    private void writeGraphStart(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        // <graph>
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(
            "",
            "",
            "edgedefault",
            "CDATA",
            (g instanceof DirectedGraph<?, ?>) ? "directed" : "undirected");
        handler.startElement("", "", "graph", attr);
    }

    private void writeGraphEnd(TransformerHandler handler)
        throws SAXException
    {
        handler.endElement("", "", "graph");
    }

    private void writeFooter(TransformerHandler handler)
        throws SAXException
    {
        handler.endElement("", "", "graphml");
    }

    private void writeKeys(TransformerHandler handler)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
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
        if (exportEdgeWeights) {
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", "edge_weight");
            attr.addAttribute("", "", "for", "CDATA", "edge");
            attr.addAttribute("", "", "attr.name", "CDATA", "weight");
            attr.addAttribute("", "", "attr.type", "CDATA", "double");
            handler.startElement("", "", "key", attr);
            handler.startElement("", "", "default", null);
            String defaultValue = "1.0";
            handler.characters(
                defaultValue.toCharArray(),
                0,
                defaultValue.length());
            handler.endElement("", "", "default");
            handler.endElement("", "", "key");
        }
    }

    private void writeNodes(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
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
    }

    private void writeEdges(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        // Add all the edges as <edge> elements...
        AttributesImpl attr = new AttributesImpl();
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
                handler
                    .characters(edgeLabel.toCharArray(), 0, edgeLabel.length());
                handler.endElement("", "", "data");
            }
            if (exportEdgeWeights) {
                Double weight = g.getEdgeWeight(e);
                if (weight != 1.0) { // not default value
                    // <data>
                    attr.clear();
                    attr.addAttribute("", "", "key", "CDATA", "edge_weight");
                    handler.startElement("", "", "data", attr);

                    // Content for <data>
                    String weightAsString = String.valueOf(weight);
                    handler.characters(
                        weightAsString.toCharArray(),
                        0,
                        weightAsString.length());
                    handler.endElement("", "", "data");
                }
            }

            handler.endElement("", "", "edge");
        }
    }

    /**
     * Exports a graph into a plain text file in GraphML format.
     *
     * @param writer the writer to which the graph to be exported
     * @param g the graph to be exported
     * @throws ExportException in case any error occurs during export
     */
    public void export(Writer writer, Graph<V, E> g)
        throws ExportException
    {
        try {
            // Prepare an XML file to receive the GraphML data
            SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory
                .newInstance();
            TransformerHandler handler = factory.newTransformerHandler();
            handler.getTransformer()
                .setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            handler.getTransformer()
                .setOutputProperty(OutputKeys.INDENT, "yes");
            handler.setResult(new StreamResult(new PrintWriter(writer)));

            // export
            handler.startDocument();

            writeHeader(handler);
            writeKeys(handler);
            writeGraphStart(handler, g);
            writeNodes(handler, g);
            writeEdges(handler, g);
            writeGraphEnd(handler);
            writeFooter(handler);

            handler.endDocument();

            // flush
            writer.flush();
        } catch (Exception e) {
            throw new ExportException("Failed to export as GraphML", e);
        }
    }
}

// End GraphMLExporter.java
