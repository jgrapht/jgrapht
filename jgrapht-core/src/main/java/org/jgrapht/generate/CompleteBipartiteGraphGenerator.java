/*
 * (C) Copyright 2008-2021, by Andrew Newell and Contributors.
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
import org.jgrapht.util.*;

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
    private final Set<V> partitionA, partitionB;

    /**
     * Creates a new CompleteBipartiteGraphGenerator object.
     *
     * @param partitionA number of vertices in the first partition
     * @param partitionB number of vertices in the second partition
     */
    public CompleteBipartiteGraphGenerator(int partitionA, int partitionB)
    {
        if (partitionA < 0 || partitionB < 0) {
            throw new IllegalArgumentException("partition sizes must be non-negative");
        }
        this.sizeA = partitionA;
        this.sizeB = partitionB;
        this.partitionA = CollectionUtil.newLinkedHashSetWithExpectedSize(sizeA);
        this.partitionB = CollectionUtil.newLinkedHashSetWithExpectedSize(sizeB);
    }

    /**
     * Creates a new CompleteBipartiteGraphGenerator object. A complete bipartite graph is generated
     * on the vertices provided between the vertices provided in the two partitions. Note that
     * <i>all</i> vertices in both {@code partitionA} and {@code partitionB} must be present in the
     * graph or an exception will be thrown during the invocation of
     * {@link #generateGraph(Graph, Map)}
     *
     * @param partitionA first partition
     * @param partitionB second partition
     */
    public CompleteBipartiteGraphGenerator(Set<V> partitionA, Set<V> partitionB)
    {
        if (partitionA.isEmpty() || partitionB.isEmpty()) {
            throw new IllegalArgumentException("partitions must be non-empty");
        }
        this.sizeA = 0;
        this.sizeB = 0;
        this.partitionA = partitionA;
        this.partitionB = partitionB;
    }

    /**
     * Construct a complete bipartite graph
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // Create vertices in each of the partitions
        for (int i = 0; i < sizeA; i++) {
            partitionA.add(target.addVertex());
        }
        for (int i = 0; i < sizeB; i++) {
            partitionB.add(target.addVertex());
        }

        // Add an edge for each pair of vertices in different partitions
        for (V u : partitionA) {
            for (V v : partitionB) {
                target.addEdge(u, v);
            }
        }
    }
}
