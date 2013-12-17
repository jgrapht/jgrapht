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
/* -------------------
 * LinearGraphGenerator.java
 * -------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;


/**
 * Generates a linear graph of any size. For a directed graph, the edges are
 * oriented from START_VERTEX to END_VERTEX.
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public class LinearGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    

    /**
     * Role for the first vertex generated.
     */
    public static final String START_VERTEX = "Start Vertex";

    /**
     * Role for the last vertex generated.
     */
    public static final String END_VERTEX = "End Vertex";

    

    private int size;

    

    /**
     * Construct a new LinearGraphGenerator.
     *
     * @param size number of vertices to be generated
     *
     * @throws IllegalArgumentException if the specified size is negative.
     */
    public LinearGraphGenerator(int size)
    {
        if (size < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        this.size = size;
    }

    

    /**
     * {@inheritDoc}
     */
    public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        V lastVertex = null;

        for (int i = 0; i < size; ++i) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);

            if (lastVertex == null) {
                if (resultMap != null) {
                    resultMap.put(START_VERTEX, newVertex);
                }
            } else {
                target.addEdge(lastVertex, newVertex);
            }

            lastVertex = newVertex;
        }

        if ((resultMap != null) && (lastVertex != null)) {
            resultMap.put(END_VERTEX, lastVertex);
        }
    }
}

// End LinearGraphGenerator.java
