/*
 * (C) Copyright 2018-2018, by Oliver Feith, Dennis Fischer and Contributors.
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
package org.jgrapht.alg.interval;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.LexBreadthFirstIterator;
import org.jgrapht.util.Ordering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that is used to perform the BFS algorithms used to detect interval graphs.
 *
 * @author Oliver Feith
 * @author Dennis Fischer
 */
final class LexBreadthFirstSearch {

    /**
     * Performs a lexicographical BFS starting at {@code startingVertex}.
     *
     * @param <V>   The vertex type
     * @param <E>   The edge type
     * @param graph the graph we want to perform LBFS on
     * @return map representing the order in which the vertices were found
     */
    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearch(Graph<V, E> graph) {
        HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size());
        LexBreadthFirstIterator<V, E> lbfIterator = new LexBreadthFirstIterator<>(graph);

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            result.put(lbfIterator.next(), i);
        }

        return result;
    }

    /**
     * Performs LBFS+ starting at {@code startingVertex} using the previous ordering {@code prevOrdering}.
     *
     * @param graph    the graph we want to perform LBFS on
     * @param priority the priority of vertices for tiebreaking
     * @param <V>      The vertex type
     * @param <E>      The edge type
     * @return an array of vertices representing the order in which the vertices were found
     */

    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearchPlus(Graph<V, E> graph, HashMap<V, Integer> priority) {
        HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size());
        LexBreadthFirstIterator<V, E> lbfIterator = new LexBreadthFirstIterator<>(graph, new Ordering<>(priority));

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            result.put(lbfIterator.next(), i);
        }

        return result;
    }

    /**
     * Performs LBFS* starting at {@code startingVertex} using two previous orderings {@code prevOrdering1} and {@code prevOrdering2}.
     *
     * @param graph     the graph we want to perform LBFS on
     * @param priorityA the first priority of vertices for tiebreaking
     * @param priorityB the second priority of vertices for tiebreaking
     * @param <V>       The vertex type
     * @param <E>       The edge type
     * @return an array of vertices representing the order in which the vertices were found
     */

    public static <V, E> HashMap<V, Integer> lexBreadthFirstSearchStar(Graph<V, E> graph, HashMap<V, Integer> priorityA, HashMap<V, Integer> priorityB) {
        HashMap<V, Integer> neighborIndexA = new HashMap<>();
        HashMap<V, Integer> neighborIndexB = new HashMap<>();

        HashMap<V, Set<V>> aSets = new HashMap<>();
        HashMap<V, Set<V>> bSets = new HashMap<>();

        for (V vertex : graph.vertexSet()) {
            // Compute indexA, indexB
            int maxNeighborA = 0;
            int maxNeighborB = 0;

            List<V> neighbors = Graphs.neighborListOf(graph, vertex);
            neighbors.add(vertex);

            for (V neighbor : neighbors) {
                maxNeighborA = Math.max(maxNeighborA, priorityA.get(neighbor));
                maxNeighborB = Math.max(maxNeighborB, priorityB.get(neighbor));
            }

            neighborIndexA.put(vertex, maxNeighborA);
            neighborIndexB.put(vertex, maxNeighborB);
        }

        for (V vertex : graph.vertexSet()) {
            HashSet<V> aVertices = new HashSet<>();
            HashSet<V> bVertices = new HashSet<>();

            for (V neighbor : Graphs.neighborListOf(graph, vertex)) {
                if (priorityA.get(neighbor) < priorityA.get(vertex) && neighborIndexA.get(neighbor) > priorityA.get(vertex)) {
                    aVertices.add(neighbor);
                }

                if (priorityB.get(neighbor) < priorityB.get(vertex) && neighborIndexB.get(neighbor) > priorityB.get(vertex)) {
                    bVertices.add(neighbor);
                }
            }

            aSets.put(vertex, aVertices);
            bSets.put(vertex, bVertices);
        }

        HashMap<V, Integer> result = new HashMap<>(graph.vertexSet().size());
        LexBreadthFirstIterator<V, E> lbfIterator = new LexBreadthFirstIterator<>(graph, new Ordering<>(priorityA),
                new Ordering<>(priorityB), new Ordering<>(neighborIndexA), new Ordering<>(neighborIndexB), aSets, bSets);

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            result.put(lbfIterator.next(), i);
        }
        return result;
    }
}
