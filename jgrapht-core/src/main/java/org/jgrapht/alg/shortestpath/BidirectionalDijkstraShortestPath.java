/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A bidirectional version of Dijkstra's algorithm.
 *
 * <p>
 * See the Wikipedia article for details and references about
 * <a href="https://en.wikipedia.org/wiki/Bidirectional_search">bidirectional search</a>. This
 * technique does not change the worst-case behavior of the algorithm but reduces, in some cases,
 * the number of visited vertices in practice. This implementation alternatively constructs forward
 * and reverse paths from the source and target vertices respectively.
 * <p>
 * This iterator can use a custom heap implementation, which can specified during the construction time.
 * Pairing heap is used by default
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Dimitrios Michail
 * @see DijkstraShortestPath
 */
public final class BidirectionalDijkstraShortestPath<V, E>
        extends
        BaseShortestPathAlgorithm<V, E> {
    private double radius;
    private final Supplier<AddressableHeap<Double, Pair<V, E>>> heapSupplier;

    /**
     * Constructs a new instance for a specified graph.
     *
     * @param graph the input graph
     */
    public BidirectionalDijkstraShortestPath(Graph<V, E> graph) {
        this(graph, Double.POSITIVE_INFINITY, PairingHeap::new);
    }

    /**
     * Constructs a new instance for a specified graph. The constructed algorithm will use the heap
     * supplied by the {@code heapSupplier}.
     *
     * @param graph the input graph
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public BidirectionalDijkstraShortestPath(Graph<V, E> graph, Supplier<AddressableHeap<Double, Pair<V, E>>> heapSupplier) {
        this(graph, Double.POSITIVE_INFINITY, heapSupplier);
    }

    /**
     * Constructs a new instance for a specified graph.
     *
     * @param graph  the input graph
     * @param radius limit on path length, or Double.POSITIVE_INFINITY for unbounded search
     */
    public BidirectionalDijkstraShortestPath(Graph<V, E> graph, double radius) {
        this(graph, radius, PairingHeap::new);
    }

    /**
     * Constructs a new instance for a specified graph. The constructed algorithm will use the heap
     * supplied by the {@code heapSupplier}.
     *
     * @param graph  the input graph
     * @param radius limit on path length, or Double.POSITIVE_INFINITY for unbounded search
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public BidirectionalDijkstraShortestPath(Graph<V, E> graph, double radius, Supplier<AddressableHeap<Double, Pair<V, E>>> heapSupplier) {
        super(graph);
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        this.heapSupplier = Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null");
        this.radius = radius;
    }

    /**
     * Find a path between two vertices. For a more advanced search (e.g. limited by radius), use
     * the constructor instead.
     *
     * @param graph  the graph to be searched
     * @param source the vertex at which the path should start
     * @param sink   the vertex at which the path should end
     * @param <V>    the graph vertex type
     * @param <E>    the graph edge type
     * @return a shortest path, or null if no path exists
     */
    public static <V, E> GraphPath<V, E> findPathBetween(Graph<V, E> graph, V source, V sink) {
        return new BidirectionalDijkstraShortestPath<>(graph).getPath(source, sink);
    }

    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
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
        SearchFrontier forwardFrontier = new SearchFrontier(graph);
        SearchFrontier backwardFrontier;
        if (graph.getType().isDirected()) {
            backwardFrontier = new SearchFrontier(new EdgeReversedGraph<>(graph));
        } else {
            backwardFrontier = new SearchFrontier(graph);
        }

        assert !source.equals(sink);

        // initialize both frontiers
        forwardFrontier.updateDistance(source, null, 0d);
        backwardFrontier.updateDistance(sink, null, 0d);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        V bestPathCommonVertex = null;

        SearchFrontier frontier = forwardFrontier;
        SearchFrontier otherFrontier = backwardFrontier;

        while (true) {
            // stopping condition
            if (frontier.heap.isEmpty() || otherFrontier.heap.isEmpty()
                    || frontier.heap.findMin().getKey() + otherFrontier.heap.findMin().getKey() >= bestPath) {
                break;
            }

            // frontier scan
            AddressableHeap.Handle<Double, Pair<V, E>> node = frontier.heap.deleteMin();
            V v = node.getValue().getFirst();
            double vDistance = node.getKey();

            for (E e : frontier.graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(frontier.graph, e, v);

                double eWeight = frontier.graph.getEdgeWeight(e);

                frontier.updateDistance(u, e, vDistance + eWeight);

                // check path with u's distance from the other frontier
                double pathDistance = vDistance + eWeight + otherFrontier.getDistance(u);

                if (pathDistance < bestPath) {
                    bestPath = pathDistance;
                    bestPathCommonVertex = u;
                }

            }

            // swap frontiers
            SearchFrontier tmpFrontier = frontier;
            frontier = otherFrontier;
            otherFrontier = tmpFrontier;

        }

        // create path if found
        if (Double.isFinite(bestPath) && bestPath <= radius) {
            return createPath(
                    forwardFrontier, backwardFrontier, bestPath, source, bestPathCommonVertex, sink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    private GraphPath<V, E> createPath(
            SearchFrontier forwardFrontier, SearchFrontier backwardFrontier, double weight, V source,
            V commonVertex, V sink) {
        LinkedList<E> edgeList = new LinkedList<>();
        LinkedList<V> vertexList = new LinkedList<>();

        // add common vertex
        vertexList.add(commonVertex);

        // traverse forward path
        V v = commonVertex;
        while (true) {
            E e = forwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addFirst(e);
            v = Graphs.getOppositeVertex(forwardFrontier.graph, e, v);
            vertexList.addFirst(v);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            E e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addLast(e);
            v = Graphs.getOppositeVertex(backwardFrontier.graph, e, v);
            vertexList.addLast(v);
        }

        return new GraphWalk<>(graph, source, sink, vertexList, edgeList, weight);
    }

    /**
     * Helper class to maintain the search frontier
     */
    class SearchFrontier {
        final Graph<V, E> graph;

        final AddressableHeap<Double, Pair<V, E>> heap;
        final Map<V, AddressableHeap.Handle<Double, Pair<V, E>>> seen;

        public SearchFrontier(Graph<V, E> graph) {
            this.graph = graph;
            this.heap = heapSupplier.get();
            this.seen = new HashMap<>();
        }

        public void updateDistance(V v, E e, double distance) {
            AddressableHeap.Handle<Double, Pair<V, E>> node = seen.get(v);
            if (node == null) {
                node = heap.insert(distance, new Pair<>(v, e));
                seen.put(v, node);
            } else {
                if (distance < node.getKey()) {
                    node.decreaseKey(distance);
                    node.setValue(Pair.of(v, e));
                }
            }
        }

        public double getDistance(V v) {
            AddressableHeap.Handle<Double, Pair<V, E>> node = seen.get(v);
            if (node == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                return node.getKey();
            }
        }

        public E getTreeEdge(V v) {
            AddressableHeap.Handle<Double, Pair<V, E>> node = seen.get(v);
            if (node == null) {
                return null;
            } else {
                return node.getValue().getSecond();
            }
        }

    }

}
