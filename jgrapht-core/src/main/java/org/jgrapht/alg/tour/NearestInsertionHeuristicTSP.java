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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.HamiltonianCycleImprovementAlgorithm;

/**
 * The nearest insertion heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a
 * list of cities and the distances between each pair of cities, what is the
 * shortest possible route that visits each city exactly once and returns to the
 * origin city?".
 * </p>
 *
 * <p>
 * Insertion heuristics are quite straightforward, and there are many variants
 * to choose from. The basics of insertion heuristics is to start with a tour of
 * a subset of all cities, and then inserting the rest by some heuristic. The
 * initial sub-tour is often a triangle or the convex hull. One can also start
 * with a single edge as sub-tour. This implementation uses the shortest edge by
 * default as the initial sub-tour.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * Nilsson, Christian. "Heuristics for the traveling salesman problem."
 * Linkoping University 38 (2003)
 * </p>
 *
 * <p>
 * This implementation can also be used in order to augment an existing partial
 * tour. See method {@link #augmentTour(GraphPath)}.
 * </p>
 *
 * <p>
 * The runtime complexity of this class is $O(V^2)$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class NearestInsertionHeuristicTSP<V, E> extends
        HamiltonianCycleAlgorithmBase<V, E> {

    private GraphPath<V, E> subtour;

    /**
     * Constructor. By default a sub-tour is chosen based on the shortest edge
     */
    public NearestInsertionHeuristicTSP() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param subtour Initial sub-tour, or null to start with shortest edge
     */
    public NearestInsertionHeuristicTSP(GraphPath<V, E> subtour) {
        this.subtour = subtour;
    }

    /**
     * Computes a tour using the nearest insertion heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph) {
        // Check that graph is appropriate
        checkGraph(graph);

        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }
        return vertexListToTour(augment(subtour(graph), graph), graph);
    }

    /**
     * Get or create a sub-tour to start augmenting
     *
     * @param graph The graph
     * @return Vertices of an initial sub-tour
     */
    private List<V> subtour(Graph<V, E> graph) {
        List<V> subtourVertices = new ArrayList<>();
        if (subtour != null) {
            if (subtour.getStartVertex().equals(subtour.getEndVertex())) {
                subtourVertices.addAll(subtour.getVertexList()
                        .subList(1, subtour.getVertexList().size()));
            } else {
                subtourVertices.addAll(subtour.getVertexList());
            }
        }
        if (subtourVertices.isEmpty()) {
            // If no initial subtour exists, create one based on the shortest edge
            E shortestEdge = Collections.min(graph.edgeSet(),
                    (e1, e2) -> Double.compare(graph.getEdgeWeight(e1), graph.getEdgeWeight(e2)));
            subtourVertices.add(graph.getEdgeSource(shortestEdge));
            subtourVertices.add(graph.getEdgeTarget(shortestEdge));
        }
        return subtourVertices;
    }

    /**
     * Initialise the Map storing closest unvisited vertex for each tour vertex
     *
     * @param tourVertices Current tour vertices
     * @param unvisited Set of unvisited vertices
     * @param graph The graph
     * @return Map storing closest unvisited vertex for each tour vertex
     */
    private Map<V, Closest<V>> getClosest(List<V> tourVertices, Set<V> unvisited, Graph<V, E> graph) {
        return tourVertices.stream().collect(Collectors.toMap(v -> v, v -> getClosest(v, unvisited, graph)));
    }

    /**
     * Determines closest unvisited vertex to a vertex in the current tour
     *
     * @param tourVertex Vertex in the current tour
     * @param unvisited Set of vertices not in the current tour
     * @param graph The graph
     * @return Closest unvisited vertex
     */
    private Closest<V> getClosest(V tourVertex, Set<V> unvisited, Graph<V, E> graph) {
        V closest = null;
        double minDist = Double.MAX_VALUE;
        for (V unvisitedVertex : unvisited) {
            double vDist = graph.getEdgeWeight(graph.getEdge(tourVertex, unvisitedVertex));
            if (vDist < minDist) {
                closest = unvisitedVertex;
                minDist = vDist;
            }
        }
        return new Closest<>(tourVertex, closest, minDist);
    }

    /**
     * Update the Map storing closest unvisited vertex for each tour vertex
     *
     * @param currentClosest Map storing closest unvisited vertex for each tour
     * vertex
     * @param chosen Latest vertex added to tour
     * @param unvisited Set of unvisited vertices
     * @param graph The graph
     */
    private void updateClosest(Map<V, Closest<V>> currentClosest, Closest<V> chosen, Set<V> unvisited, Graph<V, E> graph) {
        // Update the set of unvisited vertices, and exit if none remain
        unvisited.remove(chosen.getUnvisitedVertex());
        if (unvisited.isEmpty()) {
            currentClosest.clear();
            return;
        }
        // Update any entries impacted by the choice of new vertex
        currentClosest.replaceAll((v, c) -> {
            if (chosen.getTourVertex().equals(v) || chosen.getUnvisitedVertex().equals(c.getUnvisitedVertex())) {
                return getClosest(v, unvisited, graph);
            }
            return c;
        });
        currentClosest.put(chosen.getUnvisitedVertex(), getClosest(chosen.getUnvisitedVertex(), unvisited, graph));
    }

    /**
     * Chooses the closest unvisited vertex to the sub-tour
     *
     * @param closestVertices Map storing closest unvisited vertex for each tour
     * vertex
     * @return First result of sorting values
     */
    private Closest<V> chooseClosest(Map<V, Closest<V>> closestVertices) {
        return Collections.min(closestVertices.values());
    }

    /**
     * Augment an existing tour to give a complete tour
     *
     * @param subtour The vertices of the existing tour
     * @param graph The graph
     * @return List of vertices representing the complete tour
     */
    private List<V> augment(List<V> subtour, Graph<V, E> graph) {
        Set<V> unvisited = new HashSet<>(graph.vertexSet());
        unvisited.removeAll(subtour);
        return augment(subtour, getClosest(subtour, unvisited, graph), unvisited, graph);
    }

    /**
     * Augment an existing tour to give a complete tour
     *
     * @param subtour The vertices of the existing tour
     * @param closestVertices Map of data for closest unvisited vertices
     * @param unvisited Set of unvisited vertices
     * @param graph The graph
     * @return List of vertices representing the complete tour
     */
    private List<V> augment(List<V> subtour, Map<V, Closest<V>> closestVertices, Set<V> unvisited, Graph<V, E> graph) {
        while (!unvisited.isEmpty()) {
            // Select a city not in the subtour, having the shortest distance to any one of the cities in the subtoor.
            Closest<V> closestVertex = chooseClosest(closestVertices);

            // Determine the vertices either side of the selected tour vertex
            int i = subtour.indexOf(closestVertex.getTourVertex());
            V vertexBefore = subtour.get(i == 0 ? subtour.size() - 1 : i - 1);
            V vertexAfter = subtour.get(i == subtour.size() - 1 ? 0 : i + 1);

            // Find an edge in the subtour such that the cost of inserting the selected city between the edge’s cities will be minimal.
            // Making assumption this is a neighbouring edge, test the edges before and after
            double insertionCostBefore = graph.getEdgeWeight(graph.getEdge(vertexBefore, closestVertex.getUnvisitedVertex()))
                    + closestVertex.getDistance()
                    - graph.getEdgeWeight(graph.getEdge(vertexBefore, closestVertex.getTourVertex()));
            double insertionCostAfter = graph.getEdgeWeight(graph.getEdge(vertexAfter, closestVertex.getUnvisitedVertex()))
                    + closestVertex.getDistance()
                    - graph.getEdgeWeight(graph.getEdge(vertexAfter, closestVertex.getTourVertex()));

            // Add the selected vertex to the tour
            if (insertionCostBefore < insertionCostAfter) {
                subtour.add(i, closestVertex.getUnvisitedVertex());
            } else {
                subtour.add(i + 1, closestVertex.getUnvisitedVertex());
            }

            // Repeat until no more cities remain
            updateClosest(closestVertices, closestVertex, unvisited, graph);
        }
        return subtour;
    }

    /**
     * Class holding data for the closest unvisited vertex to a particular
     * vertex in the tour.
     *
     * @param <V> vertex type
     */
    private static class Closest<V> implements Comparable<Closest<V>> {

        private final V tourVertex;
        private final V unvisitedVertex;
        private final double distance;

        Closest(V tourVertex, V unvisitedVertex, double distance) {
            this.tourVertex = tourVertex;
            this.unvisitedVertex = unvisitedVertex;
            this.distance = distance;
        }

        public V getTourVertex() {
            return tourVertex;
        }

        public V getUnvisitedVertex() {
            return unvisitedVertex;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(Closest<V> o) {
            return Double.compare(distance, o.distance);
        }

    }
}
