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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.util.VertexToIntegerMapping;

/**
 * The Gon heuristic algorithm for the vertex $k$-center problem.
 *
 * <p>
 * The vertex $k$-center problem is an NP-hard combinatorial optimization problem that receives a complete
 * edge-weighted undirected graph $G = (V, E, w)$, and a positive integer $k$. The goal is to find a subset $C$ of $V$ such
 * that $|C| = k$ and the maximum distance from any vertex in $V$ to the nearest vertex in $C$ is minimized.
 * $C$ is called the set of centers. The vertex $k$-center problem has applications in clustering and facility location.
 * </p>
 *
 * <p>
 * The Gon heuristic is a classic heuristic approximation algorithm for the vertex $k$-center problem. 
 * It works in a straightforward way. First, a vertex from the input graph is chosen randomly and added to the set of centers $C$. 
 * Then, iteratively, the farthest vertex from $V$ to $C$ is chosen and added to $C$. This process is repeated until $|C| = k$.
 * 
 * This algorithm provides a guarantee to compute solutions for the vertex $k$-center problem no more than
 * 2-times optimum. According to the literature, this is the best approximation factor (under $P \neq NP$).
 * The implementation chooses the first vertex randomly. Alternatively, an existing set of centers $C$ with fewer than $k$ centers
 * can be provided to be augmented. In this implementation, ties are broken by choosing the vertex with the lowest index.
 * </p>
 *
 * <p>
 * The description of this algorithm can be consulted on: <br>
 * 
 * T. F. Gonzalez Clustering to minimize the maximum intercluster distance. Theor. Comput. Sci. 1985, 38, 293-306.
 * 
 * J. Garcia-Diaz, R. Menchaca-Mendez, R. Menchaca-Mendez, S. Pomares Hernández, J. C. Pérez-Sansalvador and N. Lakouari, 
 * "Approximation Algorithms for the Vertex K-Center Problem: Survey and Experimental Evaluation," in IEEE Access, vol. 7, 
 * pp. 109228-109245, 2019, doi: 10.1109/ACCESS.2019.2933875.
 * </p>
 *
 * <p>
 * This implementation can also be used in order to augment an existing partial set of centers. See
 * constructor {@link #GonHeuristic(Set)}.
 * </p>
 *
 * <p>
 * The runtime complexity is $O(k*|V|)$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete, undirected, and edge-weighted.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Jose Alejandro Cornejo-Acosta
 */
public class GonHeuristic<V, E> extends CentersLocationAlgorithmBase<V, E>
{

    /**
     * Initial vertices in the set of centers
     */
    private Set<V> initialCenters;

    /**
     * Distances from the vertices to the set of centers
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


    private Random rng;


    /**
     * Constructor. 
     * 
     * By default the first vertex is chosen randomly.
     * 
     * @param rng random number generator.
     */
    public GonHeuristic(Random rng)
    {
        this.rng = rng;
        this.initialCenters = null;
    }

    /**
     * Constructor
     *
     * Specifies a partial set of initial centers that will be augmented to form a set of k centers when
     * {@link #getCenters(java.util.Set) } is called.
     *
     * @param initialCenters Initial set of centers.
     */
    public GonHeuristic(Set<V> initialCenters)
    {
        if (initialCenters == null) {
            throw new IllegalArgumentException("The set of initial centers cannot be null.");
        }
        this.initialCenters = initialCenters;
    }

    // algorithm

    /**
     * Computes the set of k centers by using the Gon heuristic.
     *
     * @param graph the input graph.
     * @return a set of centers.
     * @throws IllegalArgumentException if the graph is not undirected.
     * @throws IllegalArgumentException if the graph is not complete.
     * @throws IllegalArgumentException if the graph contains no vertices.
     */
    @Override
    public Set<V> getCenters(Graph<V, E> graph, int k)
    {
        checkGraph(graph);
        if (graph.vertexSet().size() == k) {
            return graph.vertexSet();
        }

        if (graph.vertexSet().size() < k) {
            throw new IllegalArgumentException(
                "The number of vertices in the graph must be at least k");
        }

        if (k<=0) {
            throw new IllegalArgumentException("k must be at least 1");
        }

        mapping = Graphs.getVertexToIntegerMapping(graph);
        int n = mapping.getIndexList().size();

        // Computes matrix of distances
        computeDistanceMatrix(graph);
        if (initialCenters == null || initialCenters.isEmpty()) {

            // If no initial set of centers was provided, choose the first center randomly
            V v = mapping.getIndexList().get(rng.nextInt(n));
            initialCenters = new HashSet<>(k);
            initialCenters.add(v);
        }

        // initialize set of centers C
        Set<Integer> centers = initPartialC();
        // complement of C (i.e. C')
        Set<Integer> comp = graph.vertexSet().stream().map(v->mapping.getVertexMap().get(v)).collect(Collectors.toSet());
        comp.removeAll(centers);

        // init distances from vertices to the set of centers
        initDistances(centers, comp);

        // compute centers
        while (centers.size()<k) {

            // Find the index of the farthest vertex.
            int v = getFarthest(comp);

            // remove from C'
            comp.remove(v);

            // insert to centers
            centers.add(v);
            
            // Update distances from vertices to the centers
            updateDistances(v, comp);
        }

        // Map the set of centers from integer values to V values
        return centers.stream().map(i->mapping.getIndexList().get(i)).collect(Collectors.toSet());
    }

    /**
     * Initialize the partial set of centers C with the vertices of {@code initialCenters}.
     *
     * @return a partial set of centers with the vertices of {@code initialCenters}.
     */
    private Set<Integer> initPartialC()
    {
        Set<Integer> centers = initialCenters.stream().map(
            v->mapping.getVertexMap().get(v)).collect(Collectors.toSet());
        return centers;
    }


    /**
     * Computes the matrix of distances by using the already computed {@code mapping}
     * of vertices to integers.
     *
     * @param graph the input graph.
     */
    private void computeDistanceMatrix(Graph<V, E> graph)
    {
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
                }
            }
        }
    }


    /**
     * Find the index of the vertex in C' which is farthest from C.
     *
     * @param comp the set of vertices that are not centers (i.e. C').
     * @return the index of the vertex which is farthest from the set of centers.
     */
    private int getFarthest(Set<Integer> comp)
    {
        int farthest = -1;
        double maxDist = -1;
        for (int v : comp) {
            double dist = distances[v];
            if (dist > maxDist) {
                farthest = v;
                maxDist = dist;
            }
        }
        return farthest;
    }

    /**
     * Initialize distances from the vertices to the initial set of centers
     *
     * @param centers a partial set of centers. {@code initialCenters}.
     * @param comp the vertices that are not centers (i.e. the complement of C).
     */
    private void initDistances(Set<Integer> centers, Set<Integer> comp)
    {
        distances = new double[mapping.getVertexMap().size()];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        for (int v : comp) {
            for (int c : centers) {
                distances[v] = Math.min(distances[v], allDist[v][c]);
            }
        }
    }

    /**
     * Update the distances from the vertices to the partial set of centers.
     *
     * @param v the last vertex added to the set of centers.
     * @param comp the vertices that are not centers.
     */
    private void updateDistances(int v, Set<Integer> comp)
    {
        for (int i : comp) {
            distances[i] = Math.min(allDist[v][i], distances[i]);
        }
    }
}
