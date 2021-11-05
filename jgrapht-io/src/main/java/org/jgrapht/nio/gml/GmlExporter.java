/*
 * (C) Copyright 2006-2021, by Dimitrios Michail and Contributors.
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

import org.apache.commons.text.*;
import org.jgrapht.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Exports a graph into a GML file (Graph Modeling Language).
 *
 * <p>
 * For a description of the format see <a href=
 * "https://github.com/GunterMueller/UNI_PASSAU_FMI_Graph_Drawing/blob/master/GML/gml-technical-report.pdf">
 * https://github.com/GunterMueller/UNI_PASSAU_FMI_Graph_Drawing/blob/master/GML/gml-technical-report.pdf</a>.
 * 
 * <p>
 * The behavior of the exporter such as whether to print vertex labels, edge labels, and/or edge
 * weights can be adjusted using the {@link #setParameter(Parameter, boolean) setParameter} method.
 * When exporting labels, the exporter escapes them as Java strings.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class GmlExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private static final String CREATOR = "JGraphT GML Exporter";
    private static final String VERSION = "1";

    private static final String DELIM = " ";
    private static final String TAB1 = "\t";
    private static final String TAB2 = "\t\t";

    private static final String LABEL_ATTRIBUTE_KEY = "label";
    private static final String WEIGHT_ATTRIBUTE_KEY = "weight";

    private static final Set<String> FORBIDDEN_VERTEX_CUSTOM_ATTRIBUTE_KEYS = Set.of("id");
    private static final Set<String> FORBIDDEN_EDGE_CUSTOM_ATTRIBUTE_KEYS =
        Set.of("id", "source", "target");

    private final Set<Parameter> parameters;

    /**
     * Parameters that affect the behavior of the {@link GmlExporter} exporter.
     */
    public enum Parameter
    {
        /**
         * If set the exporter outputs edge labels. The labels are found from the edge attribute
         * provider of the importer using the key "label".
         */
        EXPORT_EDGE_LABELS,
        /**
         * If set the exporter outputs edge weights
         */
        EXPORT_EDGE_WEIGHTS,
        /**
         * If set the exporter outputs all custom edge attributes. The attributes are located from
         * the edge attribute provider of the importer. Note that these attributes have lowest
         * priority compared to special handled ones like "label", or "weight" and cannot contain
         * special keys like "id", "source" and "target".
         */
        EXPORT_CUSTOM_EDGE_ATTRIBUTES,
        /**
         * If set the exporter outputs vertex labels. The labels are found from the vertex attribute
         * provider of the importer using the key "label".
         */
        EXPORT_VERTEX_LABELS,
        /**
         * If set the exporter outputs all custom vertex attributes. The attributes are located from
         * the vertex attribute provider of the importer. Note that these attributes have lowest
         * priority compared to special handled ones like "label" and cannot contain special keys
         * like "id".
         */
        EXPORT_CUSTOM_VERTEX_ATTRIBUTES,
        /**
         * If set the exporter escapes all strings as Java strings, otherwise no escaping is
         * performed.
         */
        ESCAPE_STRINGS_AS_JAVA,
    }

    /**
     * Creates a new GmlExporter object with integer id providers for the vertex identifiers.
     */
    public GmlExporter()
    {
        this(new IntegerIdProvider<>());
    }

    /**
     * Constructs a new GmlExporter object with the given id providers.
     *
     * @param vertexIdProvider for generating vertex IDs. Must not be null.
     */
    public GmlExporter(Function<V, String> vertexIdProvider)
    {
        super(vertexIdProvider);
        this.parameters = new HashSet<>();
    }

    /**
     * Exports an graph into a plain text GML format.
     *
     * @param writer the writer
     * @param g the graph
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            getVertexId(from);
        }

        exportHeader(out);
        out.println("graph");
        out.println("[");
        out.println(TAB1 + "label" + DELIM + quoted(""));
        if (g.getType().isDirected()) {
            out.println(TAB1 + "directed" + DELIM + "1");
        } else {
            out.println(TAB1 + "directed" + DELIM + "0");
        }
        exportVertices(out, g);
        exportEdges(out, g);
        out.println("]");
        out.flush();
    }

    /**
     * Return if a particular parameter of the exporter is enabled
     * 
     * @param p the parameter
     * @return {@code true} if the parameter is set, {@code false} otherwise
     */
    public boolean isParameter(Parameter p)
    {
        return parameters.contains(p);
    }

    /**
     * Set the value of a parameter of the exporter
     * 
     * @param p the parameter
     * @param value the value to set
     */
    public void setParameter(Parameter p, boolean value)
    {
        if (value) {
            parameters.add(p);
        } else {
            parameters.remove(p);
        }
    }

    private String quoted(final String s)
    {
        boolean escapeStringAsJava = parameters.contains(Parameter.ESCAPE_STRINGS_AS_JAVA);
        if (escapeStringAsJava) {
            return "\"" + StringEscapeUtils.escapeJava(s) + "\"";
        } else {
            return "\"" + s + "\"";
        }
    }

    private void exportHeader(PrintWriter out)
    {
        out.println("Creator" + DELIM + quoted(CREATOR));
        out.println("Version" + DELIM + VERSION);
    }

    private void exportAttribute(PrintWriter out, String key, Attribute attribute)
    {
        AttributeType type = attribute.getType();
        switch (type) {
        case INT:
            out.println(TAB2 + key + DELIM + Integer.valueOf(attribute.getValue()));
            break;
        case LONG:
            out.println(TAB2 + key + DELIM + Long.valueOf(attribute.getValue()));
            break;
        case FLOAT:
            out.println(TAB2 + key + DELIM + Float.valueOf(attribute.getValue()));
            break;
        case DOUBLE:
            out.println(TAB2 + key + DELIM + Double.valueOf(attribute.getValue()));
            break;
        default:
            out.println(TAB2 + key + DELIM + quoted(attribute.getValue()));
            break;
        }
    }

    private void exportVertices(PrintWriter out, Graph<V, E> g)
    {
        boolean exportVertexLabels = parameters.contains(Parameter.EXPORT_VERTEX_LABELS);
        boolean exportCustomVertexAttributes =
            parameters.contains(Parameter.EXPORT_CUSTOM_VERTEX_ATTRIBUTES);

        for (V from : g.vertexSet()) {
            out.println(TAB1 + "node");
            out.println(TAB1 + "[");
            out.println(TAB2 + "id" + DELIM + getVertexId(from));

            if (exportVertexLabels) {
                String label = getVertexAttribute(from, LABEL_ATTRIBUTE_KEY)
                    .map(Attribute::getValue).orElse(from.toString());
                out.println(TAB2 + "label" + DELIM + quoted(label));
            }
            if (exportCustomVertexAttributes) {
                getVertexAttributes(from).ifPresent(vertexAttributes -> {
                    vertexAttributes.entrySet().stream().forEach(e -> {
                        String customAttributeKey = e.getKey();
                        Attribute customAttributeValue = e.getValue();

                        if (FORBIDDEN_VERTEX_CUSTOM_ATTRIBUTE_KEYS.contains(customAttributeKey)) {
                            throw new IllegalArgumentException(
                                "Key " + customAttributeKey + " is reserved");
                        }

                        if (LABEL_ATTRIBUTE_KEY.equals(customAttributeKey) && exportVertexLabels) {
                            // give higher priority to vertex labels
                            return;
                        }

                        exportAttribute(out, customAttributeKey, customAttributeValue);
                    });
                });
            }

            out.println(TAB1 + "]");
        }
    }

    private void exportEdges(PrintWriter out, Graph<V, E> g)
    {
        boolean exportEdgeWeights = parameters.contains(Parameter.EXPORT_EDGE_WEIGHTS);
        boolean exportEdgeLabels = parameters.contains(Parameter.EXPORT_EDGE_LABELS);
        boolean exportCustomEdgeAttributes =
            parameters.contains(Parameter.EXPORT_CUSTOM_EDGE_ATTRIBUTES);

        for (E edge : g.edgeSet()) {
            out.println(TAB1 + "edge");
            out.println(TAB1 + "[");

            getEdgeId(edge).ifPresent(eId -> {
                out.println(TAB2 + "id" + DELIM + eId);
            });

            String s = getVertexId(g.getEdgeSource(edge));
            out.println(TAB2 + "source" + DELIM + s);

            String t = getVertexId(g.getEdgeTarget(edge));
            out.println(TAB2 + "target" + DELIM + t);

            if (exportEdgeLabels) {
                Attribute label = getEdgeAttribute(edge, LABEL_ATTRIBUTE_KEY)
                    .orElse(DefaultAttribute.createAttribute(edge.toString()));
                exportAttribute(out, "label", label);
            }
            if (exportEdgeWeights && g.getType().isWeighted()) {
                exportAttribute(
                    out, "weight", DefaultAttribute.createAttribute(g.getEdgeWeight(edge)));
            }
            if (exportCustomEdgeAttributes) {
                getEdgeAttributes(edge).ifPresent(edgeAttributes -> {
                    edgeAttributes.entrySet().stream().forEach(e -> {
                        String customAttributeKey = e.getKey();
                        Attribute customAttributeValue = e.getValue();

                        if (FORBIDDEN_EDGE_CUSTOM_ATTRIBUTE_KEYS.contains(customAttributeKey)) {
                            throw new IllegalArgumentException(
                                "Key " + customAttributeKey + " is reserved");
                        }

                        if (LABEL_ATTRIBUTE_KEY.equals(customAttributeKey) && exportEdgeLabels) {
                            // give higher priority to edge labels
                            return;
                        }

                        if (WEIGHT_ATTRIBUTE_KEY.equals(customAttributeKey) && exportEdgeWeights) {
                            // give higher priority to edge weights
                            return;
                        }

                        exportAttribute(out, customAttributeKey, customAttributeValue);
                    });
                });
            }

            out.println(TAB1 + "]");
        }
    }

}
