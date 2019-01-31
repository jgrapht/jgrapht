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
package org.jgrapht.alg.lca;

import org.jgrapht.*;
import org.jgrapht.alg.decomposition.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * Algorithm for computing lowest common ancestors in rooted trees and forests based on
 * {@link HeavyPathDecomposition}.
 *
 * <p>
 * Preprocessing Time complexity: $O(|V|)$<br>
 * Preprocessing Space complexity: $O(|V|)$<br>
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
public class HeavyPathLCAFinder<V, E>
    implements
    LowestCommonAncestorAlgorithm<V>
{

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private int[] parent;
    private int[] depth;
    private int[] path;
    private int[] positionInPath;
    private int[] component;
    private int[] firstNodeInPath;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public HeavyPathLCAFinder(Graph<V, E> graph, V root)
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
    public HeavyPathLCAFinder(Graph<V, E> graph, Set<V> roots)
    {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeHeavyPathDecomposition();
    }

    /**
     * Compute the heavy path decomposition and get the corresponding arrays from the internal
     * state.
     */
    private void computeHeavyPathDecomposition()
    {
        HeavyPathDecomposition<V, E> heavyPath = new HeavyPathDecomposition<>(graph, roots);
        HeavyPathDecomposition<V, E>.InternalState state = heavyPath.getInternalState();

        vertexMap = state.getVertexMap();
        indexList = state.getIndexList();

        parent = state.getParentArray();
        depth = state.getDepthArray();
        component = state.getComponentArray();
        firstNodeInPath = state.getFirstNodeInPathArray();
        path = state.getPathArray();
        positionInPath = state.getPositionInPathArray();
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

        int componentA = component[indexA];
        int componentB = component[indexB];

        // If a and b are in different components (or haven't been explored yet) then they do not
        // have a lca
        if (componentA != componentB || componentA == -1)
            return null;

        /*
         * Idea: Get a and b on the same vertex path by 'jumping' from one path to another
         *
         * while (a and b are on different paths) do if a's path starts lower than b's path (in the
         * tree) set a := father of the first node in a's path else set b: = father of the first
         * node in b's path
         *
         * now a and b are on the same path
         *
         * return a if a is closer to the root than b; otherwise return b
         */

        int pathA = path[indexA];
        int pathB = path[indexB];

        while (pathA != pathB) {
            int firstNodePathA = firstNodeInPath[pathA];
            int firstNodePathB = firstNodeInPath[pathB];

            if (depth[firstNodePathA] < depth[firstNodePathB]) {
                indexB = parent[firstNodePathB];
                pathB = path[indexB];
            } else {
                indexA = parent[firstNodePathA];
                pathA = path[indexA];
            }
        }

        return positionInPath[indexA] < positionInPath[indexB] ? indexList.get(indexA)
            : indexList.get(indexB);
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
