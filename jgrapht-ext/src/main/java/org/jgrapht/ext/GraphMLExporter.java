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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Exports a graph as GraphML.
 *
 * <p>
 * For a description of the format see
 * <a href="http://en.wikipedia.org/wiki/GraphML"> http://en.wikipedia.org/wiki/
 * GraphML</a>.
 * </p>
 *
 * @author Trevor Harmon
 * @author Dimitrios Michail
 */
public class GraphMLExporter<V, E>
    implements GraphExporter<V, E>
{
    // providers
    private VertexNameProvider<V> vertexIDProvider;
    private VertexNameProvider<V> vertexLabelProvider;
    private EdgeNameProvider<E> edgeIDProvider;
    private EdgeNameProvider<E> edgeLabelProvider;
    private ComponentAttributeProvider<V> vertexAttributeProvider;
    private ComponentAttributeProvider<E> edgeAttributeProvider;

    // registered attributes
    private Map<String, AttributeDetails> registeredAttributes = new LinkedHashMap<>();
    private static final String ATTRIBUTE_KEY_PREFIX = "key";
    private int totalAttributes = 0;

    // special attributes
    private static final String VERTEX_LABEL_DEFAULT_ATTRIBUTE_NAME = "Vertex Label";
    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";
    private static final String EDGE_LABEL_DEFAULT_ATTRIBUTE_NAME = "Edge Label";

    private String vertexLabelAttributeName = VERTEX_LABEL_DEFAULT_ATTRIBUTE_NAME;
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;
    private String edgeLabelAttributeName = EDGE_LABEL_DEFAULT_ATTRIBUTE_NAME;

    /**
     * Whether to print edge weights in case the graph is weighted.
     */
    private boolean exportEdgeWeights = false;

    /**
     * Constructs a new GraphMLExporter with integer name providers for the
     * vertex and edge identifiers.
     */
    public GraphMLExporter()
    {
        this(
            new IntegerNameProvider<>(),
            null,
            null,
            new IntegerEdgeNameProvider<>(),
            null,
            null);
    }

    /**
     * Constructs a new GraphMLExporter.
     *
     * @param vertexIDProvider for generating vertex identifiers. Must not be
     *        null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     *        labels will not exported.
     * @param edgeIDProvider for generating edge identifiers. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     *        will not be exported.
     */
    public GraphMLExporter(
        VertexNameProvider<V> vertexIDProvider,
        VertexNameProvider<V> vertexLabelProvider,
        EdgeNameProvider<E> edgeIDProvider,
        EdgeNameProvider<E> edgeLabelProvider)
    {
        this(
            vertexIDProvider,
            vertexLabelProvider,
            null,
            edgeIDProvider,
            edgeLabelProvider,
            null);
    }

    /**
     * Constructs a new GraphMLExporter.
     *
     * @param vertexIDProvider for generating vertex identifiers. Must not be
     *        null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     *        labels will not exported.
     * @param vertexAttributeProvider for generating vertex attributes. If null,
     *        no additional attributes will be exported.
     * @param edgeIDProvider for generating edge identifiers. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     *        will not be exported.
     * @param edgeAttributeProvider for generating edge attributes. If null, no
     *        additional attributes will be exported.
     */
    public GraphMLExporter(
        VertexNameProvider<V> vertexIDProvider,
        VertexNameProvider<V> vertexLabelProvider,
        ComponentAttributeProvider<V> vertexAttributeProvider,
        EdgeNameProvider<E> edgeIDProvider,
        EdgeNameProvider<E> edgeLabelProvider,
        ComponentAttributeProvider<E> edgeAttributeProvider)
    {
        if (vertexIDProvider == null) {
            throw new IllegalArgumentException(
                "Vertex ID provider must not be null");
        }
        this.vertexIDProvider = vertexIDProvider;
        this.vertexLabelProvider = vertexLabelProvider;
        this.vertexAttributeProvider = vertexAttributeProvider;
        if (edgeIDProvider == null) {
            throw new IllegalArgumentException(
                "Edge ID provider must not be null");
        }
        this.edgeIDProvider = edgeIDProvider;
        this.edgeLabelProvider = edgeLabelProvider;
        this.edgeAttributeProvider = edgeAttributeProvider;
    }

    /**
     * Denotes the category of a GraphML-Attribute.
     */
    public enum AttributeCategory
    {
        GRAPH("graph"),
        NODE("node"),
        EDGE("edge"),
        ALL("all");

        private String name;

        private AttributeCategory(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return name;
        }
    }

    /**
     * Denotes the type of a GraphML-Attribute.
     */
    public enum AttributeType
    {
        BOOLEAN("boolean"),
        INT("int"),
        LONG("long"),
        FLOAT("float"),
        DOUBLE("double"),
        STRING("string");

        private String name;

        private AttributeType(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return name;
        }

    }

    /**
     * Register a GraphML-Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     * @param type the attribute type
     */
    public void registerAttribute(
        String name,
        AttributeCategory category,
        AttributeType type)
    {
        registerAttribute(name, category, type, null);
    }

    /**
     * Register a GraphML-Attribute
     * 
     * @param name the attribute name
     * @param category the attribute category
     * @param type the attribute type
     * @param defaultValue default value
     */
    public void registerAttribute(
        String name,
        AttributeCategory category,
        AttributeType type,
        String defaultValue)
    {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (name.equals(vertexLabelAttributeName)
            || name.equals(edgeWeightAttributeName)
            || name.equals(edgeLabelAttributeName))
        {
            throw new IllegalArgumentException("Reserved attribute name");
        }
        if (category == null) {
            throw new IllegalArgumentException(
                "Attribute category must be one of node, edge, graph or all");
        }
        String nextKey = ATTRIBUTE_KEY_PREFIX + (totalAttributes++);
        registeredAttributes.put(
            name,
            new AttributeDetails(nextKey, category, type, defaultValue));
    }

    /**
     * Unregister a GraphML-Attribute
     * 
     * @param name the attribute name
     */
    public void unregisterAttribute(String name)
    {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (name.equals(vertexLabelAttributeName)
            || name.equals(edgeWeightAttributeName)
            || name.equals(edgeLabelAttributeName))
        {
            throw new IllegalArgumentException("Reserved attribute name");
        }
        registeredAttributes.remove(name);
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

    /**
     * Get the attribute name for vertex labels
     * 
     * @return the attribute name
     */
    public String getVertexLabelAttributeName()
    {
        return vertexLabelAttributeName;
    }

    /**
     * Set the attribute name to use for vertex labels.
     * 
     * @param vertexLabelAttributeName the attribute name
     */
    public void setVertexLabelAttributeName(String vertexLabelAttributeName)
    {
        if (vertexLabelAttributeName == null) {
            throw new IllegalArgumentException(
                "Vertex label attribute name cannot be null");
        }
        String key = vertexLabelAttributeName.trim();
        if (registeredAttributes.containsKey(key)) {
            throw new IllegalArgumentException("Reserved attribute name");
        }
        this.vertexLabelAttributeName = key;
    }

    /**
     * Get the attribute name for edge labels
     * 
     * @return the attribute name
     */
    public String getEdgeLabelAttributeName()
    {
        return edgeLabelAttributeName;
    }

    /**
     * Set the attribute name to use for edge labels.
     * 
     * @param edgeLabelAttributeName the attribute name
     */
    public void setEdgeLabelAttributeName(String edgeLabelAttributeName)
    {
        if (edgeLabelAttributeName == null) {
            throw new IllegalArgumentException(
                "Edge label attribute name cannot be null");
        }
        String key = edgeLabelAttributeName.trim();
        if (registeredAttributes.containsKey(key)) {
            throw new IllegalArgumentException("Reserved attribute name");
        }
        this.edgeLabelAttributeName = key;
    }

    /**
     * Get the attribute name for edge weights
     * 
     * @return the attribute name
     */
    public String getEdgeWeightAttributeName()
    {
        return edgeWeightAttributeName;
    }

    /**
     * Set the attribute name to use for edge weights.
     * 
     * @param edgeWeightAttributeName the attribute name
     */
    public void setEdgeWeightAttributeName(String edgeWeightAttributeName)
    {
        if (edgeWeightAttributeName == null) {
            throw new IllegalArgumentException(
                "Edge weight attribute name cannot be null");
        }
        String key = edgeWeightAttributeName.trim();
        if (registeredAttributes.containsKey(key)) {
            throw new IllegalArgumentException("Reserved attribute name");
        }
        this.edgeWeightAttributeName = key;
    }

    /**
     * Get the vertex label provider
     * 
     * @return the vertex label provider
     */
    public VertexNameProvider<V> getVertexLabelProvider()
    {
        return vertexLabelProvider;
    }

    /**
     * Set the vertex label provider.
     * 
     * @param vertexLabelProvider the vertex label provider to set
     */
    public void setVertexLabelProvider(
        VertexNameProvider<V> vertexLabelProvider)
    {
        this.vertexLabelProvider = vertexLabelProvider;
    }

    /**
     * Get the edge label provider
     * 
     * @return the edge label provider
     */
    public EdgeNameProvider<E> getEdgeLabelProvider()
    {
        return edgeLabelProvider;
    }

    /**
     * Set the edge label provider.
     * 
     * @param edgeLabelProvider the edge label provider to set
     */
    public void setEdgeLabelProvider(EdgeNameProvider<E> edgeLabelProvider)
    {
        this.edgeLabelProvider = edgeLabelProvider;
    }

    /**
     * Get the vertex attribute provider
     * 
     * @return the vertex attribute provider
     */
    public ComponentAttributeProvider<V> getVertexAttributeProvider()
    {
        return vertexAttributeProvider;
    }

    /**
     * Set the vertex attribute provider.
     * 
     * @param vertexAttributeProvider the vertex attribute provider to set
     */
    public void setVertexAttributeProvider(
        ComponentAttributeProvider<V> vertexAttributeProvider)
    {
        this.vertexAttributeProvider = vertexAttributeProvider;
    }

    /**
     * Get the edge attribute provider
     * 
     * @return the edge attribute provider
     */
    public ComponentAttributeProvider<E> getEdgeAttributeProvider()
    {
        return edgeAttributeProvider;
    }

    /**
     * Set the edge attribute provider.
     * 
     * @param edgeAttributeProvider the edge attribute provider to set
     */
    public void setEdgeAttributeProvider(
        ComponentAttributeProvider<E> edgeAttributeProvider)
    {
        this.edgeAttributeProvider = edgeAttributeProvider;
    }

    /**
     * Exports a graph into a plain text file in GraphML format.
     *
     * @param writer the writer to which the graph to be exported
     * @param g the graph to be exported
     * 
     * @throws SAXException in case of a SAX error
     * @throws TransformerConfigurationException in case of a configuration
     *         error
     */
    @Deprecated
    public void export(Writer writer, Graph<V, E> g)
        throws SAXException, TransformerConfigurationException
    {
        try {
            exportGraph(g, writer);
        } catch (ExportException e) {
            throw new SAXException(e);
        }
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
        if (vertexLabelProvider != null) {
            writeAttribute(
                handler,
                vertexLabelAttributeName,
                new AttributeDetails(
                    "vertex_label_key",
                    AttributeCategory.NODE,
                    AttributeType.STRING,
                    null));
        }

        if (edgeLabelProvider != null) {
            writeAttribute(
                handler,
                edgeLabelAttributeName,
                new AttributeDetails(
                    "edge_label_key",
                    AttributeCategory.EDGE,
                    AttributeType.STRING,
                    null));
        }

        if (exportEdgeWeights) {
            writeAttribute(
                handler,
                edgeWeightAttributeName,
                new AttributeDetails(
                    "edge_weight_key",
                    AttributeCategory.EDGE,
                    AttributeType.DOUBLE,
                    Double.toString(WeightedGraph.DEFAULT_EDGE_WEIGHT)));
        }

        for (String attributeName : registeredAttributes.keySet()) {
            AttributeDetails details = registeredAttributes.get(attributeName);
            writeAttribute(handler, attributeName, details);
        }

    }

    private void writeData(TransformerHandler handler, String key, String value)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "key", "CDATA", key);
        handler.startElement("", "", "data", attr);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", "", "data");
    }

    private void writeAttribute(
        TransformerHandler handler,
        String name,
        AttributeDetails details)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "", "id", "CDATA", details.key);
        attr.addAttribute("", "", "for", "CDATA", details.category.toString());
        attr.addAttribute("", "", "attr.name", "CDATA", name);
        attr.addAttribute(
            "",
            "",
            "attr.type",
            "CDATA",
            details.type.toString());
        handler.startElement("", "", "key", attr);
        if (details.defaultValue != null) {
            handler.startElement("", "", "default", null);
            handler.characters(
                details.defaultValue.toCharArray(),
                0,
                details.defaultValue.length());
            handler.endElement("", "", "default");
        }
        handler.endElement("", "", "key");
    }

    private void writeNodes(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        // Add all the vertices as <node> elements...
        for (V v : g.vertexSet()) {
            // <node>
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute(
                "",
                "",
                "id",
                "CDATA",
                vertexIDProvider.getVertexName(v));
            handler.startElement("", "", "node", attr);

            if (vertexLabelProvider != null) {
                String vertexLabel = vertexLabelProvider.getVertexName(v);
                if (vertexLabel != null) {
                    writeData(handler, "vertex_label_key", vertexLabel);
                }
            }

            // find vertex attributes
            Map<String, String> vertexAttributes = null;
            if (vertexAttributeProvider != null) {
                vertexAttributes = vertexAttributeProvider
                    .getComponentAttributes(v);
            }
            if (vertexAttributes == null) {
                vertexAttributes = Collections.emptyMap();
            }

            // check all registered
            for (Entry<String, AttributeDetails> e : registeredAttributes
                .entrySet())
            {
                AttributeDetails details = e.getValue();
                if (details.category.equals(AttributeCategory.NODE)
                    || details.category.equals(AttributeCategory.ALL))
                {
                    String name = e.getKey();
                    String defaultValue = details.defaultValue;
                    if (vertexAttributes.containsKey(name)) {
                        String value = vertexAttributes.get(name);
                        if (defaultValue == null
                            || !defaultValue.equals(value))
                        {
                            if (value != null) {
                                writeData(handler, details.key, value);
                            }
                        }
                    }

                }
            }

            handler.endElement("", "", "node");
        }
    }

    private void writeEdges(TransformerHandler handler, Graph<V, E> g)
        throws SAXException
    {
        // Add all the edges as <edge> elements...
        for (E e : g.edgeSet()) {
            // <edge>
            AttributesImpl attr = new AttributesImpl();
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
                String edgeLabel = edgeLabelProvider.getEdgeName(e);
                if (edgeLabel != null) {
                    writeData(handler, "edge_label_key", edgeLabel);
                }
            }

            if (exportEdgeWeights) {
                Double weight = g.getEdgeWeight(e);
                if (!weight.equals(WeightedGraph.DEFAULT_EDGE_WEIGHT)) { // not
                                                                         // default
                                                                         // value
                    writeData(
                        handler,
                        "edge_weight_key",
                        String.valueOf(weight));
                }
            }

            // find edge attributes
            Map<String, String> edgeAttributes = null;
            if (edgeAttributeProvider != null) {
                edgeAttributes = edgeAttributeProvider
                    .getComponentAttributes(e);
            }
            if (edgeAttributes == null) {
                edgeAttributes = Collections.emptyMap();
            }

            // check all registered
            for (Entry<String, AttributeDetails> entry : registeredAttributes
                .entrySet())
            {
                AttributeDetails details = entry.getValue();
                if (details.category.equals(AttributeCategory.EDGE)
                    || details.category.equals(AttributeCategory.ALL))
                {
                    String name = entry.getKey();
                    String defaultValue = details.defaultValue;
                    if (edgeAttributes.containsKey(name)) {
                        String value = edgeAttributes.get(name);
                        if (defaultValue == null
                            || !defaultValue.equals(value))
                        {
                            if (value != null) {
                                writeData(handler, details.key, value);
                            }
                        }
                    }

                }
            }

            handler.endElement("", "", "edge");
        }
    }

    private class AttributeDetails
    {
        public String key;
        public AttributeCategory category;
        public AttributeType type;
        public String defaultValue;

        public AttributeDetails(
            String key,
            AttributeCategory category,
            AttributeType type,
            String defaultValue)
        {
            this.key = key;
            this.category = category;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }

}

// End GraphMLExporter.java
