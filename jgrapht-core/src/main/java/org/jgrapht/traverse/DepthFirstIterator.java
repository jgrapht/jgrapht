/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.traverse;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 * A depth-first iterator for directed and undirected graphs.
 * This is a non-recursive implementation with a worst-case space complexity of $O(|E|)$.
 * 
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class DepthFirstIterator<V, E>
    extends CrossComponentIterator<V, E, DepthFirstIterator.SearchNodeData>
{

    private Deque<StackEntry> stack = new ArrayDeque<>(graph.edgeSet().size());

    /**
     * Creates a new depth-first iterator for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public DepthFirstIterator(Graph<V, E> g)
    {
        this(g, (V) null);
    }

    /**
     * Creates a new depth-first iterator for the specified graph. Iteration will start at the
     * specified start vertex and will be limited to the connected component that includes that
     * vertex. If the specified start vertex is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public DepthFirstIterator(Graph<V, E> g, V startVertex)
    {
        super(g, startVertex);
    }


    /**
     * Creates a new depth-first iterator for the specified graph. Iteration will start at the
     * specified start vertices and will be limited to the connected component that includes those
     * vertices. If the specified start vertices is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertices the vertices iteration to be started.
     */
    public DepthFirstIterator(Graph<V, E> g, Iterable<V> startVertices)
    {
        super(g, startVertices);
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        while(!stack.isEmpty() && isSeenVertex(stack.peek().vertex))
            stack.pop();
        return stack.isEmpty();
    }

    @Override
    protected void encounterVertex(V vertex, E edge)
    {
        stack.push(new StackEntry(vertex, edge));
    }

    @Override
    protected void encounterVertexAgain(V vertex, E edge)
    {
        //Do nothing, not interesting
    }

    @Override
    protected V provideNextVertex()
    {
        StackEntry next=stack.pop();

        int depth= next.edge == null ? 0 : getSeenData(Graphs.getOppositeVertex(graph, next.edge, next.vertex)).depth+1;
        SearchNodeData snd=new SearchNodeData(next.edge, depth);

        putSeenData(next.vertex, snd);
        finishVertex(next.vertex);
        return next.vertex;
    }

    /**
     * Private data to associate with each entry in the stack.
     */
    private class StackEntry
    {
        /**
         * The vertex reached.
         */
        final V vertex;

        /**
         * Edge through which the vertex was reached
         */
        final E edge;

        private StackEntry(V vertex, E edge) {
            this.vertex = vertex;
            this.edge = edge;
        }
    }

    class SearchNodeData{
        /**
         * Edge to parent
         */
        final E edge;
        /**
         * Depth of node in search tree
         */
        final int depth;

        SearchNodeData(E edge, int depth) {
            this.edge = edge;
            this.depth = depth;
        }
    }

    /**
     * Returns the parent node of vertex v in the DFS search tree, or null if v is the root node.
     * This method can only be invoked on a vertex v once the iterator has visited vertex v!
     * @param v vertex
     * @return parent node of vertex v in the DFS search tree, or null if v is the root node
     */
    public V getParent(V v){
        assert getSeenData(v) != null;
        E edge=(E) getSeenData(v).edge;
        if(edge == null)
            return null;
        else
            return Graphs.getOppositeVertex(graph, edge, v);
    }

    /**
     * Returns the edge connecting vertex v to its parent in the DFS search tree, or null if v is the root node.
     * This method can only be invoked on a vertex v once the iterator has visited vertex v!
     * @param v vertex
     * @return parent node of vertex v in the DFS search tree, or null if v is the root node
     */
    public E getEdgeToParent(V v){
        assert getSeenData(v) != null;
        return (E)getSeenData(v).edge;
    }

    /**
     * Returns the depth of vertex v in the search tree. The depth of a vertex v is defined as the number of edges
     * traversed on the path from the root of the DFS tree to vertex v. The root of the search tree has depth 0.
     * This method can only be invoked on a vertex v once the iterator has visited vertex v!
     * @param v vertex
     * @return depth of vertex v in the search tree
     */
    public int getDepth(V v){
        assert getSeenData(v) != null;
        return getSeenData(v).depth;
    }
}

// End DepthFirstIterator.java
