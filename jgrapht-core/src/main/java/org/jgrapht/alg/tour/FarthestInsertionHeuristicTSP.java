/*
 * (C) Copyright 2021-2024, by J. Alejandro Cornejo-Acosta and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.ArrayUtil;
import org.jgrapht.util.VertexToIntegerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The farthest insertion heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 * </p>
 *
 * <p>
 * Insertion heuristics are quite straightforward, and there are many variants to choose from. The
 * basics of insertion heuristics is to start with a partial tour of a subset of all cities,
 * and then iteratively an unvisited vertex (a vertex whichis not in the tour) is chosen given a criterion
 * and inserted in the best position of the partial tour. Per each iteration, the farthest insertion
 * heuristic selects the farthest unvisited vertex from the partial tour.
 * This algorithm provides a guarantee to compute tours no more than
 * 0(log N) times optimum (assuming the triangle inequality). However, regarding practical results,
 * some references refer to this heuristic as one of the best among the category of insertion heuristics.
 * This implementation uses the longest edge by default as the initial sub-tour if one is not provided.
 * </p>
 *
 * <p>
 * The description of this algorithm can be consulted on: <br>
 * Johnson, D. S., & McGeoch, L. A. (2007). Experimental Analysis of Heuristics for the STSP.
 * In G. Gutin & A. P. Punnen (Eds.), The Traveling Salesman Problem and Its Variations (pp. 369–443).
 * Springer US. https://doi.org/10.1007/0-306-48213-4_9
 * </p>
 *
 * <p>
 * This implementation can also be used in order to augment an existing partial tour. See
 * constructor {@link #FarthestInsertionHeuristicTSP(GraphPath)}.
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
 * @author J. Alejandro Cornejo-Acosta
 */
public class FarthestInsertionHeuristicTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
{

    /**
     * Initial vertices in the tour
     */
    private GraphPath<V, E> initialSubtour;

    /**
     * Distances from unvisited vertices to the partially constructed tour
     */
    private double[] distances = null;

    /**
     * Matrix of distances between all vertices
     */
    private double[][] allDist;

    /**
     * Mapping of vertices to integers to work on.
     */
    private VertexToIntegerMapping<V> mapping;


    /**
     * Constructor. By default a sub-tour is chosen based on the longest edge
     */
    public FarthestInsertionHeuristicTSP()
    {
        this(null);
    }

    /**
     * Constructor
     *
     * Specifies an existing sub-tour that will be augmented to form a complete tour when
     * {@link #getTour(org.jgrapht.Graph) } is called
     *
     * @param subtour Initial sub-tour, or null to start with longest edge
     */
    public FarthestInsertionHeuristicTSP(GraphPath<V, E> subtour)
    {
        this.initialSubtour = subtour;
    }

    // algorithm

    /**
     * Computes a tour using the farthest insertion heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        checkGraph(graph);
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }

        mapping = Graphs.getVertexToIntegerMapping(graph);


        // Computes matrix of distances
        E longesEdge = computeDistanceMatrix(graph);
        if (initialSubtour == null || initialSubtour.getVertexList().isEmpty()) {
            // If no initial subtour was provided, create one based on the longest edge
            V v = graph.getEdgeSource(longesEdge);
            V u = graph.getEdgeTarget(longesEdge);

            // at this point weight does not matter
            initialSubtour = new GraphWalk<>(graph, List.of(v, u), -1);
        }

        int n = mapping.getIndexList().size();

        // initialize tour
        int[] tour = initPartialTour();

        // init distances from unvisited vertices to the partially constructed tour
        initDistances(tour);

        // construct tour
        for (int i = initialSubtour.getVertexList().size(); i < n; i++) {

            // Find the index of the farthest unvisited vertex.
            int idxFarthest = getFarthest(i);
            int k = tour[idxFarthest];

            // Search for the best position of vertex k in the tour
            double saving = Double.POSITIVE_INFINITY;
            int bestIndex = -1;
            for (int j = 0; j <= i; j++) {

                int x = (j == 0 ? tour[i - 1] : tour[j - 1]);
                int y = (j == i ? tour[0] : tour[j]);

                double dxk = allDist[x][k];
                double dky = allDist[k][y];
                double dxy = (x == y ? 0 : allDist[x][y]);

                double savingTmp = dxk + dky - dxy;
                if (savingTmp < saving) {
                    saving = savingTmp;
                    bestIndex = j;
                }
            }
            ArrayUtil.swap(tour, i, idxFarthest);
            ArrayUtil.swap(distances, i, idxFarthest);

            // perform insertion of vertex k
            for (int j = i; j > bestIndex; j--) {
                tour[j] = tour[j - 1];
            }
            tour[bestIndex] = k;

            // Update distances from vertices to the partial tour
            updateDistances(k, i + 1);
        }

        tour[n] = tour[0]; // close tour manually. Arrays.asList does not support add

        // Map the tour from integer values to V values
        List<V> tourList = Arrays.stream(tour).mapToObj(i -> mapping.getIndexList().get(i))
            .collect(Collectors.toList());
        return closedVertexListToTour(tourList, graph);
    }

    /**
     * Initialize the partial tour with the vertices of {@code initialSubtour} at the beginning of
     * the tour.
     *
     * @return a dummy tour with the vertices of {@code initialSubtour} at the beginning.
     */
    private int[] initPartialTour()
    {
        int n = mapping.getVertexMap().size();
        int[] tour = new int[n + 1];
        Set<Integer> visited = new HashSet<>();
        int i = 0;
        for (var v : initialSubtour.getVertexList()) {
            int iv = mapping.getVertexMap().get(v);
            visited.add(iv);
            tour[i++] = iv;
        }
        for (int v = 0; v < n; v++) {
            if (!visited.contains(v)) {
                tour[i++] = v;
            }
        }

        return tour;
    }


    /**
     * Computes the matrix of distances by using the already computed {@code mapping}
     * of vertices to integers
     *
     * @param graph the input graph
     * @return the longest edge to initialize the partial tour if necessary
     */
    private E computeDistanceMatrix(Graph<V, E> graph)
    {
        E longestEdge = null;
        double longestEdgeWeight = -1;
        int n = graph.vertexSet().size();
        allDist = new double[n][n];
        for (var edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (!source.equals(target)) {
                int i = mapping.getVertexMap().get(source);
                int j = mapping.getVertexMap().get(target);
                if (allDist[i][j] == 0) {
                    allDist[i][j] = allDist[j][i] = graph.getEdgeWeight(edge);
                    if (longestEdgeWeight < allDist[i][j]) {
                        longestEdgeWeight = allDist[i][j];
                        longestEdge = edge;
                    }
                }
            }
        }
        return longestEdge;
    }


    /**
     * Find the index of the unvisited vertex which is farthest from the partially constructed tour.
     *
     * @param start The unvisited vertices start at index {@code start}
     * @return the index of the unvisited vertex which is farthest from the partially constructed tour.
     */
    private int getFarthest(int start)
    {
        int n = distances.length;
        int farthest = -1;
        double maxDist = -1;
        for (int i = start; i < n; i++) {
            double dist = distances[i];
            if (dist > maxDist) {
                farthest = i;
                maxDist = dist;
            }
        }
        return farthest;
    }

    /**
     * Initialize distances from the unvisited vertices to the initial subtour
     *
     * @param tour a partial tour with {@code initialSubtour} at the beginning
     */
    private void initDistances(int[] tour)
    {
        int n = mapping.getVertexMap().size();
        int start = initialSubtour.getVertexList().size();
        distances = new double[n];
        Arrays.fill(distances, start, n, Double.POSITIVE_INFINITY);
        for (int i = start; i < n; i++) {
            for (int j = 0; j < start; j++) {
                distances[i] = Math.min(distances[i], allDist[tour[i]][tour[j]]);
            }
        }
    }

    /**
     * Update the distances from the unvisited vertices to the partially constructed tour
     *
     * @param v the last vertex added to the tour
     * @param start the unvisited vertices start at index {@code start}
     */
    private void updateDistances(int v, int start)
    {
        for (int i = start; i < distances.length; i++) {
            distances[i] = Math.min(allDist[v][i], distances[i]);
        }
    }
}
