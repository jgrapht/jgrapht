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
 * CSVExporter.java
 * ------------------
 * (C) Copyright 2014, by Ivan Gavrilovic.
 *
 * Original Author:  Ivan Gavrilovic
 *
 *
 */
package org.jgrapht.ext;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Exports a graph to a CSV file, as a adjacency list. Default delimiter is
 * the comma(,) character. Look <a href="http://www.ietf.org/rfc/rfc4180.txt">http://www.ietf.org/rfc/rfc4180.txt<a/>
 * for more on the format. In current implementation, each row may not contain the same number of fields.
 *
 * @author Ivan Gavrilović
 */
public class CSVExporter<V, E> {
    private String delimiter = ",";
    final private String newLine = System.getProperty("line.separator");
    private VertexNameProvider<V> provider;

    /**
     * Creates instance of exporter, with {@link org.jgrapht.ext.StringEdgeNameProvider}
     * as {@link org.jgrapht.ext.VertexNameProvider}.
     */
    public CSVExporter() {
        this(new StringNameProvider<V>());
    }

    /**
     * Creates instance of exporter with specified {@link org.jgrapht.ext.VertexNameProvider}
     *
     * @param vertexNameProvider specified vertex name provider
     */
    public CSVExporter(VertexNameProvider<V> vertexNameProvider) {
        this.provider = vertexNameProvider;
    }

    /**
     * <p>Export edge per line, whole graph to a CSV file.
     * Directed graph: {@link org.jgrapht.DirectedGraph} with vertices A, B, C, D,
     * and edges AB, AD, and BD, will be exported to:<br/>
     * A,B<br/>
     * A,D<br/>
     * B,D<br/>
     * C<br/>
     * D<br/>
     * Undirected graph: {@link org.jgrapht.UndirectedGraph} with vertices A, B, C, D,
     * and edges AB, AD, and BD, will be exported to:<br/>
     * A,B<br/>
     * A,D<br/>
     * B,A<br/>
     * B,D<br/>
     * C<br/>
     * D,A<br/>
     * D,B<br/>
     * <p/>
     *
     * @param graph  graph to export
     * @param writer output for csv
     * @throws IOException if an I/O error occurs
     */
    public void export(Graph<V, E> graph, Writer writer) throws IOException {
        exportImpl(graph, writer, false);
    }

    /**
     * <p>Export node and its neighbours per line, whole graph to a CSV file.
     * Directed graph: {@link org.jgrapht.DirectedGraph} with vertices A, B, C, D,
     * and edges AB, AD, and BD, will be exported to:<br/>
     * A,B,D<br/>
     * B,D<br/>
     * C<br/>
     * D<br/>
     * Undirected graph: {@link org.jgrapht.UndirectedGraph} with vertices A, B, C, D,
     * and edges AB, AD, and BD, will be exported to:<br/>
     * A,B,D<br/>
     * B,A,D<br/>
     * B,D<br/>
     * C<br/>
     * D,A,B<br/>
     * <p/>
     *
     * @param graph  graph to export
     * @param writer output for csv
     * @throws IOException if an I/O error occurs
     */
    public void exportCollapsed(Graph<V, E> graph, Writer writer) throws IOException {
        exportImpl(graph, writer, true);
    }


    /**
     * Actual implementation of the export
     *
     * @param graph     graph to export
     * @param out       output for csv
     * @param collapsed if {@code true} each line is node and its neighbours, otherwise output edge per line
     * @throws IOException if an I/O error occurs
     */
    private void exportImpl(Graph<V, E> graph, Writer out, boolean collapsed) throws IOException {
        boolean firstLine = true;
        for (V node : graph.vertexSet()) {
            List<V> neighbours = getNeighbours(graph, node);

            out.write((firstLine ? "" : newLine) + escapeCSV(provider.getVertexName(node)));
            if (collapsed) {
                for (V n : neighbours) {
                    out.write(delimiter
                            + escapeCSV(provider.getVertexName(n)));
                }
            } else {
                for (int i = 0; i < neighbours.size(); i++) {
                    V next = neighbours.get(i);
                    out.write(delimiter
                            + escapeCSV(provider.getVertexName(next)));
                    if (i != neighbours.size() - 1) {
                        out.write(newLine
                                + escapeCSV(provider.getVertexName(node)));
                    }
                }
            }
            firstLine = false;

        }
        out.flush();
    }

    /**
     * <p>Get the neighbours of the specified node. In case of {@link org.jgrapht.DirectedGraph} returns the
     * successors, in case of {@link org.jgrapht.UndirectedGraph} returns all neighbours
     * </p>
     *
     * @param graph graph to examine
     * @param curr  node to lookup
     * @return {@link java.util.List} containing all neighbours
     */
    private List<V> getNeighbours(Graph<V, E> graph, V curr) {
        if (graph instanceof DirectedGraph) {
            return Graphs.successorListOf((DirectedGraph<V, E>) graph, curr);
        } else {
            return Graphs.neighborListOf(graph, curr);
        }
    }

    /**
     * Escapes string according to the <a href="http://www.ietf.org/rfc/rfc4180.txt">http://www.ietf.org/rfc/rfc4180.txt<a/>
     * specification
     *
     * @param unescaped string to process
     * @return valid csv field
     */
    private String escapeCSV(String unescaped) {
        String escapeCharacter[] = new String[]{delimiter, "\"", "\r", "\n"};

        for (String s : escapeCharacter) {
            if (unescaped.contains(s)) {
                unescaped = unescaped.replaceAll("\"", "\"\"");
                return "\"" + unescaped + "\"";
            }
        }

        return unescaped;
    }

    /**
     * Sets the value for the delimiter used when outputting the graph. By default,
     * delimiter is space character
     *
     * @param delimiter value of the new delimiter
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
