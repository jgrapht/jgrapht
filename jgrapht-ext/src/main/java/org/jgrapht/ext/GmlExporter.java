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
 * GmlExporter.java
 * ------------------
 * (C) Copyright 2006, by Dimitrios Michail.
 *
 * Original Author:  Dimitrios Michail <dmichail@yahoo.com>
 *
 * $Id$
 *
 * Changes
 * -------
 * 15-Dec-2006 : Initial Version (DM);
 *
 */
package org.jgrapht.ext;

import java.io.*;

import org.jgrapht.*;


/**
 * Exports a graph into a GML file (Graph Modelling Language).
 *
 * <p>For a description of the format see <a
 * href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.</p>
 *
 * <p>The objects associated with vertices and edges are exported as labels
 * using their toString() implementation. See the {@link
 * #setPrintLabels(Integer)} method. The default behavior is to export no label
 * information.</p>
 *
 * @author Dimitrios Michail
 */
public class GmlExporter<V, E>
{
    

    private static final String creator = "JGraphT GML Exporter";
    private static final String version = "1";

    private static final String delim = " ";
    private static final String tab1 = "\t";
    private static final String tab2 = "\t\t";

    // TODO jvs 27-Jan-2008:  convert these to enum

    /**
     * Option to export no vertex or edge labels.
     */
    public static final Integer PRINT_NO_LABELS = 1;

    /**
     * Option to export only the edge labels.
     */
    public static final Integer PRINT_EDGE_LABELS = 2;

    /**
     * Option to export both edge and vertex labels.
     */
    public static final Integer PRINT_EDGE_VERTEX_LABELS = 3;

    /**
     * Option to export only the vertex labels.
     */
    public static final Integer PRINT_VERTEX_LABELS = 4;

    

    private Integer printLabels = PRINT_NO_LABELS;

    private VertexNameProvider<V> vertexIDProvider;
    private VertexNameProvider<V> vertexLabelProvider;
    private EdgeNameProvider<E> edgeIDProvider;
    private EdgeNameProvider<E> edgeLabelProvider;

    

    /**
     * Creates a new GmlExporter object with integer name providers for the
     * vertex and edge IDs and null providers for the vertex and edge labels.
     */
    public GmlExporter()
    {
        this(
            new IntegerNameProvider<V>(),
            null,
            new IntegerEdgeNameProvider<E>(),
            null);
    }

    /**
     * Constructs a new GmlExporter object with the given ID and label
     * providers.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     * labels will be generated using the toString() method of the vertex
     * object.
     * @param edgeIDProvider for generating vertex IDs. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     * will be generated using the toString() method of the edge object.
     */
    public GmlExporter(
        VertexNameProvider<V> vertexIDProvider,
        VertexNameProvider<V> vertexLabelProvider,
        EdgeNameProvider<E> edgeIDProvider,
        EdgeNameProvider<E> edgeLabelProvider)
    {
        this.vertexIDProvider = vertexIDProvider;
        this.vertexLabelProvider = vertexLabelProvider;
        this.edgeIDProvider = edgeIDProvider;
        this.edgeLabelProvider = edgeLabelProvider;
    }

    

    private String quoted(final String s)
    {
        return "\"" + s + "\"";
    }

    private void exportHeader(PrintWriter out)
    {
        out.println("Creator" + delim + quoted(creator));
        out.println("Version" + delim + version);
    }

    private void exportVertices(
        PrintWriter out,
        Graph<V, E> g)
    {
        for (V from : g.vertexSet()) {
            out.println(tab1 + "node");
            out.println(tab1 + "[");
            out.println(
                tab2 + "id" + delim + vertexIDProvider.getVertexName(from));
            if ((printLabels == PRINT_VERTEX_LABELS)
                || (printLabels == PRINT_EDGE_VERTEX_LABELS))
            {
                String label =
                    (vertexLabelProvider == null) ? from.toString()
                    : vertexLabelProvider.getVertexName(from);
                out.println(tab2 + "label" + delim + quoted(label));
            }
            out.println(tab1 + "]");
        }
    }

    private void exportEdges(
        PrintWriter out,
        Graph<V, E> g)
    {
        for (E edge : g.edgeSet()) {
            out.println(tab1 + "edge");
            out.println(tab1 + "[");
            String id = edgeIDProvider.getEdgeName(edge);
            out.println(tab2 + "id" + delim + id);
            String s = vertexIDProvider.getVertexName(g.getEdgeSource(edge));
            out.println(tab2 + "source" + delim + s);
            String t = vertexIDProvider.getVertexName(g.getEdgeTarget(edge));
            out.println(tab2 + "target" + delim + t);
            if ((printLabels == PRINT_EDGE_LABELS)
                || (printLabels == PRINT_EDGE_VERTEX_LABELS))
            {
                String label =
                    (edgeLabelProvider == null) ? edge.toString()
                    : edgeLabelProvider.getEdgeName(edge);
                out.println(tab2 + "label" + delim + quoted(label));
            }
            out.println(tab1 + "]");
        }
    }

    private void export(Writer output, Graph<V, E> g, boolean directed)
    {
        PrintWriter out = new PrintWriter(output);

        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            vertexIDProvider.getVertexName(from);
        }

        exportHeader(out);
        out.println("graph");
        out.println("[");
        out.println(tab1 + "label" + delim + quoted(""));
        if (directed) {
            out.println(tab1 + "directed" + delim + "1");
        } else {
            out.println(tab1 + "directed" + delim + "0");
        }
        exportVertices(out, g);
        exportEdges(out, g);
        out.println("]");
        out.flush();
    }

    /**
     * Exports an undirected graph into a plain text file in GML format.
     *
     * @param output the writer to which the graph to be exported
     * @param g the undirected graph to be exported
     */
    public void export(Writer output, UndirectedGraph<V, E> g)
    {
        export(output, g, false);
    }

    /**
     * Exports a directed graph into a plain text file in GML format.
     *
     * @param output the writer to which the graph to be exported
     * @param g the directed graph to be exported
     */
    public void export(Writer output, DirectedGraph<V, E> g)
    {
        export(output, g, true);
    }

    /**
     * Set whether to export the vertex and edge labels. The default behavior is
     * to export no vertex or edge labels.
     *
     * @param i What labels to export. Valid options are {@link
     * #PRINT_NO_LABELS}, {@link #PRINT_EDGE_LABELS}, {@link
     * #PRINT_EDGE_VERTEX_LABELS}, and {@link #PRINT_VERTEX_LABELS}.
     *
     * @throws IllegalArgumentException if a non-supported value is used
     *
     * @see #PRINT_NO_LABELS
     * @see #PRINT_EDGE_LABELS
     * @see #PRINT_EDGE_VERTEX_LABELS
     * @see #PRINT_VERTEX_LABELS
     */
    public void setPrintLabels(final Integer i)
    {
        if ((i != PRINT_NO_LABELS)
            && (i != PRINT_EDGE_LABELS)
            && (i != PRINT_EDGE_VERTEX_LABELS)
            && (i != PRINT_VERTEX_LABELS))
        {
            throw new IllegalArgumentException(
                "Non-supported parameter value: " + Integer.toString(i));
        }
        printLabels = i;
    }

    /**
     * Get whether to export the vertex and edge labels.
     *
     * @return One of the {@link #PRINT_NO_LABELS}, {@link #PRINT_EDGE_LABELS},
     * {@link #PRINT_EDGE_VERTEX_LABELS}, or {@link #PRINT_VERTEX_LABELS}.
     */
    public Integer getPrintLabels()
    {
        return printLabels;
    }
}

// End GmlExporter.java
