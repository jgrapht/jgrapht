/*
 * (C) Copyright 2015-2018, by Barak Naveh and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

/**
 * Plain implementation of UndirectedSpecifics. This implementation requires the least amount of
 * memory, at the expense of slow edge retrievals. Methods which depend on edge retrievals, e.g.
 * getEdge(V u, V v), containsEdge(V u, V v), addEdge(V u, V v), etc may be relatively slow when the
 * average degree of a vertex is high (dense graphs). For a fast implementation, use
 * {@link FastLookupUndirectedSpecifics}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @author Joris Kinable
 */
public class UndirectedSpecifics<V, E>
    implements
    Specifics<V, E>,
    Serializable
{
    private static final long serialVersionUID = 4206026440450450992L;

    protected Graph<V, E> graph;
    protected Map<V, UndirectedEdgeContainer<V, E>> vertexMap;
    protected EdgeSetFactory<V, E> edgeSetFactory;

    /**
     * Construct a new undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @deprecated Since default strategies should be decided at a higher level.
     */
    @Deprecated
    public UndirectedSpecifics(Graph<V, E> graph)
    {
        this(graph, new LinkedHashMap<>(), new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     * @deprecated Since default strategies should be decided at a higher level.
     */
    @Deprecated
    public UndirectedSpecifics(Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap)
    {
        this(graph, vertexMap, new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public UndirectedSpecifics(
        Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap,
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
        UndirectedEdgeContainer<V, E> ec = vertexMap.get(v);
        if (ec == null) {
            vertexMap.put(v, new UndirectedEdgeContainer<>(edgeSetFactory, v));
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

            for (E e : getEdgeContainer(sourceVertex).vertexEdges) {
                boolean equal = isEqualsStraightOrInverted(sourceVertex, targetVertex, e);

                if (equal) {
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

            for (E e : getEdgeContainer(sourceVertex).vertexEdges) {
                boolean equal = isEqualsStraightOrInverted(sourceVertex, targetVertex, e);

                if (equal) {
                    return e;
                }
            }
        }

        return null;
    }

    private boolean isEqualsStraightOrInverted(Object sourceVertex, Object targetVertex, E e)
    {
        boolean equalStraight = sourceVertex.equals(graph.getEdgeSource(e))
            && targetVertex.equals(graph.getEdgeTarget(e));

        boolean equalInverted = sourceVertex.equals(graph.getEdgeTarget(e))
            && targetVertex.equals(graph.getEdgeSource(e));
        return equalStraight || equalInverted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdgeToTouchingVertices(E e)
    {
        V source = graph.getEdgeSource(e);
        V target = graph.getEdgeTarget(e);

        getEdgeContainer(source).addEdge(e);

        if (!source.equals(target)) {
            getEdgeContainer(target).addEdge(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        if (graph.getType().isAllowingSelfLoops()) {
            /*
             * Then we must count, and add loops twice
             */
            int degree = 0;
            Set<E> edges = getEdgeContainer(vertex).vertexEdges;

            for (E e : edges) {
                if (graph.getEdgeSource(e).equals(graph.getEdgeTarget(e))) {
                    degree += 2;
                } else {
                    degree += 1;
                }
            }

            return degree;
        } else {
            return getEdgeContainer(vertex).edgeCount();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        return degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        return degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEdgeFromTouchingVertices(E e)
    {
        V source = graph.getEdgeSource(e);
        V target = graph.getEdgeTarget(e);

        getEdgeContainer(source).removeEdge(e);

        if (!source.equals(target)) {
            getEdgeContainer(target).removeEdge(e);
        }
    }

    /**
     * Get the edge container for a specified vertex.
     *
     * @param vertex a vertex in this graph
     *
     * @return an edge container
     */
    protected UndirectedEdgeContainer<V, E> getEdgeContainer(V vertex)
    {
        UndirectedEdgeContainer<V, E> ec = vertexMap.get(vertex);

        if (ec == null) {
            ec = new UndirectedEdgeContainer<>(edgeSetFactory, vertex);
            vertexMap.put(vertex, ec);
        }

        return ec;
    }
}
