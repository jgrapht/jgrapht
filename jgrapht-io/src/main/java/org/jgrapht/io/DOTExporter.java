/*
 * (C) Copyright 2006-2017, by Trevor Harmon and Contributors.
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
import java.util.Map.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Exports a graph into a DOT file.
 *
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a>.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Trevor Harmon
 */
public class DOTExporter<V, E>
    extends AbstractBaseExporter<V, E>
    implements GraphExporter<V, E>
{
    /**
     * Default graph id used by the exporter.
     */
    public static final String DEFAULT_GRAPH_ID = "G";

    private ComponentNameProvider<Graph<V, E>> graphIDProvider;
    private ComponentNameProvider<V> vertexLabelProvider;
    private ComponentNameProvider<E> edgeLabelProvider;
    private ComponentAttributeProvider<V> vertexAttributeProvider;
    private ComponentAttributeProvider<E> edgeAttributeProvider;
    private Map<String, String> graphAttributes;

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
        String indent = "  ";
        String connector;
        String header = (g instanceof AbstractBaseGraph
            && !((AbstractBaseGraph<V, E>) g).isAllowingMultipleEdges())
                ? DOTUtils.DONT_ALLOW_MULTIPLE_EDGES_KEYWORD + " " : "";
        String graphId = graphIDProvider.getName(g);
        if (graphId == null || graphId.trim().isEmpty()) {
            graphId = DEFAULT_GRAPH_ID;
        }
        if (!DOTUtils.isValidID(graphId)) {
            throw new RuntimeException(
                "Generated graph ID '" + graphId
                    + "' is not valid with respect to the .dot language");
        }
        if (g.getType().isDirected()) {
            header += DOTUtils.DIRECTED_GRAPH_KEYWORD;
            connector = " " + DOTUtils.DIRECTED_GRAPH_EDGEOP + " ";
        } else {
            header += DOTUtils.UNDIRECTED_GRAPH_KEYWORD;
            connector = " " + DOTUtils.UNDIRECTED_GRAPH_EDGEOP + " ";
        }
        header += " " + graphId + " {";
        out.println(header);

        // graph attributes
        for(Entry<String, String> attr: graphAttributes.entrySet()) {
            out.print(indent);
            out.print(attr.getKey());
            out.print('=');
            out.print(attr.getValue());
            out.println(";");
        }

        // vertex set
        for (V v : g.vertexSet()) {
            out.print(indent + getVertexID(v));

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

        // edge set
        for (E e : g.edgeSet()) {
            String source = getVertexID(g.getEdgeSource(e));
            String target = getVertexID(g.getEdgeTarget(e));

            out.print(indent + source + connector + target);

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

        out.println("}");

        out.flush();
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

    private void renderAttributes(PrintWriter out, String labelName, Map<String, Attribute> attributes)
    {
        if (labelName == null && attributes == null) {
            return;
        }
        out.print(" [ ");
        if (labelName == null) {
            Attribute labelAttribute = attributes.get("label");
            if (labelAttribute != null) { 
                labelName = labelAttribute.getValue();
            }
        }
        if (labelName != null) {
            out.print("label=\"" + labelName + "\" ");
        }
        if (attributes != null) {
            for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
                String name = entry.getKey();
                if (name.equals("label")) {
                    // already handled by special case above
                    continue;
                }
                out.print(name + "=\"" + entry.getValue().getValue() + "\" ");
            }
        }
        out.print("]");
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
        // TODO jvs 28-Jun-2008: possible optimizations here are
        // (a) only validate once per vertex

        // use the associated id provider for an ID of the given vertex
        String idCandidate = vertexIDProvider.getName(v);

        // test if it is a valid ID
        if (DOTUtils.isValidID(idCandidate)) {
            return idCandidate;
        }

        throw new RuntimeException(
            "Generated id '" + idCandidate + "'for vertex '" + v
                + "' is not valid with respect to the .dot language");
    }

}

// End DOTExporter.java
