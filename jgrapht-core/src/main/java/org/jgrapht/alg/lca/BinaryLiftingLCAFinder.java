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
import org.jgrapht.util.*;

import java.util.*;

import static org.jgrapht.util.MathUtil.log2;

/**
 * Algorithm for computing lowest common ancestors in rooted trees and forests using the binary
 * lifting method.
 *
 * <p>
 * The method appears in <i>Bender, Michael A., and Martın Farach-Colton. "The level ancestor
 * problem simplified." Theoretical Computer Science 321.1 (2004): 5-12</i> and it is also nicely
 * presented in the following article on <a href=
 * "https://www.topcoder.com/community/data-science/data-science-tutorials/range-minimum-query-and-lowest-common-ancestor/#Another%20easy%20solution%20in%20O(N%20logN,%20O(logN)">Topcoder</a>
 * for more details about the algorithm.
 * </p>
 *
 * <p>
 * Algorithm idea:<br>
 * We improve on the naive approach by using jump pointers. These are pointers at a node which
 * reference one of the node’s ancestors. Each node stores jump pointers to ancestors at levels 1,
 * 2, 4, . . . , 2^k. <br>
 * Queries are answered by repeatedly jumping from node to node, each time jumping more than half of
 * the remaining levels between the current ancestor and the goal ancestor (i.e. the lca). The
 * worst-case number of jumps is $O(log(|V|))$.
 * </p>
 *
 *
 * <p>
 * Preprocessing Time complexity: $O(|V| log(|V|))$<br>
 * Preprocessing Space complexity: $O(|V| log(|V|))$<br>
 * Query Time complexity: $O(log(|V|))$<br>
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
public class BinaryLiftingLCAFinder<V, E>
    implements
    LowestCommonAncestorAlgorithm<V>
{

    private final Graph<V, E> graph;
    private final Set<V> roots;
    private final int maxLevel;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    // ancestors[u][i] = the 2^i ancestor of u (e.g ancestors[u][0] = father(u))
    private int[][] ancestors;

    private int[] timeIn, timeOut;
    private int clock = 0;

    private int numberComponent;
    private int[] component;

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public BinaryLiftingLCAFinder(Graph<V, E> graph, V root)
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
    public BinaryLiftingLCAFinder(Graph<V, E> graph, Set<V> roots)
    {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");
        this.maxLevel = log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeAncestorMatrix();
    }

    private void normalizeGraph()
    {
        VertexToIntegerMapping<V> vertexToIntegerMapping = Graphs.getVertexToIntegerMapping(graph);
        vertexMap = vertexToIntegerMapping.getVertexMap();
        indexList = vertexToIntegerMapping.getIndexList();
    }

    private void dfs(int u, int parent)
    {
        component[u] = numberComponent;
        timeIn[u] = ++clock;

        ancestors[0][u] = parent;
        for (int l = 1; l < maxLevel; l++) {
            if (ancestors[l - 1][u] != -1)
                ancestors[l][u] = ancestors[l - 1][ancestors[l - 1][u]];
        }

        V vertexU = indexList.get(u);
        for (E edge : graph.outgoingEdgesOf(vertexU)) {
            int v = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (v != parent) {
                dfs(v, u);
            }
        }

        timeOut[u] = ++clock;
    }

    private void computeAncestorMatrix()
    {
        ancestors = new int[maxLevel + 1][graph.vertexSet().size()];

        for (int l = 0; l < maxLevel; l++) {
            Arrays.fill(ancestors[l], -1);
        }

        timeIn = new int[graph.vertexSet().size()];
        timeOut = new int[graph.vertexSet().size()];

        // Ensure that isAncestor(x, y) == false if either x and y hasn't been explored yet
        for (int i = 0; i < graph.vertexSet().size(); i++) {
            timeIn[i] = timeOut[i] = -(i + 1);
        }

        numberComponent = 0;
        component = new int[graph.vertexSet().size()];

        normalizeGraph();

        for (V root : roots) {
            if (component[vertexMap.get(root)] == 0) {
                numberComponent++;
                dfs(vertexMap.get(root), -1);
            } else {
                throw new IllegalArgumentException("multiple roots in the same tree");
            }
        }
    }

    private boolean isAncestor(int ancestor, int descendant)
    {
        return timeIn[ancestor] <= timeIn[descendant] && timeOut[descendant] <= timeOut[ancestor];
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

        // if a and b are in different components then they do not have a lca
        if (component[indexA] != component[indexB] || component[indexA] == 0)
            return null;

        if (isAncestor(indexA, indexB))
            return a;

        if (isAncestor(indexB, indexA))
            return b;

        for (int l = maxLevel - 1; l >= 0; l--)
            if (ancestors[l][indexA] != -1 && !isAncestor(ancestors[l][indexA], indexB))
                indexA = ancestors[l][indexA];

        int lca = ancestors[0][indexA];

        // if lca is null
        if (lca == -1)
            return null;
        else
            return indexList.get(lca);
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
