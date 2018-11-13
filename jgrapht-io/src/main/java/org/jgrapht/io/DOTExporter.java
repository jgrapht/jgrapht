/*
 * (C) Copyright 2006-2017, by Trevor Harmon and Contributors.
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

import org.jgrapht.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

/**
 * Exports a graph into a DOT file.
 *
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a>.
 * </p>
 * 
 * The user can adjust the behavior using {@link ComponentNameProvider} and
 * {@link ComponentAttributeProvider} instances given through the constructor.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Trevor Harmon
 */
public class DOTExporter<V, E>
    extends
    AbstractBaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    /**
     * Default graph id used by the exporter.
     */
    public static final String DEFAULT_GRAPH_ID = "G";

    private final ComponentNameProvider<Graph<V, E>> graphIDProvider;
    private final ComponentNameProvider<V> vertexLabelProvider;
    private final ComponentNameProvider<E> edgeLabelProvider;
    private final ComponentAttributeProvider<V> vertexAttributeProvider;
    private final ComponentAttributeProvider<E> edgeAttributeProvider;
    private final Map<String, String> graphAttributes;
    private final Map<V, String> vertexIds;

    private static final String INDENT = "  ";

    /**
     * Constructs a new DOTExporter object with an integer name provider for the vertex IDs and null
     * providers for the vertex and edge labels.
     */
    public DOTExporter()
    {
        this(new IntegerComponentNameProvider<>(), null, null, null, null, null);
    }

    /**
     * Constructs a new DOTExporter object with the given ID and label providers.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex labels will not be
     *        written to the file.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels will not be written
     *        to the file.
     */
    public DOTExporter(
        ComponentNameProvider<V> vertexIDProvider, ComponentNameProvider<V> vertexLabelProvider,
        ComponentNameProvider<E> edgeLabelProvider)
    {
        this(vertexIDProvider, vertexLabelProvider, edgeLabelProvider, null, null, null);
    }

    /**
     * Constructs a new DOTExporter object with the given ID, label, and attribute providers. Note
     * that if a label provider conflicts with a label-supplying attribute provider, the label
     * provider is given precedence.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex labels will not be
     *        written to the file (unless an attribute provider is supplied which also supplies
     *        labels).
     * @param edgeLabelProvider for generating edge labels. If null, edge labels will not be written
     *        to the file.
     * @param vertexAttributeProvider for generating vertex attributes. If null, vertex attributes
     *        will not be written to the file.
     * @param edgeAttributeProvider for generating edge attributes. If null, edge attributes will
     *        not be written to the file.
     */
    public DOTExporter(
        ComponentNameProvider<V> vertexIDProvider, ComponentNameProvider<V> vertexLabelProvider,
        ComponentNameProvider<E> edgeLabelProvider,
        ComponentAttributeProvider<V> vertexAttributeProvider,
        ComponentAttributeProvider<E> edgeAttributeProvider)
    {
        this(
            vertexIDProvider, vertexLabelProvider, edgeLabelProvider, vertexAttributeProvider,
            edgeAttributeProvider, null);
    }

    /**
     * Constructs a new DOTExporter object with the given ID, label, attribute, and graph id
     * providers. Note that if a label provider conflicts with a label-supplying attribute provider,
     * the label provider is given precedence.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex labels will not be
     *        written to the file (unless an attribute provider is supplied which also supplies
     *        labels).
     * @param edgeLabelProvider for generating edge labels. If null, edge labels will not be written
     *        to the file.
     * @param vertexAttributeProvider for generating vertex attributes. If null, vertex attributes
     *        will not be written to the file.
     * @param edgeAttributeProvider for generating edge attributes. If null, edge attributes will
     *        not be written to the file.
     * @param graphIDProvider for generating the graph ID. If null the graph is named
     *        {@link #DEFAULT_GRAPH_ID}.
     */
    public DOTExporter(
        ComponentNameProvider<V> vertexIDProvider, ComponentNameProvider<V> vertexLabelProvider,
        ComponentNameProvider<E> edgeLabelProvider,
        ComponentAttributeProvider<V> vertexAttributeProvider,
        ComponentAttributeProvider<E> edgeAttributeProvider,
        ComponentNameProvider<Graph<V, E>> graphIDProvider)
    {
        super(vertexIDProvider);
        this.vertexLabelProvider = vertexLabelProvider;
        this.edgeLabelProvider = edgeLabelProvider;
        this.vertexAttributeProvider = vertexAttributeProvider;
        this.edgeAttributeProvider = edgeAttributeProvider;
        this.graphIDProvider =
            (graphIDProvider == null) ? any -> DEFAULT_GRAPH_ID : graphIDProvider;
        this.graphAttributes = new LinkedHashMap<>();
        this.vertexIds = new HashMap<>();
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
        for (Entry<String, String> attr : graphAttributes.entrySet()) {
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

            String labelName = null;
            if (vertexLabelProvider != null) {
                labelName = vertexLabelProvider.getName(v);
            }
            Map<String, Attribute> attributes = null;
            if (vertexAttributeProvider != null) {
                attributes = vertexAttributeProvider.getComponentAttributes(v);
            }
            renderAttributes(out, labelName, attributes);

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

            String labelName = null;
            if (edgeLabelProvider != null) {
                labelName = edgeLabelProvider.getName(e);
            }
            Map<String, Attribute> attributes = null;
            if (edgeAttributeProvider != null) {
                attributes = edgeAttributeProvider.getComponentAttributes(e);
            }
            renderAttributes(out, labelName, attributes);

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
     * Clear a graph attribute.
     *
     * @param key the graph attribute key
     */
    public void removeGraphAttribute(String key)
    {
        Objects.requireNonNull(key, "Graph attribute key cannot be null");
        graphAttributes.remove(key);
    }

    /**
     * Set a graph attribute.
     *
     * @param key the graph attribute key
     * @param value the graph attribute value
     */
    public void putGraphAttribute(String key, String value)
    {
        Objects.requireNonNull(key, "Graph attribute key cannot be null");
        Objects.requireNonNull(value, "Graph attribute value cannot be null");
        graphAttributes.put(key, value);
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
        String graphId = graphIDProvider.getName(graph);
        if (graphId == null || graphId.trim().isEmpty()) {
            graphId = DEFAULT_GRAPH_ID;
        }
        if (!DOTUtils.isValidID(graphId)) {
            throw new RuntimeException(
                "Generated graph ID '" + graphId
                    + "' is not valid with respect to the .dot language");
        }
        return graphId;
    }

    private void renderAttributes(
        PrintWriter out, String labelName, Map<String, Attribute> attributes)
    {
        if (labelName == null && attributes == null) {
            return;
        }
        out.print(" [ ");
        final Attribute labelAttribute;
        if (labelName != null) {
            labelAttribute = DefaultAttribute.createAttribute(labelName);
        } else {
            labelAttribute = attributes.get("label");
        }
        if (labelAttribute != null) {
            renderAttribute(out, "label", labelAttribute);
        }
        if (attributes != null) {
            for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
                String name = entry.getKey();
                if (name.equals("label")) {
                    // already handled by special case above
                    continue;
                }
                renderAttribute(out, name, entry.getValue());
            }
        }
        out.print("]");
    }

    private void renderAttribute(PrintWriter out, String attrName, Attribute attribute)
    {
        out.print(attrName + "=");
        final String attrValue = attribute.getValue();
        if (AttributeType.HTML.equals(attribute.getType())) {
            out.print("<" + attrValue + ">");
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
     * http://www.graphviz.org/doc/info/lang.html Quoted from above mentioned source: An ID is valid
     * if it meets one of the following criteria:
     *
     * <ul>
     * <li>any string of alphabetic characters, underscores or digits, not beginning with a digit;
     * <li>a number [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
     * <li>any double-quoted string ("...") possibly containing escaped quotes (\");
     * <li>an HTML string (<...>).
     * </ul>
     *
     * @throws RuntimeException if the given <code>vertexIDProvider</code> didn't generate a valid
     *         vertex ID.
     */
    private String getVertexID(V v)
    {
        String vertexId = vertexIds.get(v);
        if (vertexId == null) {
            /*
             * use the associated id provider for an ID of the given vertex
             */
            String idCandidate = vertexIDProvider.getName(v);

            /*
             * test if it is a valid ID
             */
            if (!DOTUtils.isValidID(idCandidate)) {
                throw new RuntimeException(
                    "Generated id '" + idCandidate + "'for vertex '" + v
                        + "' is not valid with respect to the .dot language");
            }

            vertexIds.put(v, idCandidate);
            vertexId = idCandidate;
        }
        return vertexId;
    }

}
