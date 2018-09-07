/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
