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

import com.google.common.graph.Graphs;
import com.google.common.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A graph adapter class using Guava's {@link MutableValueGraph}.
 * 
 * <p>
 * The adapter uses class {@link EndpointPair} to represent edges. Changes in the adapter such as
 * adding or removing vertices and edges are reflected in the underlying value graph.
 *
 * <p>
 * The class uses a converter from Guava's values to JGraphT's double weights. Thus, the resulting
 * graph is weighted. Assume for example that the following class is the value type: <blockquote>
 * 
 * <pre>
 * class MyValue
 *     implements
 *     Serializable
 * {
 *     private double value;
 *
 *     public MyValue(double value)
 *     {
 *         this.value = value;
 *     }
 *
 *     public double getValue()
 *     {
 *         return value;
 *     }
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * Then one could create an adapter using the following code: <blockquote>
 * 
 * <pre>
 * MutableValueGraph&lt;String, MyValue&gt; valueGraph =
 *     ValueGraphBuilder.directed().allowsSelfLoops(true).build();
 * valueGraph.addNode("v1");
 * valueGraph.addNode("v2");
 * valueGraph.putEdgeValue("v1", "v2", new MyValue(5.0));
 * 
 * Graph&lt;String, EndpointPair&lt;String&gt;&gt; graph = new MutableValueGraphAdapter&lt;&gt;(
 *     valueGraph, new MyValue(1.0), (ToDoubleFunction&lt;MyValue&gt; &amp; Serializable) MyValue::getValue);
 * 
 * double weight = graph.getEdgeWeight(EndpointPair.ordered("v1", "v2")); // should return 5.0
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * This is a one-way conversion meaning that calling {@link #setEdgeWeight(EndpointPair, double)}
 * will throw an unsupported operation exception. Adjusting the weights can be done directly (by
 * keeping an external reference) on the underlying {@link MutableValueGraph} and calling
 * {@link MutableValueGraph#putEdgeValue(Object, Object, Object)}. Changes on the values will be
 * propagated upstream using the provided value converter.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <W> the value type
 */
public class MutableValueGraphAdapter<V, W>
    extends
    BaseValueGraphAdapter<V, W, MutableValueGraph<V, W>>
    implements
    Graph<V, EndpointPair<V>>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = -5095044027783397573L;

    protected final W defaultValue;

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     * @param defaultValue a default value to be used when creating new edges
     * @param valueConverter a function that converts a value to a double
     */
    public MutableValueGraphAdapter(
        MutableValueGraph<V, W> valueGraph, W defaultValue, ToDoubleFunction<W> valueConverter)
    {
        this(valueGraph, defaultValue, valueConverter, null, null);
    }

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     * @param defaultValue a default value to be used when creating new edges
     * @param valueConverter a function that converts a value to a double
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     */
    public MutableValueGraphAdapter(
        MutableValueGraph<V, W> valueGraph, W defaultValue, ToDoubleFunction<W> valueConverter,
        Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier)
    {
        super(
            valueGraph, valueConverter, vertexSupplier, edgeSupplier,
            ElementOrderMethod.internal());
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the value graph
     * @param defaultValue a default value to be used when creating new edges
     * @param valueConverter a function that converts a value to a double
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param vertexOrderMethod the method used to ensure a total order of the graph vertices. This
     *        is required in order to make edge source/targets be consistent.
     */
    public MutableValueGraphAdapter(
        MutableValueGraph<V, W> valueGraph, W defaultValue, ToDoubleFunction<W> valueConverter,
        Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier,
        ElementOrderMethod<V> vertexOrderMethod)
    {
        super(valueGraph, valueConverter, vertexSupplier, edgeSupplier, vertexOrderMethod);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    @Override
    public EndpointPair<V> addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (containsEdge(sourceVertex, targetVertex)) {
            return null;
        }

        if (!valueGraph.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        valueGraph.putEdgeValue(sourceVertex, targetVertex, defaultValue);
        return createEdge(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     * 
     * The provided edge object can either be null or must respect the source and target vertices
     * that are provided as parameters.
     * 
     * @throws IllegalArgumentException if edge e is not null and the sourceVertex parameter does
     *         not match the node U of the endpoint-pair
     * @throws IllegalArgumentException if edge e is not null and the targetVertex parameter does
     *         not match the node V of the endpoint-pair
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, EndpointPair<V> e)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (e != null) {
            if (!sourceVertex.equals(e.nodeU())) {
                throw new IllegalArgumentException(
                    "Provided edge must have node U equal to source vertex");
            }
            if (!targetVertex.equals(e.nodeV())) {
                throw new IllegalArgumentException(
                    "Provided edge must have node V equal to target vertex");
            }
        }

        if (containsEdge(sourceVertex, targetVertex)) {
            return false;
        }

        if (!valueGraph.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        valueGraph.putEdgeValue(sourceVertex, targetVertex, defaultValue);
        return true;
    }

    @Override
    public V addVertex()
    {
        if (vertexSupplier == null) {
            throw new UnsupportedOperationException("The graph contains no vertex supplier");
        }

        V v = vertexSupplier.get();

        if (valueGraph.addNode(v)) {
            return v;
        }
        return null;
    }

    @Override
    public boolean addVertex(V v)
    {
        return valueGraph.addNode(v);
    }

    @Override
    public EndpointPair<V> removeEdge(V sourceVertex, V targetVertex)
    {
        EndpointPair<V> e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            valueGraph.removeEdge(sourceVertex, targetVertex);
        }

        return e;
    }

    @Override
    public boolean removeEdge(EndpointPair<V> e)
    {
        if (e == null) {
            return false;
        }
        return valueGraph.removeEdge(e.nodeU(), e.nodeV()) != null;
    }

    @Override
    public boolean removeVertex(V v)
    {
        vertexOrder.notifyRemoval(v);
        return valueGraph.removeNode(v);
    }

    /**
     * {@inheritDoc}
     * 
     * This method always throws an {@link UnsupportedOperationException} since the adapter works
     * one-way from values to weights. Adjusting the weights can be done by adjusting the values in
     * the underlying {@link ValueGraph} which will automatically be propagated using the provided
     * converter.
     *
     * @param e edge on which to set weight
     * @param weight new weight for edge
     * @throws UnsupportedOperationException if the graph does not support weights
     */
    @Override
    public void setEdgeWeight(EndpointPair<V> e, double weight)
    {
        throw new UnsupportedOperationException(
            "Not supported operation. Change directly the underlying value graph");
    }

    /**
     * Returns a shallow copy of this graph instance. Neither edges nor vertices are cloned.
     *
     * @return a shallow copy of this graph.
     *
     * @throws RuntimeException in case the clone is not supported
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()
    {
        try {
            MutableValueGraphAdapter<V, W> newGraph = TypeUtil.uncheckedCast(super.clone());

            newGraph.vertexSupplier = this.vertexSupplier;
            newGraph.edgeSupplier = this.edgeSupplier;
            newGraph.unmodifiableVertexSet = null;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.valueConverter = this.valueConverter;
            newGraph.valueGraph = Graphs.copyOf(this.valueGraph);
            newGraph.vertexOrder = createVertexOrder(newGraph.vertexOrderMethod);

            return newGraph;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void writeObject(ObjectOutputStream oos)
        throws IOException
    {
        oos.defaultWriteObject();

        // write type
        oos.writeObject(getType());

        // write vertices
        int n = vertexSet().size();
        oos.writeInt(n);
        for (V v : vertexSet()) {
            oos.writeObject(v);
        }

        // write edges
        int m = edgeSet().size();
        oos.writeInt(m);
        for (EndpointPair<V> e : edgeSet()) {
            V u = e.nodeU();
            V v = e.nodeV();
            oos.writeObject(u);
            oos.writeObject(v);
            oos.writeObject(valueGraph.edgeValue(u, v).get());
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException,
        IOException
    {
        ois.defaultReadObject();

        GraphType type = (GraphType) ois.readObject();
        if (type.isMixed() || type.isAllowingMultipleEdges()) {
            throw new IOException("Graph type not supported");
        }

        valueGraph =
            (type.isDirected() ? ValueGraphBuilder.directed() : ValueGraphBuilder.undirected())
                .allowsSelfLoops(type.isAllowingSelfLoops()).build();

        // read vertices
        int n = ois.readInt();
        for (int i = 0; i < n; i++) {
            V v = (V) ois.readObject();
            valueGraph.addNode(v);
        }

        // read edges
        int m = ois.readInt();
        for (int i = 0; i < m; i++) {
            V s = (V) ois.readObject();
            V t = (V) ois.readObject();
            W w = (W) ois.readObject();
            valueGraph.putEdgeValue(s, t, w);
        }

        // setup the vertex order
        vertexOrder = createVertexOrder(vertexOrderMethod);
    }

}
