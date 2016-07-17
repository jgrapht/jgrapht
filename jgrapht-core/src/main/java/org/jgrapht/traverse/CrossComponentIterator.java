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
/* ---------------------------
 * CrossComponentIterator.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   John V. Sichi
 *                   Christian Hammer
 *                   Assaf Mizrachi
 *
 * $Id$
 *
 * Changes
 * -------
 * 31-Jul-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 31-Jan-2004 : Extracted cross-component traversal functionality (BN);
 * 04-May-2004 : Made generic (CH)
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 15-Jul-2016 : Moved Specifics and Flyweight events from AbstractGraphIterator.
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;


/**
 * Provides a cross-connected-component traversal functionality for iterator
 * subclasses.
 *
 * @param <V> vertex type
 * @param <E> edge type
 * @param <D> type of data associated to seen vertices
 *
 * @author Barak Naveh
 * @since Jan 31, 2004
 */
public abstract class CrossComponentIterator<V, E, D>
    extends AbstractGraphIterator<V, E>
{
    private static final int CCS_BEFORE_COMPONENT = 1;
    private static final int CCS_WITHIN_COMPONENT = 2;
    private static final int CCS_AFTER_COMPONENT = 3;

    /**
     * Standard vertex visit state enumeration.
     */
    protected static enum VisitColor
    {
        /**
         * Vertex has not been returned via iterator yet.
         */
        WHITE,

        /**
         * Vertex has been returned via iterator, but we're not done with all of
         * its out-edges yet.
         */
        GRAY,

        /**
         * Vertex has been returned via iterator, and we're done with all of its
         * out-edges.
         */
        BLACK
    }

    //
    private final ConnectedComponentTraversalEvent ccFinishedEvent =
        new ConnectedComponentTraversalEvent(
            this,
            ConnectedComponentTraversalEvent.CONNECTED_COMPONENT_FINISHED);
    private final ConnectedComponentTraversalEvent ccStartedEvent =
        new ConnectedComponentTraversalEvent(
            this,
            ConnectedComponentTraversalEvent.CONNECTED_COMPONENT_STARTED);

    private Iterator<V> vertexIterator = null;

    /**
     * Stores the vertices that have been seen during iteration and (optionally)
     * some additional traversal info regarding each vertex.
     */
    private Map<V, D> seen = new HashMap<>();
    private V startVertex;

    private final Graph<V, E> graph;

    /**
     * The connected component state
     */
    private int state = CCS_BEFORE_COMPONENT;

    /**
     * Creates a new iterator for the specified graph. Iteration will start at
     * the specified start vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     *
     * @throws IllegalArgumentException if <code>g==null</code> or does not
     * contain <code>startVertex</code>
     */
    public CrossComponentIterator(Graph<V, E> g, V startVertex)
    {
        super();

        if (g == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        graph = g;

        specifics = createGraphSpecifics(g);
        vertexIterator = g.vertexSet().iterator();
        setCrossComponentTraversal(startVertex == null);

        reusableEdgeEvent = new FlyweightEdgeEvent<>(this, null);
        reusableVertexEvent = new FlyweightVertexEvent<>(this, null);

        if (startVertex == null) {
            // pick a start vertex if graph not empty
            if (vertexIterator.hasNext()) {
                this.startVertex = vertexIterator.next();
            } else {
                this.startVertex = null;
            }
        } else if (g.containsVertex(startVertex)) {
            this.startVertex = startVertex;
        } else {
            throw new IllegalArgumentException(
                "graph must contain the start vertex");
        }
    }

    /**
     * @return the graph being traversed
     */
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override public boolean hasNext()
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

            if (isCrossComponentTraversal()) {
                while (vertexIterator.hasNext()) {
                    V v = vertexIterator.next();

                    if (!isSeenVertex(v)) {
                        encounterVertex(v, null);
                        state = CCS_BEFORE_COMPONENT;

                        return true;
                    }
                }

                return false;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override public V next()
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
     * Returns <tt>true</tt> if there are no more uniterated vertices in the
     * currently iterated connected component; <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if there are no more uniterated vertices in the
     * currently iterated connected component; <tt>false</tt> otherwise.
     */
    protected abstract boolean isConnectedComponentExhausted();

    /**
     * Update data structures the first time we see a vertex.
     *
     * @param vertex the vertex encountered
     * @param edge the edge via which the vertex was encountered, or null if the
     * vertex is a starting point
     */
    protected abstract void encounterVertex(V vertex, E edge);

    /**
     * Returns the vertex to be returned in the following call to the iterator
     * <code>next</code> method.
     *
     * @return the next vertex to be returned by this iterator.
     */
    protected abstract V provideNextVertex();

    /**
     * Access the data stored for a seen vertex.
     *
     * @param vertex a vertex which has already been seen.
     *
     * @return data associated with the seen vertex or <code>null</code> if no
     * data was associated with the vertex. A <code>null</code> return can also
     * indicate that the vertex was explicitly associated with <code>
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
     * Called whenever we re-encounter a vertex. The default implementation does
     * nothing.
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
     * null</code> return can also indicate that the vertex was explicitly
     * associated with <code>null</code>.
     */
    protected D putSeenData(V vertex, D data)
    {
        return seen.put(vertex, data);
    }

    /**
     * Called when a vertex has been finished (meaning is dependent on traversal
     * represented by subclass).
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
        for (E edge : specifics.edgesOf(vertex)) {
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

    private EdgeTraversalEvent<E> createEdgeTraversalEvent(E edge)
    {
        if (isReuseEvents()) {
            reusableEdgeEvent.setEdge(edge);

            return reusableEdgeEvent;
        } else {
            return new EdgeTraversalEvent<>(this, edge);
        }
    }

    private VertexTraversalEvent<V> createVertexTraversalEvent(V vertex)
    {
        if (isReuseEvents()) {
            reusableVertexEvent.setVertex(vertex);

            return reusableVertexEvent;
        } else {
            return new VertexTraversalEvent<>(this, vertex);
        }
    }

    private void encounterStartVertex()
    {
        encounterVertex(startVertex, null);
        startVertex = null;
    }

    static interface SimpleContainer<T>
    {
        /**
         * Tests if this container is empty.
         *
         * @return <code>true</code> if empty, otherwise <code>false</code>.
         */
        public boolean isEmpty();

        /**
         * Adds the specified object to this container.
         *
         * @param o the object to be added.
         */
        public void add(T o);

        /**
         * Remove an object from this container and return it.
         *
         * @return the object removed from this container.
         */
        public T remove();
    }
    
}

// End CrossComponentIterator.java
