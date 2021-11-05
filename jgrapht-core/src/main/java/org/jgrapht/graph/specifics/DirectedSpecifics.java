/*
 * (C) Copyright 2015-2021, by Barak Naveh and Contributors.
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
package org.jgrapht.graph.specifics;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Plain implementation of DirectedSpecifics. This implementation requires the least amount of
 * memory, at the expense of slow edge retrievals. Methods which depend on edge retrievals, e.g.
 * getEdge(V u, V v), containsEdge(V u, V v), addEdge(V u, V v), etc may be relatively slow when the
 * average degree of a vertex is high (dense graphs). For a fast implementation, use
 * {@link FastLookupDirectedSpecifics}.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @author Joris Kinable
 */
public class DirectedSpecifics<V, E>
    implements
    Specifics<V, E>,
    Serializable
{
    private static final long serialVersionUID = 5964807709682219859L;

    protected Graph<V, E> graph;
    protected Map<V, DirectedEdgeContainer<V, E>> vertexMap;
    protected EdgeSetFactory<V, E> edgeSetFactory;

    /**
     * Construct a new directed specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public DirectedSpecifics(
        Graph<V, E> graph, Map<V, DirectedEdgeContainer<V, E>> vertexMap,
        EdgeSetFactory<V, E> edgeSetFactory)
    {
        this.graph = Objects.requireNonNull(graph);
        this.vertexMap = Objects.requireNonNull(vertexMap);
        this.edgeSetFactory = Objects.requireNonNull(edgeSetFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        DirectedEdgeContainer<V, E> ec = vertexMap.get(v);
        if (ec == null) {
            vertexMap.put(v, new DirectedEdgeContainer<>(edgeSetFactory, v));
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> getVertexSet()
    {
        return vertexMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> edges = null;

        if (graph.containsVertex(sourceVertex) && graph.containsVertex(targetVertex)) {
            edges = new ArrayUnenforcedSet<>();

            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (graph.getEdgeTarget(e).equals(targetVertex)) {
                    edges.add(e);
                }
            }
        }

        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        if (graph.containsVertex(sourceVertex) && graph.containsVertex(targetVertex)) {
            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (graph.getEdgeTarget(e).equals(targetVertex)) {
                    return e;
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdgeToTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        getEdgeContainer(sourceVertex).addOutgoingEdge(e);
        getEdgeContainer(targetVertex).addIncomingEdge(e);
        return true;
    }

    @Override
    public boolean addEdgeToTouchingVerticesIfAbsent(V sourceVertex, V targetVertex, E e)
    {
        // lookup for edge with same source and target
        DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);
        for (E outEdge : ec.outgoing) {
            if (graph.getEdgeTarget(outEdge).equals(targetVertex)) {
                return false;
            }
        }

        // add
        ec.addOutgoingEdge(e);
        getEdgeContainer(targetVertex).addIncomingEdge(e);

        return true;
    }

    @Override
    public E createEdgeToTouchingVerticesIfAbsent(
        V sourceVertex, V targetVertex, Supplier<E> edgeSupplier)
    {
        // lookup for edge with same source and target
        DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);
        for (E e : ec.outgoing) {
            if (graph.getEdgeTarget(e).equals(targetVertex)) {
                return null;
            }
        }

        // create and add
        E e = edgeSupplier.get();
        ec.addOutgoingEdge(e);
        getEdgeContainer(targetVertex).addIncomingEdge(e);

        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        return inDegreeOf(vertex) + outDegreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        ArrayUnenforcedSet<E> inAndOut =
            new ArrayUnenforcedSet<>(getEdgeContainer(vertex).incoming);

        if (graph.getType().isAllowingSelfLoops()) {
            for (E e : getEdgeContainer(vertex).outgoing) {
                V target = graph.getEdgeTarget(e);
                if (!vertex.equals(target)) {
                    inAndOut.add(e);
                }
            }
        } else {
            inAndOut.addAll(getEdgeContainer(vertex).outgoing);
        }

        return Collections.unmodifiableSet(inAndOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        return getEdgeContainer(vertex).incoming.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableIncomingEdges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        return getEdgeContainer(vertex).outgoing.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableOutgoingEdges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEdgeFromTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        getEdgeContainer(sourceVertex).removeOutgoingEdge(e);
        getEdgeContainer(targetVertex).removeIncomingEdge(e);
    }

    /**
     * Get the edge container for specified vertex.
     *
     * @param vertex a vertex in this graph.
     *
     * @return an edge container
     */
    protected DirectedEdgeContainer<V, E> getEdgeContainer(V vertex)
    {
        DirectedEdgeContainer<V, E> ec = vertexMap.get(vertex);

        if (ec == null) {
            ec = new DirectedEdgeContainer<>(edgeSetFactory, vertex);
            vertexMap.put(vertex, ec);
        }

        return ec;
    }

}
