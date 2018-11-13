/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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

import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.function.*;

/**
 * A collection of utilities to assist with graph manipulation.
 *
 * @author Barak Naveh
 */
public abstract class Graphs
{

    /**
     * Creates a new edge and adds it to the specified graph similarly to the
     * {@link Graph#addEdge(Object, Object)} method.
     *
     * @param g the graph for which the edge to be added
     * @param sourceVertex source vertex of the edge
     * @param targetVertex target vertex of the edge
     * @param weight weight of the edge
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     * 
     * @throws UnsupportedOperationException if the graph has no edge supplier
     *
     * @see Graph#addEdge(Object, Object)
     */
    public static <V, E> E addEdge(Graph<V, E> g, V sourceVertex, V targetVertex, double weight)
    {
        Supplier<E> edgeSupplier = g.getEdgeSupplier();
        if (edgeSupplier == null) {
            throw new UnsupportedOperationException("Graph contains no edge supplier");
        }
        E e = edgeSupplier.get();

        if (g.addEdge(sourceVertex, targetVertex, e)) {
            g.setEdgeWeight(e, weight);
            return e;
        } else {
            return null;
        }
    }

    /**
     * Adds the specified source and target vertices to the graph, if not already included, and
     * creates a new edge and adds it to the specified graph similarly to the
     * {@link Graph#addEdge(Object, Object)} method.
     *
     * @param g the graph for which the specified edge to be added
     * @param sourceVertex source vertex of the edge
     * @param targetVertex target vertex of the edge
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     */
    public static <V, E> E addEdgeWithVertices(Graph<V, E> g, V sourceVertex, V targetVertex)
    {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return g.addEdge(sourceVertex, targetVertex);
    }

    /**
     * Adds the specified edge to the graph, including its vertices if not already included.
     *
     * @param targetGraph the graph for which the specified edge to be added
     * @param sourceGraph the graph in which the specified edge is already present
     * @param edge edge to add
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return <tt>true</tt> if the target graph did not already contain the specified edge.
     */
    public static <V,
        E> boolean addEdgeWithVertices(Graph<V, E> targetGraph, Graph<V, E> sourceGraph, E edge)
    {
        V sourceVertex = sourceGraph.getEdgeSource(edge);
        V targetVertex = sourceGraph.getEdgeTarget(edge);

        targetGraph.addVertex(sourceVertex);
        targetGraph.addVertex(targetVertex);

        return targetGraph.addEdge(sourceVertex, targetVertex, edge);
    }

    /**
     * Adds the specified source and target vertices to the graph, if not already included, and
     * creates a new weighted edge and adds it to the specified graph similarly to the
     * {@link Graph#addEdge(Object, Object)} method.
     *
     * @param g the graph for which the specified edge to be added
     * @param sourceVertex source vertex of the edge
     * @param targetVertex target vertex of the edge
     * @param weight weight of the edge
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     */
    public static <V,
        E> E addEdgeWithVertices(Graph<V, E> g, V sourceVertex, V targetVertex, double weight)
    {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return addEdge(g, sourceVertex, targetVertex, weight);
    }

    /**
     * Adds all the vertices and all the edges of the specified source graph to the specified
     * destination graph. First all vertices of the source graph are added to the destination graph.
     * Then every edge of the source graph is added to the destination graph. This method returns
     * <code>true</code> if the destination graph has been modified as a result of this operation,
     * otherwise it returns <code>false</code>.
     *
     * <p>
     * The behavior of this operation is undefined if any of the specified graphs is modified while
     * operation is in progress.
     * </p>
     *
     * @param destination the graph to which vertices and edges are added
     * @param source the graph used as source for vertices and edges to add
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return <code>true</code> if and only if the destination graph has been changed as a result
     *         of this operation.
     */
    public static <V,
        E> boolean addGraph(Graph<? super V, ? super E> destination, Graph<V, E> source)
    {
        boolean modified = addAllVertices(destination, source.vertexSet());
        modified |= addAllEdges(destination, source, source.edgeSet());

        return modified;
    }

    /**
     * Adds all the vertices and all the edges of the specified source digraph to the specified
     * destination digraph, reversing all of the edges. If you want to do this as a linked view of
     * the source graph (rather than by copying to a destination graph), use
     * {@link EdgeReversedGraph} instead.
     *
     * <p>
     * The behavior of this operation is undefined if any of the specified graphs is modified while
     * operation is in progress.
     *
     * @param destination the graph to which vertices and edges are added
     * @param source the graph used as source for vertices and edges to add
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @see EdgeReversedGraph
     */
    public static <V,
        E> void addGraphReversed(Graph<? super V, ? super E> destination, Graph<V, E> source)
    {
        if (!source.getType().isDirected() || !destination.getType().isDirected()) {
            throw new IllegalArgumentException("graph must be directed");
        }

        addAllVertices(destination, source.vertexSet());

        for (E edge : source.edgeSet()) {
            destination.addEdge(source.getEdgeTarget(edge), source.getEdgeSource(edge));
        }
    }

    /**
     * Adds a subset of the edges of the specified source graph to the specified destination graph.
     * The behavior of this operation is undefined if either of the graphs is modified while the
     * operation is in progress. {@link #addEdgeWithVertices} is used for the transfer, so source
     * vertexes will be added automatically to the target graph.
     *
     * @param destination the graph to which edges are to be added
     * @param source the graph used as a source for edges to add
     * @param edges the edges to be added
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return <tt>true</tt> if this graph changed as a result of the call
     */
    public static <V, E> boolean addAllEdges(
        Graph<? super V, ? super E> destination, Graph<V, E> source, Collection<? extends E> edges)
    {
        boolean modified = false;

        for (E e : edges) {
            V s = source.getEdgeSource(e);
            V t = source.getEdgeTarget(e);
            destination.addVertex(s);
            destination.addVertex(t);
            modified |= destination.addEdge(s, t, e);
        }

        return modified;
    }

    /**
     * Adds all of the specified vertices to the destination graph. The behavior of this operation
     * is undefined if the specified vertex collection is modified while the operation is in
     * progress. This method will invoke the {@link Graph#addVertex(Object)} method.
     *
     * @param destination the graph to which edges are to be added
     * @param vertices the vertices to be added to the graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return <tt>true</tt> if graph changed as a result of the call
     *
     * @throws NullPointerException if the specified vertices contains one or more null vertices, or
     *         if the specified vertex collection is <tt>
     * null</tt>.
     *
     * @see Graph#addVertex(Object)
     */
    public static <V, E> boolean addAllVertices(
        Graph<? super V, ? super E> destination, Collection<? extends V> vertices)
    {
        boolean modified = false;

        for (V v : vertices) {
            modified |= destination.addVertex(v);
        }

        return modified;
    }

    /**
     * Returns a list of vertices that are the neighbors of a specified vertex. If the graph is a
     * multigraph vertices may appear more than once in the returned list.
     *
     * <p>
     * The method uses {@link Graph#edgesOf(Object)} to traverse the graph.
     *
     * @param g the graph to look for neighbors in
     * @param vertex the vertex to get the neighbors of
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return a list of the vertices that are the neighbors of the specified vertex.
     */
    public static <V, E> List<V> neighborListOf(Graph<V, E> g, V vertex)
    {
        List<V> neighbors = new ArrayList<>();

        for (E e : g.edgesOf(vertex)) {
            neighbors.add(getOppositeVertex(g, e, vertex));
        }

        return neighbors;
    }

    /**
     * Returns a set of vertices that are neighbors of a specified vertex.
     *
     * @param g the graph to look for neighbors in
     * @param vertex the vertex to get the neighbors of
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a set of the vertices that are neighbors of the specified vertex
     */
    public static <V, E> Set<V> neighborSetOf(Graph<V, E> g, V vertex)
    {
        Set<V> neighbors = new LinkedHashSet<>();

        for (E e : g.edgesOf(vertex)) {
            neighbors.add(Graphs.getOppositeVertex(g, e, vertex));
        }

        return neighbors;
    }

    /**
     * Returns a list of vertices that are the direct predecessors of a specified vertex. If the
     * graph is a multigraph, vertices may appear more than once in the returned list.
     *
     * <p>
     * The method uses {@link Graph#incomingEdgesOf(Object)} to traverse the graph.
     *
     * @param g the graph to look for predecessors in
     * @param vertex the vertex to get the predecessors of
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return a list of the vertices that are the direct predecessors of the specified vertex.
     */
    public static <V, E> List<V> predecessorListOf(Graph<V, E> g, V vertex)
    {
        List<V> predecessors = new ArrayList<>();
        Set<? extends E> edges = g.incomingEdgesOf(vertex);

        for (E e : edges) {
            predecessors.add(getOppositeVertex(g, e, vertex));
        }

        return predecessors;
    }

    /**
     * Returns a list of vertices that are the direct successors of a specified vertex. If the graph
     * is a multigraph vertices may appear more than once in the returned list.
     *
     * <p>
     * The method uses {@link Graph#outgoingEdgesOf(Object)} to traverse the graph.
     *
     * @param g the graph to look for successors in
     * @param vertex the vertex to get the successors of
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return a list of the vertices that are the direct successors of the specified vertex.
     */
    public static <V, E> List<V> successorListOf(Graph<V, E> g, V vertex)
    {
        List<V> successors = new ArrayList<>();
        Set<? extends E> edges = g.outgoingEdgesOf(vertex);

        for (E e : edges) {
            successors.add(getOppositeVertex(g, e, vertex));
        }

        return successors;
    }

    /**
     * Returns an undirected view of the specified graph. If the specified graph is directed,
     * returns an undirected view of it. If the specified graph is already undirected, just returns
     * it.
     *
     * @param g the graph for which an undirected view is to be returned
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return an undirected view of the specified graph, if it is directed, or or the specified
     *         graph itself if it is already undirected.
     *
     * @throws IllegalArgumentException if the graph is neither directed nor undirected
     * @see AsUndirectedGraph
     */
    public static <V, E> Graph<V, E> undirectedGraph(Graph<V, E> g)
    {
        if (g.getType().isDirected()) {
            return new AsUndirectedGraph<>(g);
        } else if (g.getType().isUndirected()) {
            return g;
        } else {
            throw new IllegalArgumentException("graph must be either directed or undirected");
        }
    }

    /**
     * Tests whether an edge is incident to a vertex.
     *
     * @param g graph containing e and v
     * @param e edge in g
     * @param v vertex in g
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true iff e is incident on v
     */
    public static <V, E> boolean testIncidence(Graph<V, E> g, E e, V v)
    {
        return (g.getEdgeSource(e).equals(v)) || (g.getEdgeTarget(e).equals(v));
    }

    /**
     * Gets the vertex opposite another vertex across an edge.
     *
     * @param g graph containing e and v
     * @param e edge in g
     * @param v vertex in g
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return vertex opposite to v across e
     */
    public static <V, E> V getOppositeVertex(Graph<V, E> g, E e, V v)
    {
        V source = g.getEdgeSource(e);
        V target = g.getEdgeTarget(e);
        if (v.equals(source)) {
            return target;
        } else if (v.equals(target)) {
            return source;
        } else {
            throw new IllegalArgumentException("no such vertex: " + v.toString());
        }
    }

    /**
     * Removes the given vertex from the given graph. If the vertex to be removed has one or more
     * predecessors, the predecessors will be connected directly to the successors of the vertex to
     * be removed.
     *
     * @param graph graph to be mutated
     * @param vertex vertex to be removed from this graph, if present
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if the graph contained the specified vertex; false otherwise.
     */
    public static <V, E> boolean removeVertexAndPreserveConnectivity(Graph<V, E> graph, V vertex)
    {
        if (!graph.containsVertex(vertex)) {
            return false;
        }

        if (vertexHasPredecessors(graph, vertex)) {
            List<V> predecessors = Graphs.predecessorListOf(graph, vertex);
            List<V> successors = Graphs.successorListOf(graph, vertex);

            for (V predecessor : predecessors) {
                addOutgoingEdges(graph, predecessor, successors);
            }
        }

        graph.removeVertex(vertex);
        return true;
    }

    /**
     * Filters vertices from the given graph and subsequently removes them. If the vertex to be
     * removed has one or more predecessors, the predecessors will be connected directly to the
     * successors of the vertex to be removed.
     *
     * @param graph graph to be mutated
     * @param predicate a non-interfering stateless predicate to apply to each vertex to determine
     *        if it should be removed from the graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if at least one vertex has been removed; false otherwise.
     */
    public static <V,
        E> boolean removeVerticesAndPreserveConnectivity(Graph<V, E> graph, Predicate<V> predicate)
    {
        List<V> verticesToRemove = new ArrayList<>();

        for (V node : graph.vertexSet()) {
            if (predicate.test(node)) {
                verticesToRemove.add(node);
            }
        }

        return removeVertexAndPreserveConnectivity(graph, verticesToRemove);
    }

    /**
     * Removes all the given vertices from the given graph. If the vertex to be removed has one or
     * more predecessors, the predecessors will be connected directly to the successors of the
     * vertex to be removed.
     *
     * @param graph to be mutated
     * @param vertices vertices to be removed from this graph, if present
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if at least one vertex has been removed; false otherwise.
     */
    public static <V,
        E> boolean removeVertexAndPreserveConnectivity(Graph<V, E> graph, Iterable<V> vertices)
    {
        boolean atLeastOneVertexHasBeenRemoved = false;

        for (V vertex : vertices) {
            if (removeVertexAndPreserveConnectivity(graph, vertex)) {
                atLeastOneVertexHasBeenRemoved = true;
            }
        }

        return atLeastOneVertexHasBeenRemoved;
    }

    /**
     * Add edges from one source vertex to multiple target vertices. Whether duplicates are created
     * depends on the underlying {@link Graph} implementation.
     *
     * @param graph graph to be mutated
     * @param source source vertex of the new edges
     * @param targets target vertices for the new edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> void addOutgoingEdges(Graph<V, E> graph, V source, Iterable<V> targets)
    {
        if (!graph.containsVertex(source)) {
            graph.addVertex(source);
        }
        for (V target : targets) {
            if (!graph.containsVertex(target)) {
                graph.addVertex(target);
            }
            graph.addEdge(source, target);
        }
    }

    /**
     * Add edges from multiple source vertices to one target vertex. Whether duplicates are created
     * depends on the underlying {@link Graph} implementation.
     *
     * @param graph graph to be mutated
     * @param target target vertex for the new edges
     * @param sources source vertices for the new edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> void addIncomingEdges(Graph<V, E> graph, V target, Iterable<V> sources)
    {
        if (!graph.containsVertex(target)) {
            graph.addVertex(target);
        }
        for (V source : sources) {
            if (!graph.containsVertex(source)) {
                graph.addVertex(source);
            }
            graph.addEdge(source, target);
        }
    }

    /**
     * Check if a vertex has any direct successors.
     *
     * @param graph the graph to look for successors
     * @param vertex the vertex to look for successors
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if the vertex has any successors, false otherwise
     */
    public static <V, E> boolean vertexHasSuccessors(Graph<V, E> graph, V vertex)
    {
        return !graph.outgoingEdgesOf(vertex).isEmpty();
    }

    /**
     * Check if a vertex has any direct predecessors.
     *
     * @param graph the graph to look for predecessors
     * @param vertex the vertex to look for predecessors
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if the vertex has any predecessors, false otherwise
     */
    public static <V, E> boolean vertexHasPredecessors(Graph<V, E> graph, V vertex)
    {
        return !graph.incomingEdgesOf(vertex).isEmpty();
    }

    /**
     * Compute a new mapping from the vertices of a graph to the integer range $[0, n)$ where $n$ is
     * the number of vertices in the graph.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @throws NullPointerException if {@code graph} is {@code null}
     *
     * @return the mapping as an object containing the {@code vertexMap} and the {@code indexList}
     *
     * @see VertexToIntegerMapping
     */
    public static <V, E> VertexToIntegerMapping<V> getVertexToIntegerMapping(Graph<V, E> graph)
    {
        return new VertexToIntegerMapping<>(Objects.requireNonNull(graph).vertexSet());
    }
}
