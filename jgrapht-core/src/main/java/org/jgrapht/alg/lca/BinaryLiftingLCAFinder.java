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
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

/**
 * Algorithm for computing lowest common ancestors in forests using the binary lifting method.
 *
 * Preprocessing Time complexity: $O(|V| log(|V|))
 * Preprocessing Memory complexity:  $O(|V| log(|V|))
 * Query complexity: $O(log(|V|))$
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BinaryLiftingLCAFinder<V, E> implements LCAAlgorithm<V> {

    private final Graph<V, E> graph;
    private final Set<V> roots;
    private final int MAX_LEVEL;

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
     * @param graph the input graph
     * @param root the root of the graph
     */
    public BinaryLiftingLCAFinder(Graph<V, E> graph, V root){
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
    public BinaryLiftingLCAFinder(Graph<V, E> graph, Set<V> roots){
//  TODO      assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");
        this.MAX_LEVEL = log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("At least one root is not a valid vertex");
    }

    private void dfs(int u, int parent){
        component[u] = numberComponent;
        timeIn[u] = ++clock;

        ancestors[0][u] = parent;
        for (int l = 1; l < MAX_LEVEL; l++) {
            if (ancestors[l - 1][u] != -1)
                ancestors[l][u] = ancestors[l - 1][ancestors[l - 1][u]];
        }

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int v = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (v != parent){
                dfs(v, u);
            }
        }

        timeOut[u] = ++clock;
    }

    private boolean isAncestor(int ancestor, int descendant) {
        return timeIn[ancestor] <= timeIn[descendant] && timeOut[descendant] <= timeOut[ancestor];
    }

    @Override
    public V getLCA(V a, V b) {
        if (a.equals(b))
            return a;

        computeAncestorMatrix();

        int x = vertexMap.get(a);
        int y = vertexMap.get(b);

        // if x or y hasn't been explored or they are not in the same tree
        if (component[x] != component[y] || component[x] == 0)
            return null;

        if (isAncestor(x, y))
            return a;

        if (isAncestor(y, x))
            return b;

        for (int l = MAX_LEVEL - 1; l >= 0; l--)
            if (ancestors[l][x] != -1 && !isAncestor(ancestors[l][x], y))
                x = ancestors[l][x];

        int lca = ancestors[0][x];

        // if lca is null
        if (lca == -1)
            return null;
        else
            return indexList.get(lca);
    }

    private static int log2(int n){
        int result = 1;

        while ((1 << result) <= n)
            ++result;

        return result;
    }

    private void normalizeGraph(){
        /*
         * Normalize the graph map each vertex to an integer (using a HashMap) keep the reverse
         * mapping (using an ArrayList)
         */
        vertexMap = new HashMap<>(graph.vertexSet().size());
        indexList = new ArrayList<>(graph.vertexSet().size());

        for (V v : graph.vertexSet()) {
            if (!vertexMap.containsKey(v)) {
                vertexMap.put(v, vertexMap.size());
                indexList.add(v);
            }
        }
    }

    private void computeAncestorMatrix(){
        if (ancestors != null)
            return;

        ancestors = new int[MAX_LEVEL][graph.vertexSet().size()];

        for (int l = 0; l < MAX_LEVEL; l++) {
            Arrays.fill(ancestors[l], -1);
        }

        timeIn = new int[graph.vertexSet().size()];
        timeOut = new int[graph.vertexSet().size()];

        numberComponent = 0;
        component = new int[graph.vertexSet().size()];

        normalizeGraph();

        for (V root: roots)
            if (component[vertexMap.get(root)] == 0) {
                numberComponent++;
                dfs(vertexMap.get(root), -1);
            }
    }
}
