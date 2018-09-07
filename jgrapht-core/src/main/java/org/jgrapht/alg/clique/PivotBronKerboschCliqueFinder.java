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
package org.jgrapht.alg.clique;

import org.jgrapht.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Bron-Kerbosch maximal clique enumeration algorithm with pivot.
 * 
 * <p>
 * The pivoting follows the rule from the paper
 * <ul>
 * <li>E. Tomita, A. Tanaka, and H. Takahashi. The worst-case time complexity for generating all
 * maximal cliques and computational experiments. Theor. Comput. Sci. 363(1):28–42, 2006.</li>
 * </ul>
 * 
 * <p>
 * where the authors show that using that rule guarantees that the Bron-Kerbosch algorithm has
 * worst-case running time $O(3^{n/3})$ where $n$ is the number of vertices of the graph, excluding
 * time to write the output, which is worst-case optimal.
 * 
 * <p>
 * The algorithm first computes all maximal cliques and then returns the result to the user. A
 * timeout can be set using the constructor parameters.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see BronKerboschCliqueFinder
 * @see DegeneracyBronKerboschCliqueFinder
 *
 * @author Dimitrios Michail
 */
public class PivotBronKerboschCliqueFinder<V, E>
    extends
    BaseBronKerboschCliqueFinder<V, E>
{
    /**
     * Constructs a new clique finder.
     *
     * @param graph the input graph; must be simple
     */
    public PivotBronKerboschCliqueFinder(Graph<V, E> graph)
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
    public PivotBronKerboschCliqueFinder(Graph<V, E> graph, long timeout, TimeUnit unit)
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
                new HashSet<>(graph.vertexSet()), new HashSet<>(), new HashSet<>(), nanosTimeLimit);
        }
    }

    /**
     * Choose a pivot.
     * 
     * @param P vertices to consider adding to the clique
     * @param X vertices which must be excluded from the clique
     * @return a pivot
     */
    private V choosePivot(Set<V> P, Set<V> X)
    {
        int max = -1;
        V pivot = null;

        Iterator<V> it = Stream.concat(P.stream(), X.stream()).iterator();
        while (it.hasNext()) {
            V u = it.next();
            int count = 0;
            for (E e : graph.edgesOf(u)) {
                if (P.contains(Graphs.getOppositeVertex(graph, e, u))) {
                    count++;
                }
            }
            if (count > max) {
                max = count;
                pivot = u;
            }
        }

        return pivot;
    }

    /**
     * Recursive implementation of the Bron-Kerbosch with pivot.
     * 
     * @param P vertices to consider adding to the clique
     * @param R a possibly non-maximal clique
     * @param X vertices which must be excluded from the clique
     * @param nanosTimeLimit time limit
     */
    protected void findCliques(Set<V> P, Set<V> R, Set<V> X, final long nanosTimeLimit)
    {
        /*
         * Check if maximal clique
         */
        if (P.isEmpty() && X.isEmpty()) {
            Set<V> maximalClique = new HashSet<>(R);
            allMaximalCliques.add(maximalClique);
            maxSize = Math.max(maxSize, maximalClique.size());
            return;
        }

        /*
         * Check if timeout
         */
        if (nanosTimeLimit - System.nanoTime() < 0) {
            timeLimitReached = true;
            return;
        }

        /*
         * Choose pivot
         */
        V u = choosePivot(P, X);

        /*
         * Find candidates for addition
         */
        Set<V> uNeighbors = new HashSet<>();
        for (E e : graph.edgesOf(u)) {
            uNeighbors.add(Graphs.getOppositeVertex(graph, e, u));
        }
        Set<V> candidates = new HashSet<>();
        for (V v : P) {
            if (!uNeighbors.contains(v)) {
                candidates.add(v);
            }
        }

        /*
         * Main loop
         */
        for (V v : candidates) {
            Set<V> vNeighbors = new HashSet<>();
            for (E e : graph.edgesOf(v)) {
                vNeighbors.add(Graphs.getOppositeVertex(graph, e, v));
            }

            Set<V> newP = P.stream().filter(vNeighbors::contains).collect(Collectors.toSet());
            Set<V> newX = X.stream().filter(vNeighbors::contains).collect(Collectors.toSet());
            Set<V> newR = new HashSet<>(R);
            newR.add(v);

            findCliques(newP, newR, newX, nanosTimeLimit);

            P.remove(v);
            X.add(v);
        }

    }

}
