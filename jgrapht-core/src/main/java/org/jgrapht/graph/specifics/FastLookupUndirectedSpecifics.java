/*
 * (C) Copyright 2015-2020, by Joris Kinable and Contributors.
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
 * Fast implementation of UndirectedSpecifics. This class uses additional data structures to improve
 * the performance of methods which depend on edge retrievals, e.g. getEdge(V u, V v),
 * containsEdge(V u, V v), addEdge(V u, V v). A disadvantage is an increase in memory consumption.
 * If memory utilization is an issue, use a {@link UndirectedSpecifics} instead.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class FastLookupUndirectedSpecifics<V, E>
    extends
    UndirectedSpecifics<V, E>
{
    private static final long serialVersionUID = 225772727571597846L;

    /**
     * Maps a pair of vertices &lt;u,v&gt; to a set of edges {(u,v)}. In case of a multigraph, all
     * edges which touch both u and v are included in the set.
     */
    protected Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap;

    /**
     * Construct a new fast lookup undirected specifics.
     *
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param touchingVerticesToEdgeMap Additional map for caching. No need for a predictable
     *        iteration order.
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public FastLookupUndirectedSpecifics(
        Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap,
        Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap, EdgeSetFactory<V, E> edgeSetFactory)
    {
        super(graph, vertexMap, edgeSetFactory);
        this.touchingVerticesToEdgeMap = Objects.requireNonNull(touchingVerticesToEdgeMap);
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (graph.containsVertex(sourceVertex) && graph.containsVertex(targetVertex)) {
            Set<E> edges =
                touchingVerticesToEdgeMap.get(new UnorderedPair<>(sourceVertex, targetVertex));
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
        Set<E> edges =
            touchingVerticesToEdgeMap.get(new UnorderedPair<>(sourceVertex, targetVertex));
        if (edges == null || edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    @Override
    public boolean addEdgeToTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        addToTouchingVertices(sourceVertex, targetVertex, e, null, false);
        return true;
    }

    @Override
    public boolean addEdgeToTouchingVerticesIfAbsent(V sourceVertex, V targetVertex, E e)
    {
        return addToTouchingVertices(sourceVertex, targetVertex, e, null, true) != null;
    }

    @Override
    public E createEdgeToTouchingVerticesIfAbsent(
        V sourceVertex, V targetVertex, Supplier<E> edgeSupplier)
    {
        return addToTouchingVertices(sourceVertex, targetVertex, null, edgeSupplier, true);
    }

    private E addToTouchingVertices(
        V source, V target, E edge, Supplier<E> edgeSupplier, boolean addOnlyIfAbsent)
    {
        int previousSize = touchingVerticesToEdgeMap.size();
        UnorderedPair<V, V> pair = new UnorderedPair<>(source, target);

        Set<E> edgeSet = touchingVerticesToEdgeMap
            .computeIfAbsent(pair, p -> edgeSetFactory.createEdgeSet(p.getFirst()));

        if (!addOnlyIfAbsent || previousSize < touchingVerticesToEdgeMap.size()) { // new pair added
            E e = edge != null ? edge : edgeSupplier.get();
            super.addEdgeToTouchingVertices(source, target, e);
            edgeSet.add(e);
            addToIndex(source, target, e);
            return e;
        }
        return null;
    }

    @Override
    public void removeEdgeFromTouchingVertices(V sourceVertex, V targetVertex, E e)
    {
        super.removeEdgeFromTouchingVertices(sourceVertex, targetVertex, e);

        Pair<V, V> vertexPair = new UnorderedPair<>(sourceVertex, targetVertex);
        touchingVerticesToEdgeMap.computeIfPresent(vertexPair, (p, edgeSet) -> {
            edgeSet.remove(e);
            return !edgeSet.isEmpty() ? edgeSet : null; // remove if empty
        });

        removeFromIndex(sourceVertex, targetVertex, e);
    }

    /**
     * Add an edge to the index.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     * @deprecated not used anymore, without replacement
     */
    @Deprecated(forRemoval = true, since = "1.5.1")
    protected void addToIndex(V sourceVertex, V targetVertex, E e)
    {
        // TODO: Remove this after next release. Only kept for backward compatibility.
    }

    /**
     * Remove an edge from the index.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     * @deprecated not used anymore, without replacement
     */
    @Deprecated(forRemoval = true, since = "1.5.1")
    protected void removeFromIndex(V sourceVertex, V targetVertex, E e)
    {
        // TODO: Remove this after next release. Only kept for backward compatibility.
        // Code of this method was in-lined into the only caller and deprecated to be consistent
        // with addToIndex(V,V,E)
    }

}
