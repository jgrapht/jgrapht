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

public class OPTHeavyPathLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private HeavyPathDecomposition<V, E> heavyPath;

    /**
     * Construct a new instance of the algorithm.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public OPTHeavyPathLCAFinder(Graph<V, E> graph, V root){
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
    public OPTHeavyPathLCAFinder(Graph<V, E> graph, Set<V> roots){
//    TODO:    assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("At least one root is not a valid vertex");
    }

    private int[] father;
    private int[] depth;

    private int[] path;
    private int[] positionInPath;
    private int[] component;

    private int[] firstNode;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private void normalizeGraph(){
        vertexMap = heavyPath.getNormalizedGraph().getFirst();
        indexList = heavyPath.getNormalizedGraph().getSecond();
    }

    private void computeHeavyPathDecomposition(){
        if (heavyPath != null)
            return;

        heavyPath = new HeavyPathDecomposition<>(graph, roots);

        normalizeGraph();

        father = new int[graph.vertexSet().size()];
        depth = new int[graph.vertexSet().size()];
        component = new int[graph.vertexSet().size()];

        for (V v: graph.vertexSet()){
            int indexV = vertexMap.get(v);
            V u = heavyPath.getFather(v);

            if (u == null)
                father[indexV] = -1;
            else
                father[indexV] = vertexMap.get(u);

            depth[indexV] = heavyPath.getDepth(v);
            component[indexV] = heavyPath.getComponent(v);
        }

        path = new int[graph.vertexSet().size()];
        positionInPath = new int[graph.vertexSet().size()];

        List<List<V>> paths = heavyPath.getPaths();
        firstNode = new int[paths.size()];

        for (int i = 0; i < paths.size(); i++){
            List<V> p = paths.get(i);
            firstNode[i] = vertexMap.get(p.get(0));

            for (int j = 0; j < p.size(); j++) {
                int ind = vertexMap.get(p.get(j));

                path[ind] = i;
                positionInPath[ind] = j;
            }
        }
    }

    @Override
    public V getLCA(V a, V b) {
        if (a.equals(b))
            return a;

        computeHeavyPathDecomposition();

        int indexA = vertexMap.get(a);
        int indexB = vertexMap.get(b);

        int componentA = component[indexA];
        int componentB = component[indexB];

        if (componentA != componentB || componentA == 0)
            return null;

        int pathA = path[indexA];
        int pathB = path[indexB];

        while (pathA != pathB){
            int firstNodePathA = firstNode[pathA];
            int firstNodePathB = firstNode[pathB];
            
            if (depth[firstNodePathA] < depth[firstNodePathB]) {
                indexB = father[firstNodePathB];
                pathB = path[indexB];
            }
            else {
                indexA = father[firstNodePathA];
                pathA = path[indexA];
            }
        }

        return positionInPath[indexA] < positionInPath[indexB] ? indexList.get(indexA) : indexList.get(indexB);
    }
}
