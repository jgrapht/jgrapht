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
 * GraphExporter.java
 * ------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 *
 * Changes
 * -------
 * 17-Aug-2016 : Initial Version (DM);
 *
 */
package org.jgrapht.ext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.jgrapht.Graph;

/**
 * Interface for graph exporters
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