/*
 * (C) Copyright 2018-2018, by Lukas Harzenetter and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Provides a weighted view of a graph. The class stores edge weights internally.
 * All @link{getEdgeWeight} calls are handled by this view; all other graph operations are
 * propagated to the graph backing this view.
 *
 * <p>
 * This class can be used to make an unweighted graph weighted, to override the weights of a
 * weighted graph, or to provide different weighted views of the same underlying graph. For
 * instance, the edges of a graph representing a road network might have two weights associated with
 * them: a travel time and a travel distance. Instead of creating two weighted graphs of the same
 * network, one would simply create two weighted views of the same underlying graph.
 *
 * <p>
 * This class offers two ways to associate a weight with an edge:
 * <ol>
 * <li>Explicitly through a map which contains a mapping from an edge to a weight</li>
 * <li>Implicitly through a function which computes a weight for a given edge</li>
 * </ol>
 * In the first way, the map is used to lookup edge weights. In the second way, a function is
 * provided to calculate the weight of an edge. If the map does not contain a particular edge, or
 * the function does not provide a weight for a particular edge, the @link{getEdgeWeight} call is
 * propagated to the backing graph.
 *
 * Finally, the view provides a @link{setEdgeWeight} method. This method behaves differently
 * depending on how the view is constructed. See @link{setEdgeWeight} for details.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class AsWeightedGraph<V, E>
    extends
    GraphDelegator<V, E>
    implements
    Serializable,
    Graph<V, E>
{

    private static final long serialVersionUID = -6838132233557L;
    private final Function<E, Double> weightFunction;
    private final Map<E, Double> weights;
    private final boolean writeWeightsThrough;
    private final boolean cacheWeights;

    /**
     * Constructor for AsWeightedGraph where the weights are provided through a map. Invocations of
     * the @link{setEdgeWeight} method will update the map. Moreover, calls to @link{setEdgeWeight}
     * are propagated to the underlying graph.
     *
     * @param graph the backing graph over which a weighted view is to be created.
     * @param weights the map containing the edge weights.
     * @throws NullPointerException if the graph or the weights are null.
     */
    public AsWeightedGraph(Graph<V, E> graph, Map<E, Double> weights)
    {
        this(graph, weights, graph.getType().isWeighted());
    }

    /**
     * Constructor for AsWeightedGraph which allows weight write propagation to be requested
     * explicitly.
     *
     * @param graph the backing graph over which an weighted view is to be created
     * @param weights the map containing the edge weights
     * @param writeWeightsThrough if set to true, the weights will get propagated to the backing
     *        graph in the <code>setEdgeWeight()</code> method.
     * @throws NullPointerException if the graph or the weights are null
     * @throws IllegalArgumentException if <code>writeWeightsThrough</code> is set to true and
     *         <code>graph</code> is not a weighted graph
     */
    public AsWeightedGraph(Graph<V, E> graph, Map<E, Double> weights, boolean writeWeightsThrough)
    {
        super(graph);
        this.weights = Objects.requireNonNull(weights);
        this.weightFunction = null;
        this.cacheWeights = false;
        this.writeWeightsThrough = writeWeightsThrough;

        if (this.writeWeightsThrough)
            GraphTests.requireWeighted(graph);
    }

    /**
     * Constructor for AsWeightedGraph which uses a weight function to compute edge weights. When
     * the weight of an edge is queried, the weight function is invoked. If
     * <code>cacheWeights</code> is set to <code>true</code>, the weight of an edge returned by the
     * <code>weightFunction</code> after its first invocation is stored in a map. The weight of an
     * edge returned by subsequent calls to @link{getEdgeWeight} for the same edge will then be
     * retrieved directly from the map, instead of re-invoking the weight function. If
     * <code>cacheWeights</code> is set to <code>false</code>, each invocation of
     * the @link{getEdgeWeight} method will invoke the weight function. Caching the edge weights is
     * particularly useful when pre-computing all edge weights is expensive and it is expected that
     * the weights of only a subset of all edges will be queried.
     *
     * @param graph the backing graph over which an weighted view is to be created
     * @param weightFunction function which maps an edge to a weight
     * @param cacheWeights if set to <code>true</code>, weights are cached once computed by the
     *        weight function
     * @param writeWeightsThrough if set to <code>true</code>, the weight set directly by
     *        the @link{setEdgeWeight} method will be propagated to the backing graph.
     * @throws NullPointerException if the graph or the weight function is null
     * @throws IllegalArgumentException if <code>writeWeightsThrough</code> is set to true and
     *         <code>graph</code> is not a weighted graph
     */
    public AsWeightedGraph(
        Graph<V, E> graph, Function<E, Double> weightFunction, boolean cacheWeights,
        boolean writeWeightsThrough)
    {
        super(graph);
        this.weightFunction = Objects.requireNonNull(weightFunction);
        this.cacheWeights = cacheWeights;
        this.writeWeightsThrough = writeWeightsThrough;
        this.weights = new HashMap<>();

        if (this.writeWeightsThrough)
            GraphTests.requireWeighted(graph);
    }

    /**
     * Returns the weight assigned to a given edge. If weights are provided through a map, first a
     * map lookup is performed. If the edge is not found, the @link{getEdgeWeight} method of the
     * underlying graph is invoked instead. If, on the other hand, the weights are provided through
     * a function, this method will first attempt to lookup the weight of an edge in the cache (that
     * is, if <code>cacheWeights</code> is set to <code>true</code> in the constructor). If caching
     * was disabled, or the edge could not be found in the cache, the weight function is invoked. If
     * the function does not provide a weight for a given edge, the call is again propagated to the
     * underlying graph.
     *
     * @param e edge of interest
     * @return the edge weight
     * @throws NullPointerException if the edge is null
     */
    @Override
    public double getEdgeWeight(E e)
    {
        Double weight;
        if (weightFunction != null) {
            if (cacheWeights) // If weights are cached, check map first before invoking the weight
                              // function
                weight = weights.computeIfAbsent(e, weightFunction);
            else
                weight = weightFunction.apply(e);
        } else {
            weight = weights.get(e);
        }

        if (Objects.isNull(weight))
            weight = super.getEdgeWeight(e);

        return weight;
    }

    /**
     * Assigns a weight to an edge. If <code>writeWeightsThrough</code> is set to <code>true</code>,
     * the same weight is set in the backing graph. If this class was constructed using a weight
     * function, it only makes sense to invoke this method when <code>cacheWeights</code> is set to
     * true. This method can then be used to preset weights in the cache, or to overwrite existing
     * values.
     *
     * @param e edge on which to set weight
     * @param weight new weight for edge
     * @throws NullPointerException if the edge is null
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        assert weightFunction == null
            || cacheWeights : "Cannot set an edge weight when a weight function is used and caching is disabled";
        assert e != null;

        this.weights.put(e, weight);

        if (this.writeWeightsThrough)
            this.getDelegate().setEdgeWeight(e, weight);
    }

    @Override
    public GraphType getType()
    {
        return super.getType().asWeighted();
    }

}
