/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generates a linear graph of any size. For a directed graph, the edges are oriented from
 * START_VERTEX to END_VERTEX.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public class LinearGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
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
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        V lastVertex = null;

        for (int i = 0; i < size; ++i) {
            V newVertex = target.addVertex();

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
