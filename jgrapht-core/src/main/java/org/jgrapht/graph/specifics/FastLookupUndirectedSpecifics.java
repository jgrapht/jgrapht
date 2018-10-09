/*
 * (C) Copyright 2015-2018, by Joris Kinable and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;

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
    extends UndirectedSpecifics<V, E>
{
    private static final long serialVersionUID = 225772727571597846L;

    /**
     * Maps a pair of vertices &lt;u,v&gt; to a set of edges {(u,v)}. In case of a multigraph, all edges
     * which touch both u and v are included in the set.
     */
    protected Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap;

    /**
     * Construct a new fast lookup undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @deprecated Since default strategies should be decided at a higher level.
     */
    @Deprecated
    public FastLookupUndirectedSpecifics(Graph<V, E> graph)
    {
        this(graph, new LinkedHashMap<>(), new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new fast lookup undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets
     * @deprecated Since default strategies should be decided at a higher level.
     */
    @Deprecated
    public FastLookupUndirectedSpecifics(
        Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap)
    {
        this(graph, vertexMap, new ArrayUnenforcedSetEdgeSetFactory<>());
    }

    /**
     * Construct a new fast lookup undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param edgeSetFactory factory for the creation of vertex edge sets
     * @deprecated Since default strategies should be decided at a higher level. 
     */
    @Deprecated
    public FastLookupUndirectedSpecifics(
        Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap,
        EdgeSetFactory<V, E> edgeSetFactory)
    {
        this(graph, vertexMap, new HashMap<>(), edgeSetFactory);
    }

    /**
     * Construct a new fast lookup undirected specifics.
     * 
     * @param graph the graph for which these specifics are for
     * @param vertexMap map for the storage of vertex edge sets. Needs to have a predictable
     *        iteration order.
     * @param touchingVerticesToEdgeMap Additional map for caching. No need for a predictable iteration order.        
     * @param edgeSetFactory factory for the creation of vertex edge sets
     */
    public FastLookupUndirectedSpecifics(
        Graph<V, E> graph, Map<V, UndirectedEdgeContainer<V, E>> vertexMap,
        Map<Pair<V, V>, Set<E>> touchingVerticesToEdgeMap,
        EdgeSetFactory<V, E> edgeSetFactory)
    {
        super(graph, vertexMap, edgeSetFactory);
        this.touchingVerticesToEdgeMap = Objects.requireNonNull(touchingVerticesToEdgeMap);
    }
    
    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        Set<E> edges =
            touchingVerticesToEdgeMap.get(new UnorderedPair<>(sourceVertex, targetVertex));
        if (edges == null || edges.isEmpty())
            return null;
        else
            return edges.iterator().next();
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

        // Add edge to touchingVerticesToEdgeMap for the UnorderedPair {u,v}
        Pair<V, V> vertexPair = new UnorderedPair<>(source, target);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null)
            edgeSet.add(e);
        else {
            edgeSet = edgeSetFactory.createEdgeSet(source);
            edgeSet.add(e);
            touchingVerticesToEdgeMap.put(vertexPair, edgeSet);
        }

        if (!source.equals(target)) { // If not a self loop
            getEdgeContainer(target).addEdge(e);
        }
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

        if (!source.equals(target))
            getEdgeContainer(target).removeEdge(e);

        /*
         * Remove the edge from the touchingVerticesToEdgeMap. If there are no more remaining edges
         * for a pair of touching vertices, remove the pair from the map.
         */
        Pair<V, V> vertexPair = new UnorderedPair<>(source, target);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null) {
            edgeSet.remove(e);
            if (edgeSet.isEmpty())
                touchingVerticesToEdgeMap.remove(vertexPair);
        }
    }

}
