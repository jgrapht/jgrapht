/*
 * (C) Copyright 2021-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse.specifics;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;

/**
 * Specifics which provide a sparse graph implementation.
 * 
 * @author Dimitrios Michail
 */
public interface SparseGraphSpecifics
{
    /**
     * Get the number of edges.
     * 
     * @return the number of edges
     */
    long edgesCount();

    /**
     * Get the number of vertices
     * 
     * @return the number of vertices
     */
    long verticesCount();

    /**
     * Returns <code>true</code> if this graph contains the specified edge. More formally, returns
     * <code>true</code> if and only if this graph contains an edge <code>e2</code> such that
     * <code>e.equals(e2)</code>. If the specified edge is <code>null</code> returns
     * <code>false</code>.
     *
     * @param e edge whose presence in this graph is to be tested.
     *
     * @return <code>true</code> if this graph contains the specified edge.
     */
    default boolean containsEdge(Integer e)
    {
        return e >= 0 && e < edgesCount();
    }

    /**
     * Returns <code>true</code> if this graph contains the specified vertex. More formally, returns
     * <code>true</code> if and only if this graph contains a vertex <code>u</code> such that
     * <code>u.equals(v)</code>. If the specified vertex is <code>null</code> returns
     * <code>false</code>.
     *
     * @param v vertex whose presence in this graph is to be tested.
     *
     * @return <code>true</code> if this graph contains the specified vertex.
     */
    default boolean containsVertex(Integer v)
    {
        return v >= 0 && v < verticesCount();
    }

    /**
     * Returns a set of the edges contained in this graph. The set is backed by the graph, so
     * changes to the graph are reflected in the set. If the graph is modified while an iteration
     * over the set is in progress, the results of the iteration are undefined.
     *
     * <p>
     * The graph implementation may maintain a particular set ordering (e.g. via
     * {@link java.util.LinkedHashSet}) for deterministic iteration, but this is not required. It is
     * the responsibility of callers who rely on this behavior to only use graph implementations
     * which support it.
     * </p>
     *
     * @return a set of the edges contained in this graph.
     */
    default Set<Integer> edgeSet()
    {
        Long edgesCount = edgesCount();
        if (edgesCount > Integer.MAX_VALUE) { 
            throw new ArithmeticException("integer overflow");
        }
        return new CompleteIntegerSet(edgesCount.intValue());
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
     * @throws ArithmeticException if the result overflows an int
     */
    long degreeOf(Integer vertex);

    /**
     * Returns a set of all edges touching the specified vertex. If no edges are touching the
     * specified vertex returns an empty set.
     *
     * @param vertex the vertex for which a set of touching edges is to be returned.
     * @return a set of all edges touching the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    Set<Integer> edgesOf(Integer vertex);

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
     * @throws ArithmeticException if the result overflows an int
     */
    long inDegreeOf(Integer vertex);

    /**
     * Returns a set of all edges incoming into the specified vertex.
     *
     * <p>
     * In the case of undirected graphs this method returns all edges touching the vertex, thus,
     * some of the returned edges may have their source and target vertices in the opposite order.
     *
     * @param vertex the vertex for which the list of incoming edges to be returned.
     * @return a set of all edges incoming into the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    Set<Integer> incomingEdgesOf(Integer vertex);

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
     * @throws ArithmeticException if the result overflows an int
     */
    long outDegreeOf(Integer vertex);

    /**
     * Returns a set of all edges outgoing from the specified vertex.
     * 
     * <p>
     * In the case of undirected graphs this method returns all edges touching the vertex, thus,
     * some of the returned edges may have their source and target vertices in the opposite order.
     *
     * @param vertex the vertex for which the list of outgoing edges to be returned.
     * @return a set of all edges outgoing from the specified vertex.
     *
     * @throws IllegalArgumentException if vertex is not found in the graph.
     * @throws NullPointerException if vertex is <code>null</code>.
     */
    Set<Integer> outgoingEdgesOf(Integer vertex);

    /**
     * Returns a set of the vertices contained in this graph. The set is backed by the graph, so
     * changes to the graph are reflected in the set. If the graph is modified while an iteration
     * over the set is in progress, the results of the iteration are undefined.
     *
     * <p>
     * The graph implementation may maintain a particular set ordering (e.g. via
     * {@link java.util.LinkedHashSet}) for deterministic iteration, but this is not required. It is
     * the responsibility of callers who rely on this behavior to only use graph implementations
     * which support it.
     * </p>
     *
     * @return a set view of the vertices contained in this graph.
     */
    default Set<Integer> vertexSet()
    {
        Long verticesCount = verticesCount();
        if (verticesCount > Integer.MAX_VALUE) { 
            throw new ArithmeticException("integer overflow");
        }
        return new CompleteIntegerSet(verticesCount.intValue());
    }

    /**
     * Returns the source vertex of an edge. For an undirected graph, source and target are
     * distinguishable designations (but without any mathematical meaning).
     *
     * @param e edge of interest
     *
     * @return source vertex
     */
    Integer getEdgeSource(Integer e);

    /**
     * Returns the target vertex of an edge. For an undirected graph, source and target are
     * distinguishable designations (but without any mathematical meaning).
     *
     * @param e edge of interest
     *
     * @return target vertex
     */
    Integer getEdgeTarget(Integer e);

    /**
     * Get the graph type. The graph type can be used to query for additional metadata such as
     * whether the graph supports directed or undirected edges, self-loops, multiple (parallel)
     * edges, weights, etc.
     * 
     * @return the graph type
     */
    GraphType getType();

    /**
     * Returns the weight assigned to a given edge. Unweighted graphs return 1.0 (as defined by
     * {@link #DEFAULT_EDGE_WEIGHT}), allowing weighted-graph algorithms to apply to them when
     * meaningful.
     *
     * @param e edge of interest
     * @return edge weight
     */
    default double getEdgeWeight(Integer e)
    {
        return Graph.DEFAULT_EDGE_WEIGHT;
    }

    /**
     * Assigns a weight to an edge.
     *
     * @param e edge on which to set weight
     * @param weight new weight for edge
     * @throws UnsupportedOperationException if the graph does not support weights
     */
    default public void setEdgeWeight(Integer e, double weight)
    {
        throw new UnsupportedOperationException("this graph is unmodifiable");
    }

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
    Integer getEdge(Integer sourceVertex, Integer targetVertex);

    /**
     * Returns a set of all edges connecting source vertex to target vertex if such vertices exist
     * in this graph. If any of the vertices does not exist or is <code>null</code>, returns
     * <code>null</code>. If both vertices exist but no edges found, returns an empty set.
     *
     * <p>
     * In undirected graphs, some of the returned edges may have their source and target vertices in
     * the opposite order. In simple graphs the returned set is either singleton set or empty set.
     * </p>
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return a set of all edges connecting source vertex to target vertex.
     */
    Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex);

    /**
     * Ensures that the specified vertex exists in this graph, or else throws exception.
     *
     * @param v vertex
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified vertex does not exist in this graph.
     */
    default boolean assertVertexExist(Integer v)
    {
        if (v >= 0 && v < verticesCount()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    /**
     * Ensures that the specified edge exists in this graph, or else throws exception.
     *
     * @param e edge
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified edge does not exist in this graph.
     */
    default boolean assertEdgeExist(Integer e)
    {
        if (e >= 0 && e < edgesCount()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

}
