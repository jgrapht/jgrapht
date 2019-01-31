/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
