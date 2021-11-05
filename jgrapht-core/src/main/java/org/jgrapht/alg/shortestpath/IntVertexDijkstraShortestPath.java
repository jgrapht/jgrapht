/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.graph.*;
import org.jheaps.*;
import org.jheaps.AddressableHeap.*;
import org.jheaps.array.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * Dijkstra Shortest Path implementation specialized for graphs with integer vertices.
 * 
 * <p>
 * This class avoids using hash tables when the vertices are numbered from $0$ to $n-1$ where $n$ is
 * the number of vertices of the graph. If vertices are not in this range, then they are mapped in
 * this range using an open addressing hash table (linear probing).
 * 
 * <p>
 * This implementation should be faster than our more generic one which is
 * {@link DijkstraShortestPath} since it avoids the extensive use of hash tables.
 * 
 * <p>
 * By default a 4-ary heap is used. The user is free to use a custom heap implementation during
 * construction time. Regarding the choice of heap, it is generally known that it depends on the
 * particular workload. For more details and experiments which can help someone make the choice,
 * read the following paper: Larkin, Daniel H., Siddhartha Sen, and Robert E. Tarjan. "A
 * back-to-basics empirical study of priority queues." 2014 Proceedings of the Sixteenth Workshop on
 * Algorithm Engineering and Experiments (ALENEX). Society for Industrial and Applied Mathematics,
 * 2014.
 * 
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public final class IntVertexDijkstraShortestPath<E>
    extends
    BaseShortestPathAlgorithm<Integer, E>
{
    private final Supplier<AddressableHeap<Double, Integer>> heapSupplier;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     */
    public IntVertexDijkstraShortestPath(Graph<Integer, E> graph)
    {
        this(graph, () -> new DaryArrayAddressableHeap<>(4));
    }

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public IntVertexDijkstraShortestPath(
        Graph<Integer, E> graph, Supplier<AddressableHeap<Double, Integer>> heapSupplier)
    {
        super(graph);
        this.heapSupplier = heapSupplier;
    }

    /**
     * Find a path between two vertices. For a more advanced search (e.g. using another heap), use
     * the constructor instead.
     *
     * @param graph the graph to be searched
     * @param source the vertex at which the path should start
     * @param sink the vertex at which the path should end
     * @param <E> the graph edge type
     * @return a shortest path, or null if no path exists
     */
    public static <E> GraphPath<Integer, E> findPathBetween(
        Graph<Integer, E> graph, Integer source, Integer sink)
    {
        return new IntVertexDijkstraShortestPath<>(graph).getPath(source, sink);
    }

    @Override
    public GraphPath<Integer, E> getPath(Integer source, Integer sink)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        return new Algorithm().getPath(source, sink);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that in the case of Dijkstra's algorithm it is more efficient to compute all
     * single-source shortest paths using this method than repeatedly invoking
     * {@link #getPath(Integer, Integer)} for the same source but different sink vertex.
     */
    @Override
    public SingleSourcePaths<Integer, E> getPaths(Integer source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        return new Algorithm().getPaths(source);
    }

    /**
     * The actual implementation class. We use this inner class pattern in order to allow the user
     * to keep a reference to the implementation class, but allow the garbage collector to collect
     * the auxiliary memory used during the algorithm's execution.
     */
    private class Algorithm
    {
        private int totalVertices;
        private AddressableHeap<Double, Integer> heap;
        private AddressableHeap.Handle<Double, Integer>[] nodes;
        private double[] dist;
        private E[] pred;
        private IdentifierMap idMap;

        @SuppressWarnings("unchecked")
        public Algorithm()
        {
            this.totalVertices = graph.vertexSet().size();
            this.nodes = (Handle<Double, Integer>[]) Array
                .newInstance(AddressableHeap.Handle.class, totalVertices);
            this.heap = heapSupplier.get();

            this.dist = new double[totalVertices];
            this.pred = (E[]) new Object[totalVertices];

            boolean remapVertices = false;
            int i = 0;
            for (Integer v : graph.vertexSet()) {
                if (v < 0 || v >= totalVertices) {
                    remapVertices = true;
                }
                dist[i] = Double.POSITIVE_INFINITY;
                pred[i] = null;
                i++;
            }

            /*
             * In case vertices are not in $[0 \ldots n)$ where $n$ is the number of vertices, we
             * map them.
             */
            if (remapVertices) {
                idMap = new IdentifierMap(totalVertices);
                i = 0;
                for (Integer v : graph.vertexSet()) {
                    idMap.put(v, i++);
                }
            }
        }

        public SingleSourcePaths<Integer, E> getPaths(Integer source)
        {
            if (idMap == null) {
                return getPathsWithoutIdMap(source, null);
            } else {
                return getPathsWithIdMap(source, null);
            }
        }

        public SingleSourcePaths<Integer, E> getPathsWithoutIdMap(Integer source, Integer target)
        {
            dist[source] = 0d;
            pred[source] = null;
            nodes[source] = heap.insert(0d, source);

            while (!heap.isEmpty()) {
                AddressableHeap.Handle<Double, Integer> vNode = heap.deleteMin();
                Integer v = vNode.getValue();
                double vDistance = vNode.getKey();
                dist[v] = vDistance;

                if (target != null && v.intValue() == target.intValue()) {
                    break;
                }

                for (E e : graph.outgoingEdgesOf(v)) {
                    Integer u = Graphs.getOppositeVertex(graph, e, v);
                    double eWeight = graph.getEdgeWeight(e);
                    if (eWeight < 0.0) {
                        throw new IllegalArgumentException("Negative edge weight not allowed");
                    }
                    AddressableHeap.Handle<Double, Integer> uNode = nodes[u];
                    double uDist = vDistance + eWeight;
                    if (uNode == null) {
                        nodes[u] = heap.insert(uDist, u);
                        pred[u] = e;
                    } else if (uDist < uNode.getKey()) {
                        uNode.decreaseKey(uDist);
                        pred[u] = e;
                    }
                }
            }

            return new ArrayBasedSingleSourcePathsImpl(source, dist, pred, idMap);
        }

        public SingleSourcePaths<Integer, E> getPathsWithIdMap(Integer source, Integer target)
        {
            dist[idMap.get(source)] = 0d;
            pred[idMap.get(source)] = null;
            nodes[idMap.get(source)] = heap.insert(0d, source);

            while (!heap.isEmpty()) {
                AddressableHeap.Handle<Double, Integer> vNode = heap.deleteMin();
                Integer v = vNode.getValue();
                double vDistance = vNode.getKey();
                dist[idMap.get(v)] = vDistance;

                if (target != null && v.intValue() == target.intValue()) {
                    break;
                }

                for (E e : graph.outgoingEdgesOf(v)) {
                    Integer u = Graphs.getOppositeVertex(graph, e, v);
                    double eWeight = graph.getEdgeWeight(e);
                    if (eWeight < 0.0) {
                        throw new IllegalArgumentException("Negative edge weight not allowed");
                    }
                    AddressableHeap.Handle<Double, Integer> uNode = nodes[idMap.get(u)];
                    double uDist = vDistance + eWeight;
                    if (uNode == null) {
                        nodes[idMap.get(u)] = heap.insert(uDist, u);
                        pred[idMap.get(u)] = e;
                    } else if (uDist < uNode.getKey()) {
                        uNode.decreaseKey(uDist);
                        pred[idMap.get(u)] = e;
                    }
                }
            }

            return new ArrayBasedSingleSourcePathsImpl(source, dist, pred, idMap);
        }

        public GraphPath<Integer, E> getPath(Integer source, Integer target)
        {
            if (idMap == null) {
                return getPathsWithoutIdMap(source, target).getPath(target);
            } else {
                return getPathsWithIdMap(source, target).getPath(target);
            }
        }

    }

    private class ArrayBasedSingleSourcePathsImpl
        implements
        SingleSourcePaths<Integer, E>,
        Serializable
    {
        private static final long serialVersionUID = 2912496450441089175L;

        private Integer source;
        private double[] dist;
        private E[] pred;
        private IdentifierMap idMap;

        public ArrayBasedSingleSourcePathsImpl(
            Integer source, double[] dist, E[] pred, IdentifierMap idMap)
        {
            this.source = source;
            this.dist = dist;
            this.pred = pred;
            this.idMap = idMap;
        }

        @Override
        public Graph<Integer, E> getGraph()
        {
            return graph;
        }

        @Override
        public Integer getSourceVertex()
        {
            return source;
        }

        @Override
        public double getWeight(Integer targetVertex)
        {
            if (idMap == null) {
                return dist[targetVertex];
            } else {
                return dist[idMap.get(targetVertex)];
            }
        }

        @Override
        public GraphPath<Integer, E> getPath(Integer targetVertex)
        {
            if (source.equals(targetVertex)) {
                return GraphWalk.singletonWalk(graph, source, 0d);
            }

            Deque<E> edgeList = new ArrayDeque<>();
            Integer cur = targetVertex;

            double distance;
            E e;
            if (idMap != null) {
                if (pred[idMap.get(cur)] == null) {
                    return null;
                }
                while ((e = pred[idMap.get(cur)]) != null) {
                    edgeList.addFirst(e);
                    cur = Graphs.getOppositeVertex(graph, e, cur);
                }
                distance = dist[idMap.get(targetVertex)];
            } else {
                if (pred[cur] == null) {
                    return null;
                }
                while ((e = pred[cur]) != null) {
                    edgeList.addFirst(e);
                    cur = Graphs.getOppositeVertex(graph, e, cur);
                }
                distance = dist[targetVertex];
            }

            return new GraphWalk<>(
                graph, source, targetVertex, null, new ArrayList<>(edgeList), distance);
        }
    }

    /**
     * A very special case linear probing hash table, fit for this particular use case. The code
     * assumes several invariants such as that the user will never add more elements than its
     * capacity, etc.
     */
    private class IdentifierMap
    {
        private int[] keys;
        private int[] values;
        private int m;

        public IdentifierMap(int m)
        {
            this.m = m;
            this.keys = new int[m];
            Arrays.fill(keys, -1);
            this.values = new int[m];
        }

        public void put(int key, int value)
        {
            int i;
            for (i = hash(key); keys[i] != -1; i = (i + 1) % m) {
                if (keys[i] == key) {
                    values[i] = value;
                    return;
                }
            }
            keys[i] = key;
            values[i] = value;
        }

        public int get(int key)
        {
            for (int i = hash(key); keys[i] != -1; i = (i + 1) % m) {
                if (keys[i] == key) {
                    return values[i];
                }
            }
            return -1;
        }

        private int hash(int key)
        {
            return (key & 0x7fffffff) % m;
        }
    }

}
