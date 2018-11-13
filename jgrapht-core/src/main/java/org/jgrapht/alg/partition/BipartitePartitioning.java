/*
 * (C) Copyright 2016-2018, by Dimitrios Michail, Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.partition;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

import static org.jgrapht.GraphTests.isEmpty;

/**
 * Algorithm for computing bipartite partitions thus checking whether a graph is bipartite or not.
 *
 * <p>
 * The algorithm runs in linear time in the number of vertices and edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Alexandru Valeanu
 */
public class BipartitePartitioning<V, E>
    implements
    PartitioningAlgorithm<V>
{

    /* Input graph */
    private Graph<V, E> graph;

    /* Cached bipartite partitioning */
    private boolean computed = false;
    private Partitioning<V> cachedPartitioning;

    /**
     * Constructs a new bipartite partitioning.
     *
     * @param graph the input graph;
     */
    public BipartitePartitioning(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
    }

    /**
     * Test whether the input graph is bipartite.
     *
     * @return true if the input graph is bipartite, false otherwise
     */
    public boolean isBipartite()
    {
        if (isEmpty(graph)) {
            return true;
        }
        try {
            // at most n^2/4 edges
            if (Math.multiplyExact(4, graph.edgeSet().size()) > Math
                .multiplyExact(graph.vertexSet().size(), graph.vertexSet().size()))
            {
                return false;
            }
        } catch (ArithmeticException e) {
            // ignore
        }

        return this.getPartitioning() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Partitioning<V> getPartitioning()
    {
        if (computed) {
            return cachedPartitioning;
        }

        Set<V> unknown = new HashSet<>(graph.vertexSet());
        Set<V> odd = new HashSet<>();
        Deque<V> queue = new ArrayDeque<>();

        while (!unknown.isEmpty()) {
            if (queue.isEmpty()) {
                queue.add(unknown.iterator().next());
            }

            V v = queue.removeFirst();
            unknown.remove(v);

            for (E e : graph.edgesOf(v)) {
                V n = Graphs.getOppositeVertex(graph, e, v);
                if (unknown.contains(n)) {
                    queue.add(n);
                    if (!odd.contains(v)) {
                        odd.add(n);
                    }
                } else if (odd.contains(v) == odd.contains(n)) {
                    computed = true;
                    cachedPartitioning = null;
                    return null;
                }
            }
        }

        Set<V> even = new HashSet<>(graph.vertexSet());
        even.removeAll(odd);

        computed = true;
        cachedPartitioning = new PartitioningImpl<>(Arrays.asList(even, odd));
        return cachedPartitioning;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidPartitioning(Partitioning<V> partitioning)
    {
        Objects.requireNonNull(partitioning, "Partition cannot be null");

        if (partitioning.getNumberPartitions() != 2)
            return false;

        Set<V> firstPartition = partitioning.getPartition(0);
        Set<V> secondPartition = partitioning.getPartition(1);

        Objects.requireNonNull(firstPartition, "First partition class cannot be null");
        Objects.requireNonNull(secondPartition, "Second partition class cannot be null");

        if (graph.vertexSet().size() != firstPartition.size() + secondPartition.size()) {
            return false;
        }

        for (V v : graph.vertexSet()) {
            Collection<? extends V> otherPartition;
            if (firstPartition.contains(v)) {
                otherPartition = secondPartition;
            } else if (secondPartition.contains(v)) {
                otherPartition = firstPartition;
            } else {
                // v does not belong to any of the two partitions
                return false;
            }

            for (E e : graph.edgesOf(v)) {
                V other = Graphs.getOppositeVertex(graph, e, v);
                if (!otherPartition.contains(other)) {
                    return false;
                }
            }
        }

        return true;
    }
}
