/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;

import java.util.*;

/**
 * A breadth-first iterator for a directed or undirected graph.
 *
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class BreadthFirstIterator<V, E>
    extends
    CrossComponentIterator<V, E, BreadthFirstIterator.SearchNodeData<E>>
{
    private Deque<V> queue = new ArrayDeque<>();

    /**
     * Creates a new breadth-first iterator for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public BreadthFirstIterator(Graph<V, E> g)
    {
        this(g, (V) null);
    }

    /**
     * Creates a new breadth-first iterator for the specified graph. Iteration will start at the
     * specified start vertex and will be limited to the connected component that includes that
     * vertex. If the specified start vertex is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public BreadthFirstIterator(Graph<V, E> g, V startVertex)
    {
        super(g, startVertex);
    }

    /**
     * Creates a new breadth-first iterator for the specified graph. Iteration will start at the
     * specified start vertices and will be limited to the connected component that includes those
     * vertices. If the specified start vertices is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertices the vertices iteration to be started.
     */
    public BreadthFirstIterator(Graph<V, E> g, Iterable<V> startVertices)
    {
        super(g, startVertices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isConnectedComponentExhausted()
    {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encounterVertex(V vertex, E edge)
    {
        int depth = (edge == null ? 0
            : getSeenData(Graphs.getOppositeVertex(graph, edge, vertex)).depth + 1);
        putSeenData(vertex, new SearchNodeData<>(edge, depth));
        queue.add(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encounterVertexAgain(V vertex, E edge)
    {
    }

    /**
     * Returns the parent node of vertex $v$ in the BFS search tree, or null if $v$ is the root
     * node. This method can only be invoked on a vertex $v$ once the iterator has visited vertex
     * $v$!
     *
     * @param v vertex
     * @return parent node of vertex $v$ in the BFS search tree, or null if $v$ is a root node
     */
    public V getParent(V v)
    {
        assert getSeenData(v) != null;
        E edge = getSeenData(v).edge;
        if (edge == null)
            return null;
        else
            return Graphs.getOppositeVertex(graph, edge, v);
    }

    /**
     * Returns the edge connecting vertex $v$ to its parent in the spanning tree formed by the BFS
     * search, or null if $v$ is a root node. This method can only be invoked on a vertex $v$ once
     * the iterator has visited vertex $v$!
     *
     * @param v vertex
     * @return edge connecting vertex $v$ in the BFS search tree to its parent, or null if $v$ is a
     *         root node
     */
    public E getSpanningTreeEdge(V v)
    {
        assert getSeenData(v) != null;
        return getSeenData(v).edge;
    }

    /**
     * Returns the depth of vertex $v$ in the search tree. The depth of a vertex $v$ is defined as
     * the number of edges traversed on the path from the root of the BFS tree to vertex $v$. The
     * root of the search tree has depth 0. This method can only be invoked on a vertex $v$ once the
     * iterator has visited vertex $v$!
     *
     * @param v vertex
     * @return depth of vertex $v$ in the search tree
     */
    public int getDepth(V v)
    {
        assert getSeenData(v) != null;
        return getSeenData(v).depth;
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    @Override
    protected V provideNextVertex()
    {
        return queue.removeFirst();
    }

    /**
     * Data kept for discovered vertices.
     *
     * @param <E> the graph edge type
     */
    protected static class SearchNodeData<E>
    {
        private final E edge;
        private final int depth;

        /**
         * Constructor
         * 
         * @param edge edge to parent
         * @param depth depth of node in search tree
         */
        public SearchNodeData(E edge, int depth)
        {
            this.edge = edge;
            this.depth = depth;
        }

        /**
         * Edge to parent
         * 
         * @return the edge to the parent
         */
        public E getEdge()
        {
            return edge;
        }

        /**
         * Depth of node in search tree
         * 
         * @return the depth of the node in the search tree
         */
        public int getDepth()
        {
            return depth;
        }
    }
}
