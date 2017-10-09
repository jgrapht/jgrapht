/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.lang3.*;
import org.jgrapht.*;
import org.jgrapht.io.GmlParser.*;

/**
 * Imports a graph from a GML file (Graph Modeling Language).
 * 
 * <p>
 * For a description of the format see <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www. infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
 *
 * <p>
 * Below is small example of a graph in GML format.
 * 
 * <pre>
 * graph [
 *   node [ 
 *     id 1
 *   ]
 *   node [
 *     id 2
 *     label "Node 2 has an optional label"
 *   ]
 *   node [
 *     id 3
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     weight 2.0
 *     label "Edge between 1 and 2"
 *   ]
 *   edge [
 *     source 2
 *     target 3
 *     weight 3.0
 *     label "Edge between 2 and 3"
 *   ]
 * ]
 * </pre>
 * 
 * <p>
 * In case the graph is weighted then the importer also reads edge weights. Otherwise edge weights
 * are ignored. The importer also supports reading additional string attributes such as label or
 * custom user attributes. String attributes are unescaped as if they are Java strings.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Dimitrios Michail
 */
public class GmlImporter<V, E>
    extends AbstractBaseImporter<V, E>
    implements GraphImporter<V, E>
{
    /**
     * Constructs a new importer.
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public GmlImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        super(vertexProvider, edgeProvider);
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the gml file contains self-loops then the graph provided must also support
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
            GmlLexer lexer = new GmlLexer(new ANTLRInputStream(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // create parser
            GmlParser parser = new GmlParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Specify our entry point
            GmlContext graphContext = parser.gml();

            // Walk it and attach our listener
            ParseTreeWalker walker = new ParseTreeWalker();
            CreateGraphGmlListener listener = new CreateGraphGmlListener();
            walker.walk(listener, graphContext);

            // update graph
            listener.updateGraph(graph);
        } catch (IOException e) {
            throw new ImportException("Failed to import gml graph: " + e.getMessage(), e);
        } catch (ParseCancellationException pe) {
            throw new ImportException("Failed to import gml graph: " + pe.getMessage(), pe);
        } catch (IllegalArgumentException iae) {
            throw new ImportException("Failed to import gml graph: " + iae.getMessage(), iae);
        }
    }

    private class ThrowingErrorListener
        extends BaseErrorListener
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
    private class CreateGraphGmlListener
        extends GmlBaseListener
    {
        private static final String NODE = "node";
        private static final String EDGE = "edge";
        private static final String GRAPH = "graph";
        private static final String WEIGHT = "weight";
        private static final String ID = "id";
        private static final String SOURCE = "source";
        private static final String TARGET = "target";

        // current state of parser
        private boolean foundGraph;
        private boolean insideGraph;
        private boolean insideNode;
        private boolean insideEdge;
        private int level;
        private Integer nodeId;
        private Integer sourceId;
        private Integer targetId;
        private Double weight;
        private Map<String, String> attributes;

        // collected nodes and edges
        private Map<Integer, Node> nodes;
        private List<Node> singletons;
        private List<PartialEdge> edges;

        public void updateGraph(Graph<V, E> graph)
            throws ImportException
        {
            if (foundGraph) {
                boolean isWeighted = graph.getType().isWeighted();

                // add nodes
                int maxVertexId = 1;
                Map<Integer, V> map = new HashMap<Integer, V>();
                for (Integer id : nodes.keySet()) {
                    Node n = nodes.get(id);
                    maxVertexId = Math.max(maxVertexId, id);
                    V vertex = vertexProvider.buildVertex(id.toString(), n.attributes);
                    map.put(id, vertex);
                    graph.addVertex(vertex);
                }

                // add singleton nodes
                for (Node n : singletons) {
                    String id = String.valueOf(++maxVertexId);
                    graph.addVertex(vertexProvider.buildVertex(id, n.attributes));
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
                    if (pe.weight != null && isWeighted) {
                        graph.setEdgeWeight(e, pe.weight);
                    }
                }

            }
        }

        @Override
        public void enterGml(GmlParser.GmlContext ctx)
        {
            foundGraph = false;
            insideGraph = false;
            insideNode = false;
            insideEdge = false;
            nodes = new HashMap<>();
            singletons = new ArrayList<>();
            edges = new ArrayList<PartialEdge>();
            level = 0;
        }

        @Override
        public void enterNumberKeyValue(GmlParser.NumberKeyValueContext ctx)
        {
            String key = ctx.ID().getText();

            if (insideNode && level == 2 && key.equals(ID)) {
                try {
                    nodeId = Integer.parseInt(ctx.NUMBER().getText());
                } catch (NumberFormatException e) {
                    // ignore error
                }
            } /* TODO: graphics and its level 3 attributes */
            else if(insideNode && level == 2) {
                attributes.put(key, ctx.getText());
            } else if (insideEdge && level == 2 && key.equals(SOURCE)) {
                try {
                    sourceId = Integer.parseInt(ctx.NUMBER().getText());
                } catch (NumberFormatException e) {
                    // ignore error
                }
            } else if (insideEdge && level == 2 && key.equals(TARGET)) {
                try {
                    targetId = Integer.parseInt(ctx.NUMBER().getText());
                } catch (NumberFormatException e) {
                    // ignore error
                }
            } else if (insideEdge && level == 2 && key.equals(WEIGHT)) {
                try {
                    weight = Double.parseDouble(ctx.NUMBER().getText());
                } catch (NumberFormatException e) {
                    // ignore error
                }
            }
        }

        @Override
        public void enterListKeyValue(GmlParser.ListKeyValueContext ctx)
        {
            String key = ctx.ID().getText();
            if (level == 0 && key.equals(GRAPH)) {
                foundGraph = true;
                insideGraph = true;
            } else if (level == 1 && insideGraph && key.equals(NODE)) {
                insideNode = true;
                nodeId = null;
                attributes = new HashMap<>();
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                insideEdge = true;
                sourceId = null;
                targetId = null;
                weight = null;
                attributes = new HashMap<>();
            }
            level++;
        }

        @Override
        public void exitListKeyValue(GmlParser.ListKeyValueContext ctx)
        {
            String key = ctx.ID().getText();
            level--;
            if (level == 0 && key.equals(GRAPH)) {
                insideGraph = false;
            } else if (level == 1 && insideGraph && key.equals(NODE)) {
                if (nodeId == null) {
                    singletons.add(new Node(attributes));
                } else {
                    nodes.put(nodeId, new Node(attributes));
                }
                insideNode = false;
                attributes = null;
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                if (sourceId != null && targetId != null) {
                    edges.add(new PartialEdge(sourceId, targetId, weight, attributes));
                }
                insideEdge = false;
                attributes = null;
            }
        }

        @Override
        public void enterStringKeyValue(GmlParser.StringKeyValueContext ctx)
        {
            if (!insideEdge && !insideNode && level != 2) {
                return;
            }

            String key = ctx.ID().getText();

            if (key.equals(ID)) {
                throw new IllegalArgumentException("Invalid type for attribute id: string");
            } else if (key.equals(SOURCE)) {
                throw new IllegalArgumentException("Invalid type for attribute source: string");
            } else if (key.equals(TARGET)) {
                throw new IllegalArgumentException("Invalid type for attribute target: string");
            } else if (key.equals(WEIGHT)) {
                throw new IllegalArgumentException("Invalid type for attribute weight: string");
            }

            String text = ctx.STRING().getText();
            String noQuotes = text.subSequence(1, text.length() - 1).toString();
            String unescapedText = StringEscapeUtils.unescapeJava(noQuotes);

            attributes.put(key, unescapedText);
        }

    }

    private class Node
    {
        Map<String, String> attributes;

        public Node(Map<String, String> attributes)
        {
            this.attributes = attributes;
        }
    }

    private class PartialEdge
    {
        Integer source;
        Integer target;
        Double weight;
        Map<String, String> attributes;

        public PartialEdge(
            Integer source, Integer target, Double weight, Map<String, String> attributes)
        {
            this.source = source;
            this.target = target;
            this.weight = weight;
            this.attributes = attributes;
        }
    }

}
