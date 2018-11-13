/*
 * (C) Copyright 2008-2018, by Andrew Newell and Contributors.
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
 * Generates a <a href="http://mathworld.wolfram.com/StarGraph.html">star graph</a> of any size.
 * This is a graph where every vertex has exactly one edge with a center vertex.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andrew Newell
 */
public class StarGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    public static final String CENTER_VERTEX = "Center Vertex";

    private final int order;

    /**
     * Creates a new StarGraphGenerator object.
     *
     * @param order number of total vertices including the center vertex
     * @throws IllegalArgumentException if the order is negative
     */
    public StarGraphGenerator(int order)
    {
        if (order < 0) {
            throw new IllegalArgumentException("Order must be non-negative");
        }
        this.order = order;
    }

    /**
     * Generates a star graph with the designated order from the constructor
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (order < 1) {
            return;
        }

        // Create center vertex
        V centerVertex = target.addVertex();
        if (resultMap != null) {
            resultMap.put(CENTER_VERTEX, centerVertex);
        }

        // Create other vertices
        for (int i = 0; i < (order - 1); i++) {
            target.addEdge(target.addVertex(), centerVertex);
        }
    }
}
