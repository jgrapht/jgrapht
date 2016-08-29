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
 * GraphImporter.java
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.jgrapht.Graph;

/**
 * Interface for graph importers
 */
public interface GraphImporter<V, E>
{

    /**
     * Import a graph
     * 
     * @param g the graph
     * @param in the input stream
     * @throws ImportException in case any error occurs, such as I/O or parse
     *         error
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
     * @throws ImportException in case any error occurs, such as I/O or parse
     *         error
     */
    void importGraph(Graph<V, E> g, Reader in)
        throws ImportException;

    /**
     * Import a graph
     * 
     * @param g the graph
     * @param file the file to read from
     * @throws ImportException in case any error occurs, such as I/O or parse
     *         error
     */
    default void importGraph(Graph<V, E> g, File file)
        throws ImportException
    {
        try {
            importGraph(
                g,
                new InputStreamReader(
                    new FileInputStream(file),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

}

// End GraphImporter.java