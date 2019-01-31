/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Johnson's all pairs shortest paths algorithm.
 *
 * <p>
 * Finds the shortest paths between all pairs of vertices in a sparse graph. Edge weights can be
 * negative, but no negative-weight cycles may exist. It first executes the Bellman-Ford algorithm
 * to compute a transformation of the input graph that removes all negative weights, allowing
 * Dijkstra's algorithm to be used on the transformed graph.
 *
 * <p>
 * Running time is $O(n m + n^2 \log n)$.
 *
 * <p>
 * Since Johnson's algorithm creates additional vertices, this implementation requires the user to
 * provide a graph which is initialized with a vertex supplier.
 * 
 * <p>
 * In case the algorithm detects a negative weight cycle it will throw an exception of type
 * {@link NegativeCycleDetectedException} which will contain the detected negative weight cycle.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class JohnsonShortestPaths<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    private double[][] distance;
    private E[][] pred;
    private Map<V, Integer> vertexIndices;

    private final Comparator<Double> comparator;

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     */
    public JohnsonShortestPaths(Graph<V, E> graph)
    {
        this(graph, ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     * @param epsilon tolerance when comparing floating point values
     */
    public JohnsonShortestPaths(Graph<V, E> graph, double epsilon)
    {
        super(graph);
        this.comparator = new ToleranceDoubleComparator(epsilon);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException in case the provided vertex factory creates vertices which
     *         are already in the original graph
     * @throws NegativeCycleDetectedException in case a negative weight cycle is detected
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

        run();

        if (source.equals(sink)) {
            return GraphWalk.singletonWalk(graph, source, 0d);
        }

        int vSource = vertexIndices.get(source);
        int vSink = vertexIndices.get(sink);

        V cur = sink;
        E e = pred[vSource][vSink];
        if (e == null) {
            return null;
        }

        LinkedList<E> edgeList = new LinkedList<>();
        while (e != null) {
            edgeList.addFirst(e);
            cur = Graphs.getOppositeVertex(graph, e, cur);
            e = pred[vSource][vertexIndices.get(cur)];
        }

        return new GraphWalk<>(graph, source, sink, null, edgeList, distance[vSource][vSink]);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException in case the provided vertex factory creates vertices which
     *         are already in the original graph
     */
    @Override
    public double getPathWeight(V source, V sink)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        run();
        return distance[vertexIndices.get(source)][vertexIndices.get(sink)];
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException in case the provided vertex factory creates vertices which
     *         are already in the original graph
     * @throws NegativeCycleDetectedException in case a negative weight cycle is detected
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        run();
        return new JohnsonSingleSourcePaths(source);
    }

    /**
     * Executes the actual algorithm.
     */
    private void run()
    {
        if (pred != null) {
            return;
        }
        GraphTests.requireDirectedOrUndirected(graph);

        E detectedNegativeEdge = null;
        for (E e : graph.edgeSet()) {
            if (comparator.compare(graph.getEdgeWeight(e), 0.0) < 0) {
                detectedNegativeEdge = e;
                break;
            }
        }

        if (detectedNegativeEdge != null) {
            if (graph.getType().isUndirected()) {
                V source = graph.getEdgeSource(detectedNegativeEdge);
                double weight = graph.getEdgeWeight(detectedNegativeEdge);
                GraphWalk<V,
                    E> cycle = new GraphWalk<>(
                        graph, source, source,
                        Arrays.asList(detectedNegativeEdge, detectedNegativeEdge), 2d * weight);
                throw new NegativeCycleDetectedException(
                    GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE, cycle);
            }
            runWithNegativeEdgeWeights(graph);
        } else {
            runWithPositiveEdgeWeights(graph);
        }
    }

    /**
     * Graph has no edges with negative weights. Only perform the last step of Johnson's algorithm:
     * run Dijkstra's algorithm from every vertex.
     *
     * @param g the input graph
     */
    private void runWithPositiveEdgeWeights(Graph<V, E> g)
    {
        /*
         * Create vertex numbering for array representation of results.
         */
        vertexIndices = computeVertexIndices(g);
        final int n = g.vertexSet().size();
        distance = new double[n][n];
        pred = TypeUtil.uncheckedCast(new Object[n][n]);

        /*
         * Execute Dijkstra multiple times
         */
        for (V v : g.vertexSet()) {
            DijkstraClosestFirstIterator<V, E> it =
                new DijkstraClosestFirstIterator<>(g, v, Double.POSITIVE_INFINITY);
            while (it.hasNext()) {
                it.next();
            }
            Map<V, Pair<Double, E>> distanceAndPredecessorMap = it.getDistanceAndPredecessorMap();

            // transform result
            for (V u : g.vertexSet()) {
                Pair<Double, E> pair = distanceAndPredecessorMap
                    .getOrDefault(u, Pair.of(Double.POSITIVE_INFINITY, null));
                distance[vertexIndices.get(v)][vertexIndices.get(u)] = pair.getFirst();
                pred[vertexIndices.get(v)][vertexIndices.get(u)] = pair.getSecond();
            }
        }
    }

    /**
     * Graph contains edges with negative weights. Transform the input graph, thereby ensuring that
     * there are no edges with negative weights. Then run Dijkstra's algorithm for all vertices.
     *
     * @param g the input graph
     */
    private void runWithNegativeEdgeWeights(Graph<V, E> g)
    {
        /*
         * Compute vertex weights using Bellman-Ford
         */
        Map<V, Double> vertexWeights = computeVertexWeights(g);

        /*
         * Compute new non-negative edge weights
         */
        Map<E, Double> newEdgeWeights = new HashMap<>();
        for (E e : g.edgeSet()) {
            V u = g.getEdgeSource(e);
            V v = g.getEdgeTarget(e);
            double weight = g.getEdgeWeight(e);
            newEdgeWeights.put(e, weight + vertexWeights.get(u) - vertexWeights.get(v));
        }

        /*
         * Create graph with new edge weights
         */
        Graph<V, E> newEdgeWeightsGraph = new AsWeightedGraph<>(g, newEdgeWeights);

        /*
         * Create vertex numbering, for array representation of results
         */
        vertexIndices = computeVertexIndices(g);
        final int n = g.vertexSet().size();
        distance = new double[n][n];
        pred = TypeUtil.uncheckedCast(new Object[n][n]);

        /*
         * Run Dijkstra using new weights for all vertices
         */
        for (V v : g.vertexSet()) {
            DijkstraClosestFirstIterator<V, E> it = new DijkstraClosestFirstIterator<>(
                newEdgeWeightsGraph, v, Double.POSITIVE_INFINITY);
            while (it.hasNext()) {
                it.next();
            }
            Map<V, Pair<Double, E>> distanceAndPredecessorMap = it.getDistanceAndPredecessorMap();

            // transform distances to original weights
            for (V u : g.vertexSet()) {
                Pair<Double, E> oldPair = distanceAndPredecessorMap.get(u);

                Pair<Double, E> newPair;
                if (oldPair != null) {
                    newPair = Pair.of(
                        oldPair.getFirst() - vertexWeights.get(v) + vertexWeights.get(u),
                        oldPair.getSecond());
                } else {
                    newPair = Pair.of(Double.POSITIVE_INFINITY, null);
                }

                distance[vertexIndices.get(v)][vertexIndices.get(u)] = newPair.getFirst();
                pred[vertexIndices.get(v)][vertexIndices.get(u)] = newPair.getSecond();
            }
        }

    }

    /**
     * Compute vertex weights for edge re-weighting using Bellman-Ford.
     *
     * @param g the input graph
     * @return the vertex weights
     */
    private Map<V, Double> computeVertexWeights(Graph<V, E> g)
    {
        assert g.getType().isDirected();

        // create extra graph
        Graph<V,
            E> extraGraph = GraphTypeBuilder
                .<V, E> directed().allowingMultipleEdges(true).allowingSelfLoops(true)
                .edgeSupplier(graph.getEdgeSupplier()).vertexSupplier(graph.getVertexSupplier())
                .buildGraph();

        // add new vertex
        V s = extraGraph.addVertex();
        if (s == null) {
            throw new IllegalArgumentException(
                "Invalid vertex supplier (does not return unique vertices on each call).");
        }

        // add new edges with zero weight
        Map<E, Double> zeroWeightFunction = new HashMap<>();
        for (V v : g.vertexSet()) {
            extraGraph.addVertex(v);
            zeroWeightFunction.put(extraGraph.addEdge(s, v), 0d);
        }

        /*
         * Union extra and input graph
         */
        Graph<V, E> unionGraph =
            new AsGraphUnion<>(new AsWeightedGraph<>(extraGraph, zeroWeightFunction), g);

        /*
         * Run Bellman-Ford from new vertex
         */
        SingleSourcePaths<V, E> paths = new BellmanFordShortestPath<>(unionGraph).getPaths(s);
        Map<V, Double> weights = new HashMap<>();
        for (V v : g.vertexSet()) {
            weights.put(v, paths.getWeight(v));
        }
        return weights;
    }

    /**
     * Compute a unique integer for each vertex of the graph
     * 
     * @param g the graph
     * @return a map with the result
     */
    private Map<V, Integer> computeVertexIndices(Graph<V, E> g)
    {
        Map<V, Integer> numbering = new HashMap<>();
        int num = 0;
        for (V v : g.vertexSet()) {
            numbering.put(v, num++);
        }
        return numbering;
    }

    class JohnsonSingleSourcePaths
        implements
        SingleSourcePaths<V, E>
    {
        private V source;

        public JohnsonSingleSourcePaths(V source)
        {
            this.source = source;
        }

        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        @Override
        public V getSourceVertex()
        {
            return source;
        }

        @Override
        public double getWeight(V sink)
        {
            return JohnsonShortestPaths.this.getPathWeight(source, sink);
        }

        @Override
        public GraphPath<V, E> getPath(V sink)
        {
            return JohnsonShortestPaths.this.getPath(source, sink);
        }

    }

}
