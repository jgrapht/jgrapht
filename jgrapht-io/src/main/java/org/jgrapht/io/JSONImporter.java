/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jgrapht.Graph;
import org.jgrapht.io.JsonParser.JsonContext;
import org.jgrapht.util.SupplierUtil;

/**
 * Imports a graph from a <a href="https://tools.ietf.org/html/rfc8259">JSON</a> file.
 * 
 * Below is a small example of a graph in JSON format.
 * 
 * <pre>
 * {
 *   "nodes": [
 *     { "id": "1" },
 *     { "id": "2", "label": "Node 2 label" },
 *     { "id": "3" }
 *   ],
 *   "edges": [
 *     { "source": "1", "target": "2", "weight": 2.0, "label": "Edge between 1 and 2" },
 *     { "source": "2", "target": "3", "weight": 3.0, "label": "Edge between 2 and 3" }
 *   ]
 * }
 * </pre>
 * 
 * <p>
 * In case the graph is weighted then the importer also reads edge weights. Otherwise edge weights
 * are ignored. The importer also supports reading additional string attributes such as label or
 * custom user attributes.
 * 
 * <p>
 * The parser completely ignores elements from the input that are not related to vertices or edges
 * of the graph. Moreover, complicated nested structures which are inside vertices or edges are
 * simply returned as a whole. For example, in the following graph
 * 
 * <pre>
 * {
 *   "nodes": [
 *     { "id": "1" },
 *     { "id": "2" }
 *   ],
 *   "edges": [
 *     { "source": "1", "target": "2", "points": { "x": 1.0, "y": 2.0 } }
 *   ]
 * }
 * </pre>
 * 
 * the points attribute of the edge is returned as a string containing {"x":1.0,"y":2.0}. The same
 * is done for arrays or any other arbitrary nested structure.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Dimitrios Michail
 * 
 * @deprecated Use {@link org.jgrapht.nio.json.JSONImporter} instead
 */
@Deprecated
public class JSONImporter<V, E>
    extends
    AbstractBaseImporter<V, E>
    implements
    GraphImporter<V, E>
{
    /**
     * Constructs a new importer.
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public JSONImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        super(vertexProvider, edgeProvider);
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the JSON file contains self-loops then the graph provided must also support
     * self-loops. The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge weights. Otherwise
     * edge weights are ignored.
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
            ThrowingErrorListener errorListener = new ThrowingErrorListener();

            // create lexer
            JsonLexer lexer = new JsonLexer(CharStreams.fromReader(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // create parser
            JsonParser parser = new JsonParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Specify our entry point
            JsonContext graphContext = parser.json();

            // Walk it and attach our listener
            ParseTreeWalker walker = new ParseTreeWalker();
            CreateGraphJsonListener listener = new CreateGraphJsonListener();
            walker.walk(listener, graphContext);

            // update graph
            listener.updateGraph(graph);
        } catch (IOException e) {
            throw new ImportException("Failed to import json graph: " + e.getMessage(), e);
        } catch (ParseCancellationException pe) {
            throw new ImportException("Failed to import json graph: " + pe.getMessage(), pe);
        } catch (IllegalArgumentException iae) {
            throw new ImportException("Failed to import json graph: " + iae.getMessage(), iae);
        }
    }

    private class ThrowingErrorListener
        extends
        BaseErrorListener
    {
        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e)
            throws ParseCancellationException
        {
            throw new ParseCancellationException(
                "line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    // create graph from parse tree
    private class CreateGraphJsonListener
        extends
        JsonBaseListener
    {
        private static final String GRAPH = "graph";
        private static final String NODES = "nodes";
        private static final String EDGES = "edges";
        private static final String ID = "id";

        private static final String WEIGHT = "weight";
        private static final String SOURCE = "source";
        private static final String TARGET = "target";

        // current state of parser
        private int objectLevel;
        private int arrayLevel;
        private boolean insideNodes;
        private boolean insideNodesArray;
        private boolean insideNode;
        private boolean insideEdges;
        private boolean insideEdgesArray;
        private boolean insideEdge;
        private Deque<String> pairNames;

        private String nodeId;
        private String sourceId;
        private String targetId;
        private Map<String, Attribute> attributes;

        // collected nodes and edges
        private Map<String, Node> nodes;
        private List<Node> singletons;
        private List<PartialEdge> edges;

        public void updateGraph(Graph<V, E> graph)
            throws ImportException
        {
            boolean isWeighted = graph.getType().isWeighted();

            // add nodes
            Map<String, V> map = new HashMap<>();
            for (String id : nodes.keySet()) {
                Node n = nodes.get(id);
                V vertex = vertexProvider.buildVertex(id, n.attributes);
                map.put(id, vertex);
                graph.addVertex(vertex);
            }

            // add singleton nodes
            if (!singletons.isEmpty()) {
                Supplier<String> singletonIdSupplier =
                    SupplierUtil.createRandomUUIDStringSupplier();
                for (Node n : singletons) {
                    graph
                        .addVertex(
                            vertexProvider.buildVertex(singletonIdSupplier.get(), n.attributes));
                }
            }

            // add edges
            for (PartialEdge pe : edges) {
                String label = "e_" + pe.source + "_" + pe.target;

                V from = map.get(pe.source);
                if (from == null) {
                    throw new ImportException("Node " + pe.source + " does not exist");
                }

                V to = map.get(pe.target);
                if (to == null) {
                    throw new ImportException("Node " + pe.target + " does not exist");
                }

                E e = edgeProvider.buildEdge(from, to, label, pe.attributes);
                graph.addEdge(from, to, e);

                if (isWeighted) {
                    Attribute weight = pe.attributes.get(WEIGHT);
                    if (weight != null) {
                        AttributeType type = weight.getType();
                        if (type.equals(AttributeType.FLOAT) || type.equals(AttributeType.DOUBLE)) {
                            graph.setEdgeWeight(e, Double.parseDouble(weight.getValue()));
                        }
                    }
                }
            }
        }

        @Override
        public void enterJson(JsonParser.JsonContext ctx)
        {
            objectLevel = 0;
            arrayLevel = 0;

            insideNodes = false;
            insideNodesArray = false;
            insideNode = false;
            insideEdges = false;
            insideEdgesArray = false;
            insideEdge = false;

            nodes = new LinkedHashMap<>();
            singletons = new ArrayList<>();
            edges = new ArrayList<PartialEdge>();
            pairNames = new ArrayDeque<String>();
            pairNames.push(GRAPH);
        }

        @Override
        public void enterObj(JsonParser.ObjContext ctx)
        {
            objectLevel++;
            if (objectLevel == 2 && arrayLevel == 1) {
                if (insideNodesArray) {
                    insideNode = true;
                    nodeId = null;
                    attributes = new HashMap<>();
                } else if (insideEdgesArray) {
                    insideEdge = true;
                    sourceId = null;
                    targetId = null;
                    attributes = new HashMap<>();
                }
            }
        }

        @Override
        public void exitObj(JsonParser.ObjContext ctx)
        {
            if (objectLevel == 2 && arrayLevel == 1) {
                if (insideNodesArray) {
                    if (nodeId == null) {
                        singletons.add(new Node(attributes));
                    } else {
                        if (nodes.put(nodeId, new Node(attributes)) != null) {
                            throw new IllegalArgumentException("Duplicate node id " + nodeId);
                        }
                    }
                    insideNode = false;
                    attributes = null;
                } else if (insideEdgesArray) {
                    if (sourceId != null && targetId != null) {
                        edges.add(new PartialEdge(sourceId, targetId, attributes));
                    } else if (sourceId == null) {
                        throw new IllegalArgumentException("Edge with missing source detected");
                    } else {
                        throw new IllegalArgumentException("Edge with missing target detected");
                    }
                    insideEdge = false;
                    attributes = null;
                }
            }
            objectLevel--;
        }

        @Override
        public void enterArray(JsonParser.ArrayContext ctx)
        {
            arrayLevel++;
            if (insideNodes && objectLevel == 1 && arrayLevel == 1) {
                insideNodesArray = true;
            } else if (insideEdges && objectLevel == 1 && arrayLevel == 1) {
                insideEdgesArray = true;
            }
        }

        @Override
        public void exitArray(JsonParser.ArrayContext ctx)
        {
            if (insideNodes && objectLevel == 1 && arrayLevel == 1) {
                insideNodesArray = false;
            } else if (insideEdges && objectLevel == 1 && arrayLevel == 1) {
                insideEdgesArray = false;
            }
            arrayLevel--;
        }

        @Override
        public void enterPair(JsonParser.PairContext ctx)
        {
            String name = unquote(ctx.STRING().getText());

            if (objectLevel == 1 && arrayLevel == 0) {
                if (NODES.equals(name)) {
                    insideNodes = true;
                } else if (EDGES.equals(name)) {
                    insideEdges = true;
                }
            }

            pairNames.push(name);
        }

        @Override
        public void exitPair(JsonParser.PairContext ctx)
        {
            String name = unquote(ctx.STRING().getText());

            if (objectLevel == 1 && arrayLevel == 0) {
                if (NODES.equals(name)) {
                    insideNodes = false;
                } else if (EDGES.equals(name)) {
                    insideEdges = false;
                }
            }

            pairNames.pop();
        }

        @Override
        public void enterValue(JsonParser.ValueContext ctx)
        {
            String name = pairNames.element();

            if (objectLevel == 2 && arrayLevel < 2) {
                if (insideNode) {
                    if (ID.equals(name)) {
                        nodeId = readIdentifier(ctx);
                    } else {
                        attributes.put(name, readAttribute(ctx));
                    }
                } else if (insideEdge) {
                    if (SOURCE.equals(name)) {
                        sourceId = readIdentifier(ctx);
                    } else if (TARGET.equals(name)) {
                        targetId = readIdentifier(ctx);
                    } else {
                        attributes.put(name, readAttribute(ctx));
                    }
                }
            }

        }

        private Attribute readAttribute(JsonParser.ValueContext ctx)
        {
            // string
            String stringValue = readString(ctx);
            if (stringValue != null) {
                return DefaultAttribute.createAttribute(stringValue);
            }

            // number
            TerminalNode tn = ctx.NUMBER();
            if (tn != null) {
                String value = tn.getText();
                try {
                    return DefaultAttribute.createAttribute(Integer.parseInt(value, 10));
                } catch (NumberFormatException e) {
                    // ignore
                }
                try {
                    return DefaultAttribute.createAttribute(Long.parseLong(value, 10));
                } catch (NumberFormatException e) {
                    // ignore
                }
                try {
                    return DefaultAttribute.createAttribute(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            // other
            String other = ctx.getText();
            if (other != null) {
                if ("true".equals(other)) {
                    return DefaultAttribute.createAttribute(Boolean.TRUE);
                } else if ("false".equals(other)) {
                    return DefaultAttribute.createAttribute(Boolean.FALSE);
                } else if ("null".equals(other)) {
                    return DefaultAttribute.NULL;
                } else {
                    return new DefaultAttribute<>(other, AttributeType.UNKNOWN);
                }
            }
            return DefaultAttribute.NULL;
        }

        private String unquote(String value)
        {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

        private String readString(JsonParser.ValueContext ctx)
        {
            TerminalNode tn = ctx.STRING();
            if (tn == null) {
                return null;
            }
            return unquote(tn.getText());
        }

        private String readIdentifier(JsonParser.ValueContext ctx)
        {
            TerminalNode tn = ctx.STRING();
            if (tn != null) {
                return unquote(tn.getText());
            }
            tn = ctx.NUMBER();
            if (tn == null) {
                return null;
            }
            try {
                return Long.valueOf(tn.getText(), 10).toString();
            } catch (NumberFormatException e) {
            }

            throw new IllegalArgumentException("Failed to read valid identifier");
        }

    }

    private static class Node
    {
        Map<String, Attribute> attributes;

        public Node(Map<String, Attribute> attributes)
        {
            this.attributes = attributes;
        }
    }

    private static class PartialEdge
    {
        String source;
        String target;
        Map<String, Attribute> attributes;

        public PartialEdge(String source, String target, Map<String, Attribute> attributes)
        {
            this.source = source;
            this.target = target;
            this.attributes = attributes;
        }
    }

}
