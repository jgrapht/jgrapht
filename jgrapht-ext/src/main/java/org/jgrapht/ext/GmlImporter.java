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
/* -------------------
 * GmlImporter.java
 * -------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 * Contributor(s): 
 *
 * Changes
 * -------
 * 16-July-2016 : Initial revision (DM);
 *
 */
package org.jgrapht.ext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.GmlParser.GmlContext;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Imports a graph from a GML file (Graph Modeling Language).
 * 
 * <p>
 * For a description of the format see
 * <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/"> http://www.
 * infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
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
 *   ]
 *   node [
 *     id 3
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     weight 2.0
 *   ]
 *   edge [
 *     source 2
 *     target 3
 *     weight 3.0
 *   ]
 * ]
 * </pre>
 * 
 * <p>
 * In case the graph is an instance of {@link org.jgrapht.WeightedGraph} then
 * the importer also reads edge weights. Otherwise edge weights are ignored.
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
     * @param vertexProvider provider for the generation of vertices. Must not
     *        be null.
     * @param edgeProvider provider for the generation of edges. Must not be
     *        null.
     */
    public GmlImporter(
        VertexProvider<V> vertexProvider,
        EdgeProvider<V, E> edgeProvider)
    {
        if (vertexProvider == null) {
            throw new IllegalArgumentException(
                "Vertex provider cannot be null");
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
            throw new IllegalArgumentException(
                "Vertex provider cannot be null");
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
     * The provided graph must be able to support the features of the graph that
     * is read. For example if the gml file contains self-loops then the graph
     * provided must also support self-loops. The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge
     * weights. Otherwise edge weights are ignored.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse
     *         error
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
            throw new ImportException(
                "Failed to import gml graph: " + e.getMessage(),
                e);
        } catch (ParseCancellationException pe) {
            throw new ImportException(
                "Failed to import gml graph: " + pe.getMessage(),
                pe);
        } catch (IllegalArgumentException iae) {
            throw new ImportException(
                "Failed to import gml graph: " + iae.getMessage(),
                iae);
        }
    }

    private class ThrowingErrorListener
        extends BaseErrorListener
    {

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e)
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

        private boolean foundGraph;
        private boolean insideGraph;
        private boolean insideNode;
        private boolean insideEdge;
        private int level;
        private Integer nodeId;
        private Integer sourceId;
        private Integer targetId;
        private Double weight;

        private Set<Integer> nodes;
        private int singletons;
        private List<PartialEdge> edges;

        public void updateGraph(Graph<V, E> graph)
            throws ImportException
        {
            if (foundGraph) {
                // add nodes
                int maxV = 1;
                Map<Integer, V> map = new HashMap<Integer, V>();
                for (Integer id : nodes) {
                    maxV = Math.max(maxV, id);
                    V vertex = vertexProvider.buildVertex(
                        id.toString(),
                        new HashMap<String, String>());
                    map.put(id, vertex);
                    graph.addVertex(vertex);
                }

                // add singleton nodes
                for (int i = 0; i < singletons; i++) {
                    String label = String.valueOf(maxV + 1 + i);
                    graph.addVertex(
                        vertexProvider
                            .buildVertex(label, new HashMap<String, String>()));
                }

                // add edges
                for (PartialEdge pe : edges) {
                    String label = "e_" + pe.source + "_" + pe.target;
                    V from = map.get(pe.source);
                    if (from == null) {
                        throw new ImportException(
                            "Node " + pe.source + " does not exist");
                    }
                    V to = map.get(pe.target);
                    if (to == null) {
                        throw new ImportException(
                            "Node " + pe.target + " does not exist");
                    }
                    E e = edgeProvider.buildEdge(
                        from,
                        to,
                        label,
                        new HashMap<String, String>());
                    graph.addEdge(from, to, e);
                    if (pe.weight != null) {
                        if (graph instanceof WeightedGraph<?, ?>) {
                            ((WeightedGraph<V, E>) graph)
                                .setEdgeWeight(e, pe.weight);
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
            nodes = new HashSet<Integer>();
            singletons = 0;
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
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                insideEdge = true;
                sourceId = null;
                targetId = null;
                weight = null;
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
                    singletons++;
                } else {
                    nodes.add(nodeId);
                }
                insideNode = false;
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                if (sourceId != null && targetId != null) {
                    edges.add(new PartialEdge(sourceId, targetId, weight));
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

        public PartialEdge(Integer source, Integer target, Double weight)
        {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }
    }

}
