/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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
 * Imports a graph from a GEXF data source. The importer does not construct a graph but calls the
 * provided consumers with the appropriate arguments. Vertices are returned simply by their vertex
 * id and edges are returns as triples (source, target, weight) where weight maybe null.
 *
 * <p>
 * The importer notifies lazily and completely out-of-order for any additional vertex, edge or graph
 * attributes in the input file. Users can register consumers for vertex, edge and graph attributes
 * after construction of the importer. Finally, default attribute values and any nested elements are
 * completely ignored.
 * 
 * <p>
 * This is a simple implementation with supports only a limited set of features of the GEXF
 * specification, oriented towards parsing speed.
 * 
 * <p>
 * For a description of the format see <a href="https://gephi.org/gexf/format/index.html">
 * https://gephi.org/gexf/format/index.html</a> or the
 * <a href="https://gephi.org/gexf/format/primer.html">GEXF Primer</a>.
 * </p>
 * 
 * <p>
 * Below is small example of a graph in GEXF format.
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <gexf xmlns="http://www.gexf.net/1.2draft"
 *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *       xsi:schemaLocation="http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd"
 *       version="1.2">
 *   <graph defaultedgetype="undirected">
 *     <nodes>
 *       <node id="n0" label="node 0"/>
 *       <node id="n1" label="node 1"/>
 *       <node id="n2" label="node 2"/>
 *       <node id="n3" label="node 3"/>
 *       <node id="n4" label="node 4"/>
 *       <node id="n5" label="node 5"/>
 *     </nodes>
 *     <edges>
 *       <edge id="e0" source="n0" target="n2" weight="1.0"/>
 *       <edge id="e1" source="n0" target="n1" weight="1.0"/>
 *       <edge id="e2" source="n1" target="n3" weight="2.0"/>
 *       <edge id="e3" source="n3" target="n2"/>
 *       <edge id="e4" source="n2" target="n4"/>
 *       <edge id="e5" source="n3" target="n5"/>
 *       <edge id="e6" source="n5" target="n4" weight="1.1"/>
 *     </edges>
 *   </graph>
 * </gexf>
 * }
 * </pre>
 * 
 * <p>
 * The importer by default validates the input using the 1.2draft
 * <a href="https://gephi.org/gexf/1.2draft/gexf.xsd">GEXF Schema</a>. The user can (not
 * recommended) disable the validation by calling {@link #setSchemaValidation(boolean)}. Older
 * schemas are not supported.
 * 
 * @author Dimitrios Michail
 */
public class SimpleGEXFEventDrivenImporter
    extends
    BaseEventDrivenImporter<String, Triple<String, String, Double>>
    implements
    EventDrivenImporter<String, Triple<String, String, Double>>
{
    private static final List<String> SCHEMA_FILENAMES = List.of("viz.xsd", "gexf.xsd");

    private boolean schemaValidation;

    /**
     * Constructs a new importer.
     */
    public SimpleGEXFEventDrivenImporter()
    {
        super();
        this.schemaValidation = true;
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
            GEXFHandler handler = new GEXFHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            notifyImportEvent(ImportEvent.START);
            xmlReader.parse(new InputSource(input));
            notifyImportEvent(ImportEvent.END);
        } catch (Exception se) {
            throw new ImportException("Failed to parse GEXF", se);
        }
    }

    private Schema createSchema()
        throws SAXException
    {
        Source[] sources = SCHEMA_FILENAMES.stream().map(filename -> {
            InputStream is =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null) {
                throw new ImportException("Failed to locate xsd: " + filename);
            }
            return is;
        }).map(is -> new StreamSource(is)).toArray(Source[]::new);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        return factory.newSchema(sources);
    }

    private XMLReader createXMLReader()
    {
        try {
            // create parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (schemaValidation) {
                spf.setSchema(createSchema());
            }
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();

            // create reader
            return saxParser.getXMLReader();
        } catch (Exception se) {
            throw new ImportException("Failed to parse GEXF", se);
        }
    }

    // content handler
    private class GEXFHandler
        extends
        DefaultHandler
    {
        private static final String GRAPH = "graph";
        private final List<String> GRAPH_ATTRS =
            List.of("defaultedgetype", "timeformat", "mode", "start", "end");
        private static final String NODE = "node";
        private static final String NODE_ID = "id";
        private final List<String> NODE_ATTRS = List.of("label", "pid");
        private static final String EDGE = "edge";
        private static final String EDGE_ID = "id";
        private static final String EDGE_SOURCE = "source";
        private static final String EDGE_TARGET = "target";
        private static final String EDGE_WEIGHT = "weight";
        private final List<String> EDGE_ATTRS = List.of("type", "label");

        private static final String ATTRIBUTES = "attributes";
        private static final String ATTRIBUTES_CLASS = "class";
        private static final String ATTRIBUTE = "attribute";
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_TITLE = "title";
        private static final String ATTRIBUTE_TYPE = "type";
        private static final String ATTVALUES = "attvalues";
        private static final String ATTVALUE = "attvalue";
        private static final String ATTVALUE_FOR = "for";
        private static final String ATTVALUE_VALUE = "value";

        // parser state
        private int insideGraph;
        private int insideNode;
        private String currentNode;
        private int insideEdge;
        private Triple<String, String, Double> currentEdge;
        private int insideAttributes;
        private String attributesClass;
        private int insideAttribute;
        private int insideAttValues;
        private int insideAttValue;
        private Map<String, Attribute> nodeValidAttributes;
        private Map<String, Attribute> edgeValidAttributes;

        public GEXFHandler()
        {
        }

        @Override
        public void startDocument()
            throws SAXException
        {
            insideGraph = 0;
            insideNode = 0;
            currentNode = null;
            insideEdge = 0;
            currentEdge = null;

            insideAttributes = 0;
            attributesClass = null;
            insideAttribute = 0;

            insideAttValues = 0;
            insideAttValue = 0;

            nodeValidAttributes = new HashMap<>();
            edgeValidAttributes = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            switch (localName) {
            case GRAPH:
                insideGraph++;
                if (insideGraph == 1) {
                    for (String attrName : GRAPH_ATTRS) {
                        findAttribute(attrName, attributes).ifPresent(value -> {
                            notifyGraphAttribute(attrName, DefaultAttribute.createAttribute(value));
                        });
                    }
                }
                break;
            case NODE:
                insideNode++;
                if (insideNode == 1 ^ insideEdge == 1) {
                    String nodeId = findAttribute(NODE_ID, attributes)
                        .orElseThrow(
                            () -> new IllegalArgumentException("Node must have an identifier"));
                    currentNode = nodeId;
                    notifyVertex(currentNode);
                    for (String attrName : NODE_ATTRS) {
                        findAttribute(attrName, attributes).ifPresent(value -> {
                            notifyVertexAttribute(
                                currentNode, attrName, DefaultAttribute.createAttribute(value));
                        });
                    }
                }
                break;
            case EDGE:
                insideEdge++;
                if (insideNode == 1 ^ insideEdge == 1) {
                    String sourceId = findAttribute(EDGE_SOURCE, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Edge source missing"));
                    String targetId = findAttribute(EDGE_TARGET, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Edge target missing"));
                    String edgeId = findAttribute(EDGE_ID, attributes).orElse(null);

                    String edgeWeight = findAttribute(EDGE_WEIGHT, attributes).orElse(null);
                    Double edgeWeightAsDouble = null;
                    if (edgeWeight != null) {
                        try {
                            edgeWeightAsDouble = Double.parseDouble(edgeWeight);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }

                    currentEdge = Triple.of(sourceId, targetId, edgeWeightAsDouble);
                    notifyEdge(currentEdge);

                    if (edgeId != null) {
                        notifyEdgeAttribute(
                            currentEdge, EDGE_ID, DefaultAttribute.createAttribute(edgeId));
                    }
                    notifyEdgeAttribute(
                        currentEdge, EDGE_SOURCE, DefaultAttribute.createAttribute(sourceId));
                    notifyEdgeAttribute(
                        currentEdge, EDGE_TARGET, DefaultAttribute.createAttribute(targetId));

                    if (edgeWeightAsDouble != null) {
                        notifyEdgeAttribute(
                            currentEdge, "weight",
                            DefaultAttribute.createAttribute(edgeWeightAsDouble));
                    }
                    for (String attrName : EDGE_ATTRS) {
                        findAttribute(attrName, attributes).ifPresent(value -> {
                            notifyEdgeAttribute(
                                currentEdge, attrName, DefaultAttribute.createAttribute(value));
                        });
                    }
                }
                break;
            case ATTRIBUTES:
                insideAttributes++;
                if (insideGraph == 1 && insideAttributes == 1) {
                    attributesClass = findAttribute(ATTRIBUTES_CLASS, attributes)
                        .orElseThrow(
                            () -> new IllegalArgumentException("Attributes class missing"));
                }
                break;
            case ATTRIBUTE:
                insideAttribute++;
                if (insideGraph == 1 && insideAttributes == 1 && insideAttribute == 1) {
                    String attributeId = findAttribute(ATTRIBUTE_ID, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Attribute id missing"));
                    String attributeTitle = findAttribute(ATTRIBUTE_TITLE, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Attribute title missing"));
                    String attributeType = findAttribute(ATTRIBUTE_TYPE, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Attribute type missing"));
                    Attribute curAttribute = new Attribute(
                        attributeId, attributeTitle, GEXFAttributeType.create(attributeType));
                    if ("node".equals(attributesClass)) {
                        nodeValidAttributes.put(curAttribute.id, curAttribute);
                    } else if ("edge".equals(attributesClass)) {
                        edgeValidAttributes.put(curAttribute.id, curAttribute);
                    } else {
                        throw new IllegalArgumentException("Wrong attribute class provided");
                    }
                }
                break;
            case ATTVALUES:
                insideAttValues++;
                break;
            case ATTVALUE:
                insideAttValue++;
                if (insideAttValues == 1 && insideAttValue == 1
                    && (insideNode == 1 ^ insideEdge == 1))
                {
                    String attValueFor = findAttribute(ATTVALUE_FOR, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Attribute for missing"));
                    String attValueValue = findAttribute(ATTVALUE_VALUE, attributes)
                        .orElseThrow(() -> new IllegalArgumentException("Attribute value missing"));
                    if (insideNode == 1 && currentNode != null) {
                        Attribute attr = nodeValidAttributes.get(attValueFor);
                        notifyVertexAttribute(
                            currentNode, attr.title,
                            new DefaultAttribute<>(attValueValue, toAttributeType(attr.type)));
                    } else if (insideEdge == 1 && currentEdge != null) {
                        Attribute attr = edgeValidAttributes.get(attValueFor);
                        notifyEdgeAttribute(
                            currentEdge, attr.title,
                            new DefaultAttribute<>(attValueValue, toAttributeType(attr.type)));
                    }
                }
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
                insideNode--;
                if (insideNode == 0) {
                    currentNode = null;
                }
                break;
            case EDGE:
                insideEdge--;
                if (insideEdge == 0) {
                    currentEdge = null;
                }
                break;
            case ATTRIBUTES:
                insideAttributes--;
                if (insideAttributes == 0) {
                    attributesClass = null;
                }
                break;
            case ATTRIBUTE:
                insideAttribute--;
                break;
            case ATTVALUES:
                insideAttValues--;
                break;
            case ATTVALUE:
                insideAttValue--;
                break;
            default:
                break;
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
    }

    private static class Attribute
    {
        String id;
        String title;
        GEXFAttributeType type;

        public Attribute(String id, String title, GEXFAttributeType type)
        {
            this.id = id;
            this.title = title;
            this.type = type;
        }
    }

    private static AttributeType toAttributeType(GEXFAttributeType type)
    {
        switch (type) {
        case BOOLEAN:
            return AttributeType.BOOLEAN;
        case INTEGER:
            return AttributeType.INT;
        case LONG:
            return AttributeType.LONG;
        case FLOAT:
            return AttributeType.FLOAT;
        case DOUBLE:
            return AttributeType.DOUBLE;
        case ANYURI:
        case LISTSTRING:
        case STRING:
            return AttributeType.STRING;
        default:
            return AttributeType.UNKNOWN;
        }

    }

}
