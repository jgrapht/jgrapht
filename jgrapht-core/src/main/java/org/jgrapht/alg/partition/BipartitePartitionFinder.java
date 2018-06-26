/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.partition;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.PartitionAlgorithm;

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
 * @since June 2018
 */
public class BipartitePartitionFinder<V, E> implements PartitionAlgorithm<V> {

    /* Input graph */
    private Graph<V, E> graph;

    /* Cached bipartite partition */
    private boolean computed = false;
    private Partition<V> cachedPartition;

    /**
     * Constructs a new bipartite partition finder.
     *
     * @param graph the input graph;
     */
    public BipartitePartitionFinder(Graph<V, E> graph){
        this.graph = Objects.requireNonNull(graph);
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

        return this.getPartition() != null;
    }

    @Override
    public Partition<V> getPartition() {
        if (computed)
            return cachedPartition;

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
                    cachedPartition = null;
                    return null;
                }
            }
        }

        Set<V> even = new HashSet<>(graph.vertexSet());
        even.removeAll(odd);

        computed = true;
        cachedPartition = new PartitionImpl<>(even, odd);
        return cachedPartition;
    }

    @Override
    public boolean isValidPartition(Partition<V> partition){
        Objects.requireNonNull(partition);

        Set<V> firstPartition = partition.getFirstPartition();
        Set<V> secondPartition = partition.getSecondPartition();

        Objects.requireNonNull(firstPartition);
        Objects.requireNonNull(secondPartition);

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
