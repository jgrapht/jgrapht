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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.event.*;

import java.util.*;

/**
 * Provides a cross-connected-component traversal functionality for iterator subclasses.
 *
 * @param <V> vertex type
 * @param <E> edge type
 * @param <D> type of data associated to seen vertices
 *
 * @author Barak Naveh
 */
public abstract class CrossComponentIterator<V, E, D>
    extends
    AbstractGraphIterator<V, E>
{
    private static final int CCS_BEFORE_COMPONENT = 1;
    private static final int CCS_WITHIN_COMPONENT = 2;
    private static final int CCS_AFTER_COMPONENT = 3;

    private final ConnectedComponentTraversalEvent ccFinishedEvent =
        new ConnectedComponentTraversalEvent(
            this, ConnectedComponentTraversalEvent.CONNECTED_COMPONENT_FINISHED);
    private final ConnectedComponentTraversalEvent ccStartedEvent =
        new ConnectedComponentTraversalEvent(
            this, ConnectedComponentTraversalEvent.CONNECTED_COMPONENT_STARTED);

    /**
     * Stores the vertices that have been seen during iteration and (optionally) some additional
     * traversal info regarding each vertex.
     */
    private Map<V, D> seen = new HashMap<>();

    /**
     * Iterator which provides start vertices for cross-component iteration.
     */
    private Iterator<V> entireGraphVertexIterator = null;

    /**
     * Iterator which provides start vertices for specified start vertices.
     */
    private Iterator<V> startVertexIterator = null;

    /**
     * The current vertex.
     */
    private V startVertex;

    /**
     * The connected component state
     */
    private int state = CCS_BEFORE_COMPONENT;

    /**
     * Creates a new iterator for the specified graph.
     *
     * @param g the graph to be iterated
     */
    public CrossComponentIterator(Graph<V, E> g)
    {
        this(g, (V) null);
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     *
     * @throws IllegalArgumentException if <code>g==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public CrossComponentIterator(Graph<V, E> g, V startVertex)
    {
        this(g, startVertex == null ? null : Collections.singletonList(startVertex));
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertices. If the specified start vertices is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     *
     * @param g the graph to be iterated.
     * @param startVertices the vertices iteration to be started.
     *
     * @throws IllegalArgumentException if <code>g==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public CrossComponentIterator(Graph<V, E> g, Iterable<V> startVertices)
    {
        super(g);

        /*
         * Initialize crossComponentTraversal and test for containment
         */
        this.entireGraphVertexIterator = graph.vertexSet().iterator();
        if (startVertices == null) {
            this.crossComponentTraversal = true;
        } else {
            this.crossComponentTraversal = false;
            this.startVertexIterator = startVertices.iterator();
        }

        /*
         * Initialize start vertex
         */
        Iterator<V> it = crossComponentTraversal ? entireGraphVertexIterator : startVertexIterator;
        // pick a start vertex if possible
        if (it.hasNext()) {
            this.startVertex = it.next();
            if (!graph.containsVertex(startVertex)) {
                throw new IllegalArgumentException("graph must contain the start vertex");
            }
        } else {
            this.startVertex = null;
        }

    }

    @Override
    public boolean hasNext()
    {
        if (startVertex != null) {
            encounterStartVertex();
        }

        if (isConnectedComponentExhausted()) {
            if (state == CCS_WITHIN_COMPONENT) {
                state = CCS_AFTER_COMPONENT;
                if (nListeners != 0) {
                    fireConnectedComponentFinished(ccFinishedEvent);
                }
            }

            Iterator<V> it =
                isCrossComponentTraversal() ? entireGraphVertexIterator : startVertexIterator;
            while (it != null && it.hasNext()) {
                V v = it.next();
                if (!graph.containsVertex(v)) {
                    throw new IllegalArgumentException("graph must contain the start vertex");
                }
                if (!isSeenVertex(v)) {
                    encounterVertex(v, null);
                    state = CCS_BEFORE_COMPONENT;

                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public V next()
    {
        if (startVertex != null) {
            encounterStartVertex();
        }

        if (hasNext()) {
            if (state == CCS_BEFORE_COMPONENT) {
                state = CCS_WITHIN_COMPONENT;
                if (nListeners != 0) {
                    fireConnectedComponentStarted(ccStartedEvent);
                }
            }

            V nextVertex = provideNextVertex();
            if (nListeners != 0) {
                fireVertexTraversed(createVertexTraversalEvent(nextVertex));
            }

            addUnseenChildrenOf(nextVertex);

            return nextVertex;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns <tt>true</tt> if there are no more uniterated vertices in the currently iterated
     * connected component; <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if there are no more uniterated vertices in the currently iterated
     *         connected component; <tt>false</tt> otherwise.
     */
    protected abstract boolean isConnectedComponentExhausted();

    /**
     * Update data structures the first time we see a vertex.
     *
     * @param vertex the vertex encountered
     * @param edge the edge via which the vertex was encountered, or null if the vertex is a
     *        starting point
     */
    protected abstract void encounterVertex(V vertex, E edge);

    /**
     * Returns the vertex to be returned in the following call to the iterator <code>next</code>
     * method.
     *
     * @return the next vertex to be returned by this iterator.
     */
    protected abstract V provideNextVertex();

    /**
     * Access the data stored for a seen vertex.
     *
     * @param vertex a vertex which has already been seen.
     *
     * @return data associated with the seen vertex or <code>null</code> if no data was associated
     *         with the vertex. A <code>null</code> return can also indicate that the vertex was
     *         explicitly associated with <code>
     * null</code>.
     */
    protected D getSeenData(V vertex)
    {
        return seen.get(vertex);
    }

    /**
     * Determines whether a vertex has been seen yet by this traversal.
     *
     * @param vertex vertex in question
     *
     * @return <tt>true</tt> if vertex has already been seen
     */
    protected boolean isSeenVertex(V vertex)
    {
        return seen.containsKey(vertex);
    }

    /**
     * Called whenever we re-encounter a vertex. The default implementation does nothing.
     *
     * @param vertex the vertex re-encountered
     * @param edge the edge via which the vertex was re-encountered
     */
    protected abstract void encounterVertexAgain(V vertex, E edge);

    /**
     * Stores iterator-dependent data for a vertex that has been seen.
     *
     * @param vertex a vertex which has been seen.
     * @param data data to be associated with the seen vertex.
     *
     * @return previous value associated with specified vertex or <code>
     * null</code> if no data was associated with the vertex. A <code>
     * null</code> return can also indicate that the vertex was explicitly associated with
     *         <code>null</code>.
     */
    protected D putSeenData(V vertex, D data)
    {
        return seen.put(vertex, data);
    }

    /**
     * Called when a vertex has been finished (meaning is dependent on traversal represented by
     * subclass).
     *
     * @param vertex vertex which has been finished
     */
    protected void finishVertex(V vertex)
    {
        if (nListeners != 0) {
            fireVertexFinished(createVertexTraversalEvent(vertex));
        }
    }

    private void addUnseenChildrenOf(V vertex)
    {
        for (E edge : graph.outgoingEdgesOf(vertex)) {
            if (nListeners != 0) {
                fireEdgeTraversed(createEdgeTraversalEvent(edge));
            }

            V oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);

            if (isSeenVertex(oppositeV)) {
                encounterVertexAgain(oppositeV, edge);
            } else {
                encounterVertex(oppositeV, edge);
            }
        }
    }

    private void encounterStartVertex()
    {
        encounterVertex(startVertex, null);
        startVertex = null;
    }

}
