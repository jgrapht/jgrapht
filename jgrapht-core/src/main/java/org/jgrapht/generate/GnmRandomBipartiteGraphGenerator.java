/*
 * (C) Copyright 2004-2018, by Michael Behrisch, Dimitrios Michail and Contributors.
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
 * Create a random bipartite graph based on the $G(n, M)$ Erdős–Rényi model. See the Wikipedia
 * article for details and references about
 * <a href="https://en.wikipedia.org/wiki/Random_graph">Random Graphs</a> and the
 * <a href="https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model">Erdős–Rényi model</a>
 * .
 * 
 * The user provides the sizes $n_1$ and $n_2$ of the two partitions $(n_1+n_2=n)$ and a number $m$
 * which is the total number of edges to create. The generator supports both directed and undirected
 * graphs.
 *
 * @author Michael Behrisch
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GnpRandomBipartiteGraphGenerator
 */
public class GnmRandomBipartiteGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;
    private final int n1;
    private final int n2;
    private final int m;

    private Map<Integer, V> partitionA;
    private Map<Integer, V> partitionB;

    /**
     * Create a new random bipartite graph generator. The generator uses the $G(n, m)$ model when $n
     * = n1 + n2$ and the bipartite graph has one partition with size $n_1$ and one partition with
     * size $n_2$. In this model a graph is chosen uniformly at random from the collection of
     * bipartite graphs whose partitions have sizes $n_1$ and $n_2$ respectively and $m$ edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param m the number of edges
     */
    public GnmRandomBipartiteGraphGenerator(int n1, int n2, int m)
    {
        this(n1, n2, m, new Random());
    }

    /**
     * Create a new random bipartite graph generator. The generator uses the $G(n, m)$ model when $n
     * = n1 + n2$ and the bipartite graph has one partition with size $n_1$ and one partition with
     * size $n_2$. In this model a graph is chosen uniformly at random from the collection of
     * bipartite graphs whose partitions have sizes $n_1$ and $n_2$ respectively and m edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param m the number of edges
     * @param seed seed for the random number generator
     */
    public GnmRandomBipartiteGraphGenerator(int n1, int n2, int m, long seed)
    {
        this(n1, n2, m, new Random(seed));
    }

    /**
     * Create a new random bipartite graph generator. The generator uses the $G(n, m)$ model when $n
     * = n_1 + n_2$ and the bipartite graph has one partition with size $n_1$ and one partition with
     * size $n_2$. In this model a graph is chosen uniformly at random from the collection of
     * bipartite graphs whose partitions have sizes $n_1$ and $n_2$ respectively and $m$ edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param m the number of edges
     * @param rng random number generator
     */
    public GnmRandomBipartiteGraphGenerator(int n1, int n2, int m, Random rng)
    {
        if (n1 < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n1 = n1;
        if (n2 < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n2 = n2;
        if (m < 0) {
            throw new IllegalArgumentException("number of edges must be non-negative");
        }
        this.m = m;
        this.rng = Objects.requireNonNull(rng);
    }

    /**
     * Generates a random bipartite graph.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (n1 + n2 == 0) {
            return;
        }

        // create vertices
        int previousVertexSetSize = target.vertexSet().size();

        partitionA = new LinkedHashMap<>(n1);
        for (int i = 0; i < n1; i++) {
            partitionA.put(i, target.addVertex());
        }

        partitionB = new LinkedHashMap<>(n2);
        for (int i = 0; i < n2; i++) {
            partitionB.put(i, target.addVertex());
        }

        if (target.vertexSet().size() != previousVertexSetSize + n1 + n2) {
            throw new IllegalArgumentException(
                "Vertex factory did not produce " + (n1 + n2) + " distinct vertices.");
        }

        // check if graph is directed
        final boolean isDirected = target.getType().isDirected();

        int maxAllowedEdges;
        try {
            if (isDirected) {
                maxAllowedEdges = Math.multiplyExact(2, Math.multiplyExact(n1, n2));
            } else {
                // assume undirected
                maxAllowedEdges = Math.multiplyExact(n1, n2);
            }
        } catch (ArithmeticException e) {
            maxAllowedEdges = Integer.MAX_VALUE;
        }

        if (m > maxAllowedEdges) {
            throw new IllegalArgumentException(
                "number of edges not valid for bipartite graph with " + n1 + " and " + n2
                    + " vertices");
        }

        // create edges
        int edgesCounter = 0;
        while (edgesCounter < m) {
            // find random edge
            V s = partitionA.get(rng.nextInt(n1));
            V t = partitionB.get(rng.nextInt(n2));

            // if directed, maybe reverse direction
            if (isDirected && rng.nextBoolean()) {
                V tmp = s;
                s = t;
                t = tmp;
            }

            // check whether to add the edge
            if (!target.containsEdge(s, t)) {
                try {
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
     * Returns the first partition of vertices in the bipartite graph. This partition is guaranteed
     * to be smaller than or equal in size to the second partition.
     * 
     * @return one partition of the bipartite graph
     */
    public Set<V> getFirstPartition()
    {
        if (partitionA.size() <= partitionB.size())
            return new LinkedHashSet<>(partitionA.values());
        else
            return new LinkedHashSet<>(partitionB.values());
    }

    /**
     * Returns the second partitions of vertices in the bipartite graph. This partition is
     * guaranteed to be larger than or equal in size to the first partition.
     * 
     * @return one partition of the bipartite graph
     */
    public Set<V> getSecondPartition()
    {
        if (partitionB.size() >= partitionA.size())
            return new LinkedHashSet<>(partitionB.values());
        else
            return new LinkedHashSet<>(partitionA.values());
    }

}
