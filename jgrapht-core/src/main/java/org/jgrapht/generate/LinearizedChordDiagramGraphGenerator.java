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
import org.jgrapht.graph.*;

import java.util.*;

/**
 * The linearized chord diagram graph model generator.
 * 
 * <p>
 * The generator makes precise several unspecified mathematical details of the Barabási-Albert
 * model, such as the initial configuration of the first nodes, and whether the $m$ links assigned
 * to a new node are added one by one, or simultaneously, etc. The generator is described in the
 * paper: Bélaa Bollobás and Oliver Riordan. The Diameter of a Scale-Free Random Graph. Journal
 * Combinatorica, 24(1): 5--34, 2004.
 * 
 * <p>
 * In contrast with the Barabási-Albert model, the model of Bollobás and Riordan allows for multiple
 * edges (parallel-edges) and self-loops. They show, however, that their number will be small. This
 * means that this generator works only on graphs which allow multiple edges (parallel-edges) such
 * as {@link Pseudograph} or {@link DirectedPseudograph}.
 * 
 * <p>
 * The generator starts with a graph of one node and grows the network by adding $n-1$ additional
 * nodes. The additional nodes are added one by one and each of them is connected to $m$ previously
 * added nodes (or to itself with a small probability), where the probability of connecting to a
 * node is proportional to its degree.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class LinearizedChordDiagramGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;
    private final int m;
    private final int n;

    /**
     * Constructor
     * 
     * @param n number of nodes
     * @param m number of edges of each new node added during the network growth
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public LinearizedChordDiagramGraphGenerator(int n, int m)
    {
        this(n, m, new Random());
    }

    /**
     * Constructor
     * 
     * @param n number of nodes
     * @param m number of edges of each new node added during the network growth
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public LinearizedChordDiagramGraphGenerator(int n, int m, long seed)
    {
        this(n, m, new Random(seed));
    }

    /**
     * Constructor
     * 
     * @param n number of nodes
     * @param m number of edges of each new node added during the network growth
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public LinearizedChordDiagramGraphGenerator(int n, int m, Random rng)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("invalid number of nodes: must be positive");
        }
        this.n = n;
        if (m <= 0) {
            throw new IllegalArgumentException("invalid edges per node (" + m + " <= 0");
        }
        this.m = m;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates an instance.
     * 
     * @param target the target graph, which must allow self-loops and parallel edges
     * @param resultMap not used by this generator, can be null
     * @throws IllegalArgumentException if the graph does not allow self-loops or parallel edges
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        /*
         * Add nodes by maintaining a list with vertex multiplicity equal to its degree for sampling
         * purposes.
         */
        List<V> nodes = new ArrayList<>(2 * n * m);
        for (int t = 0; t < n; t++) {
            // add node
            V vt = target.addVertex();

            // add edges
            for (int j = 0; j < m; j++) {
                // add outward half degree before sampling
                nodes.add(vt);

                // sample
                V vs = nodes.get(rng.nextInt(nodes.size()));
                if (target.addEdge(vt, vs) == null) {
                    throw new IllegalArgumentException("Graph does not permit parallel-edges.");
                }

                // add inward half-degree after sampling
                nodes.add(vs);
            }
        }

    }

}
