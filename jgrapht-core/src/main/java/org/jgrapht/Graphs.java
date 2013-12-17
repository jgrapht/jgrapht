/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ----------------
 * Graphs.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *                   Mikael Hansen
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Jul-2003 : Initial revision (BN);
 * 06-Nov-2003 : Change edge sharing semantics (JVS);
 * 11-Mar-2004 : Made generic (CH);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht;

import java.util.*;

import org.jgrapht.graph.*;


/**
 * A collection of utilities to assist with graph manipulation.
 *
 * @author Barak Naveh
 * @since Jul 31, 2003
 */
public abstract class Graphs
{
    

    /**
     * Creates a new edge and adds it to the specified graph similarly to the
     * {@link Graph#addEdge(Object, Object)} method.
     *
     * @param g the graph for which the edge to be added.
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     * @param weight weight of the edge.
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     *
     * @see Graph#addEdge(Object, Object)
     */
    public static <V, E> E addEdge(
        Graph<V, E> g,
        V sourceVertex,
        V targetVertex,
        double weight)
    {
        EdgeFactory<V, E> ef = g.getEdgeFactory();
        E e = ef.createEdge(sourceVertex, targetVertex);

        // we first create the edge and set the weight to make sure that
        // listeners will see the correct weight upon addEdge.

        assert (g instanceof WeightedGraph<?, ?>) : g.getClass();
        ((WeightedGraph<V, E>) g).setEdgeWeight(e, weight);

        return g.addEdge(sourceVertex, targetVertex, e) ? e : null;
    }

    /**
     * Adds the specified source and target vertices to the graph, if not
     * already included, and creates a new edge and adds it to the specified
     * graph similarly to the {@link Graph#addEdge(Object, Object)} method.
     *
     * @param g the graph for which the specified edge to be added.
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     */
    public static <V, E> E addEdgeWithVertices(
        Graph<V, E> g,
        V sourceVertex,
        V targetVertex)
    {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return g.addEdge(sourceVertex, targetVertex);
    }

    /**
     * Adds the specified edge to the graph, including its vertices if not
     * already included.
     *
     * @param targetGraph the graph for which the specified edge to be added.
     * @param sourceGraph the graph in which the specified edge is already
     * present
     * @param edge edge to add
     *
     * @return <tt>true</tt> if the target graph did not already contain the
     * specified edge.
     */
    public static <V, E> boolean addEdgeWithVertices(
        Graph<V, E> targetGraph,
        Graph<V, E> sourceGraph,
        E edge)
    {
        V sourceVertex = sourceGraph.getEdgeSource(edge);
        V targetVertex = sourceGraph.getEdgeTarget(edge);

        targetGraph.addVertex(sourceVertex);
        targetGraph.addVertex(targetVertex);

        return targetGraph.addEdge(sourceVertex, targetVertex, edge);
    }

    /**
     * Adds the specified source and target vertices to the graph, if not
     * already included, and creates a new weighted edge and adds it to the
     * specified graph similarly to the {@link Graph#addEdge(Object, Object)}
     * method.
     *
     * @param g the graph for which the specified edge to be added.
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     * @param weight weight of the edge.
     *
     * @return The newly created edge if added to the graph, otherwise <code>
     * null</code>.
     */
    public static <V, E> E addEdgeWithVertices(
        Graph<V, E> g,
        V sourceVertex,
        V targetVertex,
        double weight)
    {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return addEdge(g, sourceVertex, targetVertex, weight);
    }

    /**
     * Adds all the vertices and all the edges of the specified source graph to
     * the specified destination graph. First all vertices of the source graph
     * are added to the destination graph. Then every edge of the source graph
     * is added to the destination graph. This method returns <code>true</code>
     * if the destination graph has been modified as a result of this operation,
     * otherwise it returns <code>false</code>.
     *
     * <p>The behavior of this operation is undefined if any of the specified
     * graphs is modified while operation is in progress.</p>
     *
     * @param destination the graph to which vertices and edges are added.
     * @param source the graph used as source for vertices and edges to add.
     *
     * @return <code>true</code> if and only if the destination graph has been
     * changed as a result of this operation.
     */
    public static <V, E> boolean addGraph(
        Graph<? super V, ? super E> destination,
        Graph<V, E> source)
    {
        boolean modified = addAllVertices(destination, source.vertexSet());
        modified |= addAllEdges(destination, source, source.edgeSet());

        return modified;
    }

    /**
     * Adds all the vertices and all the edges of the specified source digraph
     * to the specified destination digraph, reversing all of the edges. If you
     * want to do this as a linked view of the source graph (rather than by
     * copying to a destination graph), use {@link EdgeReversedGraph} instead.
     *
     * <p>The behavior of this operation is undefined if any of the specified
     * graphs is modified while operation is in progress.</p>
     *
     * @param destination the graph to which vertices and edges are added.
     * @param source the graph used as source for vertices and edges to add.
     *
     * @see EdgeReversedGraph
     */
    public static <V, E> void addGraphReversed(
        DirectedGraph<? super V, ? super E> destination,
        DirectedGraph<V, E> source)
    {
        addAllVertices(destination, source.vertexSet());

        for (E edge : source.edgeSet()) {
            destination.addEdge(
                source.getEdgeTarget(edge),
                source.getEdgeSource(edge));
        }
    }

    /**
     * Adds a subset of the edges of the specified source graph to the specified
     * destination graph. The behavior of this operation is undefined if either
     * of the graphs is modified while the operation is in progress. {@link
     * #addEdgeWithVertices} is used for the transfer, so source vertexes will
     * be added automatically to the target graph.
     *
     * @param destination the graph to which edges are to be added
     * @param source the graph used as a source for edges to add
     * @param edges the edges to be added
     *
     * @return <tt>true</tt> if this graph changed as a result of the call
     */
    public static <V, E> boolean addAllEdges(
        Graph<? super V, ? super E> destination,
        Graph<V, E> source,
        Collection<? extends E> edges)
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
     * Adds all of the specified vertices to the destination graph. The behavior
     * of this operation is undefined if the specified vertex collection is
     * modified while the operation is in progress. This method will invoke the
     * {@link Graph#addVertex(Object)} method.
     *
     * @param destination the graph to which edges are to be added
     * @param vertices the vertices to be added to the graph.
     *
     * @return <tt>true</tt> if graph changed as a result of the call
     *
     * @throws NullPointerException if the specified vertices contains one or
     * more null vertices, or if the specified vertex collection is <tt>
     * null</tt>.
     *
     * @see Graph#addVertex(Object)
     */
    public static <V, E> boolean addAllVertices(
        Graph<? super V, ? super E> destination,
        Collection<? extends V> vertices)
    {
        boolean modified = false;

        for (V v : vertices) {
            modified |= destination.addVertex(v);
        }

        return modified;
    }

    /**
     * Returns a list of vertices that are the neighbors of a specified vertex.
     * If the graph is a multigraph vertices may appear more than once in the
     * returned list.
     *
     * @param g the graph to look for neighbors in.
     * @param vertex the vertex to get the neighbors of.
     *
     * @return a list of the vertices that are the neighbors of the specified
     * vertex.
     */
    public static <V, E> List<V> neighborListOf(Graph<V, E> g,
        V vertex)
    {
        List<V> neighbors = new ArrayList<V>();

        for (E e : g.edgesOf(vertex)) {
            neighbors.add(getOppositeVertex(g, e, vertex));
        }

        return neighbors;
    }

    /**
     * Returns a list of vertices that are the direct predecessors of a
     * specified vertex. If the graph is a multigraph, vertices may appear more
     * than once in the returned list.
     *
     * @param g the graph to look for predecessors in.
     * @param vertex the vertex to get the predecessors of.
     *
     * @return a list of the vertices that are the direct predecessors of the
     * specified vertex.
     */
    public static <V, E> List<V> predecessorListOf(
        DirectedGraph<V, E> g,
        V vertex)
    {
        List<V> predecessors = new ArrayList<V>();
        Set<? extends E> edges = g.incomingEdgesOf(vertex);

        for (E e : edges) {
            predecessors.add(getOppositeVertex(g, e, vertex));
        }

        return predecessors;
    }

    /**
     * Returns a list of vertices that are the direct successors of a specified
     * vertex. If the graph is a multigraph vertices may appear more than once
     * in the returned list.
     *
     * @param g the graph to look for successors in.
     * @param vertex the vertex to get the successors of.
     *
     * @return a list of the vertices that are the direct successors of the
     * specified vertex.
     */
    public static <V, E> List<V> successorListOf(
        DirectedGraph<V, E> g,
        V vertex)
    {
        List<V> successors = new ArrayList<V>();
        Set<? extends E> edges = g.outgoingEdgesOf(vertex);

        for (E e : edges) {
            successors.add(getOppositeVertex(g, e, vertex));
        }

        return successors;
    }

    /**
     * Returns an undirected view of the specified graph. If the specified graph
     * is directed, returns an undirected view of it. If the specified graph is
     * already undirected, just returns it.
     *
     * @param g the graph for which an undirected view is to be returned.
     *
     * @return an undirected view of the specified graph, if it is directed, or
     * or the specified graph itself if it is already undirected.
     *
     * @throws IllegalArgumentException if the graph is neither DirectedGraph
     * nor UndirectedGraph.
     *
     * @see AsUndirectedGraph
     */
    public static <V, E> UndirectedGraph<V, E> undirectedGraph(Graph<V, E> g)
    {
        if (g instanceof DirectedGraph<?, ?>) {
            return new AsUndirectedGraph<V, E>((DirectedGraph<V, E>) g);
        } else if (g instanceof UndirectedGraph<?, ?>) {
            return (UndirectedGraph<V, E>) g;
        } else {
            throw new IllegalArgumentException(
                "Graph must be either DirectedGraph or UndirectedGraph");
        }
    }

    /**
     * Tests whether an edge is incident to a vertex.
     *
     * @param g graph containing e and v
     * @param e edge in g
     * @param v vertex in g
     *
     * @return true iff e is incident on v
     */
    public static <V, E> boolean testIncidence(Graph<V, E> g, E e, V v)
    {
        return (g.getEdgeSource(e).equals(v))
            || (g.getEdgeTarget(e).equals(v));
    }

    /**
     * Gets the vertex opposite another vertex across an edge.
     *
     * @param g graph containing e and v
     * @param e edge in g
     * @param v vertex in g
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
            throw new IllegalArgumentException("no such vertex");
        }
    }

    /**
     * Gets the list of vertices visited by a path.
     *
     * @param path path of interest
     *
     * @return corresponding vertex list
     */
    public static <V, E> List<V> getPathVertexList(GraphPath<V, E> path)
    {
        Graph<V, E> g = path.getGraph();
        List<V> list = new ArrayList<V>();
        V v = path.getStartVertex();
        list.add(v);
        for (E e : path.getEdgeList()) {
            v = getOppositeVertex(g, e, v);
            list.add(v);
        }
        return list;
    }
}

// End Graphs.java
