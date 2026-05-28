/*
 * (C) Copyright 2017-2026, by Dimitrios Michail and Contributors.
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

/**
 * Graph tours and Hamiltonian path related algorithms.
 *
 * <p>
 * This package groups algorithms for traversing every vertex of a graph exactly once. Two
 * related but distinct problems live here:
 *
 * <ul>
 * <li><em>Hamiltonian cycle / tour</em> — closed traversals that return to the start vertex.
 * Implementations include {@link org.jgrapht.alg.tour.PalmerHamiltonianCycle},
 * {@link org.jgrapht.alg.tour.HeldKarpTSP}, and the various TSP heuristics; they share the
 * {@link org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm} interface and the
 * {@link org.jgrapht.alg.tour.HamiltonianCycleAlgorithmBase} helper class.</li>
 * <li><em>Hamiltonian path</em> — open traversals that need not return to the start vertex.
 * Implementations include {@link org.jgrapht.alg.tour.BacktrackingHamiltonianPath},
 * {@link org.jgrapht.alg.tour.HeldKarpHamiltonianPath}, and
 * {@link org.jgrapht.alg.tour.DagHamiltonianPath}; they share the
 * {@link org.jgrapht.alg.interfaces.HamiltonianPathAlgorithm} interface and the
 * {@link org.jgrapht.alg.tour.HamiltonianPathAlgorithmBase} helper class.</li>
 * </ul>
 *
 * <p>
 * Deciding whether a Hamiltonian path or cycle exists in a general graph is NP-complete. The
 * exact algorithms in this package run in exponential time in the worst case; the polynomial
 * special cases (such as {@code DagHamiltonianPath}) and structural prechecks document their
 * scope individually.
 */
package org.jgrapht.alg.tour;
