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
package org.jgrapht.nio.gml;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.text.*;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.*;
import org.jgrapht.nio.gml.GmlParser.*;

import java.io.*;
import java.util.*;

/**
 * Imports a graph from a GML file (Graph Modeling Language).
 * 
 * <p>
 * For a description of the format see <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
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
 * If the input file contains edge weights then the importer also reads edge weights. The importer
 * also supports reading additional string attributes such as label or custom user attributes.
 * String attributes are unescaped as if they are Java strings.
 * 
 * <p>
 * The parser completely ignores elements from the input that are not related to vertices or edges
 * of the graph. Moreover, complicated nested structures are simply returned as a whole. For
 * example, in the following graph
 * 
 * <pre>
 * graph [
 *   node [ 
 *     id 1
 *   ]
 *   node [ 
 *     id 2
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     points [ x 1.0 y 2.0 ]
 *   ]
 * ]
 * </pre>
 * 
 * the points attribute of the edge is returned as a string containing "[ x 1.0 y 2.0 ]".
 * 
 * @author Dimitrios Michail
 */
public class GmlEventDrivenImporter
    extends
    BaseEventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
    implements
    EventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
{
    /**
     * Constructs a new importer.
     */
    public GmlEventDrivenImporter()
    {
        super();
    }

    @Override
    public void importInput(Reader input)
        throws ImportException
    {
        try {
            ThrowingErrorListener errorListener = new ThrowingErrorListener();

            // create lexer
            GmlLexer lexer = new GmlLexer(CharStreams.fromReader(input));
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
            NotifyGmlListener listener = new NotifyGmlListener();
            notifyImportEvent(ImportEvent.START);
            walker.walk(listener, graphContext);
            listener.notifySingletons();
            notifyImportEvent(ImportEvent.END);
        } catch (IOException e) {
            throw new ImportException("Failed to import gml graph: " + e.getMessage(), e);
        } catch (ParseCancellationException pe) {
            throw new ImportException("Failed to import gml graph: " + pe.getMessage(), pe);
        } catch (IllegalArgumentException iae) {
            throw new ImportException("Failed to import gml graph: " + iae.getMessage(), iae);
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

    // notify from parse tree
    private class NotifyGmlListener
        extends
        GmlBaseListener
    {
        private static final String NODE = "node";
        private static final String EDGE = "edge";
        private static final String GRAPH = "graph";
        private static final String WEIGHT = "weight";
        private static final String ID = "id";
        private static final String SOURCE = "source";
        private static final String TARGET = "target";

        // current state of parser
        private boolean insideGraph;
        private boolean insideNode;
        private boolean insideEdge;
        private int level;
        private Integer nodeId;
        private Integer sourceId;
        private Integer targetId;
        private Double weight;
        private Map<String, Attribute> attributes;
        private StringBuilder stringBuffer;
        private int maxNodeId;
        private List<Singleton> singletons;

        public void notifySingletons()
        {
            for (Singleton s : singletons) {
                maxNodeId++;
                notifyVertex(maxNodeId);
                for (String attrKey : s.attributes.keySet()) {
                    notifyVertexAttribute(maxNodeId, attrKey, s.attributes.get(attrKey));
                }
            }
        }

        @Override
        public void enterGml(GmlParser.GmlContext ctx)
        {
            insideGraph = false;
            insideNode = false;
            insideEdge = false;
            level = 0;
            singletons = new ArrayList<>();
            maxNodeId = 0;
        }

        @Override
        public void enterNumberKeyValue(GmlParser.NumberKeyValueContext ctx)
        {
            if (!insideNode && !insideEdge) {
                return;
            }

            if (level < 2) {
                return;
            }

            String key = ctx.ID().getText();
            String value = ctx.NUMBER().getText();

            if (level == 2) {
                if (insideNode) {
                    if (key.equals(ID)) {
                        try {
                            nodeId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // ignore error
                        }
                    } else {
                        attributes.put(key, parseNumberAttribute(value));
                    }
                } else {
                    // insideEdge
                    assert insideEdge;

                    switch (key) {
                    case SOURCE:
                        try {
                            sourceId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // ignore error
                        }
                        break;
                    case TARGET:
                        try {
                            targetId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // ignore error
                        }
                        break;
                    case WEIGHT:
                        try {
                            weight = Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            // ignore error
                        }
                        break;
                    default:
                        attributes.put(key, parseNumberAttribute(value));
                    }
                }
            } else {
                assert level >= 3;
                /*
                 * Inside a list. We simply concatenate everything here to allow the user to do
                 * something fancier in user-code.
                 */
                stringBuffer.append(' ');
                stringBuffer.append(key);
                stringBuffer.append(' ');
                stringBuffer.append(value);
            }

        }

        @Override
        public void enterListKeyValue(GmlParser.ListKeyValueContext ctx)
        {
            String key = ctx.ID().getText();
            if (level == 0 && key.equals(GRAPH)) {
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
            } else if (insideNode || insideEdge) {
                if (level == 2) {
                    stringBuffer = new StringBuilder();
                    stringBuffer.append('[');
                } else if (level >= 3) {
                    stringBuffer.append(' ');
                    stringBuffer.append(key);
                    stringBuffer.append(' ');
                    stringBuffer.append('[');
                }
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
                    singletons.add(new Singleton(attributes));
                } else {
                    notifyVertex(nodeId);
                    for (String attrKey : attributes.keySet()) {
                        notifyVertexAttribute(nodeId, attrKey, attributes.get(attrKey));
                    }
                    maxNodeId = Math.max(maxNodeId, nodeId);
                }
                insideNode = false;
                attributes = null;
            } else if (level == 1 && insideGraph && key.equals(EDGE)) {
                if (sourceId != null && targetId != null) {
                    Triple<Integer, Integer, Double> et = Triple.of(sourceId, targetId, weight);
                    notifyEdge(et);
                    if (weight != null) {
                        notifyEdgeAttribute(et, WEIGHT, DefaultAttribute.createAttribute(weight));
                    }
                    for (String attrKey : attributes.keySet()) {
                        notifyEdgeAttribute(et, attrKey, attributes.get(attrKey));
                    }
                }
                insideEdge = false;
                attributes = null;
            } else if (insideNode || insideEdge) {
                if (level == 2) {
                    stringBuffer.append(' ');
                    stringBuffer.append(']');
                    attributes
                        .put(
                            key,
                            new DefaultAttribute<>(stringBuffer.toString(), AttributeType.UNKNOWN));
                    stringBuffer = null;
                } else if (level >= 3) {
                    stringBuffer.append(' ');
                    stringBuffer.append(']');
                }
            }
        }

        @Override
        public void enterStringKeyValue(GmlParser.StringKeyValueContext ctx)
        {
            if (!insideNode && !insideEdge) {
                return;
            }

            if (level < 2) {
                return;
            }

            String key = ctx.ID().getText();
            String text = ctx.STRING().getText();
            String noQuotes = text.subSequence(1, text.length() - 1).toString();
            String unescapedText = StringEscapeUtils.unescapeJava(noQuotes);

            if (level == 2) {
                /*
                 * Store attribute
                 */
                if (key.equals(ID)) {
                    throw new IllegalArgumentException("Invalid type for attribute id: string");
                } else if (key.equals(SOURCE)) {
                    throw new IllegalArgumentException("Invalid type for attribute source: string");
                } else if (key.equals(TARGET)) {
                    throw new IllegalArgumentException("Invalid type for attribute target: string");
                } else if (key.equals(WEIGHT)) {
                    throw new IllegalArgumentException("Invalid type for attribute weight: string");
                }

                attributes.put(key, DefaultAttribute.createAttribute(unescapedText));
            } else if (level >= 3) {
                /*
                 * Inside a list. We simply concatenate everything here to allow the user to do
                 * something fancier in user-code.
                 */
                stringBuffer.append(' ');
                stringBuffer.append(key);
                stringBuffer.append(' ');
                stringBuffer.append(text);
            }
        }

        private Attribute parseNumberAttribute(String value)
        {
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
            return DefaultAttribute.createAttribute(value);
        }

    }

    private class Singleton
    {
        Map<String, Attribute> attributes;

        public Singleton(Map<String, Attribute> attributes)
        {
            this.attributes = attributes;
        }
    }

}
