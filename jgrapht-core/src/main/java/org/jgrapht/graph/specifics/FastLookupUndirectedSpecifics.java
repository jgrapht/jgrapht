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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.UnorderedPair;
import org.jgrapht.graph.EdgeSetFactory;

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
        if (edges == null || edges.isEmpty())
            return null;
        else
            return edges.iterator().next();
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
        if (!super.addEdgeToTouchingVerticesIfAbsent(sourceVertex, targetVertex, e)) {
            return false;
        }
        addToIndex(sourceVertex, targetVertex, e);
        return true;
    }

    @Override
    public E computeEdgeToTouchingVerticesIfAbsent(
        V sourceVertex, V targetVertex, Supplier<E> edgeSupplier)
    {
        E e = super.computeEdgeToTouchingVerticesIfAbsent(sourceVertex, targetVertex, edgeSupplier);
        if (e == null) {
            return null;
        }
        addToIndex(sourceVertex, targetVertex, e);
        return e;
    }

    @Override
    public void removeEdgeFromTouchingVertices(E e)
    {
        super.removeEdgeFromTouchingVertices(e);

        V source = graph.getEdgeSource(e);
        V target = graph.getEdgeTarget(e);
        removeFromIndex(source, target, e);
    }

    /*
     * Add edge to touchingVerticesToEdgeMap for the UnorderedPair {u,v}
     */
    private void addToIndex(V sourceVertex, V targetVertex, E e)
    {
        Pair<V, V> vertexPair = new UnorderedPair<>(sourceVertex, targetVertex);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null)
            edgeSet.add(e);
        else {
            edgeSet = edgeSetFactory.createEdgeSet(sourceVertex);
            edgeSet.add(e);
            touchingVerticesToEdgeMap.put(vertexPair, edgeSet);
        }
    }

    /*
     * Remove the edge from the touchingVerticesToEdgeMap. If there are no more remaining edges for
     * a pair of touching vertices, remove the pair from the map.
     */
    private void removeFromIndex(V sourceVertex, V targetVertex, E e)
    {
        Pair<V, V> vertexPair = new UnorderedPair<>(sourceVertex, targetVertex);
        Set<E> edgeSet = touchingVerticesToEdgeMap.get(vertexPair);
        if (edgeSet != null) {
            edgeSet.remove(e);
            if (edgeSet.isEmpty())
                touchingVerticesToEdgeMap.remove(vertexPair);
        }
    }

}
