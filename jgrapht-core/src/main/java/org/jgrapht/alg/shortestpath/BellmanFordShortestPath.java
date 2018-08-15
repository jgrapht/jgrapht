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

import org.jgrapht.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * The Bellman-Ford algorithm.
 *
 * <p>
 * Computes shortest paths from a single source vertex to all other vertices in a weighted graph.
 * The Bellman-Ford algorithm supports graphs with negative weight cycles.
 *
 * <p>
 * The running time is $O(|E||V|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Miron Balcerzak
 */
public class BellmanFordShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    private final Comparator<Double> comparator;

    private boolean allowNegativeWeightCycle = false;

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

        // Initialize distance and predecessor.
        Set<V> vertices = graph.vertexSet();
        int n = vertices.size();
        Map<V, Double> distances = new HashMap<>();
        Map<V, E> predecessor = new HashMap<>();
        for (V v : vertices) {
            distances.put(v, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0d);

        // Relax edges
        for (int i = 0; i < n - 1; i++) {
            boolean updateDone = false;
            for (V vSource : vertices) {
                Double sourceDist = distances.get(vSource);
                if (sourceDist == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (E e : graph.outgoingEdgesOf(vSource)) {
                    V vTarget = Graphs.getOppositeVertex(graph, e, vSource);
                    Double newTargetDist = sourceDist + graph.getEdgeWeight(e);
                    if (comparator.compare(newTargetDist, distances.get(vTarget)) < 0) {
                        distances.put(vTarget, newTargetDist);
                        predecessor.put(vTarget, e);
                        updateDone = true;
                    }
                }
            }
            if (!updateDone) {
                break;
            }
        }

        // Check for negative cycles
        boolean negativeCycleDetected = false;
        for (V vSource : distances.keySet()) {
            for (E e : graph.outgoingEdgesOf(vSource)) {
                V vTarget = graph.getEdgeTarget(e);
                Double newTargetDist = distances.get(vSource) + graph.getEdgeWeight(e);
                if (comparator.compare(newTargetDist, distances.get(vTarget)) < 0) {
                    negativeCycleDetected = true;
                }
            }
        }

        if (negativeCycleDetected && !allowNegativeWeightCycle) {
            throw new RuntimeException(GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE);
        }

        // Transform result
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = new LinkedHashMap<>();
        for (V v : vertices) {
            distanceAndPredecessorMap.put(v, Pair.of(distances.get(v), predecessor.get(v)));
        }
        return new TreeSingleSourcePathsImpl<>(graph, source, distanceAndPredecessorMap);
    }

    /**
     * Permits negative weight cycles in a graph. Without that algo will raise an exception.
     *
     * @return returns instance of algo
     */
    public BellmanFordShortestPath<V, E> allowNegativeWeightCycle()
    {
        this.allowNegativeWeightCycle = true;
        return this;
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
