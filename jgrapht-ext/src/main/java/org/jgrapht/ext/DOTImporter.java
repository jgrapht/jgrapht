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
/* ------------------
 * DOTImporter.java
 * ------------------
 * (C) Copyright 2015-2016, by  Wil Selwood and Contributors.
 *
 * Original Author:  Wil Selwood <wselwood@ijento.com>
 * Contributor(s): Dimitrios Michail
 * 
 * Changes
 * -------
 * 2015 : Initial revision (WS);
 * 19-Aug-2016: Always provide id to vertex providers (DM);
 *
 */
package org.jgrapht.ext;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

/**
 * Imports a graph from a DOT file.
 *
 * <p>For a description of the format see <a
 * href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a> and <a
 * href="http://www.graphviz.org/doc/info/lang.html">
 * http://www.graphviz.org/doc/info/lang.html</a></p>
 *
 * state machine description (In dot format naturally):
 *
 * <pre><code>
 *
 * digraph G {
 *    1 [label="start" description="Entry point"];
 *    2 [label="header" description="Processing The header"];
 *    3 [label="next" description="work out what the type of the next node is"];
 *    4 [label="edge" description="process an edge entry"];
 *    5 [label="edge_quotes" description="process a section of an edge in quotes"];
 *    6 [label="node" description="process a node entry"];
 *    7 [label="node_quotes" description="process a section of a node in quotes"];
 *    8 [label="line_comment" description="process and ignore a line comment"];
 *    9 [label="block_comment" description="process and ignore a block comment"];
 *    10 [label="done" description="exit point"];
 *    1 -&gt; 2;
 *    2 -&gt; 3;
 *    3 -&gt; 4;
 *    4 -&gt; 3;
 *    4 -&gt; 5;
 *    5 -&gt; 4;
 *    3 -&gt; 6;
 *    6 -&gt; 3;
 *    6 -&gt; 7;
 *    7 -&gt; 6;
 *    3 -&gt; 10;
 *    2 -&gt; 8;
 *    8 -&gt; 2;
 *    2 -&gt; 9;
 *    9 -&gt; 2;
 *    3 -&gt; 8;
 *    8 -&gt; 3;
 *    3 -&gt; 9;
 *    9 -&gt; 3;
 *    4 -&gt; 8;
 *    8 -&gt; 4;
 *    4 -&gt; 9;
 *    9 -&gt; 4;
 *    6 -&gt; 8;
 *    8 -&gt; 6;
 *    6 -&gt; 9;
 *    9 -&gt; 6;
 * }
 *
 * </code></pre>
 *
 * @author Wil Selwood
 */
public class DOTImporter<V, E>
    implements GraphImporter<V, E>
{
    // Constants for the state machine
    private static final int HEADER = 1;
    private static final int NODE = 2;
    private static final int EDGE = 3;
    private static final int LINE_COMMENT = 4;
    private static final int BLOCK_COMMENT = 5;
    private static final int NODE_QUOTES = 6;
    private static final int EDGE_QUOTES = 7;
    private static final int NEXT = 8;
    private static final int DONE = 32;

    private VertexProvider<V> vertexProvider;
    private VertexUpdater<V> vertexUpdater;
    private EdgeProvider<V, E> edgeProvider;

    /**
     * Constructs a new DOTImporter with the given providers
     *
     * @param vertexProvider Provider to create a vertex
     * @param edgeProvider Provider to create an edge
     */
    public DOTImporter(
        VertexProvider<V> vertexProvider,
        EdgeProvider<V, E> edgeProvider)
    {
        this.vertexProvider = vertexProvider;
        this.vertexUpdater = null;
        this.edgeProvider = edgeProvider;
    }

    /**
     * Constructs a new DOTImporter with the given providers
     *
     * @param vertexProvider Provider to create a vertex
     * @param edgeProvider Provider to create an edge
     * @param updater Method used to update an existing Vertex
     */
    public DOTImporter(
        VertexProvider<V> vertexProvider,
        EdgeProvider<V, E> edgeProvider,
        VertexUpdater<V> updater)
    {
        this.vertexProvider = vertexProvider;
        this.vertexUpdater = updater;
        this.edgeProvider = edgeProvider;
    }

    /**
     * Read a dot formatted input and populate the provided graph.
     * 
     * The current implementation reads the whole input as a string and then
     * parses the graph.
     *
     * @param graph the graph to update
     * @param input the input reader
     *
     * @throws ImportException if there is a problem parsing the file.
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
        throws ImportException
    {
        BufferedReader br;
        if (input instanceof BufferedReader) {
            br = (BufferedReader) input;
        } else {
            br = new BufferedReader(input);
        }
        read(br.lines().collect(Collectors.joining("\n")), (Graph<V, E>) graph);
    }

    /**
     * Read a dot formatted string and populate the provided graph.
     *
     * @param input the content of a dot file as a string
     * @param graph the graph to update
     *
     * @throws ImportException if there is a problem parsing the file.
     */
    @Deprecated
    public void read(String input, AbstractBaseGraph<V, E> graph)
        throws ImportException
    {
        read(input, (Graph<V,E>) graph);
    }
    
    /**
     * Read a dot formatted string and populate the provided graph.
     *
     * @param input the content of a dot file.
     * @param graph the graph to update.
     *
     * @throws ImportException if there is a problem parsing the file.
     */
    private void read(String input, Graph<V, E> graph)
        throws ImportException
    {
        if ((input == null) || input.isEmpty()) {
            throw new ImportException("Dot string was empty");
        }

        Map<String, V> vertexes = new HashMap<>();

        int state = HEADER;
        int lastState = HEADER;
        int position = 0;

        StringBuilder sectionBuffer = new StringBuilder();

        while ((state != DONE) && (position < input.length())) {
            int existingState = state;
            switch (state) {
            case HEADER:
                state = processHeader(input, position, sectionBuffer, graph);
                break;
            case NODE:
                state =
                    processNode(
                        input,
                        position,
                        sectionBuffer,
                        graph,
                        vertexes);
                break;
            case EDGE:
                state =
                    processEdge(
                        input,
                        position,
                        sectionBuffer,
                        graph,
                        vertexes);
                break;
            case LINE_COMMENT:
                state =
                    processLineComment(
                        input,
                        position,
                        sectionBuffer,
                        lastState);
                if (state == lastState) {
                    // when we leave a line comment we need the new line to
                    // still appear in the old block
                    position = position - 1;
                }
                break;
            case BLOCK_COMMENT:
                state = processBlockComment(input, position, lastState);
                break;
            case NODE_QUOTES:
                state = processNodeQuotes(input, position, sectionBuffer);
                break;
            case EDGE_QUOTES:
                state = processEdgeQuotes(input, position, sectionBuffer);
                break;
            case NEXT:
                state =
                    processNext(
                        input,
                        position,
                        sectionBuffer,
                        graph,
                        vertexes);
                break;

            // DONE not included here as we can't get to it with the while loop.
            default:
                throw new ImportException(
                    "Error importing escaped state machine");
            }

            position = position + 1;

            if (state != existingState) {
                lastState = existingState;
            }
        }

        // if we get to the end and are some how still in the header the input
        // must be invalid.
        if (state == HEADER) {
            throw new ImportException("Invalid Header");
        }
    }

    /**
     * Process the header block.
     *
     * @param input the input string to read from.
     * @param position how far along the input string we are.
     * @param sectionBuffer Current buffer.
     * @param graph the graph we are updating
     *
     * @return the new state.
     *
     * @throws ImportException if there is a problem with the header section.
     */
    private int processHeader(
        String input,
        int position,
        StringBuilder sectionBuffer,
        Graph<V, E> graph)
        throws ImportException
    {
        if (isStartOfLineComment(input, position)) {
            return LINE_COMMENT;
        }

        if (isStartOfBlockComment(input, position)) {
            return BLOCK_COMMENT;
        }

        char current = input.charAt(position);
        sectionBuffer.append(current);
        if (current == '{') {
            // reached the end of the header. Validate it.

            String [] headerParts = sectionBuffer.toString().split(" ", 4);
            if (headerParts.length < 3) {
                throw new ImportException("Not enough parts in header");
            }

            int i = 0;
            if (graph instanceof AbstractBaseGraph
                && ((AbstractBaseGraph<V, E>) graph).isAllowingMultipleEdges()
                && headerParts[i].equals("strict"))
            {
                throw new ImportException(
                    "graph defines strict but Multigraph given.");
            } else if (headerParts[i].equals("strict")) {
                i = i + 1;
            }

            if ((graph instanceof DirectedGraph)
                && headerParts[i].equals("graph"))
            {
                throw new ImportException(
                    "input asks for undirected graph and directed graph provided.");
            } else if (
                !(graph instanceof DirectedGraph)
                && headerParts[i].equals("digraph"))
            {
                throw new ImportException(
                    "input asks for directed graph but undirected graph provided.");
            } else if (
                !headerParts[i].equals("graph")
                && !headerParts[i].equals("digraph"))
            {
                throw new ImportException("unknown graph type");
            }

            sectionBuffer.setLength(0); //reset the buffer.
            return NEXT;
        }
        return HEADER;
    }

    /**
     * When we start a new section of the graph we don't know what it is going
     * to be. We work in here until we can work out what type of section this
     * is.
     *
     * @param input the input string to read from.
     * @param position how far into the string we have got.
     * @param sectionBuffer the current section.
     * @param graph the graph we are creating.
     * @param vertexes the existing set of vertexes that have been created so
     * far.
     *
     * @return the next state.
     *
     * @throws ImportException if there is a problem with creating a node.
     */
    private int processNext(
        String input,
        int position,
        StringBuilder sectionBuffer,
        Graph<V, E> graph,
        Map<String, V> vertexes)
        throws ImportException
    {
        if (isStartOfLineComment(input, position)) {
            return LINE_COMMENT;
        }

        if (isStartOfBlockComment(input, position)) {
            return BLOCK_COMMENT;
        }

        char current = input.charAt(position);

        // ignore new line characters or section breaks between identified
        // sections.
        if ((current == '\n') || (current == '\r')) {
            return NEXT;
        }

        // if the buffer is currently empty skip spaces too.
        if ((sectionBuffer.length() == 0)
            && ((current == ' ') || (current == ';')))
        {
            return NEXT;
        }

        // If we have a semi colon and some thing in the buffer we must be at
        // the end of a block. as we can't have had a dash yet we must be at the
        // end of a node.
        if (current == ';') {
            processCompleteNode(sectionBuffer.toString(), graph, vertexes);
            sectionBuffer.setLength(0);
            return NEXT;
        }

        sectionBuffer.append(input.charAt(position));
        if (position < (input.length() - 1)) {
            char next = input.charAt(position + 1);
            if (current == '-') {
                if ((next == '-') && (graph instanceof DirectedGraph)) {
                    throw new ImportException(
                        "graph is directed but undirected edge found");
                } else if ((next == '>') && !(graph instanceof DirectedGraph)) {
                    throw new ImportException(
                        "graph is undirected but directed edge found");
                } else if ((next == '-') || (next == '>')) {
                    return EDGE;
                }
            }
        }

        if (current == '[') {
            return
                NODE; // if this was an edge we should have found a dash before
                      // here.
        }

        return NEXT;
    }

    /**
     * Process a node entry. When we detect that we are at the end of the node
     * create it in the graph.
     *
     * @param input the input string to read from.
     * @param position how far into the string we have got.
     * @param sectionBuffer the current section.
     * @param graph the graph we are creating.
     * @param vertexes the existing set of vertexes that have been created so
     * far.
     *
     * @return the next state.
     *
     * @throws ImportException if there is a problem with creating a node.
     */
    private int processNode(
        String input,
        int position,
        StringBuilder sectionBuffer,
        Graph<V, E> graph,
        Map<String, V> vertexes)
        throws ImportException
    {
        if (isStartOfLineComment(input, position)) {
            return LINE_COMMENT;
        }

        if (isStartOfBlockComment(input, position)) {
            return BLOCK_COMMENT;
        }

        char current = input.charAt(position);
        sectionBuffer.append(input.charAt(position));
        if (current == '"') {
            return NODE_QUOTES;
        }
        if ((current == ']') || (current == ';')) {
            processCompleteNode(sectionBuffer.toString(), graph, vertexes);
            sectionBuffer.setLength(0);
            return NEXT;
        }

        return NODE;
    }

    /**
     * Process a quoted section of a node entry. This skips most of the exit
     * conditions so quoted strings can contain comments, semi colons, dashes,
     * newlines and so on.
     *
     * @param input the input string to read from.
     * @param position how far into the string we have got.
     * @param sectionBuffer the current section.
     *
     * @return the state for the next character.
     */
    private int processNodeQuotes(
        String input,
        int position,
        StringBuilder sectionBuffer)
    {
        char current = input.charAt(position);
        sectionBuffer.append(input.charAt(position));

        if (current == '"') {
            if (input.charAt(position - 1) != '\\') {
                return NODE;
            }
        }
        return NODE_QUOTES;
    }

    private int processEdge(
        String input,
        int position,
        StringBuilder sectionBuffer,
        Graph<V, E> graph,
        Map<String, V> vertexes)
        throws ImportException
    {
        if (isStartOfLineComment(input, position)) {
            return LINE_COMMENT;
        }

        if (isStartOfBlockComment(input, position)) {
            return BLOCK_COMMENT;
        }

        char current = input.charAt(position);
        sectionBuffer.append(input.charAt(position));
        if (current == '"') {
            return EDGE_QUOTES;
        }

        if ((current == ';') || (current == '\r') || (current == '\n')) {
            processCompleteEdge(sectionBuffer.toString(), graph, vertexes);
            sectionBuffer.setLength(0);
            return NEXT;
        }

        return EDGE;
    }

    private int processEdgeQuotes(
        String input,
        int position,
        StringBuilder sectionBuffer)
    {
        char current = input.charAt(position);
        sectionBuffer.append(input.charAt(position));

        if (current == '"') {
            if (input.charAt(position - 1) != '\\') {
                return EDGE;
            }
        }
        return EDGE_QUOTES;
    }

    private int processLineComment(
        String input,
        int position,
        StringBuilder sectionBuffer,
        int returnState)
    {
        char current = input.charAt(position);
        if ((current == '\r') || (current == '\n')) {
            sectionBuffer.append(current);
            return returnState;
        }

        return LINE_COMMENT;
    }

    private int processBlockComment(String input, int position, int returnState)
    {
        char current = input.charAt(position);
        if (current == '/') {
            if (input.charAt(position - 1) == '*') {
                return returnState;
            }
        }

        return BLOCK_COMMENT;
    }

    private boolean isStartOfLineComment(String input, int position)
    {
        char current = input.charAt(position);
        if (current == '#') {
            return true;
        } else if (current == '/') {
            if (position < (input.length() - 1)) {
                if (input.charAt(position + 1) == '/') {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isStartOfBlockComment(String input, int position)
    {
        char current = input.charAt(position);
        if (current == '/') {
            if (position < (input.length() - 1)) {
                if (input.charAt(position + 1) == '*') {
                    return true;
                }
            }
        }
        return false;
    }

    private void processCompleteNode(
        String node,
        Graph<V, E> graph,
        Map<String, V> vertexes)
        throws ImportException
    {
        Map<String, String> attributes = extractAttributes(node);

        String id = node.trim();
        int bracketIndex = node.indexOf('[');
        if (bracketIndex > 0) {
            id = node.substring(0, node.indexOf('[')).trim();
        }

        V existing = vertexes.get(id);
        if (existing == null) {
            V vertex = vertexProvider.buildVertex(id, attributes);
            graph.addVertex(vertex);
            vertexes.put(id, vertex);
        } else {
            if (vertexUpdater != null) {
                vertexUpdater.updateVertex(existing, attributes);
            } else {
                throw new ImportException(
                    "Update required for vertex "
                    + id
                    + " but no vertexUpdater provided");
            }
        }
    }

    private void processCompleteEdge(
        String edge,
        Graph<V, E> graph,
        Map<String, V> vertexes)
        throws ImportException
    {
        Map<String, String> attributes = extractAttributes(edge);

        List<String> ids = extractEdgeIds(edge);

        // for each pair of ids in the list create an edge.
        for (int i = 0; i < (ids.size() - 1); i++) {
            V v1 = getVertex(ids.get(i), vertexes, graph);
            V v2 = getVertex(ids.get(i + 1), vertexes, graph);

            E resultEdge =
                edgeProvider.buildEdge(
                    v1,
                    v2,
                    attributes.get("label"),
                    attributes);
            graph.addEdge(v1, v2, resultEdge);
        }
    }

    // if a vertex id doesn't already exist create one for it
    // with no attributes.
    private V getVertex(String id, Map<String, V> vertexes, Graph<V, E> graph)
    {
        V v = vertexes.get(id);
        if (v == null) {
            v = vertexProvider.buildVertex(id, new HashMap<>());
            graph.addVertex(v);
            vertexes.put(id, v);
        }
        return v;
    }

    private List<String> extractEdgeIds(String line)
    {
        String idChunk = line.trim();
        if (idChunk.endsWith(";")) {
            idChunk = idChunk.substring(0, idChunk.length() - 1);
        }
        int bracketIndex = idChunk.indexOf('[');
        if (bracketIndex > 1) {
            idChunk = idChunk.substring(0, bracketIndex).trim();
        }
        int index = 0;
        List<String> ids = new ArrayList<>();
        while (index < idChunk.length()) {
            int nextSpace = idChunk.indexOf(' ', index);
            String chunk;
            if (nextSpace > 0) { // is this the last chunk
                chunk = idChunk.substring(index, nextSpace);
                index = nextSpace + 1;
            } else {
                chunk = idChunk.substring(index);
                index = idChunk.length() + 1;
            }
            if (!chunk.equals("--") && !chunk.equals("->")) { // a label then?
                ids.add(chunk);
            }
        }

        return ids;
    }

    private Map<String, String> extractAttributes(String line)
        throws ImportException
    {
        Map<String, String> attributes = new HashMap<>();
        int bracketIndex = line.indexOf("[");
        if (bracketIndex > 0) {
            attributes =
                splitAttributes(
                    line.substring(bracketIndex + 1, line.lastIndexOf(']'))
                        .trim());
        }
        return attributes;
    }

    private Map<String, String> splitAttributes(String input)
        throws ImportException
    {
        int index = 0;
        Map<String, String> result = new HashMap<>();
        while (index < input.length()) {
            // skip any leading white space
            index = skipWhiteSpace(input, index);

            // Now check for quotes
            int endOfKey = findEndOfSection(input, index, '=');
            if (endOfKey < 0) {
                throw new ImportException("Invalid attributes");
            }
            if (input.charAt(endOfKey) == '"') {
                index = index + 1;
            }

            String key = input.substring(index, endOfKey).trim();

            if ((endOfKey + 1) >= input.length()) {
                throw new ImportException("Invalid attributes");
            }

            // Attribute value may be quoted or a single word.
            // First ignore any white space before the start
            int start = skipWhiteSpace(input, endOfKey + 1);

            int endChar = findEndOfSection(input, start, ' ');
            if (input.charAt(start) == '"') {
                start = start + 1;
            }

            if (endChar < 0) {
                endChar = input.length();
            }

            String value = input.substring(start, endChar);
            result.put(key, value);
            index = endChar + 1;
        }
        return result;
    }

    private int skipWhiteSpace(String input, int start)
        throws ImportException
    {
        int i = 0;
        while (
            Character.isWhitespace(input.charAt(start + i))
            || (input.charAt(start + i) == '='))
        {
            i = i + 1;
            if ((start + i) >= input.length()) {
                throw new ImportException("Invalid attributes");
            }
        }

        return start + i;
    }

    private int findEndOfSection(String input, int start, char terminator)
    {
        if (input.charAt(start) == '"') {
            return findNextQuote(input, start);
        } else {
            return input.indexOf(terminator, start);
        }
    }

    private int findNextQuote(String input, int start)
    {
        int result = start;
        do {
            result = input.indexOf('\"', result + 1);
            // if the previous character is an escape then keep going
        } while (
            (input.charAt(result - 1) == '\\')
            && !((input.charAt(result - 1) == '\\')
                && (input.charAt(result - 2) == '\\'))); // unless its escaped
        return result;
    }
}

// End DOTImporter.java
