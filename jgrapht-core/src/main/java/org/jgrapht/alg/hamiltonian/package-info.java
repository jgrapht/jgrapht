/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
 * Algorithms for the Hamiltonian path problem.
 *
 * <p>
 * This package contains exact algorithms that solve the
 * <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Hamiltonian path</a> existence and
 * construction problem on directed and undirected graphs. A Hamiltonian path visits every vertex
 * of the graph exactly once; unlike a Hamiltonian cycle, it does not need to return to its start
 * vertex.
 *
 * <p>
 * The general Hamiltonian path problem is NP-complete. Implementations therefore expose either
 * exponential-time exact solvers (with pruning) or polynomial-time solvers for restricted graph
 * classes. Algorithms in this package implement
 * {@link org.jgrapht.alg.interfaces.HamiltonianPathAlgorithm}.
 *
 * <p>
 * Hamiltonian <em>cycle</em> / Hamiltonian tour algorithms live in
 * {@link org.jgrapht.alg.tour}. They are kept separate from the path algorithms because tour
 * APIs commonly assume cycle or complete-graph semantics that are inappropriate for plain
 * Hamiltonian paths.
 */
package org.jgrapht.alg.hamiltonian;
