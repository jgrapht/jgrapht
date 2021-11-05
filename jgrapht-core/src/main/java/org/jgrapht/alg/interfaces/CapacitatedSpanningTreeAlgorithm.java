/*
 * (C) Copyright 2018-2021, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.io.*;
import java.util.*;

/**
 * An algorithm which computes a capacitated (minimum) spanning tree of a given connected graph with
 * a designated root vertex. The input is a connected undirected graph G = (V, E) with a designated
 * root r \in V, a capacity constraint K \in \mathbb{N}, a demand function d: V \rightarrow
 * \mathbb{N} and a capacity function c: E \rightarrow \mathbb{N}. A
 * <a href="https://en.wikipedia.org/wiki/Capacitated_minimum_spanning_tree">Capacitated Minimum
 * Spanning Tree</a> (CMST) is a rooted minimal cost spanning tree that satisfies the capacity
 * constraint on all trees that are connected to the designated root. That is, the sum of the
 * demands of all vertices is smaller or equal than K. These trees build up a partition on the
 * vertex set of the graph. The problem is NP-hard.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface CapacitatedSpanningTreeAlgorithm<V, E>
{
    /**
     * Computes a capacitated spanning tree.
     *
     * @return a capacitated spanning tree
     */
    CapacitatedSpanningTree<V, E> getCapacitatedSpanningTree();

    /**
     * A spanning tree.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface CapacitatedSpanningTree<V, E>
        extends
        Iterable<E>,
        SpanningTreeAlgorithm.SpanningTree<E>
    {

        /**
         * Tests whether <code>cmst</code> is a CMST on <code>graph</code> with root
         * <code>root</code>, capacity <code>capacity</code> and demand function
         * <code>demands</code>.
         *
         * @param graph the graph
         * @param root the expected root of cmst
         * @param capacity the expected capacity of cmst
         * @param demands the demand function
         *
         * @return whether <code>cmst</code> is a CMST
         */
        boolean isCapacitatedSpanningTree(
            Graph<V, E> graph, V root, double capacity, Map<V, Double> demands);

        /**
         * Return the set of labels of the underlying partition of the capacitated spanning tree.
         * The labels are a key to the vertex sets of the partition.
         *
         * @return the label set of the capacitated spanning tree.
         */
        Map<V, Integer> getLabels();

        /**
         * Return the label-to-partition map of the underlying partition of capacitated spanning
         * tree.
         *
         * @return map from labels to the subsets of the partition of the capacitated spanning tree.
         */
        Map<Integer, Pair<Set<V>, Double>> getPartition();
    }

    /**
     * Default implementation of the spanning tree interface.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class CapacitatedSpanningTreeImpl<V, E>
        implements
        CapacitatedSpanningTree<V, E>,
        Serializable
    {

        private static final long serialVersionUID = 7088989899889893333L;

        private final Map<V, Integer> labels;
        private final Map<Integer, Pair<Set<V>, Double>> partition;
        private final double weight;
        private final Set<E> edges;

        /**
         * Construct a new capacitated spanning tree.
         *
         * @param labels the labelling of the vertices marking their subset membership in the
         *        partition
         * @param partition the implicitly defined partition of the vertices in the capacitated
         *        spanning tree
         * @param edges the edge set of the capacitated spanning tree
         * @param weight the weight of the capacitated spanning tree, i.e. the sum of all edge
         *        weights
         */
        public CapacitatedSpanningTreeImpl(
            Map<V, Integer> labels, Map<Integer, Pair<Set<V>, Double>> partition, Set<E> edges,
            double weight)
        {
            this.labels = labels;
            this.partition = partition;
            this.edges = edges;
            this.weight = weight;
        }

        @Override
        public boolean isCapacitatedSpanningTree(
            Graph<V, E> graph, V root, double capacity, Map<V, Double> demands)
        {
            if (this.getEdges().size() != graph.vertexSet().size() - 1) {
                return false;
            }

            // check for disjointness
            for (Pair<Set<V>, Double> set1 : this.getPartition().values()) {
                for (Pair<Set<V>, Double> set2 : this.getPartition().values()) {
                    if (set1 != set2 && !Collections.disjoint(set1.getFirst(), set2.getFirst())) {
                        return false;
                    }
                }
            }

            // check demands and number of vertices
            int numberOfNodesExplored = 0;
            for (Pair<Set<V>, Double> set1 : this.getPartition().values()) {
                int currentCapacity = 0;
                for (V v : set1.getFirst()) {
                    currentCapacity += demands.get(v);
                    numberOfNodesExplored++;
                }
                if (currentCapacity > capacity) {
                    return false;
                }
            }
            if (graph.vertexSet().size() - 1 != numberOfNodesExplored) {
                return false;
            }

            // check if partition and tree correspond to each other
            Graph<V, E> spanningTreeGraph =
                new AsSubgraph<>(graph, graph.vertexSet(), this.getEdges());

            DepthFirstIterator<V, E> depthFirstIterator =
                new DepthFirstIterator<>(spanningTreeGraph, root);
            if (depthFirstIterator.hasNext()) {
                depthFirstIterator.next();
            }

            int numberOfRootEdgesExplored = 0;
            Set<V> currentSubtree = new HashSet<>();

            while (depthFirstIterator.hasNext()) {
                V next = depthFirstIterator.next();

                if (spanningTreeGraph.containsEdge(root, next)) {
                    if (!currentSubtree.isEmpty()) {
                        if (!currentSubtree
                            .equals(
                                this
                                    .getPartition()
                                    .get(this.getLabels().get(currentSubtree.iterator().next()))
                                    .getFirst()))
                        {
                            return false;
                        }
                        currentSubtree = new HashSet<>();
                    }
                    numberOfRootEdgesExplored++;
                }
                currentSubtree.add(next);
            }

            if (numberOfRootEdgesExplored != spanningTreeGraph.degreeOf(root)) {
                return false;
            }

            return true;
        }

        @Override
        public Map<V, Integer> getLabels()
        {
            return labels;
        }

        @Override
        public Map<Integer, Pair<Set<V>, Double>> getPartition()
        {
            return partition;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        @Override
        public String toString()
        {
            return "Capacitated Spanning-Tree [weight=" + weight + ", edges=" + edges + ", labels="
                + labels + ", partition=" + partition + "]";
        }
    }
}
