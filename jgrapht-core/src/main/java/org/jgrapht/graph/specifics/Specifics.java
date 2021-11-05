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

import java.util.*;
import java.util.function.*;

/**
 * An interface encapsulating the basic graph operations. Different implementations have different
 * space-time tradeoffs.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public interface Specifics<V, E>
{
    /**
     * Adds a vertex.
     *
     * @param vertex vertex to be added.
     * @return true if the vertex was added, false if the vertex was already present
     */
    boolean addVertex(V vertex);

    /**
     * Get the vertex set.
     * 
     * @return the vertex set
     */
    Set<V> getVertexSet();

    /**
     * Returns a set of all edges connecting source vertex to target vertex if such vertices exist
     * in this graph. If any of the vertices does not exist or is <code>null</code>, returns
     * <code>null</code>. If both vertices exist but no edges found, returns an empty set.
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return a set of all edges connecting source vertex to target vertex.
     */
    Set<E> getAllEdges(V sourceVertex, V targetVertex);

    /**
     * Returns an edge connecting source vertex to target vertex if such vertices and such edge
     * exist in this graph. Otherwise returns <code>
     * null</code>. If any of the specified vertices is <code>null</code> returns <code>null</code>
     *
     * <p>
     * In undirected graphs, the returned edge may have its source and target vertices in the
     * opposite order.
     * </p>
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return an edge connecting source vertex to target vertex.
     */
    E getEdge(V sourceVertex, V targetVertex);

    /**
     * Adds the specified edge to the edge containers of its source and target vertices.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     * @return true if the edge was added, false otherwise
     */
    boolean addEdgeToTouchingVertices(V sourceVertex, V targetVertex, E e);

    /**
     * Adds the specified edge to the edge containers of its source and target vertices only if the
     * edge is not already in the graph.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     * @return true if the edge was added, false otherwise
     */
    boolean addEdgeToTouchingVerticesIfAbsent(V sourceVertex, V targetVertex, E e);

    /**
     * Creates an edge given an edge supplier and adds it to the edge containers of its source and
     * target vertices only if the graph does not contain other edges with the same source and
     * target vertices.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param edgeSupplier the function which will create the edge
     * @return the newly created edge or null if an edge with the same source and target vertices
     *         was already present
     */
    E createEdgeToTouchingVerticesIfAbsent(
        V sourceVertex, V targetVertex, Supplier<E> edgeSupplier);

    /**
     * Returns the degree of the specified vertex. A degree of a vertex in an undirected graph is
     * the number of edges touching that vertex.
     *
     * @param vertex vertex whose degree is to be calculated.
     *
     * @return the degree of the specified vertex.
     */
    int degreeOf(V vertex);

    /**
     * Returns a set of all edges touching the specified vertex. If no edges are touching the
     * specified vertex returns an empty set.
     *
     * @param vertex the vertex for which a set of touching edges is to be returned.
     * @return a set of all edges touching the specified vertex.
     */
    Set<E> edgesOf(V vertex);

    /**
     * Returns the "in degree" of the specified vertex.
     *
     * @param vertex vertex whose in degree is to be calculated.
     * @return the in degree of the specified vertex.
     */
    int inDegreeOf(V vertex);

    /**
     * Returns a set of all edges incoming into the specified vertex.
     *
     * @param vertex the vertex for which the list of incoming edges to be returned.
     * @return a set of all edges incoming into the specified vertex.
     */
    Set<E> incomingEdgesOf(V vertex);

    /**
     * Returns the "out degree" of the specified vertex.
     *
     * @param vertex vertex whose out degree is to be calculated.
     * @return the out degree of the specified vertex.
     */
    int outDegreeOf(V vertex);

    /**
     * Returns a set of all edges outgoing from the specified vertex.
     *
     * @param vertex the vertex for which the list of outgoing edges to be returned.
     *
     * @return a set of all edges outgoing from the specified vertex.
     */
    Set<E> outgoingEdgesOf(V vertex);

    /**
     * Removes the specified edge from the edge containers of its source and target vertices.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param e the edge
     */
    void removeEdgeFromTouchingVertices(V sourceVertex, V targetVertex, E e);

}
