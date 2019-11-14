/*
 * (C) Copyright 2006-2019, by Dimitrios Michail and Contributors.
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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.text.StringEscapeUtils;
import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.BaseExporter;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.IntegerIdProvider;

/**
 * Exports a graph into a GML file (Graph Modeling Language).
 *
 * <p>
 * For a description of the format see <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www. infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
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
         * If set the exporter outputs vertex labels. The labels are found from the vertex attribute
         * provider of the importer using the key "label".
         */
        EXPORT_VERTEX_LABELS,
        /**
         * If set the exporter outputs edge weights
         */
        EXPORT_EDGE_WEIGHTS,
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

    private void exportVertices(PrintWriter out, Graph<V, E> g)
    {
        boolean exportVertexLabels = parameters.contains(Parameter.EXPORT_VERTEX_LABELS);

        for (V from : g.vertexSet()) {
            out.println(TAB1 + "node");
            out.println(TAB1 + "[");
            out.println(TAB2 + "id" + DELIM + getVertexId(from));
            if (exportVertexLabels) {
                String label = getVertexAttribute(from, LABEL_ATTRIBUTE_KEY)
                    .map(Attribute::getValue).orElse(from.toString());
                out.println(TAB2 + "label" + DELIM + quoted(label));
            }
            out.println(TAB1 + "]");
        }
    }

    private void exportEdges(PrintWriter out, Graph<V, E> g)
    {
        boolean exportEdgeWeights = parameters.contains(Parameter.EXPORT_EDGE_WEIGHTS);
        boolean exportEdgeLabels = parameters.contains(Parameter.EXPORT_EDGE_LABELS);

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
                String label = getEdgeAttribute(edge, LABEL_ATTRIBUTE_KEY)
                    .map(Attribute::getValue).orElse(edge.toString());
                out.println(TAB2 + "label" + DELIM + quoted(label));
            }
            if (exportEdgeWeights && g.getType().isWeighted()) {
                out.println(TAB2 + "weight" + DELIM + Double.toString(g.getEdgeWeight(edge)));
            }
            out.println(TAB1 + "]");
        }
    }

}
