/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * The greedy algorithm for computing a maximum weight matching in an arbitrary graph. The algorithm
 * runs in $O(m + m \log n)$ where $n$ is the number of vertices and $m$ is the number of edges of
 * the graph. This implementation accepts directed and undirected graphs which may contain
 * self-loops and multiple (parallel) edges. There is no assumption on the edge weights, i.e. they
 * can also be negative or zero.
 * 
 * <p>
 * This algorithm can be run in two modes: with and without edge cost normalization. Without
 * normalization, the algorithm first orders the edge set in non-increasing order of weights and
 * then greedily constructs a maximal cardinality matching out of the edges with positive weight. A
 * maximal cardinality matching (not to be confused with maximum cardinality) is a matching that
 * cannot be increased in cardinality without removing an edge first. The resulting matching is
 * guaranteed to be a $\frac{1}{2}$-Approximation. <br>
 * With normalization, the edges are sorted in non-increasing order of their normalized costs
 * $\frac{c(u,v)}{d(u)+d(v)}$ instead, after which the algorithm proceeds in the same manner. Here,
 * $c(u,v)$ is the cost of edge $(u,v)$, and $d(u)$ resp $d(v)$ are the degrees of vertices $u$ resp
 * $v$. Running this algorithm in normalized mode often (but not always!) produces a better result
 * than running the algorithm without normalization. <i>Note however that the normalized version
 * does NOT produce a $\frac{1}{2}$-approximation</i>. See <a href=
 * "https://mathoverflow.net/questions/269526/is-greedy-matching-algorithm-with-normalized-edge-weights-a-2-approximation/269760#269760">this
 * proof for details.</a> The runtime complexity remains the same, independent of whether
 * normalization is used.
 *
 * <p>
 * For more information about approximation algorithms for the maximum weight matching problem in
 * arbitrary graphs see:
 * <ul>
 * <li>R. Preis, Linear Time $\frac{1}{2}$-Approximation Algorithm for Maximum Weighted Matching in
 * General Graphs. Symposium on Theoretical Aspects of Computer Science, 259-269, 1999.</li>
 * <li>D.E. Drake, S. Hougardy, A Simple Approximation Algorithm for the Weighted Matching Problem,
 * Information Processing Letters 85, 211-213, 2003.</li>
 * </ul>
 * 
 * @see PathGrowingWeightedMatching
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class GreedyWeightedMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private final Comparator<Double> comparator;
    private final boolean normalizeEdgeCosts;

    /**
     * Create and execute a new instance of the greedy maximum weight matching algorithm. Floating
     * point values are compared using {@link #DEFAULT_EPSILON} tolerance.
     * 
     * @param graph the input graph
     * @param normalizeEdgeCosts boolean indicating whether edge normalization has to be used.
     */
    public GreedyWeightedMatching(Graph<V, E> graph, boolean normalizeEdgeCosts)
    {
        this(graph, normalizeEdgeCosts, DEFAULT_EPSILON);
    }

    /**
     * Create and execute a new instance of the greedy maximum weight matching algorithm.
     * 
     * @param graph the input graph
     * @param normalizeEdgeCosts boolean indicating whether edge normalization has to be used.
     * @param epsilon tolerance when comparing floating point values
     */
    public GreedyWeightedMatching(Graph<V, E> graph, boolean normalizeEdgeCosts, double epsilon)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Input graph cannot be null");
        }
        this.graph = graph;
        this.comparator = new ToleranceDoubleComparator(epsilon);
        this.normalizeEdgeCosts = normalizeEdgeCosts;
    }

    /**
     * Get a matching that is a $\frac{1}{2}$-approximation of the maximum weighted matching.
     * 
     * @return a matching
     */
    @Override
    public Matching<V, E> getMatching()
    {
        // sort edges in non-decreasing order of weight
        // (the lambda uses e1 and e2 in the reverse order on purpose)
        List<E> allEdges = new ArrayList<>(graph.edgeSet());
        if (normalizeEdgeCosts) {
            allEdges.sort((e1, e2) -> {
                double degreeE1 = graph.degreeOf(graph.getEdgeSource(e1))
                    + graph.degreeOf(graph.getEdgeTarget(e1));
                double degreeE2 = graph.degreeOf(graph.getEdgeSource(e2))
                    + graph.degreeOf(graph.getEdgeTarget(e2));
                return comparator
                    .compare(
                        graph.getEdgeWeight(e2) / degreeE2, graph.getEdgeWeight(e1) / degreeE1);
            });
        } else {
            allEdges
                .sort(
                    (e1, e2) -> comparator
                        .compare(graph.getEdgeWeight(e2), graph.getEdgeWeight(e1)));
        }

        double matchingWeight = 0d;
        Set<E> matching = new HashSet<>();
        Set<V> matchedVertices = new HashSet<>();

        // find maximal matching
        for (E e : allEdges) {
            double edgeWeight = graph.getEdgeWeight(e);
            V s = graph.getEdgeSource(e);
            V t = graph.getEdgeTarget(e);
            if (!s.equals(t) && comparator.compare(edgeWeight, 0d) > 0) {
                if (!matchedVertices.contains(s) && !matchedVertices.contains(t)) {
                    matching.add(e);
                    matchedVertices.add(s);
                    matchedVertices.add(t);
                    matchingWeight += edgeWeight;
                }
            }
        }

        // return matching
        return new MatchingImpl<>(graph, matching, matchingWeight);
    }

}
