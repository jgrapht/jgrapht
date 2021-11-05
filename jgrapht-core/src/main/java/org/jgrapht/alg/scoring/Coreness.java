/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Computes the coreness of each vertex in an undirected graph.
 * 
 * <p>
 * A $k$-core of a graph $G$ is a maximal connected subgraph of $G$ in which all vertices have
 * degree at least $k$. Equivalently, it is one of the connected components of the subgraph of $G$
 * formed by repeatedly deleting all vertices of degree less than $k$. A vertex $u$ has coreness $c$
 * if it belongs to a $c$-core but not to any $(c+1)$-core.
 *
 * <p>
 * If a non-empty k-core exists, then, clearly, $G$ has
 * <a href="https://en.wikipedia.org/wiki/Degeneracy_(graph_theory)">degeneracy</a> at least $k$,
 * and the degeneracy of $G$ is the largest $k$ for which $G$ has a $k$-core.
 *
 * <p>
 * As described in the following paper
 * <ul>
 * <li>D. W. Matula and L. L. Beck. Smallest-last ordering and clustering and graph coloring
 * algorithms. Journal of the ACM, 30(3):417--427, 1983.</li>
 * </ul>
 * it is possible to find a vertex ordering of a finite graph $G$ that optimizes the coloring number
 * of the ordering, in linear time, by using a bucket queue to repeatedly find and remove the vertex
 * of smallest degree.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public final class Coreness<V, E>
    implements
    VertexScoringAlgorithm<V, Integer>
{
    private final Graph<V, E> g;
    private Map<V, Integer> scores;
    private int degeneracy;

    /**
     * Constructor
     * 
     * @param g the input graph
     */
    public Coreness(Graph<V, E> g)
    {
        this.g = GraphTests.requireUndirected(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Integer> getScores()
    {
        lazyRun();
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getVertexScore(V v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        lazyRun();
        return scores.get(v);
    }

    /**
     * Compute the degeneracy of a graph.
     * 
     * <p>
     * The degeneracy of a graph is the smallest value of $k$ for which it is $k$-degenerate. In
     * graph theory, a $k$-degenerate graph is an undirected graph in which every subgraph has a
     * vertex of degree at most $k$: that is, some vertex in the subgraph touches $k$ or fewer of
     * the subgraph's edges.
     * 
     * @return the degeneracy of a graph
     */
    public int getDegeneracy()
    {
        lazyRun();
        return degeneracy;
    }

    @SuppressWarnings("unchecked")
    private void lazyRun()
    {
        if (scores != null) {
            return;
        }

        if (!GraphTests.isSimple(g)) {
            throw new IllegalArgumentException("Graph must be simple");
        }

        scores = new HashMap<>();
        degeneracy = 0;

        /*
         * Initialize buckets
         */
        int n = g.vertexSet().size();
        int maxDegree = n - 1;
        Set<V>[] buckets = (Set<V>[]) Array.newInstance(Set.class, maxDegree + 1);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new HashSet<>();
        }

        int minDegree = n;
        Map<V, Integer> degrees = new HashMap<>();
        for (V v : g.vertexSet()) {
            int d = g.degreeOf(v);
            buckets[d].add(v);
            degrees.put(v, d);
            minDegree = Math.min(minDegree, d);
        }

        /*
         * Extract from buckets
         */
        while (minDegree < n) {
            Set<V> b = buckets[minDegree];
            if (b.isEmpty()) {
                minDegree++;
                continue;
            }

            V v = b.iterator().next();
            b.remove(v);
            scores.put(v, minDegree);
            degeneracy = Math.max(degeneracy, minDegree);

            for (E e : g.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(g, e, v);
                int uDegree = degrees.get(u);
                if (uDegree > minDegree && !scores.containsKey(u)) {
                    buckets[uDegree].remove(u);
                    uDegree--;
                    degrees.put(u, uDegree);
                    buckets[uDegree].add(u);
                    minDegree = Math.min(minDegree, uDegree);
                }
            }
        }

    }

}
