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

import java.util.*;

/**
 * This class represents a GraphMapping between two isomorphic trees or forests.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class IsomorphicTreeMapping<V, E> implements GraphMapping<V, E> {

    private final Map<V, V> forwardMapping;
    private final Map<V, V> backwardMapping;

    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    /**
     * Construct a new isomorphic tree (or forest) mapping.
     *
     * @param forwardMapping the mapping from graph1 to graph2
     * @param backwardMapping the mapping from graph2 to graph1 (inverse of forwardMapping)
     * @param graph1 the first tree (forest)
     * @param graph2 the second tree (forest)

     */
    public IsomorphicTreeMapping(Map<V, V> forwardMapping, Map<V, V> backwardMapping, Graph<V, E> graph1, Graph<V, E> graph2) {
        this.forwardMapping = Objects.requireNonNull(forwardMapping);
        this.backwardMapping = Objects.requireNonNull(backwardMapping);

        this.graph1 = Objects.requireNonNull(graph1);
        this.graph2 = Objects.requireNonNull(graph2);
    }


    @Override
    public V getVertexCorrespondence(V v, boolean forward) {
        if (forward)
            return forwardMapping.get(v);
        else
            return backwardMapping.get(v);
    }

    @Override
    public E getEdgeCorrespondence(E e, boolean forward) {
        if (forward) {
            V u = graph1.getEdgeSource(e);
            V v = graph1.getEdgeTarget(e);

            return graph2.getEdge(forwardMapping.get(u), forwardMapping.get(v));

        } else {
            V u = graph2.getEdgeSource(e);
            V v = graph2.getEdgeTarget(e);

            return graph1.getEdge(backwardMapping.get(u), backwardMapping.get(v));
        }
    }

    /**
     * Get an unmodifiable version of the forward mapping function.
     *
     * @return the unmodifiable forward mapping function
     */
    public Map<V, V> getForwardMapping(){
        return Collections.unmodifiableMap(forwardMapping);
    }

    /**
     * Get an unmodifiable version of the backward mapping function.
     *
     * @return the unmodifiable backward mapping function
     */
    public Map<V, V> getBackwardMapping(){
        return Collections.unmodifiableMap(backwardMapping);
    }

    /**
     * Get the active domain of the isomorphism.
     *
     * @return the set of vertices $v$ for which the mapping is defined
     */
    public Set<V> getMappingDomain(){
        return forwardMapping.keySet();
    }

    /**
     * Get the range of the isomorphism.
     *
     * @return the set of vertices $v$ for which a preimage exists
     */
    public Set<V> getMappingRange(){
        return backwardMapping.keySet();
    }

    @Override
    public String toString() {
        return forwardMapping.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsomorphicTreeMapping<?, ?> that = (IsomorphicTreeMapping<?, ?>) o;
        return Objects.equals(forwardMapping, that.forwardMapping) &&
                Objects.equals(backwardMapping, that.backwardMapping);
    }

    @Override
    public int hashCode() {

        return Objects.hash(forwardMapping, backwardMapping);
    }

    /**
     * Computes the composition of two isomorphism.
     * Let $f : V_{G_1} \rightarrow V_{G_2}$ be an isomorphism from $V_{G_1}$ to $V_{G_2}$ and
     * $g : V_{G_2} \rightarrow V_{G_3}$ one from $V_{G_2}$ to $V_{G_3}$.
     *
     * This method computes an isomorphism $h : V_{G_1} \rightarrow V_{G_3}$ from $V_{G_1}$ to $V_{G_3}$.
     *
     * Note: The composition $g ∘ f$ can be built only if $f$'s codomain equals $g$'s domain;
     * this implementation only requires that the former is a subset of the latter.
     *
     * @param treeMapping the other isomorphism (i.e. function $g$)
     * @return the composition of the two isomorphism
     */
    public IsomorphicTreeMapping<V, E> compose(IsomorphicTreeMapping<V, E> treeMapping){
        Map<V, V> fMap = new HashMap<>(forwardMapping.size());
        Map<V, V> bMap = new HashMap<>(forwardMapping.size());

        for (V v: graph1.vertexSet()){
            V u = treeMapping.getVertexCorrespondence(forwardMapping.get(v), true);
            fMap.put(v, u);
            bMap.put(u, v);
        }

        return new IsomorphicTreeMapping<>(fMap, bMap, graph1, treeMapping.graph2);
    }

    /**
     * Computes an automorphism (i.e. an isomorphism mapping from a graph to itself).
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a mapping from graph to graph
     */
    public static <V, E> IsomorphicTreeMapping<V, E> identity(Graph<V, E> graph){
        Map<V, V> fMap = new HashMap<>(graph.vertexSet().size());
        Map<V, V> bMap = new HashMap<>(graph.vertexSet().size());

        for (V v: graph.vertexSet()){
            fMap.put(v, v);
            bMap.put(v, v);
        }

        return new IsomorphicTreeMapping<>(fMap, bMap, graph, graph);
    }
}
