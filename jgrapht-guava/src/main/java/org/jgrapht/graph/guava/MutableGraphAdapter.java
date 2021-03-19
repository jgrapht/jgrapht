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
import java.util.function.*;

/**
 * A graph adapter class using Guava's {@link MutableGraph}.
 * 
 * <p>
 * The adapter uses class {@link EndpointPair} to represent edges. Changes in the adapter such as
 * adding or removing vertices and edges are reflected in the underlying graph.
 *
 * <p>
 * See the example below on how to create such an adapter: <blockquote>
 * 
 * <pre>
 * MutableGraph&lt;String&gt; mutableGraph = GraphBuilder.directed().allowsSelfLoops(true).build();
 * 
 * mutableGraph.addNode("v1");
 * mutableGraph.addNode("v2");
 * mutableGraph.addEdge("v1", "v2");
 * 
 * Graph&lt;String, EndpointPair&lt;String&gt;&gt; graph = new MutableGraphAdapter&lt;&gt;(mutableGraph);
 * </pre>
 * 
 * </blockquote>
 *
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 */
public class MutableGraphAdapter<V>
    extends
    BaseGraphAdapter<V, MutableGraph<V>>
    implements
    Graph<V, EndpointPair<V>>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = -7556855931445010748L;

    /**
     * Create a new adapter.
     * 
     * @param graph the graph
     */
    public MutableGraphAdapter(MutableGraph<V> graph)
    {
        this(graph, null, null);
    }

    /**
     * Create a new adapter.
     * 
     * @param graph the graph
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     */
    public MutableGraphAdapter(
        MutableGraph<V> graph, Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier)
    {
        super(graph, vertexSupplier, edgeSupplier, ElementOrderMethod.internal());
    }

    /**
     * Create a new adapter.
     * 
     * @param graph the graph
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param vertexOrderMethod the method used to ensure a total order of the graph vertices. This
     *        is required in order to make edge source/targets be consistent.
     */
    public MutableGraphAdapter(
        MutableGraph<V> graph, Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier,
        ElementOrderMethod<V> vertexOrderMethod)
    {
        super(graph, vertexSupplier, edgeSupplier, vertexOrderMethod);
    }

    @Override
    public EndpointPair<V> addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (containsEdge(sourceVertex, targetVertex)) {
            return null;
        }

        if (!graph.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        graph.putEdge(sourceVertex, targetVertex);
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

        if (!graph.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        graph.putEdge(sourceVertex, targetVertex);
        return true;
    }

    @Override
    public V addVertex()
    {
        if (vertexSupplier == null) {
            throw new UnsupportedOperationException("The graph contains no vertex supplier");
        }

        V v = vertexSupplier.get();

        if (graph.addNode(v)) {
            return v;
        }
        return null;
    }

    @Override
    public boolean addVertex(V v)
    {
        return graph.addNode(v);
    }

    @Override
    public EndpointPair<V> removeEdge(V sourceVertex, V targetVertex)
    {
        EndpointPair<V> e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            graph.removeEdge(sourceVertex, targetVertex);
        }

        return e;
    }

    @Override
    public boolean removeEdge(EndpointPair<V> e)
    {
        if (e == null) {
            return false;
        }
        return graph.removeEdge(e.nodeU(), e.nodeV());
    }

    @Override
    public boolean removeVertex(V v)
    {
        vertexOrder.notifyRemoval(v);
        return graph.removeNode(v);
    }

    @Override
    public void setEdgeWeight(EndpointPair<V> e, double weight)
    {
        throw new UnsupportedOperationException("Graph is unweighted");
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
            MutableGraphAdapter<V> newGraph = TypeUtil.uncheckedCast(super.clone());

            newGraph.vertexSupplier = this.vertexSupplier;
            newGraph.edgeSupplier = this.edgeSupplier;
            newGraph.unmodifiableVertexSet = null;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.graph = Graphs.copyOf(this.graph);
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

        graph = (type.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected())
            .allowsSelfLoops(type.isAllowingSelfLoops()).build();

        // read vertices
        int n = ois.readInt();
        for (int i = 0; i < n; i++) {
            V v = (V) ois.readObject();
            graph.addNode(v);
        }

        // read edges
        int m = ois.readInt();
        for (int i = 0; i < m; i++) {
            V s = (V) ois.readObject();
            V t = (V) ois.readObject();
            graph.putEdge(s, t);
        }

        // setup the vertex order
        vertexOrder = createVertexOrder(vertexOrderMethod);
    }

}
