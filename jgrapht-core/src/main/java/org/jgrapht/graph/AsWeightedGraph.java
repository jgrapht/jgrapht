/*
 * (C) Copyright 2018-2018, by Lukas Harzenetter and Contributors.
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
package org.jgrapht.graph;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.GraphType;

/**
 * Provides a weighted view on a graph.
 *
 * Algorithms designed for weighted graphs should also work on unweighted graphs. This class
 * emulates a weighted graph based on a backing one by handling the storage of edge weights
 * internally and passing all other operations on the underlying graph. As a consequence, the edges
 * returned are the edges of the original graph.
 *
 * Additionally, if the underlying graph is weighted, weight changes can be
 * propagated to it. By default, this happens automatically when the backing
 * graph is weighted; this behavior can be disabled via an optional constructor
 * parameter.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class AsWeightedGraph<V, E>
    extends GraphDelegator<V, E>
    implements Serializable, Graph<V, E>
{

    private static final long serialVersionUID = -6838132233557L;
    private final Map<E, Double> weights;
    private final boolean writeWeightsThrough;

    /**
     * Constructor for AsWeightedGraph which enables weight write propagation
     * automatically if the backing graph is weighted (otherwise, weight changes
     * only affect the weighted view).
     *
     * @param graph   the backing graph over which an weighted view is to be created.
     * @param weights the map containing the edge weights.
     * @throws NullPointerException if the graph or the weights are null.
     */
    public AsWeightedGraph(Graph<V, E> graph, Map<E, Double> weights)
    {
        this(graph, weights, graph.getType().isWeighted());
    }

    /**
     * Constructor for AsWeightedGraph which allows weight write propagation
     * to be requested explicitly.
     *
     * @param graph               the backing graph over which an weighted view is to be created
     * @param weights             the map containing the edge weights
     * @param writeWeightsThrough if set to true, the weights will get propagated to the backing
     *                            graph in the <code>setEdgeWeight()</code> method.
     * @throws NullPointerException     if the graph or the weights are null
     * @throws IllegalArgumentException if <code>writeWeightsThrough</code> is set to true and
     *                                  <code>graph</code> is not a weighted graph
     */
    public AsWeightedGraph(Graph<V, E> graph, Map<E, Double> weights, boolean writeWeightsThrough)
    {
        super(graph);
        this.weights = Objects.requireNonNull(weights);
        this.writeWeightsThrough = writeWeightsThrough;

        if (this.writeWeightsThrough) {
            GraphTests.requireWeighted(graph);
        }
    }

    /**
     * Returns the weight assigned to a given edge.
     * If there is no edge weight set for the given edge, the value of the backing graph's
     * getEdgeWeight method is returned.
     *
     * @param e edge of interest
     * @return the edge weight
     * @throws NullPointerException if the edge is null
     */
    @Override public double getEdgeWeight(E e)
    {
        Double weight = this.weights.get(e);

        if (Objects.isNull(weight)) {
            weight = super.getEdgeWeight(e);
        }

        return weight;
    }

    /**
     * Assigns a weight to an edge.
     *
     * @param e      edge on which to set weight
     * @param weight new weight for edge
     * @throws NullPointerException if the edge is null
     */
    @Override public void setEdgeWeight(E e, double weight)
    {
        this.weights.put(Objects.requireNonNull(e), weight);

        if (this.writeWeightsThrough) {
            this.getDelegate().setEdgeWeight(e, weight);
        }
    }

    @Override public GraphType getType()
    {
        return super.getType().asWeighted();
    }

}
