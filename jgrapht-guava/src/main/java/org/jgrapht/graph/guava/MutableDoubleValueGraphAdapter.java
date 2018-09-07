/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph.guava;

import com.google.common.graph.*;
import org.jgrapht.Graph;

import java.io.*;
import java.util.function.*;

/**
 * A graph adapter class using Guava's {@link MutableValueGraph} specialized with double values.
 * 
 * <p>
 * The adapter uses class {@link EndpointPair} to represent edges. Changes in the adapter such as
 * adding or removing vertices and edges are reflected in the underlying value graph.
 *
 * <p>
 * Each edge in {@link MutableValueGraph} is associated with a double value which is mapped to the
 * edge weight in the resulting {@link Graph}. Thus, the graph is weighted and calling methods
 * {@link #getEdgeWeight(Object)} and {@link #setEdgeWeight(EndpointPair, double)} will get and set
 * the value of an edge.
 * 
 * <p>
 * See the example below on how to create such an adapter: <blockquote>
 * 
 * <pre>
 * MutableValueGraph&lt;String, Double&gt; mutableValueGraph =
 *     ValueGraphBuilder.directed().allowsSelfLoops(true).build();
 * 
 * mutableValueGraph.addNode("v1");
 * mutableValueGraph.addNode("v2");
 * mutableValueGraph.putEdgeValue("v1", "v2", 3.0);
 * 
 * Graph&lt;String, EndpointPair&lt;String&gt;&gt; graph = new MutableDoubleValueGraphAdapter&lt;&gt;(mutableValueGraph);
 * 
 * System.out.println(graph.getEdgeWeight(EndpointPair.ordered("v1", "v2")); // outputs 3.0
 * 
 * graph.setEdgeWeight(EndpointPair.ordered("v1", "v2"), 7.0);
 * 
 * System.out.println(graph.getEdgeWeight(EndpointPair.ordered("v1", "v2")); // outputs 7.0
 * </pre>
 * 
 * </blockquote>
 *
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 */
public class MutableDoubleValueGraphAdapter<V>
    extends
    MutableValueGraphAdapter<V, Double>
{
    private static final long serialVersionUID = -6335845255406679994L;

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     */
    public MutableDoubleValueGraphAdapter(MutableValueGraph<V, Double> valueGraph)
    {
        this(valueGraph, null, null);
    }

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     */
    public MutableDoubleValueGraphAdapter(
        MutableValueGraph<V, Double> valueGraph, Supplier<V> vertexSupplier,
        Supplier<EndpointPair<V>> edgeSupplier)
    {
        super(
            valueGraph, Graph.DEFAULT_EDGE_WEIGHT, (ToDoubleFunction<Double> & Serializable) x -> x,
            vertexSupplier, edgeSupplier);
    }

    @Override
    public void setEdgeWeight(EndpointPair<V> e, double weight)
    {
        if (e == null) {
            throw new NullPointerException();
        }
        if (!containsEdge(e)) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
        super.valueGraph.putEdgeValue(e.nodeU(), e.nodeV(), weight);
    }

}
