/*
 * (C) Copyright 2019-2019, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import static org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath.DijkstraSearchFrontier;
import static org.jgrapht.alg.shortestpath.ContractionHierarchy.ContractionEdge;
import static org.jgrapht.alg.shortestpath.ContractionHierarchy.ContractionVertex;

/**
 * Implementation of the hierarchical query algorithm based on the bidirectional Dijkstra search.
 * This algorithm is designed to contracted graphs. The best speedup is achieved on sparse graphs
 * with low average outdegree.
 *
 * <p>
 * The query algorithm is originally described the article: Robert Geisberger, Peter Sanders, Dominik Schultes,
 * and Daniel Delling. 2008. Contraction hierarchies: faster and simpler hierarchical routing in road networks.
 * In Proceedings of the 7th international conference on Experimental algorithms (WEA'08), Catherine C. McGeoch (Ed.).
 * Springer-Verlag, Berlin, Heidelberg, 319-333.
 *
 * <p>
 * During contraction graph is divided into 2 parts which are called upward and downward graphs. Both parts have all
 * vertices of the original graph. The upward graph ($G_{&#92;uparrow}$) contains only those edges which source
 * has lower level than the target and vice versa for the downward graph ($G_{\downarrow}$).
 *
 * <p>
 * For the shortest path query from $s$ to $t$, a modified bidirectional Dijkstra shortest path search is
 * performed. The forward search from $s$ operates on $G_{&#92;uparrow}$ and the backward search from $t$ -
 * on the $G_{\downarrow}$. In each direction only the edges of the corresponding part of the graph are
 * considered. Both searches eventually meet at the vertex $v$, which has the highest level in the shortest
 * path from $s$ to $t$. Whenever a search in one direction reaches a vertex that has already been processed
 * in other direction, a new candidate for a shortest path is found. Search is aborted in one direction
 * if the smallest element in the corresponding priority queue is at least as large as the best candidate path
 * found so far.
 *
 * <p>
 * After computing a contracted path, the algorithm unpacks it recursively into the actual shortest path using
 * the bypassed edges stored in the contraction hierarchy graph.
 *
 * <p>
 * There is a possibility to provide an already computed contraction for the graph. For now there is no means
 * to ensure that the specified contraction is correct, nor to fail-fast. If algorithm uses an incorrect contraction,
 * the results of the search are unpredictable.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see ContractionHierarchy
 * @since July 2019
 */
public class ContractionHierarchyBidirectionalDijkstra<V, E> extends BaseShortestPathAlgorithm<V, E> {

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
    private Supplier<AddressableHeap<Double, Pair<ContractionVertex<V>,
            ContractionEdge<E>>>> heapSupplier;

    /**
     * Radius of the search.
     */
    private double radius;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     */
    public ContractionHierarchyBidirectionalDijkstra(Graph<V, E> graph) {
        super(graph);
        Pair<Graph<ContractionVertex<V>, ContractionEdge<E>>, Map<V, ContractionVertex<V>>> p
                = new ContractionHierarchy<>(graph).computeContractionHierarchy();
        init(p.getFirst(), p.getSecond(), Double.POSITIVE_INFINITY, PairingHeap::new);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, contracted graph
     * and contraction mapping.
     *
     * @param graph              the graph
     * @param contractedGraph    contracted graph
     * @param contractionMapping mapping from vertices in
     *                           graph to vertices in {@code contractionGraph}
     */
    public ContractionHierarchyBidirectionalDijkstra(Graph<V, E> graph,
                                                     Graph<ContractionVertex<V>, ContractionEdge<E>> contractedGraph,
                                                     Map<V, ContractionVertex<V>> contractionMapping) {
        this(graph, contractedGraph, contractionMapping, Double.POSITIVE_INFINITY, PairingHeap::new);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, contracted graph,
     * contraction mapping, radius and heap supplier.
     *
     * @param graph              the graph
     * @param contractedGraph    contracted graph
     * @param contractionMapping mapping from vertices in
     *                           graph to vertices in {@code contractionGraph}
     * @param radius             search radius
     * @param heapSupplier       supplier of the preferable heap implementation
     */
    public ContractionHierarchyBidirectionalDijkstra(Graph<V, E> graph,
                                                     Graph<ContractionVertex<V>, ContractionEdge<E>> contractedGraph,
                                                     Map<V, ContractionVertex<V>> contractionMapping,
                                                     double radius,
                                                     Supplier<AddressableHeap<Double, Pair<ContractionVertex<V>,
                                                             ContractionEdge<E>>>> heapSupplier) {
        super(graph);
        init(contractedGraph, contractionMapping, radius, heapSupplier);
    }

    /**
     * Initializes fields of a new instance of the algorithm.
     *
     * @param contractedGraph    contracted graph
     * @param contractionMapping mapping from original to contracted vertices
     * @param radius             search radius
     * @param heapSupplier       supplier of the preferable heap implementation
     */
    private void init(Graph<ContractionVertex<V>, ContractionEdge<E>> contractedGraph,
                      Map<V, ContractionVertex<V>> contractionMapping,
                      double radius,
                      Supplier<AddressableHeap<Double, Pair<ContractionVertex<V>, ContractionEdge<E>>>> heapSupplier) {
        this.contractionGraph = contractedGraph;
        this.contractionMapping = contractionMapping;
        this.radius = radius;
        this.heapSupplier = heapSupplier;
    }

    /**
     * {@inheritDoc}
     */
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

        ContractionVertex<V> contractedSource = contractionMapping.get(source);
        ContractionVertex<V> contractedSink = contractionMapping.get(sink);

        // create frontiers
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> forwardFrontier
                = new ContractionSearchFrontier<>(new MaskSubgraph<>(contractionGraph, v -> false, e -> !e.isUpward),
                heapSupplier);


        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> backwardFrontier
                = new ContractionSearchFrontier<>(new MaskSubgraph<>(
                new EdgeReversedGraph<>(contractionGraph), v -> false, e -> e.isUpward), heapSupplier);


        // initialize both frontiers
        forwardFrontier.updateDistance(contractedSource, null, 0d);
        backwardFrontier.updateDistance(contractedSink, null, 0d);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        ContractionVertex<V> bestPathCommonVertex = null;

        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> frontier = forwardFrontier;
        ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> otherFrontier = backwardFrontier;

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
                AddressableHeap.Handle<Double, Pair<ContractionVertex<V>, ContractionEdge<E>>> node
                        = frontier.heap.deleteMin();
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
                ContractionSearchFrontier<ContractionVertex<V>,
                        ContractionEdge<E>> tmpFrontier = frontier;
                frontier = otherFrontier;
                otherFrontier = tmpFrontier;
            }
        }

        // create path if found
        if (Double.isFinite(bestPath) && bestPath <= radius) {
            return createPath(forwardFrontier, backwardFrontier,
                    bestPath, contractedSource, bestPathCommonVertex, contractedSink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    /**
     * Builds shortest unpacked path between {@code source} and {@code sink} based on the information
     * provided by search frontiers and common vertex.
     *
     * @param forwardFrontier  forward direction frontier
     * @param backwardFrontier backward direction frontier
     * @param weight           weight of the shortest path
     * @param source           path source
     * @param commonVertex     path common vertex
     * @param sink             path sink
     * @return unpacked shortest path between source and sink
     */
    private GraphPath<V, E> createPath(
            ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> forwardFrontier,
            ContractionSearchFrontier<ContractionVertex<V>, ContractionEdge<E>> backwardFrontier,
            double weight,
            ContractionVertex<V> source,
            ContractionVertex<V> commonVertex,
            ContractionVertex<V> sink) {

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

            unpackBackward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeSource(e);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            ContractionEdge<E> e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            unpackForward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeTarget(e);
        }

        return new GraphWalk<>(graph, source.vertex, sink.vertex, vertexList, edgeList, weight);
    }

    /**
     * Unpacks {@code edge} by recursively going from target to source.
     *
     * @param edge       edge to unpack
     * @param vertexList vertex list of the path
     * @param edgeList   edge list of the path
     */
    private void unpackBackward(ContractionEdge<E> edge, LinkedList<V> vertexList, LinkedList<E> edgeList) {
        if (edge.bypassedEdges == null) {
            vertexList.addFirst(contractionGraph.getEdgeSource(edge).vertex);
            edgeList.addFirst(edge.edge);
        } else {
            unpackBackward(edge.bypassedEdges.getSecond(), vertexList, edgeList);
            unpackBackward(edge.bypassedEdges.getFirst(), vertexList, edgeList);
        }
    }

    /**
     * Unpacks {@code edge} by recursively going from source to target.
     *
     * @param edge       edge to unpack
     * @param vertexList vertex list of the path
     * @param edgeList   edge list of the path
     */
    private void unpackForward(ContractionEdge<E> edge, LinkedList<V> vertexList, LinkedList<E> edgeList) {
        if (edge.bypassedEdges == null) {
            vertexList.addLast(contractionGraph.getEdgeTarget(edge).vertex);
            edgeList.addLast(edge.edge);
        } else {
            unpackForward(edge.bypassedEdges.getFirst(), vertexList, edgeList);
            unpackForward(edge.bypassedEdges.getSecond(), vertexList, edgeList);
        }
    }

    /**
     * Maintains search frontier during shortest path computation.
     *
     * @param <V> vertices type
     * @param <E> edges type
     */
    static class ContractionSearchFrontier<V, E>
            extends DijkstraSearchFrontier<V, E> {
        boolean isFinished;

        /**
         * Constructs an instance of a search frontier for the given graph, heap supplier and
         * {@code isDownwardEdge} function.
         *
         * @param graph        the graph
         * @param heapSupplier supplier for the preferable heap implementation
         */
        ContractionSearchFrontier(Graph<V, E> graph,
                                  Supplier<AddressableHeap<Double, Pair<V, E>>> heapSupplier) {
            super(graph, heapSupplier);
        }
    }
}
