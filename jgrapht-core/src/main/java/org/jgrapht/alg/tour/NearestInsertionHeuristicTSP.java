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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
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
 * Insertion heuristics are quite straighforward, and there are many variants to 
 * choose from. The basics of insertion heuristics is to start with a tour of a 
 * subset of all cities, and then inserting the rest by some heuristic. The 
 * initial subtour is often a triangle or the convex hull. One can also start 
 * with a single edge as subtour. This implementation uses the shortest edge by
 * default as the initial sub-tour.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * <a href="http://160592857366.free.fr/joe/ebooks/ShareData/Heuristics%20for%20the%20Traveling%20Salesman%20Problem%20By%20Christian%20Nillson.pdf">Nilsson
 * C., "Heuristics for the Traveling Salesman Problem"</a>
 * </p>
 * 
 * <p>
 * This implementation can also be used in order to try to improve an existing tour. See method
 * {@link #improveTour(GraphPath)}.
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
public class NearestInsertionHeuristicTSP<V, E> extends
        HamiltonianCycleAlgorithmBase<V, E>
        implements HamiltonianCycleImprovementAlgorithm<V, E> {

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
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }
        return listToTour(augment(subtour(graph), graph), graph);
    }

    /**
     * Improves an existing tour. With this algorithm this does not improve the
     * cost, but instead adds any missing vertices to the tour. It also removes
     * vertices from the tour if not in the graph, so this can be used to update
     * a tour following changes to a graph.
     *
     * @param tour The existing tour
     * @return A valid and complete tour for the same graph
     */
    @Override
    public GraphPath<V, E> improveTour(GraphPath<V, E> tour) {
        this.subtour = tour;
        Graph<V, E> graph = this.subtour.getGraph();
        return listToTour(augment(subtour(graph), graph), graph);
    }

    /**
     * Get or create a sub-tour to start augmenting
     *
     * @param graph The graph
     * @return Vertices of an initial sub-tour
     */
    private List<V> subtour(final Graph<V, E> graph) {
        // Check that graph is appropriate
        GraphTests.requireUndirected(graph);
        if (!GraphTests.isComplete(graph)) {
            throw new IllegalArgumentException("Graph is not complete");
        }
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }
        List<V> subtourVertices = new ArrayList<>();
        if (subtour != null) {
            if (subtour.getStartVertex().equals(subtour.getEndVertex())) {
                subtour.getVertexList()
                        .subList(1, subtour.getVertexList().size())
                        .stream()
                        .filter(v -> graph.containsVertex(v))
                        .forEachOrdered(v -> subtourVertices.add(v));
            } else {
                subtour.getVertexList()
                        .stream()
                        .filter(v -> graph.containsVertex(v))
                        .forEachOrdered(v -> subtourVertices.add(v));
            }
        }
        if (subtourVertices.isEmpty()) {
            // If no initial subtour exists, create one based on the shortest edge
            E shortestEdge = graph.edgeSet().stream()
                    .sorted((e1, e2) -> (int) Math.signum(graph.getEdgeWeight(e1) - graph.getEdgeWeight(e2)))
                    .findFirst().get();
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
    private Map<V, Closest<V, E>> getClosest(List<V> tourVertices, Set<V> unvisited, Graph<V, E> graph) {
        return tourVertices.stream().collect(Collectors.toMap((v) -> v, (v) -> getClosest(v, unvisited, graph)));
    }

    /**
     * Determines closest unvisited vertex to a vertex in the current tour
     *
     * @param tourVertex Vertex in the current tour
     * @param unvisited Set of vertices not in the current tour
     * @param graph The graph
     * @return Closest unvisited vertex
     */
    private Closest<V, E> getClosest(V tourVertex, Set<V> unvisited, Graph<V, E> graph) {
        V closest = null;
        E closestEdge = null;
        double minDist = Double.MAX_VALUE;
        for (V unvisitedVertex : unvisited) {
            double vDist = getDistance(graph, tourVertex, unvisitedVertex);
            if (vDist < minDist) {
                closest = unvisitedVertex;
                closestEdge = graph.getEdge(tourVertex, unvisitedVertex);
                minDist = vDist;
            }
        }
        return new Closest<>(tourVertex, closest, closestEdge, minDist);
    }

    /**
     * Update the Map storing closest unvisited vertex for each tour vertex
     *
     * @param currentClosest Map storing closest unvisited vertex for each tour
     * vertex
     * @param chosen Latest vertex added to tour
     * @param unvisited Set of unvisited vertices
     * @param graph The graph
     * @return Updated map
     */
    private Map<V, Closest<V, E>> updateClosest(Map<V, Closest<V, E>> currentClosest, Closest<V, E> chosen, Set<V> unvisited, Graph<V, E> graph) {
        // Update the set of unvisited vertices, and exit if none remain
        unvisited.remove(chosen.getUnvisitedVertex());
        if (unvisited.isEmpty()) {
            return new HashMap<>(0);
        }
        // Update any entries impacted by the choice of new vertex
        currentClosest.replaceAll((v, c) -> {
            if (chosen.getTourVertex().equals(v) || chosen.getUnvisitedVertex().equals(c.getUnvisitedVertex())) {
                return getClosest(v, unvisited, graph);
            }
            return c;
        });
        currentClosest.put(chosen.getUnvisitedVertex(), getClosest(chosen.getUnvisitedVertex(), unvisited, graph));
        return currentClosest;
    }

    /**
     * Chooses the closest unvisited vertex to the sub-tour
     *
     * @param closestVertices Map storing closest unvisited vertex for each tour
     * vertex
     * @return First result of sorting values
     */
    private Closest<V, E> chooseClosest(Map<V, Closest<V, E>> closestVertices) {
        return closestVertices.values().stream().sorted().findFirst().get();
    }

    /**
     * Augment an existing tour to give a complete tour
     *
     * @param subtour The vertices of the existing tour
     * @param graph The graph
     * @return List of vertices representing the complete tour
     */
    private List<V> augment(List<V> subtour, Graph<V, E> graph) {
        Set<V> unvisited = new HashSet<>();
        unvisited.addAll(graph.vertexSet());
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
    private List<V> augment(List<V> subtour, Map<V, Closest<V, E>> closestVertices, Set<V> unvisited, Graph<V, E> graph) {
        while (!unvisited.isEmpty()) {
            // Select a city not in the subtour, having the shortest distance to any one of the cities in the subtoor.
            Closest<V, E> closestVertex = chooseClosest(closestVertices);

            // Determine the vertices either side of the selected tour vertex
            int i = subtour.indexOf(closestVertex.getTourVertex());
            V vertexBefore = subtour.get(i == 0 ? subtour.size() - 1 : i - 1);
            V vertexAfter = subtour.get(i == subtour.size() - 1 ? 0 : i + 1);

            // Find an edge in the subtour such that the cost of inserting the selected city between the edge’s cities will be minimal.
            // Making assumption this is a neighbouring edge, test the edges before and after
            double insertionCostBefore = getDistance(graph, vertexBefore, closestVertex.getUnvisitedVertex())
                    + closestVertex.getDistance()
                    - getDistance(graph, vertexBefore, closestVertex.getTourVertex());
            double insertionCostAfter = getDistance(graph, vertexAfter, closestVertex.getUnvisitedVertex())
                    + closestVertex.getDistance()
                    - getDistance(graph, vertexAfter, closestVertex.getTourVertex());

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
     * @param <E> edge type
     */
    private static class Closest<V, E> implements Comparable<Closest<V, E>> {

        private final V tourVertex;
        private final V unvisitedVertex;
        private final E edge;
        private final double distance;

        Closest(V tourVertex, V unvisitedVertex, E edge, double distance) {
            this.tourVertex = tourVertex;
            this.unvisitedVertex = unvisitedVertex;
            this.edge = edge;
            this.distance = distance;
        }

        public V getTourVertex() {
            return tourVertex;
        }

        public V getUnvisitedVertex() {
            return unvisitedVertex;
        }

        public E getEdge() {
            return edge;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(Closest<V, E> o) {
            return (int) Math.signum(distance - o.distance);
        }

    }
}
