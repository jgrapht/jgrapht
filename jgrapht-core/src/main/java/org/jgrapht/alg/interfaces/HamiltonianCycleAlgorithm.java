/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
