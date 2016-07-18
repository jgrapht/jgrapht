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
 * (C) Copyright 2006-2016, by Dimitrios Michail.
 *
 * Original Author:  Dimitrios Michail <dimitrios.michail@gmail.com>
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
 * Exports a graph into a GML file (Graph Modeling Language).
 *
 * <p>
 * For a description of the format see
 * <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
 * </p>
 *
 * @author Dimitrios Michail
 */
public class GmlExporter<V, E>
{
    private static final String CREATOR = "JGraphT GML Exporter";
    private static final String VERSION = "1";

    private static final String DELIM = " ";
    private static final String TAB1 = "\t";
    private static final String TAB2 = "\t\t";

    /**
     * Whether to print edge weights in case the graph is weighted.
     */
    private boolean exportEdgeWeights = false;

    /**
     * Whether to print vertex labels
     */
    private boolean exportVertexLabels = false;

    /**
     * Whether to print edge labels
     */
    private boolean exportEdgeLabels = false;

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
            new IntegerNameProvider<>(),
            null,
            new IntegerEdgeNameProvider<>(),
            null);
    }

    /**
     * Constructs a new GmlExporter object with the given ID and label
     * providers.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param vertexLabelProvider for generating vertex labels. If null, vertex
     *        labels will be generated using the toString() method of the vertex
     *        object.
     * @param edgeIDProvider for generating vertex IDs. Must not be null.
     * @param edgeLabelProvider for generating edge labels. If null, edge labels
     *        will be generated using the toString() method of the edge object.
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
        out.println("Creator" + DELIM + quoted(CREATOR));
        out.println("Version" + DELIM + VERSION);
    }

    private void exportVertices(PrintWriter out, Graph<V, E> g)
    {
        for (V from : g.vertexSet()) {
            out.println(TAB1 + "node");
            out.println(TAB1 + "[");
            out.println(
                TAB2 + "id" + DELIM + vertexIDProvider.getVertexName(from));
            if (exportVertexLabels) {
                String label = (vertexLabelProvider == null) ? from.toString()
                    : vertexLabelProvider.getVertexName(from);
                out.println(TAB2 + "label" + DELIM + quoted(label));
            }
            out.println(TAB1 + "]");
        }
    }

    private void exportEdges(PrintWriter out, Graph<V, E> g)
    {
        for (E edge : g.edgeSet()) {
            out.println(TAB1 + "edge");
            out.println(TAB1 + "[");
            String id = edgeIDProvider.getEdgeName(edge);
            out.println(TAB2 + "id" + DELIM + id);
            String s = vertexIDProvider.getVertexName(g.getEdgeSource(edge));
            out.println(TAB2 + "source" + DELIM + s);
            String t = vertexIDProvider.getVertexName(g.getEdgeTarget(edge));
            out.println(TAB2 + "target" + DELIM + t);
            if (exportEdgeLabels) {
                String label = (edgeLabelProvider == null) ? edge.toString()
                    : edgeLabelProvider.getEdgeName(edge);
                out.println(TAB2 + "label" + DELIM + quoted(label));
            }
            if (exportEdgeWeights && g instanceof WeightedGraph) {
                out.println(
                    TAB2 + "weight" + DELIM
                        + Double.toString(g.getEdgeWeight(edge)));
            }
            out.println(TAB1 + "]");
        }
    }

    /**
     * Exports an graph into a plain text GML format.
     *
     * @param writer the writer
     * @param g the graph
     */
    public void export(Writer writer, Graph<V, E> g)
    {
        PrintWriter out = new PrintWriter(writer);

        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            vertexIDProvider.getVertexName(from);
        }

        exportHeader(out);
        out.println("graph");
        out.println("[");
        out.println(TAB1 + "label" + DELIM + quoted(""));
        if (g instanceof DirectedGraph<?, ?>) {
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
     * Whether the exporter will print vertex labels. If no vertex label
     * provider is supplied, the toString() method of the vertex class is used.
     * 
     * @return {@code true} if the exporter prints vertex labels, {@code false}
     *         otherwise
     */
    public boolean isExportVertexLabels()
    {
        return exportVertexLabels;
    }

    /**
     * Set whether the exporter will print vertex labels
     *
     * @param exportVertexLabels value to set
     */
    public void setExportVertexLabels(boolean exportVertexLabels)
    {
        this.exportVertexLabels = exportVertexLabels;
    }

    /**
     * Whether the exporter will print edge labels. If no edge label provider is
     * supplied, the toString() method of the edge class is used.
     * 
     * @return {@code true} if the exporter prints edge labels, {@code false}
     *         otherwise
     */
    public boolean isExportEdgeLabels()
    {
        return exportEdgeLabels;
    }

    /**
     * Set whether the exporter will print edge labels
     *
     * @param exportEdgeLabels value to set
     */
    public void setExportEdgeLabels(boolean exportEdgeLabels)
    {
        this.exportEdgeLabels = exportEdgeLabels;
    }

    /**
     * Whether the exporter will print edge weights in case the graph is edge
     * weighted.
     *
     * @return {@code true} if the exporter prints edge weights, {@code false}
     *         otherwise
     */
    public boolean isExportEdgeWeights()
    {
        return exportEdgeWeights;
    }

    /**
     * Set whether the exporter will print edge weights in case the graph is
     * edge weighted.
     *
     * @param exportEdgeWeights value to set
     */
    public void setExportEdgeWeights(boolean exportEdgeWeights)
    {
        this.exportEdgeWeights = exportEdgeWeights;
    }
}

// End GmlExporter.java
