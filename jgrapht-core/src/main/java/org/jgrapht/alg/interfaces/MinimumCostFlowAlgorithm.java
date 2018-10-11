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

import java.util.Collections;
import java.util.Map;

/**
 * Allows to calculate minimum cost flow on the specified
 * <a href="https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">minimum cost flow problem</a>.
 * <p>
 * For more information see: <i>K. Ahuja, Ravindra &amp; L. Magnanti, Thomas &amp; Orlin, James. (1993). Network Flows.</i>
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Timofey Chudakov
 */
public interface MinimumCostFlowAlgorithm<V, E> {

    /**
     * Calculates feasible flow of minimum cost for the minimum cost flow problem. If minimum cost
     * flow in not unique, the algorithm chooses the result arbitrarily.
     *
     * @return minimum cost flow
     */
    MinimumCostFlow<E> getMinimumCostFlow();

    /**
     * Returns the cost of the computed minimum cost flow.
     *
     * @return the cost of a minimum cost flow.
     */
    default double getFlowCost() {
        return getMinimumCostFlow().getCost();
    }

    /**
     * Returns a <em>read-only</em> mapping from edges to the corresponding flow values.
     *
     * @return a <em>read-only</em> mapping from edges to the corresponding flow values.
     */
    default Map<E, Double> getFlowMap() {
        return getMinimumCostFlow().getFlowMap();
    }

    /**
     * For the specified {@code edge} $(u, v)$ return vertex $v$ if the flow goes from $u$ to $v$, or returns
     * vertex $u$ otherwise. For directed flow networks the result is always the head of the specified arc.
     * <p>
     * <em>Note:</em> not all minimum cost flow algorithms may support undirected graphs.
     *
     * @param edge an edge from the specified flow network
     * @return the direction of the flow on the {@code edge}
     */
    V getFlowDirection(E edge);

    /**
     * Represents a minimum cost flow.
     *
     * @param <E> graph edge type
     * @since July 2018
     */
    interface MinimumCostFlow<E> {
        /**
         * Returns the cost of the flow
         *
         * @return the cost of the flow
         */
        double getCost();

        /**
         * Returns the flow on the {@code edge}
         *
         * @param edge an edge from the flow network
         * @return the flow on the {@code edge}
         */
        double getFlow(E edge);

        /**
         * Returns a mapping from the network flow edges to the corresponding flow values. The mapping
         * contains all edges of the flow network regardless of whether there is a non-zero flow on an
         * edge or not.
         *
         * @return a read-only map that defines a feasible flow of minimum cost.
         */
        Map<E, Double> getFlowMap();
    }

    /**
     * Default implementation of the {@link MinimumCostFlow}
     *
     * @param <E> graph edge type
     */
    class MinimumCostFlowImpl<E> implements MinimumCostFlow<E> {
        /**
         * The cost of the flow defined by the mapping {@code flowMap}
         */
        double cost;
        /**
         * A mapping defining the flow on the network
         */
        private Map<E, Double> flowMap;

        /**
         * Constructs a new instance of minimum cost flow
         *
         * @param cost    the cost of the flow
         * @param flowMap the mapping defining the flow on the network
         */
        public MinimumCostFlowImpl(double cost, Map<E, Double> flowMap) {
            this.cost = cost;
            this.flowMap = Collections.unmodifiableMap(flowMap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<E, Double> getFlowMap() {
            return flowMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getCost() {
            return cost;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getFlow(E edge) {
            return flowMap.get(edge);
        }
    }
}
