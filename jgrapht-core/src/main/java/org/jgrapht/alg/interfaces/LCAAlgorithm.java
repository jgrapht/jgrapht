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
package org.jgrapht.alg.interfaces;

import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Algorithm to compute a <a href="https://en.wikipedia.org/wiki/Lowest_common_ancestor">lowest common ancestor</a>
 * in a tree, forest or DAG.
 *
 * @param <V> vertex the graph vertex type
 *
 * @author Alexandru Valeanu
 */
public interface LCAAlgorithm<V> {

    /**
     * Return the LCA of a and b
     *
     * @param a the first element to find LCA for
     * @param b the other element to find the LCA for
     *
     * @return the LCA of a and b, or null if there is no LCA.
     */
    V getLCA(V a, V b);

    /**
     * Return a list of LCA for a batch of queries
     *
     * @param queries a list of pairs of vertices
     * @return a list L of LCAs where L(i) is the LCA of pair queries(i)
     */
    default List<V> getLCAs(List<Pair<V, V>> queries){
        return queries.stream().map(p -> getLCA(p.getFirst(), p.getSecond())).collect(Collectors.toList());
    }
}
