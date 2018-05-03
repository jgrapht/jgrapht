/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.shortestpath;

import java.lang.reflect.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;

/**
 * The Bellman-Ford algorithm.
 *
 * <p>
 * Computes shortest paths from a single source vertex to all other vertices in a weighted graph.
 * The Bellman-Ford algorithm supports negative edge weights. Negative weight cycles are not allowed
 * and will be reported by the algorithm. This implies that negative edge weights are not allowed in
 * undirected graphs.
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
    extends BaseShortestPathAlgorithm<V, E>
{
    private final Comparator<Double> comparator;

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
        super(graph);
        this.comparator = new ToleranceDoubleComparator(epsilon);
    }

    /**
     * {@inheritDoc}
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
         * set of vertices while the second if the set for the subsequent iteration.
         */
        Set<V>[] updated = (Set<V>[]) Array.newInstance(Set.class, 2);
        updated[0] = new LinkedHashSet<>();
        updated[1] = new LinkedHashSet<>();
        int curUpdated = 0;
        updated[curUpdated].add(source);

        /*
         * Relax edges.
         */
        for (int i = 0; i < n - 1; i++) {
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
         * Check for negative cycles
         */
        for (V v : updated[curUpdated]) {
            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                double newDist = distance.get(v) + graph.getEdgeWeight(e);
                if (comparator.compare(newDist, distance.get(u)) < 0) {
                    throw new RuntimeException(GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE);
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
}
