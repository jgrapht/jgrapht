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
 * Original Author:  Dimitrios Michail <dimitrios.michail@gmail.com>
 *
 * Changes
 * -------
 * 15-Dec-2006 : Initial Version (DM);
 *
 */
package org.jgrapht.ext;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;

/**
 * Exports a graph into a GML file (Graph Modeling Language).
 *
 * <p>
 * For a description of the format see
 * <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/"> http://www.
 * infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
 * </p>
 * 
 * <p>
 * The behavior of the exporter such as whether to print vertex labels, edge
 * labels, and/or edge weights can be adjusted using the
 * {@link #setParameter(Parameter, boolean) setParameter} method.
 * </p>
 *
 * @author Dimitrios Michail
 */
public class GmlExporter<V, E>
    implements GraphExporter<V, E>
{
    private static final String CREATOR = "JGraphT GML Exporter";
    private static final String VERSION = "1";

    private static final String DELIM = " ";
    private static final String TAB1 = "\t";
    private static final String TAB2 = "\t\t";

    /**
     * Parameters that affect the behavior of the exporter.
     */
    public enum Parameter
    {
        /**
         * If set the exporter outputs edge labels
         */
        EXPORT_EDGE_LABELS,
        /**
         * If set the exporter outputs vertex labels
         */
        EXPORT_VERTEX_LABELS,
        /**
         * If set the exporter outputs edge weights
         */
        EXPORT_EDGE_WEIGHTS
    }

    /**
     * Option to export no vertex or edge labels.
     */
    @Deprecated
    public static final Integer PRINT_NO_LABELS = 1;

    /**
     * Option to export only the edge labels.
     */
    @Deprecated
    public static final Integer PRINT_EDGE_LABELS = 2;

    /**
     * Option to export both edge and vertex labels.
     */
    @Deprecated
    public static final Integer PRINT_EDGE_VERTEX_LABELS = 3;

    /**
     * Option to export only the vertex labels.
     */
    @Deprecated
    public static final Integer PRINT_VERTEX_LABELS = 4;

    private VertexNameProvider<V> vertexIDProvider;
    private VertexNameProvider<V> vertexLabelProvider;
    private EdgeNameProvider<E> edgeIDProvider;
    private EdgeNameProvider<E> edgeLabelProvider;
    private final Set<Parameter> parameters;

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
        this.parameters = new HashSet<>();
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
        boolean exportVertexLabels = parameters
            .contains(Parameter.EXPORT_VERTEX_LABELS);

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
        boolean exportEdgeWeights = parameters
            .contains(Parameter.EXPORT_EDGE_WEIGHTS);
        boolean exportEdgeLabels = parameters
            .contains(Parameter.EXPORT_EDGE_LABELS);

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
     * Exports an undirected graph into a plain text file in GML format.
     *
     * @param writer the writer to which the graph to be exported
     * @param g the undirected graph to be exported
     */
    @Deprecated
    public void export(Writer writer, UndirectedGraph<V, E> g)
    {
        exportGraph(g, writer);
    }

    /**
     * Exports a directed graph into a plain text file in GML format.
     *
     * @param writer the writer to which the graph to be exported
     * @param g the directed graph to be exported
     */
    @Deprecated
    public void export(Writer writer, DirectedGraph<V, E> g)
    {
        exportGraph(g, writer);
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
     * Set whether to export the vertex and edge labels. The default behavior is
     * to export no vertex or edge labels.
     *
     * @param i What labels to export. Valid options are
     *        {@link #PRINT_NO_LABELS}, {@link #PRINT_EDGE_LABELS},
     *        {@link #PRINT_EDGE_VERTEX_LABELS}, and
     *        {@link #PRINT_VERTEX_LABELS}.
     *
     * @throws IllegalArgumentException if a non-supported value is used
     *
     * @see #PRINT_NO_LABELS
     * @see #PRINT_EDGE_LABELS
     * @see #PRINT_EDGE_VERTEX_LABELS
     * @see #PRINT_VERTEX_LABELS
     */
    @Deprecated
    public void setPrintLabels(final Integer i)
    {
        if (i == PRINT_NO_LABELS) {
            parameters.remove(Parameter.EXPORT_VERTEX_LABELS);
            parameters.remove(Parameter.EXPORT_EDGE_LABELS);
        } else if (i == PRINT_EDGE_LABELS) {
            parameters.remove(Parameter.EXPORT_VERTEX_LABELS);
            parameters.add(Parameter.EXPORT_EDGE_LABELS);
        } else if (i == PRINT_VERTEX_LABELS) {
            parameters.add(Parameter.EXPORT_VERTEX_LABELS);
            parameters.remove(Parameter.EXPORT_EDGE_LABELS);
        } else if (i == PRINT_EDGE_VERTEX_LABELS) {
            parameters.add(Parameter.EXPORT_VERTEX_LABELS);
            parameters.add(Parameter.EXPORT_EDGE_LABELS);
        } else {
            throw new IllegalArgumentException(
                "Non-supported parameter value: " + Integer.toString(i));
        }
    }

    /**
     * Get whether to export the vertex and edge labels.
     *
     * @return One of the {@link #PRINT_NO_LABELS}, {@link #PRINT_EDGE_LABELS},
     *         {@link #PRINT_EDGE_VERTEX_LABELS}, or
     *         {@link #PRINT_VERTEX_LABELS}.
     */
    @Deprecated
    public Integer getPrintLabels()
    {
        if (parameters.contains(Parameter.EXPORT_VERTEX_LABELS)) {
            if (parameters.contains(Parameter.EXPORT_EDGE_LABELS)) {
                return PRINT_EDGE_VERTEX_LABELS;
            } else {
                return PRINT_VERTEX_LABELS;
            }
        } else {
            if (parameters.contains(Parameter.EXPORT_EDGE_LABELS)) {
                return PRINT_EDGE_LABELS;
            } else {
                return PRINT_NO_LABELS;
            }
        }
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

}

// End GmlExporter.java
