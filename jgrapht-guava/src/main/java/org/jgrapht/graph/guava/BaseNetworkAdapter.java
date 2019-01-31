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

import com.google.common.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.*;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A base abstract implementation for the graph adapter class using Guava's {@link Network}. This is
 * a helper class in order to support both mutable and immutable networks.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <N> type of the underlying Guava's network
 */
public abstract class BaseNetworkAdapter<V, E, N extends Network<V, E>>
    extends
    AbstractGraph<V, E>
    implements
    Graph<V, E>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = -6233085794632237761L;

    protected static final String LOOPS_NOT_ALLOWED = "loops not allowed";

    protected transient Set<V> unmodifiableVertexSet = null;
    protected transient Set<E> unmodifiableEdgeSet = null;

    protected Supplier<V> vertexSupplier;
    protected Supplier<E> edgeSupplier;
    protected transient N network;

    /**
     * Create a new network adapter.
     * 
     * @param network the mutable network
     */
    public BaseNetworkAdapter(N network)
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
    public BaseNetworkAdapter(N network, Supplier<V> vertexSupplier, Supplier<E> edgeSupplier)
    {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public Supplier<V> getVertexSupplier()
    {
        return vertexSupplier;
    }

    /**
     * Set the vertex supplier that the graph uses whenever it needs to create new vertices.
     * 
     * <p>
     * A graph uses the vertex supplier to create new vertex objects whenever a user calls method
     * {@link Graph#addVertex()}. Users can also create the vertex in user code and then use method
     * {@link Graph#addVertex(Object)} to add the vertex.
     * 
     * <p>
     * In contrast with the {@link Supplier} interface, the vertex supplier has the additional
     * requirement that a new and distinct result is returned every time it is invoked. More
     * specifically for a new vertex to be added in a graph <code>v</code> must <i>not</i> be equal
     * to any other vertex in the graph. More formally, the graph must not contain any vertex
     * <code>v2</code> such that <code>v2.equals(v)</code>.
     * 
     * @param vertexSupplier the vertex supplier
     */
    public void setVertexSupplier(Supplier<V> vertexSupplier)
    {
        this.vertexSupplier = vertexSupplier;
    }

    @Override
    public Supplier<E> getEdgeSupplier()
    {
        return edgeSupplier;
    }

    /**
     * Set the edge supplier that the graph uses whenever it needs to create new edges.
     * 
     * <p>
     * A graph uses the edge supplier to create new edge objects whenever a user calls method
     * {@link Graph#addEdge(Object, Object)}. Users can also create the edge in user code and then
     * use method {@link Graph#addEdge(Object, Object, Object)} to add the edge.
     * 
     * <p>
     * In contrast with the {@link Supplier} interface, the edge supplier has the additional
     * requirement that a new and distinct result is returned every time it is invoked. More
     * specifically for a new edge to be added in a graph <code>e</code> must <i>not</i> be equal to
     * any other edge in the graph (even if the graph allows edge-multiplicity). More formally, the
     * graph must not contain any edge <code>e2</code> such that <code>e2.equals(e)</code>.
     * 
     * @param edgeSupplier the edge supplier
     */
    public void setEdgeSupplier(Supplier<E> edgeSupplier)
    {
        this.edgeSupplier = edgeSupplier;
    }

    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        return network
            .edgesConnecting(sourceVertex, targetVertex).stream().findFirst().orElse(null);
    }

    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(network.nodes());
        }
        return unmodifiableVertexSet;
    }

    @Override
    public V getEdgeSource(E e)
    {
        return network.incidentNodes(e).nodeU();
    }

    @Override
    public V getEdgeTarget(E e)
    {
        return network.incidentNodes(e).nodeV();
    }

    @Override
    public GraphType getType()
    {
        return (network.isDirected() ? new DefaultGraphType.Builder().directed()
            : new DefaultGraphType.Builder().undirected())
                .weighted(false).allowMultipleEdges(network.allowsParallelEdges())
                .allowSelfLoops(network.allowsSelfLoops()).build();
    }

    @Override
    public boolean containsEdge(E e)
    {
        return network.edges().contains(e);
    }

    @Override
    public boolean containsVertex(V v)
    {
        return network.nodes().contains(v);
    }

    @Override
    public Set<E> edgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(network.edges());
        }
        return unmodifiableEdgeSet;
    }

    @Override
    public int degreeOf(V vertex)
    {
        return network.degree(vertex);
    }

    @Override
    public Set<E> edgesOf(V vertex)
    {
        return network.incidentEdges(vertex);
    }

    @Override
    public int inDegreeOf(V vertex)
    {
        return network.inDegree(vertex);
    }

    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return network.inEdges(vertex);
    }

    @Override
    public int outDegreeOf(V vertex)
    {
        return network.outDegree(vertex);
    }

    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return network.outEdges(vertex);
    }

    @Override
    public double getEdgeWeight(E e)
    {
        if (e == null) {
            throw new NullPointerException();
        } else if (!network.edges().contains(e)) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        } else {
            return Graph.DEFAULT_EDGE_WEIGHT;
        }
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return network.edgesConnecting(sourceVertex, targetVertex);
    }

}
