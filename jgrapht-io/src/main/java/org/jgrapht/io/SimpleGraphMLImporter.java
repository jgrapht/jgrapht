/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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

/**
 * Imports a graph from a GraphML data source.
 * 
 * <p>
 * This is a simple implementation with supports only a limited set of features of the GraphML
 * specification. For a more rigorous parser use {@link GraphMLImporter}. This version is oriented
 * towards parsing speed.
 * 
 * <p>
 * The importer uses the graph suppliers ({@link Graph#getVertexSupplier()} and
 * {@link Graph#getEdgeSupplier()}) in order to create new vertices and edges. Moreover, it notifies
 * lazily and completely out-of-order for any additional vertex, edge or graph attributes in the
 * input file. Users can register consumers for vertex, edge and graph attributes after construction
 * of the importer. Finally, default attribute values are completely ignored.
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
 *   <key id="d0" for="node" attr.name="color" attr.type="string" />
 *   <key id="d1" for="edge" attr.name="weight" attr.type="double"/>
 *   <graph id="G" edgedefault="undirected">
 *     <node id="n0">
 *       <data key="d0">green</data>
 *     </node>
 *     <node id="n1">
 *       <data key="d0">black</data>
 *     </node>     
 *     <node id="n2">
 *       <data key="d0">blue</data>
 *     </node>
 *     <node id="n3">
 *       <data key="d0">red</data>
 *     </node>
 *     <node id="n4">
 *       <data key="d0">white</data>
 *     </node>
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
public class SimpleGraphMLImporter<V, E>
    extends
    BaseListenableImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private static final String GRAPHML_SCHEMA_FILENAME = "graphml.xsd";
    private static final String XLINK_SCHEMA_FILENAME = "xlink.xsd";
    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";

    private boolean schemaValidation;
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;

    /**
     * Constructs a new importer.
     */
    public SimpleGraphMLImporter()
    {
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
        this.edgeWeightAttributeName = Objects
            .requireNonNull(edgeWeightAttributeName, "Edge weight attribute name cannot be null");
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
            GraphMLHandler handler = new GraphMLHandler(graph);
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            xmlReader.parse(new InputSource(input));
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
        private static final String GRAPH_EDGE_DEFAULT = "edgedefault";
        private static final String NODE = "node";
        private static final String NODE_ID = "id";
        private static final String EDGE = "edge";
        private static final String EDGE_ID = "id";
        private static final String EDGE_SOURCE = "source";
        private static final String EDGE_TARGET = "target";
        private static final String ALL = "all";
        private static final String KEY = "key";
        private static final String KEY_FOR = "for";
        private static final String KEY_ATTR_NAME = "attr.name";
        private static final String KEY_ATTR_TYPE = "attr.type";
        private static final String KEY_ID = "id";
        private static final String DEFAULT = "default";
        private static final String DATA = "data";
        private static final String DATA_KEY = "key";

        private Graph<V, E> graph;
        private boolean isWeighted;
        private Map<String, V> nodes;

        // parser state
        private int insideDefault;
        private int insideKey;
        private int insideData;
        private int insideGraph;
        private int insideNode;
        private V currentNode;
        private int insideEdge;
        private E currentEdge;
        private Key currentKey;
        private String currentDataKey;
        private StringBuilder currentDataValue;
        private Map<String, Key> nodeValidKeys;
        private Map<String, Key> edgeValidKeys;
        private Map<String, Key> graphValidKeys;

        public GraphMLHandler(Graph<V, E> graph)
        {
            this.graph = Objects.requireNonNull(graph);
            this.isWeighted = graph.getType().isWeighted();
        }

        @Override
        public void startDocument()
            throws SAXException
        {
            nodes = new HashMap<>();
            insideDefault = 0;
            insideKey = 0;
            insideData = 0;
            insideGraph = 0;
            insideNode = 0;
            currentNode = null;
            insideEdge = 0;
            currentEdge = null;
            currentKey = null;
            currentDataKey = null;
            currentDataValue = new StringBuilder();
            nodeValidKeys = new HashMap<>();
            edgeValidKeys = new HashMap<>();
            graphValidKeys = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            switch (localName) {
            case GRAPH:
                if (insideGraph > 0) {
                    throw new IllegalArgumentException(
                        "This importer does not support nested graphs");
                }
                insideGraph++;
                findAttribute(GRAPH_ID, attributes).ifPresent(
                    value -> notifyGraph(GRAPH_ID, DefaultAttribute.createAttribute(value)));
                findAttribute(GRAPH_EDGE_DEFAULT, attributes).ifPresent(
                    value -> notifyGraph(
                        GRAPH_EDGE_DEFAULT, DefaultAttribute.createAttribute(value)));
                break;
            case NODE:
                if (insideNode > 0 || insideEdge > 0) {
                    throw new IllegalArgumentException(
                        "Nodes cannot be inside other nodes or edges");
                }
                insideNode++;
                String nodeId = findAttribute(NODE_ID, attributes).orElseThrow(
                    () -> new IllegalArgumentException("Node must have an identifier"));
                V vertex = nodes.get(nodeId);
                if (vertex == null) {
                    vertex = graph.addVertex();
                    nodes.put(nodeId, vertex);
                }
                currentNode = vertex;
                notifyVertex(currentNode, NODE_ID, DefaultAttribute.createAttribute(nodeId));
                break;
            case EDGE:
                if (insideNode > 0 || insideEdge > 0) {
                    throw new IllegalArgumentException(
                        "Edges cannot be inside other nodes or edges");
                }
                insideEdge++;
                String sourceId = findAttribute(EDGE_SOURCE, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Edge source missing"));
                String targetId = findAttribute(EDGE_TARGET, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Edge target missing"));
                V source = nodes.computeIfAbsent(sourceId, k -> graph.addVertex());
                V target = nodes.computeIfAbsent(targetId, k -> graph.addVertex());
                currentEdge = graph.addEdge(source, target);
                notifyEdge(currentEdge, EDGE_SOURCE, DefaultAttribute.createAttribute(sourceId));
                notifyEdge(currentEdge, EDGE_TARGET, DefaultAttribute.createAttribute(targetId));
                findAttribute(EDGE_ID, attributes).ifPresent(
                    value -> notifyEdge(
                        currentEdge, EDGE_ID, DefaultAttribute.createAttribute(value)));
                break;
            case KEY:
                insideKey++;
                String keyId = findAttribute(KEY_ID, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Key id missing"));
                String keyAttrName = findAttribute(KEY_ATTR_NAME, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Key attribute name missing"));
                currentKey = new Key(
                    keyId, keyAttrName, findAttribute(KEY_ATTR_TYPE, attributes)
                        .map(AttributeType::create).orElse(AttributeType.UNKNOWN),
                    findAttribute(KEY_FOR, attributes).orElse("ALL"));
                break;
            case DEFAULT:
                insideDefault++;
                break;
            case DATA:
                insideData++;
                findAttribute(DATA_KEY, attributes).ifPresent(data -> currentDataKey = data);
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
                insideGraph--;
                break;
            case NODE:
                currentNode = null;
                insideNode--;
                break;
            case EDGE:
                currentEdge = null;
                insideEdge--;
                break;
            case KEY:
                insideKey--;
                registerKey();
                currentKey = null;
                break;
            case DEFAULT:
                insideDefault--;
                break;
            case DATA:
                if (--insideData == 0) {
                    notifyData();
                    currentDataValue.setLength(0);
                    currentDataKey = null;
                }
                break;
            default:
                break;
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
            throws SAXException
        {
            if (insideData == 1) {
                currentDataValue.append(ch, start, length);
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

        private Optional<String> findAttribute(String localName, Attributes attributes)
        {
            for (int i = 0; i < attributes.getLength(); i++) {
                String attrLocalName = attributes.getLocalName(i);
                if (attrLocalName.equals(localName)) {
                    return Optional.ofNullable(attributes.getValue(i));
                }
            }
            return Optional.empty();
        }

        private void notifyData()
        {
            if (currentDataKey == null || currentDataValue.length() == 0) {
                return;
            }

            if (currentNode != null) {
                Key key = nodeValidKeys.get(currentDataKey);
                if (key != null) {
                    notifyVertex(
                        currentNode, key.attributeName,
                        new DefaultAttribute<>(currentDataValue.toString(), key.type));
                }
            }
            if (currentEdge != null) {
                Key key = edgeValidKeys.get(currentDataKey);
                if (key != null) {
                    /*
                     * Handle special weight key
                     */
                    if (isWeighted && key.attributeName.equals(edgeWeightAttributeName)) {
                        try {
                            graph.setEdgeWeight(
                                currentEdge, Double.parseDouble(currentDataValue.toString()));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                    notifyEdge(
                        currentEdge, key.attributeName,
                        new DefaultAttribute<>(currentDataValue.toString(), key.type));
                }
            }
            if (graph != null) {
                Key key = graphValidKeys.get(currentDataKey);
                if (key != null) {
                    notifyGraph(
                        key.attributeName,
                        new DefaultAttribute<>(currentDataValue.toString(), key.type));
                }
            }
        }

        private void registerKey()
        {
            if (currentKey.isValid()) {
                switch (currentKey.target) {
                case NODE:
                    nodeValidKeys.put(currentKey.id, currentKey);
                    break;
                case EDGE:
                    edgeValidKeys.put(currentKey.id, currentKey);
                    break;
                case GRAPH:
                    graphValidKeys.put(currentKey.id, currentKey);
                    break;
                case ALL:
                    nodeValidKeys.put(currentKey.id, currentKey);
                    edgeValidKeys.put(currentKey.id, currentKey);
                    graphValidKeys.put(currentKey.id, currentKey);
                    break;
                }
            }
        }

    }

    private static class Key
    {
        String id;
        String attributeName;
        String target;
        AttributeType type;

        public Key(String id, String attributeName, AttributeType type, String target)
        {
            this.id = id;
            this.attributeName = attributeName;
            this.type = type;
            this.target = target;
        }

        public boolean isValid()
        {
            return id != null && attributeName != null && target != null;
        }

    }

}
