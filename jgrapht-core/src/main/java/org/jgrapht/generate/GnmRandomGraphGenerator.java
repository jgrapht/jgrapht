/*
 * (C) Copyright 2005-2021, by Assaf Lehr, Dimitrios Michail and Contributors.
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
 * Create a random graph based on the $G(n, M)$ Erdős–Rényi model. See the Wikipedia article for
 * details and references about <a href="https://en.wikipedia.org/wiki/Random_graph">Random
 * Graphs</a> and the
 * <a href="https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model">Erdős–Rényi model</a>
 * .
 * 
 * <p>
 * In the $G(n, M)$ model, a graph is chosen uniformly at random from the collection of all graphs
 * which have $n$ nodes and $M$ edges. For example, in the $G(3, 2)$ model, each of the three
 * possible graphs on three vertices and two edges are included with probability $\frac{1}{3}$.
 * 
 * <p>
 * The implementation creates the vertices and then randomly chooses an edge and tries to add it. If
 * the add fails for any reason (an edge already exists and multiple (parallel) edges are not
 * allowed) it will just choose another and try again. The performance therefore varies
 * significantly based on the probability of successfully constructing an acceptable edge.
 * 
 * <p>
 * The implementation tries to guess the number of allowed edges based on the following. If
 * self-loops or multiple edges are allowed and requested, the maximum number of edges is
 * {@link Integer#MAX_VALUE}. Otherwise the maximum for undirected graphs with n vertices is
 * $\frac{n(n-1)}{2}$ while for directed $n(n-1)$.
 * 
 * <p>
 * For the $G(n, p)$ model please see {@link GnpRandomGraphGenerator}.
 *
 * @author Assaf Lehr
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GnpRandomGraphGenerator
 */
public class GnmRandomGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private static final boolean DEFAULT_ALLOW_LOOPS = false;
    private static final boolean DEFAULT_ALLOW_MULTIPLE_EDGES = false;

    private final Random rng;
    private final int n;
    private final int m;
    private final boolean loops;
    private final boolean multipleEdges;

    /**
     * Create a new $G(n, M)$ random graph generator. The generator does not create self-loops or
     * multiple (parallel) edges between the same two vertices.
     * 
     * @param n the number of nodes
     * @param m the number of edges
     */
    public GnmRandomGraphGenerator(int n, int m)
    {
        this(n, m, new Random(), DEFAULT_ALLOW_LOOPS, DEFAULT_ALLOW_MULTIPLE_EDGES);
    }

    /**
     * Create a new $G(n, M)$ random graph generator. The generator does not create self-loops or
     * multiple (parallel) edges between the same two vertices.
     * 
     * @param n the number of nodes
     * @param m the number of edges
     * @param seed seed for the random number generator
     */
    public GnmRandomGraphGenerator(int n, int m, long seed)
    {
        this(n, m, new Random(seed), DEFAULT_ALLOW_LOOPS, DEFAULT_ALLOW_MULTIPLE_EDGES);
    }

    /**
     * Create a new $G(n, M)$ random graph generator
     * 
     * @param n the number of nodes
     * @param m the number of edges
     * @param seed seed for the random number generator
     * @param loops whether the generated graph may contain loops
     * @param multipleEdges whether the generated graph many contain multiple (parallel) edges
     *        between the same two vertices
     */
    public GnmRandomGraphGenerator(int n, int m, long seed, boolean loops, boolean multipleEdges)
    {
        this(n, m, new Random(seed), loops, multipleEdges);
    }

    /**
     * Create a new $G(n, M)$ random graph generator
     * 
     * @param n the number of nodes
     * @param m the number of edges
     * @param rng the random number generator
     * @param loops whether the generated graph may contain loops
     * @param multipleEdges whether the generated graph many contain multiple (parallel) edges
     *        between the same two vertices
     */
    public GnmRandomGraphGenerator(int n, int m, Random rng, boolean loops, boolean multipleEdges)
    {
        if (n < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n = n;
        if (m < 0) {
            throw new IllegalArgumentException("number of edges must be non-negative");
        }
        this.m = m;
        this.rng = Objects.requireNonNull(rng);
        this.loops = loops;
        this.multipleEdges = multipleEdges;
    }

    /**
     * Generates a random graph based on the $G(n, M)$ model
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     * 
     * @throws IllegalArgumentException if the number of edges, passed in the constructor, cannot be
     *         created on a graph of the concrete type with the specified number of vertices
     * @throws IllegalArgumentException if the graph does not support a requested feature such as
     *         self-loops or multiple (parallel) edges
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        // special case
        if (n == 0) {
            return;
        }

        // check whether to create loops
        if (loops && !target.getType().isAllowingSelfLoops()) {
            throw new IllegalArgumentException("Provided graph does not support self-loops");
        }

        // check whether to create multiple edges
        if (multipleEdges && !target.getType().isAllowingMultipleEdges()) {
            throw new IllegalArgumentException(
                "Provided graph does not support multiple edges between the same vertices");
        }

        // compute maximum allowed edges
        if (m > computeMaximumAllowedEdges(
            n, target.getType().isDirected(), loops, multipleEdges))
        {
            throw new IllegalArgumentException(
                "number of edges is not valid for the graph type " + "\n-> invalid number of edges="
                    + m + " for:" + " graph type=" + target.getType() + ", number of vertices="
                    + n);
        }

        // create vertices
        List<V> vertices = new ArrayList<>(n);
        int previousVertexSetSize = target.vertexSet().size();
        for (int i = 0; i < n; i++) {
            vertices.add(target.addVertex());
        }

        if (target.vertexSet().size() != previousVertexSetSize + n) {
            throw new IllegalArgumentException(
                "Vertex factory did not produce " + n + " distinct vertices.");
        }

        // create edges
        int edgesCounter = 0;
        while (edgesCounter < m) {
            int sIndex = rng.nextInt(n);
            int tIndex = rng.nextInt(n);

            // lazy to avoid lookups
            V s = null;
            V t = null;

            // check whether to add the edge
            boolean addEdge = false;
            if (sIndex == tIndex) { // self-loop
                if (loops) {
                    addEdge = true;
                }
            } else {
                if (multipleEdges) {
                    addEdge = true;
                } else {
                    s = vertices.get(sIndex);
                    t = vertices.get(tIndex);
                    if (!target.containsEdge(s, t)) {
                        addEdge = true;
                    }
                }
            }

            // if yes, add it
            if (addEdge) {
                try {
                    if (s == null) {
                        s = vertices.get(sIndex);
                        t = vertices.get(tIndex);
                    }
                    E resultEdge = target.addEdge(s, t);
                    if (resultEdge != null) {
                        edgesCounter++;
                    }
                } catch (IllegalArgumentException e) {
                    // do nothing, just ignore the edge
                }
            }
        }
    }

    /**
     * Return the number of allowed edges based on the graph type.
     * 
     * @param n number of nodes
     * @param isDirected whether the graph is directed or not
     * @param createLoops if loops are allowed
     * @param createMultipleEdges if multiple (parallel) edges are allowed
     * @return the number of maximum edges
     */
    static int computeMaximumAllowedEdges(
        int n, boolean isDirected, boolean createLoops, boolean createMultipleEdges)
    {
        if (n == 0) {
            return 0;
        }

        int maxAllowedEdges;
        try {
            if (isDirected) {
                maxAllowedEdges = Math.multiplyExact(n, n - 1);
            } else {
                // assume undirected
                if (n % 2 == 0) {
                    maxAllowedEdges = Math.multiplyExact(n / 2, n - 1);
                } else {
                    maxAllowedEdges = Math.multiplyExact(n, (n - 1) / 2);
                }
            }

            if (createLoops) {
                if (createMultipleEdges) {
                    return Integer.MAX_VALUE;
                } else {
                    if (isDirected) {
                        maxAllowedEdges = Math.addExact(maxAllowedEdges, Math.multiplyExact(2, n));
                    } else {
                        // assume undirected
                        maxAllowedEdges = Math.addExact(maxAllowedEdges, n);
                    }
                }
            } else {
                if (createMultipleEdges) {
                    if (n > 1) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
        return maxAllowedEdges;
    }

}
