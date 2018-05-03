/*
 * (C) Copyright 2015-2018, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.graph.specifics;

import java.io.*;
import java.util.*;

import org.jgrapht.graph.*;
import org.jgrapht.util.*;

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
    implements Specifics<V, E>, Serializable
{
    private static final long serialVersionUID = 6494588405178655873L;

    protected AbstractBaseGraph<V, E> abstractBaseGraph;
    protected Map<V, UndirectedEdgeContainer<V, E>> vertexMapUndirected;
    protected EdgeSetFactory<V, E> edgeSetFactory;

    /**
     * Construct a new undirected specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     */
    public UndirectedSpecifics(AbstractBaseGraph<V, E> abstractBaseGraph)
    {
        this(abstractBaseGraph, new LinkedHashMap<>(), new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new undirected specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     */
    public UndirectedSpecifics(
        AbstractBaseGraph<V, E> abstractBaseGraph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap)
    {
        this(abstractBaseGraph, vertexMap, new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new undirected specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public UndirectedSpecifics(
        AbstractBaseGraph<V, E> abstractBaseGraph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap,
        EdgeSetFactory<V, E> edgeSetFactory)
    {
        this.abstractBaseGraph = abstractBaseGraph;
        this.vertexMapUndirected = vertexMap;
        this.edgeSetFactory = edgeSetFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        UndirectedEdgeContainer<V, E> ec = vertexMapUndirected.get(v);
        if (ec == null) {
            vertexMapUndirected.put(v, new UndirectedEdgeContainer<>(edgeSetFactory, v));
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
        return vertexMapUndirected.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> edges = null;

        if (abstractBaseGraph.containsVertex(sourceVertex)
            && abstractBaseGraph.containsVertex(targetVertex))
        {
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
        if (abstractBaseGraph.containsVertex(sourceVertex)
            && abstractBaseGraph.containsVertex(targetVertex))
        {

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
        boolean equalStraight = sourceVertex.equals(abstractBaseGraph.getEdgeSource(e))
            && targetVertex.equals(abstractBaseGraph.getEdgeTarget(e));

        boolean equalInverted = sourceVertex.equals(abstractBaseGraph.getEdgeTarget(e))
            && targetVertex.equals(abstractBaseGraph.getEdgeSource(e));
        return equalStraight || equalInverted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdgeToTouchingVertices(E e)
    {
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

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
        if (abstractBaseGraph.getType().isAllowingSelfLoops()) { // then we must count, and add loops twice
            int degree = 0;
            Set<E> edges = getEdgeContainer(vertex).vertexEdges;

            for (E e : edges) {
                if (abstractBaseGraph.getEdgeSource(e).equals(abstractBaseGraph.getEdgeTarget(e))) {
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
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

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
        UndirectedEdgeContainer<V, E> ec = vertexMapUndirected.get(vertex);

        if (ec == null) {
            ec = new UndirectedEdgeContainer<>(edgeSetFactory, vertex);
            vertexMapUndirected.put(vertex, ec);
        }

        return ec;
    }
}
