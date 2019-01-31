/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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

import java.util.*;

/**
 * Barabási-Albert growth and preferential attachment graph generator.
 * 
 * <p>
 * The generator is described in the paper: A.-L. Barabási and R. Albert. Emergence of scaling in
 * random networks. Science, 286:509-512, 1999.
 * 
 * <p>
 * The generator starts with a complete graph of $m_0$ nodes and grows the network by adding $n -
 * m_0$ additional nodes. The additional nodes are added one by one and each of them is connected to
 * $m$ previously added nodes, where the probability of connecting to a node is proportional to its
 * degree.
 * 
 * <p>
 * Note that the Barabàsi-Albert model is designed for undirected networks. Nevertheless, this
 * generator also works with directed networks where the probabilities are proportional to the sum
 * of incoming and outgoing degrees. For a more general discussion see the paper: M. E. J. Newman.
 * The Structure and Function of Complex Networks. SIAM Rev., 45(2):167--256, 2003.
 *
 * <p>
 * For a version that generates trees/forests see {@link BarabasiAlbertForestGenerator}.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BarabasiAlbertGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;
    private final int m0;
    private final int m;
    private final int n;

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n)
    {
        this(m0, m, n, new Random());
    }

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n, long seed)
    {
        this(m0, m, n, new Random(seed));
    }

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n, Random rng)
    {
        if (m0 < 1) {
            throw new IllegalArgumentException("invalid initial nodes (" + m0 + " < 1)");
        }
        this.m0 = m0;
        if (m <= 0) {
            throw new IllegalArgumentException("invalid edges per node (" + m + " <= 0");
        }
        if (m > m0) {
            throw new IllegalArgumentException("invalid edges per node (" + m + " > " + m0 + ")");
        }
        this.m = m;
        if (n < m0) {
            throw new IllegalArgumentException(
                "total number of nodes must be at least equal to the initial set");
        }
        this.n = n;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates an instance.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        /*
         * Create complete graph with m0 nodes
         */
        Set<V> oldNodes = new HashSet<>(target.vertexSet());
        Set<V> newNodes = new HashSet<>();
        new CompleteGraphGenerator<V, E>(m0).generateGraph(target, resultMap);
        target.vertexSet().stream().filter(v -> !oldNodes.contains(v)).forEach(newNodes::add);

        List<V> nodes = new ArrayList<>(n * m);
        nodes.addAll(newNodes);
        /*
         * Augment node list to have node multiplicity equal to min(1,m0-1).
         */
        for (int i = 0; i < m0 - 2; i++) {
            nodes.addAll(newNodes);
        }

        /*
         * Grow network with preferential attachment
         */
        for (int i = m0; i < n; i++) {
            V v = target.addVertex();

            List<V> newEndpoints = new ArrayList<>();
            int added = 0;
            while (added < m) {
                V u = nodes.get(rng.nextInt(nodes.size()));
                if (!target.containsEdge(v, u)) {
                    target.addEdge(v, u);
                    added++;
                    newEndpoints.add(v);
                    if (i > 1) {
                        newEndpoints.add(u);
                    }
                }
            }
            nodes.addAll(newEndpoints);
        }

    }

}
