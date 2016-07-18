/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------------
* GreedyMultiplicativeSpanner.java
* -------------------------
* (C) Copyright 2016, by Dimitrios Michail and Contributors.
*
* Original Author:  Dimitrios Michail
* Contributor(s):
* $Id$
*
* Changes
* -------
* 15-July-2016 : Initial revision (DM);
*
*/
package org.jgrapht.alg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * Greedy algorithm for (2k-1)-multiplicative spanner construction (for any
 * integer k >= 1).
 *
 * <p>
 * The spanner is guaranteed to contain O(n^{1+1/k}) edges and the shortest path
 * distance between any two vertices in the spanner is at most 2k-1 times the
 * corresponding shortest path distance in the original graph. Here n denotes
 * the number of vertices of the graph.
 *
 * <p>
 * The algorithm is described in: Althoefer, Das, Dobkin, Joseph, Soares.
 * <a href="https://doi.org/10.1007/BF02189308">On Sparse Spanners of Weighted
 * Graphs</a>. Discrete Computational Geometry 9(1):81-100, 1993.
 *
 * <p>
 * If the graph is unweighted the algorithm runs in O(m n^{1+1/k}) time. Setting
 * k to infinity will result in a slow version of Kruskal's algorithm where
 * cycle detection is performed by a BFS computation. In such a case use the
 * implementation of Kruskal with union-find. Here n and m are the number of
 * vertices and edges of the graph respectively.
 *
 * <p>
 * If the graph is weighted the algorithm runs in O(m (n^{1+1/k} + nlogn)) time
 * by using Dijkstra's algorithm. Edge weights must be non-negative.
 *
 * @author Dimitrios Michail
 * @since July 15, 2016
 */
public class GreedyMultiplicativeSpanner<V, E>
{
    private final Set<E> edgeList;
    private final UndirectedGraph<V, E> graph;
    private final int k;
    private static final int MAX_K = 1 << 29;

    /**
     * Constructs instance to compute a (2k-1)-spanner of a graph.
     * 
     * @param graph the input graph
     * @param k positive integer.
     */
    public GreedyMultiplicativeSpanner(UndirectedGraph<V, E> graph, int k)
    {
        this.graph = graph;
        this.edgeList = new LinkedHashSet<>();
        if (k <= 0) {
            throw new IllegalArgumentException(
                "k should be positive in (2k-1)-spanner construction");
        }
        this.k = Math.min(k, MAX_K);
        if (graph instanceof WeightedGraph) {
            new WeightedSpannerAlgorithm().run();
        } else {
            new UnweightedSpannerAlgorithm().run();
        }
    }

    /**
     * Get the edge set of the spanner.
     *
     * @return the set of edges of the spanner
     */
    public Set<E> getSpannerEdgeSet()
    {
        return edgeList;
    }

    // base algorithm implementation
    private abstract class SpannerAlgorithmBase
    {

        public abstract boolean isSpannerReachable(V s, V t, double distance);

        public abstract void addSpannerEdge(V s, V t, double weight);

        public void run()
        {
            // sort edges
            ArrayList<E> allEdges = new ArrayList<>(graph.edgeSet());
            Collections.sort(
                allEdges,
                (e1, e2) -> Double.valueOf(graph.getEdgeWeight(e1))
                    .compareTo(graph.getEdgeWeight(e2)));

            // check precondition
            double minWeight = graph.getEdgeWeight(allEdges.get(0));
            if (minWeight < 0.0) {
                throw new IllegalArgumentException(
                    "Illegal edge weight: negative");
            }

            // run main loop
            for (E e : allEdges) {
                V s = graph.getEdgeSource(e);
                V t = graph.getEdgeTarget(e);

                if (!s.equals(t)) { // self-loop?
                    double distance = (2 * k - 1) * graph.getEdgeWeight(e);
                    if (!isSpannerReachable(s, t, distance)) {
                        edgeList.add(e);
                        addSpannerEdge(s, t, graph.getEdgeWeight(e));
                    }
                }
            }
        }

    }

    private class UnweightedSpannerAlgorithm
        extends SpannerAlgorithmBase
    {

        protected UndirectedGraph<V, E> spanner;
        protected Map<V, Integer> vertexDistance;
        protected Deque<V> queue;
        protected Deque<V> touchedVertices;

        public UnweightedSpannerAlgorithm()
        {
            spanner = new SimpleGraph<V, E>(graph.getEdgeFactory());
            touchedVertices = new ArrayDeque<V>(graph.vertexSet().size());
            for (V v : graph.vertexSet()) {
                spanner.addVertex(v);
                touchedVertices.push(v);
            }
            vertexDistance = new HashMap<V, Integer>(graph.vertexSet().size());
            queue = new ArrayDeque<>();
        }

        @Override
        public void addSpannerEdge(V s, V t, double weight)
        {
            spanner.addEdge(s, t);
        }

        /**
         * Check if two vertices are reachable by a BFS in the spanner graph
         * using only a certain number of hops.
         *
         * We execute this procedure repeatedly, therefore we need to keep track
         * of what it touches and only clean those before the next execution.
         */
        @Override
        public boolean isSpannerReachable(V s, V t, double hops)
        {
            // initialize distances and queue
            while (!touchedVertices.isEmpty()) {
                V u = touchedVertices.pop();
                vertexDistance.put(u, Integer.MAX_VALUE);
            }
            while (!queue.isEmpty()) {
                queue.pop();
            }

            // do BFS
            touchedVertices.push(s);
            queue.push(s);
            vertexDistance.put(s, 0);

            while (!queue.isEmpty()) {
                V u = queue.pop();
                Integer uDistance = vertexDistance.get(u);

                if (u.equals(t)) { // found
                    return uDistance <= hops;
                }

                for (E e : spanner.edgesOf(u)) {
                    V v = Graphs.getOppositeVertex(spanner, e, u);
                    Integer vDistance = vertexDistance.get(v);

                    if (vDistance == Integer.MAX_VALUE) {
                        touchedVertices.push(v);
                        vertexDistance.put(v, uDistance + 1);
                        queue.push(v);
                    }
                }
            }

            return false;
        }

    }

    private class WeightedSpannerAlgorithm
        extends SpannerAlgorithmBase
    {

        protected WeightedGraph<V, E> spanner;
        protected FibonacciHeap<V> heap;
        protected Map<V, FibonacciHeapNode<V>> nodes;

        public WeightedSpannerAlgorithm()
        {
            this.spanner = new SimpleWeightedGraph<V, E>(
                graph.getEdgeFactory());
            for (V v : graph.vertexSet()) {
                spanner.addVertex(v);
            }
            this.heap = new FibonacciHeap<V>();
            this.nodes = new LinkedHashMap<V, FibonacciHeapNode<V>>();
        }

        @Override
        public boolean isSpannerReachable(V s, V t, double distance)
        {
            // init
            heap.clear();
            nodes.clear();

            FibonacciHeapNode<V> sNode = new FibonacciHeapNode<V>(s);
            nodes.put(s, sNode);
            heap.insert(sNode, 0d);

            while (!heap.isEmpty()) {
                FibonacciHeapNode<V> uNode = heap.removeMin();
                double uDistance = uNode.getKey();
                V u = uNode.getData();

                if (uDistance > distance) {
                    return false;
                }

                if (u.equals(t)) { // found
                    return true;
                }

                for (E e : spanner.edgesOf(u)) {
                    V v = Graphs.getOppositeVertex(spanner, e, u);
                    FibonacciHeapNode<V> vNode = nodes.get(v);
                    double vDistance = uDistance + spanner.getEdgeWeight(e);

                    if (vNode == null) { // no distance
                        vNode = new FibonacciHeapNode<V>(v);
                        nodes.put(v, vNode);
                        heap.insert(vNode, vDistance);
                    } else if (vDistance < vNode.getKey()) {
                        heap.decreaseKey(vNode, vDistance);
                    }
                }

            }

            return false;
        }

        @Override
        public void addSpannerEdge(V s, V t, double weight)
        {
            Graphs.addEdge(spanner, s, t, weight);
        }

    }

}
