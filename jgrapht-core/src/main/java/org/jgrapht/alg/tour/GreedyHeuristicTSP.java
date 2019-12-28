/*
 * (C) Copyright 2019-2019, by Peter Harman and Contributors.
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.traverse.DepthFirstIterator;

/**
 * The greedy heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a
 * list of cities and the distances between each pair of cities, what is the
 * shortest possible route that visits each city exactly once and returns to the
 * origin city?".
 * </p>
 *
 * <p>
 * The Greedy heuristic gradually constructs a tour by repeatedly selecting the
 * shortest edge and adding it to the tour as long as it doesn’t create a cycle
 * with less than N edges, or increases the degree of any node to more than 2.
 * We must not add the same edge twice of course.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * <a href="http://160592857366.free.fr/joe/ebooks/ShareData/Heuristics%20for%20the%20Traveling%20Salesman%20Problem%20By%20Christian%20Nillson.pdf">Nilsson
 * C., "Heuristics for the Traveling Salesman Problem"</a>
 * </p>
 *
 * <p>
 * The runtime complexity of this class is $O(V^2 log(V))$.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class GreedyHeuristicTSP<V, E> extends HamiltonianCycleAlgorithmBase<V, E> {

    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph) {
        int n = graph.vertexSet().size();
        // Handle single vertex graph
        if (n == 1) {
            return getSingletonTour(graph);
        }
        // Sort all the edges by weight
        Deque<E> edges = graph.edgeSet().stream()
                .sorted((e1, e2) -> Double.compare(graph.getEdgeWeight(e1), graph.getEdgeWeight(e2)))
                .collect(Collectors.toCollection(() -> new ArrayDeque<>()));
        Set<E> tourEdges = new HashSet<>(n);
        // Create a sub-graph that only includes the tour edges
        Graph<V, E> tourGraph = new MaskSubgraph<>(graph, (v) -> false, (e) -> !tourEdges.contains(e));
        // Iterate until the tour is complete
        while (!edges.isEmpty() && tourEdges.size() < n) {
            // Select the shortest available edge
            E edge = edges.pollFirst();
            V vertex1 = graph.getEdgeSource(edge);
            V vertex2 = graph.getEdgeTarget(edge);
            // If it matches constraints, add it to the tour
            if (canAddEdge(tourGraph, edge, vertex1, vertex2, tourEdges.size() == n - 1)) {
                tourEdges.add(edge);
            }
        }
        // Build the tour into a GraphPath
        List<V> tourVertices = new ArrayList<>(n);
        Iterator<V> dfs = new DepthFirstIterator<>(tourGraph);
        while (dfs.hasNext()) {
            tourVertices.add(dfs.next());
        }
        return listToTour(tourVertices, graph);
    }

    /**
     * Tests if an edge can be added. Returns false if it would increase the
     * degree of a vertex to more than 2. Returns false if a cycle is created
     * and we are not at the last edge, or false if we do not create a cycle and
     * are at the last edge.
     *
     * @param tourGraph The graph masked to only include tour edges
     * @param edge The proposed edge
     * @param vertex1 First vertex
     * @param vertex2 Second vertex
     * @param lastEdge true if we are looking for the last edge
     * @return true if this edge can be added
     */
    private boolean canAddEdge(Graph<V, E> tourGraph, E edge, V vertex1, V vertex2, boolean lastEdge) {
        // Would form a tree rather than loop
        if (tourGraph.degreeOf(vertex1) > 1 || tourGraph.degreeOf(vertex2) > 1) {
            return false;
        }
        // Test if a path already exists between the vertices
        Iterator<V> dfs = new DepthFirstIterator<>(tourGraph, vertex1);
        while (dfs.hasNext()) {
            V v = dfs.next();
            if (v.equals(vertex2)) {
                return lastEdge;
            }
        }
        return !lastEdge;
    }
}
