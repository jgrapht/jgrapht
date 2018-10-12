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

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;

import java.util.function.Function;

/**
 * This class represents a <a href="https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">
 * minimum cost flow problem</a>. It serves as input for the minimum cost flow algorithms.
 * <p>
 * The minimum cost flow problem is defined as follows:
 * \[ \begin{align} \mbox{minimize}~&amp; \sum_{e\in \delta^+(s)}c_e\cdot f_e &amp;\\
 * \mbox{s.t. }&amp;\sum_{e\in \delta^-(i)} f_e - \sum_{e\in \delta^+(i)} f_e = b_e &amp; \forall i\in V\\
 * &amp;l_e\leq f_e \leq u_e &amp; \forall e\in E
 * \end{align} \]
 * Here $\delta^+(i)$ and $\delta^-(i)$ denote the outgoing and incoming edges of vertex $i$ respectively.
 * The parameters $c_{e}$ define a cost for each unit of flow on the arc $e$, $l_{e}$ define minimum arc flow
 * and $u_{e}$ define maximum arc flow.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see MinimumCostFlowAlgorithm
 */
public class MinimumCostFlowProblem<V, E> {
    /**
     * The flow network
     */
    private Graph<V, E> graph;
    /**
     * Function specifying the demand of each node. Demands can be positive, negative or 0. The positive
     * demand nodes are the supply nodes, the nodes with 0 demand are transhipment nodes. Flow is always
     * directed from supply nodes to nodes with negative demand. Summed over all nodes, the total demand
     * should equal 0.
     */
    private Function<V, Integer> nodeDemands;
    /**
     * Function specifying the lower arc capacities. Every feasible solution to this problem should satisfy the
     * property that every arc's flow should be no less than its lower bound. This is an optional part of
     * the problem.
     */
    private Function<E, Integer> arcCapacityLowerBounds;
    /**
     * Function specifying the upper arc capacities. Every feasible solution to this problem should satisfy the
     * property that the flow on an arc doesn't exceeds its upper bound.
     */
    private Function<E, Integer> arcCapacityUpperBounds;

    /**
     * Constructs a new minimum cost flow problem without arc capacity lower bounds.
     *
     * @param graph                  the flow network
     * @param supplyMap              the node demands
     * @param arcCapacityUpperBounds the arc capacity upper bounds
     */
    public MinimumCostFlowProblem(Graph<V, E> graph, Function<V, Integer> supplyMap, Function<E, Integer> arcCapacityUpperBounds) {
        this(graph, supplyMap, arcCapacityUpperBounds, a -> 0);
    }

    /**
     * Constructs a new minimum cost flow problem
     *
     * @param graph                  the flow network
     * @param nodeDemands            the node demands
     * @param arcCapacityUpperBounds the arc capacity upper bounds
     * @param arcCapacityLowerBounds the arc capacity lower bounds
     */
    public MinimumCostFlowProblem(Graph<V, E> graph, Function<V, Integer> nodeDemands, Function<E, Integer> arcCapacityUpperBounds, Function<E, Integer> arcCapacityLowerBounds) {
        this.graph = graph;
        this.nodeDemands = nodeDemands;
        this.arcCapacityUpperBounds = arcCapacityUpperBounds;
        this.arcCapacityLowerBounds = arcCapacityLowerBounds;
    }

    /**
     * Returns the flow network
     *
     * @return the flow network
     */
    public Graph<V, E> getGraph() {
        return graph;
    }

    /**
     * Returns the supply function of the flow network
     *
     * @return the supply function of the flow network
     */
    public Function<V, Integer> getNodeDemands() {
        return nodeDemands;
    }

    /**
     * Returns the lower capacity function of the flow network
     *
     * @return the lower capacity function of the flow network
     */
    public Function<E, Integer> getArcCapacityLowerBounds() {
        return arcCapacityLowerBounds;
    }

    /**
     * Returns the upper capacity function of the flow network
     *
     * @return the upper capacity function of the flow network
     */
    public Function<E, Integer> getArcCapacityUpperBounds() {
        return arcCapacityUpperBounds;
    }
}
