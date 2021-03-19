/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.dot;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.apache.commons.text.*;
import org.apache.commons.text.translate.*;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Import a graph from a DOT file.
 * 
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a> and
 * <a href="http://www.graphviz.org/doc/info/lang.html">
 * http://www.graphviz.org/doc/info/lang.html</a>
 * 
 * <p>
 * The importer notifies interested parties using consumers.
 *
 * @author Dimitrios Michail
 */
public class DOTEventDrivenImporter
    extends
    BaseEventDrivenImporter<String, Pair<String, String>>
    implements
    EventDrivenImporter<String, Pair<String, String>>
{
    /**
     * Default key used for the graph ID.
     */
    public static final String DEFAULT_GRAPH_ID_KEY = "ID";

    // identifier unescape rule
    private final CharSequenceTranslator UNESCAPE_ID;

    private boolean notifyVertexAttributesOutOfOrder;
    private boolean notifyEdgeAttributesOutOfOrder;

    /**
     * Constructs a new importer.
     */
    public DOTEventDrivenImporter()
    {
        this(true, true);
    }

    /**
     * Constructs a new importer.
     * 
     * @param notifyVertexAttributesOutOfOrder whether to notify for vertex attributes out-of-order
     *        even if they appear together in the input
     * @param notifyEdgeAttributesOutOfOrder whether to notify for edge attributes out-of-order even
     *        if they appear together in the input
     */
    public DOTEventDrivenImporter(
        boolean notifyVertexAttributesOutOfOrder, boolean notifyEdgeAttributesOutOfOrder)
    {
        super();
        Map<CharSequence, CharSequence> lookupMap = new HashMap<>();
        lookupMap.put("\\\\", "\\");
        lookupMap.put("\\\"", "\"");
        lookupMap.put("\\'", "'");
        lookupMap.put("\\", "");
        UNESCAPE_ID = new AggregateTranslator(new LookupTranslator(lookupMap));

        this.notifyVertexAttributesOutOfOrder = notifyVertexAttributesOutOfOrder;
        this.notifyEdgeAttributesOutOfOrder = notifyEdgeAttributesOutOfOrder;
    }

    @Override
    public void importInput(Reader in)
        throws ImportException
    {
        try {
            /**
             * Create lexer with unbuffered input stream and use a token factory which copies
             * characters from the input stream into the text of the tokens.
             */
            DOTLexer lexer = new DOTLexer(new UnbufferedCharStream(in));
            lexer.setTokenFactory(new CommonTokenFactory(true));
            lexer.removeErrorListeners();
            ThrowingErrorListener errorListener = new ThrowingErrorListener();
            lexer.addErrorListener(errorListener);

            /**
             * Create parser with unbuffered token stream.
             */
            DOTParser parser = new DOTParser(new UnbufferedTokenStream<>(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            /**
             * Disable parse tree building and attach listener.
             */
            parser.setBuildParseTree(false);
            parser.addParseListener(new NotifyDOTListener());

            /**
             * Parse
             */
            notifyImportEvent(ImportEvent.START);
            parser.graph();
            notifyImportEvent(ImportEvent.END);
        } catch (ParseCancellationException | IllegalArgumentException e) {
            throw new ImportException("Failed to import DOT graph: " + e.getMessage(), e);
        }
    }

    /*
     * Common error listener for both lexer and parser which throws an exception.
     */
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

    /*
     * Listen on parser events and construct the graph. The listener is strongly dependent on the
     * grammar.
     */
    private class NotifyDOTListener
        extends
        DOTBaseListener
    {
        private Set<String> vertices;

        // stacks to maintain scope and state
        private Deque<SubgraphScope> subgraphScopes;
        private Deque<State> stack;

        public NotifyDOTListener()
        {
            this.vertices = new HashSet<>();
            this.stack = new ArrayDeque<>();
            this.subgraphScopes = new ArrayDeque<>();
        }

        @Override
        public void enterGraph(DOTParser.GraphContext ctx)
        {
            stack.push(new State());
            subgraphScopes.push(new SubgraphScope());
        }

        @Override
        public void exitGraph(DOTParser.GraphContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }
            subgraphScopes.pop();
            stack.pop();
        }

        @Override
        public void enterGraphHeader(DOTParser.GraphHeaderContext ctx)
        {
            // nothing
        }

        @Override
        public void exitGraphHeader(DOTParser.GraphHeaderContext ctx)
        {
            // nothing
        }

        @Override
        public void enterGraphIdentifier(DOTParser.GraphIdentifierContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitGraphIdentifier(DOTParser.GraphIdentifierContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // read graph id
            State s = stack.pop();
            State idPartial = s.children.peekFirst();

            if (idPartial != null) {
                notifyGraphAttribute(
                    DEFAULT_GRAPH_ID_KEY, DefaultAttribute.createAttribute(idPartial.getId()));
            }

            // add as child of parent
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterAttributeStatement(DOTParser.AttributeStatementContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitAttributeStatement(DOTParser.AttributeStatementContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            // read attributes
            State s = stack.pop();
            State child = s.children.peekFirst();
            if (child != null && child.attrs != null) {
                Map<String, Attribute> attrs = child.attrs;

                // update current scope
                SubgraphScope scope = subgraphScopes.element();
                if (ctx.NODE() != null) {
                    scope.nodeAttrs.putAll(attrs);
                } else if (ctx.EDGE() != null) {
                    scope.edgeAttrs.putAll(attrs);
                } else if (ctx.GRAPH() != null) {
                    scope.graphAttrs.putAll(attrs);
                }
            }
        }

        @Override
        public void enterAttributesList(DOTParser.AttributesListContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitAttributesList(DOTParser.AttributesListContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // union children attributes
            State s = stack.pop();
            for (State child : s.children) {
                if (child.attrs != null) {
                    s.putAll(child.attrs);
                }
            }

            // add as child of parent
            s.children.clear();
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterAList(DOTParser.AListContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitAList(DOTParser.AListContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // collect attributes in map
            State s = stack.pop();
            Iterator<State> it = s.children.iterator();
            while (it.hasNext()) {
                State child = it.next();
                if (child.ids != null && child.ids.size() == 1) {
                    s.put(child.ids.get(0), null);
                } else if (child.ids != null && child.ids.size() >= 2) {
                    s.put(child.ids.get(0), DefaultAttribute.createAttribute(child.ids.get(1)));
                }
                it.remove();
            }

            // add as child of parent
            s.children.clear();
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterEdgeStatement(DOTParser.EdgeStatementContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitEdgeStatement(DOTParser.EdgeStatementContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            State s = stack.pop();

            // find attributes (last child)
            Map<String, Attribute> attrs = null;
            State last = s.children.peekLast();
            if (last != null && last.attrs != null) {
                attrs = last.attrs;
            }

            Iterator<State> it = s.children.iterator();
            State cur, prev = null;
            while (it.hasNext()) {
                cur = it.next();
                if (cur.attrs != null) {
                    // last node with attributes
                    break;
                } else if (prev != null) {
                    for (String sourceVertex : prev.getVertices()) {
                        for (String targetVertex : cur.getVertices()) {
                            // find default attributes
                            Map<String, Attribute> edgeAttrs =
                                new HashMap<>(subgraphScopes.element().edgeAttrs);
                            // add extra attributes
                            if (attrs != null) {
                                edgeAttrs.putAll(attrs);
                            }

                            Pair<String, String> pe = Pair.of(sourceVertex, targetVertex);

                            if (notifyEdgeAttributesOutOfOrder) {
                                // notify individually
                                notifyEdge(pe);
                                for (Entry<String, Attribute> entry : edgeAttrs.entrySet()) {
                                    notifyEdgeAttribute(pe, entry.getKey(), entry.getValue());
                                }
                            } else {
                                // notify with all attributes
                                notifyEdgeWithAttributes(pe, edgeAttrs);
                            }
                        }
                    }
                }

                prev = cur;
            }
        }

        @Override
        public void enterIdentifierPairStatement(DOTParser.IdentifierPairStatementContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitIdentifierPairStatement(DOTParser.IdentifierPairStatementContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            // read key value pair
            State s = stack.pop();
            State idPairChild = s.children.peekFirst();
            if (idPairChild == null || idPairChild.ids == null) {
                return;
            }
            String key = idPairChild.ids.get(0);
            String value = idPairChild.ids.get(1);

            // update attributes in current scope
            SubgraphScope scope = subgraphScopes.element();
            scope.graphAttrs.put(key, DefaultAttribute.createAttribute(value));
            if (subgraphScopes.size() == 1) {
                notifyGraphAttribute(key, DefaultAttribute.createAttribute(value));
            }
        }

        @Override
        public void enterNodeStatement(DOTParser.NodeStatementContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitNodeStatement(DOTParser.NodeStatementContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            // read node id
            State s = stack.pop();
            Iterator<State> it = s.children.iterator();
            if (!it.hasNext()) {
                return;
            }
            State nodeIdPartialState = it.next();
            String nodeId = nodeIdPartialState.getId();

            // read attributes
            Map<String, Attribute> attrs = null;
            if (it.hasNext()) {
                attrs = it.next().attrs;
            }
            if (attrs == null) {
                attrs = Collections.emptyMap();
            }

            // create or update vertex
            if (!vertices.contains(nodeId)) {
                SubgraphScope scope = subgraphScopes.element();
                // find default attributes
                Map<String, Attribute> defaultAttrs = new HashMap<>(scope.nodeAttrs);
                // append extra attributes
                defaultAttrs.putAll(attrs);

                if (notifyVertexAttributesOutOfOrder) {
                    notifyVertex(nodeId);
                    for (Entry<String, Attribute> entry : defaultAttrs.entrySet()) {
                        notifyVertexAttribute(nodeId, entry.getKey(), entry.getValue());
                    }
                } else {
                    notifyVertexWithAttributes(nodeId, defaultAttrs);
                }

                vertices.add(nodeId);
                scope.addVertex(nodeId);
            } else {
                for (String key : attrs.keySet()) {
                    notifyVertexAttribute(nodeId, key, attrs.get(key));
                }
            }
            s.addVertex(nodeId);

            // add as child of parent
            s.children.clear();
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterNodeStatementNoAttributes(DOTParser.NodeStatementNoAttributesContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitNodeStatementNoAttributes(DOTParser.NodeStatementNoAttributesContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            // read node id
            State s = stack.pop();
            Iterator<State> it = s.children.iterator();
            if (!it.hasNext()) {
                return;
            }
            State nodeIdPartial = it.next();
            String nodeId = nodeIdPartial.getId();

            // create or update vertex
            if (!vertices.contains(nodeId)) {
                SubgraphScope scope = subgraphScopes.element();
                // find default attributes
                Map<String, Attribute> defaultAttrs = new HashMap<>(scope.nodeAttrs);

                if (notifyVertexAttributesOutOfOrder) {
                    notifyVertex(nodeId);
                    for (Entry<String, Attribute> entry : defaultAttrs.entrySet()) {
                        notifyVertexAttribute(nodeId, entry.getKey(), entry.getValue());
                    }
                } else {
                    notifyVertexWithAttributes(nodeId, defaultAttrs);
                }

                vertices.add(nodeId);
                scope.addVertex(nodeId);
            }
            s.addVertex(nodeId);

            // add as child of parent
            s.children.clear();
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterNodeIdentifier(DOTParser.NodeIdentifierContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitNodeIdentifier(DOTParser.NodeIdentifierContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // collect only first child (ignore ports)
            State s = stack.pop();
            if (!s.children.isEmpty()) {
                s.addId(s.children.getFirst().getId());

                // add as child of parent
                s.children.clear();
                if (!stack.isEmpty()) {
                    stack.element().children.addLast(s);
                }
            }
        }

        @Override
        public void enterSubgraphStatement(DOTParser.SubgraphStatementContext ctx)
        {
            // Create new scope with inherited attributes
            Map<String, Attribute> defaultGraphAttrs = subgraphScopes.element().graphAttrs;
            Map<String, Attribute> defaultNodeAttrs = subgraphScopes.element().nodeAttrs;
            Map<String, Attribute> defaultEdgeAttrs = subgraphScopes.element().edgeAttrs;
            SubgraphScope newState = new SubgraphScope();
            newState.graphAttrs.putAll(defaultGraphAttrs);
            newState.nodeAttrs.putAll(defaultNodeAttrs);
            newState.edgeAttrs.putAll(defaultEdgeAttrs);
            subgraphScopes.push(newState);

            // Add partial state
            State s = new State();
            s.subgraph = newState;
            stack.push(s);
        }

        @Override
        public void exitSubgraphStatement(DOTParser.SubgraphStatementContext ctx)
        {
            if (stack.isEmpty() || subgraphScopes.isEmpty()) {
                return;
            }

            // remove last scope
            SubgraphScope scope = subgraphScopes.pop();
            State s = stack.pop();

            // if not on root graph, append nodes to subgraph one level up
            if (scope.vertices != null && subgraphScopes.size() > 1) {
                subgraphScopes.element().addVertices(scope.vertices);
            }

            // add as child of parent
            s.children.clear();
            if (!stack.isEmpty()) {
                stack.element().children.addLast(s);
            }
        }

        @Override
        public void enterIdentifierPair(DOTParser.IdentifierPairContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitIdentifierPair(DOTParser.IdentifierPairContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // collect our two children as one pair
            State s = stack.pop();
            Iterator<State> it = s.children.iterator();
            if (it.hasNext()) {
                s.addId(it.next().getId());
            }
            if (it.hasNext()) {
                s.addId(it.next().getId());
            }

            if (s.ids != null) {
                // add as child of parent
                s.children.clear();
                if (!stack.isEmpty()) {
                    stack.element().children.addLast(s);
                }
            }
        }

        @Override
        public void enterIdentifier(DOTParser.IdentifierContext ctx)
        {
            // add partial state
            stack.push(new State());
        }

        @Override
        public void exitIdentifier(DOTParser.IdentifierContext ctx)
        {
            if (stack.isEmpty()) {
                return;
            }

            // collect actual identifier
            State s = stack.pop();
            String id = null;
            if (ctx.Id() != null) {
                id = ctx.Id().toString();
            } else if (ctx.String() != null) {
                id = unescapeId(ctx.String().toString());
            } else if (ctx.HtmlString() != null) {
                id = unescapeHtmlString(ctx.HtmlString().toString());
            } else if (ctx.Numeral() != null) {
                id = ctx.Numeral().toString();
            }

            // record id
            if (id != null) {
                s.addId(id);

                // add as child of parent
                if (!stack.isEmpty()) {
                    stack.element().children.addLast(s);
                }
            }
        }
    }

    /*
     * Partial parsed state depending on node type.
     */
    private class State
    {
        LinkedList<State> children;
        List<String> ids;
        Map<String, Attribute> attrs;
        List<String> vertices;
        SubgraphScope subgraph;

        public State()
        {
            this.children = new LinkedList<>();
            this.ids = null;
            this.attrs = null;
            this.vertices = null;
            this.subgraph = null;
        }

        public String getId()
        {
            if (ids == null || ids.isEmpty()) {
                return "";
            } else {
                return ids.get(0);
            }
        }

        public void addId(String id)
        {
            if (this.ids == null) {
                this.ids = new ArrayList<>();
            }
            this.ids.add(id);
        }

        public void put(String key, Attribute value)
        {
            if (this.attrs == null) {
                this.attrs = new HashMap<>();
            }
            this.attrs.put(key, value);
        }

        public void putAll(Map<String, Attribute> attrs)
        {
            if (this.attrs == null) {
                this.attrs = new HashMap<>();
            }
            this.attrs.putAll(attrs);
        }

        public void addVertex(String v)
        {
            if (this.vertices == null) {
                this.vertices = new ArrayList<>();
            }
            this.vertices.add(v);
        }

        public List<String> getVertices()
        {
            if (vertices != null) {
                return vertices;
            } else if (subgraph != null && subgraph.vertices != null) {
                return subgraph.vertices;
            }
            return Collections.emptyList();
        }
    }

    /*
     * Records default attributes per subgraph
     */
    private class SubgraphScope
    {
        Map<String, Attribute> graphAttrs;
        Map<String, Attribute> nodeAttrs;
        Map<String, Attribute> edgeAttrs;
        List<String> vertices;

        public SubgraphScope()
        {
            this.graphAttrs = new HashMap<>();
            this.nodeAttrs = new HashMap<>();
            this.edgeAttrs = new HashMap<>();
            this.vertices = null;
        }

        public void addVertex(String v)
        {
            if (this.vertices == null) {
                this.vertices = new ArrayList<>();
            }
            this.vertices.add(v);
        }

        public void addVertices(List<String> v)
        {
            if (this.vertices == null) {
                this.vertices = new ArrayList<>();
            }
            this.vertices.addAll(v);
        }
    }

    /**
     * Unescape a string DOT identifier.
     *
     * @param input the input
     * @return the unescaped output
     */
    private String unescapeId(String input)
    {
        final char QUOTE = '"';
        if (input.charAt(0) != QUOTE || input.charAt(input.length() - 1) != QUOTE) {
            return input;
        }
        String noQuotes = input.subSequence(1, input.length() - 1).toString();
        String unescaped = UNESCAPE_ID.translate(noQuotes);
        return unescaped;
    }

    /**
     * Unescape an HTML string DOT identifier.
     *
     * @param input the input
     * @return the unescaped output
     */
    private static String unescapeHtmlString(String input)
    {
        if (input.charAt(0) != '<' || input.charAt(input.length() - 1) != '>') {
            return input;
        }
        String noQuotes = input.subSequence(1, input.length() - 1).toString();
        String unescaped = StringEscapeUtils.unescapeXml(noQuotes);
        return unescaped;
    }

}
