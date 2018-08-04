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
import org.jgrapht.GraphMapping;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.function.Supplier;

/**
 * This is an implementation of the AHU algorithm for detecting an (unweighted) isomorphism between two rooted forests.
 * Please see <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a complete
 * definition of the isomorphism problem for general graphs.
 *
 * <p>
 *     The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974.
 *     The Design and Analysis of Computer Algorithms (1st ed., page 84). Addison-Wesley
 *     Longman Publishing Co., Inc., Boston, MA, USA."
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
 * <p>
 *     Note: This implementation requires the input graphs to be modifiable and to have valid vertex suppliers
 *     (see {@link Graph#getVertexSupplier()}).
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHUForestIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {
    private final Graph<V, E> forest1;
    private final Graph<V, E> forest2;

    private final Set<V> roots1;
    private final Set<V> roots2;

    private boolean computed = false;
    private IsomorphicTreeMapping<V, E> isomorphicMapping;

    /**
     * Construct a new AHU rooted forest isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input forests are valid trees.
     *
     * @param forest1 the first rooted forest
     * @param roots1 the roots of the first forest
     * @param forest2 the second rooted forest
     * @param roots2 the roots of the second forest
     * @throws NullPointerException if {@code forest1} is {@code null}
     * @throws NullPointerException if {@code roots1} is {@code null}
     * @throws NullPointerException if {@code forest2} is {@code null}
     * @throws NullPointerException if {@code roots2} is {@code null}
     * @throws IllegalArgumentException if {@code roots1} is empty
     * @throws IllegalArgumentException if {@code roots2} is empty
     * @throws IllegalArgumentException if either {@code roots1} or {@code roots2} contain an invalid vertex
     */
    public AHUForestIsomorphismInspector(Graph<V, E> forest1, Set<V> roots1, Graph<V, E> forest2, Set<V> roots2){
        this.forest1 = Objects.requireNonNull(forest1, "input forest cannot be null");
        this.forest2 = Objects.requireNonNull(forest2, "input forest cannot be null");

        this.roots1 = Objects.requireNonNull(roots1, "set of roots cannot be null");
        this.roots2 = Objects.requireNonNull(roots2, "set of roots cannot be null");

        if (roots1.isEmpty()){
            throw new IllegalArgumentException("roots1 cannot be empty");
        }

        if (roots2.isEmpty()){
            throw new IllegalArgumentException("roots2 cannot be empty");
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

        V v = supplier.get();
        assert !graph.vertexSet().contains(v);
        return v;
    }

    private Graph<V, E> takeSubgraph(Graph<V, E> graph, Set<V> vertices){
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings() {
        return new Iterator<GraphMapping<V, E>>() {
            private IsomorphicTreeMapping<V, E> iterMapping = getMapping();

            @Override
            public boolean hasNext() {
                return iterMapping != null;
            }

            @Override
            public GraphMapping<V, E> next() {
                if (iterMapping == null){
                    throw new NoSuchElementException("no mapping available");
                }

                IsomorphicTreeMapping<V, E> tmp = iterMapping;
                iterMapping = null;
                return tmp;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isomorphismExists(){
        if (!computed){
            isomorphicMapping = getMapping();
        }

        return isomorphicMapping != null;
    }

    private V addDummyRoot(Graph<V, E> forest, Set<V> roots){
        V fresh = getFreshVertex(forest);

        forest.addVertex(fresh);

        for (V root: roots)
            forest.addEdge(fresh, root);

        return fresh;
    }

    /**
     * Get an isomorphism between the input forest or {@code null} if none exists.
     *
     * @return isomorphic mapping, {@code null} is none exists
     */
    public IsomorphicTreeMapping<V, E> getMapping(){
        if (computed) {
            return isomorphicMapping;
        }

        ConnectivityInspector<V, E> connectivityInspector1 = new ConnectivityInspector<>(forest1);
        List<Set<V>> trees1 = connectivityInspector1.connectedSets();

        ConnectivityInspector<V, E> connectivityInspector2 = new ConnectivityInspector<>(forest2);
        List<Set<V>> trees2 = connectivityInspector2.connectedSets();

        if (trees1.size() <= 1 && trees2.size() <= 1) {
            V root1 = roots1.iterator().next();
            V root2 = roots2.iterator().next();

            isomorphicMapping = new AHUTreeIsomorphismInspector<>(forest1, root1, forest2, root2).getMapping();
        }
        else{
            V fresh1 = addDummyRoot(forest1, roots1);
            V fresh2 = addDummyRoot(forest2, roots2);

            IsomorphicTreeMapping<V, E> mapping =
                    new AHUTreeIsomorphismInspector<>(forest1, fresh1, forest2, fresh2).getMapping();

            forest1.removeVertex(fresh1);
            forest2.removeVertex(fresh2);

            if (mapping != null){
                Map<V, V> newForwardMapping = new HashMap<>(mapping.getForwardMapping());
                Map<V, V> newBackwardMapping = new HashMap<>(mapping.getBackwardMapping());

                // remove the mapping from fresh1 to fresh 2 (and vice-versa)
                newForwardMapping.remove(fresh1);
                newBackwardMapping.remove(fresh2);

                isomorphicMapping = new IsomorphicTreeMapping<>(newForwardMapping, newBackwardMapping, forest1, forest2);
            }
        }

        computed = true;
        return isomorphicMapping;
    }
}
