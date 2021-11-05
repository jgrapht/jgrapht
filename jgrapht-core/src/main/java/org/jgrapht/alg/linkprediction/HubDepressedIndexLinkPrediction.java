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
package org.jgrapht.alg.linkprediction;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LinkPredictionAlgorithm;
import org.jgrapht.alg.util.Pair;

/**
 * Predict links using the Hub Depressed Index.
 * 
 * <p>
 * This is a local method which computes $s_{xy} =
 * \frac{2|\Gamma(u)\cap\Gamma(v))|}{max(k(u),k(v))}$ where for a node $v$, $\Gamma(v)$ denotes the
 * set of neighbors of $v$ and $k(v) = |\Gamma(v)|$ denotes the degree of $v$.
 * </p>
 * 
 * See the following paper:
 * <ul>
 * <li>E. Ravasz, A.L. Somera, D.A. Mongru, Z.N. Oltvai, A.-L. Barabási, Science 297, 1553
 * (2002)</li>
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class HubDepressedIndexLinkPrediction<V, E>
    implements
    LinkPredictionAlgorithm<V, E>
{
    private Graph<V, E> graph;

    /**
     * Create a new prediction
     * 
     * @param graph the input graph
     */
    public HubDepressedIndexLinkPrediction(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
    }

    @Override
    public double predict(V u, V v)
    {
        int du = graph.outDegreeOf(u);
        int dv = graph.outDegreeOf(v);

        if (du == 0 && dv == 0) {
            throw new LinkPredictionIndexNotWellDefinedException(
                "Both vertices have zero neighbors", Pair.of(u, v));
        }

        List<V> gu = Graphs.successorListOf(graph, u);
        List<V> gv = Graphs.successorListOf(graph, v);

        Set<V> intersection = new HashSet<>(gu);
        intersection.retainAll(gv);

        return (double) intersection.size() / Math.max(du, dv);
    }

}
