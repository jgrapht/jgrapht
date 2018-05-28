/*
 * (C) Copyright 2016-2018, by Leo Crawford and Contributors.
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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.UnionFind;

import java.util.*;

/**
 * Used to calculate Tarjan's Lowest Common Ancestors Algorithm
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 * @author Leo Crawford
 */
public class TarjanLCAFinder<V, E> implements LCAAlgorithm<V>
{
    private Graph<V, E> graph;
    private V root;

    /**
     * Create an instance with a reference to the graph that we will find LCAs for
     * 
     * @param graph the input graph
     * @param root the root of the graph
     */
    public TarjanLCAFinder(Graph<V, E> graph, V root) {
        assert GraphTests.isForest(graph);
        this.graph = graph;
        this.root = root;

        if (!graph.containsVertex(root)){
            throw new IllegalArgumentException("root not contained in graph");
        }
    }

    @Override
    public V getLCA(V a, V b) {
        List<Pair<V, V>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(a, b));

        return getLCAs(pairs).get(0);
    }

    @Override
    public List<V> getLCAs(List<Pair<V, V>> queries) {
        return computeTarjan(queries);
    }

    private UnionFind<V> unionFind;

    private Map<V, V> ancestors;

    private Set<V> blackNodes;

    private MultiMap<V> queryOccurs;
    private List<V> lowestCommonAncestors;

    private List<Pair<V, V>> queries;

    private void initialize(){
        unionFind = new UnionFind<>(Collections.emptySet());
        ancestors = new HashMap<>();
        blackNodes = new HashSet<>();
        queryOccurs = new MultiMap<>();
    }

    private void clear(){
        unionFind = null;
        ancestors = null;
        blackNodes = null;
        queryOccurs = null;

        queries = null;
        lowestCommonAncestors = null;
    }

    private List<V> computeTarjan(List<Pair<V, V>> queries){
        initialize();

        this.queries = queries;
        this.lowestCommonAncestors = new ArrayList<>(queries.size());

        for (int i = 0; i < queries.size(); i++){
            V a = this.queries.get(i).getFirst();
            V b = this.queries.get(i).getSecond();

            queryOccurs.addToSet(a, i);
            queryOccurs.addToSet(b, i);

            this.lowestCommonAncestors.add(null);
        }

        TarjanOLCA(root, null);

        List<V> tmpRef = lowestCommonAncestors;
        clear();

        return tmpRef;
    }

    private void TarjanOLCA(V u, V p){
        unionFind.addElement(u);
        ancestors.put(u, u);

        for (E edge: graph.edgesOf(u)){
            V v = Graphs.getOppositeVertex(graph, edge, u);

            if (!v.equals(p)){
                TarjanOLCA(v, u);
                unionFind.union(u, v);
                ancestors.put(unionFind.find(u), u);
            }
        }

        blackNodes.add(u);

        for (int index: queryOccurs.getOrCreate(u)){
            Pair<V, V> query = queries.get(index);
            V v;

            if (query.getFirst().equals(u))
                v = query.getSecond();
            else
                v = query.getFirst();

            if (blackNodes.contains(v)){
                lowestCommonAncestors.set(index, ancestors.get(unionFind.find(v)));
            }
        }
    }

    private static final class MultiMap<V> extends HashMap<V, Set<Integer>> {

        void addToSet(V key, Integer n){
            getOrCreate(key).add(n);
        }

        Set<Integer> getOrCreate(V key) {
            if (!containsKey(key)) {
                put(key, new HashSet<>());
            }

            return get(key);
        }
    }
}
