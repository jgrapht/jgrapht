/*
 * (C) Copyright 2018-2021, by Emilio Cruciani and Contributors.
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
 * Generate a random $d$-regular undirected graph with $n$ vertices. A regular graph is a graph
 * where each vertex has the same degree, i.e. the same number of neighbors.
 *
 * <p>
 * The algorithm for the simple case, proposed in [SW99] and extending the one for the non-simple
 * case [W99], runs in expected $\mathcal{O}(nd^2)$ time. It has been proved in [KV03] to sample
 * from the space of random d-regular graphs in a way which is asymptotically uniform at random when
 * $d = \mathcal{O}(n^{1/3 - \epsilon})$.
 *
 * <p>
 * [KV03] Kim, Jeong Han, and Van H. Vu. "Generating random regular graphs." Proceedings of the
 * thirty-fifth annual ACM symposium on Theory of computing. ACM, 2003.
 *
 * [SW99] Steger, Angelika, and Nicholas C. Wormald. "Generating random regular graphs quickly."
 * Combinatorics, Probability and Computing 8.4 (1999): 377-396.
 *
 * [W99] Wormald, Nicholas C. "Models of random regular graphs." London Mathematical Society Lecture
 * Note Series (1999): 239-298.
 *
 * @author Emilio Cruciani
 *
 * @param <V> graph node type
 * @param <E> graph edge type
 */
public class RandomRegularGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int n;
    private final int d;
    private final Random rng;

    /**
     * Construct a new RandomRegularGraphGenerator.
     *
     * @param n number of nodes
     * @param d degree of nodes
     * @throws IllegalArgumentException if number of nodes is negative
     * @throws IllegalArgumentException if degree is negative
     * @throws IllegalArgumentException if degree is greater than number of nodes
     * @throws IllegalArgumentException if the value "n * d" is odd
     */
    public RandomRegularGraphGenerator(int n, int d)
    {
        this(n, d, new Random());
    }

    /**
     * Construct a new RandomRegularGraphGenerator.
     *
     * @param n number of nodes
     * @param d degree of nodes
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException if number of nodes is negative
     * @throws IllegalArgumentException if degree is negative
     * @throws IllegalArgumentException if degree is greater than number of nodes
     * @throws IllegalArgumentException if the value "n * d" is odd
     */
    public RandomRegularGraphGenerator(int n, int d, long seed)
    {
        this(n, d, new Random(seed));
    }

    /**
     * Construct a new RandomRegularGraphGenerator.
     *
     * @param n number of nodes
     * @param d degree of nodes
     * @param rng the random number generator to use
     * @throws IllegalArgumentException if number of nodes is negative
     * @throws IllegalArgumentException if degree is negative
     * @throws IllegalArgumentException if degree is greater than number of nodes
     * @throws IllegalArgumentException if the value "n * d" is odd
     */
    public RandomRegularGraphGenerator(int n, int d, Random rng)
    {
        if (n < 0) {
            throw new IllegalArgumentException("number of nodes must be non-negative");
        }
        if (d < 0) {
            throw new IllegalArgumentException("degree of nodes must be non-negative");
        }
        if (d > n) {
            throw new IllegalArgumentException(
                "degree of nodes must be smaller than or equal to number of nodes");
        }
        if ((n * d) % 2 != 0) {
            throw new IllegalArgumentException("value 'n * d' must be even");
        }
        this.n = n;
        this.d = d;
        this.rng = rng;
    }

    /**
     * Generate a random regular graph.
     *
     * @param target the target graph
     * @param resultMap the result map
     * @throws IllegalArgumentException if target is not an undirected graph
     * @throws IllegalArgumentException if "n == d" and the graph is simple
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (!target.getType().isUndirected()) {
            throw new IllegalArgumentException("target graph must be undirected");
        }

        if (target.getType().isSimple()) {
            // simple case
            if (n == 0 || d == 0) {
                // no nodes or zero degree case
                new EmptyGraphGenerator<V, E>(n).generateGraph(target);
            } else if (d == n) {
                throw new IllegalArgumentException("target graph must be simple if 'n == d'");
            } else if (d == n - 1) {
                // complete case
                new CompleteGraphGenerator<V, E>(n).generateGraph(target);
            } else {
                // general case
                generateSimpleRegularGraph(target);
            }
        } else {
            // non-simple case
            generateNonSimpleRegularGraph(target);
        }
    }

    /*
     * Auxiliary method to check if there are remaining suitable edges, in the simple regular graph
     * generator.
     */
    private boolean suitable(
        Set<Map.Entry<Integer, Integer>> edges, Map<Integer, Integer> potentialEdges)
    {
        if (potentialEdges.isEmpty()) {
            return true;
        }

        Integer[] keys = potentialEdges.keySet().toArray(new Integer[0]);
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            int s2 = keys[i];
            for (int j = 0; j < i; j++) {
                int s1 = keys[j];
                Map.Entry<Integer, Integer> e = new AbstractMap.SimpleImmutableEntry<>(s1, s2);
                if (!edges.contains(e)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Generate simple regular graph
     */
    private void generateSimpleRegularGraph(Graph<V, E> target)
    {
        // integers to vertices
        List<V> vertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            vertices.add(target.addVertex());
        }

        // set of final edges to add to target graph
        Set<Map.Entry<Integer, Integer>> edges = CollectionUtil.newHashSetWithExpectedSize(n * d);
        do {
            List<Integer> stubs = new ArrayList<>(n * d);
            for (int i = 0; i < n * d; i++) {
                stubs.add(i % n);
            }

            while (!stubs.isEmpty()) {
                Map<Integer, Integer> potentialEdges = new HashMap<>();
                Collections.shuffle(stubs, rng);

                for (int i = 0; i < stubs.size() - 1; i += 2) {
                    int s1 = stubs.get(i);
                    int s2 = stubs.get(i + 1);
                    // s1 < s2 has to be true
                    if (s1 > s2) {
                        int temp = s1;
                        s1 = s2;
                        s2 = temp;
                    }

                    Map.Entry<Integer, Integer> edge =
                        new AbstractMap.SimpleImmutableEntry<>(s1, s2);
                    if (s1 != s2 && !edges.contains(edge)) {
                        edges.add(edge);
                    } else {
                        potentialEdges.put(s1, potentialEdges.getOrDefault(s1, 0) + 1);
                        potentialEdges.put(s2, potentialEdges.getOrDefault(s2, 0) + 1);
                    }
                }

                if (!suitable(edges, potentialEdges)) {
                    edges.clear();
                    break;
                }

                stubs.clear();
                for (Map.Entry<Integer, Integer> e : potentialEdges.entrySet()) {
                    int node = e.getKey();
                    int potential = e.getValue();
                    for (int i = 0; i < potential; i++) {
                        stubs.add(node);
                    }
                }
            }

        } while (edges.isEmpty());

        // add edges to target
        for (Map.Entry<Integer, Integer> e : edges) {
            target.addEdge(vertices.get(e.getKey()), vertices.get(e.getValue()));
        }
    }

    /*
     * Generate non-simple regular graph.
     */
    private void generateNonSimpleRegularGraph(Graph<V, E> target)
    {
        List<V> vertices = new ArrayList<>(n * d);
        for (int i = 0; i < n; i++) {
            V vertex = target.addVertex();
            for (int j = 0; j < d; j++) {
                vertices.add(vertex);
            }
        }

        Collections.shuffle(vertices, rng);
        for (int i = 0; i < (n * d) / 2; i++) {
            V u = vertices.get(2 * i);
            V v = vertices.get(2 * i + 1);
            target.addEdge(u, v);
        }
    }

}
