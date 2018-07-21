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
 * Algorithm for computing lowest common ancestors in rooted trees and forests using the Euler tour approached
 * introduced in <i>Berkman, Omer; Vishkin, Uzi (1993), "Recursive Star-Tree Parallel Data Structure",
 * SIAM Journal on Computing, 22 (2): 221–242, doi:10.1137/0222017</i>
 *
 * Preprocessing Time complexity: $O(|V| log(|V|))
 * Preprocessing Memory complexity:  $O(|V| log(|V|))
 * Query complexity: $O(1)$
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class EulerTourRMQLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private final int MAX_LEVEL;

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
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public EulerTourRMQLCAFinder(Graph<V, E> graph, V root){
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
    public EulerTourRMQLCAFinder(Graph<V, E> graph, Set<V> roots){
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");
        this.MAX_LEVEL = 1 + log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");

        computeAncestorsStructure();
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

    private void dfs(int u, int parent, int lvl) {
        component[u] = numberComponent;
        eulerTour[sizeTour] = u;
        level[sizeTour] = lvl;
        sizeTour++;

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int v = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (v != parent){
                dfs(v, u, lvl + 1);

                eulerTour[sizeTour] = u;
                level[sizeTour] = lvl;
                sizeTour++;
            }
        }
    }

    private void computeRMQ(){
        rmq = new int[MAX_LEVEL + 1][sizeTour];
        log2 = new int[sizeTour + 1];

        for (int i = 0; i < sizeTour; i++)
            rmq[0][i] = i;

        for (int i = 1; (1 << i) <= sizeTour; i++)
            for (int j = 0; j + ( 1 << i ) - 1 < sizeTour; j++) {
                int p = 1 << (i - 1);

                if (level[rmq[i - 1][j]] < level[rmq[i - 1][j + p]]){
                    rmq[i][j] = rmq[i - 1][j];
                }
                else{
                    rmq[i][j] = rmq[i - 1][j + p];
                }
            }

        for (int i = 2; i <= sizeTour; ++i)
            log2[i] = log2[i / 2] + 1;
    }

    private void computeAncestorsStructure(){
        normalizeGraph();

        eulerTour = new int[2 * graph.vertexSet().size()];
        level = new int[2 * graph.vertexSet().size()];
        representative = new int[graph.vertexSet().size()];

        numberComponent = 0;
        component = new int[graph.vertexSet().size()];

        for (V root: roots){
            int u = vertexMap.get(root);

            if (component[u] == 0) {
                numberComponent++;
                dfs(u, -1, 0);
            }
            else{
                throw new IllegalArgumentException("multiple roots in the same tree");
            }
        }

        Arrays.fill(representative, -1);
        for (int i = 0; i < sizeTour; i++){
            if (representative[eulerTour[i]] == -1){
                representative[eulerTour[i]] = i;
            }
        }

        computeRMQ();
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

        if(level[sol] > level[rmq[l][indexB - pwl + 1]])
            sol = rmq[l][indexB - pwl + 1];

        return indexList.get(eulerTour[sol]);
    }
}
