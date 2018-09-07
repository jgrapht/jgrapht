/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
import java.nio.charset.*;

/**
 * Interface for graph exporters
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface GraphExporter<V, E>
{

    /**
     * Export a graph
     * 
     * @param g the graph to export
     * @param out the output stream
     * @throws ExportException in case any error occurs
     */
    default void exportGraph(Graph<V, E> g, OutputStream out)
        throws ExportException
    {
        exportGraph(g, new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    /**
     * Export a graph
     * 
     * @param g the graph to export
     * @param writer the output writer
     * @throws ExportException in case any error occurs
     */
    void exportGraph(Graph<V, E> g, Writer writer)
        throws ExportException;

    /**
     * Export a graph
     * 
     * @param g the graph to export
     * @param file the file to write to
     * @throws ExportException in case any error occurs
     */
    default void exportGraph(Graph<V, E> g, File file)
        throws ExportException
    {
        try {
            exportGraph(g, new FileWriter(file));
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

}

// End GraphExporter.java
