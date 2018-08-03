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
package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a <a href="https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">
 * minimum cost flow problem</a>. It serves as input for the minimum cost flow algorithms.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see MinimumCostFlowAlgorithm
 * @since July 2018
 */
public class MinimumCostFlowProblem<V, E> {
    /**
     * The flow network
     */
    Graph<V, E> graph;
    /**
     * The mapping from nodes to the corresponding supplies of the nodes. In every feasible for every node of
     * the network the sum of all outgoing flows minus the sum of all incoming flows should be equal to the
     * supply of the node. flow This mapping doesn't need to contain transhipment nodes (with zero supply).
     */
    Map<V, Integer> supplyMap;
    /**
     * The mapping from edges of the network to the corresponding lower bounds of the flow on edges.
     * Every feasible flow should satisfy the property that every arc's flow should be no less than
     * its lower bound. This is an optional part of the problem. This mapping doesn't need to contains
     * edges with zero lower capacities.
     */
    Map<E, Integer> lowerCapacityMap;
    /**
     * The mapping from edges of the network to the corresponding upper bounds of the flow on edges.
     * Every feasible flow should satisfy the property that the flow on an arc doesn't exceeds its
     * upper bound. This mapping must contain all edges of the network.
     */
    Map<E, Integer> upperCapacityMap;

    /**
     * Constructs a new minimum cost flow problem without arcs' lower bounds.
     *
     * @param graph            the flow network
     * @param supplyMap        the supply map of the network
     * @param upperCapacityMap the lower capacity map of the network
     */
    public MinimumCostFlowProblem(Graph<V, E> graph, Map<V, Integer> supplyMap, Map<E, Integer> upperCapacityMap) {
        this(graph, supplyMap, upperCapacityMap, new HashMap<>(0));
    }

    /**
     * Constructs a new minimum cost flow problem
     *
     * @param graph            the flow network
     * @param supplyMap        the supply map of the network
     * @param upperCapacityMap the lower capacity map of the network
     * @param lowerCapacityMap the upper capacity map of the network
     */
    public MinimumCostFlowProblem(Graph<V, E> graph, Map<V, Integer> supplyMap, Map<E, Integer> upperCapacityMap, Map<E, Integer> lowerCapacityMap) {
        this.graph = graph;
        this.supplyMap = supplyMap;
        this.upperCapacityMap = upperCapacityMap;
        this.lowerCapacityMap = lowerCapacityMap;
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
     * Returns the supply map of the flow network
     *
     * @return the supply map of the flow network
     */
    public Map<V, Integer> getSupplyMap() {
        return supplyMap;
    }

    /**
     * Returns the lower capacity map of the flow network
     *
     * @return the lower capacity of the flow network
     */
    public Map<E, Integer> getLowerCapacityMap() {
        return lowerCapacityMap;
    }

    /**
     * Returns the upper capacity map of the flow network
     *
     * @return the upper capacity of the flow network
     */
    public Map<E, Integer> getUpperCapacityMap() {
        return upperCapacityMap;
    }
}
