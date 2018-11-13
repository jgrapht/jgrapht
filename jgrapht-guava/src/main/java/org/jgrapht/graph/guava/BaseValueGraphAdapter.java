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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * A base abstract implementation for the graph adapter class using Guava's {@link ValueGraph}. This
 * is a helper class in order to support both mutable and immutable value graphs.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <W> the value type
 * @param <VG> type of the underlying Guava's value graph
 */
public abstract class BaseValueGraphAdapter<V, W, VG extends ValueGraph<V, W>>
    extends
    AbstractGraph<V, EndpointPair<V>>
    implements
    Graph<V, EndpointPair<V>>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = 3833510139696864917L;

    protected static final String LOOPS_NOT_ALLOWED = "loops not allowed";

    protected transient Set<V> unmodifiableVertexSet = null;
    protected transient Set<EndpointPair<V>> unmodifiableEdgeSet = null;

    protected Supplier<V> vertexSupplier;
    protected Supplier<EndpointPair<V>> edgeSupplier;
    protected ToDoubleFunction<W> valueConverter;
    protected transient VG valueGraph;

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the mutable value graph
     * @param valueConverter a function that converts a value to a double
     */
    public BaseValueGraphAdapter(VG valueGraph, ToDoubleFunction<W> valueConverter)
    {
        this(valueGraph, valueConverter, null, null);
    }

    /**
     * Create a new adapter.
     * 
     * @param valueGraph the mutable value graph
     * @param valueConverter a function that converts a value to a double
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     */
    public BaseValueGraphAdapter(
        VG valueGraph, ToDoubleFunction<W> valueConverter, Supplier<V> vertexSupplier,
        Supplier<EndpointPair<V>> edgeSupplier)
    {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.valueGraph = Objects.requireNonNull(valueGraph);
        this.valueConverter = Objects.requireNonNull(valueConverter);
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
        } else if (!valueGraph.hasEdgeConnecting(sourceVertex, targetVertex)) {
            return null;
        } else {
            return createEdge(sourceVertex, targetVertex);
        }
    }

    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(valueGraph.nodes());
        }
        return unmodifiableVertexSet;
    }

    @Override
    public V getEdgeSource(EndpointPair<V> e)
    {
        return e.nodeU();
    }

    @Override
    public V getEdgeTarget(EndpointPair<V> e)
    {
        return e.nodeV();
    }

    @Override
    public GraphType getType()
    {
        return (valueGraph.isDirected() ? new DefaultGraphType.Builder().directed()
            : new DefaultGraphType.Builder().undirected())
                .weighted(true).allowMultipleEdges(false)
                .allowSelfLoops(valueGraph.allowsSelfLoops()).build();
    }

    @Override
    public boolean containsEdge(EndpointPair<V> e)
    {
        return valueGraph.edges().contains(e);
    }

    @Override
    public boolean containsVertex(V v)
    {
        return valueGraph.nodes().contains(v);
    }

    @Override
    public Set<EndpointPair<V>> edgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(valueGraph.edges());
        }
        return unmodifiableEdgeSet;
    }

    @Override
    public int degreeOf(V vertex)
    {
        return valueGraph.degree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> edgesOf(V vertex)
    {
        return valueGraph.incidentEdges(vertex);
    }

    @Override
    public int inDegreeOf(V vertex)
    {
        return valueGraph.inDegree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> incomingEdgesOf(V vertex)
    {
        return valueGraph
            .predecessors(vertex).stream().map(other -> createEdge(other, vertex))
            .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Override
    public int outDegreeOf(V vertex)
    {
        return valueGraph.outDegree(vertex);
    }

    @Override
    public Set<EndpointPair<V>> outgoingEdgesOf(V vertex)
    {
        return valueGraph
            .successors(vertex).stream().map(other -> createEdge(vertex, other))
            .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Override
    public double getEdgeWeight(EndpointPair<V> e)
    {
        if (e == null) {
            throw new NullPointerException();
        } else if (!valueGraph.hasEdgeConnecting(e.nodeU(), e.nodeV())) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        } else {
            return valueGraph
                .edgeValue(e.nodeU(), e.nodeV()).map(valueConverter::applyAsDouble)
                .orElse(Graph.DEFAULT_EDGE_WEIGHT);
        }
    }

    @Override
    public Set<EndpointPair<V>> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (sourceVertex == null || targetVertex == null
            || !valueGraph.nodes().contains(sourceVertex)
            || !valueGraph.nodes().contains(targetVertex))
        {
            return null;
        } else if (!valueGraph.hasEdgeConnecting(sourceVertex, targetVertex)) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(createEdge(sourceVertex, targetVertex));
        }
    }

    /**
     * Create an edge
     * 
     * @param s the source vertex
     * @param t the target vertex
     * @return the edge
     */
    final EndpointPair<V> createEdge(V s, V t)
    {
        return valueGraph.isDirected() ? EndpointPair.ordered(s, t) : EndpointPair.unordered(s, t);
    }

}
