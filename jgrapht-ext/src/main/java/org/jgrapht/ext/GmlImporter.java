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
package org.jgrapht.ext;

import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import org.jgrapht.*;
import org.jgrapht.ext.GmlParser.*;

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
 *     label "node label string"
 *   ]
 *   node [
 *     id 2
 *   ]
 *   node [
 *     id 3
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     weight 2.0
 *     label "label string"
 *   ]
 *   edge [
 *     source 2
 *     target 3
 *     weight 3.0
 *     label "another label string"
 *   ]
 * ]
 * </pre>
 * 
 * <p>
 * In case the graph is an instance of {@link org.jgrapht.WeightedGraph} then the importer also
 * reads edge weights. Otherwise edge weights are ignored.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class GmlImporter<V, E>
    implements GraphImporter<V, E>
{
    private VertexProvider<V> vertexProvider;
    private EdgeProvider<V, E> edgeProvider;

    /**
     * Constructs a new importer.
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public GmlImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        if (vertexProvider == null) {
            throw new IllegalArgumentException("Vertex provider cannot be null");
        }
        this.vertexProvider = vertexProvider;
        if (edgeProvider == null) {
            throw new IllegalArgumentException("Edge provider cannot be null");
        }
        this.edgeProvider = edgeProvider;
    }

    /**
     * Get the vertex provider
     * 
     * @return the vertex provider
     */
    public VertexProvider<V> getVertexProvider()
    {
        return vertexProvider;
    }

    /**
     * Set the vertex provider
     * 
     * @param vertexProvider the new vertex provider. Must not be null.
     */
    public void setVertexProvider(VertexProvider<V> vertexProvider)
    {
        if (vertexProvider == null) {
            throw new IllegalArgumentException("Vertex provider cannot be null");
        }
        this.vertexProvider = vertexProvider;
    }

    /**
     * Get the edge provider
     * 
     * @return The edge provider
     */
    public EdgeProvider<V, E> getEdgeProvider()
    {
        return edgeProvider;
    }

    /**
     * Set the edge provider.
     * 
     * @param edgeProvider the new edge provider. Must not be null.
     */
    public void setEdgeProvider(EdgeProvider<V, E> edgeProvider)
    {
        if (edgeProvider == null) {
            throw new IllegalArgumentException("Edge provider cannot be null");
        }
        this.edgeProvider = edgeProvider;
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
        private static final String LABEL = "label";

        private boolean foundGraph;
        private boolean insideGraph;
        private boolean insideNode;
        private boolean insideEdge;
        private int level;
        private Integer nodeId;
        private Integer sourceId;
        private Integer targetId;
        private Double weight;
        private String label;

        /**
         * Maps IDs, which are always integers, to the corresponding node labels
         * for nodes that are connected in the graph.
         */
        private HashMap<Integer, String> nodeLabels;
        /** Labels for stand-alone nodes that are not connected.  Note that these
         * can be NULL.
         */
        private List<String> singletonLabels;
        private List<PartialEdge> edges;

        public void updateGraph(Graph<V, E> graph)
            throws ImportException
        {
            if (foundGraph) {
                // Add connected nodes
                int maxV = 1;
                Map<Integer, V> map = new HashMap<Integer, V>();
                for (Integer id : nodeLabels.keySet()) {
                	String label = nodeLabels.get(id);
                	// Substitute the ID for the label if no label provided
                	if (label == null || (label != null && label.equals(""))) {
                		label = id + "";
                	}
                    maxV = Math.max(maxV, id);
                    V vertex = vertexProvider.buildVertex(label, new HashMap<String, String>());
                    if (vertex != null) {
                    	map.put(id, vertex);
                    	graph.addVertex(vertex);
                    }
                }

                // Add singleton nodes--those with no IDs and that are not connected.
                for (int i = 0; i < singletonLabels.size(); i++) {
                	String label = singletonLabels.get(i);
                	// For nodes that for whatever reason do not have IDs set in the file,
                	// assign the next higher ID as the label.
                	if (label == null || (label != null && label.equals(""))) {
                		label = String.valueOf(maxV + 1 + i);
                	}
                	V vertex = vertexProvider.buildVertex(label, new HashMap<String, String>());
                	if (vertex != null) {
                		graph.addVertex(vertex);
                	}
                }

                // Add edges
                for (PartialEdge pe : edges) {
                    String label = pe.label;
                    // If no label assigned, then substitute e_X_Y where X is the source ID
                    // and Y is the target ID.
                    if (label == null || (label != null && label.equals(""))) {
                    	label = "e_" + pe.source + "_" + pe.target;
                    }
                    V from = map.get(pe.source);
                    if (from == null) {
                        throw new ImportException("Node " + pe.source + " does not exist");
                    }
                    V to = map.get(pe.target);
                    if (to == null) {
                        throw new ImportException("Node " + pe.target + " does not exist");
                    }
                    E edge = edgeProvider.buildEdge(from, to, label, new HashMap<String, String>());
                    if (edge != null) {
                    	graph.addEdge(from, to, edge);
                    	if (pe.weight != null) {
                    		if (graph instanceof WeightedGraph<?, ?>) {
                    			((WeightedGraph<V, E>) graph).setEdgeWeight(edge, pe.weight);
                    		}
                    	}
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
            nodeLabels = new HashMap<>();
            singletonLabels = new ArrayList<>();
            edges = new ArrayList<PartialEdge>();
            level = 0;
        }
        
        @Override
        public void enterStringKeyValue(GmlParser.StringKeyValueContext ctx)
        {
        	String key = ctx.ID().getText();
        	
        	if ((insideEdge || insideNode) && level == 2 && key.equals(LABEL)) {
        		// Label will be enclosed in quotes and may contain escape sequences
        		label = StringUtility.deQuote(ctx.STRING().getText());
        	}
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
                label = null;
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                insideEdge = true;
                sourceId = null;
                targetId = null;
                weight = null;
                label = null;
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
                if (nodeId != null) {
                	// Normal case: The node has an ID, so record its ID and its label.
                	nodeLabels.put(nodeId, label);
                } else {
                	// A node without a numeric identifier is called a singleton in this
                	// context.  Lacking an identifier, it cannot be connected.  All we
                	// can do is track its label.  The label may be null!
                	singletonLabels.add(label);
                }
                insideNode = false;
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                if (sourceId != null && targetId != null) {
                    edges.add(new PartialEdge(sourceId, targetId, weight, label));
                }
                insideEdge = false;
            }
        }
    }
    
    private class PartialEdge
    {
        Integer source;
        Integer target;
        Double weight;
        String label;

        public PartialEdge(Integer source, Integer target, Double weight, String label)
        {
            this.source = source;
            this.target = target;
            this.weight = weight;
            this.label = label;
        }
    }

}
