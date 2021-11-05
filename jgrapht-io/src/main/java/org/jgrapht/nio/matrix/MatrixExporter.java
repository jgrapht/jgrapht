/*
 * (C) Copyright 2005-2021, by Charles Fry and Contributors.
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
package org.jgrapht.nio.matrix;

import org.jgrapht.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Exports a graph to a plain text matrix format, which can be processed by matrix manipulation
 * software, such as <a href="http://rs.cipr.uib.no/mtj/">MTJ</a> or
 * <a href="http://www.mathworks.com/products/matlab/">MATLAB</a>.
 * 
 * <p>
 * The exporter supports three different formats, see {@link Format}.
 * </p>
 * 
 * @see Format
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Charles Fry
 * @author Dimitrios Michail
 */
public class MatrixExporter<V, E>
    extends
    BaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private final String delimiter = " ";
    private Format format;

    /**
     * Formats supported by the {@link MatrixExporter} exporter.
     */
    public enum Format
    {
        /**
         * A sparse representation of the adjacency matrix. This is the default. Exports the
         * specified graph into a plain text file format containing a sparse representation of the
         * graph's adjacency matrix. The value stored in each position of the matrix indicates the
         * number of edges between two vertices. With an undirected graph, the adjacency matrix is
         * symmetric.
         */
        SPARSE_ADJACENCY_MATRIX,
        /**
         * A sparse representation of the Laplacian.
         */
        SPARSE_LAPLACIAN_MATRIX,
        /**
         * A sparse representation of the normalized Laplacian.
         */
        SPARSE_NORMALIZED_LAPLACIAN_MATRIX,
    }

    /**
     * Creates a new MatrixExporter with integer name provider for the vertex identifiers and
     * {@link Format#SPARSE_ADJACENCY_MATRIX} as the default format.
     */
    public MatrixExporter()
    {
        this(Format.SPARSE_ADJACENCY_MATRIX, new IntegerIdProvider<>());
    }

    /**
     * Creates a new MatrixExporter with integer name provider for the vertex identifiers.
     * 
     * @param format format to use
     */
    public MatrixExporter(Format format)
    {
        this(format, new IntegerIdProvider<>());
    }

    /**
     * Creates a new MatrixExporter.
     * 
     * @param format format to use
     * @param vertexIdProvider for generating vertex identifiers. Must not be null.
     */
    public MatrixExporter(Format format, Function<V, String> vertexIdProvider)
    {
        super(vertexIdProvider);
        this.format = format;
    }

    /**
     * Get the format that the exporter is using.
     * 
     * @return the output format
     */
    public Format getFormat()
    {
        return format;
    }

    /**
     * Set the output format of the exporter
     * 
     * @param format the format to use
     */
    public void setFormat(Format format)
    {
        this.format = format;
    }

    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
        throws ExportException
    {
        switch (format) {
        case SPARSE_ADJACENCY_MATRIX:
            exportAdjacencyMatrix(g, writer);
            break;
        case SPARSE_LAPLACIAN_MATRIX:
            if (g.getType().isUndirected()) {
                exportLaplacianMatrix(g, writer);
            } else {
                throw new ExportException(
                    "Exporter can only export undirected graphs in this format");
            }
            break;
        case SPARSE_NORMALIZED_LAPLACIAN_MATRIX:
            if (g.getType().isUndirected()) {
                exportNormalizedLaplacianMatrix(g, writer);
            } else {
                throw new ExportException(
                    "Exporter can only export undirected graphs in this format");
            }
            break;
        }
    }

    private void exportAdjacencyMatrix(Graph<V, E> g, Writer writer)
    {
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            getVertexId(from);
        }

        PrintWriter out = new PrintWriter(writer);

        if (g.getType().isDirected()) {
            for (V from : g.vertexSet()) {
                exportAdjacencyMatrixVertex(out, from, Graphs.successorListOf(g, from));
            }
        } else {
            for (V from : g.vertexSet()) {
                exportAdjacencyMatrixVertex(out, from, Graphs.neighborListOf(g, from));
            }
        }

        out.flush();
    }

    private void exportAdjacencyMatrixVertex(PrintWriter writer, V from, List<V> neighbors)
    {
        String fromName = getVertexId(from);
        Map<String, ModifiableInteger> counts = new LinkedHashMap<>();
        for (V to : neighbors) {
            String toName = getVertexId(to);
            ModifiableInteger count = counts.get(toName);
            if (count == null) {
                count = new ModifiableInteger(0);
                counts.put(toName, count);
            }

            count.increment();
            if (from.equals(to)) {
                // count loops twice, once for each end
                count.increment();
            }
        }
        for (Map.Entry<String, ModifiableInteger> entry : counts.entrySet()) {
            String toName = entry.getKey();
            ModifiableInteger count = entry.getValue();
            exportEntry(writer, fromName, toName, count.toString());
        }
    }

    private void exportEntry(PrintWriter writer, String from, String to, String value)
    {
        writer.println(from + delimiter + to + delimiter + value);
    }

    private void exportLaplacianMatrix(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            getVertexId(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = getVertexId(from);

            List<V> neighbors = Graphs.neighborListOf(g, from);
            exportEntry(out, fromName, fromName, Integer.toString(neighbors.size()));
            for (V to : neighbors) {
                String toName = getVertexId(to);
                exportEntry(out, fromName, toName, "-1");
            }
        }

        out.flush();
    }

    private void exportNormalizedLaplacianMatrix(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            getVertexId(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = getVertexId(from);
            Set<V> neighbors = new LinkedHashSet<>(Graphs.neighborListOf(g, from));
            if (neighbors.isEmpty()) {
                exportEntry(out, fromName, fromName, "0");
            } else {
                exportEntry(out, fromName, fromName, "1");

                for (V to : neighbors) {
                    String toName = getVertexId(to);
                    double value = -1 / Math.sqrt(g.degreeOf(from) * g.degreeOf(to));
                    exportEntry(out, fromName, toName, Double.toString(value));
                }
            }
        }

        out.flush();
    }

}
