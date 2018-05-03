/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

/**
 * The algorithm determines the k shortest simple paths in increasing order of weight. Weights can
 * be negative (but no negative cycle is allowed), and paths can be constrained by a maximum number
 * of edges. Graphs with multiple (parallel) edges are allowed.
 *
 * <p>
 * The algorithm is a variant of the Bellman-Ford algorithm but instead of only storing the best
 * path it stores the "k" best paths at each pass, yielding a complexity of $O(k \cdot n \cdot (m^2))$ where $m$ is
 * the number of edges and $n$ is the number of vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @since July 5, 2007
 * @deprecated In favor of {@link KShortestSimplePaths}
 */
@Deprecated
public class KShortestPaths<V, E>
    implements KShortestPathAlgorithm<V, E>
{
    /**
     * Graph on which shortest paths are searched.
     */
    private Graph<V, E> graph;

    private int nMaxHops;

    private int nPaths;

    private PathValidator<V, E> pathValidator;

    /**
     * Constructs an object to compute ranking shortest paths in a graph.
     *
     * @param graph graph on which shortest paths are searched
     * @param k number of paths to be computed
     */
    public KShortestPaths(Graph<V, E> graph, int k)
    {
        this(graph, k, null);
    }

    /**
     * Constructs an object to compute ranking shortest paths in a graph. A non-null path validator
     * may be used to accept/deny paths according to some external logic. These validations will be
     * used in addition to the basic path validations which are that the path is from start to
     * target with no loops.
     *
     * @param graph graph on which shortest paths are searched.
     * @param k number of paths to be computed.
     * @param pathValidator the path validator to use
     * @throws IllegalArgumentException if k is negative or 0.
     */
    public KShortestPaths(Graph<V, E> graph, int k, PathValidator<V, E> pathValidator)
    {
        this(graph, k, graph.vertexSet().size() - 1, pathValidator);
    }

    /**
     * Constructs an object to calculate ranking shortest paths in a graph.
     *
     * @param graph graph on which shortest paths are searched
     * @param k number of ranking paths between the start vertex and an end vertex
     * @param nMaxHops maximum number of edges of the calculated paths
     *
     * @throws IllegalArgumentException if k is negative or 0.
     * @throws IllegalArgumentException if nMaxHops is negative or 0.
     */
    public KShortestPaths(Graph<V, E> graph, int k, int nMaxHops)
    {
        this(graph, k, nMaxHops, null);
    }

    /**
     * Constructs an object to calculate ranking shortest paths in a graph. A non-null path
     * validator may be used to accept/deny paths according to some external logic. These
     * validations will be used in addition to the basic path validations which are that the path is
     * from start to target with no loops.
     *
     * @param graph graph on which shortest paths are searched
     * @param k number of ranking paths between the start vertex and the end vertex
     * @param nMaxHops maximum number of edges of the calculated paths
     * @param pathValidator the path validator to use
     *
     * @throws IllegalArgumentException if k is negative or 0.
     * @throws IllegalArgumentException if nMaxHops is negative or 0.
     */
    public KShortestPaths(Graph<V, E> graph, int k, int nMaxHops, PathValidator<V, E> pathValidator)
    {
        this.graph = Objects.requireNonNull(graph, "graph is null");
        if (k <= 0) {
            throw new IllegalArgumentException("Number of paths must be positive");
        }
        this.nMaxHops = nMaxHops;
        if (nMaxHops <= 0) {
            throw new IllegalArgumentException("Max number of hops must be positive");
        }
        this.nPaths = k;
        this.pathValidator = pathValidator;
    }

    /**
     * Returns the $k$ shortest simple paths in increasing order of weight.
     *
     * @param startVertex source vertex of the calculated paths.
     * @param endVertex target vertex of the calculated paths.
     *
     * @return list of paths between the start vertex and the end vertex
     * @throws IllegalArgumentException if the graph does not contain the startVertex or the
     *         endVertex
     * @throws IllegalArgumentException if the startVertex and the endVertex are the same vertices
     */
    @Override
    public List<GraphPath<V, E>> getPaths(V startVertex, V endVertex)
    {
        Objects.requireNonNull(startVertex, "Start vertex cannot be null");
        Objects.requireNonNull(endVertex, "End vertex cannot be null");
        if (endVertex.equals(startVertex)) {
            throw new IllegalArgumentException("The end vertex is the same as the start vertex!");
        }
        if (!graph.containsVertex(startVertex)) {
            throw new IllegalArgumentException("Graph must contain the start vertex!");
        }
        if (!graph.containsVertex(endVertex)) {
            throw new IllegalArgumentException("Graph must contain the end vertex!");
        }

        KShortestSimplePathsIterator<V, E> iter =
            new KShortestSimplePathsIterator<>(graph, startVertex, endVertex, nPaths, pathValidator);

        // at the i-th pass the shortest paths with less (or equal) than i edges
        // are calculated.
        for (int passNumber = 1; (passNumber <= nMaxHops) && iter.hasNext(); passNumber++) {
            iter.next();
        }

        List<RankingPathElement<V, E>> list = iter.getPathElements(endVertex);

        if (list == null) {
            return Collections.emptyList();
        }

        List<GraphPath<V, E>> pathList = new ArrayList<>();
        for (RankingPathElement<V, E> element : list) {
            pathList.add(
                new GraphWalk<>(
                    graph, startVertex, element.getVertex(), null, element.createEdgeListPath(),
                    element.getWeight()));
        }

        return pathList;
    }

    @Override
    public List<GraphPath<V, E>> getPaths(V source, V sink, int k)
    {
        this.nPaths = k;
        return getPaths(source, sink);
    }

}
