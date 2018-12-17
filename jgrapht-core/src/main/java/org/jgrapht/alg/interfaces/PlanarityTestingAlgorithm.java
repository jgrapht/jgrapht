/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows to check the planarity of the graph. A graph is defined to be
 * <a href="https://en.wikipedia.org/wiki/Planar_graph">planar</a> if it can be drawn on a
 * two-dimensional plane without any of its edges crossing.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 */
public interface PlanarityTestingAlgorithm<V, E> {

    /**
     * Tests the planarity of the {@code graph}. Returns true if the input graph is planar, false otherwise.
     * If this method returns true, the combinatorial embedding of the {@code graph} is provided after the
     * call to the {@link PlanarityTestingAlgorithm#getEmbedding()}. Otherwise, a Kuratowski subdivision
     * is provided after the call to the {@link PlanarityTestingAlgorithm#getKuratowskiSubdivision()}.
     *
     * @return {@code true} if the {@code graph} is planar, false otherwise
     */
    boolean isPlanar();

    /**
     * Computes combinatorial embedding of the input {@code graph}. This method will return
     * a valid result only if the {@code graph} is planar. For more information on the combinarotial
     * embedding, see {@link PlanarityTestingAlgorithm.Embedding}
     *
     * @return combinatorial embedding of the input {@code graph}
     */
    Embedding<V, E> getEmbedding();

    /**
     * Extracts a Kuratowski subdivision from the {@code graph}. The returned value certifies the
     * nonplanarity of the graph. The returned certificate can be verified through the call to the
     * {@link PlanarityTestingAlgorithm#isKuratowskiSubdivision(Graph)}. This method will return a valid
     * result only if the {@code graph} is not planar.
     *
     * @return a Kuratowski subdivision from the {@code graph}
     */
    Graph<V, E> getKuratowskiSubdivision();

    /**
     * Checks whether the {@code graph} is a
     * <a href="https://en.wikipedia.org/wiki/Kuratowski%27s_theorem#Kuratowski_subgraphs">Kuratowski subdivision</a>.
     * Effectively checks whether the {@code graph} is a $K_{3,3}$ subdivision or $K_{5}$ subdivision
     *
     * @param graph the graph to test
     * @param <V>   the graph vertex type
     * @param <E>   the graph edge type
     * @return true if the {@code graph} is a Kuratowski subdivision, false otherwise
     */
    static <V, E> boolean isKuratowskiSubdivision(Graph<V, E> graph) {
        return isK33Subdivision(graph) || isK5Subdivision(graph);
    }

    /**
     * Checks whether the {@code graph} is a $K_{3,3}$ subdivision.
     *
     * @param graph the graph to test
     * @param <V>   the graph vertex type
     * @param <E>   the graph edge type
     * @return true if the {@code graph} is a $K_{3,3}$ subdivision, false otherwise
     */
    static <V, E> boolean isK33Subdivision(Graph<V, E> graph) {
        List<V> degree3 = new ArrayList<>();
        // collect all vertices with degree 3
        for (V vertex : graph.vertexSet()) {
            if (graph.degreeOf(vertex) == 3) {
                degree3.add(vertex);
            } else if (graph.degreeOf(vertex) != 2) {
                return false;
            }
        }
        if (degree3.size() != 6) {
            return false;
        }
        V vertex = degree3.remove(degree3.size() - 1);
        Set<V> reachable = reachableWithDegree(graph, vertex, 3);
        if (reachable.size() != 3) {
            return false;
        }
        degree3.removeAll(reachable);
        return reachable.equals(reachableWithDegree(graph, degree3.get(0), 3))
                && reachable.equals(reachableWithDegree(graph, degree3.get(1), 3));
    }

    /**
     * Checks whether the {@code graph} is a $K_5$ subdivision.
     *
     * @param graph the graph to test
     * @param <V>   the graph vertex type
     * @param <E>   the graph edge type
     * @return true if the {@code graph} is a $K_5$ subdivision, false otherwise
     */
    static <V, E> boolean isK5Subdivision(Graph<V, E> graph) {
        Set<V> degree5 = new HashSet<>();
        for (V vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree == 4) {
                degree5.add(vertex);
            } else if (degree != 2) {
                return false;
            }
        }
        if (degree5.size() != 5) {
            return false;
        }
        for (V vertex : degree5) {
            Set<V> reachable = reachableWithDegree(graph, vertex, 4);
            if (reachable.size() != 4 || !degree5.containsAll(reachable) || reachable.contains(vertex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Uses BFS to find all vertices of the {@code graph} which have a degree {@code degree}.
     * This method doesn't advance to new nodes after the it finds a node with a degree {@code degree}
     *
     * @param graph       the graph to search in
     * @param startVertex the start vertex
     * @param degree      the degree of desired vertices
     * @param <V>         the graph vertex type
     * @param <E>         the graph edge type
     * @return all vertices of the {@code graph} reachable from {@code startVertex}, which have
     * degree {@code degree}
     */
    static <V, E> Set<V> reachableWithDegree(Graph<V, E> graph, V startVertex, int degree) {
        Set<V> visited = new HashSet<>();
        Set<V> reachable = new HashSet<>();
        Queue<V> queue = new ArrayDeque<>();
        queue.add(startVertex);
        while (!queue.isEmpty()) {
            V current = queue.poll();
            visited.add(current);
            for (E e : graph.edgesOf(current)) {
                V opposite = Graphs.getOppositeVertex(graph, e, current);
                if (visited.contains(opposite)) {
                    continue;
                }
                if (graph.degreeOf(opposite) == degree) {
                    reachable.add(opposite);
                } else {
                    queue.add(opposite);
                }
            }
        }
        return reachable;
    }

    /**
     * A <a href="https://en.wikipedia.org/wiki/Graph_embedding#Combinatorial_embedding">combinatorial embedding</a>
     * of the graph. It is represented as the edges ordered <b>clockwise</b> around the vertices. The edge order
     * around the vertices is sufficient to embed the graph on a plane, i.e. assign coordinates to its vertices
     * and draw its edges such that none of the cross.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @author Timofey Chudakov
     */
    interface Embedding<V, E> {
        /**
         * Returns the clockwise order of edges incident to the {@code vertex}
         *
         * @param vertex the vertex whose incident edges are returned
         * @return the clockwise order of edges incident to the {@code vertex}
         */
        List<E> getEdgesAround(V vertex);

        /**
         * Returns the underlying {@code graph}
         *
         * @return the underlying {@code graph}
         */
        Graph<V, E> getGraph();
    }

    /**
     * Implementation of the {@link PlanarityTestingAlgorithm.Embedding}.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class EmbeddingImpl<V, E> implements Embedding<V, E> {
        /**
         * The underlying {@code graph}
         */
        private Graph<V, E> graph;
        /**
         * The map from vertices of the {@code graph} to the clockwise order of edges
         */
        private Map<V, List<E>> embeddingMap;

        /**
         * Creates new embedding of the {@code graph}
         *
         * @param graph        the {@code graph}
         * @param embeddingMap map from vertices of {@code graph} to the clockwise order of edges
         */
        public EmbeddingImpl(Graph<V, E> graph, Map<V, List<E>> embeddingMap) {
            this.graph = graph;
            this.embeddingMap = embeddingMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<E> getEdgesAround(V vertex) {
            return embeddingMap.get(vertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Graph<V, E> getGraph() {
            return graph;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for (Map.Entry<V, List<E>> entry : embeddingMap.entrySet()) {
                builder.append(entry.getKey().toString()).append(" -> ").append(entry.getValue().stream().map(e -> Graphs.getOppositeVertex(graph, e, entry.getKey()).toString()).collect(Collectors.joining(", ", "[", "]"))).append(", ");
            }
            return builder.append("]").toString();
        }
    }
}
