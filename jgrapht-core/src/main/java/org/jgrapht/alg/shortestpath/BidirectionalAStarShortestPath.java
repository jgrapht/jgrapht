/*
 * (C) Copyright 2019-2020, by Semen Chudakov and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.util.*;
import java.util.function.*;

/**
 * A bidirectional version of A* algorithm.
 *
 * <p>
 * See the Wikipedia article for details and references about
 * <a href="https://en.wikipedia.org/wiki/Bidirectional_search">bidirectional search</a>. This
 * technique does not change the worst-case behavior of the algorithm but reduces, in some cases,
 * the number of visited vertices in practice.
 * <p>
 * The algorithm was first introduced in Ira Sheldon Pohl. 1969. Bi-Directional and Heuristic Search
 * in Path Problems. Ph.D. Dissertation. Stanford University, Stanford, CA, USA. AAI7001588.
 * <p>
 * The implementation uses two termination criteria depending on if the provided heuristic is
 * consistent or not. Both criteria are based on the shortest path distance $\mu$ seen thus far in
 * the search. Initially, in both cases the algorithm sets $\mu=\infty$. Whenever the search updates
 * the information about the vertex $v$, it sets $\mu = min\{\mu; g_f(v) + g_b(v)\}$, where $g_f(v)$
 * is the current best-known path cost from $source$ to $sink$ and $g_b(v)$ is the current
 * best-known path cost from $sink$ to $source$.
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
    BaseBidirectionalShortestPathAlgorithm<V, E>
{
    /**
     * Heuristic used for forward search.
     */
    private AStarAdmissibleHeuristic<V> forwardHeuristic;
    /**
     * Heuristic used for backward search. In general $d(u,v)\neq d(v,u)$, e.g. in the directed
     * graphs.
     */
    private AStarAdmissibleHeuristic<V> backwardHeuristic;
    private final Supplier<AddressableHeap<Double, V>> heapSupplier;

    /**
     * Constructs a new instance of the algorithm for a given graph and heuristic.
     *
     * @param graph the graph
     * @param heuristic heuristic that estimates distances between nodes
     */
    public BidirectionalAStarShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> heuristic)
    {
        this(graph, heuristic, PairingHeap::new);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, heuristic and heap supplier.
     *
     * @param graph the graph
     * @param heuristic heuristic that estimates distances between nodes
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public BidirectionalAStarShortestPath(
        Graph<V, E> graph, AStarAdmissibleHeuristic<V> heuristic,
        Supplier<AddressableHeap<Double, V>> heapSupplier)
    {
        super(graph);
        this.forwardHeuristic =
            Objects.requireNonNull(heuristic, "Heuristic function cannot be null!");
        if (graph.getType().isDirected()) {
            backwardHeuristic = new ReversedGraphHeuristic(
                Objects.requireNonNull(heuristic, "Heuristic function cannot be null!"));
        } else {
            this.backwardHeuristic =
                Objects.requireNonNull(heuristic, "Heuristic function cannot be null!");
        }
        this.heapSupplier = Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
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
        AStarSearchFrontier forwardFrontier =
            new AStarSearchFrontier(graph, sink, forwardHeuristic);
        AStarSearchFrontier backwardFrontier;
        if (graph.getType().isDirected()) {
            backwardFrontier =
                new AStarSearchFrontier(new EdgeReversedGraph<>(graph), source, backwardHeuristic);
        } else {
            backwardFrontier = new AStarSearchFrontier(graph, source, backwardHeuristic);
        }

        forwardFrontier.updateDistance(source, null, 0.0, 0.0);
        backwardFrontier.updateDistance(sink, null, 0.0, 0.0);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        V bestPathCommonVertex = null;

        AStarSearchFrontier frontier = forwardFrontier;
        AStarSearchFrontier otherFrontier = backwardFrontier;

        TerminationCriterion condition;
        if (forwardHeuristic.isConsistent(graph)) {
            double sourceTargetEstimate = forwardFrontier.heuristic.getCostEstimate(source, sink);
            condition = new ConsistentTerminationCriterion(
                forwardFrontier, backwardFrontier, sourceTargetEstimate);
        } else {
            condition = new InconsistentTerminationCriterion(forwardFrontier, backwardFrontier);
        }

        while (true) {
            // stopping condition
            if (condition.stop(bestPath)) {
                break;
            }

            // frontier scan
            AddressableHeap.Handle<Double, V> node = frontier.openList.deleteMin();
            V v = node.getValue();

            for (E edge : frontier.graph.outgoingEdgesOf(v)) {
                V successor = Graphs.getOppositeVertex(frontier.graph, edge, v);

                if (successor.equals(v)) { // Ignore self-loop
                    continue;
                }

                double edgeWeight = frontier.graph.getEdgeWeight(edge);
                double gScore_current = frontier.getDistance(v);
                double tentativeGScore = gScore_current + edgeWeight;
                double fScore = tentativeGScore
                    + frontier.heuristic.getCostEstimate(successor, frontier.endVertex);

                frontier.updateDistance(successor, edge, tentativeGScore, fScore);

                // check if best path can be updated
                double pathDistance =
                    gScore_current + edgeWeight + otherFrontier.getDistance(successor);
                if (pathDistance < bestPath) {
                    bestPath = pathDistance;
                    bestPathCommonVertex = successor;
                }
            }
            // close current vertex
            frontier.closedList.add(v);

            // swap frontiers
            if (frontier.openList.size() > otherFrontier.openList.size()) {
                AStarSearchFrontier tmpFrontier = frontier;
                frontier = otherFrontier;
                otherFrontier = tmpFrontier;
            }
        }

        // create path if found
        if (Double.isFinite(bestPath)) {
            return createPath(
                forwardFrontier, backwardFrontier, bestPath, source, bestPathCommonVertex, sink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    /**
     * Maintains search frontier during shortest path computation.
     */
    class AStarSearchFrontier
        extends
        BaseSearchFrontier<V, E>
    {
        /**
         * End vertex of the frontier.
         */
        final V endVertex;
        /**
         * Heuristic used in this frontier.
         */
        final AStarAdmissibleHeuristic<V> heuristic;
        /**
         * Open nodes of the frontier.
         */
        final AddressableHeap<Double, V> openList;
        final Map<V, AddressableHeap.Handle<Double, V>> vertexToHeapNodeMap;
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

        AStarSearchFrontier(Graph<V, E> graph, V endVertex, AStarAdmissibleHeuristic<V> heuristic)
        {
            super(graph);
            this.endVertex = endVertex;
            this.heuristic = heuristic;
            openList = heapSupplier.get();
            vertexToHeapNodeMap = new HashMap<>();
            closedList = new HashSet<>();
            gScoreMap = new HashMap<>();
            cameFrom = new HashMap<>();
        }

        void updateDistance(V v, E e, double tentativeGScore, double fScore)
        {
            AddressableHeap.Handle<Double, V> node = vertexToHeapNodeMap.get(v);
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
                    openList.insert(fScore, v);
                } else { // It's in the open list
                    node.decreaseKey(fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);
                node = openList.insert(fScore, v);
                vertexToHeapNodeMap.put(v, node);
            }
        }

        @Override
        double getDistance(V v)
        {
            Double distance = gScoreMap.get(v);
            if (distance == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                return distance;
            }
        }

        @Override
        E getTreeEdge(V v)
        {
            return cameFrom.get(v);
        }
    }

    /**
     * Helper class for backward search, since it should operate on the reversed graph.
     */
    class ReversedGraphHeuristic
        implements
        AStarAdmissibleHeuristic<V>
    {

        private final AStarAdmissibleHeuristic<V> heuristic;

        ReversedGraphHeuristic(AStarAdmissibleHeuristic<V> heuristic)
        {
            this.heuristic = heuristic;
        }

        @Override
        public double getCostEstimate(V sourceVertex, V targetVertex)
        {
            return heuristic.getCostEstimate(targetVertex, sourceVertex);
        }
    }

    /**
     * Termination criterion for the heuristic search.
     */
    abstract class TerminationCriterion
    {
        final AStarSearchFrontier forward;
        final AStarSearchFrontier backward;

        TerminationCriterion(AStarSearchFrontier forward, AStarSearchFrontier backward)
        {
            this.forward = forward;
            this.backward = backward;
        }

        /**
         * Determines if the search should be terminated.
         *
         * @param bestPath length of the shortest path seen so far
         * @return true iff the search should be terminated
         */
        abstract boolean stop(double bestPath);
    }

    /**
     * Termination criterion for the consistent heuristics.
     */
    class ConsistentTerminationCriterion
        extends
        TerminationCriterion
    {
        final double sourceTargetEstimate;

        ConsistentTerminationCriterion(
            AStarSearchFrontier forward, AStarSearchFrontier backward, double sourceTargetEstimate)
        {
            super(forward, backward);
            this.sourceTargetEstimate = sourceTargetEstimate;
        }

        @Override
        boolean stop(double bestPath)
        {
            return forward.openList.isEmpty() || backward.openList.isEmpty()
                || forward.openList.findMin().getKey()
                    + backward.openList.findMin().getKey() >= bestPath + sourceTargetEstimate;
        }
    }

    /**
     * Termination criterion for the inconsistent heuristics.
     */
    class InconsistentTerminationCriterion
        extends
        TerminationCriterion
    {
        InconsistentTerminationCriterion(AStarSearchFrontier forward, AStarSearchFrontier backward)
        {
            super(forward, backward);
        }

        @Override
        boolean stop(double bestPath)
        {
            return forward.openList.isEmpty() || backward.openList.isEmpty()
                || Math
                    .max(
                        forward.openList.findMin().getKey(),
                        backward.openList.findMin().getKey()) >= bestPath;
        }
    }
}
