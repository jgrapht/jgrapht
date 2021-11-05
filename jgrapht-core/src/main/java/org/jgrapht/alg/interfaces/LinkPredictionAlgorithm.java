/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
 * A link prediction algorithm provides a score $s_{uv}$ for any pair of vertices $u,v \in V$ in the
 * graph such that $e=(u,v) \notin E$. The nature, the magnitude and possible interpretation of such
 * a score depends solely on the actual algorithm, meaning that it might be a similarity score, a
 * distance metric, a probability, or even something completely unrelated.
 * 
 * Depending on the particular algorithm, a possible interpretation of the scores might be that they
 * measure similarity between vertices $u$ and $v$. Thus, given such scores one could sort the edges
 * in decreasing order and pick the top-k as links (edges) which are likely to exist.
 * </p>
 * 
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface LinkPredictionAlgorithm<V, E>
{

    /**
     * Predict an edge between a set of vertex pairs. The magnitude and the interpretation of the
     * returned scores depend solely on the algorithm.
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
     * score depend solely on the algorithm.
     * 
     * @param u first vertex
     * @param v second vertex
     * @return a prediction score
     */
    double predict(V u, V v);

}
