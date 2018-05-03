/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.interfaces;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

/**
 * An algorithm solving the
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Travelling salesman
 * problem</a> (TSP).
 * 
 * <p>
 * Given a list of cities and the distances between each pair of cities, what is the shortest
 * possible route that visits each city exactly once and returns to the origin city?
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface TSPAlgorithm<V, E> extends HamiltonianCycleAlgorithm<V, E>
{

    /**
     * Computes a tour.
     *
     * @param graph the input graph
     * @return a tour
     */
    GraphPath<V, E> getTour(Graph<V, E> graph);

}
