/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io.edgelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.jgrapht.io.ImportException;

/**
 * Interface for an edge list importer
 */
public interface EdgeListImporter
{

    /**
     * Import an edge list
     * 
     * @param in the input stream
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importEdgeList(InputStream in)
        throws ImportException
    {
        importEdgeList(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Import an edge list
     * 
     * @param input the input reader
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    void importEdgeList(Reader input)
        throws ImportException;

    /**
     * Import an edge list
     * 
     * @param file the file to read from
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importEdgeList(File file)
        throws ImportException
    {
        try {
            importEdgeList(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

}
