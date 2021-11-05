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
package org.jgrapht.nio.csv;

import org.jgrapht.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Exports a graph into a CSV Format or any other Delimiter-separated value format.
 * 
 * <p>
 * The exporter supports three different formats which can be adjusted using the
 * {@link #setFormat(CSVFormat) setFormat} method. The supported formats are the same CSV formats
 * used by <a href="https://gephi.org/users/supported-graph-formats/csv-format">Gephi </a>. For some
 * of the formats, the behavior of the exporter can be adjusted using the
 * {@link #setParameter(org.jgrapht.nio.csv.CSVFormat.Parameter, boolean) setParameter} method. See
 * {@link CSVFormat} for a description of the formats.
 * </p>
 * 
 * <p>
 * The default output respects <a href="http://www.ietf.org/rfc/rfc4180.txt">rfc4180</a>. The caller
 * can also adjust the separator to something like semicolon or pipe instead of comma. In such a
 * case, all fields are escaped using the new separator. See
 * <a href="https://en.wikipedia.org/wiki/Delimiter-separated_values">Delimiter-separated values</a>
 * for more information.
 * </p>
 * 
 * @see CSVFormat
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class CSVExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private static final char DEFAULT_DELIMITER = ',';

    private final Set<CSVFormat.Parameter> parameters;
    private CSVFormat format;
    private char delimiter;

    /**
     * Creates a new CSVExporter with {@link CSVFormat#ADJACENCY_LIST} format and integer name
     * provider for the vertices.
     */
    public CSVExporter()
    {
        this(CSVFormat.ADJACENCY_LIST);
    }

    /**
     * Creates a new CSVExporter with integer id providers for the vertices.
     * 
     * @param format the format to use
     */
    public CSVExporter(CSVFormat format)
    {
        this(format, DEFAULT_DELIMITER);
    }

    /**
     * Creates a new CSVExporter with integer id providers for the vertices.
     * 
     * @param format the format to use
     * @param delimiter delimiter to use
     */
    public CSVExporter(CSVFormat format, char delimiter)
    {
        this(new IntegerIdProvider<>(), format, delimiter);
    }

    /**
     * Constructs a new CSVExporter with the given ID providers and format.
     *
     * @param vertexIdProvider for generating vertex IDs. Must not be null.
     * @param format the format to use
     * @param delimiter delimiter to use
     */
    public CSVExporter(Function<V, String> vertexIdProvider, CSVFormat format, char delimiter)
    {
        super(vertexIdProvider);
        this.format = format;
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
        this.parameters = new HashSet<>();
    }

    /**
     * Exports a graph
     *
     * @param g the graph
     * @param writer the writer
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);
        switch (format) {
        case EDGE_LIST:
            exportAsEdgeList(g, out);
            break;
        case ADJACENCY_LIST:
            exportAsAdjacencyList(g, out);
            break;
        case MATRIX:
            exportAsMatrix(g, out);
            break;
        }
        out.flush();
    }

    /**
     * Return if a particular parameter of the exporter is enabled
     * 
     * @param p the parameter
     * @return {@code true} if the parameter is set, {@code false} otherwise
     */
    public boolean isParameter(CSVFormat.Parameter p)
    {
        return parameters.contains(p);
    }

    /**
     * Set the value of a parameter of the exporter
     * 
     * @param p the parameter
     * @param value the value to set
     */
    public void setParameter(CSVFormat.Parameter p, boolean value)
    {
        if (value) {
            parameters.add(p);
        } else {
            parameters.remove(p);
        }
    }

    /**
     * Get the format of the exporter
     * 
     * @return the format of the exporter
     */
    public CSVFormat getFormat()
    {
        return format;
    }

    /**
     * Set the format of the exporter
     * 
     * @param format the format to use
     */
    public void setFormat(CSVFormat format)
    {
        this.format = format;
    }

    /**
     * Get the delimiter (comma, semicolon, pipe, etc).
     * 
     * @return the delimiter
     */
    public char getDelimiter()
    {
        return delimiter;
    }

    /**
     * Set the delimiter (comma, semicolon, pipe, etc).
     * 
     * @param delimiter the delimiter to use
     */
    public void setDelimiter(char delimiter)
    {
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
    }

    private void exportAsEdgeList(Graph<V, E> g, PrintWriter out)
    {
        boolean exportEdgeWeights = parameters.contains(CSVFormat.Parameter.EDGE_WEIGHTS);

        for (E e : g.edgeSet()) {
            exportEscapedField(out, getVertexId(g.getEdgeSource(e)));
            out.print(delimiter);
            exportEscapedField(out, getVertexId(g.getEdgeTarget(e)));
            if (exportEdgeWeights) {
                out.print(delimiter);
                exportEscapedField(out, String.valueOf(g.getEdgeWeight(e)));
            }
            out.println();
        }
    }

    private void exportAsAdjacencyList(Graph<V, E> g, PrintWriter out)
    {
        boolean exportEdgeWeights = parameters.contains(CSVFormat.Parameter.EDGE_WEIGHTS);

        for (V v : g.vertexSet()) {
            exportEscapedField(out, getVertexId(v));
            for (E e : g.outgoingEdgesOf(v)) {
                V w = Graphs.getOppositeVertex(g, e, v);
                out.print(delimiter);
                exportEscapedField(out, getVertexId(w));
                if (exportEdgeWeights) {
                    out.print(delimiter);
                    exportEscapedField(out, String.valueOf(g.getEdgeWeight(e)));
                }
            }
            out.println();
        }
    }

    private void exportAsMatrix(Graph<V, E> g, PrintWriter out)
    {
        boolean exportNodeId = parameters.contains(CSVFormat.Parameter.MATRIX_FORMAT_NODEID);
        boolean exportEdgeWeights = parameters.contains(CSVFormat.Parameter.EDGE_WEIGHTS);
        boolean zeroWhenNoEdge =
            parameters.contains(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE);

        if (exportNodeId) {
            for (V v : g.vertexSet()) {
                out.print(delimiter);
                exportEscapedField(out, getVertexId(v));
            }
            out.println();
        }
        int n = g.vertexSet().size();
        for (V v : g.vertexSet()) {
            if (exportNodeId) {
                exportEscapedField(out, getVertexId(v));
                out.print(delimiter);
            }
            int i = 0;
            for (V u : g.vertexSet()) {
                E e = g.getEdge(v, u);
                if (e == null) {
                    if (zeroWhenNoEdge) {
                        exportEscapedField(out, "0");
                    }
                } else {
                    if (exportEdgeWeights) {
                        exportEscapedField(out, String.valueOf(g.getEdgeWeight(e)));
                    } else {
                        exportEscapedField(out, "1");
                    }
                }
                if (i++ < n - 1) {
                    out.print(delimiter);
                }
            }
            out.println();
        }
    }

    private void exportEscapedField(PrintWriter out, String field)
    {
        out.print(DSVUtils.escapeDSV(field, delimiter));
    }

}
