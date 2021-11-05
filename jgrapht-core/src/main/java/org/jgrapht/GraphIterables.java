/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

import org.jgrapht.util.LiveIterableWrapper;

/**
 * Presents a graph as a collection of views suitable for graphs which contain a very large number
 * of vertices or edges. Graph algorithms written these methods can work with graphs without the
 * restrictions imposed by 32-bit arithmetic.
 *
 * <p>
 * Whether the returned iterators support removal of elements is left to the graph implementation.
 * It is the responsibility of callers who rely on this behavior to only use graph implementations
 * which support it.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface GraphIterables<V, E>
{
    /**
     * Get the underlying graph.
     * 
     * @return the underlying graph
     */
    Graph<V, E> getGraph();

    /**
     * Returns an iterable over the edges of the graph.
     * 
     * <p>
     * Whether the ordering is deterministic, depends on the actual graph implementation. It is the
     * responsibility of callers who rely on this behavior to only use graph implementations which
     * support it.
     * 
     * @return an iterable over the edges of the graph.
     */
    default Iterable<E> edges()
    {
        return new LiveIterableWrapper<>(() -> getGraph().edgeSet());
    }

    /**
     * Return the number of edges in the graph.
     * 
     * @return the number of edges.
     */
    default long edgeCount()
    {
        return getGraph().edgeSet().size();
    }

    /**
     * Returns an iterable view over the vertices contained in this graph. The returned iterator is
     * a live view of the vertices. If the graph is modified while an iteration is in progress, the
     * results of the iteration are undefined.
     *
     * <p>
     * The graph implementation may maintain a particular ordering for deterministic iteration, but
     * this is not required. It is the responsibility of callers who rely on this behavior to only
     * use graph implementations which support it.
     * </p>
     * 
     * @return an iterable view of the vertices contained in this graph
     */
    default Iterable<V> vertices()
    {
        return new LiveIterableWrapper<>(() -> getGraph().vertexSet());
    }

    /**
     * Return the number of vertices in the graph.
     * 
     * @return the number of vertices
     */
    default long vertexCount()
    {
        return getGraph().vertexSet().size();
    }

    /**
     * Returns an iterable view over all edges touching the specified vertex. The returned iterators
     * are live views. If the graph is modified while an iteration is in progress, the results of
     * the iteration are undefined. If no edges are touching the specified vertex, the returned
     * iterators are already exhausted.
     *
     * @param vertex input vertex
     * @return an iterable view of the vertices contained in this graph
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default Iterable<E> edgesOf(V vertex)
    {
        return new LiveIterableWrapper<>(() -> getGraph().edgesOf(vertex));
    }

    /**
     * Returns the degree of the specified vertex.
     * 
     * <p>
     * A degree of a vertex in an undirected graph is the number of edges touching that vertex.
     * Edges with same source and target vertices (self-loops) are counted twice.
     * 
     * <p>
     * In directed graphs this method returns the sum of the "in degree" and the "out degree".
     *
     * @param vertex vertex whose degree is to be calculated.
     * @return the degree of the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default long degreeOf(V vertex)
    {
        return getGraph().degreeOf(vertex);
    }

    /**
     * Returns an iterable view over all edges incoming into the specified vertex. The returned
     * iterators are live views. If the graph is modified while an iteration is in progress, the
     * results of the iteration are undefined.
     * 
     * <p>
     * In the case of undirected graphs the returned iterators return all edges touching the vertex,
     * thus, some of the returned edges may have their source and target vertices in the opposite
     * order.
     *
     * @param vertex input vertex
     * @return an iterable view of all edges incoming into the specified vertex
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default Iterable<E> incomingEdgesOf(V vertex)
    {
        return new LiveIterableWrapper<>(() -> getGraph().incomingEdgesOf(vertex));
    }

    /**
     * Returns the "in degree" of the specified vertex.
     * 
     * <p>
     * The "in degree" of a vertex in a directed graph is the number of inward directed edges from
     * that vertex. See <a href="http://mathworld.wolfram.com/Indegree.html">
     * http://mathworld.wolfram.com/Indegree.html</a>.
     * 
     * <p>
     * In the case of undirected graphs this method returns the number of edges touching the vertex.
     * Edges with same source and target vertices (self-loops) are counted twice.
     *
     * @param vertex vertex whose degree is to be calculated.
     * @return the degree of the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default long inDegreeOf(V vertex)
    {
        return getGraph().inDegreeOf(vertex);
    }

    /**
     * Returns an iterable view over all edges outgoing into the specified vertex. The returned
     * iterators are live views. If the graph is modified while an iteration is in progress, the
     * results of the iteration are undefined.
     * 
     * <p>
     * In the case of undirected graphs the returned iterators return all edges touching the vertex,
     * thus, some of the returned edges may have their source and target vertices in the opposite
     * order.
     *
     * @param vertex input vertex
     * @return an iterable view of all edges outgoing from the specified vertex
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default Iterable<E> outgoingEdgesOf(V vertex)
    {
        return new LiveIterableWrapper<>(() -> getGraph().outgoingEdgesOf(vertex));
    }

    /**
     * Returns the "out degree" of the specified vertex.
     * 
     * <p>
     * The "out degree" of a vertex in a directed graph is the number of outward directed edges from
     * that vertex. See <a href="http://mathworld.wolfram.com/Outdegree.html">
     * http://mathworld.wolfram.com/Outdegree.html</a>.
     * 
     * <p>
     * In the case of undirected graphs this method returns the number of edges touching the vertex.
     * Edges with same source and target vertices (self-loops) are counted twice.
     *
     * @param vertex vertex whose degree is to be calculated.
     * @return the degree of the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default long outDegreeOf(V vertex)
    {
        return getGraph().outDegreeOf(vertex);
    }

    /**
     * Returns an iterable view over all edges connecting source vertex to target vertex if such
     * vertices exist in this graph. The returned iterators are live views. If the graph is modified
     * while an iteration is in progress, the results of the iteration are undefined.
     * 
     * If any of the vertices does not exist or is <code>null</code>, returns <code>null</code>. If
     * both vertices exist but no edges found, returns an iterable which returns exhausted
     * iterators.
     * 
     * <p>
     * In undirected graphs, some of the returned edges may have their source and target vertices in
     * the opposite order. In simple graphs the returned set is either singleton set or empty set.
     * </p>
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return an iterable view of all edges connecting source to target vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    default Iterable<E> allEdges(V sourceVertex, V targetVertex)
    {
        return new LiveIterableWrapper<>(() -> getGraph().getAllEdges(sourceVertex, targetVertex));
    }

}
