/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
/* -------------------------
 * DOTUtils.java
 * -------------------------
 * (C) Copyright 2003-2016, by Christoph Zauner and Contributors
 *
 * Original Author:  Christoph Zauner
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.ext;

import java.io.StringWriter;

import org.jgrapht.Graph;

/**
 * @author Christoph Zauner
 */
public class DOTUtils
{
    /**
     * Convert a graph into a String in DOT format.
     *  
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a {@link String} representation in DOT format of the given graph.
     */
    public static <V, E> String convertGraphToDotString(
        Graph<V, E> graph)
    {

        StringWriter outputWriter = new StringWriter();
        new DOTExporter<V, E>(
            new IntegerNameProvider<V>(),
            // vertex name provider
            new StringNameProvider<V>(),
            // edge label provider
            null).exportGraph(graph, outputWriter);

        return outputWriter.toString();
    }
}

// End DOTUtils.java
