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

import java.util.*;

/**
 * Base class for the bidirectional shortest path algorithms. Currently known extensions are
 * {@link BidirectionalDijkstraShortestPath} and {@link BidirectionalAStarShortestPath}.
 *
 * @param <V> vertices type
 * @param <E> edges type
 * @author Dimitrios Michail
 */
public abstract class BaseBidirectionalShortestPathAlgorithm<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     */
    public BaseBidirectionalShortestPathAlgorithm(Graph<V, E> graph)
    {
        super(graph);
    }

    /**
     * Builds shortest path between {@code source} and {@code sink} based on the information
     * provided by search frontiers and common vertex.
     *
     * @param forwardFrontier forward direction frontier
     * @param backwardFrontier backward direction frontier
     * @param weight weight of the shortest path
     * @param source path source
     * @param commonVertex path common vertex
     * @param sink path sink
     * @return shortest path between source and sink
     */
    protected GraphPath<V, E> createPath(
        BaseSearchFrontier<V, E> forwardFrontier, BaseSearchFrontier<V, E> backwardFrontier,
        double weight, V source, V commonVertex, V sink)
    {
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
     * Base class of the search frontier used by bidirectional shortest path algorithms.
     *
     * @param <V> vertices type
     * @param <E> edges type
     */
    abstract static class BaseSearchFrontier<V, E>
    {
        /**
         * Frontier`s graph.
         */
        final Graph<V, E> graph;

        /**
         * Constructs instance for a given {@code graph}.
         *
         * @param graph graph
         */
        BaseSearchFrontier(Graph<V, E> graph)
        {
            this.graph = graph;
        }

        /**
         * Returns distance to vertex {@code v} computed so far.
         *
         * @param v vertex
         * @return distance to {@code v}
         */
        abstract double getDistance(V v);

        /**
         * Returns edge which connects {@code v} to its predecessor in the shortest paths tree of
         * this frontier.
         *
         * @param v vertex
         * @return edge in shortest paths tree
         */
        abstract E getTreeEdge(V v);
    }
}
