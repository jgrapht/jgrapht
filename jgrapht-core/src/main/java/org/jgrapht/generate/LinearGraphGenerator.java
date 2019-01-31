/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
