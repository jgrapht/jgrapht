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
import org.jgrapht.alg.vertexcover.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Greedy algorithm to find a vertex cover for a graph. A vertex cover is a set of vertices that
 * touches all the edges in the graph. The graph's vertex set is a trivial cover. However, a
 * <i>minimal</i> vertex set (or at least an approximation for it) is usually desired. Finding a
 * true minimal vertex cover is an NP-Complete problem. For more on the vertex cover problem, see
 * <a href="http://mathworld.wolfram.com/VertexCover.html">
 * http://mathworld.wolfram.com/VertexCover.html</a>
 *
 * Note: this class supports pseudo-graphs Runtime: $O(|E| \log |V|)$ This class produces often, but
 * not always, better solutions than the 2-approximation algorithms. Nevertheless, there are
 * instances where the solution is significantly worse. In those cases, consider using
 * {@link ClarksonTwoApproxVCImpl}.
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class GreedyVCImpl<V, E>
    implements
    VertexCoverAlgorithm<V>
{

    private static int vertexCounter = 0;

    private final Graph<V, E> graph;
    private final Map<V, Double> vertexWeightMap;

    /**
     * Constructs a new GreedyVCImpl instance where all vertices have uniform weights.
     * 
     * @param graph input graph
     */
    public GreedyVCImpl(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.vertexWeightMap = graph
            .vertexSet().stream().collect(Collectors.toMap(Function.identity(), vertex -> 1.0));
    }

    /**
     * Constructs a new GreedyVCImpl instance
     * 
     * @param graph input graph
     * @param vertexWeightMap mapping of vertex weights
     */
    public GreedyVCImpl(Graph<V, E> graph, Map<V, Double> vertexWeightMap)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.vertexWeightMap = Objects.requireNonNull(vertexWeightMap);
    }

    /**
     * Finds a greedy solution to the minimum weighted vertex cover problem. At each iteration, the
     * algorithm picks the vertex v with the smallest ratio {@code weight(v)/degree(v)} and adds it
     * to the cover. Next vertex v and all edges incident to it are removed. The process repeats
     * until all vertices are covered. Runtime: O(|E|*log|V|)
     *
     * @return greedy solution
     */
    @Override
    public VertexCoverAlgorithm.VertexCover<V> getVertexCover()
    {
        Set<V> cover = new LinkedHashSet<>();
        double weight = 0;

        // Create working graph: for every vertex, create a RatioVertex which maintains its own list
        // of neighbors
        Map<V, RatioVertex<V>> vertexEncapsulationMap = new HashMap<>();
        graph.vertexSet().stream().filter(v -> graph.degreeOf(v) > 0).forEach(
            v -> vertexEncapsulationMap
                .put(v, new RatioVertex<>(vertexCounter++, v, vertexWeightMap.get(v))));

        for (E e : graph.edgeSet()) {
            V u = graph.getEdgeSource(e);
            RatioVertex<V> ux = vertexEncapsulationMap.get(u);
            V v = graph.getEdgeTarget(e);
            RatioVertex<V> vx = vertexEncapsulationMap.get(v);
            ux.addNeighbor(vx);
            vx.addNeighbor(ux);

            assert (ux.neighbors.get(vx) == vx.neighbors.get(
                ux)) : " in an undirected graph, if vx is a neighbor of ux, then ux must be a neighbor of vx";
        }

        TreeSet<RatioVertex<V>> workingGraph = new TreeSet<>();
        workingGraph.addAll(vertexEncapsulationMap.values());
        assert (workingGraph.size() == vertexEncapsulationMap
            .size()) : "vertices in vertexEncapsulationMap: " + graph.vertexSet().size()
                + "vertices in working graph: " + workingGraph.size();

        while (!workingGraph.isEmpty()) { // Continue until all edges are covered

            // Find a vertex vx for which W(vx)/degree(vx) is minimal
            RatioVertex<V> vx = workingGraph.pollFirst();
            assert (workingGraph.parallelStream().allMatch(
                ux -> vx.getRatio() <= ux
                    .getRatio())) : "vx does not have the smallest ratio among all elements. VX: "
                        + vx + " WorkingGraph: " + workingGraph;

            for (RatioVertex<V> nx : vx.neighbors.keySet()) {

                if (nx == vx) // Ignore self loops
                    continue;

                workingGraph.remove(nx);

                // Delete vx from nx' neighbor list. Delete nx from the graph and place it back,
                // thereby updating the ordering of the graph
                nx.removeNeighbor(vx);

                if (nx.getDegree() > 0)
                    workingGraph.add(nx);

            }

            // Update cover
            cover.add(vx.v);
            weight += vertexWeightMap.get(vx.v);
            assert (workingGraph.parallelStream().noneMatch(
                ux -> ux.ID == vx.ID)) : "vx should no longer exist in the working graph";
        }
        return new VertexCoverAlgorithm.VertexCoverImpl<>(cover, weight);
    }

}
