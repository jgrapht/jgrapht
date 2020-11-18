/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

/**
 * A link prediction algorithm.
 * 
 * <p>
 * A link prediction algorithm assigns a score $s_{uv}$ for each pair of nodes $u, v \in V$. This
 * score can be viewed as a measure of similarity between nodes $u$ and $v$. All the non-existent
 * links are sorted in decreasing order according to their scores, and the links at the top are most
 * likely to exist.
 * <p>
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface LinkPredictionAlgorithm<V, E>
{

    /**
     * Predict an edge between a set of vertex pairs.
     * 
     * @param queries a list of vertex pairs
     * @return a list of vertex triples where the last component is an edge prediction score
     */
    default List<Triple<V, V, Double>> predict(List<Pair<V, V>> queries)
    {
        List<Triple<V, V, Double>> result = new ArrayList<>();
        for (Pair<V, V> q : queries) {
            result
                .add(Triple.of(q.getFirst(), q.getSecond(), predict(q.getFirst(), q.getSecond())));
        }
        return result;
    }

    /**
     * Predict an edge between two vertices. The magnitude and the interpretation of the returned
     * scores depend solely on the algorithm.
     * 
     * @param u first vertex
     * @param v second vertex
     * @return a prediction score
     */
    double predict(V u, V v);

}
