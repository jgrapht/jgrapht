/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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

import org.jgrapht.*;

import java.util.*;
import java.util.stream.*;

/**
 * Allows to check the planarity of the graph. A graph is defined to be
 * <a href="https://en.wikipedia.org/wiki/Planar_graph">planar</a> if it can be drawn on a
 * two-dimensional plane without any of its edges crossing.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 */
public interface PlanarityTestingAlgorithm<V, E>
{

    /**
     * Tests the planarity of the {@code graph}. Returns true if the input graph is planar, false
     * otherwise. If this method returns true, the combinatorial embedding of the {@code graph} is
     * provided after the call to the {@link PlanarityTestingAlgorithm#getEmbedding()}. Otherwise, a
     * Kuratowski subdivision is provided after the call to the
     * {@link PlanarityTestingAlgorithm#getKuratowskiSubdivision()}.
     *
     * @return {@code true} if the {@code graph} is planar, false otherwise
     */
    boolean isPlanar();

    /**
     * Computes combinatorial embedding of the input {@code graph}. This method will return a valid
     * result only if the {@code graph} is planar. For more information on the combinatorial
     * embedding, see {@link PlanarityTestingAlgorithm.Embedding}
     *
     * @return combinatorial embedding of the input {@code graph}
     */
    Embedding<V, E> getEmbedding();

    /**
     * Extracts a Kuratowski subdivision from the {@code graph}. The returned value certifies the
     * nonplanarity of the graph. The returned certificate can be verified through the call to the
     * {@link org.jgrapht.GraphTests#isKuratowskiSubdivision(Graph)}. This method will return a
     * valid result only if the {@code graph} is not planar.
     *
     * @return a Kuratowski subdivision from the {@code graph}
     */
    Graph<V, E> getKuratowskiSubdivision();

    /**
     * A
     * <a href="https://en.wikipedia.org/wiki/Graph_embedding#Combinatorial_embedding">combinatorial
     * embedding</a> of the graph. It is represented as the edges ordered <b>clockwise</b> around
     * the vertices. The edge order around the vertices is sufficient to embed the graph on a plane,
     * i.e. assign coordinates to its vertices and draw its edges such that none of the cross.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @author Timofey Chudakov
     */
    interface Embedding<V, E>
    {
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
    class EmbeddingImpl<V, E>
        implements
        Embedding<V, E>
    {
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
         * @param graph the {@code graph}
         * @param embeddingMap map from vertices of {@code graph} to the clockwise order of edges
         */
        public EmbeddingImpl(Graph<V, E> graph, Map<V, List<E>> embeddingMap)
        {
            this.graph = graph;
            this.embeddingMap = embeddingMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<E> getEdgesAround(V vertex)
        {
            return embeddingMap.get(vertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder("[");
            for (Map.Entry<V, List<E>> entry : embeddingMap.entrySet()) {
                builder
                    .append(entry.getKey().toString()).append(" -> ")
                    .append(
                        entry
                            .getValue().stream()
                            .map(e -> Graphs.getOppositeVertex(graph, e, entry.getKey()).toString())
                            .collect(Collectors.joining(", ", "[", "]")))
                    .append(", ");
            }
            return builder.append("]").toString();
        }
    }
}
