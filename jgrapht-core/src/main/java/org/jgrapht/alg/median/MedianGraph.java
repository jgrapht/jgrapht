/*
 * (C) Copyright 2024-2024, by Louis Depiesse, Auguste Célérier and Contributors.
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

package org.jgrapht.alg.median;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides utility methods to determine if a graph is a median graph.
 * A median graph is a graph where, for any triplet of vertices (u, v, w),
 * there exists exactly one vertex that minimizes the sum of distances to these three vertices.
 * 
 */
public class MedianGraph {

    /**
     * Checks if the given graph is a median graph.
     *
     * A graph is defined as median if, for every triplet of distinct vertices (u, v, w),
     * there exists exactly one vertex (called the "median") that minimizes the sum of distances
     * from itself to u, v, and w.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @param graph the graph to check
     * @return {@code true} if the graph is a median graph; {@code false} otherwise
     * @throws IllegalArgumentException if the graph is null or empty
     */
    public static <V, E> boolean isMedian(Graph<V, E> graph) {
        if (graph == null || graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph cannot be null or empty.");
        }

        // Get all vertices from the graph
        Set<V> vertices = graph.vertexSet();

        // Iterate over all triplets of vertices
        for (V u : vertices) {
            for (V v : vertices) {
                for (V w : vertices) {
                    // Skip triplets with repeated vertices
                    if (u.equals(v) || v.equals(w) || u.equals(w)) {
                        continue;
                    }

                    // Find the set of possible medians
                    Set<V> medians = findMedians(graph, u, v, w);

                    // A graph is median if and only if there is exactly one median for each triplet
                    if (medians.size() != 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Finds the set of "median" vertices for a given triplet of vertices (u, v, w).
     *
     * A vertex is a median if it minimizes the sum of distances from itself to u, v, and w.
     * This method uses Dijkstra's algorithm to compute shortest path distances.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @param graph the graph in which to find the medians
     * @param u the first vertex in the triplet
     * @param v the second vertex in the triplet
     * @param w the third vertex in the triplet
     * @return a set of vertices that minimize the sum of distances to u, v, and w
     */
    private static <V, E> Set<V> findMedians(Graph<V, E> graph, V u, V v, V w) {
        Set<V> medians = new HashSet<>();
        DijkstraShortestPath<V, E> dijkstra = new DijkstraShortestPath<>(graph);

        double minDistanceSum = Double.MAX_VALUE;

        // Iterate over all vertices to find the medians
        for (V candidate : graph.vertexSet()) {
            double distanceSum =
                    dijkstra.getPathWeight(u, candidate) +
                    dijkstra.getPathWeight(v, candidate) +
                    dijkstra.getPathWeight(w, candidate);

            if (distanceSum < minDistanceSum) {
                minDistanceSum = distanceSum;
                medians.clear();
                medians.add(candidate);
            } else if (distanceSum == minDistanceSum) {
                medians.add(candidate);
            }
        }

        return medians;
    }
}
