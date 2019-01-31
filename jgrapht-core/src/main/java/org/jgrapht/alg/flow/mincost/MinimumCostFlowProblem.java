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
package org.jgrapht.alg.flow.mincost;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;
import java.util.function.*;

/**
 * This class represents a <a href="https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">
 * minimum cost flow problem</a>. It serves as input for the minimum cost flow algorithms.
 * <p>
 * The minimum cost flow problem is defined as follows: \[ \begin{align} \mbox{minimize}~&amp;
 * \sum_{e\in \delta^+(s)}c_e\cdot f_e &amp;\\ \mbox{s.t. }&amp;\sum_{e\in \delta^-(i)} f_e -
 * \sum_{e\in \delta^+(i)} f_e = b_e &amp; \forall i\in V\\ &amp;l_e\leq f_e \leq u_e &amp; \forall
 * e\in E \end{align} \] Here $\delta^+(i)$ and $\delta^-(i)$ denote the outgoing and incoming edges
 * of vertex $i$ respectively. The parameters $c_{e}$ define a cost for each unit of flow on the arc
 * $e$, $l_{e}$ define minimum arc flow and $u_{e}$ define maximum arc flow.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see MinimumCostFlowAlgorithm
 */
public interface MinimumCostFlowProblem<V, E>
{

    /**
     * Returns the flow network
     *
     * @return the flow network
     */
    Graph<V, E> getGraph();

    /**
     * Returns a function which defines the supply and demand of each node in thet network. Supplies
     * can be positive, negative or 0. Nodes with positive negative supply are the demand nodes,
     * nodes with zero supply are the transhipment nodes. Flow is always directed from nodes with
     * postive supply to nodes with negative supply. Summed over all nodes, the total demand should
     * equal 0.
     *
     * @return supply function
     */
    Function<V, Integer> getNodeSupply();

    /**
     * Returns a function which specifies the minimum capacity of an arc. The minimum capacity is
     * the minimum amount of flow that has to go through an arc.
     *
     * @return arc capacity lower bounding function
     */
    Function<E, Integer> getArcCapacityLowerBounds();

    /**
     * Returns a function which specifies the maximum capacity of an arc. The flow through an arc
     * cannot exceed this upper bound.
     *
     * @return arc capacity upper bounding function
     */
    Function<E, Integer> getArcCapacityUpperBounds();

    /**
     * Default implementation of a Minimum Cost Flow Problem
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class MinimumCostFlowProblemImpl<V, E>
        implements
        MinimumCostFlowProblem<V, E>
    {

        private final Graph<V, E> graph;
        private final Function<V, Integer> nodeSupplies;
        private final Function<E, Integer> arcCapacityLowerBounds;
        private final Function<E, Integer> arcCapacityUpperBounds;

        /**
         * Constructs a new minimum cost flow problem without arc capacity lower bounds.
         *
         * @param graph the flow network
         * @param supplyMap the node demands
         * @param arcCapacityUpperBounds the arc capacity upper bounds
         */
        public MinimumCostFlowProblemImpl(
            Graph<V, E> graph, Function<V, Integer> supplyMap,
            Function<E, Integer> arcCapacityUpperBounds)
        {
            this(graph, supplyMap, arcCapacityUpperBounds, a -> 0);
        }

        /**
         * Constructs a new minimum cost flow problem
         *
         * @param graph the flow network
         * @param nodeSupplies the node demands
         * @param arcCapacityUpperBounds the arc capacity upper bounds
         * @param arcCapacityLowerBounds the arc capacity lower bounds
         */
        public MinimumCostFlowProblemImpl(
            Graph<V, E> graph, Function<V, Integer> nodeSupplies,
            Function<E, Integer> arcCapacityUpperBounds,
            Function<E, Integer> arcCapacityLowerBounds)
        {
            this.graph = Objects.requireNonNull(graph);
            this.nodeSupplies = Objects.requireNonNull(nodeSupplies);
            this.arcCapacityUpperBounds = Objects.requireNonNull(arcCapacityUpperBounds);
            this.arcCapacityLowerBounds = Objects.requireNonNull(arcCapacityLowerBounds);
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
        public Function<V, Integer> getNodeSupply()
        {
            return nodeSupplies;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Function<E, Integer> getArcCapacityLowerBounds()
        {
            return arcCapacityLowerBounds;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Function<E, Integer> getArcCapacityUpperBounds()
        {
            return arcCapacityUpperBounds;
        }
    }
}
