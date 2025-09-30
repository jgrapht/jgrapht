/*
 * (C) Copyright 2019-2023, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

/**
 * Dial's shortest path algorithm for graphs with non-negative integer edge weights.
 *
 * <p>Complexity: {@code O(m + n * C)}, where {@code C} is the maximum edge weight and
 * {@code n, m} are the numbers of vertices and edges. Dial typically outperforms
 * binary/pairing-heap Dijkstra when weights are small integers.</p>
 *
 * <p><b>Preconditions</b></p>
 * <ul>
 *   <li>All edge weights must be integers in {@code [0, maxEdgeWeight]}.</li>
 *   <li>No negative weights.</li>
 * </ul>
 *
 * <p><b>Notes</b></p>
 * <ul>
 *   <li>Works for directed and undirected graphs (relaxes {@code outgoingEdgesOf}).</li>
 *   <li>Bucket array length is {@code C*(n-1)+1}. If this bound is huge (large {@code C} or {@code n}),
 *       prefer standard Dijkstra.</li>
 * </ul>
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DialShortestPath<V, E> implements ShortestPathAlgorithm<V, E> {

    private final Graph<V, E> graph;
    private final int maxEdgeWeight;

    /**
     * @param graph weighted graph (edge weights must be integers in {@code [0, maxEdgeWeight]})
     * @param maxEdgeWeight maximum allowed edge weight (>= 0)
     * @throws IllegalArgumentException if {@code maxEdgeWeight < 0}
     * @throws NullPointerException if {@code graph} is null
     */
    public DialShortestPath(Graph<V, E> graph, int maxEdgeWeight) {
        if (maxEdgeWeight < 0) {
            throw new IllegalArgumentException("maxEdgeWeight must be >= 0");
        }
        this.graph = Objects.requireNonNull(graph, "graph");
        this.maxEdgeWeight = maxEdgeWeight;
    }

    @Override
    public SingleSourcePaths<V, E> getPaths(V source) {
        Objects.requireNonNull(source, "source");
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Source vertex is not in the graph: " + source);
        }

        final int n = graph.vertexSet().size();
        // Upper bound for any simple path cost
        final int maxDistance = Math.multiplyExact(maxEdgeWeight, Math.max(0, n - 1));

        // Distance & predecessor maps
        final Map<V, Integer> dist = new HashMap<>(n * 2);
        final Map<V, E> pred = new HashMap<>(n * 2);
        for (V v : graph.vertexSet()) dist.put(v, Integer.MAX_VALUE);
        dist.put(source, 0);

        // Buckets[d] holds vertices whose current distance == d. Lazily allocate.
        @SuppressWarnings("unchecked")
        final Deque<V>[] buckets = (Deque<V>[]) new Deque[maxDistance + 1];
        addToBucket(buckets, 0, source);

        int current = 0;
        while (current <= maxDistance) {
            V u = pollFromBucket(buckets, current);
            if (u == null) { current++; continue; }

            final int du = dist.get(u);
            if (du < current) continue; // stale (a shorter key for u was already queued)

            for (E e : graph.outgoingEdgesOf(u)) {
                final V v = Graphs.getOppositeVertex(graph, e, u);
                final int w = validateEdgeWeight(e); // throws if invalid

                final long altL = (long) du + (long) w;   // guard overflow
                if (altL > Integer.MAX_VALUE) continue;
                final int alt = (int) altL;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pred.put(v, e);
                    addToBucket(buckets, alt, v);
                }
            }
        }

        // Convert to Double distances for the paths view
        final Map<V, Double> distD = new HashMap<>(dist.size() * 2);
        for (Map.Entry<V, Integer> en : dist.entrySet()) {
            distD.put(en.getKey(),
                en.getValue() == Integer.MAX_VALUE ? Double.POSITIVE_INFINITY : (double) en.getValue());
        }

        // Provide a minimal SingleSourcePaths backed by our maps
        return new SimpleSingleSourcePaths(source, pred, distD);
    }

    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
        return getPaths(source).getPath(sink);
    }

    @Override
    public double getPathWeight(V source, V sink) { // expected by this JGraphT version
        return getPaths(source).getWeight(sink);     // SingleSourcePaths has getWeight(V)
    }

    // -------------------------- helpers --------------------------

    /**
     * Validate that the edge weight is an integer in {@code [0, maxEdgeWeight]}.
     * @throws IllegalArgumentException if out of range or non-integer
     */
    private int validateEdgeWeight(E e) {
        final double w = graph.getEdgeWeight(e);
        if (w < 0 || w > maxEdgeWeight) {
            throw new IllegalArgumentException(
                "Edge weight out of bounds [0," + maxEdgeWeight + "]: " + w + " (edge " + e + ")");
        }
        if (w != Math.rint(w)) {
            throw new IllegalArgumentException("Non-integer edge weight: " + w + " (edge " + e + ")");
        }
        return (int) Math.round(w);
    }

    private void addToBucket(Deque<V>[] buckets, int idx, V v) {
        if (idx < 0 || idx >= buckets.length) return;
        Deque<V> q = buckets[idx];
        if (q == null) { q = new ArrayDeque<>(); buckets[idx] = q; }
        q.addLast(v);
    }

    private V pollFromBucket(Deque<V>[] buckets, int idx) {
        if (idx < 0 || idx >= buckets.length) return null;
        final Deque<V> q = buckets[idx];
        if (q == null || q.isEmpty()) return null;
        return q.removeFirst();
    }

    /**
     * Minimal {@link SingleSourcePaths} implementation backed by predecessor & distance maps.
     * Uses the outer class's generics to avoid type shadowing/mismatch.
     */
    private final class SimpleSingleSourcePaths implements SingleSourcePaths<V, E> {
        private final V source;
        private final Map<V, E> pred;
        private final Map<V, Double> dist;

        SimpleSingleSourcePaths(V source, Map<V, E> pred, Map<V, Double> dist) {
            this.source = source;
            this.pred = pred;
            this.dist = dist;
        }

        @Override public Graph<V, E> getGraph() { return graph; }
        @Override public V getSourceVertex() { return source; }

        @Override
        public GraphPath<V, E> getPath(V sink) {
            final double d = getWeight(sink);
            if (Double.isInfinite(d)) return null;
            if (Objects.equals(source, sink)) {
                return GraphWalk.singletonWalk(graph, source, 0.0);
            }

            // Reconstruct edge list backward via predecessors
            final List<E> edges = new ArrayList<>();
            V v = sink;
            while (!Objects.equals(v, source)) {
                E e = pred.get(v);
                if (e == null) return null; // defensive
                edges.add(e);
                v = Graphs.getOppositeVertex(graph, e, v);
            }
            Collections.reverse(edges);

            // Build vertex list from edges
            final List<V> vertices = new ArrayList<>(edges.size() + 1);
            v = source;
            vertices.add(v);
            for (E e : edges) {
                v = Graphs.getOppositeVertex(graph, e, v);
                vertices.add(v);
            }
            return new GraphWalk<>(graph, source, sink, vertices, edges, d);
        }

        @Override
        public double getWeight(V sink) { // expected by this JGraphT version
            final Double w = dist.get(sink);
            return w == null ? Double.POSITIVE_INFINITY : w;
        }
    }
}
