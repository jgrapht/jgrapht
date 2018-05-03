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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.util.ArrayUnenforcedSet;

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
    implements Specifics<V, E>, Serializable
{
    private static final long serialVersionUID = 8971725103718958232L;

    protected AbstractBaseGraph<V, E> abstractBaseGraph;
    protected Map<V, DirectedEdgeContainer<V, E>> vertexMapDirected;
    protected EdgeSetFactory<V, E> edgeSetFactory;

    /**
     * Construct a new directed specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     */
    public DirectedSpecifics(AbstractBaseGraph<V, E> abstractBaseGraph)
    {
        this(abstractBaseGraph, new LinkedHashMap<>(), new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new directed specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     */
    public DirectedSpecifics(
        AbstractBaseGraph<V, E> abstractBaseGraph, Map<V, DirectedEdgeContainer<V, E>> vertexMap)
    {
        this(abstractBaseGraph, vertexMap, new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new directed specifics.
     * 
     * @param abstractBaseGraph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public DirectedSpecifics(
        AbstractBaseGraph<V, E> abstractBaseGraph, Map<V, DirectedEdgeContainer<V, E>> vertexMap,
        EdgeSetFactory<V, E> edgeSetFactory)
    {
        this.abstractBaseGraph = abstractBaseGraph;
        this.vertexMapDirected = vertexMap;
        this.edgeSetFactory = edgeSetFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        DirectedEdgeContainer<V, E> ec = vertexMapDirected.get(v);
        if (ec == null) {
            vertexMapDirected.put(v, new DirectedEdgeContainer<>(edgeSetFactory, v));
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
        return vertexMapDirected.keySet();
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

            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (abstractBaseGraph.getEdgeTarget(e).equals(targetVertex)) {
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
            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (abstractBaseGraph.getEdgeTarget(e).equals(targetVertex)) {
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
    public void addEdgeToTouchingVertices(E e)
    {
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

        getEdgeContainer(source).addOutgoingEdge(e);
        getEdgeContainer(target).addIncomingEdge(e);
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
        inAndOut.addAll(getEdgeContainer(vertex).outgoing);

        // we have two copies for each self-loop - remove one of them.
        if (abstractBaseGraph.getType().isAllowingSelfLoops()) {
            Set<E> loops = getAllEdges(vertex, vertex);

            for (int i = 0; i < inAndOut.size();) {
                E e = inAndOut.get(i);

                if (loops.contains(e)) {
                    inAndOut.remove(i);
                    loops.remove(e); // so we remove it only once
                } else {
                    i++;
                }
            }
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
    public void removeEdgeFromTouchingVertices(E e)
    {
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

        getEdgeContainer(source).removeOutgoingEdge(e);
        getEdgeContainer(target).removeIncomingEdge(e);
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
        DirectedEdgeContainer<V, E> ec = vertexMapDirected.get(vertex);

        if (ec == null) {
            ec = new DirectedEdgeContainer<>(edgeSetFactory, vertex);
            vertexMapDirected.put(vertex, ec);
        }

        return ec;
    }
}
