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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.function.Supplier;

/**
 * This is an implementation of the AHU algorithm for detecting an isomorphism between two rooted forests.
 * Please see <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a complete
 * definition of the isomorphism problem for general graphs.
 *
 * <p>
 *     The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974.
 *     The Design and Analysis of Computer Algorithms (1st ed.). Addison-Wesley Longman Publishing Co., Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 *     This implementation runs in linear time (in the number of vertices of the input forests)
 *     while using a linear amount of memory.
 * </p>
 *
 * <p>
 *      For an implementation that supports both unrooted and rooted trees see {@link AHUTreeIsomorphismInspector}.
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHUForestIsomorphismInspector<V, E> {
    private final Graph<V, E> forest1;
    private final Graph<V, E> forest2;

    private Set<V> roots1;
    private Set<V> roots2;

    private boolean computed = false;
    private IsomorphicTreeMapping<V, E> isomorphicMapping = null;

    /**
     * Construct a new AHU rooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first rooted tree
     * @param root1 the root of the first tree
     * @param tree2 the second rooted tree
     * @param root2 the root of the second tree
     */
    public AHUForestIsomorphismInspector(Graph<V, E> tree1, V root1, Graph<V, E> tree2, V root2){
        this(tree1, Collections.singleton(Objects.requireNonNull(root1, "root cannot be null")),
                tree2, Collections.singleton(Objects.requireNonNull(root2, "root cannot be null")));
    }

    /**
     * Construct a new AHU rooted forest isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input forests are valid.
     *
     * @param forest1 the first rooted forest
     * @param roots1 the roots of the first forest
     * @param forest2 the second rooted forest
     * @param roots2 the roots of the second forest
     */
    public AHUForestIsomorphismInspector(Graph<V, E> forest1, Set<V> roots1, Graph<V, E> forest2, Set<V> roots2){
        this.forest1 = Objects.requireNonNull(forest1, "input forest cannot be null");
        this.forest2 = Objects.requireNonNull(forest2, "input forest cannot be null");

        this.roots1 = Objects.requireNonNull(roots1, "set of roots cannot be null");
        this.roots2 = Objects.requireNonNull(roots2, "set of roots cannot be null");

        if (roots1.isEmpty()){
            throw new IllegalArgumentException("root set cannot be empty");
        }

        if (roots2.isEmpty()){
            throw new IllegalArgumentException("root set cannot be empty");
        }

        if (!forest1.vertexSet().containsAll(roots1)){
            throw new IllegalArgumentException("root not contained in forest");
        }

        if (!forest2.vertexSet().containsAll(roots2)){
            throw new IllegalArgumentException("root not contained in forest");
        }
    }

    private V getFreshVertex(Graph<V, E> graph){
        Supplier<V> supplier = graph.getVertexSupplier();

        if (Objects.isNull(supplier))
            throw new IllegalArgumentException("vertex supplier cannot be null");

        while (true){
            V v = supplier.get();

            if (!graph.containsVertex(v))
                return v;
        }
    }

    public boolean isomorphismExists(){
        if (!computed){
            isomorphicMapping = getIsomorphism();
        }

        return isomorphicMapping != null;
    }

    private Graph<V, E> takeSubraph(Graph<V, E> graph, Set<V> vertices){
        Graph<V, E> subgraph = new SimpleGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);

        for (V v: vertices)
            subgraph.addVertex(v);

        for (V v: vertices){
            for (E edge: graph.edgesOf(v)){
                V u = Graphs.getOppositeVertex(graph, edge, v);

                if (vertices.contains(u))
                    subgraph.addEdge(u, v);
            }
        }

        return subgraph;
    }

    public IsomorphicTreeMapping<V, E> getIsomorphism(){
        if (computed)
            return isomorphicMapping;

        ConnectivityInspector<V, E> connectivityInspector1 = new ConnectivityInspector<>(forest1);
        List<Set<V>> trees1 = connectivityInspector1.connectedSets();

        ConnectivityInspector<V, E> connectivityInspector2 = new ConnectivityInspector<>(forest2);
        List<Set<V>> trees2 = connectivityInspector2.connectedSets();

        if (trees1.size() <= 1 && trees2.size() <= 1) {
            V root1 = roots1.iterator().next();
            V root2 = roots2.iterator().next();

            computed = true;
            isomorphicMapping = new AHUTreeIsomorphismInspector<>(forest1, root1, forest2, root2).getIsomorphism();
            return isomorphicMapping;
        }

        V fresh1 = getFreshVertex(forest1);
        V fresh2 = getFreshVertex(forest2);

        forest1.addVertex(fresh1);

        for (V root: roots1)
            forest1.addEdge(fresh1, root);

        forest2.addVertex(fresh2);

        for (V root: roots2)
            forest2.addEdge(fresh2, root);

        IsomorphicTreeMapping<V, E> mapping = new AHUTreeIsomorphismInspector<>(forest1, fresh1, forest2, fresh2).getIsomorphism();

        forest1.removeVertex(fresh1);
        forest2.removeVertex(fresh2);

        this.computed = true;

        if (mapping != null){
            Map<V, V> newForwardMapping = new HashMap<>(mapping.getForwardMapping().size());
            Map<V, V> newBackwardMapping = new HashMap<>(mapping.getBackwardMapping().size());

            for (V root1: roots1){
                V root2 = mapping.getVertexCorrespondence(root1, true);

                Graph<V, E> subgraph1 = takeSubraph(forest1, connectivityInspector1.connectedSetOf(root1));
                Graph<V, E> subgraph2 = takeSubraph(forest2, connectivityInspector2.connectedSetOf(root2));

                IsomorphicTreeMapping<V, E> tmpMapping =
                        new AHUTreeIsomorphismInspector<>(
                                subgraph1, root1,
                                subgraph2, root2
                        ).getIsomorphism();

                assert tmpMapping != null;
                assert Collections.disjoint(newForwardMapping.keySet(), tmpMapping.getForwardMapping().keySet());
                assert Collections.disjoint(newBackwardMapping.keySet(), tmpMapping.getBackwardMapping().keySet());

                newForwardMapping.putAll(tmpMapping.getForwardMapping());
                newBackwardMapping.putAll(tmpMapping.getBackwardMapping());
            }

            isomorphicMapping = new IsomorphicTreeMapping<>(newForwardMapping, newBackwardMapping, forest1, forest2);
        }

        return isomorphicMapping;
    }
}
