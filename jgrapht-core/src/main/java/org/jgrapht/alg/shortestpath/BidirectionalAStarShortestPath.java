/*
 * (C) Copyright 2015-2018, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A bidirectional version of A* algorithm.
 *
 * <p>
 * See the Wikipedia article for details and references about
 * <a href="https://en.wikipedia.org/wiki/Bidirectional_search">bidirectional search</a>. This
 * technique does not change the worst-case behavior of the algorithm but reduces, in some cases,
 * the number of visited vertices in practice.
 * <p>
 * The algorithm was first introduced in Ira Sheldon Pohl. 1969.
 * Bi-Directional and Heuristic Search in Path Problems.
 * Ph.D. Dissertation. Stanford University, Stanford, CA, USA. AAI7001588.
 * <p>
 * The termination criterion suggested in (Pohl 1969) is
 * based on the shortest path distance, $\mu$, seen thus far in the
 * search. Initially, the bidirectional algorithm sets $\mu=\infty$.
 * Whenever the search in a given direction closes a node $v$,
 * such that $v$ has already been closed in the opposite search
 * direction, then the algorithm sets $\mu = min\{\mu; g_f(v) + g_b(v)\}$,
 * where $g_f(v)$ is the current best-known path cost from $source$ to $sink$ and
 * $g_b(v)$ is the current best-known path cost from $source$ to $sink$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @author Dimitrios Michail
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 * @see AStarShortestPath
 */
public class BidirectionalAStarShortestPath<V, E>
        extends
        BaseShortestPathAlgorithm<V, E> {
    /**
     * Heuristic used while shortest path computation.
     */
    private AStarAdmissibleHeuristic<V> admissibleHeuristic;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *      the target node. The heuristic must never overestimate the distance.
     */
    public BidirectionalAStarShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic) {
        super(graph);
        this.admissibleHeuristic =
                Objects.requireNonNull(admissibleHeuristic, "Heuristic function cannot be null!");
    }

    /**
     * Calculates (and returns) the shortest path from the {@code source} to the {@code sink}.
     * Note: each time you invoke this method, the path gets recomputed.
     *
     * @param source source vertex
     * @param sink   target vertex
     * @return the shortest path from sourceVertex to targetVertex
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }

        // handle special case if source equals target
        if (source.equals(sink)) {
            return createEmptyPath(source, sink);
        }

        // create frontiers
        SearchFrontier forwardFrontier = new SearchFrontier(graph, sink);
        SearchFrontier backwardFrontier;
        if (graph.getType().isDirected()) {
            backwardFrontier = new SearchFrontier(new EdgeReversedGraph<>(graph), source);
        } else {
            backwardFrontier = new SearchFrontier(graph, source);
        }

        forwardFrontier.updateDistance(source, null, 0.0, 0.0);
        backwardFrontier.updateDistance(sink, null, 0.0, 0.0);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        V bestPathCommonVertex = null;

        SearchFrontier frontier = forwardFrontier;
        SearchFrontier otherFrontier = backwardFrontier;

        while (true) {
            // stopping condition
            if (frontier.openList.isEmpty() || otherFrontier.openList.isEmpty()
                    || Math.max(frontier.openList.min().getKey(), otherFrontier.openList.min().getKey()) >= bestPath) {
                break;
            }

            // frontier scan
            FibonacciHeapNode<V> node = frontier.openList.removeMin();
            V v = node.getData();

            for (E edge : frontier.graph.outgoingEdgesOf(v)) {
                V successor = Graphs.getOppositeVertex(frontier.graph, edge, v);

                if (successor.equals(v)) { // Ignore self-loop
                    continue;
                }

                double gScore_current = frontier.getDistance(v);
                double tentativeGScore = gScore_current + frontier.graph.getEdgeWeight(edge);
                double fScore = tentativeGScore + admissibleHeuristic.getCostEstimate(successor, frontier.endVertex);

                frontier.updateDistance(successor, edge, tentativeGScore, fScore);


            }
            // close current vertex
            frontier.closedList.add(node.getData());
            // check if best path can be updated
            if (otherFrontier.closedList.contains(node.getData())) {
                double pathDistance = frontier.getDistance(v) + otherFrontier.getDistance(v);
                if (pathDistance < bestPath) {
                    bestPath = pathDistance;
                    bestPathCommonVertex = v;
                }
            }

            // swap frontiers
            SearchFrontier tmpFrontier = frontier;
            frontier = otherFrontier;
            otherFrontier = tmpFrontier;
        }

        // create path if found
        if (Double.isFinite(bestPath)) {
            return createPath(
                    forwardFrontier, backwardFrontier, bestPath, source, bestPathCommonVertex, sink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    private GraphPath<V, E> createPath(
            SearchFrontier forwardFrontier, SearchFrontier backwardFrontier, double weight, V source,
            V commonVertex, V sink) {
        LinkedList<E> edgeList = new LinkedList<>();
        LinkedList<V> vertexList = new LinkedList<>();

        // add common vertex
        vertexList.add(commonVertex);

        // traverse forward path
        V v = commonVertex;
        while (true) {
            E e = forwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addFirst(e);
            v = Graphs.getOppositeVertex(forwardFrontier.graph, e, v);
            vertexList.addFirst(v);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            E e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addLast(e);
            v = Graphs.getOppositeVertex(backwardFrontier.graph, e, v);
            vertexList.addLast(v);
        }

        return new GraphWalk<>(graph, source, sink, vertexList, edgeList, weight);
    }

    /**
     * Helper class to maintain the search frontier
     */
    class SearchFrontier {
        /**
         * Frontier`s graph.
         */
        final Graph<V, E> graph;
        /**
         * End vertex of the frontier.
         */
        final V endVertex;

        /**
         * Open nodes of the frontier.
         */
        final FibonacciHeap<V> openList;
        final Map<V, FibonacciHeapNode<V>> vertexToHeapNodeMap;
        /**
         * Closed nodes of the frontier.
         */
        final Set<V> closedList;

        /**
         * Tentative distance to the vertices in tha graph computed so far.
         */
        final Map<V, Double> gScoreMap;
        /**
         * Predecessor map.
         */
        final Map<V, E> cameFrom;

        SearchFrontier(Graph<V, E> graph, V endVertex) {
            this.graph = graph;
            this.endVertex = endVertex;
            openList = new FibonacciHeap<>();
            vertexToHeapNodeMap = new HashMap<>();
            closedList = new HashSet<>();
            gScoreMap = new HashMap<>();
            cameFrom = new HashMap<>();
        }

        void updateDistance(V v, E e, double tentativeGScore, double fScore) {
            if (vertexToHeapNodeMap.containsKey(v)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(v)) {// Ignore path since it is non-improving
                    return;
                }

                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);

                if (closedList.contains(v)) { // it's in the closed list. Move node back to
                    // open list, since we discovered a shorter path to this node
                    closedList.remove(v);
                    openList.insert(vertexToHeapNodeMap.get(v), fScore);
                } else { // It's in the open list
                    openList.decreaseKey(vertexToHeapNodeMap.get(v), fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(v);
                openList.insert(heapNode, fScore);
                vertexToHeapNodeMap.put(v, heapNode);
            }
        }

        double getDistance(V v) {
            Double distance = gScoreMap.get(v);
            if (distance == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                return distance;
            }
        }

        E getTreeEdge(V v) {
            E e = cameFrom.get(v);
            if (e == null) {
                return null;
            } else {
                return e;
            }
        }
    }
}
