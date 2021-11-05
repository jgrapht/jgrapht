/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.scoring;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.EdgeScoringAlgorithm;
import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.PairingHeap;

/**
 * Edge betweenness centrality.
 * 
 * <p>
 * A natural extension of betweenness to edges by counting the total shortest paths that pass
 * through an edge. See the paper: Ulrik Brandes: On Variants of Shortest-Path Betweenness
 * Centrality and their Generic Computation. Social Networks 30(2):136-145, 2008, for a nice
 * discussion of different variants of betweenness centrality. Note that this implementation does
 * not work for graphs which have multiple edges. Self-loops do not influence the result and are
 * thus ignored.
 * 
 * <p>
 * This implementation allows the user to compute centrality contributions only from a subset of the
 * graph vertices, i.e. to start shortest path computations only from a subset of the vertices. This
 * allows centrality approximations in big graphs. Note that in this case, the user is responsible
 * for any normalization necessary due to duplicate shortest paths that might occur in undirected
 * graphs.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class EdgeBetweennessCentrality<V, E>
    implements
    EdgeScoringAlgorithm<E, Double>
{
    private final Graph<V, E> graph;
    private final Iterable<V> startVertices;
    private final boolean divideByTwo;
    private Map<E, Double> scores;
    private final OverflowStrategy overflowStrategy;

    /**
     * Strategy followed when counting paths.
     */
    public enum OverflowStrategy
    {
        /**
         * Do not check for overflow in counters. This means that on certain instances the results
         * might be wrong due to counters being too large to fit in a long.
         */
        IGNORE_OVERFLOW,
        /**
         * An exception is thrown if an overflow in counters is detected.
         */
        THROW_EXCEPTION_ON_OVERFLOW,
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     */
    public EdgeBetweennessCentrality(Graph<V, E> graph)
    {
        this(graph, OverflowStrategy.IGNORE_OVERFLOW, null);
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     * @param overflowStrategy strategy to use if overflow is detected
     */
    public EdgeBetweennessCentrality(Graph<V, E> graph, OverflowStrategy overflowStrategy)
    {
        this(graph, overflowStrategy, null);
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     * @param overflowStrategy strategy to use if overflow is detected
     * @param startVertices vertices from which to start shortest path computations. This parameter
     *        allows the user to compute edge centrality contributions only from a subset of the
     *        vertices of the graph. If null the whole graph vertex set is used.
     */
    public EdgeBetweennessCentrality(
        Graph<V, E> graph, OverflowStrategy overflowStrategy, Iterable<V> startVertices)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (GraphTests.hasMultipleEdges(graph)) {
            throw new IllegalArgumentException("Graphs with multiple edges not supported");
        }
        this.scores = null;
        this.overflowStrategy = overflowStrategy;
        if (startVertices == null) {
            this.startVertices = graph.vertexSet();
            // divide by two only if all pairs are used
            this.divideByTwo = graph.getType().isUndirected();
        } else {
            this.startVertices = startVertices;
            // the user is responsible for duplicate shortest paths
            this.divideByTwo = false;
        }
    }

    @Override
    public Map<E, Double> getScores()
    {
        if (scores == null) {
            scores = graph.getType().isWeighted() ? new WeightedAlgorithm().getScores()
                : new Algorithm().getScores();
        }
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public Double getEdgeScore(E e)
    {
        if (!graph.containsEdge(e)) {
            throw new IllegalArgumentException("Cannot return score of unknown edge");
        }
        if (scores == null) {
            scores = graph.getType().isWeighted() ? new WeightedAlgorithm().getScores()
                : new Algorithm().getScores();
        }
        return scores.get(e);
    }

    /*
     * The basic algorithm
     */
    private class Algorithm
    {
        protected Map<E, Double> scores = new HashMap<>();
        protected Deque<V> stack = new ArrayDeque<>();

        public Map<E, Double> getScores()
        {
            for (E e : graph.iterables().edges()) {
                scores.put(e, 0d);
            }
            for (V v : startVertices) {
                singleVertexUpdate(v);
            }
            if (divideByTwo) {
                scores.forEach((e, score) -> scores.put(e, score / 2d));
            }
            return scores;
        }

        protected void singleVertexUpdate(V source)
        {
            // initialization
            Map<V, List<E>> pred = new HashMap<>();
            Map<V, Double> dist = new HashMap<>();
            Map<V, Long> sigma = new HashMap<>();
            Deque<V> queue = new ArrayDeque<>();

            for (V v : graph.vertexSet()) {
                sigma.put(v, 0l);
            }
            sigma.put(source, 1l);
            dist.put(source, 0d);
            queue.add(source);

            // main loop
            while (!queue.isEmpty()) {
                V v = queue.remove();
                stack.push(v);
                double vDistance = dist.get(v);

                for (E e : graph.outgoingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(graph, e, v);

                    if (w.equals(v)) {
                        // ignore self-loops
                        continue;
                    }

                    // path discovery
                    if (!dist.containsKey(w)) {
                        dist.put(w, vDistance + 1d);
                        queue.add(w);
                    }

                    // path counting
                    double wDistance = dist.get(w);
                    if (Double.compare(wDistance, vDistance + 1d) == 0) {
                        long wCounter = sigma.get(w);
                        long vCounter = sigma.get(v);
                        long sum = wCounter + vCounter;
                        if (overflowStrategy.equals(OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW)
                            && sum < 0)
                        {
                            throw new ArithmeticException("long overflow");
                        }
                        sigma.put(w, sum);
                        pred.computeIfAbsent(w, k -> new ArrayList<>()).add(e);
                    }
                }
            }

            // accumulation
            accumulate(pred, sigma);
        }

        protected void accumulate(Map<V, List<E>> pred, Map<V, Long> sigma)
        {
            Map<V, Double> delta = new HashMap<>();
            for (V v : graph.iterables().vertices()) {
                delta.put(v, 0d);
            }
            while (!stack.isEmpty()) {
                V w = stack.pop();
                List<E> wPred = pred.get(w);
                if (wPred != null) {
                    for (E e : wPred) {
                        V v = Graphs.getOppositeVertex(graph, e, w);
                        double c = (sigma.get(v).doubleValue() / sigma.get(w).doubleValue())
                            * (1 + delta.get(w));
                        scores.put(e, scores.get(e) + c);
                        delta.put(v, delta.get(v) + c);
                    }
                }
            }
        }

    }

    /*
     * Weighted variant where shortest paths are computed using edge weights.
     */
    private class WeightedAlgorithm
        extends
        Algorithm
    {
        @Override
        protected void singleVertexUpdate(V source)
        {
            // initialization
            Map<V, List<E>> pred = new HashMap<>();
            Map<V, AddressableHeap.Handle<Double, V>> dist = new HashMap<>();
            Map<V, Long> sigma = new HashMap<>();
            AddressableHeap<Double, V> heap = new PairingHeap<>();

            for (V v : graph.vertexSet()) {
                sigma.put(v, 0l);
            }
            sigma.put(source, 1l);
            dist.put(source, heap.insert(0d, source));

            // main loop
            while (!heap.isEmpty()) {
                Handle<Double, V> vHandle = heap.deleteMin();
                V v = vHandle.getValue();
                double vDistance = vHandle.getKey();
                stack.push(v);

                for (E e : graph.outgoingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(graph, e, v);

                    if (w.equals(v)) {
                        // ignore self-loops
                        continue;
                    }

                    double eWeight = graph.getEdgeWeight(e);
                    if (eWeight < 0d) {
                        throw new IllegalArgumentException("Negative edge weights are not allowed");
                    }
                    double newDistance = vDistance + eWeight;

                    // path discovery
                    Handle<Double, V> wHandle = dist.get(w);

                    if (wHandle == null) {
                        wHandle = heap.insert(newDistance, w);
                        dist.put(w, wHandle);
                        sigma.put(w, 0l);
                        pred.put(w, new ArrayList<>());
                    } else if (Double.compare(wHandle.getKey(), newDistance) > 0) {
                        wHandle.decreaseKey(newDistance);
                        sigma.put(w, 0l);
                        pred.put(w, new ArrayList<>());
                    }

                    // path counting
                    if (Double.compare(wHandle.getKey(), newDistance) == 0) {
                        long wCounter = sigma.get(w);
                        long vCounter = sigma.get(v);
                        long sum = wCounter + vCounter;
                        if (overflowStrategy.equals(OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW)
                            && sum < 0)
                        {
                            throw new ArithmeticException("long overflow");
                        }
                        sigma.put(w, sum);
                        pred.computeIfAbsent(w, k -> new ArrayList<>()).add(e);
                    }
                }
            }

            // accumulation
            accumulate(pred, sigma);
        }

    }

}
