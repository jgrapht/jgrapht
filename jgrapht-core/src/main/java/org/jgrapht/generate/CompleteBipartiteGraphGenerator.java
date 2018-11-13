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
 * Generates a <a href="http://mathworld.wolfram.com/CompleteBipartiteGraph.html">complete bipartite
 * graph</a> of any size. This is a graph with two partitions; two vertices will contain an edge if
 * and only if they belong to different partitions.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andrew Newell
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
