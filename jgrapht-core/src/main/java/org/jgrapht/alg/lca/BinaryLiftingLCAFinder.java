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
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

import static org.jgrapht.util.MathUtil.log2;

/**
 * Algorithm for computing lowest common ancestors in rooted trees and forests using the binary lifting method.
 *
 * Preprocessing Time complexity: $O(|V| log(|V|))
 * Preprocessing Memory complexity:  $O(|V| log(|V|))
 * Query complexity: $O(log(|V|))$
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
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
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public BinaryLiftingLCAFinder(Graph<V, E> graph, V root){
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
    public BinaryLiftingLCAFinder(Graph<V, E> graph, Set<V> roots){
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");
        this.MAX_LEVEL = log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeAncestorMatrix();
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

    private void computeAncestorMatrix(){
        ancestors = new int[MAX_LEVEL + 1][graph.vertexSet().size()];

        for (int l = 0; l < MAX_LEVEL; l++) {
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

        for (V root: roots)
            if (component[vertexMap.get(root)] == 0) {
                numberComponent++;
                dfs(vertexMap.get(root), -1);
            }
            else{
                throw new IllegalArgumentException("multiple roots in the same tree");
            }
    }

    private boolean isAncestor(int ancestor, int descendant) {
        return timeIn[ancestor] <= timeIn[descendant] && timeOut[descendant] <= timeOut[ancestor];
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

        // if a and b are in different components then they do not have a lca
        if (component[indexA] != component[indexB] || component[indexA] == 0)
            return null;

        if (isAncestor(indexA, indexB))
            return a;

        if (isAncestor(indexB, indexA))
            return b;

        for (int l = MAX_LEVEL - 1; l >= 0; l--)
            if (ancestors[l][indexA] != -1 && !isAncestor(ancestors[l][indexA], indexB))
                indexA = ancestors[l][indexA];

        int lca = ancestors[0][indexA];

        // if lca is null
        if (lca == -1)
            return null;
        else
            return indexList.get(lca);
    }
}
