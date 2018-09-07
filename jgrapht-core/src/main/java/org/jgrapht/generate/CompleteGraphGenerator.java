/*
 * (C) Copyright 2003-2018, by Tim Shearouse and Contributors.
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
 * Generates a complete graph of any size.
 * 
 * <p>
 * A complete graph is a graph where every vertex shares an edge with every other vertex. If it is a
 * directed graph, then edges must always exist in both directions.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Tim Shearouse
 * @since Nov 02, 2008
 */
public class CompleteGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int size;

    /**
     * Construct a new CompleteGraphGenerator.
     *
     * @param size number of vertices to be generated
     * @throws IllegalArgumentException if the specified size is negative
     */
    public CompleteGraphGenerator(int size)
    {
        if (size < 0) {
            throw new IllegalArgumentException("size must be non-negative");
        }
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (size < 1) {
            return;
        }

        /*
         * Ensure directed or undirected
         */
        GraphTests.requireDirectedOrUndirected(target);
        boolean isDirected = target.getType().isDirected();

        /*
         * Add vertices
         */
        List<V> nodes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodes.add(target.addVertex());
        }

        /*
         * Add edges
         */
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                V v = nodes.get(i);
                V u = nodes.get(j);
                target.addEdge(v, u);
                if (isDirected) {
                    target.addEdge(u, v);
                }
            }
        }
    }
}

// End CompleteGraphGenerator.java
