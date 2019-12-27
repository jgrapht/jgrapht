/*
 * (C) Copyright 2019-2019, by Peter Harman and Contributors.
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
package org.jgrapht.alg.tour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;

/**
 * The nearest neighbour heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a
 * list of cities and the distances between each pair of cities, what is the
 * shortest possible route that visits each city exactly once and returns to the
 * origin city?".
 * </p>
 *
 * <p>
 * This is perhaps the simplest and most straightforward TSP heuristic. The key
 * to this algorithm is to always visit the nearest city.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * <a href="http://160592857366.free.fr/joe/ebooks/ShareData/Heuristics%20for%20the%20Traveling%20Salesman%20Problem%20By%20Christian%20Nillson.pdf">Nilsson
 * C., "Heuristics for the Traveling Salesman Problem"</a>
 * </p>
 *
 * <p>
 * The runtime complexity of this class is $O(V^2)$.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class NearestNeighborHeuristicTSP<V, E>
        extends
        HamiltonianCycleAlgorithmBase<V, E> {

    private Random rng;
    private V first;

    /**
     * Constructor. By default a random vertex is chosen to start.
     */
    public NearestNeighborHeuristicTSP() {
        this(null, new Random());
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit, or null to choose at random
     */
    public NearestNeighborHeuristicTSP(V first) {
        this(first, new Random());
    }

    /**
     * Constructor
     *
     * @param seed seed for the random number generator
     */
    public NearestNeighborHeuristicTSP(long seed) {
        this(null, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit, or null to choose at random
     * @param rng Random number generator
     */
    public NearestNeighborHeuristicTSP(V first, Random rng) {
        this.first = first;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Computes a tour using the nearest neighbour heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph) {
        // Handle a graph with single vertex
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }
        Set<V> unvisited = new HashSet<>();
        // Get the initial vertex
        V current = first(unvisited, graph);
        List<V> visited = new ArrayList<>(unvisited.size() + 1);
        visited.add(current);
        // Iterate until tour is complete
        while (!unvisited.isEmpty()) {
            // Find the nearest vertex and add to the tour
            current = nearest(current, unvisited, graph);
            visited.add(current);
        }
        return listToTour(visited, graph);
    }

    /**
     * Get or determine the first vertex
     * 
     * @param unvisited Set to populate with unvisited vertices
     * @param graph The graph
     * @return A suitable vertex to start
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    private V first(Set<V> unvisited, Graph<V, E> graph) {
        // Check that graph is appropriate
        graph = GraphTests.requireUndirected(graph);
        if (!GraphTests.isComplete(graph)) {
            throw new IllegalArgumentException("Graph is not complete");
        }
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }
        unvisited.addAll(graph.vertexSet());
        if (first == null || !graph.vertexSet().contains(first)) {
            first = (V) unvisited.toArray()[rng.nextInt(unvisited.size())];
        }
        // Create Set containing all but first vertex
        unvisited.remove(first);
        return first;
    }

    /**
     * Find the nearest unvisited vertex
     *
     * @param current The last vertex visited
     * @param unvisited Vertices not visited
     * @param graph The graph
     * @return The closest available vertex
     */
    private V nearest(V current, Set<V> unvisited, Graph<V, E> graph) {
        Iterator<V> it = unvisited.iterator();
        V closest = it.next();
        double minDist = getDistance(graph, current, closest);
        while (it.hasNext()) {
            V v = it.next();
            double vDist = getDistance(graph, current, v);
            if (vDist < minDist) {
                closest = v;
                minDist = vDist;
            }
        }
        unvisited.remove(closest);
        return closest;
    }

}
