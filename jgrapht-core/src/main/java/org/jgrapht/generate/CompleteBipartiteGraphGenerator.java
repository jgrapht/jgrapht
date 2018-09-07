/*
 * (C) Copyright 2008-2018, by Andrew Newell and Contributors.
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
 * Generates a <a href="http://mathworld.wolfram.com/CompleteBipartiteGraph.html">complete bipartite
 * graph</a> of any size. This is a graph with two partitions; two vertices will contain an edge if
 * and only if they belong to different partitions.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public class CompleteBipartiteGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int sizeA, sizeB;

    /**
     * Creates a new CompleteBipartiteGraphGenerator object.
     *
     * @param partitionOne number of vertices in the first partition
     * @param partitionTwo number of vertices in the second partition
     */
    public CompleteBipartiteGraphGenerator(int partitionOne, int partitionTwo)
    {
        if (partitionOne < 0 || partitionTwo < 0) {
            throw new IllegalArgumentException("partition sizes must be non-negative");
        }
        this.sizeA = partitionOne;
        this.sizeB = partitionTwo;
    }

    /**
     * Construct a complete bipartite graph
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if ((sizeA < 1) && (sizeB < 1)) {
            return;
        }

        // Create vertices in each of the partitions
        Set<V> a = new HashSet<>();
        Set<V> b = new HashSet<>();
        for (int i = 0; i < sizeA; i++) {
            a.add(target.addVertex());
        }
        for (int i = 0; i < sizeB; i++) {
            b.add(target.addVertex());
        }

        // Add an edge for each pair of vertices in different partitions
        for (V u : a) {
            for (V v : b) {
                target.addEdge(u, v);
            }
        }
    }
}

// End CompleteBipartiteGraphGenerator.java
