/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Implementation of the 2-opt algorithm for a minimum weighted vertex cover by R. Bar-Yehuda and S.
 * Even. A linear time approximation algorithm for the weighted vertex cover problem. J. of
 * Algorithms 2:198-203, 1981. The solution is guaranteed to be within $2$ times the optimum
 * solution. An easier-to-read version of this algorithm can be found here: <a href=
 * "https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf">https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf</a>
 *
 * Note: this class supports pseudo-graphs Runtime: $O(|E|)$ This is a fast algorithm, guaranteed to
 * give a $2$-approximation. A solution of higher quality (same approximation ratio) at the
 * expensive of a higher runtime can be obtained using {@link BarYehudaEvenTwoApproxVCImpl}.
 *
 *
 * TODO: Remove the UndirectedSubgraph dependency! Querying vertex degrees on these graphs is
 * actually slow! This does affect the runtime complexity. Better would be to just work on a clone
 * of the original graph!
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class BarYehudaEvenTwoApproxVCImpl<V, E>
    implements
    VertexCoverAlgorithm<V>
{

    private final Graph<V, E> graph;
    private final Map<V, Double> vertexWeightMap;

    /**
     * Constructs a new BarYehudaEvenTwoApproxVCImpl instance where all vertices have uniform
     * weights.
     * 
     * @param graph input graph
     */
    public BarYehudaEvenTwoApproxVCImpl(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.vertexWeightMap = graph
            .vertexSet().stream().collect(Collectors.toMap(Function.identity(), vertex -> 1.0));
    }

    /**
     * Constructs a new BarYehudaEvenTwoApproxVCImpl instance
     * 
     * @param graph input graph
     * @param vertexWeightMap mapping of vertex weights
     */
    public BarYehudaEvenTwoApproxVCImpl(Graph<V, E> graph, Map<V, Double> vertexWeightMap)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.vertexWeightMap = Objects.requireNonNull(vertexWeightMap);
    }

    @Override
    public VertexCover<V> getVertexCover()
    {
        Set<V> cover = new LinkedHashSet<>();
        double weight = 0;
        Graph<V, E> copy = new AsSubgraph<>(graph, null, null);
        Map<V, Double> W = new HashMap<>();
        for (V v : graph.vertexSet())
            W.put(v, vertexWeightMap.get(v));

        // Main loop
        Set<E> edgeSet = copy.edgeSet();
        while (!edgeSet.isEmpty()) {
            // Pick arbitrary edge
            E e = edgeSet.iterator().next();
            V p = copy.getEdgeSource(e);
            V q = copy.getEdgeTarget(e);

            if (W.get(p) <= W.get(q)) {
                W.put(q, W.get(q) - W.get(p));
                cover.add(p);
                weight += vertexWeightMap.get(p);
                copy.removeVertex(p);
            } else {
                W.put(p, W.get(p) - W.get(q));
                cover.add(q);
                weight += vertexWeightMap.get(q);
                copy.removeVertex(q);
            }
        }
        return new VertexCoverAlgorithm.VertexCoverImpl<>(cover, weight);
    }

}
