/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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
 * A graph backed by the the graph specified at the constructor, which delegates all its methods to
 * the backing graph. Operations on this graph "pass through" to the to the backing graph. Any
 * modification made to this graph or the backing graph is reflected by the other.
 *
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through to the backing graph,
 * but relies on <code>Object</code>'s <code>equals</code> and <code>hashCode</code> methods.
 * </p>
 *
 * <p>
 * This class is mostly used as a base for extending subclasses. It can also be used in order to
 * override the vertex and edge supplier of a graph.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class GraphDelegator<V, E>
    extends
    AbstractGraph<V, E>
    implements
    Graph<V, E>,
    Serializable
{
    private static final long serialVersionUID = -215068279981825448L;

    /*
     * The graph to which operations are delegated.
     */
    private final Graph<V, E> delegate;
    private final Supplier<V> vertexSupplier;
    private final Supplier<E> edgeSupplier;

    /**
     * Constructor
     *
     * @param graph the backing graph (the delegate).
     */
    public GraphDelegator(Graph<V, E> graph)
    {
        this(graph, null, null);
    }

    /**
     * 
     * @param graph the backing graph (the delegate).
     * @param vertexSupplier vertex supplier for the delegator. Can be null in which case the
     *        backing graph vertex supplier will be used.
     * @param edgeSupplier edge supplier for the delegator. Can be null in which case the backing
     *        graph edge supplier will be used.
     */
    public GraphDelegator(Graph<V, E> graph, Supplier<V> vertexSupplier, Supplier<E> edgeSupplier)
    {
        super();
        this.delegate = Objects.requireNonNull(graph, "graph must not be null");
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Returns the delegator's vertex supplier or the backing graph's vertex supplier in case of
     * null.
     */
    @Override
    public Supplier<V> getVertexSupplier()
    {
        if (vertexSupplier != null) {
            return vertexSupplier;
        } else {
            return delegate.getVertexSupplier();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Returns the delegator's edge supplier or the backing graph's edge supplier in case of null.
     */
    @Override
    public Supplier<E> getEdgeSupplier()
    {
        if (edgeSupplier != null) {
            return edgeSupplier;
        } else {
            return delegate.getEdgeSupplier();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return delegate.getAllEdges(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        return delegate.getEdge(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        /*
         * Use our own edge supplier, if provided.
         */
        if (edgeSupplier != null) {
            E e = edgeSupplier.get();
            return this.addEdge(sourceVertex, targetVertex, e) ? e : null;
        }
        return delegate.addEdge(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        return delegate.addEdge(sourceVertex, targetVertex, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V addVertex()
    {
        /*
         * Use our own vertex supplier, if provided.
         */
        if (vertexSupplier != null) {
            V v = vertexSupplier.get();
            return this.addVertex(v) ? v : null;
        }
        return delegate.addVertex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        return delegate.addVertex(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return delegate.containsEdge(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return delegate.containsVertex(v);
    }

    /**
     * Returns the degree of the specified vertex.
     *
     * @param vertex vertex whose degree is to be calculated
     * @return the degree of the specified vertex
     */
    public int degreeOf(V vertex)
    {
        return delegate.degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        return delegate.edgeSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        return delegate.edgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        return delegate.inDegreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return delegate.incomingEdgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        return delegate.outDegreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return delegate.outgoingEdgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEdge(E e)
    {
        return delegate.removeEdge(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        return delegate.removeEdge(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(V v)
    {
        return delegate.removeVertex(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return delegate.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        return delegate.vertexSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        return delegate.getEdgeSource(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        return delegate.getEdgeTarget(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        return delegate.getEdgeWeight(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        delegate.setEdgeWeight(e, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return delegate.getType();
    }

    /**
     * Return the backing graph (the delegate).
     * 
     * @return the backing graph (the delegate)
     */
    protected Graph<V, E> getDelegate()
    {
        return delegate;
    }

}
