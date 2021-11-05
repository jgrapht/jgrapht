/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
 * Create a random graph based on the $G(n, p)$ Erdős–Rényi model. See the Wikipedia article for
 * details and references about <a href="https://en.wikipedia.org/wiki/Random_graph">Random
 * Graphs</a> and the
 * <a href="https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model">Erdős–Rényi model</a>
 * .
 * 
 * <p>
 * In the $G(n, p)$ model, a graph is constructed by connecting nodes randomly. Each edge is
 * included in the graph with probability $p$ independent from every other edge. The complexity of
 * the generator is $O(n^2)$ where $n$ is the number of vertices.
 * 
 * <p>
 * For the $G(n, M)$ model please see {@link GnmRandomGraphGenerator}.
 *
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GnmRandomGraphGenerator
 */
public class GnpRandomGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private static final boolean DEFAULT_ALLOW_LOOPS = false;

    private final Random rng;
    private final int n;
    private final double p;
    private final boolean createLoops;

    /**
     * Create a new $G(n, p)$ random graph generator. The generator does not create self-loops.
     * 
     * @param n the number of nodes
     * @param p the edge probability
     */
    public GnpRandomGraphGenerator(int n, double p)
    {
        this(n, p, new Random(), DEFAULT_ALLOW_LOOPS);
    }

    /**
     * Create a new $G(n, p)$ random graph generator. The generator does not create self-loops.
     * 
     * @param n the number of nodes
     * @param p the edge probability
     * @param seed seed for the random number generator
     */
    public GnpRandomGraphGenerator(int n, double p, long seed)
    {
        this(n, p, new Random(seed), DEFAULT_ALLOW_LOOPS);
    }

    /**
     * Create a new $G(n, p)$ random graph generator.
     * 
     * @param n the number of nodes
     * @param p the edge probability
     * @param seed seed for the random number generator
     * @param createLoops whether the generated graph may create loops
     */
    public GnpRandomGraphGenerator(int n, double p, long seed, boolean createLoops)
    {
        this(n, p, new Random(seed), createLoops);
    }

    /**
     * Create a new $G(n, p)$ random graph generator.
     * 
     * @param n the number of nodes
     * @param p the edge probability
     * @param rng the random number generator to use
     * @param createLoops whether the generated graph may create loops
     */
    public GnpRandomGraphGenerator(int n, double p, Random rng, boolean createLoops)
    {
        if (n < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n = n;
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("not valid probability of edge existence");
        }
        this.p = p;
        this.rng = Objects.requireNonNull(rng);
        this.createLoops = createLoops;
    }

    /**
     * Generates a random graph based on the $G(n, p)$ model.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // special case
        if (n == 0) {
            return;
        }

        // check whether to also create loops
        if (createLoops && !target.getType().isAllowingSelfLoops()) {
            throw new IllegalArgumentException("Provided graph does not support self-loops");
        }

        // create vertices
        int previousVertexSetSize = target.vertexSet().size();
        List<V> vertices = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            vertices.add(target.addVertex());
        }

        if (target.vertexSet().size() != previousVertexSetSize + n) {
            throw new IllegalArgumentException(
                "Vertex factory did not produce " + n + " distinct vertices.");
        }

        // check if graph is directed
        boolean isDirected = target.getType().isDirected();

        // create edges
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {

                if (i == j) {
                    if (!createLoops) {
                        // no self-loops
                        continue;
                    }
                }

                V s = null;
                V t = null;

                // s->t
                if (rng.nextDouble() < p) {
                    s = vertices.get(i);
                    t = vertices.get(j);
                    target.addEdge(s, t);
                }

                if (isDirected) {
                    // t->s
                    if (rng.nextDouble() < p) {
                        if (s == null) {
                            s = vertices.get(i);
                            t = vertices.get(j);
                        }
                        target.addEdge(t, s);
                    }
                }
            }
        }

    }

}
