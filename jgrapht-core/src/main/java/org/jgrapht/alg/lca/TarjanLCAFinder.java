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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.UnionFind;

import java.util.*;

/**
 * Tarjan's offline algorithm for computing lowest common ancestors in forests.
 *
 * <p>
 * See the article on
 * <a href="https://en.wikipedia.org/wiki/Tarjan%27s_off-line_lowest_common_ancestors_algorithm">wikipedia</a> for more
 * information on the algorithm.
 *
 * </p>
 *
 * Query complexity: $O(|V| log^{*}(|V|) + |Q|)$ where $|Q|$ is the number of queries
 * Memory complexity: $O(|V| + |Q|)$ where $|Q|$ is the number of queries
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class TarjanLCAFinder<V, E> implements LCAAlgorithm<V> {
    private Graph<V, E> graph;
    private Set<V> roots;

    private UnionFind<V> unionFind;

    private Map<V, V> ancestors;

    private Set<V> blackNodes;

    private HashMap<V, Set<Integer>> queryOccurs;
    private List<V> lowestCommonAncestors;

    private List<Pair<V, V>> queries;

    /**
     * Create an instance with a reference to the graph that we will find LCAs for
     * 
     * @param graph the input graph
     * @param root the root of the graph
     */
    public TarjanLCAFinder(Graph<V, E> graph, V root) {
        this(graph, Collections.singleton(Objects.requireNonNull(root, "Root cannot be null")));
    }

    /**
     * Create an instance with a reference to the graph that we will find LCAs for
     *
     * @param graph the input graph
     * @param roots the roots of the graph
     */
    public TarjanLCAFinder(Graph<V, E> graph, Set<V> roots) {
//  TODO:      assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("At least one root is not a valid vertex");
    }

    @Override
    public V getLCA(V a, V b) {
        return getLCAs(Collections.singletonList(Pair.of(a,b))).get(0);
    }

    @Override
    public List<V> getLCAs(List<Pair<V, V>> queries) {
        return computeTarjan(queries);
    }

    private void initialize(){
        unionFind = new UnionFind<>(Collections.emptySet());
        ancestors = new HashMap<>();
        blackNodes = new HashSet<>();
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

        this.queryOccurs = new HashMap<>();

        for (int i = 0; i < queries.size(); i++){
            V a = this.queries.get(i).getFirst();
            V b = this.queries.get(i).getSecond();

            if (a.equals(b))
                this.lowestCommonAncestors.add(a);
            else{
                queryOccurs.computeIfAbsent(a, x -> new HashSet<>()).add(i);
                queryOccurs.computeIfAbsent(b, x -> new HashSet<>()).add(i);

                this.lowestCommonAncestors.add(null);
            }

        }

        Set<V> visited = new HashSet<>();

        for (V root: roots)
            if (!visited.contains(root)) {
                blackNodes.clear();
                TarjanOLCA(root, null, visited);
            }

        List<V> tmpRef = lowestCommonAncestors;
        clear();

        return tmpRef;
    }

    private void TarjanOLCA(V u, V p, Set<V> visited){
        visited.add(u);
        unionFind.addElement(u);
        ancestors.put(u, u);

        for (E edge: graph.edgesOf(u)){
            V v = Graphs.getOppositeVertex(graph, edge, u);

            if (!v.equals(p)){
                TarjanOLCA(v, u, visited);
                unionFind.union(u, v);
                ancestors.put(unionFind.find(u), u);
            }
        }

        blackNodes.add(u);

        for (int index: queryOccurs.computeIfAbsent(u, x -> new HashSet<>())){
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
}