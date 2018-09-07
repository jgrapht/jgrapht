/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import org.jgrapht.*;

import java.io.*;
import java.util.*;

/**
 * Exports a graph into DIMACS format.
 *
 * <p>
 * For a description of the format see <a href="http://dimacs.rutgers.edu/Challenges/">
 * http://dimacs.rutgers.edu/Challenges</a>. Note that there are a lot of different formats based on
 * each different challenge, see {@link DIMACSFormat} for the supported formats. The exporter uses
 * the {@link DIMACSFormat#MAX_CLIQUE} by default.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @since January 2017
 */
public class DIMACSExporter<V, E>
    extends
    AbstractBaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    /**
     * The default format used by the exporter.
     */
    public static final DIMACSFormat DEFAULT_DIMACS_FORMAT = DIMACSFormat.MAX_CLIQUE;

    private static final String HEADER = "Generated using the JGraphT library";

    private final Set<Parameter> parameters;
    private DIMACSFormat format;

    /**
     * Parameters that affect the behavior of the {@link DIMACSExporter} exporter.
     */
    public enum Parameter
    {
        /**
         * If set the exporter outputs edge weights
         */
        EXPORT_EDGE_WEIGHTS,
    }

    /**
     * Constructs a new exporter.
     */
    public DIMACSExporter()
    {
        this(new IntegerComponentNameProvider<>());
    }

    /**
     * Constructs a new exporter with a given vertex ID provider.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     */
    public DIMACSExporter(ComponentNameProvider<V> vertexIDProvider)
    {
        this(vertexIDProvider, DEFAULT_DIMACS_FORMAT);
    }

    /**
     * Constructs a new exporter with a given vertex ID provider.
     *
     * @param vertexIDProvider for generating vertex IDs. Must not be null.
     * @param format the format to use
     */
    public DIMACSExporter(ComponentNameProvider<V> vertexIDProvider, DIMACSFormat format)
    {
        super(vertexIDProvider);
        this.format = Objects.requireNonNull(format, "Format cannot be null");
        this.parameters = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        out.println("c");
        out.println("c SOURCE: " + HEADER);
        out.println("c");
        out.println(
            "p " + format.getProblem() + " " + g.vertexSet().size() + " " + g.edgeSet().size());

        boolean exportEdgeWeights = parameters.contains(Parameter.EXPORT_EDGE_WEIGHTS);

        for (E edge : g.edgeSet()) {
            out.print(format.getEdgeDescriptor());
            out.print(" ");
            out.print(vertexIDProvider.getName(g.getEdgeSource(edge)));
            out.print(" ");
            out.print(vertexIDProvider.getName(g.getEdgeTarget(edge)));
            if (exportEdgeWeights) {
                out.print(" ");
                out.print(Double.toString(g.getEdgeWeight(edge)));
            }
            out.println();
        }

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

    /**
     * Get the format of the exporter
     * 
     * @return the format of the exporter
     */
    public DIMACSFormat getFormat()
    {
        return format;
    }

    /**
     * Set the format of the exporter
     * 
     * @param format the format to use
     */
    public void setFormat(DIMACSFormat format)
    {
        this.format = Objects.requireNonNull(format, "Format cannot be null");
    }

}
