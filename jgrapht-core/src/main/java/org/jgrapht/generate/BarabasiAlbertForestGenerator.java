/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
 * Barabási-Albert growth and preferential attachment forest generator.
 * 
 * <p>
 * The general graph generator is described in the paper: A.-L. Barabási and R. Albert. Emergence of
 * scaling in random networks. Science, 286:509-512, 1999.
 * 
 * <p>
 * The generator starts with a $t$ isolated nodes and grows the network by adding $n - t$ additional
 * nodes. The additional nodes are added one by one and each of them is connected to one previously
 * added node, where the probability of connecting to a node is proportional to its degree.
 * 
 * <p>
 * Note that this Barabàsi-Albert generator only works on undirected graphs. For a version that
 * works on both directed and undirected graphs and generates only connected graphs see
 * {@link BarabasiAlbertGraphGenerator}.
 * 
 * @author Alexandru Valeanu
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BarabasiAlbertForestGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{

    private final Random rng;
    private final int t;
    private final int n;

    /**
     * Constructor
     * 
     * @param t number of trees
     * @param n final number of nodes
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertForestGenerator(int t, int n)
    {
        this(t, n, new Random());
    }

    /**
     * Constructor
     * 
     * @param t number of trees
     * @param n final number of nodes
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertForestGenerator(int t, int n, long seed)
    {
        this(t, n, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param t number of trees
     * @param n final number of nodes
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertForestGenerator(int t, int n, Random rng)
    {
        if (t < 1) {
            throw new IllegalArgumentException("invalid number of trees (" + t + " < 1)");
        }

        this.t = t;

        if (n < t) {
            throw new IllegalArgumentException(
                "total number of nodes must be at least equal to the number of trees");
        }

        this.n = n;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates an instance.
     *
     * <p>
     * Note: An exception will be thrown if the target graph is not empty (i.e. contains at least
     * one vertex)
     * </p>
     *
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     * @throws NullPointerException if {@code target} is {@code null}
     * @throws IllegalArgumentException if {@code target} is not undirected
     * @throws IllegalArgumentException if {@code target} is not empty
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        GraphTests.requireUndirected(target);

        if (!target.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("target graph is not empty");
        }

        assert target.vertexSet().isEmpty();
        assert target.edgeSet().isEmpty();

        List<V> nodes = new ArrayList<>();

        /*
         * Add t roots, one for each tree in the forest
         */
        for (int i = 0; i < t; i++) {
            nodes.add(target.addVertex());
        }

        /*
         * Grow forest with preferential attachment
         */
        for (int i = t; i < n; i++) {
            V v = target.addVertex();
            V u = nodes.get(rng.nextInt(nodes.size()));

            assert !target.containsEdge(v, u);
            target.addEdge(v, u);

            nodes.add(v);

            if (i > 1) {
                nodes.add(u);
            }
        }

    }

}
