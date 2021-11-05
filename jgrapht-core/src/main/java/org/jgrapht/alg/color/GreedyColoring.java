/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * The greedy coloring algorithm.
 *
 * <p>
 * The algorithm iterates over all vertices and assigns the smallest possible color that is not used
 * by any neighbors. Subclasses may provide a different vertex ordering.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class GreedyColoring<V, E>
    implements
    VertexColoringAlgorithm<V>
{
    /**
     * Error message if the input graph contains self-loops.
     */
    protected static final String SELF_LOOPS_NOT_ALLOWED = "Self-loops not allowed";

    /**
     * The input graph
     */
    protected final Graph<V, E> graph;

    /**
     * Construct a new coloring algorithm.
     * 
     * @param graph the input graph
     */
    public GreedyColoring(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * Get the ordering of the vertices used by the algorithm.
     * 
     * @return the ordering of the vertices used by the algorithm
     */
    protected Iterable<V> getVertexOrdering()
    {
        return graph.vertexSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coloring<V> getColoring()
    {
        int maxColor = -1;
        Map<V, Integer> colors = new HashMap<>();
        Set<Integer> used = new HashSet<>();

        for (V v : getVertexOrdering()) {
            // find used colors
            for (E e : graph.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (v.equals(u)) {
                    throw new IllegalArgumentException(SELF_LOOPS_NOT_ALLOWED);
                }
                if (colors.containsKey(u)) {
                    used.add(colors.get(u));
                }
            }

            // find first free
            int candidate = 0;
            while (used.contains(candidate)) {
                candidate++;
            }
            used.clear();

            // set color
            colors.put(v, candidate);
            maxColor = Math.max(maxColor, candidate);
        }

        return new ColoringImpl<>(colors, maxColor + 1);
    }

}
