/*
 * (C) Copyright 2016-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.graphml;

import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;
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
 * Imports a graph from a GraphML data source. The importer does not construct a graph but calls the
 * provided consumers with the appropriate arguments. Vertices are returned simply by their vertex
 * id and edges are returns as triples (source, target, weight) where weight maybe null.
 *
 * <p>
 * The importer notifies lazily and completely out-of-order for any additional vertex, edge or graph
 * attributes in the input file. Users can register consumers for vertex, edge and graph attributes
 * after construction of the importer. Finally, default attribute values are completely ignored.
 * Lazily here means that an edge is first reported with a null weight and its weight is reported
 * later using the edge attribute consumer. Since the same triple instance is used in all cases, an
 * edge may appear having a null weight when it is first reported and having a non-null weight after
 * the edge weight is reported.
 * 
 * <p>
 * This is a simple implementation with supports only a limited set of features of the GraphML
 * specification. For a more rigorous parser use {@link GraphMLImporter}. This version is oriented
 * towards parsing speed.
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
 * The importer by default validates the input using the 1.0
 * <a href="http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">GraphML Schema</a>. The user can
 * (not recommended) disable the validation by calling {@link #setSchemaValidation(boolean)}.
 * 
 * @author Dimitrios Michail
 */
public class SimpleGraphMLEventDrivenImporter
    extends
    BaseEventDrivenImporter<String, Triple<String, String, Double>>
    implements
    EventDrivenImporter<String, Triple<String, String, Double>>
{
    private static final String GRAPHML_SCHEMA_FILENAME = "graphml.xsd";
    private static final String XLINK_SCHEMA_FILENAME = "xlink.xsd";
    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";

    private boolean schemaValidation;
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;

    /**
     * Constructs a new importer.
     */
    public SimpleGraphMLEventDrivenImporter()
    {
        super();
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

    @Override
    public void importInput(Reader input)
    {
        try {
            // parse
            XMLReader xmlReader = createXMLReader();
            GraphMLHandler handler = new GraphMLHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            notifyImportEvent(ImportEvent.START);
            xmlReader.parse(new InputSource(input));
            notifyImportEvent(ImportEvent.END);
        } catch (Exception se) {
            throw new ImportException("Failed to parse GraphML", se);
        }
    }

    private XMLReader createXMLReader()
    {
        try {
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // create parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (schemaValidation) {
                // load schema
                InputStream xsdStream = Thread
                    .currentThread().getContextClassLoader()
                    .getResourceAsStream(GRAPHML_SCHEMA_FILENAME);
                if (xsdStream == null) {
                    throw new ImportException("Failed to locate GraphML xsd");
                }
                InputStream xlinkStream = Thread
                    .currentThread().getContextClassLoader()
                    .getResourceAsStream(XLINK_SCHEMA_FILENAME);
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

        // parser state
        private int insideData;
        private int insideGraph;
        private int insideNode;
        private String currentNode;
        private int insideEdge;
        private Triple<String, String, Double> currentEdge;
        private Key currentKey;
        private String currentDataKey;
        private StringBuilder currentDataValue;
        private Map<String, Key> nodeValidKeys;
        private Map<String, Key> edgeValidKeys;
        private Map<String, Key> graphValidKeys;

        public GraphMLHandler()
        {
        }

        @Override
        public void startDocument()
            throws SAXException
        {
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
                findAttribute(GRAPH_ID, attributes)
                    .ifPresent(
                        value -> notifyGraphAttribute(
                            GRAPH_ID, DefaultAttribute.createAttribute(value)));
                findAttribute(GRAPH_EDGE_DEFAULT, attributes)
                    .ifPresent(
                        value -> notifyGraphAttribute(
                            GRAPH_EDGE_DEFAULT, DefaultAttribute.createAttribute(value)));
                break;
            case NODE:
                if (insideNode > 0 || insideEdge > 0) {
                    throw new IllegalArgumentException(
                        "Nodes cannot be inside other nodes or edges");
                }
                insideNode++;
                String nodeId = findAttribute(NODE_ID, attributes)
                    .orElseThrow(
                        () -> new IllegalArgumentException("Node must have an identifier"));
                currentNode = nodeId;
                notifyVertex(currentNode);
                notifyVertexAttribute(
                    currentNode, NODE_ID, DefaultAttribute.createAttribute(nodeId));
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
                String edgeId = findAttribute(EDGE_ID, attributes).orElse(null);
                currentEdge = Triple.of(sourceId, targetId, null);
                notifyEdge(currentEdge);
                if (edgeId != null) {
                    notifyEdgeAttribute(
                        currentEdge, EDGE_ID, DefaultAttribute.createAttribute(edgeId));
                }
                notifyEdgeAttribute(
                    currentEdge, EDGE_SOURCE, DefaultAttribute.createAttribute(sourceId));
                notifyEdgeAttribute(
                    currentEdge, EDGE_TARGET, DefaultAttribute.createAttribute(targetId));
                break;
            case KEY:
                String keyId = findAttribute(KEY_ID, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Key id missing"));
                String keyAttrName = findAttribute(KEY_ATTR_NAME, attributes)
                    .orElseThrow(() -> new IllegalArgumentException("Key attribute name missing"));
                currentKey = new Key(
                    keyId, keyAttrName,
                    findAttribute(KEY_ATTR_TYPE, attributes)
                        .map(AttributeType::create).orElse(AttributeType.UNKNOWN),
                    findAttribute(KEY_FOR, attributes).orElse("ALL"));
                break;
            case DEFAULT:
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
                if (currentEdge != null && currentEdge.getThird() != null) {
                    notifyEdgeAttribute(
                        currentEdge, edgeWeightAttributeName,
                        DefaultAttribute.createAttribute(currentEdge.getThird()));
                }
                currentEdge = null;
                insideEdge--;
                break;
            case KEY:
                registerKey();
                currentKey = null;
                break;
            case DEFAULT:
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
                    notifyVertexAttribute(
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
                    if (key.attributeName.equals(edgeWeightAttributeName)) {
                        try {
                            currentEdge.setThird(Double.parseDouble(currentDataValue.toString()));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    } else {
                        notifyEdgeAttribute(
                            currentEdge, key.attributeName,
                            new DefaultAttribute<>(currentDataValue.toString(), key.type));
                    }
                }
            }

            Key key = graphValidKeys.get(currentDataKey);
            if (key != null) {
                notifyGraphAttribute(
                    key.attributeName,
                    new DefaultAttribute<>(currentDataValue.toString(), key.type));
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
