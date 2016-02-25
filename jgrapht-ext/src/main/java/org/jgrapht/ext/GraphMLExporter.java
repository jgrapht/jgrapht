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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/GraphML</a>.
 * </p>
 *
 * @author Trevor Harmon
 */
public class GraphMLExporter<V, E> {
    //~ Instance fields --------------------------------------------------------

    private VertexNameProvider<V> vertexIDProvider;

    private ComponentAttributeProvider<V> vertexAttributeProvider;

    private EdgeNameProvider<E> edgeIDProvider;

    private ComponentAttributeProvider<E> edgeAttributeProvider;

    /**
     * Constructs a new GraphMLExporter object with integer name providers for the vertex and edge IDs and empty
     * attribute providers for the vertex and edge labels.
     */
    public GraphMLExporter() {
        this(new IntegerNameProvider<V>(), new EmptyAttributeProvider<V>(), new IntegerEdgeNameProvider<E>(),
                new EmptyAttributeProvider<E>());
    }

    /**
     * Constructs a new GraphMLExporter object with the given ID and label providers.
     *
     * @param vertexIDProvider
     *            for generating vertex IDs. Must not be null.
     * @param vertexAttributeProvider
     *            for generating vertex labels. If null, no vertex attributes will be written to the file.
     * @param edgeIDProvider
     *            for generating vertex IDs. Must not be null.
     * @param edgeAttributeProvider
     *            for generating edge labels. If null, no edge attributes will not be written to the file.
     */
    public GraphMLExporter(VertexNameProvider<V> vertexIDProvider,
            ComponentAttributeProvider<V> vertexAttributeProvider, EdgeNameProvider<E> edgeIDProvider,
            ComponentAttributeProvider<E> edgeAttributeProvider) {
        this.vertexIDProvider = vertexIDProvider;
        this.vertexAttributeProvider = vertexAttributeProvider == null ? new EmptyAttributeProvider<V>() : vertexAttributeProvider;
        this.edgeIDProvider = edgeIDProvider;
        this.edgeAttributeProvider = edgeAttributeProvider == null ? new EmptyAttributeProvider<E>() : edgeAttributeProvider;
    }

    /**
     * Exports a graph into a plain text file in GraphML format.
     *
     * @param writer
     *            the writer to which the graph to be exported
     * @param g
     *            the graph to be exported
     */
    public void export(Writer writer, Graph<V, E> g) throws SAXException, TransformerConfigurationException {
        // Prepare an XML file to receive the GraphML data
        PrintWriter out = new PrintWriter(writer);
        StreamResult streamResult = new StreamResult(out);
        SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler handler = factory.newTransformerHandler();
        Transformer serializer = handler.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        handler.setResult(streamResult);
        handler.startDocument();
        AttributesImpl attr = new AttributesImpl();

        // <graphml>
        handler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // FIXME: Is this the proper way to add this attribute?
        attr.addAttribute("", "", "xsi:schemaLocation", "CDATA",
                          "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
        handler.startElement("http://graphml.graphdrawing.org/xmlns", "", "graphml", attr);
        handler.endPrefixMapping("xsi");

        Set<V> vertexSet = g.vertexSet();

        // Collect vertex data keys into a set and write vertex meta data
        Set<String> vertexAttributKeys = new HashSet<String>();
        for (V v : vertexSet) {
            vertexAttributKeys.addAll(vertexAttributeProvider.getComponentAttributes(v).keySet());
        }
        // <key> for vertex label attribute
        for (String key : vertexAttributKeys) {
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", key);
            attr.addAttribute("", "", "for", "CDATA", "node");
            attr.addAttribute("", "", "attr.name", "CDATA", key);
            attr.addAttribute("", "", "attr.type", "CDATA", "string");
            handler.startElement("", "", "key", attr);
            handler.endElement("", "", "key");
        }

        // Collect edge data keys into a set and write edge meta data.
        Set<E> edgeSet = g.edgeSet();
        Set<String> edgeAttributKeys = new HashSet<String>();
        for (E e : edgeSet) {
            edgeAttributKeys.addAll(edgeAttributeProvider.getComponentAttributes(e).keySet());
        }
        for (String key : edgeAttributKeys) {
            // <key> for edge label attribute
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", key);
            attr.addAttribute("", "", "for", "CDATA", "edge");
            attr.addAttribute("", "", "attr.name", "CDATA", key);
            attr.addAttribute("", "", "attr.type", "CDATA", "string");
            handler.startElement("", "", "key", attr);
            handler.endElement("", "", "key");
        }

        // <graph>
        attr.clear();
        attr.addAttribute("", "", "edgedefault", "CDATA",
                (g instanceof DirectedGraph<?, ?>) ? "directed" : "undirected");
        handler.startElement("", "", "graph", attr);

        // Add all the vertices as <node> elements...
        for (V v : vertexSet) {
            // <node>
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", vertexIDProvider.getVertexName(v));
            handler.startElement("", "", "node", attr);

            Map<String, String> vertexAttributes = vertexAttributeProvider.getComponentAttributes(v);
            for (Map.Entry<String, String> entry : vertexAttributes.entrySet()) {
                // <data>
                attr.clear();
                attr.addAttribute("", "", "key", "CDATA", entry.getKey());
                handler.startElement("", "", "data", attr);

                // Content for <data>
                String vertexLabel = entry.getValue();
                handler.characters(vertexLabel.toCharArray(), 0, vertexLabel.length());

                handler.endElement("", "", "data");
            }

            handler.endElement("", "", "node");
        }

        // Add all the edges as <edge> elements...
        for (E e : edgeSet) {
            // <edge>
            attr.clear();
            attr.addAttribute("", "", "id", "CDATA", edgeIDProvider.getEdgeName(e));
            attr.addAttribute("", "", "source", "CDATA", vertexIDProvider.getVertexName(g.getEdgeSource(e)));
            attr.addAttribute("", "", "target", "CDATA", vertexIDProvider.getVertexName(g.getEdgeTarget(e)));
            handler.startElement("", "", "edge", attr);

            Map<String, String> edgeAttributes = edgeAttributeProvider.getComponentAttributes(e);
            for (Map.Entry<String, String> entry : edgeAttributes.entrySet()) {
                // <data>
                attr.clear();
                attr.addAttribute("", "", "key", "CDATA", entry.getKey());
                handler.startElement("", "", "data", attr);

                // Content for <data>
                String edgeLabel = entry.getValue();
                handler.characters(edgeLabel.toCharArray(), 0, edgeLabel.length());
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
