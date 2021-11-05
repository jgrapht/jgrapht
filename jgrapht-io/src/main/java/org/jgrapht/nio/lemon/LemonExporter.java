/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.lemon;

import org.apache.commons.text.*;
import org.jgrapht.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Exports a graph into Lemon graph format (LGF).
 * 
 * <p>
 * This is the custom graph format used in the <a href="https://lemon.cs.elte.hu">Lemon</a> graph
 * library.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class LemonExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private static final String CREATOR = "JGraphT Lemon (LGF) Exporter";
    private static final String VERSION = "1";

    private static final String DELIM = " ";
    private static final String TAB1 = "\t";

    private final Set<Parameter> parameters;

    /**
     * Parameters that affect the behavior of the {@link LemonExporter} exporter.
     */
    public enum Parameter
    {
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
     * Constructs a new exporter.
     */
    public LemonExporter()
    {
        this(new IntegerIdProvider<>());
    }

    /**
     * Constructs a new exporter with a given vertex id provider.
     *
     * @param vertexIdProvider for generating vertex IDs. Must not be null.
     */
    public LemonExporter(Function<V, String> vertexIdProvider)
    {
        super(vertexIdProvider);
        this.parameters = new HashSet<>();
    }

    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        exportHeader(out);
        exportVertices(out, g);
        exportEdges(out, g);

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

    private String prepareId(final String s)
    {
        boolean escapeStringAsJava = parameters.contains(Parameter.ESCAPE_STRINGS_AS_JAVA);
        if (escapeStringAsJava) {
            return "\"" + StringEscapeUtils.escapeJava(s) + "\"";
        } else {
            return s;
        }
    }

    private void exportHeader(PrintWriter out)
    {
        out.println("#Creator:" + DELIM + CREATOR);
        out.println("#Version:" + DELIM + VERSION);
        out.println();
    }

    private void exportVertices(PrintWriter out, Graph<V, E> g)
    {
        out.println("@nodes");
        out.println("label");
        for (V v : g.vertexSet()) {
            String id = getVertexId(v);
            String quotedId = prepareId(id);
            out.println(quotedId);
        }
        out.println();
    }

    private void exportEdges(PrintWriter out, Graph<V, E> g)
    {
        boolean exportEdgeWeights = parameters.contains(Parameter.EXPORT_EDGE_WEIGHTS);

        out.println("@arcs");
        out.print(TAB1);
        out.print(TAB1);
        if (exportEdgeWeights) {
            out.println("weight");
        } else {
            out.println("-");
        }

        for (E edge : g.edgeSet()) {
            String s = getVertexId(g.getEdgeSource(edge));
            String t = getVertexId(g.getEdgeTarget(edge));

            out.print(prepareId(s));
            out.print(TAB1);
            out.print(prepareId(t));
            if (exportEdgeWeights) {
                out.print(TAB1);
                out.print(Double.toString(g.getEdgeWeight(edge)));
            }
            out.println();
        }
        out.println();
    }

}
