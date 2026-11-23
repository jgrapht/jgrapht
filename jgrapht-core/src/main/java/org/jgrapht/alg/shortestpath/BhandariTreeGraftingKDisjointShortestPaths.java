/*
 * (C) Copyright 2018-2018, by Benjamin Krogh and Contributors.
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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

/**
 * An implementation of Bhandari algorithm for finding $K$ edge-<em>disjoint</em> shortest paths.
 * The algorithm determines the $k$ edge-disjoint shortest simple paths in increasing order of
 * weight. Weights can be negative (but no negative cycle is allowed). Only directed simple graphs
 * are allowed.
 *
 * <p>
 * The algorithm is running $k$ sequential Bellman-Ford iterations to find the shortest path at each
 * step. Hence, yielding a complexity of $k$*O(Bellman-Ford).
 * <p>
 * In contrast to BhandariKDisjointShortestPaths, this implementation maintains an internal shortest path tree,
 * that is incrementally updated (tree grafting).
 * For sparse graphs or graphs where the shortest path tree resembles the k-shortest paths, this provides a significant
 * speedup. In the worst case, this implementation should perform equally to BhandariKDisjointShortestPaths.
 *
 * <ul>
 * <li>Bhandari, Ramesh 1999. Survivable networks: algorithms for diverse routing. 477. Springer. p.
 * 46. ISBN 0-7923-8381-8.
 * <li>Iqbal, F. and Kuipers, F. A. 2015.
 * <a href="https://www.nas.ewi.tudelft.nl/people/Fernando/papers/Wiley.pdf"> Disjoint Paths in
 * Networks </a>. Wiley Encyclopedia of Electrical and Electronics Engineering. 1–11.
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Benjamin Krogh
 */
public class BhandariTreeGraftingKDisjointShortestPaths<V, E>
        extends
        BhandariKDisjointShortestPaths<V, E> {

    /**
     * A comparator for path weights
     */
    private final Comparator<Double> comparator;
    private final static double DEFAULT_EPSILON = 0.0001d;
    private final double epsilon;
    /**
     * The distance to each vertex from the source
     */
    private final Map<V, Double> distances = new HashMap<>();

    /**
     * The predecessor edge for each vertex in the shortest path tree of the source
     */
    private final Map<V, E> predecessors = new HashMap<>();

    /**
     * The source vertex of the shortest path tree
     */
    private V shortestPathStartVertex = null;

    /**
     * The sink vertex of the shortest path tree
     */
    private V shortestPathEndVertex = null;

    /**
     * Creates a new instance of the algorithm.
     *
     * @param graph graph on which shortest paths are searched.
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     * @throws IllegalArgumentException if the graph is not simple.
     */
    public BhandariTreeGraftingKDisjointShortestPaths(Graph<V, E> graph) {
        this(graph, DEFAULT_EPSILON);
    }

    /**
     * Creates a new instance of the algorithm.
     *
     * @param graph   graph on which shortest paths are searched.
     * @param epsilon the minimum difference between the weight of two paths to be considered different
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     * @throws IllegalArgumentException if the graph is not simple.
     */
    public BhandariTreeGraftingKDisjointShortestPaths(Graph<V, E> graph, double epsilon) {
        super(graph);
        this.comparator = new ToleranceDoubleComparator(epsilon);
        this.epsilon = epsilon;
    }

    @Override
    protected void transformGraph(List<E> previousPath) {
        // First we prune the shortest path sub-tree that is invalidated when we remove the edges in previousPath
        E edge = previousPath.get(0);
        V target = workingGraph.getEdgeTarget(edge);

        // Set to hold all vertices that has been pruned from the shortest path tree
        // This set is used to selectively recompute the pruned part of the shortest path tree
        final Set<V> prunedVertices = recursivelyClearDistanceAndPredecessor(target);

        // Then we transform the graph as described in the Bhandari algorithm
        super.transformGraph(previousPath);

        // Finally, we need to recompute the pruned shortest path sub-tree
        updateVertices(prunedVertices);
    }

    /**
     * Recursively clear the shortest path sub-tree rooted in vertex
     *
     * @param vertex The root vertex of the shortest path sub-tree to clear
     * @return A set of all vertices that has been pruned from the shortest path tree
     */
    private Set<V> recursivelyClearDistanceAndPredecessor(V vertex) {
        Set<V> prunedVertices = new HashSet<>();

        // If called on the source vertex do nothing.
        // The path from source to source is empty, and defined to have zero weight (otherwise the graph would have a negative cycle)
        // If the graph has a negative cycle, the Bellman-Ford search in the initialization of the shortest path tree would fail
        if (vertex.equals(shortestPathStartVertex)) {
            return prunedVertices;
        }

        recursivelyClearDistanceAndPredecessor(vertex, prunedVertices);
        return prunedVertices;
    }

    /**
     * Recursively clear the shortest path sub-tree rooted in vertex
     *
     * @param vertex         The root vertex of the shortest path sub-tree to clear
     * @param prunedVertices A set of all vertices that has been pruned from the shortest path tree
     */
    private void recursivelyClearDistanceAndPredecessor(V vertex, Set<V> prunedVertices) {
        prunedVertices.add(vertex);

        // Clear distance and predecessor of vertex
        distances.put(vertex, Double.POSITIVE_INFINITY);
        predecessors.remove(vertex);

        //Finally we select the outgoing edges of vertex, and find those that are used in the shortest path sub-tree rooted in vertex
        final Set<E> outgoingEdges = workingGraph.outgoingEdgesOf(vertex);
        for (E outgoingEdge : outgoingEdges) {
            final V target = workingGraph.getEdgeTarget(outgoingEdge);
            final E predecessorEdge = predecessors.get(target);
            if (predecessorEdge != null && predecessorEdge.equals(outgoingEdge)) {
                // Target has vertex as predecessor in shortest path tree, so we recurse on it
                recursivelyClearDistanceAndPredecessor(target, prunedVertices);
            }
        }
    }

    /**
     * Find sub-set of prunedVertices from which a Bellman-Ford search can start
     *
     * @param prunedVertices A set of vertices pruned from the current shortest path tree
     * @return The sub-set of vertices of prunedVertices from which a Bellman-Ford search can proceed
     */
    private Set<V> getBfsSourceVertices(Set<V> prunedVertices) {
        final Set<V> bfsSourceVertices = new HashSet<>();

        for (V vertex : prunedVertices) {
            // The current best distance found to vertex
            // We use this value to prune candidates
            double currentTargetDistance = Double.POSITIVE_INFINITY;

            final Set<E> incomingEdges = workingGraph.incomingEdgesOf(vertex);
            for (E incomingEdge : incomingEdges) {
                final V source = workingGraph.getEdgeSource(incomingEdge);
                if (source.equals(shortestPathEndVertex)) {
                    // No shortest path can go over the shortestPathEndVertex
                    continue;
                }

                // distances *always* has an entry per vertex, so this call is guaranteed to provide a non-null value
                // For pruned vertices, Double.POSITIVE_INFINITY is returned
                double distance = distances.get(source);

                double newDistance = distance + workingGraph.getEdgeWeight(incomingEdge);
                if (comparator.compare(newDistance, currentTargetDistance) >= 0) {
                    // If the currentTargetDistance is smaller than or equal to newDistance, we already have a candidate that is at least as good
                    continue;
                }

                // We have a better candidate, so we update the best distance
                currentTargetDistance = newDistance;
                if (vertex.equals(shortestPathStartVertex)) {
                    continue;
                }

                // We updated the shortest path tree for vertex. Note, that newDistance may not be the actual shortest path distance from the source
                // That is only guaranteed after the Bellmand-Ford search has completed
                distances.put(vertex, newDistance);
                predecessors.put(vertex, incomingEdge);

                if (vertex.equals(shortestPathEndVertex)) {
                    // There is no need to add shortestPathEndVertex to the bfsSourceVertices as any shortest path over the sink cannot be included
                    // k-shortest path algorithm, unless there is a negative cycle in the graph (which is caught in the initialization of the shortest path tree)
                    continue;
                }
                bfsSourceVertices.add(vertex);
            }
        }
        return bfsSourceVertices;
    }

    private void updateVertices(Set<V> prunedVertices) {
        // bfsSourceVertices are used to bootstrap a Bellman-Ford search
        Set<V> bfsSourceVertices = getBfsSourceVertices(prunedVertices);

        // We remove shortestPathStartVertex and shortestPathEndVertex from bfsSourceVertices, in case they have been added
        // We never need to relax edges from these vertices
        bfsSourceVertices.remove(shortestPathEndVertex);
        bfsSourceVertices.remove(shortestPathStartVertex);

        // This is the vertices that are currently queued for relaxing
        final ArrayDeque<V> queue = new ArrayDeque<>(bfsSourceVertices);

        // We now re-add shortestPathStartVertex and shortestPathEndVertex to the bfsSourceVertices, which prevents relaxing edges from these two vertices
        bfsSourceVertices.add(shortestPathEndVertex);
        bfsSourceVertices.add(shortestPathStartVertex);

        int vertexCount = workingGraph.vertexSet().size();

        // maxIterations is used as an upper bound on iterations
        // If a negative cycle is introduced (e.g., by a weird combination of floating point rounding errors and too small epsilon in ToleranceDoubleComparator)
        // we can avoid an infinite loop by comparing the iteration count with this number
        final int maxIterations = vertexCount * vertexCount + 1;

        // A fairly standard Bellman-Ford search loop
        for (int iterations = 0; iterations < maxIterations && !queue.isEmpty(); iterations++) {
            V vertex = queue.poll();
            bfsSourceVertices.remove(vertex);

            final double distance = distances.get(vertex);
            Set<E> outgoingEdges = workingGraph.outgoingEdgesOf(vertex);
            for (E outgoingEdge : outgoingEdges) {
                V targetVertex = workingGraph.getEdgeTarget(outgoingEdge);
                double currentTargetDistance = distances.get(targetVertex);
                double edgeWeight = workingGraph.getEdgeWeight(outgoingEdge);
                double newDistance = distance + edgeWeight;
                if (comparator.compare(newDistance, currentTargetDistance) >= 0) {
                    continue;
                }

                // Update shortest path tree according to the new weights
                distances.put(targetVertex, newDistance);
                predecessors.put(targetVertex, outgoingEdge);

                // We use bfsSourceVertices to prevent re-adding vertices that are already enqueued
                boolean added = bfsSourceVertices.add(targetVertex);
                if (added) {
                    queue.add(targetVertex);
                }
            }
        }

        if (!queue.isEmpty()) {
            throw new NegativeCycleDetectedException("A negative loop has been introduced in the graph which should be impossible. This suggests that the epsilon used in the ToleranceDoubleComparator is too small");
        }
    }

    /**
     * Initialize the shortest path tree defined by distances and predecessors arguments.
     *
     * @param <V>          Vertex type
     * @param <E>          Edge type
     * @param graph        The graph
     * @param startVertex  The root of the desired shortest path tree
     * @param distances    The map between vertices and distances
     * @param predecessors The predecessor map
     * @param epsilon      The epsilon tolerance parameter used in the Bellman-Ford search
     */
    private static <V, E> void initializeShortestPathMap(Graph<V, E> graph, V startVertex, Map<V, Double> distances, Map<V, E> predecessors, double epsilon) {
        BellmanFordShortestPath<V, E> bfs = new BellmanFordShortestPath<>(graph, epsilon);
        TreeSingleSourcePathsImpl<V, E> singleSourcePaths = (TreeSingleSourcePathsImpl<V, E>) bfs.getPaths(startVertex);
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = singleSourcePaths.
                getDistanceAndPredecessorMap();
        distanceAndPredecessorMap.forEach((vertex, distancePredecessor) -> {
            Double distance = distancePredecessor.getFirst();
            E edge = distancePredecessor.getSecond();
            distances.put(vertex, distance);
            predecessors.put(vertex, edge);
        });
    }

    @Override
    protected GraphPath<V, E> calculateShortestPath(V startVertex, V endVertex) {
        Objects.requireNonNull(startVertex, "startVertex cannot be null");
        Objects.requireNonNull(endVertex, "endVertex cannot be null");
        if (startVertex.equals(endVertex)) {
            throw new RuntimeException("startVertex and endVertex cannot be identical");
        }

        // If either shortestPathStartVertex or shortestPathEndVertex are null, we need to initialize the shortest path tree
        if (shortestPathStartVertex == null || shortestPathEndVertex == null) {
            initializeShortestPathMap(this.workingGraph, startVertex, distances, predecessors, this.epsilon);
            shortestPathStartVertex = startVertex;
            shortestPathEndVertex = endVertex;
        }

        // List to hold the edges in the shortest path from source to sink
        final List<E> shortestPathEdges = new ArrayList<>();
        V currentVertex = endVertex;

        final int maxEdgesInShortestPath = workingGraph.vertexSet().size();
        double pathWeight = 0;
        while (!currentVertex.equals(startVertex)) {
            final E predecessorEdge = predecessors.get(currentVertex);
            if (shortestPathEdges.size() > maxEdgesInShortestPath) {
                throw new RuntimeException("Logical error detected - The shortest path from start to end vertex visits more vertices than included in the graph. This suggests the epsilon used in the ToleranceDoubleComparator is too low");
            }

            if (predecessorEdge == null) {
                // predecessorEdge may be null, but only if we have the source vertex or the sink vertex.
                // If predecessorEdge is null and we have the sink vertex, there are no paths from source to sink
                if (shortestPathEdges.size() > 0) {
                    // Logical error
                    throw new RuntimeException("Unable to connect a path from endVertex to startVertex, but a partial path exists which is impossible");
                }
                break;
            }
            shortestPathEdges.add(predecessorEdge);
            pathWeight += workingGraph.getEdgeWeight(predecessorEdge);
            currentVertex = workingGraph.getEdgeSource(predecessorEdge);
        }

        if (shortestPathEdges.size() == 0) {
            return null;
        }

        // The retrieved edges are from endVertex to startVertex, so we need to reverse them
        Collections.reverse(shortestPathEdges);
        return new GraphWalk<>(workingGraph, startVertex, endVertex, shortestPathEdges, pathWeight);
    }


    @Override
    public List<GraphPath<V, E>> getPaths(V startVertex, V endVertex, int k) {
        distances.clear();
        predecessors.clear();
        this.shortestPathStartVertex = this.shortestPathEndVertex = null;
        return super.getPaths(startVertex, endVertex, k);
    }
}

