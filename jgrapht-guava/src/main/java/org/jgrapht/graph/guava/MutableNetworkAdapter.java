/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
 * A graph adapter class using Guava's {@link MutableNetwork}.
 * 
 * <p>
 * Changes in the adapter such as adding or removing vertices and edges are reflected in the
 * underlying network.
 * 
 * Example usage: <blockquote>
 * 
 * <pre>
 * MutableNetwork&lt;String, DefaultEdge&gt; mutableNetwork =
 *     NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
 * 
 * Graph&lt;String, DefaultEdge&gt; graph = new MutableNetworkAdapter&lt;&gt;(
 *     mutableNetwork, SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER);
 * 
 * graph.addVertex("v1");
 * 
 * System.out.println(mutableNetwork.nodes().contains("v1")); // outputs true
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class MutableNetworkAdapter<V, E>
    extends
    BaseNetworkAdapter<V, E, MutableNetwork<V, E>>
    implements
    Graph<V, E>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = 7450826703235510224L;

    protected static final String GRAPH_IS_UNWEIGHTED = "Graph is unweighted";

    /**
     * Create a new network adapter.
     * 
     * @param network the mutable network
     */
    public MutableNetworkAdapter(MutableNetwork<V, E> network)
    {
        this(network, null, null);
    }

    /**
     * Create a new network adapter.
     * 
     * @param network the mutable network
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     */
    public MutableNetworkAdapter(
        MutableNetwork<V, E> network, Supplier<V> vertexSupplier, Supplier<E> edgeSupplier)
    {
        super(network, vertexSupplier, edgeSupplier);
    }

    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!network.allowsParallelEdges() && containsEdge(sourceVertex, targetVertex)) {
            return null;
        }

        if (!network.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        if (edgeSupplier == null) {
            throw new UnsupportedOperationException("The graph contains no edge supplier");
        }

        E e = edgeSupplier.get();

        if (network.addEdge(sourceVertex, targetVertex, e)) {
            return e;
        }
        return null;
    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        if (e == null) {
            throw new NullPointerException();
        }

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!network.allowsParallelEdges() && containsEdge(sourceVertex, targetVertex)) {
            return false;
        }

        if (!network.allowsSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        if (network.addEdge(sourceVertex, targetVertex, e)) {
            return true;
        }

        return false;
    }

    @Override
    public V addVertex()
    {
        if (vertexSupplier == null) {
            throw new UnsupportedOperationException("The graph contains no vertex supplier");
        }

        V v = vertexSupplier.get();

        if (network.addNode(v)) {
            return v;
        }
        return null;
    }

    @Override
    public boolean addVertex(V v)
    {
        return network.addNode(v);
    }

    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            network.removeEdge(e);
        }

        return e;
    }

    @Override
    public boolean removeEdge(E e)
    {
        return network.removeEdge(e);
    }

    @Override
    public boolean removeVertex(V v)
    {
        return network.removeNode(v);
    }

    @Override
    public void setEdgeWeight(E e, double weight)
    {
        throw new UnsupportedOperationException(GRAPH_IS_UNWEIGHTED);
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
            MutableNetworkAdapter<V, E> newGraph = TypeUtil.uncheckedCast(super.clone());

            newGraph.vertexSupplier = this.vertexSupplier;
            newGraph.edgeSupplier = this.edgeSupplier;
            newGraph.unmodifiableVertexSet = null;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.network = Graphs.copyOf(this.network);

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
        for (E e : edgeSet()) {
            oos.writeObject(getEdgeSource(e));
            oos.writeObject(getEdgeTarget(e));
            oos.writeObject(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException,
        IOException
    {
        ois.defaultReadObject();

        GraphType type = (GraphType) ois.readObject();
        if (type.isMixed()) {
            throw new IOException("Graph type not supported");
        }

        this.network = (type.isDirected() ? NetworkBuilder.directed() : NetworkBuilder.undirected())
            .allowsParallelEdges(type.isAllowingMultipleEdges())
            .allowsSelfLoops(type.isAllowingSelfLoops()).build();

        // read vertices
        int n = ois.readInt();
        for (int i = 0; i < n; i++) {
            V v = (V) ois.readObject();
            network.addNode(v);
        }

        // read edges
        int m = ois.readInt();
        for (int i = 0; i < m; i++) {
            V s = (V) ois.readObject();
            V t = (V) ois.readObject();
            E e = (E) ois.readObject();
            network.addEdge(s, t, e);
        }
    }

}
