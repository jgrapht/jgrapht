/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.interfaces;

import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * Algorithm to compute a <a href="https://en.wikipedia.org/wiki/Lowest_common_ancestor">lowest
 * common ancestor</a> in a tree, forest or DAG.
 *
 * @param <V> vertex the graph vertex type
 *
 * @author Alexandru Valeanu
 */
public interface LowestCommonAncestorAlgorithm<V>
{

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
     * Return a list of LCAs for a batch of queries
     *
     * @param queries a list of pairs of vertices
     * @return a list L of LCAs where L(i) is the LCA for pair queries(i)
     */
    default List<V> getBatchLCA(List<Pair<V, V>> queries)
    {
        return queries
            .stream().map(p -> getLCA(p.getFirst(), p.getSecond())).collect(Collectors.toList());
    }

    /**
     * Return the computed set of LCAs of a and b
     *
     * @param a the first element to find LCA for
     * @param b the other element to find the LCA for
     *
     * @return the set LCAs of a and b, or empty set if there is no LCA computed.
     * @throws UnsupportedOperationException - if the operation is not supported by the implementing
     *         class
     */
    Set<V> getLCASet(V a, V b);

    /**
     * Return a list of computed sets of LCAs for a batch of queries
     *
     * @param queries a list of pairs of vertices
     * @return a list L of LCAs where L(i) is the computed set of LCAs for pair queries(i)
     */
    default List<Set<V>> getBatchLCASet(List<Pair<V, V>> queries)
    {
        return queries
            .stream().map(p -> getLCASet(p.getFirst(), p.getSecond())).collect(Collectors.toList());
    }
}
