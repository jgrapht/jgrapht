/*
 * (C) Copyright 2010-2021, by Tom Conerly and Contributors.
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
 * An implementation of <a href="http://en.wikipedia.org/wiki/Kruskal's_algorithm">Kruskal's minimum
 * spanning tree algorithm</a>. If the given graph is connected it computes the minimum spanning
 * tree, otherwise it computes the minimum spanning forest. The algorithm runs in time $O(E \log
 * E)$. This implementation uses the hashCode and equals method of the vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Tom Conerly
 */
public class KruskalMinimumSpanningTree<V, E>
    implements
    SpanningTreeAlgorithm<E>
{
    private final Graph<V, E> graph;

    /**
     * Construct a new instance of the algorithm.
     * 
     * @param graph the input graph
     */
    public KruskalMinimumSpanningTree(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanningTree<E> getSpanningTree()
    {
        UnionFind<V> forest = new UnionFind<>(graph.vertexSet());
        ArrayList<E> allEdges = new ArrayList<>(graph.edgeSet());
        allEdges.sort(Comparator.comparingDouble(graph::getEdgeWeight));

        double spanningTreeCost = 0;
        Set<E> edgeList = new HashSet<>();

        for (E edge : allEdges) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (forest.find(source).equals(forest.find(target))) {
                continue;
            }

            forest.union(source, target);
            edgeList.add(edge);
            spanningTreeCost += graph.getEdgeWeight(edge);
        }

        return new SpanningTreeImpl<>(edgeList, spanningTreeCost);
    }
}
