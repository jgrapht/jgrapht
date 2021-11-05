/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
