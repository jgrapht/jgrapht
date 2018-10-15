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
public interface MinimumCostFlowAlgorithm<V, E> extends FlowAlgorithm<V,E>{

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
     * Represents a minimum cost flow.
     *
     * @param <E> graph edge type
     */
    interface MinimumCostFlow<E> extends Flow<E>{
        /**
         * Returns the cost of the flow
         *
         * @return the cost of the flow
         */
        double getCost();
    }

    /**
     * Default implementation of the {@link MinimumCostFlow}
     *
     * @param <E> graph edge type
     */
    class MinimumCostFlowImpl<E> extends FlowImpl<E> implements MinimumCostFlow<E> {
        /**
         * The cost of the flow defined by the mapping {@code flowMap}
         */
        double cost;

        /**
         * Constructs a new instance of minimum cost flow
         *
         * @param cost    the cost of the flow
         * @param flowMap the mapping defining the flow on the network
         */
        public MinimumCostFlowImpl(double cost, Map<E, Double> flowMap) {
            super(flowMap);
            this.cost = cost;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getCost() {
            return cost;
        }
    }
}
