/*
 * (C) Copyright 2017-2020, by Assaf Mizrachi and Contributors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import org.apfloat.Apfloat;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

/**
 * Betweenness centrality with arbitrary precision arithmetic.
 * 
 * <p>
 * Computes the betweenness centrality of each vertex of a graph. The betweenness centrality of a
 * node $v$ is given by the expression: $g(v)= \sum_{s \neq v \neq
 * t}\frac{\sigma_{st}(v)}{\sigma_{st}}$ where $\sigma_{st}$ is the total number of shortest paths
 * from node $s$ to node $t$ and $\sigma_{st}(v)$ is the number of those paths that pass through
 * $v$. For more details see
 * <a href="https://en.wikipedia.org/wiki/Betweenness_centrality">wikipedia</a>.
 * 
 * The algorithm is based on
 * <ul>
 * <li>Brandes, Ulrik (2001). "A faster algorithm for betweenness centrality". Journal of
 * Mathematical Sociology. 25 (2): 163–177.</li>
 * </ul>
 *
 * The running time is $O(nm)$ and $O(nm +n^2 \log n)$ for unweighted and weighted graph
 * respectively, where $n$ is the number of vertices and $m$ the number of edges of the graph. The
 * space complexity is $O(n + m)$.
 *
 * Note that this running time assumes that arithmetic is performed between numbers whose
 * representation needs a number of bits which is logarithmic in the instance size. There are
 * instances where this is not true, and thus it is not safe to assume that arithmetic takes 
 * constant time.
 * 
 * This class uses arbitrary precision arithmetic (except for the execution of Dijkstra's algorithm). 
 * The precision can be adjusted by the constructor parameters.  
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 */
public class ApBetweennessCentrality<V, E>
    implements
    VertexScoringAlgorithm<V, Apfloat>
{
    /**
     * Underlying graph
     */
    private final Graph<V, E> graph;
    /**
     * Whether to normalize scores
     */
    private final boolean normalize;
    /**
     * The actual scores
     */
    private Map<V, Apfloat> scores;

    /**
     * Precision for arbitrary precision arithmetic.
     */
    private final long precision;

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     */
    public ApBetweennessCentrality(Graph<V, E> graph)
    {
        this(graph, false);
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     * @param normalize whether to normalize by dividing the closeness by $(n-1) \cdot (n-2)$, where
     *        $n$ is the number of vertices of the graph
     */
    public ApBetweennessCentrality(Graph<V, E> graph, boolean normalize)
    {
        this(graph, normalize, Apfloat.INFINITE);
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     * @param normalize whether to normalize by dividing the closeness by $(n-1) \cdot (n-2)$, where
     *        $n$ is the number of vertices of the graph
     * @param precision precision for arbitrary precision arithmetic
     */
    public ApBetweennessCentrality(Graph<V, E> graph, boolean normalize, long precision)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.scores = null;
        this.normalize = normalize;
        this.precision = precision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Apfloat> getScores()
    {
        if (scores == null) {
            compute();
        }
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat getVertexScore(V v)
    {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        if (scores == null) {
            compute();
        }
        return scores.get(v);
    }

    /**
     * Compute the centrality index
     */
    private void compute()
    {
        // initialize result container
        scores = new HashMap<>();
        graph.iterables().vertices().forEach(v -> scores.put(v, new Apfloat(0l, precision)));

        // compute for each source
        graph.iterables().vertices().forEach(this::compute);

        // For undirected graph, divide scores by two as each shortest path
        // considered twice.
        final Apfloat two = new Apfloat(2l, precision);
        if (!graph.getType().isDirected()) {
            scores.forEach((v, score) -> scores.put(v, score.divide(two)));
        }

        final Apfloat one = new Apfloat(1l, precision);
        if (normalize) {
            long n = graph.iterables().vertexCount();
            if (n > 2) {
                Apfloat apn = new Apfloat(n, precision);
                Apfloat nf = apn.subtract(one).multiply(apn.subtract(two));
                scores.forEach((v, score) -> scores.put(v, score.divide(nf)));
            }
        }
    }

    private void compute(V s)
    {
        // initialize
        ArrayDeque<V> stack = new ArrayDeque<>();
        Map<V, List<V>> predecessors = new HashMap<>();
        graph.iterables().vertices().forEach(w -> predecessors.put(w, new ArrayList<>()));

        // Number of shortest paths from s to v
        Map<V, Apfloat> sigma = new HashMap<>();
        final Apfloat zero = new Apfloat(0l, precision);
        final Apfloat one = new Apfloat(1l, precision);
        graph.iterables().vertices().forEach(t -> sigma.put(t, zero));
        sigma.put(s, one);

        // Distance (weight) of the shortest path from s to v
        Map<V, Double> distance = new HashMap<>();
        graph.iterables().vertices().forEach(t -> distance.put(t, Double.POSITIVE_INFINITY));
        distance.put(s, 0.0);

        MyQueue<V, Double> queue =
            graph.getType().isWeighted() ? new WeightedQueue() : new UnweightedQueue();
        queue.insert(s, 0.0);

        // 1. compute the length and the number of shortest paths between all s to v
        while (!queue.isEmpty()) {
            V v = queue.remove();
            stack.push(v);

            for (E e : this.graph.outgoingEdgesOf(v)) {
                V w = Graphs.getOppositeVertex(this.graph, e, v);
                double eWeight = graph.getEdgeWeight(e);
                if (eWeight < 0.0) {
                    throw new IllegalArgumentException("Negative edge weight not allowed");
                }
                double d = distance.get(v) + eWeight;
                // w found for the first time?
                if (distance.get(w) == Double.POSITIVE_INFINITY) {
                    queue.insert(w, d);
                    distance.put(w, d);
                    sigma.put(w, sigma.get(v));
                    predecessors.get(w).add(v);
                }
                // shortest path to w via v?
                else if (distance.get(w) == d) {
                    // queue.update(w, d);
                    sigma.put(w, sigma.get(w).add(sigma.get(v)));
                    predecessors.get(w).add(v);
                } else if (distance.get(w) > d) {
                    queue.update(w, d);
                    distance.put(w, d);
                    sigma.put(w, sigma.get(v));
                    predecessors.get(w).clear();
                    predecessors.get(w).add(v);
                }
            }
        }

        // 2. sum all pair dependencies.
        // The pair-dependency of s and v in w
        Map<V, Apfloat> dependency = new HashMap<>();
        graph.iterables().vertices().forEach(v -> dependency.put(v, zero));
        // S returns vertices in order of non-increasing distance from s
        while (!stack.isEmpty()) {
            V w = stack.pop();
            for (V v : predecessors.get(w)) {
                dependency
                    .put(
                        v,
                        dependency
                            .get(v).add(
                                sigma
                                    .get(v).divide(sigma.get(w))
                                    .multiply(dependency.get(w).add(one))));
            }
            if (!w.equals(s)) {
                scores.put(w, scores.get(w).add(dependency.get(w)));
            }
        }
    }

    private interface MyQueue<T, D>
    {
        void insert(T t, D d);

        void update(T t, D d);

        T remove();

        boolean isEmpty();
    }

    private class WeightedQueue
        implements
        MyQueue<V, Double>
    {

        AddressableHeap<Double, V> delegate = new PairingHeap<>();
        Map<V, AddressableHeap.Handle<Double, V>> seen = new HashMap<>();

        @Override
        public void insert(V t, Double d)
        {
            AddressableHeap.Handle<Double, V> node = delegate.insert(d, t);
            seen.put(t, node);
        }

        @Override
        public void update(V t, Double d)
        {
            if (!seen.containsKey(t)) {
                throw new IllegalArgumentException("Element " + t + " does not exist in queue");
            }
            seen.get(t).decreaseKey(d);
        }

        @Override
        public V remove()
        {
            return delegate.deleteMin().getValue();
        }

        @Override
        public boolean isEmpty()
        {
            return delegate.isEmpty();
        }

    }

    private class UnweightedQueue
        implements
        MyQueue<V, Double>
    {

        Queue<V> delegate = new ArrayDeque<>();

        @Override
        public void insert(V t, Double d)
        {
            delegate.add(t);
        }

        @Override
        public void update(V t, Double d)
        {
            // do nothing
        }

        @Override
        public V remove()
        {
            return delegate.remove();
        }

        @Override
        public boolean isEmpty()
        {
            return delegate.isEmpty();
        }

    }
}
