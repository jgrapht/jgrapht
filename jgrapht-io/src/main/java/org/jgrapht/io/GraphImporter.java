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
 * Interface for graph importers
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface GraphImporter<V, E>
{

    /**
     * Import a graph
     * 
     * @param g the graph
     * @param in the input stream
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importGraph(Graph<V, E> g, InputStream in)
        throws ImportException
    {
        importGraph(g, new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Import a graph
     * 
     * @param g the graph
     * @param in the input reader
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    void importGraph(Graph<V, E> g, Reader in)
        throws ImportException;

    /**
     * Import a graph
     * 
     * @param g the graph
     * @param file the file to read from
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importGraph(Graph<V, E> g, File file)
        throws ImportException
    {
        try {
            importGraph(
                g, new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

}

// End GraphImporter.java
