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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a GraphMapping between two isomorphic trees.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public class IsomorphicTreeMapping<V, E> implements GraphMapping<V, E> {

    private final Map<V, V> forwardMapping;
    private final Map<V, V> backwardMapping;

    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    public IsomorphicTreeMapping(Map<V, V> forwardMapping, Map<V, V> backwardMapping, Graph<V, E> graph1, Graph<V, E> graph2) {
        this.forwardMapping = forwardMapping;
        this.backwardMapping = backwardMapping;

        this.graph1 = graph1;
        this.graph2 = graph2;
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

    public Map<V, V> getForwardMapping(){
        return Collections.unmodifiableMap(forwardMapping);
    }

    public Map<V, V> getBackwardMapping(){
        return Collections.unmodifiableMap(backwardMapping);
    }


    @Override
    public String toString() {
        return forwardMapping.toString();
    }

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
}
