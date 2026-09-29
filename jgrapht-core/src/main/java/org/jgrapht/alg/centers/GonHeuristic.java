/*
 * (C) Copyright 2026, by Jose Alejandro Cornejo-Acosta and Contributors.
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
package org.jgrapht.alg.centers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;

/**
 * The Gon heuristic algorithm for the vertex $k$-center problem.
 *
 * <p>
 * The vertex $k$-center problem is an NP-hard combinatorial optimization
 * problem that receives a complete edge-weighted undirected graph $G = (V, E,
 * w)$, and a positive integer $k$. The goal is to find a subset $C$ of $V$ such
 * that $|C| = k$ and the maximum distance from any vertex in $V$ to the nearest
 * vertex in $C$ is minimized. $C$ is called the set of centers. The vertex
 * $k$-center problem has applications in clustering and facility location.
 * </p>
 *
 * <p>
 * The Gon heuristic is a classic heuristic approximation algorithm for the
 * vertex $k$-center problem. It works in a straightforward way. First, a vertex
 * from the input graph is chosen randomly and added to the set of centers $C$.
 * Then, iteratively, the farthest vertex from $V$ to $C$ is chosen and added to
 * $C$. This process is repeated until $|C| = k$.
 *
 * This algorithm provides a guarantee to compute solutions for the vertex
 * $k$-center problem no more than 2-times optimum. According to the literature,
 * this is the best approximation factor (under $P \neq NP$). The implementation
 * chooses the first vertex randomly. Alternatively, an existing set of centers
 * $C$ with fewer than $k$ centers can be provided to be augmented. In this
 * implementation, ties are broken by choosing the vertex with the lowest index.
 * </p>
 *
 * <p>
 * The description of this algorithm can be consulted on: <br>
 * </p>
 * 
 * <p>
 * T. F. Gonzalez Clustering to minimize the maximum intercluster distance.
 * Theor. Comput. Sci. 1985, 38, 293-306.
 * </p>
 *
 * <p>
 * J. Garcia-Diaz, R. Menchaca-Mendez, R. Menchaca-Mendez, S. Pomares Hernández,
 * J. C. Pérez-Sansalvador and N. Lakouari, "Approximation Algorithms for the
 * Vertex K-Center Problem: Survey and Experimental Evaluation," in IEEE Access,
 * vol. 7, pp. 109228-109245, 2019, doi: 10.1109/ACCESS.2019.2933875.
 * </p>
 *
 * <p>
 * This implementation can also be used to augment an existing partial set of
 * centers. See constructor {@link #GonHeuristic(Set)}.
 * </p>
 *
 * <p>
 * The runtime complexity of the Gon algorithm is $O(k\cdot |V|)$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete, undirected, and
 * edge-weighted.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Jose Alejandro Cornejo-Acosta
 */
public class GonHeuristic<V, E> extends CentersLocationAlgorithmBase<V, E> {

    /**
     * Initial vertices in the set of centers
     */
    private Set<V> initialCenters;

    /**
     * Distances from the vertices to the set of centers
     */
    private Map<V, Double> distances = null;

    /**
     * Assignment of vertices to centers
     */
    private Map<V, V> assignment = null;

    /**
     * Covering radius of the solution
     */
    private double coveringRadius;

    /**
     * The input graph
     */
    private Graph<V, E> graph;

    /**
     * Random number generator to randomly select first center
     */
    private Random rng;

    /**
     * By default the first center will be chosen randomly.
     *
     * @param rng random number generator.
     */
    public GonHeuristic(Random rng) {
        this.rng = rng;
        this.initialCenters = null;
    }

    /**
     * Specifies a partial set of initial centers that will be augmented to form
     * a set of $k$ centers when {@link #getCenters } is called.
     *
     * @param initialCenters Initial set of centers.
     */
    public GonHeuristic(Set<V> initialCenters) {
        if (initialCenters == null) {
            throw new IllegalArgumentException("The set of initial centers cannot be null.");
        }
        this.initialCenters = initialCenters;
    }

    // algorithm
    /**
     * Computes the set of $k$ centers by using the Gon heuristic.
     *
     * @param graph the input graph.
     * @return a set of centers.
     * @throws IllegalArgumentException if the graph is not undirected.
     * @throws IllegalArgumentException if the graph is not complete.
     * @throws IllegalArgumentException if the graph contains no vertices.
     */
    @Override
    public Set<V> getCenters(Graph<V, E> graph, int k) {
        checkGraph(graph);
        this.graph = graph;

        // size of graph
        int n = graph.vertexSet().size();
        if (n == k) {
            return graph.vertexSet();
        }
        if (n < k) {
            throw new IllegalArgumentException(
                    "The number of vertices in the graph must be at least k");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be at least 1");
        }

        if (initialCenters == null || initialCenters.isEmpty()) {

            // If no initial set of centers was provided, choose the first center randomly
            V v = new ArrayList<>(graph.vertexSet()).get(rng.nextInt(n));
            initialCenters = new HashSet<>();
            initialCenters.add(v);
        }

        // initialize set of centers C
        Set<V> centers = initPartialC();
        // complement of C (i.e. C')
        Set<V> clients = new HashSet<>(graph.vertexSet());
        clients.removeAll(centers);

        // init distances from clients to the set of centers
        initDistances(centers, clients);

        // compute centers
        while (centers.size() < k) {

            // Find the farthest vertex.
            V v = getFarthest(clients);

            // remove from C'
            clients.remove(v);

            // insert to centers
            centers.add(v);

            // Update distances from vertices to the centers
            updateDistances(v, clients);
        }

        // Assign clients to centers
        setAssigment(centers, clients);

        // Return the set of centers
        return centers;
    }

    /**
     * Assigns each vertex to its closest center.
     * 
     * @param centers the set of centers.
     * @param clients the set of clients (vertices that are not centers).
     */
    private void setAssigment(Set<V> centers, Set<V> clients) {
        this.assignment = new HashMap<>();
        this.coveringRadius = 0.0;
        for (V client : clients) {
            V closestCenter = null;
            double minDist = Double.MAX_VALUE;
            for (V center : centers) {
                double edgeWeight = graph.getEdgeWeight(graph.getEdge(client, center));
                if (edgeWeight < minDist) {
                    minDist = edgeWeight;
                    closestCenter = center;
                }
            }
            this.assignment.put(client, closestCenter);
            this.coveringRadius = Math.max(this.coveringRadius, minDist);
        }
    }

    /**
     * Initialize the partial set of centers C with the vertices of
     * {@code initialCenters}.
     *
     * @return a partial set of centers with the vertices of
     * {@code initialCenters}.
     */
    private Set<V> initPartialC() {
        return new HashSet<>(initialCenters);
    }

    /**
     * Finds the vertex in C' which is farthest from C.
     *
     * @param clients the set of vertices that are not centers (i.e. C').
     * @return the index of the vertex which is farthest from the set of
     * centers.
     */
    private V getFarthest(Set<V> clients) {
        return Collections.max(clients, Comparator.comparingDouble(distances::get));
    }

    /**
     * Initialize distances from the vertices to the initial set of centers
     *
     * @param centers a partial set of centers. {@code initialCenters}.
     * @param clients the vertices that are not centers (i.e. the complement of
     * C).
     */
    private void initDistances(Set<V> centers, Set<V> clients) {
        distances = new HashMap<>(clients.size());
        for (V client : clients) {
            double minDist = Double.MAX_VALUE;
            for (V center : centers) {
                minDist = Math.min(minDist, graph.getEdgeWeight(graph.getEdge(client, center)));
            }
            distances.put(client, minDist);
        }
    }

    /**
     * Update the distances from the vertices to the partial set of centers.
     *
     * @param center the last vertex added to the set of centers.
     * @param clients the vertices that are not centers.
     */
    private void updateDistances(V center, Set<V> clients) {
        for (V client : clients) {
            double edgeWeight = graph.getEdgeWeight(graph.getEdge(client, center));
            double currentDist = distances.get(client);
            if(edgeWeight < currentDist){
                distances.put(client, edgeWeight);
            }
        }
    }

    @Override
    public double getCoveringRadius() {
        if (this.assignment == null) {
            throw new IllegalStateException("You must call getCenters() before calling getCoveringRadius()");
        }
        return this.coveringRadius;
    }

    @Override
    public Map<V, V> getAssignment() {
        if (this.assignment == null) {
            throw new IllegalStateException("You must call getCenters() before calling getAssignment()");
        }
        return this.assignment;
    }
}
