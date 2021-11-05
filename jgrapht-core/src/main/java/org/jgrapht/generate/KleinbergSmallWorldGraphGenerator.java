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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Kleinberg's small-world graph generator.
 * 
 * <p>
 * The generator is described in the paper: J. Kleinberg, The Small-World Phenomenon: An Algorithmic
 * Perspective, in Proc. 32nd ACM Symp. Theory of Comp., 163-170, 2000.
 * 
 * <p>
 * The basic structure is a a two-dimensional grid and allows for edges to be directed. It begins
 * with a set of nodes (representing individuals in the social network) that are identified with the
 * set of lattice points in an $n \times n$ square. For a universal constant $p \geq 1$, the node
 * $u$ has a directed edge to every other node within lattice distance $p$ (these are its local
 * contacts). For universal constants $q \geq 0$ and $r \geq 0$, we also construct directed edges
 * from $u$ to $q$ other nodes (the long-range contacts) using independent random trials; the i-th
 * directed edge from $u$ has endpoint $v$ with probability proportional to \frac{1}{d(u,v)^r}$
 * where $d(u,v)$ is the lattice distance from $u$ to $v$.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class KleinbergSmallWorldGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;

    private final int n;
    private final int p;
    private final int q;
    private final int r;

    /**
     * Constructor
     * 
     * @param n generate set of lattice points in a $n$ by $n$ square
     * @param p lattice distance for which each node is connected to every other node in the lattice
     *        (local connections)
     * @param q how many long-range contacts to add for each node
     * @param r probability distribution parameter which is a basic structural parameter measuring
     *        how widely "networked" the underlying society of nodes is
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public KleinbergSmallWorldGraphGenerator(int n, int p, int q, int r)
    {
        this(n, p, q, r, new Random());
    }

    /**
     * Constructor
     * 
     * @param n generate set of lattice points in a $n$ by $n$ square
     * @param p lattice distance for which each node is connected to every other node in the lattice
     *        (local connections)
     * @param q how many long-range contacts to add for each node
     * @param r probability distribution parameter which is a basic structural parameter measuring
     *        how widely "networked" the underlying society of nodes is
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public KleinbergSmallWorldGraphGenerator(int n, int p, int q, int r, long seed)
    {
        this(n, p, q, r, new Random(seed));
    }

    /**
     * Constructor
     * 
     * @param n generate set of lattice points in a $n \times n$ square
     * @param p lattice distance for which each node is connected to every other node in the lattice
     *        (local connections)
     * @param q how many long-range contacts to add for each node
     * @param r probability distribution parameter which is a basic structural parameter measuring
     *        how widely "networked" the underlying society of nodes is
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public KleinbergSmallWorldGraphGenerator(int n, int p, int q, int r, Random rng)
    {
        if (n < 1) {
            throw new IllegalArgumentException("parameter n must be positive");
        }
        this.n = n;
        if (p < 1) {
            throw new IllegalArgumentException("parameter p must be positive");
        }
        if (p > 2 * n - 2) {
            throw new IllegalArgumentException("lattice distance too large");
        }
        this.p = p;
        if (q < 0) {
            throw new IllegalArgumentException("parameter q must be non-negative");
        }
        this.q = q;
        if (r < 0) {
            throw new IllegalArgumentException("parameter r must be non-negative");
        }
        this.r = r;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates a small-world graph.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        /*
         * Special cases
         */
        if (n == 0) {
            return;
        } else if (n == 1) {
            target.addVertex();
            return;
        }

        /*
         * Ensure directed or undirected
         */
        GraphTests.requireDirectedOrUndirected(target);
        boolean isDirected = target.getType().isDirected();

        /*
         * Create vertices
         */
        List<V> nodes = new ArrayList<>(n * n);
        for (int i = 0; i < n * n; i++) {
            nodes.add(target.addVertex());
        }

        /*
         * Add local-contacts
         */
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int vi = i * n + j;
                V v = nodes.get(vi);

                // lookup neighborhood
                for (int di = -p; di <= p; di++) {
                    for (int dj = -p; dj <= p; dj++) {
                        int t = (i + di) * n + (j + dj);
                        if (t < 0 || t == vi || t >= n * n) {
                            continue;
                        }
                        if (Math.abs(di) + Math.abs(dj) <= p && (isDirected || t > i * n + j)) {
                            target.addEdge(v, nodes.get(t));
                        }
                    }
                }
            }
        }

        /*
         * Add long-range contacts
         */
        double[] p = new double[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                V v = nodes.get(i * n + j);

                /*
                 * Create inverse r power distribution
                 */
                double sum = 0d;
                for (int oi = 0; oi < n; oi++) {
                    for (int oj = 0; oj < n; oj++) {
                        if (oi != i || oj != j) {
                            double weight = Math.pow(Math.abs(i - oi) + Math.abs(j - oj), -r);
                            p[oi * n + oj] = weight;
                            sum += weight;
                        }
                    }
                }
                p[i * n + j] = 0d;
                for (int k = 0; k < n * n; k++) {
                    p[k] /= sum;
                }

                /*
                 * Sample from distribution and add long-range edges
                 */
                AliasMethodSampler sampler = new AliasMethodSampler(p, rng);
                for (int k = 0; k < q; k++) {
                    V u = nodes.get(sampler.next());
                    if (!u.equals(v) && !target.containsEdge(v, u)) {
                        target.addEdge(v, u);
                    }
                }
            }
        }
    }

}
