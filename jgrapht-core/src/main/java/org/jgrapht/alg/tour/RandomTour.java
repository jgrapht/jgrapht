/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

/**
 * Generate a random tour.
 *
 * <p>
 * This class generates a random Hamiltonian Cycle. This is a simple unoptimised
 * solution to the Travelling Salesman Problem, or more usefully is a starting
 * point for optimising a tour using TwoOptHeuristicTSP.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class RandomTour<V, E> extends HamiltonianCycleAlgorithmBase<V, E> {

    private final Random rng;

    /**
     * Construct with default random number generator
     */
    public RandomTour() {
        this(new Random());
    }

    /**
     * Construct with specified random number generator
     * 
     * @param rng The random number generator
     */
    public RandomTour(Random rng) {
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph) {
        List<V> vertices = new ArrayList<>(graph.vertexSet());
        int n = vertices.size();
        if (n == 1) {
            return getSingletonTour(graph);
        }
        int[] tour = new int[n + 1];
        for (int i = 0; i < n; i++) {
            tour[i] = i;
        }
        for (int i = n; i > 1; i--) {
            int j = rng.nextInt(i);
            int tmp = tour[i - 1];
            tour[i - 1] = tour[j];
            tour[j] = tmp;
        }
        tour[n] = tour[0];
        List<V> tourVertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            tourVertices.add(vertices.get(tour[i]));
        }
        return listToTour(tourVertices, graph);
    }
}
