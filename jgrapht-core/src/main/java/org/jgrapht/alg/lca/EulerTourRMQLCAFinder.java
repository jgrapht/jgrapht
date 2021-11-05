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
package org.jgrapht.alg.lca;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Algorithm for computing lowest common ancestors in rooted trees and forests based on <i>Berkman,
 * Omer; Vishkin, Uzi (1993), "Recursive Star-Tree Parallel Data Structure", SIAM Journal on
 * Computing, 22 (2): 221–242, doi:10.1137/0222017</i>.
 *
 * <p>
 * The algorithm involves forming an Euler tour of a graph formed from the input tree by doubling
 * every edge, and using this tour to compute a sequence of level numbers of the nodes in the order
 * the tour visits them. A lowest common ancestor query can then be transformed into a query that
 * seeks the minimum value occurring within some subinterval of this sequence of numbers.
 * </p>
 *
 * <p>
 * Preprocessing Time complexity: $O(|V| log(|V|))$<br>
 * Preprocessing Space complexity: $O(|V| log(|V|))$<br>
 * Query Time complexity: $O(1)$<br>
 * Query Space complexity: $O(1)$<br>
 * </p>
 *
 * <p>
 * For small (i.e. less than 100 vertices) trees or forests, all implementations behave similarly.
 * For larger trees/forests with less than 50,000 queries you can use either
 * {@link BinaryLiftingLCAFinder}, {@link HeavyPathLCAFinder} or {@link EulerTourRMQLCAFinder}. Fo
 * more than that use {@link EulerTourRMQLCAFinder} since it provides $O(1)$ per query.<br>
 * Space-wise, {@link HeavyPathLCAFinder} and {@link TarjanLCAFinder} only use a linear amount while
 * {@link BinaryLiftingLCAFinder} and {@link EulerTourRMQLCAFinder} require linearithmic space.<br>
 * For DAGs, use {@link NaiveLCAFinder}.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class EulerTourRMQLCAFinder<V, E>
    implements
    LowestCommonAncestorAlgorithm<V>
{
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private final int maxLevel;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int[] eulerTour;
    private int sizeTour;

    private int numberComponent;
    private int[] component;

    private int[] level;
    private int[] representative;

    private int[][] rmq;
    private int[] log2;

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public EulerTourRMQLCAFinder(Graph<V, E> graph, V root)
    {
        this(graph, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: If two roots appear in the same tree, an error will be thrown.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid forest.
     *
     * @param graph the input graph
     * @param roots the set of roots of the graph
     */
    public EulerTourRMQLCAFinder(Graph<V, E> graph, Set<V> roots)
    {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");
        this.maxLevel = 1 + org.jgrapht.util.MathUtil.log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeAncestorsStructure();
    }

    private void normalizeGraph()
    {
        VertexToIntegerMapping<V> vertexToIntegerMapping = Graphs.getVertexToIntegerMapping(graph);
        vertexMap = vertexToIntegerMapping.getVertexMap();
        indexList = vertexToIntegerMapping.getIndexList();
    }

    private void dfsIterative(int u, int startLevel)
    {
        // set of vertices for which the part of the if has been performed
        // (in other words: u ∈ explored iff dfs(u, ...) has been called as some point)
        Set<Integer> explored = new HashSet<>();

        ArrayDeque<Pair<Integer, Integer>> stack = new ArrayDeque<>();
        stack.push(Pair.of(u, startLevel));

        while (!stack.isEmpty()) {
            Pair<Integer, Integer> pair = stack.poll();
            u = pair.getFirst();
            int lvl = pair.getSecond();

            if (!explored.contains(u)) {
                explored.add(u);

                component[u] = numberComponent;
                eulerTour[sizeTour] = u;
                level[sizeTour] = lvl;
                sizeTour++;

                V vertexU = indexList.get(u);
                for (E edge : graph.outgoingEdgesOf(vertexU)) {
                    int child = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    // check if child has not been explored (i.e. dfs(child, ...) has not been
                    // called)
                    if (!explored.contains(child)) {
                        // simulate the return from recursion
                        stack.push(pair);
                        stack.push(Pair.of(child, lvl + 1));
                    }
                }
            } else {
                eulerTour[sizeTour] = u;
                level[sizeTour] = lvl;
                sizeTour++;
            }
        }
    }

    private void computeRMQ()
    {
        rmq = new int[maxLevel + 1][sizeTour];
        log2 = new int[sizeTour + 1];

        for (int i = 0; i < sizeTour; i++) {
            rmq[0][i] = i;
        }

        for (int i = 1; (1 << i) <= sizeTour; i++) {
            for (int j = 0; j + (1 << i) - 1 < sizeTour; j++) {
                int p = 1 << (i - 1);

                if (level[rmq[i - 1][j]] < level[rmq[i - 1][j + p]]) {
                    rmq[i][j] = rmq[i - 1][j];
                } else {
                    rmq[i][j] = rmq[i - 1][j + p];
                }
            }
        }

        for (int i = 2; i <= sizeTour; ++i) {
            log2[i] = log2[i / 2] + 1;
        }
    }

    private void computeAncestorsStructure()
    {
        normalizeGraph();

        eulerTour = new int[2 * graph.vertexSet().size()];
        level = new int[2 * graph.vertexSet().size()];
        representative = new int[graph.vertexSet().size()];

        numberComponent = 0;
        component = new int[graph.vertexSet().size()];

        for (V root : roots) {
            int u = vertexMap.get(root);

            if (component[u] == 0) {
                numberComponent++;
                dfsIterative(u, -1);
            } else {
                throw new IllegalArgumentException("multiple roots in the same tree");
            }
        }

        Arrays.fill(representative, -1);
        for (int i = 0; i < sizeTour; i++) {
            if (representative[eulerTour[i]] == -1) {
                representative[eulerTour[i]] = i;
            }
        }

        computeRMQ();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getLCA(V a, V b)
    {
        int indexA = vertexMap.getOrDefault(a, -1);
        if (indexA == -1)
            throw new IllegalArgumentException("invalid vertex: " + a);

        int indexB = vertexMap.getOrDefault(b, -1);
        if (indexB == -1)
            throw new IllegalArgumentException("invalid vertex: " + b);

        // Check if a == b because lca(a, a) == a
        if (a.equals(b))
            return a;

        // If a and b are in different components then they do not have a lca
        if (component[indexA] != component[indexB] || component[indexA] == 0)
            return null;

        indexA = representative[indexA];
        indexB = representative[indexB];

        if (indexA > indexB) {
            int t = indexA;
            indexA = indexB;
            indexB = t;
        }

        int l = log2[indexB - indexA + 1];
        int pwl = 1 << l;
        int sol = rmq[l][indexA];

        if (level[sol] > level[rmq[l][indexB - pwl + 1]])
            sol = rmq[l][indexB - pwl + 1];

        return indexList.get(eulerTour[sol]);
    }

    /**
     * Note: This operation is not supported.<br>
     *
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if the method is called
     */
    @Override
    public Set<V> getLCASet(V a, V b)
    {
        throw new UnsupportedOperationException();
    }
}
