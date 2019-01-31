/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;

/**
 * Imports a graph from a GraphML data source.
 * 
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/ GraphML</a> or the
 * <a href="http://graphml.graphdrawing.org/primer/graphml-primer.html">GraphML Primer</a>.
 * </p>
 * 
 * <p>
 * Below is small example of a graph in GraphML format.
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <graphml xmlns="http://graphml.graphdrawing.org/xmlns"  
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns 
 *     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
 *   <key id="d0" for="node" attr.name="color" attr.type="string">
 *     <default>yellow</default>
 *   </key>
 *   <key id="d1" for="edge" attr.name="weight" attr.type="double"/>
 *   <graph id="G" edgedefault="undirected">
 *     <node id="n0">
 *       <data key="d0">green</data>
 *     </node>
 *     <node id="n1"/>
 *     <node id="n2">
 *       <data key="d0">blue</data>
 *     </node>
 *     <node id="n3">
 *       <data key="d0">red</data>
 *     </node>
 *     <node id="n4"/>
 *     <node id="n5">
 *       <data key="d0">turquoise</data>
 *     </node>
 *     <edge id="e0" source="n0" target="n2">
 *       <data key="d1">1.0</data>
 *     </edge>
 *     <edge id="e1" source="n0" target="n1">
 *       <data key="d1">1.0</data>
 *     </edge>
 *     <edge id="e2" source="n1" target="n3">
 *       <data key="d1">2.0</data>
 *     </edge>
 *     <edge id="e3" source="n3" target="n2"/>
 *     <edge id="e4" source="n2" target="n4"/>
 *     <edge id="e5" source="n3" target="n5"/>
 *     <edge id="e6" source="n5" target="n4">
 *       <data key="d1">1.1</data>
 *     </edge>
 *   </graph>
 * </graphml>
 * }
 * </pre>
 * 
 * <p>
 * The importer reads the input into a graph which is provided by the user. In case the graph is
 * weighted and the corresponding edge key with attr.name="weight" is defined, the importer also
 * reads edge weights. Otherwise edge weights are ignored. To test whether the graph is weighted,
 * method {@link Graph#getType()} can be used.
 * 
 * <p>
 * GraphML-Attributes Values are read as string key-value pairs and passed on to the
 * {@link VertexProvider} and {@link EdgeProvider} respectively.
 * 
 * <p>
 * The provided graph object, where the imported graph will be stored, must be able to support the
 * features of the graph that is read. For example if the GraphML file contains self-loops then the
 * graph provided must also support self-loops. The same for multiple edges. Moreover, the parser
 * completely ignores the attribute "edgedefault" which denotes whether an edge is directed or not.
 * Whether edges are directed or not depends on the underlying implementation of the user provided
 * graph object.
 * 
 * <p>
 * The importer by default validates the input using the 1.0
 * <a href="http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">GraphML Schema</a>. The user can
 * (not recommended) disable the validation by calling {@link #setSchemaValidation(boolean)}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class GraphMLImporter<V, E>
    extends
    AbstractBaseImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private static final String GRAPHML_SCHEMA_FILENAME = "graphml.xsd";
    private static final String XLINK_SCHEMA_FILENAME = "xlink.xsd";

    // special attributes
    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;

    private boolean schemaValidation;

    /**
     * Constructs a new importer.
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public GraphMLImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        super(vertexProvider, edgeProvider);
        this.schemaValidation = true;
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
            throw new IllegalArgumentException("Edge weight attribute name cannot be null");
        }
        this.edgeWeightAttributeName = edgeWeightAttributeName;
    }

    /**
     * Whether the importer validates the input
     * 
     * @return true if the importer validates the input
     */
    public boolean isSchemaValidation()
    {
        return schemaValidation;
    }

    /**
     * Set whether the importer should validate the input
     * 
     * @param schemaValidation value for schema validation
     */
    public void setSchemaValidation(boolean schemaValidation)
    {
        this.schemaValidation = schemaValidation;
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the GraphML file contains self-loops then the graph provided must also support
     * self-loops. The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge weights.
     * 
     * <p>
     * GraphML-Attributes Values are read as string key-value pairs and passed on to the
     * {@link VertexProvider} and {@link EdgeProvider} respectively.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
        throws ImportException
    {
        try {
            // parse
            XMLReader xmlReader = createXMLReader();
            GraphMLHandler handler = new GraphMLHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            xmlReader.parse(new InputSource(input));

            // read result
            handler.updateGraph(graph);
        } catch (Exception se) {
            throw new ImportException("Failed to parse GraphML", se);
        }
    }

    private XMLReader createXMLReader()
        throws ImportException
    {
        try {
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // create parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (schemaValidation) {
                // load schema
                InputStream xsdStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        GRAPHML_SCHEMA_FILENAME);
                if (xsdStream == null) {
                    throw new ImportException("Failed to locate GraphML xsd");
                }
                InputStream xlinkStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        XLINK_SCHEMA_FILENAME);
                if (xlinkStream == null) {
                    throw new ImportException("Failed to locate XLink xsd");
                }
                Source[] sources = new Source[2];
                sources[0] = new StreamSource(xlinkStream);
                sources[1] = new StreamSource(xsdStream);
                Schema schema = schemaFactory.newSchema(sources);

                spf.setSchema(schema);
            }
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();

            // create reader
            return saxParser.getXMLReader();
        } catch (Exception se) {
            throw new ImportException("Failed to parse GraphML", se);
        }
    }

    // content handler
    private class GraphMLHandler
        extends
        DefaultHandler
    {
        private static final String GRAPH = "graph";
        private static final String GRAPH_ID = "id";
        private static final String NODE = "node";
        private static final String NODE_ID = "id";
        private static final String EDGE = "edge";
        private static final String ALL = "all";
        private static final String EDGE_SOURCE = "source";
        private static final String EDGE_TARGET = "target";
        private static final String KEY = "key";
        private static final String KEY_FOR = "for";
        private static final String KEY_ATTR_NAME = "attr.name";
        private static final String KEY_ATTR_TYPE = "attr.type";
        private static final String KEY_ID = "id";
        private static final String DEFAULT = "default";
        private static final String DATA = "data";
        private static final String DATA_KEY = "key";

        // collect graph elements here
        private Map<String, GraphElement> nodes;
        private List<GraphElement> edges;

        // record state of parser
        private boolean insideDefault;
        private boolean insideData;

        // temporary state while reading elements
        // stack needed due to nested graphs in GraphML
        private Data currentData;
        private Key currentKey;
        private Deque<GraphElement> currentGraphElement;

        // collect custom keys
        private Map<String, Key> nodeValidKeys;
        private Map<String, Key> edgeValidKeys;

        // construct the actual graph after parsing
        public void updateGraph(Graph<V, E> graph)
            throws ImportException
        {
            if (nodes.isEmpty()) {
                return;
            }

            // create nodes
            Map<String, V> graphNodes = new HashMap<String, V>();

            for (Entry<String, GraphElement> en : nodes.entrySet()) {
                String nodeId = en.getKey();
                if (nodeId == null) {
                    throw new ImportException("Node id missing");
                }

                // create attributes
                Map<String, String> collectedAttributes = en.getValue().attributes;
                Map<String, Attribute> finalAttributes = new LinkedHashMap<>();

                for (Key validKey : nodeValidKeys.values()) {
                    String validId = validKey.id;
                    AttributeType validType = validKey.type;
                    if (collectedAttributes.containsKey(validId)) {
                        finalAttributes.put(
                            validKey.attributeName,
                            new DefaultAttribute<>(collectedAttributes.get(validId), validType));
                    } else if (validKey.defaultValue != null) {
                        finalAttributes.put(
                            validKey.attributeName,
                            new DefaultAttribute<>(validKey.defaultValue, validType));
                    }
                }

                // create the actual node
                V v = vertexProvider.buildVertex(nodeId, finalAttributes);
                graphNodes.put(nodeId, v);
                graph.addVertex(v);
            }

            // check how to handle special edge weight
            boolean handleSpecialEdgeWeights = false;
            double defaultSpecialEdgeWeight = Graph.DEFAULT_EDGE_WEIGHT;
            if (graph.getType().isWeighted()) {
                for (Key k : edgeValidKeys.values()) {
                    if (k.attributeName.equals(edgeWeightAttributeName)) {
                        handleSpecialEdgeWeights = true;
                        String defaultValue = k.defaultValue;
                        try {
                            if (defaultValue != null) {
                                defaultSpecialEdgeWeight = Double.parseDouble(defaultValue);
                            }
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        // first key only which maps to special edge "weight"
                        break;
                    }
                }

            }

            // create edges
            for (GraphElement p : edges) {
                if (p.id1 == null) {
                    throw new ImportException("Edge source vertex missing");
                }
                V from = graphNodes.get(p.id1);
                if (from == null) {
                    throw new ImportException("Source vertex " + p.id1 + " not found");
                }
                if (p.id2 == null) {
                    throw new ImportException("Edge target vertex missing");
                }
                V to = graphNodes.get(p.id2);
                if (to == null) {
                    throw new ImportException("Target vertex " + p.id2 + " not found");
                }

                // create attributes
                Map<String, String> collectedAttributes = p.attributes;
                Map<String, Attribute> finalAttributes = new LinkedHashMap<>();

                for (Key validKey : edgeValidKeys.values()) {
                    String validId = validKey.id;
                    AttributeType validType = validKey.type;
                    if (collectedAttributes.containsKey(validId)) {
                        finalAttributes.put(
                            validKey.attributeName,
                            new DefaultAttribute<>(collectedAttributes.get(validId), validType));
                    } else {
                        if (validKey.defaultValue != null) {
                            finalAttributes.put(
                                validKey.attributeName,
                                new DefaultAttribute<>(validKey.defaultValue, validType));
                        }
                    }
                }

                E e = edgeProvider.buildEdge(from, to, "e_" + from + "_" + to, finalAttributes);
                graph.addEdge(from, to, e);

                // special handling for weighted graphs
                if (handleSpecialEdgeWeights) {
                    if (finalAttributes.containsKey(edgeWeightAttributeName)) {
                        try {
                            graph.setEdgeWeight(
                                e, Double.parseDouble(
                                    finalAttributes.get(edgeWeightAttributeName).getValue()));
                        } catch (NumberFormatException nfe) {
                            graph.setEdgeWeight(e, defaultSpecialEdgeWeight);
                        }
                    }

                }
            }

        }

        @Override
        public void startDocument()
            throws SAXException
        {
            nodes = new HashMap<>();
            edges = new ArrayList<>();
            nodeValidKeys = new HashMap<>();
            edgeValidKeys = new HashMap<>();
            insideDefault = false;
            insideData = false;
            currentKey = null;
            currentData = null;
            currentGraphElement = new ArrayDeque<>();
            currentGraphElement.push(new GraphElement("graphml"));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            switch (localName) {
            case GRAPH:
                currentGraphElement.push(new GraphElement(findAttribute(GRAPH_ID, attributes)));
                break;
            case NODE:
                currentGraphElement.push(new GraphElement(findAttribute(NODE_ID, attributes)));
                break;
            case EDGE:
                currentGraphElement.push(
                    new GraphElement(
                        findAttribute(EDGE_SOURCE, attributes),
                        findAttribute(EDGE_TARGET, attributes)));
                break;
            case KEY:
                String keyId = findAttribute(KEY_ID, attributes);
                String keyFor = findAttribute(KEY_FOR, attributes);
                String keyAttrName = findAttribute(KEY_ATTR_NAME, attributes);
                String keyAttrType = findAttribute(KEY_ATTR_TYPE, attributes);
                currentKey = new Key(keyId, keyAttrName, null, null);
                if (keyAttrType != null) {
                    currentKey.type = AttributeType.create(keyAttrType);
                }
                if (keyFor != null) {
                    switch (keyFor) {
                    case EDGE:
                        currentKey.target = KeyTarget.EDGE;
                        break;
                    case NODE:
                        currentKey.target = KeyTarget.NODE;
                        break;
                    case ALL:
                        currentKey.target = KeyTarget.ALL;
                        break;
                    }
                }
                break;
            case DEFAULT:
                insideDefault = true;
                break;
            case DATA:
                insideData = true;
                currentData = new Data(findAttribute(DATA_KEY, attributes), null);
                break;
            default:
                break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            switch (localName) {
            case GRAPH:
                currentGraphElement.pop();
                break;
            case NODE:
                GraphElement currentNode = currentGraphElement.pop();
                if (nodes.containsKey(currentNode.id1)) {
                    throw new SAXException("Node with id " + currentNode.id1 + " already exists");
                }
                nodes.put(currentNode.id1, currentNode);
                break;
            case EDGE:
                GraphElement currentEdge = currentGraphElement.pop();
                edges.add(currentEdge);
                break;
            case KEY:
                if (currentKey.isValid()) {
                    switch (currentKey.target) {
                    case NODE:
                        nodeValidKeys.put(currentKey.id, currentKey);
                        break;
                    case EDGE:
                        edgeValidKeys.put(currentKey.id, currentKey);
                        break;
                    case ALL:
                        nodeValidKeys.put(currentKey.id, currentKey);
                        edgeValidKeys.put(currentKey.id, currentKey);
                        break;
                    }
                }
                currentKey = null;
                break;
            case DEFAULT:
                insideDefault = false;
                break;
            case DATA:
                if (currentData.isValid()) {
                    currentGraphElement.peek().attributes.put(currentData.key, currentData.value);
                }
                insideData = false;
                currentData = null;
                break;
            default:
                break;
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
            throws SAXException
        {
            if (insideDefault) {
                if (currentKey.defaultValue != null) {
                    currentKey.defaultValue += new String(ch, start, length);
                } else {
                    currentKey.defaultValue = new String(ch, start, length);
                }
            } else if (insideData) {
                if (currentData.value != null) {
                    currentData.value += new String(ch, start, length);
                } else {
                    currentData.value = new String(ch, start, length);
                }
            }
        }

        @Override
        public void warning(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        public void error(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        private String findAttribute(String localName, Attributes attributes)
        {
            for (int i = 0; i < attributes.getLength(); i++) {
                String attrLocalName = attributes.getLocalName(i);
                if (attrLocalName.equals(localName)) {
                    return attributes.getValue(i);
                }
            }
            return null;
        }

    }

    // ----- Helper classes for storing partial parser results -----

    private enum KeyTarget
    {
        NODE,
        EDGE,
        ALL
    }

    private class Key
    {
        String id;
        String attributeName;
        String defaultValue;
        KeyTarget target;
        AttributeType type;

        public Key(String id, String attributeName, String defaultValue, KeyTarget target)
        {
            this.id = id;
            this.attributeName = attributeName;
            this.defaultValue = defaultValue;
            this.target = target;
            this.type = AttributeType.STRING;
        }

        public boolean isValid()
        {
            return id != null && attributeName != null && target != null;
        }

    }

    private class Data
    {
        String key;
        String value;

        public Data(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public boolean isValid()
        {
            return key != null && value != null;
        }
    }

    private class GraphElement
    {
        String id1;
        String id2;
        Map<String, String> attributes;

        public GraphElement(String id1)
        {
            this.id1 = id1;
            this.id2 = null;
            this.attributes = new LinkedHashMap<String, String>();
        }

        public GraphElement(String id1, String id2)
        {
            this.id1 = id1;
            this.id2 = id2;
            this.attributes = new LinkedHashMap<String, String>();
        }
    }

}
