/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
 * @since July 2018
 */
public interface MinimumCostFlowAlgorithm<V, E> {

    /**
     * Calculates feasible flow of minimum cost for the minimum cost flow problem. If minimum cost
     * flow in not unique, the algorithm chooses the result arbitrarily.
     *
     * @return minimum cost flow
     */
    MinimumCostFLow<E> getMinimumCostFlow();

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
    default Map<E, Integer> getFlowMap() {
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
    interface MinimumCostFLow<E> {
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
        int getFlowOnEdge(E edge);

        /**
         * Returns a mapping from the network flow edges to the corresponding flow values. The mapping
         * contains all edges of the flow network regardless of whether there is a non-zero flow on an
         * edge or not.
         *
         * @return a read-only map that defines a feasible flow of minimum cost.
         */
        Map<E, Integer> getFlowMap();
    }

    /**
     * Default implementation of the {@link MinimumCostFLow}
     *
     * @param <E> graph edge type
     */
    class MinimumCostFlowImpl<E> implements MinimumCostFLow<E> {
        /**
         * The cost of the flow defined by the mapping {@code flowMap}
         */
        double cost;
        /**
         * A mapping defining the flow on the network
         */
        private Map<E, Integer> flowMap;

        /**
         * Constructs a new instance of minimum cost flow
         *
         * @param cost    the cost of the flow
         * @param flowMap the mapping defining the flow on the network
         */
        public MinimumCostFlowImpl(double cost, Map<E, Integer> flowMap) {
            this.cost = cost;
            this.flowMap = Collections.unmodifiableMap(flowMap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<E, Integer> getFlowMap() {
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
        public int getFlowOnEdge(E edge) {
            return flowMap.get(edge);
        }
    }
}
