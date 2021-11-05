/*
 * (C) Copyright 2019-2021, by Peter Harman and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * The greedy heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 * </p>
 *
 * <p>
 * The Greedy heuristic gradually constructs a tour by repeatedly selecting the shortest edge and
 * adding it to the tour as long as it doesn’t create a cycle with less than N edges, or increases
 * the degree of any node to more than 2. We must not add the same edge twice of course.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * Nilsson, Christian. "Heuristics for the traveling salesman problem." Linkoping University 38
 * (2003)
 * </p>
 *
 * <p>
 * The runtime complexity of this class is $O(V^2 log(V))$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class GreedyHeuristicTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
{

    /**
     * Computes a tour using the greedy heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        checkGraph(graph);
        int n = graph.vertexSet().size();
        if (n == 1) {
            return getSingletonTour(graph);
        }

        // Sort all the edges by weight
        Deque<E> edges = graph
            .edgeSet().stream()
            .sorted((e1, e2) -> Double.compare(graph.getEdgeWeight(e1), graph.getEdgeWeight(e2)))
            .collect(Collectors.toCollection(ArrayDeque::new));
        Set<E> tourEdges = CollectionUtil.newHashSetWithExpectedSize(n);
        // Create a Map to track the degree of each vertex in tour
        Map<V, Integer> vertexDegree = CollectionUtil.newHashMapWithExpectedSize(n);
        // Create a UnionFind to track forming of loops
        UnionFind<V> tourSet = new UnionFind<>(graph.vertexSet());

        // Iterate until the tour is complete
        while (!edges.isEmpty() && tourEdges.size() < n) {
            // Select the shortest available edge
            E edge = edges.pollFirst();
            V vertex1 = graph.getEdgeSource(edge);
            V vertex2 = graph.getEdgeTarget(edge);
            // If it matches constraints, add it to the tour
            if (canAddEdge(vertexDegree, tourSet, vertex1, vertex2, tourEdges.size() == n - 1)) {
                tourEdges.add(edge);
                vertexDegree.merge(vertex1, 1, Integer::sum);
                vertexDegree.merge(vertex2, 1, Integer::sum);
                tourSet.union(vertex1, vertex2);
            }
        }
        // Build the tour into a GraphPath
        return edgeSetToTour(tourEdges, graph);
    }

    /**
     * Tests if an edge can be added. Returns false if it would increase the degree of a vertex to
     * more than 2. Returns false if a cycle is created and we are not at the last edge, or false if
     * we do not create a cycle and are at the last edge.
     *
     * @param vertexDegree A Map tracking the degree of each vertex in the tour
     * @param tourSet A UnionFind tracking the connectivity of the tour
     * @param vertex1 First vertex of proposed edge
     * @param vertex2 Second vertex of proposed edge
     * @param lastEdge true if we are looking for the last edge
     * @return true if this edge can be added
     */
    private boolean canAddEdge(
        Map<V, Integer> vertexDegree, UnionFind<V> tourSet, V vertex1, V vertex2, boolean lastEdge)
    {
        // Would form a tree rather than loop
        if (vertexDegree.getOrDefault(vertex1, 0) > 1
            || vertexDegree.getOrDefault(vertex2, 0) > 1)
        {
            return false;
        }
        // Test if a path already exists between the vertices
        return tourSet.inSameSet(vertex1, vertex2) ? lastEdge : !lastEdge;
    }
}
