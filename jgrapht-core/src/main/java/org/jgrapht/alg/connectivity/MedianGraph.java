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

package org.jgrapht.alg.connectivity;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides utility methods to determine if a graph is a median graph.
 * 
 * A median graph is a graph where, for any triplet of vertices (u, v, w),
 * there exists exactly one vertex that minimizes the sum of distances to these three vertices.
 */
public class MedianGraph {
    
    /**
     * Checks if the given graph is a median graph.
     *
     * A graph is defined as median if, for every triplet of distinct vertices (u, v, w),
     * there exists exactly one vertex (called the "median") that minimizes the sum of distances
     * from itself to u, v, and w.
     *
     * @param <V> the type of vertices in the graph
     * @param <E> the type of edges in the graph
     * @param graph the graph to check
     * @return {@code true} if the graph is a median graph; {@code false} otherwise
     * @throws IllegalArgumentException if the graph is null or empty
     */
    public static <V, E> boolean isMedian(Graph<V, E> graph) {
        if (graph == null || graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph cannot be null or empty.");
        }

        // Precompute all-pairs shortest distances using Floyd-Warshall algorithm
        FloydWarshallShortestPaths<V, E> floydWarshall = new FloydWarshallShortestPaths<>(graph);
        Map<Pair<V, V>, Double> distanceMap = precomputeDistances(graph, floydWarshall);

        // Retrieve all vertices from the graph
        Set<V> vertices = graph.vertexSet();

        // Check the median condition for every triplet of vertices
        for (V u : vertices) {
            for (V v : vertices) {
                for (V w : vertices) {
                    if (u.equals(v) || v.equals(w) || u.equals(w)) {
                        continue;
                    }

                    // Find the medians for the triplet (u, v, w)
                    Set<V> medians = findMedians(distanceMap, vertices, u, v, w);

                    if (medians.size() != 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Precomputes all-pairs shortest distances using the Floyd-Warshall algorithm.
     *
     * The distances are stored in a map for efficient access.
     *
     * @param <V> the type of vertices in the graph
     * @param graph the graph to compute distances for
     * @param floydWarshall the Floyd-Warshall shortest path algorithm instance
     * @return a map containing the shortest distances between all pairs of vertices
     */
    private static <V> Map<Pair<V, V>, Double> precomputeDistances(Graph<V, ?> graph, FloydWarshallShortestPaths<V, ?> floydWarshall) {
        Map<Pair<V, V>, Double> distanceMap = new HashMap<>();
        for (V u : graph.vertexSet()) {
            for (V v : graph.vertexSet()) {
                double distance = floydWarshall.getPathWeight(u, v);
                distanceMap.put(Pair.of(u, v), distance);
            }
        }
        return distanceMap;
    }

    /**
     * Finds the set of "median" vertices for a given triplet of vertices (u, v, w).
     *
     * A vertex is a median if it minimizes the sum of distances from itself to u, v, and w.
     *
     * @param <V> the type of vertices in the graph
     * @param distanceMap a map containing the precomputed shortest distances between all pairs of vertices
     * @param vertices the set of all vertices in the graph
     * @param u the first vertex in the triplet
     * @param v the second vertex in the triplet
     * @param w the third vertex in the triplet
     * @return a set of vertices that minimize the sum of distances to u, v, and w
     */
    private static <V> Set<V> findMedians(Map<Pair<V, V>, Double> distanceMap, Set<V> vertices, V u, V v, V w) {
        Set<V> medians = new HashSet<>();
        double minDistanceSum = Double.MAX_VALUE;

        // Iterate through all vertices to find the medians
        for (V candidate : vertices) {
            double distanceSum =
                    distanceMap.get(Pair.of(u, candidate)) +
                    distanceMap.get(Pair.of(v, candidate)) +
                    distanceMap.get(Pair.of(w, candidate));

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
