/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.nio.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;

/**
 * Exports a graph as GEXF (Graph Exchange XML Format).
 *
 * <p>
 * For a description of the format see <a href="https://gephi.org/gexf/format/schema.html">
 * https://gephi.org/gexf/format/schema.html</a>. A nice primer for the format is located at
 * <a href=
 * "https://gephi.org/gexf/1.2draft/gexf-12draft-primer.pdf">https://gephi.org/gexf/1.2draft/gexf-12draft-primer.pdf</a>.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class GEXFExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private static final String LABEL_ATTRIBUTE_NAME = "label";
    private static final String WEIGHT_ATTRIBUTE_NAME = "weight";
    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final Set<String> VERTEX_RESERVED_ATTRIBUTES =
        Set.of("id", LABEL_ATTRIBUTE_NAME);
    private static final Set<String> EDGE_RESERVED_ATTRIBUTES =
        Set.of("id", "type", LABEL_ATTRIBUTE_NAME, "source", "target", WEIGHT_ATTRIBUTE_NAME);

    private int totalVertexAttributes = 0;
    private Map<String, AttributeDetails> registeredVertexAttributes;
    private int totalEdgeAttributes = 0;
    private Map<String, AttributeDetails> registeredEdgeAttributes;
    private final Set<Parameter> parameters;

    private String creator = "The JGraphT Library";
    private String keywords;
    private String description;

    /**
     * Parameters that affect the behavior of the exporter.
     */
    public enum Parameter
    {
        /**
         * If set the exporter outputs edge weights
         */
        EXPORT_EDGE_WEIGHTS,
        /**
         * If set the exporter outputs edge labels. Labels are looked up from the edge attribute
         * provider.
         */
        EXPORT_EDGE_LABELS,
        /**
         * If set the exporter outputs edge types. Edge types are looked up from the type of the
         * graph. Mixed graphs are not supported.
         */
        EXPORT_EDGE_TYPES,
        /**
         * If set the exporter outputs the metadata information. This is true by default.
         */
        EXPORT_META,
    }

    /**
     * Denotes the category of a GEXF-Attribute.
     */
    public enum AttributeCategory
    {
        NODE("node"),
        EDGE("edge");

        private String name;

        private AttributeCategory(String name)
        {
            this.name = name;
        }

        /**
         * Get a string representation of the attribute category
         * 
         * @return the string representation of the attribute category
         */
        public String toString()
        {
            return name;
        }
    }

    /**
     * Constructs a new exporter with integer id providers for the vertices and the edges.
     */
    public GEXFExporter()
    {
        this(new IntegerIdProvider<V>(0), new IntegerIdProvider<E>(0));
    }

    /**
     * Constructs a new exporter.
     *
     * @param vertexIdProvider for generating vertex identifiers. Must not be null.
     * @param edgeIdProvider for generating edge identifiers. Must not be null.
     */
    public GEXFExporter(Function<V, String> vertexIdProvider, Function<E, String> edgeIdProvider)
    {
        super(vertexIdProvider);
        this.edgeIdProvider = Optional.of(edgeIdProvider);
        this.registeredVertexAttributes = new LinkedHashMap<>();
        this.registeredEdgeAttributes = new LinkedHashMap<>();
        this.parameters = new HashSet<>();

        // enable meta by default
        this.setParameter(Parameter.EXPORT_META, true);
    }

    /**
     * Return if a particular parameter of the exporter is enabled
     * 
     * @param p the parameter
     * @return {@code true} if the parameter is set, {@code false} otherwise
     */
    public boolean isParameter(Parameter p)
    {
        return parameters.contains(p);
    }

    /**
     * Set the value of a parameter of the exporter
     * 
     * @param p the parameter
     * @param value the value to set
     */
    public void setParameter(Parameter p, boolean value)
    {
        if (value) {
            parameters.add(p);
        } else {
            parameters.remove(p);
        }
    }

    /**
     * Register a GEXF Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     * @param type the attribute type
     */
    public void registerAttribute(String name, AttributeCategory category, GEXFAttributeType type)
    {
        registerAttribute(name, category, type, null);
    }

    /**
     * Register a GEXF Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     * @param type the attribute type
     * @param defaultValue default value
     */
    public void registerAttribute(
        String name, AttributeCategory category, GEXFAttributeType type, String defaultValue)
    {
        registerAttribute(name, category, type, null, null);
    }

    /**
     * Register a GEXF Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     * @param type the attribute type
     * @param defaultValue default value
     * @param options the possible options
     */
    public void registerAttribute(
        String name, AttributeCategory category, GEXFAttributeType type, String defaultValue,
        String options)
    {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Attribute category must be one of node or edge");
        }
        if (category.equals(AttributeCategory.NODE)) {
            if (VERTEX_RESERVED_ATTRIBUTES.contains(name.toLowerCase())) {
                throw new IllegalArgumentException("Reserved vertex attribute name");
            }
            registeredVertexAttributes
                .put(
                    name, new AttributeDetails(
                        String.valueOf(totalVertexAttributes++), type, defaultValue, options));
        } else if (category.equals(AttributeCategory.EDGE)) {
            if (EDGE_RESERVED_ATTRIBUTES.contains(name.toLowerCase())) {
                throw new IllegalArgumentException("Reserved edge attribute name");
            }
            registeredEdgeAttributes
                .put(
                    name, new AttributeDetails(
                        String.valueOf(totalEdgeAttributes++), type, defaultValue, options));
        }
    }

    /**
     * Unregister a GraphML-Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     */
    public void unregisterAttribute(String name, AttributeCategory category)
    {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Attribute category must be one of node or edge");
        }
        if (category.equals(AttributeCategory.NODE)) {
            if (VERTEX_RESERVED_ATTRIBUTES.contains(name.toLowerCase())) {
                throw new IllegalArgumentException("Reserved vertex attribute name");
            }
            registeredVertexAttributes.remove(name);
        } else if (category.equals(AttributeCategory.EDGE)) {
            if (EDGE_RESERVED_ATTRIBUTES.contains(name.toLowerCase())) {
                throw new IllegalArgumentException("Reserved edge attribute name");
            }
            registeredEdgeAttributes.remove(name);
        }
    }

    /**
     * Get the creator for the meta field.
     * 
     * @return the creator for the meta field
     */
    public String getCreator()
    {
        return creator;
    }

    /**
     * Set the creator for the meta field.
     * 
     * @param creator the creator for the meta field
     */
    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    /**
     * Get the keywords for the meta field.
     * 
     * @return the keywords for the meta field
     */
    public String getKeywords()
    {
        return keywords;
    }

    /**
     * Set the keywords for the meta field.
     * 
     * @param keywords the keywords for the meta field
     */
    public void setKeywords(String keywords)
    {
        this.keywords = keywords;
    }

    /**
     * Get the description for the meta field.
     * 
     * @return the description for the meta field
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the description for the meta field.
     * 
     * @param description the description for the meta field
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Exports a graph in GraphML format.
     *
     * @param g the graph
     * @param writer the writer to export the graph
     * @throws ExportException in case any error occurs during export
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        try {
            // Prepare an XML file to receive the data
            SAXTransformerFactory factory =
                (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = factory.newTransformerHandler();
            handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
            handler.setResult(new StreamResult(new PrintWriter(writer)));

            // export
            handler.startDocument();

            writeHeader(handler);
            writeMeta(handler);
            writeGraphStart(handler, g);
            writeVertexAttributes(handler);
            writeEdgeAttributes(handler);
            writeVertices(handler, g);
            writeEdges(handler, g);
            writeGraphEnd(handler);
            writeFooter(handler);

            handler.endDocument();

            // flush
            writer.flush();
        } catch (Exception e) {
            throw new ExportException("Failed to export as GEFX", e);
        }
    }

    private void writeHeader(TransformerHandler handler)
        throws SAXException
    {
        handler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        handler.endPrefixMapping("xsi");

        AttributesImpl attr = new AttributesImpl();
        attr
            .addAttribute(
                "", "", "xsi:schemaLocation", "CDATA",
                "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd");
        attr.addAttribute("", "", "version", "CDATA", "1.2");
        handler.startElement("http://www.gexf.net/1.2draft", "", "gexf", attr);
    }

    private void writeMeta(TransformerHandler handler)
        throws SAXException
    {
        boolean exportMeta = parameters.contains(Parameter.EXPORT_META);
        if (!exportMeta) {
            return;
        }
        if (creator == null && description == null && keywords == null) {
            return;
        }

        handler.startElement("", "", "meta", null);
        if (creator != null) {
            handler.startElement("", "", "creator", null);
            handler.characters(creator.toCharArray(), 0, creator.length());
            handler.endElement("", "", "creator");
        }
        if (description != null) {
            handler.startElement("", "", "description", null);
            handler.characters(description.toCharArray(), 0, description.length());
            handler.endElement("", "", "description");
        }
        if (keywords != null) {
            handler.startElement("", "", "keywords", null);
            handler.characters(keywords.toCharArray(), 0, keywords.length());
            handler.endElement("", "", "keywords");
        }
        handler.endElement("", "", "meta");
    }

    private void writeGraphStart(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr
            .addAttribute(
                "", "", "defaultedgetype", "CDATA",
                g.getType().isDirected() ? "directed" : "undirected");
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
        handler.endElement("", "", "gexf");
    }

    private void writeVertexAttributes(TransformerHandler handler)
        throws SAXException
    {
        if (registeredVertexAttributes.isEmpty()) {
            return;
        }
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "class", "CDATA", "node");
        handler.startElement("", "", "attributes", attr);

        for (Entry<String, AttributeDetails> e : registeredVertexAttributes.entrySet()) {
            writeAttribute(handler, e.getKey(), e.getValue());
        }

        handler.endElement("", "", "attributes");
    }

    private void writeEdgeAttributes(TransformerHandler handler)
        throws SAXException
    {
        if (registeredEdgeAttributes.isEmpty()) {
            return;
        }
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "class", "CDATA", "edge");
        handler.startElement("", "", "attributes", attr);

        for (Entry<String, AttributeDetails> e : registeredEdgeAttributes.entrySet()) {
            writeAttribute(handler, e.getKey(), e.getValue());
        }

        handler.endElement("", "", "attributes");
    }

    private void writeAttribute(TransformerHandler handler, String name, AttributeDetails details)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "id", "CDATA", details.key);
        attr.addAttribute("", "", "title", "CDATA", name);
        attr.addAttribute("", "", "type", "CDATA", details.type.toString());
        handler.startElement("", "", "attribute", attr);
        if (details.defaultValue != null) {
            handler.startElement("", "", "default", null);
            handler
                .characters(details.defaultValue.toCharArray(), 0, details.defaultValue.length());
            handler.endElement("", "", "default");
        }
        if (details.options != null) {
            handler.startElement("", "", "options", null);
            handler.characters(details.options.toCharArray(), 0, details.options.length());
            handler.endElement("", "", "options");
        }
        handler.endElement("", "", "attribute");
    }

    private void writeVertexAttributeValues(TransformerHandler handler, V v)
        throws SAXException
    {
        Map<String, Attribute> vertexAttributes =
            getVertexAttributes(v).orElse(Collections.emptyMap());
        if (vertexAttributes.isEmpty()) {
            return;
        }

        handler.startElement("", "", "attvalues", null);

        // check all registered
        for (Entry<String, AttributeDetails> entry : registeredVertexAttributes.entrySet()) {
            AttributeDetails details = entry.getValue();
            String name = entry.getKey();
            String defaultValue = details.defaultValue;
            if (vertexAttributes.containsKey(name)) {
                Attribute attribute = vertexAttributes.get(name);
                String value = attribute.getValue();
                if (defaultValue == null || !defaultValue.equals(value)) {
                    if (value != null) {
                        writeAttributeValue(handler, details.key, value);
                    }
                }
            }
        }

        handler.endElement("", "", "attvalues");
    }

    private void writeEdgeAttributeValues(TransformerHandler handler, E e)
        throws SAXException
    {
        Map<String, Attribute> edgeAttributes = getEdgeAttributes(e).orElse(Collections.emptyMap());
        if (edgeAttributes.isEmpty()) {
            return;
        }

        handler.startElement("", "", "attvalues", null);

        // check all registered
        for (Entry<String, AttributeDetails> entry : registeredEdgeAttributes.entrySet()) {
            AttributeDetails details = entry.getValue();
            String name = entry.getKey();
            String defaultValue = details.defaultValue;
            if (edgeAttributes.containsKey(name)) {
                Attribute attribute = edgeAttributes.get(name);
                String value = attribute.getValue();
                if (defaultValue == null || !defaultValue.equals(value)) {
                    if (value != null) {
                        writeAttributeValue(handler, details.key, value);
                    }
                }
            }
        }

        handler.endElement("", "", "attvalues");
    }

    private void writeAttributeValue(TransformerHandler handler, String key, String value)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "for", "CDATA", key);
        attr.addAttribute("", "", "value", "CDATA", value);
        handler.startElement("", "", "attvalue", attr);
        handler.endElement("", "", "attvalue");
    }

    private void writeVertices(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        handler.startElement("", "", "nodes", null);

        for (V v : g.vertexSet()) {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "", "id", "CDATA", getVertexId(v));
            attr
                .addAttribute(
                    "", "", LABEL_ATTRIBUTE_NAME, "CDATA",
                    getVertexAttribute(v, LABEL_ATTRIBUTE_NAME)
                        .map(Attribute::getValue).orElse(getVertexId(v)));
            handler.startElement("", "", "node", attr);
            writeVertexAttributeValues(handler, v);
            handler.endElement("", "", "node");
        }

        handler.endElement("", "", "nodes");
    }

    private void writeEdges(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        boolean exportEdgeWeights = parameters.contains(Parameter.EXPORT_EDGE_WEIGHTS);
        boolean exportEdgeTypes = parameters.contains(Parameter.EXPORT_EDGE_TYPES);
        boolean exportEdgeLabels = parameters.contains(Parameter.EXPORT_EDGE_LABELS);
        boolean isGraphDirected = g.getType().isDirected();

        handler.startElement("", "", "edges", null);

        for (E e : g.edgeSet()) {
            AttributesImpl attr = new AttributesImpl();
            attr
                .addAttribute(
                    "", "", "id", "CDATA",
                    getEdgeId(e)
                        .orElseThrow(
                            () -> new IllegalArgumentException(
                                "Missing or failing edge id provider.")));
            attr.addAttribute("", "", "source", "CDATA", getVertexId(g.getEdgeSource(e)));
            attr.addAttribute("", "", "target", "CDATA", getVertexId(g.getEdgeTarget(e)));
            if (exportEdgeTypes) {
                attr
                    .addAttribute(
                        "", "", TYPE_ATTRIBUTE_NAME, "CDATA",
                        isGraphDirected ? "directed" : "undirected");
            }
            if (exportEdgeWeights) {
                attr
                    .addAttribute(
                        "", "", WEIGHT_ATTRIBUTE_NAME, "CDATA", String.valueOf(g.getEdgeWeight(e)));
            }
            if (exportEdgeLabels) {
                getEdgeAttribute(e, LABEL_ATTRIBUTE_NAME).ifPresent(v -> {
                    attr.addAttribute("", "", LABEL_ATTRIBUTE_NAME, "CDATA", v.getValue());
                });
            }
            handler.startElement("", "", "edge", attr);
            writeEdgeAttributeValues(handler, e);
            handler.endElement("", "", "edge");
        }

        handler.endElement("", "", "edges");
    }

    private class AttributeDetails
    {
        public String key;
        public GEXFAttributeType type;
        public String defaultValue;
        public String options;

        public AttributeDetails(
            String key, GEXFAttributeType type, String defaultValue, String options)
        {
            this.key = key;
            this.type = type;
            this.defaultValue = defaultValue;
            this.options = options;
        }
    }

}
