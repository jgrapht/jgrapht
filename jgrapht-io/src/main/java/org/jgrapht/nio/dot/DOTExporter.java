/*
 * (C) Copyright 2006-2021, by Trevor Harmon and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;
import java.util.regex.*;

/**
 * Exports a graph into a DOT file.
 *
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a>.
 * </p>
 * 
 * The user can adjust the behavior using the various providers.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Trevor Harmon
 * @author Dimitrios Michail
 */
public class DOTExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    /**
     * Default graph id used by the exporter.
     */
    public static final String DEFAULT_GRAPH_ID = "G";

    private static final String INDENT = "  ";

    private final Map<V, String> validatedIds;

    /**
     * Constructs a new DOTExporter object with an integer id provider.
     */
    public DOTExporter()
    {
        this(new IntegerIdProvider<>());
    }

    /**
     * Constructs a new DOTExporter object with the given id provider. Additional providers such as
     * attributes can be given using the appropriate setter methods.
     *
     * @param vertexIdProvider for generating vertex IDs. Must not be null.
     */
    public DOTExporter(Function<V, String> vertexIdProvider)
    {
        super(vertexIdProvider);
        this.validatedIds = new HashMap<>();
    }

    /**
     * Exports a graph into a plain text file in DOT format.
     *
     * @param g the graph to be exported
     * @param writer the writer to which the graph to be exported
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        out.println(computeHeader(g));

        // graph attributes
        for (Entry<String, Attribute> attr : graphAttributeProvider
            .orElse(Collections::emptyMap).get().entrySet())
        {
            out.print(INDENT);
            out.print(attr.getKey());
            out.print('=');
            out.print(attr.getValue());
            out.println(";");
        }

        // vertex set
        for (V v : g.vertexSet()) {
            out.print(INDENT);
            out.print(getVertexID(v));

            getVertexAttributes(v).ifPresent(m -> {
                renderAttributes(out, m);
            });

            out.println(";");
        }

        String connector = computeConnector(g);

        // edge set
        for (E e : g.edgeSet()) {
            String source = getVertexID(g.getEdgeSource(e));
            String target = getVertexID(g.getEdgeTarget(e));

            out.print(INDENT);
            out.print(source);
            out.print(connector);
            out.print(target);

            getEdgeAttributes(e).ifPresent(m -> {
                renderAttributes(out, m);
            });

            out.println(";");
        }

        out.println(computeFooter(g));

        out.flush();
    }

    /**
     * Compute the header
     * 
     * @param graph the graph
     * @return the header
     */
    private String computeHeader(Graph<V, E> graph)
    {
        StringBuilder headerBuilder = new StringBuilder();
        if (!graph.getType().isAllowingMultipleEdges()) {
            headerBuilder.append(DOTUtils.DONT_ALLOW_MULTIPLE_EDGES_KEYWORD).append(" ");
        }
        if (graph.getType().isDirected()) {
            headerBuilder.append(DOTUtils.DIRECTED_GRAPH_KEYWORD);
        } else {
            headerBuilder.append(DOTUtils.UNDIRECTED_GRAPH_KEYWORD);
        }
        headerBuilder.append(" ").append(computeGraphId(graph)).append(" {");
        return headerBuilder.toString();
    }

    /**
     * Compute the footer
     * 
     * @param graph the graph
     * @return the footer
     */
    private String computeFooter(Graph<V, E> graph)
    {
        return "}";
    }

    /**
     * Compute the connector
     * 
     * @param graph the graph
     * @return the connector
     */
    private String computeConnector(Graph<V, E> graph)
    {
        StringBuilder connectorBuilder = new StringBuilder();
        if (graph.getType().isDirected()) {
            connectorBuilder.append(" ").append(DOTUtils.DIRECTED_GRAPH_EDGEOP).append(" ");
        } else {
            connectorBuilder.append(" ").append(DOTUtils.UNDIRECTED_GRAPH_EDGEOP).append(" ");
        }
        return connectorBuilder.toString();
    }

    /**
     * Get the id of the graph.
     * 
     * @param graph the graph
     * @return the graph id
     */
    private String computeGraphId(Graph<V, E> graph)
    {
        String graphId = getGraphId().orElse(DEFAULT_GRAPH_ID);
        if (!DOTUtils.isValidID(graphId)) {
            throw new ExportException(
                "Generated graph ID '" + graphId
                    + "' is not valid with respect to the .dot language");
        }
        return graphId;
    }

    private void renderAttributes(PrintWriter out, Map<String, Attribute> attributes)
    {
        if (attributes == null) {
            return;
        }
        out.print(" [ ");
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            String name = entry.getKey();
            renderAttribute(out, name, entry.getValue());
        }
        out.print("]");
    }

    private void renderAttribute(PrintWriter out, String attrName, Attribute attribute)
    {
        out.print(attrName + "=");
        final String attrValue = attribute.getValue();
        if (AttributeType.HTML.equals(attribute.getType())) {
            out.print("<" + attrValue + ">");
        } else if (AttributeType.IDENTIFIER.equals(attribute.getType())) {
            out.print(attrValue);
        } else {
            out.print("\"" + escapeDoubleQuotes(attrValue) + "\"");
        }
        out.print(" ");
    }

    private static String escapeDoubleQuotes(String labelName)
    {
        return labelName.replaceAll("\"", Matcher.quoteReplacement("\\\""));
    }

    /**
     * Return a valid vertex ID (with respect to the .dot language definition as described in
     * http://www.graphviz.org/doc/info/lang.html
     * 
     * <p>
     * Quoted from above mentioned source: An ID is valid if it meets one of the following criteria:
     *
     * <ul>
     * <li>any string of alphabetic characters, underscores or digits, not beginning with a digit;
     * <li>a number [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
     * <li>any double-quoted string ("...") possibly containing escaped quotes (\");
     * <li>an HTML string (<...>).
     * </ul>
     *
     * @throws ExportException if the given <code>vertexIDProvider</code> didn't generate a valid
     *         vertex ID.
     */
    private String getVertexID(V v)
    {
        String vertexId = validatedIds.get(v);
        if (vertexId == null) {
            /*
             * use the associated id provider for an ID of the given vertex
             */
            vertexId = getVertexId(v);

            /*
             * test if it is a valid ID
             */
            if (!DOTUtils.isValidID(vertexId)) {
                throw new ExportException(
                    "Generated id '" + vertexId + "'for vertex '" + v
                        + "' is not valid with respect to the .dot language");
            }

            validatedIds.put(v, vertexId);
        }
        return vertexId;
    }

}
