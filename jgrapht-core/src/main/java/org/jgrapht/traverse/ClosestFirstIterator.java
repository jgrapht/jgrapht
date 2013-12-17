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
/* -------------------------
 * ClosestFirstIterator.java
 * -------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Barak Naveh
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Sep-2003 : Initial revision (JVS);
 * 31-Jan-2004 : Reparented and changed interface to parent class (BN);
 * 29-May-2005 : Added radius support (JVS);
 * 06-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * A closest-first iterator for a directed or undirected graph. For this
 * iterator to work correctly the graph must not be modified during iteration.
 * Currently there are no means to ensure that, nor to fail-fast. The results of
 * such modifications are undefined.
 *
 * <p>The metric for <i>closest</i> here is the path length from a start vertex.
 * Graph.getEdgeWeight(Edge) is summed to calculate path length. Negative edge
 * weights will result in an IllegalArgumentException. Optionally, path length
 * may be bounded by a finite radius.</p>
 *
 * @author John V. Sichi
 * @since Sep 2, 2003
 */
public class ClosestFirstIterator<V, E>
    extends CrossComponentIterator<V,
        E, FibonacciHeapNode<ClosestFirstIterator.QueueEntry<V, E>>>
{
    

    /**
     * Priority queue of fringe vertices.
     */
    private FibonacciHeap<QueueEntry<V, E>> heap =
        new FibonacciHeap<QueueEntry<V, E>>();

    /**
     * Maximum distance to search.
     */
    private double radius = Double.POSITIVE_INFINITY;

    private boolean initialized = false;

    

    /**
     * Creates a new closest-first iterator for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public ClosestFirstIterator(Graph<V, E> g)
    {
        this(g, null);
    }

    /**
     * Creates a new closest-first iterator for the specified graph. Iteration
     * will start at the specified start vertex and will be limited to the
     * connected component that includes that vertex. If the specified start
     * vertex is <code>null</code>, iteration will start at an arbitrary vertex
     * and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public ClosestFirstIterator(Graph<V, E> g, V startVertex)
    {
        this(g, startVertex, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates a new radius-bounded closest-first iterator for the specified
     * graph. Iteration will start at the specified start vertex and will be
     * limited to the subset of the connected component which includes that
     * vertex and is reachable via paths of length less than or equal to the
     * specified radius. The specified start vertex may not be <code>
     * null</code>.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param radius limit on path length, or Double.POSITIVE_INFINITY for
     * unbounded search.
     */
    public ClosestFirstIterator(Graph<V, E> g, V startVertex, double radius)
    {
        super(g, startVertex);
        this.radius = radius;
        checkRadiusTraversal(isCrossComponentTraversal());
        initialized = true;
    }

    

    // override AbstractGraphIterator
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        if (initialized) {
            checkRadiusTraversal(crossComponentTraversal);
        }
        super.setCrossComponentTraversal(crossComponentTraversal);
    }

    /**
     * Get the length of the shortest path known to the given vertex. If the
     * vertex has already been visited, then it is truly the shortest path
     * length; otherwise, it is the best known upper bound.
     *
     * @param vertex vertex being sought from start vertex
     *
     * @return length of shortest path known, or Double.POSITIVE_INFINITY if no
     * path found yet
     */
    public double getShortestPathLength(V vertex)
    {
        FibonacciHeapNode<QueueEntry<V, E>> node = getSeenData(vertex);

        if (node == null) {
            return Double.POSITIVE_INFINITY;
        }

        return node.getKey();
    }

    /**
     * Get the spanning tree edge reaching a vertex which has been seen already
     * in this traversal. This edge is the last link in the shortest known path
     * between the start vertex and the requested vertex. If the vertex has
     * already been visited, then it is truly the minimum spanning tree edge;
     * otherwise, it is the best candidate seen so far.
     *
     * @param vertex the spanned vertex.
     *
     * @return the spanning tree edge, or null if the vertex either has not been
     * seen yet or is the start vertex.
     */
    public E getSpanningTreeEdge(V vertex)
    {
        FibonacciHeapNode<QueueEntry<V, E>> node = getSeenData(vertex);

        if (node == null) {
            return null;
        }

        return node.getData().spanningTreeEdge;
    }

    /**
     * @see CrossComponentIterator#isConnectedComponentExhausted()
     */
    protected boolean isConnectedComponentExhausted()
    {
        if (heap.size() == 0) {
            return true;
        } else {
            if (heap.min().getKey() > radius) {
                heap.clear();

                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @see CrossComponentIterator#encounterVertex(Object, Object)
     */
    protected void encounterVertex(V vertex, E edge)
    {
        double shortestPathLength;
        if (edge == null) {
            shortestPathLength = 0;
        } else {
            shortestPathLength = calculatePathLength(vertex, edge);
        }
        FibonacciHeapNode<QueueEntry<V, E>> node = createSeenData(vertex, edge);
        putSeenData(vertex, node);
        heap.insert(node, shortestPathLength);
    }

    /**
     * Override superclass. When we see a vertex again, we need to see if the
     * new edge provides a shorter path than the old edge.
     *
     * @param vertex the vertex re-encountered
     * @param edge the edge via which the vertex was re-encountered
     */
    protected void encounterVertexAgain(V vertex, E edge)
    {
        FibonacciHeapNode<QueueEntry<V, E>> node = getSeenData(vertex);

        if (node.getData().frozen) {
            // no improvement for this vertex possible
            return;
        }

        double candidatePathLength = calculatePathLength(vertex, edge);

        if (candidatePathLength < node.getKey()) {
            node.getData().spanningTreeEdge = edge;
            heap.decreaseKey(node, candidatePathLength);
        }
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    protected V provideNextVertex()
    {
        FibonacciHeapNode<QueueEntry<V, E>> node = heap.removeMin();
        node.getData().frozen = true;

        return node.getData().vertex;
    }

    private void assertNonNegativeEdge(E edge)
    {
        if (getGraph().getEdgeWeight(edge) < 0) {
            throw new IllegalArgumentException(
                "negative edge weights not allowed");
        }
    }

    /**
     * Determine path length to a vertex via an edge, using the path length for
     * the opposite vertex.
     *
     * @param vertex the vertex for which to calculate the path length.
     * @param edge the edge via which the path is being extended.
     *
     * @return calculated path length.
     */
    private double calculatePathLength(V vertex, E edge)
    {
        assertNonNegativeEdge(edge);

        V otherVertex = Graphs.getOppositeVertex(getGraph(), edge, vertex);
        FibonacciHeapNode<QueueEntry<V, E>> otherEntry =
            getSeenData(otherVertex);

        return otherEntry.getKey()
            + getGraph().getEdgeWeight(edge);
    }

    private void checkRadiusTraversal(boolean crossComponentTraversal)
    {
        if (crossComponentTraversal && (radius != Double.POSITIVE_INFINITY)) {
            throw new IllegalArgumentException(
                "radius may not be specified for cross-component traversal");
        }
    }

    /**
     * The first time we see a vertex, make up a new heap node for it.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     *
     * @return the new heap node.
     */
    private FibonacciHeapNode<QueueEntry<V, E>> createSeenData(
        V vertex,
        E edge)
    {
        QueueEntry<V, E> entry = new QueueEntry<V, E>();
        entry.vertex = vertex;
        entry.spanningTreeEdge = edge;

        return new FibonacciHeapNode<QueueEntry<V, E>>(entry);
    }

    

    /**
     * Private data to associate with each entry in the priority queue.
     */
    static class QueueEntry<V, E>
    {
        /**
         * Best spanning tree edge to vertex seen so far.
         */
        E spanningTreeEdge;

        /**
         * The vertex reached.
         */
        V vertex;

        /**
         * True once spanningTreeEdge is guaranteed to be the true minimum.
         */
        boolean frozen;

        QueueEntry()
        {
        }
    }
}

// End ClosestFirstIterator.java
