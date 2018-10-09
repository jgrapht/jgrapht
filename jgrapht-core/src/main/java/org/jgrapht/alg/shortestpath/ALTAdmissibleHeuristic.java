/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * An admissible heuristic for the A* algorithm using a set of landmarks and the triangle
 * inequality. Assumes that the graph contains non-negative edge weights.
 *
 * <p>
 * The heuristic requires a set of input nodes from the graph, which are used as landmarks. During a
 * pre-processing phase, which requires two shortest path computations per landmark using Dijkstra's
 * algorithm, all distances to and from these landmark nodes are computed and stored. Afterwards,
 * the heuristic estimates the distance from a vertex to another vertex using the already computed
 * distances to and from the landmarks and the fact that shortest path distances obey the
 * triangle-inequality. The heuristic's space requirement is $O(n)$ per landmark where n is the
 * number of vertices of the graph. In case of undirected graphs only one Dijkstra's algorithm
 * execution is performed per landmark.
 * 
 * <p>
 * The method generally abbreviated as ALT (from A*, Landmarks and Triangle inequality) is described
 * in detail in the following <a href=
 * "https://www.microsoft.com/en-us/research/publication/computing-the-shortest-path-a-search-meets-graph-theory">
 * paper</a> which also contains a discussion on landmark selection strategies.
 * <ul>
 * <li>Andrew Goldberg and Chris Harrelson. Computing the shortest path: A* Search Meets Graph
 * Theory. In Proceedings of the sixteenth annual ACM-SIAM symposium on Discrete algorithms (SODA'
 * 05), 156--165, 2005.</li>
 * </ul>
 * 
 * <p>
 * Note that using this heuristic does not require the edge weights to satisfy the
 * triangle-inequality. The method depends on the triangle inequality with respect to the shortest
 * path distances in the graph, not an embedding in Euclidean space or some other metric, which need
 * not be present.
 * 
 * <p>
 * In general more landmarks will speed up A* but will need more space. Given an A* query with
 * vertices source and target, a good landmark appears "before" source or "after" target where
 * before and after are relative to the "direction" from source to target.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class ALTAdmissibleHeuristic<V, E>
    implements
    AStarAdmissibleHeuristic<V>
{
    private final Graph<V, E> graph;
    private final Comparator<Double> comparator;
    private final Map<V, Map<V, Double>> fromLandmark;
    private final Map<V, Map<V, Double>> toLandmark;
    private final boolean directed;

    /**
     * Constructs a new {@link AStarAdmissibleHeuristic} using a set of landmarks.
     * 
     * @param graph the graph
     * @param landmarks a set of vertices of the graph which will be used as landmarks
     * 
     * @throws IllegalArgumentException if no landmarks are provided
     * @throws IllegalArgumentException if the graph contains edges with negative weights
     */
    public ALTAdmissibleHeuristic(Graph<V, E> graph, Set<V> landmarks)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(landmarks, "Landmarks cannot be null");
        if (landmarks.isEmpty()) {
            throw new IllegalArgumentException("At least one landmark must be provided");
        }
        this.fromLandmark = new HashMap<>();
        if (graph.getType().isDirected()) {
            this.directed = true;
            this.toLandmark = new HashMap<>();
        } else if (graph.getType().isUndirected()) {
            this.directed = false;
            this.toLandmark = this.fromLandmark;
        } else {
            throw new IllegalArgumentException("Graph must be directed or undirected");
        }
        this.comparator = new ToleranceDoubleComparator();

        // precomputation and validation
        for (V v : landmarks) {
            for (E e : graph.edgesOf(v)) {
                if (comparator.compare(graph.getEdgeWeight(e), 0d) < 0) {
                    throw new IllegalArgumentException("Graph edge weights cannot be negative");
                }
            }
            precomputeToFromLandmark(v);
        }
    }

    /**
     * An admissible heuristic estimate from a source vertex to a target vertex. The estimate is
     * always non-negative and never overestimates the true distance.
     * 
     * @param u the source vertex
     * @param t the target vertex
     * 
     * @return an admissible heuristic estimate
     */
    @Override
    public double getCostEstimate(V u, V t)
    {
        double maxEstimate = 0d;

        /*
         * Special case, source equals target
         */
        if (u.equals(t)) {
            return maxEstimate;
        }

        /*
         * Special case, source is landmark
         */
        if (fromLandmark.containsKey(u)) {
            return fromLandmark.get(u).get(t);
        }

        /*
         * Special case, target is landmark
         */
        if (toLandmark.containsKey(t)) {
            return toLandmark.get(t).get(u);
        }

        /*
         * Compute from landmarks
         */
        for (V l : fromLandmark.keySet()) {
            double estimate;
            Map<V, Double> from = fromLandmark.get(l);
            if (directed) {
                Map<V, Double> to = toLandmark.get(l);
                estimate = Math.max(to.get(u) - to.get(t), from.get(t) - from.get(u));
            } else {
                estimate = Math.abs(from.get(u) - from.get(t));
            }

            // max over all landmarks
            if (Double.isFinite(estimate)) {
                maxEstimate = Math.max(maxEstimate, estimate);
            }
        }

        return maxEstimate;
    }

    /**
     * Compute all distances to and from a landmark
     * 
     * @param landmark the landmark
     */
    private void precomputeToFromLandmark(V landmark)
    {
        // compute distances from landmark
        SingleSourcePaths<V, E> fromLandmarkPaths =
            new DijkstraShortestPath<>(graph).getPaths(landmark);
        Map<V, Double> fromLandMarkDistances = new HashMap<>();
        for (V v : graph.vertexSet()) {
            fromLandMarkDistances.put(v, fromLandmarkPaths.getWeight(v));
        }
        fromLandmark.put(landmark, fromLandMarkDistances);

        // compute distances to landmark (using reverse graph)
        if (directed) {
            Graph<V, E> reverseGraph = new EdgeReversedGraph<>(graph);
            SingleSourcePaths<V, E> toLandmarkPaths =
                new DijkstraShortestPath<>(reverseGraph).getPaths(landmark);
            Map<V, Double> toLandMarkDistances = new HashMap<>();
            for (V v : graph.vertexSet()) {
                toLandMarkDistances.put(v, toLandmarkPaths.getWeight(v));
            }
            toLandmark.put(landmark, toLandMarkDistances);
        }
    }

}
