/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;

import java.util.*;

/**
 * Generate a random tour.
 *
 * <p>
 * This class generates a random Hamiltonian Cycle. This is a simple unoptimised solution to the
 * Travelling Salesman Problem, or more usefully is a starting point for optimising a tour using
 * TwoOptHeuristicTSP.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 * @author Dimitrios Michail
 */
public class RandomTourTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
{

    private final Random rng;

    /**
     * Construct with default random number generator
     */
    public RandomTourTSP()
    {
        this(new Random());
    }

    /**
     * Construct with specified random number generator
     *
     * @param rng The random number generator
     */
    public RandomTourTSP(Random rng)
    {
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Computes a tour using the greedy heuristic.
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
        // Check that graph is appropriate
        checkGraph(graph);
        List<V> vertices = new ArrayList<>(graph.vertexSet());
        if (vertices.size() == 1) {
            return getSingletonTour(graph);
        }
        // Randomly permute the vertex list
        Collections.shuffle(vertices, rng);
        return vertexListToTour(vertices, graph);
    }
}
