/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.util.ConcurrencyUtil;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.*;

import static org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath.DijkstraSearchFrontier;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.*;

/**
 * Implementation of the hierarchical query algorithm based on the bidirectional Dijkstra search.
 * This algorithm is designed to contracted graphs. The best speedup is achieved on sparse graphs
 * with low average outdegree.
 *
 * <p>
 * The query algorithm is originally described the article: Robert Geisberger, Peter Sanders,
 * Dominik Schultes, and Daniel Delling. 2008. Contraction hierarchies: faster and simpler
 * hierarchical routing in road networks. In Proceedings of the 7th international conference on
 * Experimental algorithms (WEA'08), Catherine C. McGeoch (Ed.). Springer-Verlag, Berlin,
 * Heidelberg, 319-333.
 *
 * <p>
 * During contraction graph is divided into 2 parts which are called upward and downward graphs.
 * Both parts have all vertices of the original graph. The upward graph ($G_{&#92;uparrow}$)
 * contains only those edges which source has lower level than the target and vice versa for the
 * downward graph ($G_{\downarrow}$).
 *
 * <p>
 * For the shortest path query from $s$ to $t$, a modified bidirectional Dijkstra shortest path
 * search is performed. The forward search from $s$ operates on $G_{&#92;uparrow}$ and the backward
 * search from $t$ - on the $G_{\downarrow}$. In each direction only the edges of the corresponding
 * part of the graph are considered. Both searches eventually meet at the vertex $v$, which has the
 * highest level in the shortest path from $s$ to $t$. Whenever a search in one direction reaches a
 * vertex that has already been processed in other direction, a new candidate for a shortest path is
 * found. Search is aborted in one direction if the smallest element in the corresponding priority
 * queue is at least as large as the best candidate path found so far.
 *
 * <p>
 * After computing a contracted path, the algorithm unpacks it recursively into the actual shortest
 * path using the bypassed edges stored in the contraction hierarchy graph.
 *
 * <p>
 * There is a possibility to provide an already computed contraction for the graph. For now there is
 * no means to ensure that the specified contraction is correct, nor to fail-fast. If algorithm uses
 * an incorrect contraction, the results of the search are unpredictable.
 *
 * <p>
 * Comparing to usual shortest path algorithm, as {@link DijkstraShortestPath},
 * {@link AStarShortestPath}, etc., this algorithm spends time for computing contraction hierarchy
 * but offers significant speedup in shortest path query performance. Therefore it is efficient to
 * use it in order to compute many shortest path on a single graph. Furthermore, on small graphs
 * (i.e with less than 1.000 vertices) the overhead of precomputation is higher than the speed at
 * the stage of computing shortest paths. Typically this algorithm is used to gain speedup for
 * shortest path queries on graphs of middle and large size (i.e. starting at 1.000 vertices). If a
 * further query performance improvement is needed take a look at
 * {@link TransitNodeRoutingShortestPath}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see ContractionHierarchyPrecomputation
 * @since July 2019
 */
public class ContractionHierarchyBidirectionalDijkstra<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{

    /**
     * Contraction hierarchy which is used to compute shortest paths.
     */
    private ContractionHierarchy<V, E> contractionHierarchy;
    /**
     * Contracted graph, which is used during the queries.
     */
    private Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph;
    /**
     * Mapping from original to contracted vertices.
     */
    private Map<V, ContractionVertex<V>> contractionMapping;

    /**
     * Supplier for preferable heap implementation.
     */
    private Supplier<
        AddressableHeap<Double, Pair<ContractionVertex<V>, ContractionEdge<E>>>> heapSupplier;

    /**
     * Radius of the search.
     */
    private double radius;

    /**
     * Constructs a new instance of the algorithm for a given {@code graph}.
     *
     * @param graph the graph
     * @deprecated replaced with
     *             {@link #ContractionHierarchyBidirectionalDijkstra(Graph, ThreadPoolExecutor)}
     */
    @Deprecated
    public ContractionHierarchyBidirectionalDijkstra(Graph<V, E> graph)
    {
        this(new ContractionHierarchyPrecomputation<>(graph).computeContractionHierarchy());
    }

    /**
     * Constructs a new instance of the algorithm for a given {@code graph} and {@code executor}. It
     * is up to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. For utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link ConcurrencyUtil}.
     *
     * @param graph the graph
     * @param executor executor which is used for computing the {@link ContractionHierarchy}
     */
    public ContractionHierarchyBidirectionalDijkstra(Graph<V, E> graph, ThreadPoolExecutor executor)
    {
        this(
            new ContractionHierarchyPrecomputation<>(graph, executor)
                .computeContractionHierarchy());
    }

    /**
     * Constructs a new instance of the algorithm for a given {@code hierarchy}.
     *
     * @param hierarchy contraction of the {@code graph}
     */
    public ContractionHierarchyBidirectionalDijkstra(ContractionHierarchy<V, E> hierarchy)
    {
        this(hierarchy, Double.POSITIVE_INFINITY, PairingHeap::new);
    }

    /**
     * Constructs a new instance of the algorithm for the given {@code hierarchy}, {@code radius}
     * and {@code heapSupplier}.
     *
     * @param hierarchy contraction of the {@code graph}
     * @param radius search radius
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public ContractionHierarchyBidirectionalDijkstra(
        ContractionHierarchy<V, E> hierarchy, double radius, Supplier<
            AddressableHeap<Double, Pair<ContractionVertex<V>, ContractionEdge<E>>>> heapSupplier)
    {
        super(hierarchy.getGraph());
        this.contractionHierarchy = hierarchy;
        this.contractionGraph = hierarchy.getContractionGraph();
        this.contractionMapping = hierarchy.getContractionMapping();
        this.radius = radius;
        this.heapSupplier = heapSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
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

        ContractionVertex<V> contractedSource = contractionMapping.get(source);
        ContractionVertex<V> contractedSink = contractionMapping.get(sink);

        // create frontiers
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> forwardFrontier =
            new ContractionSearchFrontier<>(
                new MaskSubgraph<>(contractionGraph, v -> false, e -> !e.isUpward), heapSupplier);

        ContractionSearchFrontier<ContractionVertex<V>,
            ContractionEdge<E>> backwardFrontier = new ContractionSearchFrontier<>(
                new MaskSubgraph<>(
                    new EdgeReversedGraph<>(contractionGraph), v -> false, e -> e.isUpward),
                heapSupplier);

        // initialize both frontiers
        forwardFrontier.updateDistance(contractedSource, null, 0d);
        backwardFrontier.updateDistance(contractedSink, null, 0d);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        ContractionVertex<V> bestPathCommonVertex = null;

        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> frontier =
            forwardFrontier;
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> otherFrontier =
            backwardFrontier;

        while (true) {
            if (frontier.heap.isEmpty()) {
                frontier.isFinished = true;
            }
            if (otherFrontier.heap.isEmpty()) {
                otherFrontier.isFinished = true;
            }

            // stopping condition for search
            if (frontier.isFinished && otherFrontier.isFinished) {
                break;
            }

            // stopping condition for current frontier
            if (frontier.heap.findMin().getKey() >= bestPath) {
                frontier.isFinished = true;
            } else {

                // frontier scan
                AddressableHeap.Handle<Double,
                    Pair<ContractionVertex<V>, ContractionEdge<E>>> node =
                        frontier.heap.deleteMin();
                ContractionVertex<V> v = node.getValue().getFirst();
                double vDistance = node.getKey();

                for (ContractionEdge<E> e : frontier.graph.outgoingEdgesOf(v)) {
                    ContractionVertex<V> u = frontier.graph.getEdgeTarget(e);

                    double eWeight = frontier.graph.getEdgeWeight(e);

                    frontier.updateDistance(u, e, vDistance + eWeight);

                    // check path with u's distance from the other frontier
                    double pathDistance = vDistance + eWeight + otherFrontier.getDistance(u);

                    if (pathDistance < bestPath) {
                        bestPath = pathDistance;
                        bestPathCommonVertex = u;
                    }
                }
            }

            // swap frontiers only if the other frontier is not yet finished
            if (!otherFrontier.isFinished) {
                ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> tmpFrontier =
                    frontier;
                frontier = otherFrontier;
                otherFrontier = tmpFrontier;
            }
        }

        // create path if found
        if (Double.isFinite(bestPath) && bestPath <= radius) {
            return createPath(
                forwardFrontier, backwardFrontier, bestPath, contractedSource, bestPathCommonVertex,
                contractedSink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    /**
     * Builds shortest unpacked path between {@code source} and {@code sink} based on the
     * information provided by search frontiers and common vertex.
     *
     * @param forwardFrontier forward direction frontier
     * @param backwardFrontier backward direction frontier
     * @param weight weight of the shortest path
     * @param source path source
     * @param commonVertex path common vertex
     * @param sink path sink
     * @return unpacked shortest path between source and sink
     */
    private GraphPath<V, E> createPath(
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> forwardFrontier,
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> backwardFrontier,
        double weight, ContractionVertex<V> source, ContractionVertex<V> commonVertex,
        ContractionVertex<V> sink)
    {

        LinkedList<E> edgeList = new LinkedList<>();
        LinkedList<V> vertexList = new LinkedList<>();

        // add common vertex
        vertexList.add(commonVertex.vertex);

        // traverse forward path
        ContractionVertex<V> v = commonVertex;
        while (true) {
            ContractionEdge<E> e = forwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            contractionHierarchy.unpackBackward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeSource(e);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            ContractionEdge<E> e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            contractionHierarchy.unpackForward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeTarget(e);
        }

        return new GraphWalk<>(graph, source.vertex, sink.vertex, vertexList, edgeList, weight);
    }

    /**
     * Maintains search frontier during shortest path computation.
     *
     * @param <V> vertices type
     * @param <E> edges type
     */
    static class ContractionSearchFrontier<V, E>
        extends
        DijkstraSearchFrontier<V, E>
    {
        boolean isFinished;

        /**
         * Constructs an instance of a search frontier for the given graph, heap supplier and
         * {@code isDownwardEdge} function.
         *
         * @param graph the graph
         * @param heapSupplier supplier for the preferable heap implementation
         */
        ContractionSearchFrontier(
            Graph<V, E> graph, Supplier<AddressableHeap<Double, Pair<V, E>>> heapSupplier)
        {
            super(graph, heapSupplier);
        }
    }
}
