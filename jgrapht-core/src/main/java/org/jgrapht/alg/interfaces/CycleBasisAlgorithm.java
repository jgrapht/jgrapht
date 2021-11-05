/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.cycle.*;

import java.io.*;
import java.util.*;

/**
 * Allows to derive an undirected <a href="https://en.wikipedia.org/wiki/Cycle_basis">cycle
 * basis</a> of a given graph.
 * 
 * <p>
 * Note that undirected cycle bases are defined for both undirected and directed graphs. For a
 * discussion of different kinds of cycle bases in graphs see the following paper.
 * <ul>
 * <li>Christian Liebchen, and Romeo Rizzi. Classes of Cycle Bases. Discrete Applied Mathematics,
 * 155(3), 337-355, 2007.</li>
 * </ul>
 *
 * @param <V> vertex the graph vertex type
 * @param <E> edge the graph edge type
 * 
 * @author Dimitrios Michail
 */
public interface CycleBasisAlgorithm<V, E>
{
    /**
     * Return a list of cycles forming an undirected cycle basis of a graph.
     * 
     * @return an undirected cycle basis
     */
    CycleBasis<V, E> getCycleBasis();

    /**
     * An undirected cycle basis.
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface CycleBasis<V, E>
    {
        /**
         * Return the set of cycles of the cycle basis.
         *
         * @return the set of cycles of the cycle basis
         */
        Set<List<E>> getCycles();

        /**
         * Get the length of the cycle basis. The length of the cycle basis is the sum of the
         * lengths of its cycles. The length of a cycle is the total number of edges of the cycle.
         * 
         * @return the length of the cycles basis
         */
        int getLength();

        /**
         * Get the weight of the cycle basis. The weight of the cycle basis is the sum of the
         * weights of its cycles. The weight of a cycle is the sum of the weights of its edges.
         * 
         * @return the length of the cycles basis
         */
        double getWeight();

        /**
         * Return the set of cycles of the cycle basis.
         *
         * @return the set of cycles of the cycle basis
         */
        Set<GraphPath<V, E>> getCyclesAsGraphPaths();
    }

    /**
     * Default implementation of the undirected cycle basis interface.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class CycleBasisImpl<V, E>
        implements
        CycleBasis<V, E>,
        Serializable
    {
        private static final long serialVersionUID = -1420882459022219505L;

        private final Graph<V, E> graph;
        private final Set<List<E>> cycles;
        private Set<GraphPath<V, E>> graphPaths;
        private final int length;
        private final double weight;

        /**
         * Construct a new instance.
         *
         * @param graph the graph
         */
        public CycleBasisImpl(Graph<V, E> graph)
        {
            this(graph, Collections.emptySet(), 0, 0d);
        }

        /**
         * Construct a new instance.
         *
         * @param graph the graph
         * @param cycles the cycles of the basis
         * @param length the length of the cycle basis
         * @param weight the weight of the cycle basis
         */
        public CycleBasisImpl(Graph<V, E> graph, Set<List<E>> cycles, int length, double weight)
        {
            this.graph = graph;
            this.cycles = Collections.unmodifiableSet(cycles);
            this.length = length;
            this.weight = weight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<List<E>> getCycles()
        {
            return cycles;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getLength()
        {
            return length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight()
        {
            return weight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<GraphPath<V, E>> getCyclesAsGraphPaths()
        {
            // lazily construct
            if (graphPaths == null) {
                graphPaths = new LinkedHashSet<>();
                for (List<E> cycle : cycles) {
                    graphPaths.add(Cycles.simpleCycleToGraphPath(graph, cycle));
                }
            }
            return Collections.unmodifiableSet(graphPaths);
        }

    }

}
