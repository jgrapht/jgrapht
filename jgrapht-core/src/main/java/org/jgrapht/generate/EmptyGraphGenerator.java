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
 * Generates an <a href="http://mathworld.wolfram.com/EmptyGraph.html">empty graph</a> of any size.
 * An empty graph is a graph that has no edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public class EmptyGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int size;

    /**
     * Construct a new EmptyGraphGenerator.
     *
     * @param size number of vertices to be generated
     * @throws IllegalArgumentException if the specified size is negative.
     */
    public EmptyGraphGenerator(int size)
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
        for (int i = 0; i < size; ++i) {
            target.addVertex();
        }
    }
}

// End EmptyGraphGenerator.java
