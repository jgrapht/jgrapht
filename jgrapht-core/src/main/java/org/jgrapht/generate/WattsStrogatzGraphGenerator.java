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
import org.jgrapht.util.*;

import java.util.*;

/**
 * Watts-Strogatz small-world graph generator.
 * 
 * <p>
 * The generator is described in the paper: D. J. Watts and S. H. Strogatz. Collective dynamics of
 * small-world networks. Nature 393(6684):440--442, 1998.
 * 
 * <p>
 * The following paragraph from the paper describes the construction.
 * 
 * <p>
 * "The generator starts with a ring of $n$ vertices, each connected to its $k$ nearest neighbors
 * ($k$ must be even). Then it chooses a vertex and the edge that connects it to its nearest
 * neighbor in a clockwise sense. With probability $p$, it reconnects this edge to a vertex chosen
 * uniformly at random over the entire ring with duplicate edges forbidden; otherwise it leaves the
 * edge in place. The process is repeated by moving clock-wise around the ring, considering each
 * vertex in turn until one lap is completed. Next, it considers the edges that connect vertices to
 * their second-nearest neighbors clockwise. As before, it randomly rewires each of these edges with
 * probability $p$, and continues this process, circulating around the ring and proceeding outward
 * to more distant neighbors after each lap, until each edge in the original lattice has been
 * considered once. As there are $\frac{nk}{2}$ edges in the entire graph, the rewiring process
 * stops after $\frac{k}{2}$ laps. For $p = 0$, the original ring is unchanged; as $p$ increases,
 * the graph becomes increasingly disordered until for $p = 1$, all edges are rewired randomly. For
 * intermediate values of $p$, the graph is a small-world network: highly clustered like a regular
 * graph, yet with small characteristic path length, like a random graph."
 * 
 * <p>
 * The authors require $n \gg k \gg \ln(n) \gg 1$ and specifically $k \gg \ln(n)$ guarantees that a
 * random graph will be connected.
 * 
 * <p>
 * Through the constructor parameter the model can be slightly changed into adding shortcut edges
 * instead of re-wiring. This variation was proposed in the paper: M. E. J. Newman and D. J. Watts,
 * Renormalization group analysis of the small-world network model, Physics Letters A, 263, 341,
 * 1999.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class WattsStrogatzGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private static final boolean DEFAULT_ADD_INSTEAD_OF_REWIRE = false;

    private final Random rng;
    private final int n;
    private final int k;
    private final double p;
    private final boolean addInsteadOfRewire;

    /**
     * Constructor
     * 
     * @param n the number of nodes
     * @param k connect each node to its k nearest neighbors in a ring
     * @param p the probability of re-wiring each edge
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public WattsStrogatzGraphGenerator(int n, int k, double p)
    {
        this(n, k, p, DEFAULT_ADD_INSTEAD_OF_REWIRE, new Random());
    }

    /**
     * Constructor
     * 
     * @param n the number of nodes
     * @param k connect each node to its k nearest neighbors in a ring
     * @param p the probability of re-wiring each edge
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public WattsStrogatzGraphGenerator(int n, int k, double p, long seed)
    {
        this(n, k, p, DEFAULT_ADD_INSTEAD_OF_REWIRE, new Random(seed));
    }

    /**
     * Constructor
     * 
     * @param n the number of nodes
     * @param k connect each node to its k nearest neighbors in a ring
     * @param p the probability of re-wiring each edge
     * @param addInsteadOfRewire whether to add shortcut edges instead of re-wiring
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public WattsStrogatzGraphGenerator(
        int n, int k, double p, boolean addInsteadOfRewire, Random rng)
    {
        if (n < 3) {
            throw new IllegalArgumentException("number of vertices must be at least 3");
        }
        this.n = n;
        if (k < 1) {
            throw new IllegalArgumentException("number of k-nearest neighbors must be positive");
        }
        if (k % 2 == 1) {
            throw new IllegalArgumentException("number of k-nearest neighbors must be even");
        }
        if (k > n - 2 + (n % 2)) {
            throw new IllegalArgumentException("invalid k-nearest neighbors");
        }
        this.k = k;

        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("invalid probability");
        }
        this.p = p;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
        this.addInsteadOfRewire = addInsteadOfRewire;
    }

    /**
     * Generates a small-world graph based on the Watts-Strogatz model.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // special cases
        if (n == 0) {
            return;
        } else if (n == 1) {
            target.addVertex();
            return;
        }

        // create ring lattice
        List<V> ring = new ArrayList<>(n);
        Map<V, List<E>> adj = CollectionUtil.newLinkedHashMapWithExpectedSize(n);

        for (int i = 0; i < n; i++) {
            V v = target.addVertex();
            ring.add(v);
            adj.put(v, new ArrayList<>(k));
        }

        for (int i = 0; i < n; i++) {
            V vi = ring.get(i);
            List<E> viAdj = adj.get(vi);

            for (int j = 1; j <= k / 2; j++) {
                viAdj.add(target.addEdge(vi, ring.get((i + j) % n)));
            }
        }

        // re-wire edges
        for (int r = 0; r < k / 2; r++) {
            for (int i = 0; i < n; i++) {
                if (rng.nextDouble() < p) {
                    V v = ring.get(i);
                    E e = adj.get(v).get(r);
                    V other = ring.get(rng.nextInt(n));
                    if (!other.equals(v) && !target.containsEdge(v, other)) {
                        if (!addInsteadOfRewire) {
                            target.removeEdge(e);
                        }
                        target.addEdge(v, other);
                    }
                }
            }
        }

    }

}
