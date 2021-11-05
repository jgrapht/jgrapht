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
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Borůvka's algorithm for the computation of a minimum spanning tree.
 * 
 * <p>
 * See the article on
 * <a href="https://en.wikipedia.org/wiki/Bor%C5%AFvka%27s_algorithm">wikipedia</a> for more
 * information on the history of the algorithm.
 * 
 * <p>
 * This implementation uses a union-find data structure (with union by rank and path compression
 * heuristic) in order to track components. In graphs where edges have identical weights, edges with
 * equal weights are ordered lexicographically. The running time is $O((E+V) \log V)$ under the
 * assumption that the union-find uses path-compression.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class BoruvkaMinimumSpanningTree<V, E>
    implements
    SpanningTreeAlgorithm<E>
{
    private final Graph<V, E> graph;
    private final Comparator<Double> comparator;

    /**
     * Construct a new instance of the algorithm.
     * 
     * @param graph the input graph
     */
    public BoruvkaMinimumSpanningTree(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.comparator = new ToleranceDoubleComparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanningTree<E> getSpanningTree()
    {
        // create result placeholder
        Set<E> mstEdges = new LinkedHashSet<>();
        double mstWeight = 0d;

        // fix edge order for unique comparison of edge weights
        Map<E, Integer> edgeOrder = new HashMap<>();
        int i = 0;
        for (E e : graph.edgeSet()) {
            edgeOrder.put(e, i++);
        }

        // initialize forest
        UnionFind<V> forest = new UnionFind<>(graph.vertexSet());
        Map<V, E> bestEdge = new LinkedHashMap<>();

        do {
            // find safe edges
            bestEdge.clear();
            for (E e : graph.edgeSet()) {
                V sTree = forest.find(graph.getEdgeSource(e));
                V tTree = forest.find(graph.getEdgeTarget(e));

                if (sTree.equals(tTree)) {
                    // same tree, skip
                    continue;
                }

                double eWeight = graph.getEdgeWeight(e);

                // check if better edge
                E sTreeEdge = bestEdge.get(sTree);
                if (sTreeEdge == null) {
                    bestEdge.put(sTree, e);
                } else {
                    double sTreeEdgeWeight = graph.getEdgeWeight(sTreeEdge);
                    int c = comparator.compare(eWeight, sTreeEdgeWeight);
                    if (c < 0 || (c == 0 && edgeOrder.get(e) < edgeOrder.get(sTreeEdge))) {
                        bestEdge.put(sTree, e);
                    }
                }

                // check if better edge
                E tTreeEdge = bestEdge.get(tTree);
                if (tTreeEdge == null) {
                    bestEdge.put(tTree, e);
                } else {
                    double tTreeEdgeWeight = graph.getEdgeWeight(tTreeEdge);
                    int c = comparator.compare(eWeight, tTreeEdgeWeight);
                    if (c < 0 || (c == 0 && edgeOrder.get(e) < edgeOrder.get(tTreeEdge))) {
                        bestEdge.put(tTree, e);
                    }
                }
            }

            // add safe edges to forest
            for (V v : bestEdge.keySet()) {
                E e = bestEdge.get(v);

                V sTree = forest.find(graph.getEdgeSource(e));
                V tTree = forest.find(graph.getEdgeTarget(e));

                if (sTree.equals(tTree)) {
                    // same tree, skip
                    continue;
                }

                mstEdges.add(e);
                mstWeight += graph.getEdgeWeight(e);

                forest.union(sTree, tTree);
            }
        } while (!bestEdge.isEmpty());

        // return mst
        return new SpanningTreeImpl<>(mstEdges, mstWeight);
    }
}
