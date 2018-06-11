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
import org.jgrapht.GraphTests;
import org.jgrapht.alg.decomposition.HeavyPathDecomposition;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

/**
 * Algorithm for computing lowest common ancestors in forests based on heavy-path decomposition.
 *
 * Preprocessing Time complexity: $O(|V|)$
 * Preprocessing Memory complexity:  $O(|V|)$
 * Query complexity: $O(log(|V|))$
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @see HeavyPathDecomposition
 */
public class HeavyPathLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private HeavyPathDecomposition<V, E> heavyPath;

    /**
     * Construct a new instance of the algorithm.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public HeavyPathLCAFinder(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "Root cannot be null")));
    }

    /**
     * Construct a new instance of the algorithm.
     *
     * Note: If two roots are in the same connected component, then either one can be used by the algorithm.
     *
     * @param graph the input graph
     * @param roots the set of roots of the graph
     */
    public HeavyPathLCAFinder(Graph<V, E> graph, Set<V> roots){
//    TODO:    assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("At least one root is not a valid vertex");
    }

    private Map<V, V> father;
    private Map<V, Integer> depth;

    private Map<V, Integer> path;
    private Map<V, Integer> positionInPath;
    private Map<V, Integer> component;

    private V[] firstNode;

    @SuppressWarnings("unchecked")
    private void computeHeavyPathDecomposition(){
        if (heavyPath != null)
            return;

        heavyPath = new HeavyPathDecomposition<>(graph, roots);

        father = heavyPath.getFather();
        depth = heavyPath.getDepth();
        component = heavyPath.getComponent();

        path = new HashMap<>();
        positionInPath = new HashMap<>();

        List<List<V>> paths = heavyPath.getPaths();
        firstNode = (V[]) new Object[paths.size()];

        for (int i = 0; i < paths.size(); i++){
            List<V> p = paths.get(i);
            firstNode[i] = p.get(0);

            for (int j = 0; j < p.size(); j++) {
                positionInPath.put(p.get(j), j);
                path.put(p.get(j), i);
            }
        }
    }

    @Override
    public V getLCA(V a, V b) {
        if (a.equals(b))
            return a;

        computeHeavyPathDecomposition();

        int componentA = component.get(a);
        int componentB = component.get(b);

        if (componentA != componentB || componentA == 0)
            return null;

        int pathA = path.get(a);
        int pathB = path.get(b);

        while (pathA != pathB){
            V firstNodePathA = firstNode[pathA];
            V firstNodePathB = firstNode[pathB];
            
            if (depth.get(firstNodePathA) < depth.get(firstNodePathB)) {
                b = father.get(firstNodePathB);
                pathB = path.get(b);
            }
            else {
                a = father.get(firstNodePathA);
                pathA = path.get(a);
            }
        }

        return positionInPath.get(a) < positionInPath.get(b) ? a : b;
    }
}
