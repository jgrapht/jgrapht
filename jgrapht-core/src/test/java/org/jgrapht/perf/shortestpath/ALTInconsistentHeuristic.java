/*
 * (C) Copyright 2016-2018, by Dimitrios Michail, Brooks Bockman and Contributors.
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
package org.jgrapht.perf.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * An admissible and probably inconsistent heuristic for the A* algorithm using a set of 
 * landmarks and the triangle inequality. Assumes that the graph contains non-negative edge 
 * weights.
 * 
 * This class is based on {@link ALTAdmissibleHeuristic} but when given multiple landmarks
 * the heuristic value for each node will be based on a random landmark instead of the
 * maximal landmark.  This does not guarantee inconsistency, but with 5 landmarks 
 * and a large graph it is likely.  This is not necessarily a good inconsistent heuristic 
 * and this class has been created primarily for testing.
 *
 * @author Dimitrios Michail
 * @author Brooks Bockman
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class ALTInconsistentHeuristic<V, E>
    implements
    AStarAdmissibleHeuristic<V>
{
    private final Graph<V, E> graph;
    private final Comparator<Double> comparator;
    private final Map<V, Map<V, Double>> fromLandmark;
    private final Map<V, Map<V, Double>> toLandmark;
    private final V[] keySet;
    private final boolean directed;
    private final Random rng;
    
    private Map<V, V> landmarkSelection;

    /**
     * Constructs a new {@link AStarAdmissibleHeuristic} using a set of landmarks.
     *
     * @param graph The graph.
     * @param landmarks A set of vertices of the graph which will be used as landmarks.
     * @param seed Seed for random landmark selection.
     *
     * @throws IllegalArgumentException if no landmarks are provided
     * @throws IllegalArgumentException if the graph contains edges with negative weights
     */
    
    public ALTInconsistentHeuristic(Graph<V, E> graph, Set<V> landmarks, long seed)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.rng = new Random(seed);
        Objects.requireNonNull(landmarks, "Landmarks cannot be null");
        landmarkSelection = new HashMap<>();
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
        @SuppressWarnings("unchecked")
        V[] temp = (V[]) fromLandmark.keySet().toArray();
        keySet = temp;
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
         * Compute from landmarks
         */
        
        if(!landmarkSelection.containsKey(u)) {
           int index = rng.nextInt(keySet.length);
           landmarkSelection.put(u, keySet[index]);
        }
        V l = landmarkSelection.get(u);
        
        double estimate;
        Map<V, Double> from = fromLandmark.get(l);
        if (directed) {
            Map<V, Double> to = toLandmark.get(l);
            estimate = Math.max(to.get(u) - to.get(t), from.get(t) - from.get(u));
        } else {
            estimate = Math.abs(from.get(u) - from.get(t));
        }

        // ensure no overflow 
        if (Double.isFinite(estimate)) {
            maxEstimate = Math.max(maxEstimate, estimate);
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
