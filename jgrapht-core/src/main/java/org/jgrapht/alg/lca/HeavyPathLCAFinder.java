/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.decomposition.HeavyPathDecomposition;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

/**
 * Algorithm for computing lowest common ancestors in rooted trees and forests based on {@link HeavyPathDecomposition}.
 *
 * Preprocessing Time complexity: $O(|V|)$
 * Preprocessing Memory complexity: $O(|V|)$
 * Query complexity: $O(log(|V|))$
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class HeavyPathLCAFinder<V, E> implements LCAAlgorithm<V> {

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
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public HeavyPathLCAFinder(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Construct a new instance of the algorithm.
     *
     * Note: If two roots appear in the same tree, an error will be thrown.
     * Note: The constructor will NOT check if the input graph is a valid forest.
     *
     * @param graph the input graph
     * @param roots the set of roots of the graph
     */
    public HeavyPathLCAFinder(Graph<V, E> graph, Set<V> roots){
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeHeavyPathDecomposition();
    }

    /**
     * Compute the heavy path decomposition and get the corresponding arrays from the internal state.
     */
    private void computeHeavyPathDecomposition(){
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
    public V getLCA(V a, V b) {
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

        // If a and b are in different components (or haven't been explored yet) then they do not have a lca
        if (componentA != componentB || componentA == -1)
            return null;

        /*
         * Idea: Get a anb b on the same vertex path by 'jumping' from one path to another
         *
         *       while (a and b are on different paths) do
         *          if a's path starts lower than b's path (in the tree)
         *              set a := father of the first node in a's path
         *          else
         *              set b: = father of the first node in b's path
         *
         *       now a and b are on the same path
         *
         *       return a if a is closer to the root than b; otherwise return b
         */

        int pathA = path[indexA];
        int pathB = path[indexB];

        while (pathA != pathB){
            int firstNodePathA = firstNodeInPath[pathA];
            int firstNodePathB = firstNodeInPath[pathB];
            
            if (depth[firstNodePathA] < depth[firstNodePathB]) {
                indexB = parent[firstNodePathB];
                pathB = path[indexB];
            }
            else {
                indexA = parent[firstNodePathA];
                pathA = path[indexA];
            }
        }

        return positionInPath[indexA] < positionInPath[indexB] ? indexList.get(indexA) : indexList.get(indexB);
    }
}
