/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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

import org.jgrapht.*;

/**
 * An algorithm solving the <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Hamiltonian
 * cycle problem</a>.
 * 
 * <p>
 * A Hamiltonian cycle, also called a Hamiltonian circuit, Hamilton cycle, or Hamilton circuit, is a
 * graph cycle (i.e., closed loop) through a graph that visits each node exactly once (Skiena 1990,
 * p. 196).
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public interface HamiltonianCycleAlgorithm<V, E>
{

    /**
     * Computes a tour.
     *
     * @param graph the input graph
     * @return a tour
     */
    GraphPath<V, E> getTour(Graph<V, E> graph);

}
