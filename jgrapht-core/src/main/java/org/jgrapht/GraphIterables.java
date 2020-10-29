/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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

/**
 * A collection of graph iterables suitable for graph implementations which may
 * contain a large number of vertices and edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface GraphIterables<V, E> {

	/**
	 * Returns an iterable over the edges of the graph.
	 * 
	 * <p>
	 * Whether the ordering is deterministic, depends on the actual graph
	 * implementation. It is the responsibility of callers who rely on this behavior
	 * to only use graph implementations which support it.
	 * 
	 * @return an iterable over the edges of the graph.
	 */
	Iterable<E> edges();

	/**
	 * Return the number of edges in the graph.
	 * 
	 * @return the number of edges.
	 */
	long edgeCount();

	/**
	 * Returns a set of the vertices contained in this graph. The set is backed by
	 * the graph, so changes to the graph are reflected in the set. If the graph is
	 * modified while an iteration over the set is in progress, the results of the
	 * iteration are undefined.
	 *
	 * <p>
	 * The graph implementation may maintain a particular set ordering (e.g. via
	 * {@link java.util.LinkedHashSet}) for deterministic iteration, but this is not
	 * required. It is the responsibility of callers who rely on this behavior to
	 * only use graph implementations which support it.
	 * </p>
	 *
	 * @return a set view of the vertices contained in this graph.
	 */
	Iterable<V> vertices();

	/**
	 * Return the number of vertices in the graph.
	 * 
	 * @return the number of vertices
	 */
	long vertexCount();

	/**
	 * Returns a set of all edges touching the specified vertex. If no edges are
	 * touching the specified vertex returns an empty set.
	 *
	 * @param vertex the vertex for which a set of touching edges is to be returned.
	 * @return a set of all edges touching the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	Iterable<E> edgesOf(V vertex);

	/**
	 * Returns the degree of the specified vertex.
	 * 
	 * <p>
	 * A degree of a vertex in an undirected graph is the number of edges touching
	 * that vertex. Edges with same source and target vertices (self-loops) are
	 * counted twice.
	 * 
	 * <p>
	 * In directed graphs this method returns the sum of the "in degree" and the
	 * "out degree".
	 *
	 * @param vertex vertex whose degree is to be calculated.
	 * @return the degree of the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	long degreeOf(V vertex);

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 *
	 * <p>
	 * In the case of undirected graphs this method returns all edges touching the
	 * vertex, thus, some of the returned edges may have their source and target
	 * vertices in the opposite order.
	 *
	 * @param vertex the vertex for which the list of incoming edges to be returned.
	 * @return a set of all edges incoming into the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	Iterable<E> incomingEdgesOf(V vertex);

	/**
	 * Returns the "in degree" of the specified vertex.
	 * 
	 * <p>
	 * The "in degree" of a vertex in a directed graph is the number of inward
	 * directed edges from that vertex. See
	 * <a href="http://mathworld.wolfram.com/Indegree.html">
	 * http://mathworld.wolfram.com/Indegree.html</a>.
	 * 
	 * <p>
	 * In the case of undirected graphs this method returns the number of edges
	 * touching the vertex. Edges with same source and target vertices (self-loops)
	 * are counted twice.
	 *
	 * @param vertex vertex whose degree is to be calculated.
	 * @return the degree of the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	long inDegreeOf(V vertex);

	/**
	 * Returns a set of all edges outgoing from the specified vertex.
	 * 
	 * <p>
	 * In the case of undirected graphs this method returns all edges touching the
	 * vertex, thus, some of the returned edges may have their source and target
	 * vertices in the opposite order.
	 *
	 * @param vertex the vertex for which the list of outgoing edges to be returned.
	 * @return a set of all edges outgoing from the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	Iterable<E> outgoingEdgesOf(V vertex);

	/**
	 * Returns the "out degree" of the specified vertex.
	 * 
	 * <p>
	 * The "out degree" of a vertex in a directed graph is the number of outward
	 * directed edges from that vertex. See
	 * <a href="http://mathworld.wolfram.com/Outdegree.html">
	 * http://mathworld.wolfram.com/Outdegree.html</a>.
	 * 
	 * <p>
	 * In the case of undirected graphs this method returns the number of edges
	 * touching the vertex. Edges with same source and target vertices (self-loops)
	 * are counted twice.
	 *
	 * @param vertex vertex whose degree is to be calculated.
	 * @return the degree of the specified vertex.
	 *
	 * @throws IllegalArgumentException if vertex is not found in the graph.
	 * @throws NullPointerException     if vertex is <code>null</code>.
	 */
	long outDegreeOf(V vertex);

	/**
	 * Returns a set of all edges connecting source vertex to target vertex if such
	 * vertices exist in this graph. If any of the vertices does not exist or is
	 * <code>null</code>, returns <code>null</code>. If both vertices exist but no
	 * edges found, returns an empty set.
	 *
	 * <p>
	 * In undirected graphs, some of the returned edges may have their source and
	 * target vertices in the opposite order. In simple graphs the returned set is
	 * either singleton set or empty set.
	 * </p>
	 *
	 * @param sourceVertex source vertex of the edge.
	 * @param targetVertex target vertex of the edge.
	 *
	 * @return a set of all edges connecting source vertex to target vertex.
	 */
	Iterable<E> allEdges(V sourceVertex, V targetVertex);

}
