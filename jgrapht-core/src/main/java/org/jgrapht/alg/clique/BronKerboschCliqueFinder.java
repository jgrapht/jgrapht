/*
 * (C) Copyright 2005-2018, by Ewgenij Proschak and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Bron-Kerbosch maximal clique enumeration algorithm.
 * 
 * <p>
 * Implementation of the Bron-Kerbosch clique enumeration algorithm as described in:
 * <ul>
 * <li>R. Samudrala and J. Moult. A graph-theoretic algorithm for comparative modeling of protein
 * structure. Journal of Molecular Biology, 279(1):287--302, 1998.</li>
 * </ul>
 * 
 * <p>
 * The algorithm first computes all maximal cliques and then returns the result to the user. A
 * timeout can be set using the constructor parameters.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see PivotBronKerboschCliqueFinder
 * @see DegeneracyBronKerboschCliqueFinder
 *
 * @author Ewgenij Proschak
 */
public class BronKerboschCliqueFinder<V, E>
    extends
    BaseBronKerboschCliqueFinder<V, E>
{
    /**
     * Constructs a new clique finder.
     *
     * @param graph the input graph; must be simple
     */
    public BronKerboschCliqueFinder(Graph<V, E> graph)
    {
        this(graph, 0L, TimeUnit.SECONDS);
    }

    /**
     * Constructs a new clique finder.
     *
     * @param graph the input graph; must be simple
     * @param timeout the maximum time to wait, if zero no timeout
     * @param unit the time unit of the timeout argument
     */
    public BronKerboschCliqueFinder(Graph<V, E> graph, long timeout, TimeUnit unit)
    {
        super(graph, timeout, unit);
    }

    /**
     * Lazily execute the enumeration algorithm.
     */
    @Override
    protected void lazyRun()
    {
        if (allMaximalCliques == null) {
            if (!GraphTests.isSimple(graph)) {
                throw new IllegalArgumentException("Graph must be simple");
            }
            allMaximalCliques = new ArrayList<>();

            long nanosTimeLimit;
            try {
                nanosTimeLimit = Math.addExact(System.nanoTime(), nanos);
            } catch (ArithmeticException ignore) {
                nanosTimeLimit = Long.MAX_VALUE;
            }

            findCliques(
                new ArrayList<>(), new ArrayList<>(graph.vertexSet()), new ArrayList<>(),
                nanosTimeLimit);
        }
    }

    private void findCliques(
        List<V> potentialClique, List<V> candidates, List<V> alreadyFound,
        final long nanosTimeLimit)
    {
        /*
         * Termination condition: check if any already found node is connected to all candidate
         * nodes.
         */
        for (V v : alreadyFound) {
            if (candidates.stream().allMatch(c -> graph.containsEdge(v, c))) {
                return;
            }
        }

        /*
         * Check each candidate
         */
        for (V candidate : new ArrayList<>(candidates)) {
            /*
             * Check if timeout
             */
            if (nanosTimeLimit - System.nanoTime() < 0) {
                timeLimitReached = true;
                return;
            }

            List<V> newCandidates = new ArrayList<>();
            List<V> newAlreadyFound = new ArrayList<>();

            // move candidate node to potentialClique
            potentialClique.add(candidate);
            candidates.remove(candidate);

            // create newCandidates by removing nodes in candidates not
            // connected to candidate node
            for (V newCandidate : candidates) {
                if (graph.containsEdge(candidate, newCandidate)) {
                    newCandidates.add(newCandidate);
                }
            }

            // create newAlreadyFound by removing nodes in alreadyFound
            // not connected to candidate node
            for (V newFound : alreadyFound) {
                if (graph.containsEdge(candidate, newFound)) {
                    newAlreadyFound.add(newFound);
                }
            }

            // if newCandidates and newAlreadyFound are empty
            if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
                // potential clique is maximal clique
                Set<V> maximalClique = new HashSet<>(potentialClique);
                allMaximalCliques.add(maximalClique);
                maxSize = Math.max(maxSize, maximalClique.size());
            } else {
                // recursive call
                findCliques(potentialClique, newCandidates, newAlreadyFound, nanosTimeLimit);
            }

            // move candidate node from potentialClique to alreadyFound
            alreadyFound.add(candidate);
            potentialClique.remove(candidate);
        }
    }

}
