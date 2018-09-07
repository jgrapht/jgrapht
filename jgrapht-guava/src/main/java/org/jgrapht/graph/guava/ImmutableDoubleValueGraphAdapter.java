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
 * A graph adapter class using Guava's {@link ImmutableValueGraph} specialized with double values.
 * 
 * <p>
 * The adapter uses class {@link EndpointPair} to represent edges. Since the underlying value graph
 * is immutable, the resulting graph is unmodifiable.
 * 
 * <p>
 * Each edge in {@link ImmutableValueGraph} is associated with a double value which is mapped to the
 * edge weight in the resulting {@link Graph}. Thus, the graph is weighted and calling method
 * {@link #getEdgeWeight(Object)} will return the value of an edge.
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
 * ImmutableValueGraph&lt;String, Double&gt; immutableValueGraph = ImmutableValueGraph.copyOf(mutableValueGraph);
 * 
 * Graph&lt;String, EndpointPair&lt;String&gt;&gt; graph = new ImmutableDoubleValueGraphAdapter&lt;&gt;(immutableValueGraph);
 * 
 * System.out.println(graph.getEdgeWeight(EndpointPair.ordered("v1", "v2")); // outputs 3.0
 * </pre>
 * 
 * </blockquote>
 *
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 */
public class ImmutableDoubleValueGraphAdapter<V>
    extends
    ImmutableValueGraphAdapter<V, Double>
{
    private static final long serialVersionUID = 8730006126353129360L;

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     */
    public ImmutableDoubleValueGraphAdapter(ImmutableValueGraph<V, Double> valueGraph)
    {
        super(valueGraph, (ToDoubleFunction<Double> & Serializable) x -> x);
    }

}
