/*
 * (C) Copyright 2015-2021, by Joris Kinable and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.function.*;

/**
 * Fast implementation of DirectedSpecifics. This class uses additional data structures to improve
 * the performance of methods which depend on edge retrievals, e.g. getEdge(V u, V v),
 * containsEdge(V u, V v),addEdge(V u, V v). A disadvantage is an increase in memory consumption. If
 * memory utilization is an issue, use a {@link DirectedSpecifics} instead.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class FastLookupDirectedSpecifics<V, E>
    extends
    DirectedSpecifics<V, E>
{
    private static final long serialVersionUID = 4089085208843722263L;

    /**
     * Maps a pair of vertices &lt;u,v&gt; to a set of edges {(u,v)}. In case of a multigraph, all
     * edges which touch both u and v are included in the set.
     */
    protected Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap;

    /**
     * Construct a new fast lookup directed specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param touchingVerticesToEdgeMap Additional map for caching. No need for a predictable
     *        iteration order.
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public FastLookupDirectedSpecifics(
        Graph<V, E> graph, Map<V, DirectedEdgeContainer<V, E>> vertexMap,
        Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap, EdgeSetFactory<V, E> edgeSetFactory)
    {
        super(graph, vertexMap, edgeSetFactory);
        this.touchingVerticesToEdgeMap = Objects.requireNonNull(touchingVerticesToEdgeMap);
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (graph.containsVertex(sourceVertex) && graph.containsVertex(targetVertex)) {
            Set<E> edges = touchingVerticesToEdgeMap.get(new Pair<>(sourceVertex, targetVertex));
            if (edges == null) {
                return Collections.emptySet();
            } else {
                Set<E> edgeSet = edgeSetFactory.createEdgeSet(sourceVertex);
                edgeSet.addAll(edges);
                return edgeSet;
            }
        } else {
            return null;
        }
    }

    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        Set<E> edges = touchingVerticesToEdgeMap.get(new Pair<>(sourceVertex, targetVertex));
        if (edges == null || edges.isEmpty())
            return null;
        else {
            return edges.iterator().next();
        }
    }

    @Override
    public boolean addEdgeToTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        if (!super.addEdgeToTouchingVertices(sourceVertex, targetVertex, e)) {
            return false;
        }
        addToIndex(sourceVertex, targetVertex, e);
        return true;
    }

    @Override
    public boolean addEdgeToTouchingVerticesIfAbsent(V sourceVertex, V targetVertex, E e)
    {
        // first lookup using our own index
        E edge = getEdge(sourceVertex, targetVertex);
        if (edge != null) {
            return false;
        }

        return addEdgeToTouchingVertices(sourceVertex, targetVertex, e);
    }

    @Override
    public E createEdgeToTouchingVerticesIfAbsent(
        V sourceVertex, V targetVertex, Supplier<E> edgeSupplier)
    {
        // first lookup using our own index
        E edge = getEdge(sourceVertex, targetVertex);
        if (edge != null) {
            return null;
        }

        E e = edgeSupplier.get();
        addEdgeToTouchingVertices(sourceVertex, targetVertex, e);
        return e;
    }

    @Override
    public void removeEdgeFromTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        super.removeEdgeFromTouchingVertices(sourceVertex, targetVertex, e);

        removeFromIndex(sourceVertex, targetVertex, e);
    }

    /**
     * Add an edge to the index.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     */
    protected void addToIndex(V sourceVertex, V targetVertex, E e)
    {
        Pair<V, V> vertexPair = new Pair<>(sourceVertex, targetVertex);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null)
            edgeSet.add(e);
        else {
            edgeSet = edgeSetFactory.createEdgeSet(sourceVertex);
            edgeSet.add(e);
            touchingVerticesToEdgeMap.put(vertexPair, edgeSet);
        }
    }

    /**
     * Remove an edge from the index.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     */
    protected void removeFromIndex(V sourceVertex, V targetVertex, E e)
    {
        Pair<V, V> vertexPair = new Pair<>(sourceVertex, targetVertex);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null) {
            edgeSet.remove(e);
            if (edgeSet.isEmpty()) {
                touchingVerticesToEdgeMap.remove(vertexPair);
            }
        }
    }

}
