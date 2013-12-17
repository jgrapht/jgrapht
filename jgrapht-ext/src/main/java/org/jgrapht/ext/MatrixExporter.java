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

import java.io.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * Exports a graph to a plain text matrix format, which can be processed by
 * matrix manipulation software, such as <a href="http://rs.cipr.uib.no/mtj/">
 * MTJ</a> or <a href="http://www.mathworks.com/products/matlab/">MATLAB</a>.
 *
 * @author Charles Fry
 */
public class MatrixExporter<V, E>
{
    

    private String delimiter = " ";
    private String prefix = "";
    private String suffix = "";

    

    /**
     * Creates a new MatrixExporter object.
     */
    public MatrixExporter()
    {
    }

    

    private void println(
        PrintWriter out,
        String fromName,
        String toName,
        String value)
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
    public void exportAdjacencyMatrix(Writer output, UndirectedGraph<V, E> g)
    {
        PrintWriter out = new PrintWriter(output);

        VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (V from : g.vertexSet()) {
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
    public void exportAdjacencyMatrix(Writer output, DirectedGraph<V, E> g)
    {
        PrintWriter out = new PrintWriter(output);

        VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (V from : g.vertexSet()) {
            exportAdjacencyMatrixVertex(
                out,
                nameProvider,
                from,
                Graphs.successorListOf(g, from));
        }

        out.flush();
    }

    private void exportAdjacencyMatrixVertex(
        PrintWriter out,
        VertexNameProvider<V> nameProvider,
        V from,
        List<V> neighbors)
    {
        String fromName = nameProvider.getVertexName(from);
        Map<String, ModifiableInteger> counts =
            new LinkedHashMap<String, ModifiableInteger>();
        for (V to : neighbors) {
            String toName = nameProvider.getVertexName(to);
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
    public void exportLaplacianMatrix(Writer output, UndirectedGraph<V, E> g)
    {
        PrintWriter out = new PrintWriter(output);

        VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = nameProvider.getVertexName(from);

            // TODO modify Graphs to return neighbor sets
            List<V> neighbors = Graphs.neighborListOf(g, from);
            println(
                out,
                fromName,
                fromName,
                Integer.toString(neighbors.size()));
            for (V to : neighbors) {
                String toName = nameProvider.getVertexName(to);
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
        Writer output,
        UndirectedGraph<V, E> g)
    {
        PrintWriter out = new PrintWriter(output);

        VertexNameProvider<V> nameProvider = new IntegerNameProvider<V>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getVertexName(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = nameProvider.getVertexName(from);
            Set<V> neighbors =
                new LinkedHashSet<V>(Graphs.neighborListOf(g, from));
            if (neighbors.isEmpty()) {
                println(out, fromName, fromName, "0");
            } else {
                println(out, fromName, fromName, "1");

                for (V to : neighbors) {
                    String toName = nameProvider.getVertexName(to);
                    double value =
                        -1 / Math.sqrt(g.degreeOf(from) * g.degreeOf(to));
                    println(out, fromName, toName, Double.toString(value));
                }
            }
        }

        out.flush();
    }
}

// End MatrixExporter.java
