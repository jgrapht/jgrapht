/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------
 * MatrixExporter.java
 * ------------------
 * (C) Copyright 2005-2008, by Charles Fry and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 13-Dec-2005 : Initial Version (CF);
 *
 */
package org.jgrapht.ext;

import com.google.common.collect.Maps;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Exports a graph to a plain text matrix format, which can be processed by
 * matrix manipulation software, such as <a href="http://rs.cipr.uib.no/mtj/">
 * MTJ</a> or <a href="http://www.mathworks.com/products/matlab/">MATLAB</a>.
 *
 * @author Charles Fry
 */
public class MatrixExporter<V, E>
{
    //~ Instance fields --------------------------------------------------------

    private final String delimiter = " ";
    private final String prefix = "";
    private final String suffix = "";

    //~ Constructors -----------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    private void println(
        final PrintWriter out,
        final String fromName,
        final String toName,
        final String value)
    {
        out.println(
            prefix + fromName + suffix + delimiter
            + prefix + toName + suffix + delimiter
            + prefix + value + suffix);
    }

    /**
     * Exports the specified graph into a plain text file format containing a
     * sparse representation of the graph's adjacency matrix. The value stored
     * in each position of the matrix indicates the number of edges between two
     * vertices. With an undirected graph, the adjacency matrix is symetric.
     *
     * @param output the writer to which the graph to be exported.
     * @param g the graph to be exported.
     */
    public void exportAdjacencyMatrix(final Writer output, final UndirectedGraph<V, E> g)
    {
        final PrintWriter out = new PrintWriter(output);

        final VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (final V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (final V from : g.vertexSet()) {
            exportAdjacencyMatrixVertex(
                out,
                nameProvider,
                from,
                Graphs.neighborListOf(g, from));
        }

        out.flush();
    }

    /**
     * Exports the specified graph into a plain text file format containing a
     * sparse representation of the graph's adjacency matrix. The value stored
     * in each position of the matrix indicates the number of directed edges
     * going from one vertex to another.
     *
     * @param output the writer to which the graph to be exported.
     * @param g the graph to be exported.
     */
    public void exportAdjacencyMatrix(final Writer output, final DirectedGraph<V, E> g)
    {
        final PrintWriter out = new PrintWriter(output);

        final VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (final V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (final V from : g.vertexSet()) {
            exportAdjacencyMatrixVertex(
                out,
                nameProvider,
                from,
                Graphs.successorListOf(g, from));
        }

        out.flush();
    }

    private void exportAdjacencyMatrixVertex(
        final PrintWriter out,
        final VertexNameProvider<V> nameProvider,
        final V from,
        final List<V> neighbors)
    {
        final String fromName = nameProvider.getVertexName(from);
        final Map<String, AtomicInteger> counts = Maps.newLinkedHashMap();
        for (final V to : neighbors) {
            final String toName = nameProvider.getVertexName(to);
            AtomicInteger count = counts.get(toName);
            if (count == null) {
                count = new AtomicInteger(0);
                counts.put(toName, count);
            }

            count.incrementAndGet();
            if (from.equals(to))
                // count loops twice, once for each end
                count.incrementAndGet();
        }
        for (final Map.Entry<String, AtomicInteger> entry: counts.entrySet()) {
            final String toName = entry.getKey();
            final AtomicInteger count = entry.getValue();
            println(out, fromName, toName, count.toString());
        }
    }

    /**
     * Exports the specified graph into a plain text file format containing a
     * sparse representation of the graph's Laplacian matrix. Laplacian matrices
     * are only defined for simple graphs, so edge direction, multiple edges,
     * loops, and weights are all ignored when creating the Laplacian matrix. If
     * you're unsure about Laplacian matrices, see: <a
     * href="http://mathworld.wolfram.com/LaplacianMatrix.html">
     * http://mathworld.wolfram.com/LaplacianMatrix.html</a>.
     *
     * @param output the writer to which the graph is to be exported.
     * @param g the graph to be exported.
     */
    public void exportLaplacianMatrix(final Writer output, final UndirectedGraph<V, E> g)
    {
        final PrintWriter out = new PrintWriter(output);

        final VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (final V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (final V from : g.vertexSet()) {
            final String fromName = nameProvider.getVertexName(from);

            // TODO modify Graphs to return neighbor sets
            final List<V> neighbors = Graphs.neighborListOf(g, from);
            println(
                out,
                fromName,
                fromName,
                Integer.toString(neighbors.size()));
            for (final V to : neighbors) {
                final String toName = nameProvider.getVertexName(to);
                println(out, fromName, toName, "-1");
            }
        }

        out.flush();
    }

    /**
     * Exports the specified graph into a plain text file format containing a
     * sparse representation of the graph's normalized Laplacian matrix.
     * Laplacian matrices are only defined for simple graphs, so edge direction,
     * multiple edges, loops, and weights are all ignored when creating the
     * Laplacian matrix. If you're unsure about normalized Laplacian matrices,
     * see: <a href="http://mathworld.wolfram.com/LaplacianMatrix.html">
     * http://mathworld.wolfram.com/LaplacianMatrix.html</a>.
     *
     * @param output the writer to which the graph is to be exported.
     * @param g the graph to be exported.
     */
    public void exportNormalizedLaplacianMatrix(
        final Writer output,
        final UndirectedGraph<V, E> g)
    {
        final PrintWriter out = new PrintWriter(output);

        final VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (final V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (final V from : g.vertexSet()) {
            final String fromName = nameProvider.getVertexName(from);
            final Set<V> neighbors =
                new LinkedHashSet<V>(Graphs.neighborListOf(g, from));
            if (neighbors.isEmpty()) {
                println(out, fromName, fromName, "0");
            } else {
                println(out, fromName, fromName, "1");

                for (final V to : neighbors) {
                    final String toName = nameProvider.getVertexName(to);
                    final double value =
                        -1 / Math.sqrt(g.degreeOf(from) * g.degreeOf(to));
                    println(out, fromName, toName, Double.toString(value));
                }
            }
        }

        out.flush();
    }
}

// End MatrixExporter.java
