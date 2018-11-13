/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * The Bellman-Ford algorithm.
 *
 * <p>
 * Computes shortest paths from a single source vertex to all other vertices in a weighted graph.
 * The Bellman-Ford algorithm supports negative edge weights.
 * 
 * <p>
 * Negative weight cycles are not allowed and will be reported by the algorithm. This implies that
 * negative edge weights are not allowed in undirected graphs. In such cases the code will throw an
 * exception of type {@link NegativeCycleDetectedException} which will contain the detected negative
 * weight cycle. Note that the algorithm will not report or find negative weight cycles which are
 * not reachable from the source vertex.
 *
 * <p>
 * The running time is $O(|E||V|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class BellmanFordShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    protected final Comparator<Double> comparator;
    protected final int maxHops;

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     */
    public BellmanFordShortestPath(Graph<V, E> graph)
    {
        this(graph, ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     * @param epsilon tolerance when comparing floating point values
     */
    public BellmanFordShortestPath(Graph<V, E> graph, double epsilon)
    {
        this(graph, ToleranceDoubleComparator.DEFAULT_EPSILON, Integer.MAX_VALUE);
    }

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     * @param epsilon tolerance when comparing floating point values
     * @param maxHops execute the algorithm for at most this many iterations. If this is smaller
     *        than the number of vertices, then the negative cycle detection feature is disabled.
     * @throws IllegalArgumentException if the number of maxHops is not positive
     */
    public BellmanFordShortestPath(Graph<V, E> graph, double epsilon, int maxHops)
    {
        super(graph);
        this.comparator = new ToleranceDoubleComparator(epsilon);
        if (maxHops < 1) {
            throw new IllegalArgumentException("Number of hops must be positive");
        }
        this.maxHops = maxHops;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NegativeCycleDetectedException in case a negative weight cycle is detected
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        return getPaths(source).getPath(sink);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NegativeCycleDetectedException in case a negative weight cycle is detected
     */
    @Override
    @SuppressWarnings("unchecked")
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        /*
         * Initialize distance and predecessor.
         */
        int n = graph.vertexSet().size();
        Map<V, Double> distance = new HashMap<>();
        Map<V, E> pred = new HashMap<>();
        for (V v : graph.vertexSet()) {
            distance.put(v, Double.POSITIVE_INFINITY);
        }
        distance.put(source, 0d);

        /*
         * Maintain two sets of vertices whose edges need relaxation. The first set is the current
         * set of vertices while the second is the set for the subsequent iteration.
         */
        Set<V>[] updated = (Set<V>[]) Array.newInstance(Set.class, 2);
        updated[0] = new LinkedHashSet<>();
        updated[1] = new LinkedHashSet<>();
        int curUpdated = 0;
        updated[curUpdated].add(source);

        /*
         * Relax edges.
         */
        for (int i = 0; i < Math.min(n - 1, maxHops); i++) {
            Set<V> curVertexSet = updated[curUpdated];
            Set<V> nextVertexSet = updated[(curUpdated + 1) % 2];

            for (V v : curVertexSet) {
                for (E e : graph.outgoingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    double newDist = distance.get(v) + graph.getEdgeWeight(e);
                    if (comparator.compare(newDist, distance.get(u)) < 0) {
                        distance.put(u, newDist);
                        pred.put(u, e);
                        nextVertexSet.add(u);
                    }
                }
            }

            // swap next with current
            curVertexSet.clear();
            curUpdated = (curUpdated + 1) % 2;

            // stop if no relaxation
            if (nextVertexSet.isEmpty()) {
                break;
            }
        }

        /*
         * Check for negative cycles. The user can disable this by providing a maxHops parameter
         * smaller than the number of vertices.
         */
        if (maxHops >= n) {
            for (V v : updated[curUpdated]) {
                for (E e : graph.outgoingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    double newDist = distance.get(v) + graph.getEdgeWeight(e);
                    if (comparator.compare(newDist, distance.get(u)) < 0) {
                        // record update for negative cycle computation
                        pred.put(u, e);
                        throw new NegativeCycleDetectedException(
                            GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE, computeNegativeCycle(e, pred));
                    }
                }
            }
        }

        /*
         * Transform result
         */
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = new HashMap<>();
        for (V v : graph.vertexSet()) {
            distanceAndPredecessorMap.put(v, Pair.of(distance.get(v), pred.get(v)));
        }
        return new TreeSingleSourcePathsImpl<>(graph, source, distanceAndPredecessorMap);
    }

    /**
     * Find a path between two vertices.
     * 
     * @param graph the graph to be searched
     * @param source the vertex at which the path should start
     * @param sink the vertex at which the path should end
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return a shortest path, or null if no path exists
     */
    public static <V, E> GraphPath<V, E> findPathBetween(Graph<V, E> graph, V source, V sink)
    {
        return new BellmanFordShortestPath<>(graph).getPath(source, sink);
    }

    /**
     * Computes a negative weight cycle assuming that the algorithm has already determined that it
     * exists.
     * 
     * @param edge an edge which we know that belongs to the negative weight cycle
     * @param pred the predecessor array
     * 
     * @return the negative weight cycle
     */
    private GraphPath<V, E> computeNegativeCycle(E edge, Map<V, E> pred)
    {
        // find a vertex of the cycle
        Set<V> visited = new HashSet<>();
        V start = graph.getEdgeTarget(edge);
        visited.add(start);
        V cur = Graphs.getOppositeVertex(graph, edge, start);

        while (!visited.contains(cur)) {
            visited.add(cur);
            E e = pred.get(cur);
            cur = Graphs.getOppositeVertex(graph, e, cur);
        }

        // now build the actual cycle
        List<E> cycle = new ArrayList<>();
        double weight = 0d;
        start = cur;
        do {
            E e = pred.get(cur);
            cycle.add(e);
            weight += graph.getEdgeWeight(e);
            cur = Graphs.getOppositeVertex(graph, e, cur);
        } while (cur != start);
        Collections.reverse(cycle);

        return new GraphWalk<>(graph, start, start, cycle, weight);
    }
}
