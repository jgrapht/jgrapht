/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
 * Generates a random tree using Prüfer sequences.
 * 
 * <p>
 * A Prüfer sequence of length $n$ is randomly generated and converted into the corresponding tree.
 * </p>
 *
 * <p>
 * This implementation is inspired by "X. Wang, L. Wang and Y. Wu, "An Optimal Algorithm for Prufer
 * Codes," Journal of Software Engineering and Applications, Vol. 2 No. 2, 2009, pp. 111-115. doi:
 * 10.4236/jsea.2009.22016." and has a running time of $O(n)$.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class PruferTreeGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{

    // number of vertices
    private final int n;

    // random number generator
    private final Random rng;

    // input Prufer sequence
    private final int[] inputPruferSeq;

    /**
     * Construct a new PruferTreeGenerator from an input Prüfer sequence. Note that the size of the
     * generated tree will be $l+2$ where $l$ is the length of the input sequence. The Prüfer
     * sequence must contain integers between $0$ and $l+1$ (inclusive).
     *
     * Note: In this case, the same tree will be generated every time.
     *
     * @param pruferSequence the input Prüfer sequence
     * @throws IllegalArgumentException if {@code n} is &le; 0
     * @throws IllegalArgumentException if {@code pruferSequence} is {@code null}
     * @throws IllegalArgumentException if {@code pruferSequence} is invalid.
     */
    public PruferTreeGenerator(int[] pruferSequence)
    {
        if (Objects.isNull(pruferSequence)) {
            throw new IllegalArgumentException("pruferSequence cannot be null");
        }

        this.n = pruferSequence.length + 2;
        this.rng = null;
        this.inputPruferSeq = pruferSequence.clone();

        if (n <= 0) {
            throw new IllegalArgumentException("n must be greater than 0");
        }

        for (int i = 0; i < n - 2; i++) {
            if (pruferSequence[i] < 0 || pruferSequence[i] >= n) {
                throw new IllegalArgumentException("invalid pruferSequence");
            }
        }
    }

    /**
     * Construct a new PruferTreeGenerator.
     *
     * @param n number of vertices to be generated
     * @throws IllegalArgumentException if {@code n} is &le; 0
     */
    public PruferTreeGenerator(int n)
    {
        this(n, new Random());
    }

    /**
     * Construct a new PruferTreeGenerator.
     *
     * @param n number of vertices to be generated
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException if {@code n} is &le; 0
     */
    public PruferTreeGenerator(int n, long seed)
    {
        this(n, new Random(seed));
    }

    /**
     * Construct a new PruferTreeGenerator
     *
     * @param n number of vertices to be generated
     * @param rng the random number generator to use
     * @throws IllegalArgumentException if {@code n} is &le; 0
     * @throws NullPointerException if {@code rng} is {@code null}
     */
    public PruferTreeGenerator(int n, Random rng)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be greater than 0");
        }

        this.n = n;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
        this.inputPruferSeq = null;
    }

    /**
     * Generates a tree.
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

        List<V> vertexList = new ArrayList<>(n);

        // add vertices
        for (int i = 0; i < n; i++) {
            vertexList.add(target.addVertex());
        }

        // base case
        if (n == 1) {
            return;
        }

        // degree stores the remaining degree (plus one) for each node. The
        // degree of a node in the decoded tree is one more than the number
        // of times it appears in the code.
        int[] degree = new int[n];
        Arrays.fill(degree, 1);

        int[] pruferSeq;

        if (inputPruferSeq == null) {
            pruferSeq = new int[n - 2];

            for (int i = 0; i < n - 2; i++) {
                pruferSeq[i] = rng.nextInt(n);
                ++degree[pruferSeq[i]];
            }
        } else {
            pruferSeq = inputPruferSeq;
        }

        int index = -1;
        for (int k = 0; k < n; k++) {
            if (degree[k] == 1) {
                index = k;
                break;
            }
        }

        assert index != -1;
        int x = index;

        // set of nodes without a parent
        Set<V> orphans = new HashSet<>(target.vertexSet());

        for (int i = 0; i < n - 2; i++) {
            int y = pruferSeq[i];
            orphans.remove(vertexList.get(x));
            target.addEdge(vertexList.get(x), vertexList.get(y));
            --degree[y];

            if (y < index && degree[y] == 1) {
                x = y;
            } else {
                for (int k = index + 1; k < n; k++) {
                    if (degree[k] == 1) {
                        index = x = k;
                        break;
                    }
                }
            }
        }

        assert orphans.size() == 2;
        Iterator<V> iterator = orphans.iterator();
        V u = iterator.next();
        V v = iterator.next();
        target.addEdge(u, v);
    }
}
