/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * The greedy algorithm for computing a maximum cardinality matching. The algorithm can run in two
 * modes: sorted or unsorted. When unsorted, the matching is obtained by iterating through the edges
 * and adding an edge if it doesn't conflict with the edges already in the matching. When sorted,
 * the edges are first sorted by the sum of degrees of their endpoints. After that, the algorithm
 * proceeds in the same manner. Running this algorithm in sorted mode can sometimes produce better
 * results, albeit at the cost of some additional computational overhead.
 * <p>
 * Independent of the mode, the resulting matching is maximal, and is therefore guaranteed to
 * contain at least half of the edges that a maximum cardinality matching has ($\frac{1}{2}$
 * approximation). Runtime complexity: $O(m)$ when the edges are not sorted, $O(m + m \log n)$
 * otherwise, where $n$ is the number of vertices, and $m$ the number of edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class GreedyMaximumCardinalityMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private final boolean sort;

    /**
     * Creates a new GreedyMaximumCardinalityMatching instance.
     * 
     * @param graph graph
     * @param sort sort the edges prior to starting the greedy algorithm
     */
    public GreedyMaximumCardinalityMatching(Graph<V, E> graph, boolean sort)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.sort = sort;
    }

    /**
     * Get a matching that is a $\frac{1}{2}$-approximation of the maximum cardinality matching.
     *
     * @return a matching
     */
    @Override
    public Matching<V, E> getMatching()
    {
        Set<V> matched = new HashSet<>();
        Set<E> edges = new LinkedHashSet<>();
        double cost = 0;

        if (sort) {
            // sort edges in increasing order of the total degree of their endpoints
            List<E> allEdges = new ArrayList<>(graph.edgeSet());
            allEdges.sort(new EdgeDegreeComparator());

            for (E e : allEdges) {
                V v = graph.getEdgeSource(e);
                V w = graph.getEdgeTarget(e);
                if (!v.equals(w) && !matched.contains(v) && !matched.contains(w)) {
                    edges.add(e);
                    matched.add(v);
                    matched.add(w);
                    cost += graph.getEdgeWeight(e);
                }
            }
        } else {
            for (V v : graph.vertexSet()) {
                if (matched.contains(v))
                    continue;

                for (E e : graph.edgesOf(v)) {
                    V w = Graphs.getOppositeVertex(graph, e, v);
                    if (!v.equals(w) && !matched.contains(w)) {
                        edges.add(e);
                        matched.add(v);
                        matched.add(w);
                        cost += graph.getEdgeWeight(e);
                        break;
                    }
                }
            }
        }
        return new MatchingImpl<>(graph, edges, cost);
    }

    private class EdgeDegreeComparator
        implements
        Comparator<E>
    {
        @Override
        public int compare(E e1, E e2)
        {
            int degreeE1 =
                graph.degreeOf(graph.getEdgeSource(e1)) + graph.degreeOf(graph.getEdgeTarget(e1));
            int degreeE2 =
                graph.degreeOf(graph.getEdgeSource(e2)) + graph.degreeOf(graph.getEdgeTarget(e2));
            return Integer.compare(degreeE1, degreeE2);
        }
    }
}
