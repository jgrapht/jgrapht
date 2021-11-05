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
import org.jgrapht.*;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * A base abstract implementation for the graph adapter class using Guava's {@link Graph}. This is a
 * helper class in order to support both mutable and immutable graphs.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <G> type of the underlying Guava's graph
 */
public abstract class BaseGraphAdapter<V, G extends com.google.common.graph.Graph<V>>
    extends
    AbstractGraph<V, EndpointPair<V>>
    implements
    Graph<V, EndpointPair<V>>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = -6742507788742087708L;

    protected static final String LOOPS_NOT_ALLOWED = "loops not allowed";

    protected transient Set<V> unmodifiableVertexSet = null;
    protected transient Set<EndpointPair<V>> unmodifiableEdgeSet = null;

    protected Supplier<V> vertexSupplier;
    protected Supplier<EndpointPair<V>> edgeSupplier;
    protected transient G graph;

    protected ElementOrderMethod<V> vertexOrderMethod;
    protected transient ElementOrder<V> vertexOrder;

    /**
     * Create a new adapter.
     * 
     * @param graph the graph
     */
    public BaseGraphAdapter(G graph)
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
    public BaseGraphAdapter(
        G graph, Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier)
    {
        this(graph, vertexSupplier, edgeSupplier, ElementOrderMethod.internal());
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
    public BaseGraphAdapter(
        G graph, Supplier<V> vertexSupplier, Supplier<EndpointPair<V>> edgeSupplier,
        ElementOrderMethod<V> vertexOrderMethod)
    {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.graph = Objects.requireNonNull(graph);
        this.vertexOrderMethod = Objects.requireNonNull(vertexOrderMethod);
        this.vertexOrder = createVertexOrder(vertexOrderMethod);
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
    public Supplier<EndpointPair<V>> getEdgeSupplier()
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
    public void setEdgeSupplier(Supplier<EndpointPair<V>> edgeSupplier)
    {
        this.edgeSupplier = edgeSupplier;
    }

    @Override
    public EndpointPair<V> getEdge(V sourceVertex, V targetVertex)
    {
        if (sourceVertex == null || targetVertex == null) {
            return null;
        } else if (!graph.hasEdgeConnecting(sourceVertex, targetVertex)) {
            return null;
        } else {
            return createEdge(sourceVertex, targetVertex);
        }
    }

    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(graph.nodes());
        }
        return unmodifiableVertexSet;
    }

    @Override
    public V getEdgeSource(EndpointPair<V> e)
    {
        if (graph.isDirected()) {
            return e.nodeU();
        } else {
            V u = e.nodeU();
            V v = e.nodeV();
            int c = vertexOrder.compare(u, v);
            if (c <= 0) {
                return u;
            }
            return v;
        }
    }

    @Override
    public V getEdgeTarget(EndpointPair<V> e)
    {
        if (graph.isDirected()) {
            return e.nodeV();
        } else {
            V u = e.nodeU();
            V v = e.nodeV();
            int c = vertexOrder.compare(u, v);
            if (c <= 0) {
                return v;
            }
            return u;
        }
    }

    @Override
    public GraphType getType()
    {
        return (graph.isDirected() ? new DefaultGraphType.Builder().directed()
            : new DefaultGraphType.Builder().undirected())
                .weighted(false).allowMultipleEdges(false).allowSelfLoops(graph.allowsSelfLoops())
                .build();
    }

    @Override
    public boolean containsEdge(EndpointPair<V> e)
    {
        return graph.edges().contains(e);
    }

    @Override
    public boolean containsVertex(V v)
    {
        return graph.nodes().contains(v);
    }

    @Override
    public Set<EndpointPair<V>> edgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(graph.edges());
        }
        return unmodifiableEdgeSet;
    }

    @Override
    public int degreeOf(V vertex)
    {
        return graph.degree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> edgesOf(V vertex)
    {
        return graph.incidentEdges(vertex);
    }

    @Override
    public int inDegreeOf(V vertex)
    {
        return graph.inDegree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> incomingEdgesOf(V vertex)
    {
        return graph
            .predecessors(vertex).stream().map(other -> createEdge(other, vertex))
            .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Override
    public int outDegreeOf(V vertex)
    {
        return graph.outDegree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> outgoingEdgesOf(V vertex)
    {
        return graph
            .successors(vertex).stream().map(other -> createEdge(vertex, other))
            .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Override
    public double getEdgeWeight(EndpointPair<V> e)
    {
        if (e == null) {
            throw new NullPointerException();
        } else if (!graph.hasEdgeConnecting(e.nodeU(), e.nodeV())) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        } else {
            return Graph.DEFAULT_EDGE_WEIGHT;
        }
    }

    @Override
    public Set<EndpointPair<V>> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (sourceVertex == null || targetVertex == null || !graph.nodes().contains(sourceVertex)
            || !graph.nodes().contains(targetVertex))
        {
            return null;
        } else if (!graph.hasEdgeConnecting(sourceVertex, targetVertex)) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(createEdge(sourceVertex, targetVertex));
        }
    }

    /**
     * Create an edge.
     * 
     * @param s the source vertex
     * @param t the target vertex
     * @return the edge
     */
    final EndpointPair<V> createEdge(V s, V t)
    {
        return graph.isDirected() ? EndpointPair.ordered(s, t) : EndpointPair.unordered(s, t);
    }

    /**
     * Create the internal vertex order implementation.
     * 
     * @param vertexOrderMethod method to use
     * @return the vertex order
     */
    protected ElementOrder<V> createVertexOrder(ElementOrderMethod<V> vertexOrderMethod)
    {
        switch (vertexOrderMethod.getType()) {
        case COMPARATOR:
            return ElementOrder.comparator(vertexOrderMethod.comparator());
        case GUAVA_COMPARATOR:
            if (!graph
                .nodeOrder().type().equals(com.google.common.graph.ElementOrder.Type.SORTED))
            {
                throw new IllegalArgumentException(
                    "Guava comparator only usable if node order is SORTED!");
            }
            return ElementOrder.comparator(graph.nodeOrder().comparator());
        case NATURAL:
            return ElementOrder.natural();
        case INTERNAL:
        default:
            return ElementOrder.internal();
        }
    }

}
