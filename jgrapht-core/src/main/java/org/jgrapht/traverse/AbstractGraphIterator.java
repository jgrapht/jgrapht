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
 * An empty implementation of a graph iterator to minimize the effort required to implement graph
 * iterators.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public abstract class AbstractGraphIterator<V, E>
    implements
    GraphIterator<V, E>
{
    private final Set<TraversalListener<V, E>> traversalListeners = new LinkedHashSet<>();

    // We keep this cached redundantly with traversalListeners.size()
    // so that subclasses can use it as a fast check to see if
    // event firing calls can be skipped.
    protected int nListeners = 0;

    protected final FlyweightEdgeEvent<V, E> reusableEdgeEvent;
    protected final FlyweightVertexEvent<V> reusableVertexEvent;
    protected final Graph<V, E> graph;
    protected boolean crossComponentTraversal;
    protected boolean reuseEvents;

    /**
     * Create a new iterator
     * 
     * @param graph the graph
     */
    public AbstractGraphIterator(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.reusableEdgeEvent = new FlyweightEdgeEvent<>(this, null);
        this.reusableVertexEvent = new FlyweightVertexEvent<>(this, null);
        this.crossComponentTraversal = true;
        this.reuseEvents = false;
    }

    /**
     * Get the graph being traversed.
     * 
     * @return the graph being traversed
     */
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * Sets the cross component traversal flag - indicates whether to traverse the graph across
     * connected components.
     *
     * @param crossComponentTraversal if <code>true</code> traverses across connected components.
     */
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        this.crossComponentTraversal = crossComponentTraversal;
    }

    /**
     * Test whether this iterator is set to traverse the graph across connected components.
     *
     * @return <code>true</code> if traverses across connected components, otherwise
     *         <code>false</code>.
     */
    @Override
    public boolean isCrossComponentTraversal()
    {
        return crossComponentTraversal;
    }

    @Override
    public void setReuseEvents(boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    @Override
    public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    @Override
    public void addTraversalListener(TraversalListener<V, E> l)
    {
        traversalListeners.add(l);
        nListeners = traversalListeners.size();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public void removeTraversalListener(TraversalListener<V, E> l)
    {
        traversalListeners.remove(l);
        nListeners = traversalListeners.size();
    }

    /**
     * Informs all listeners that the traversal of the current connected component finished.
     *
     * @param e the connected component finished event.
     */
    protected void fireConnectedComponentFinished(ConnectedComponentTraversalEvent e)
    {
        for (TraversalListener<V, E> l : traversalListeners) {
            l.connectedComponentFinished(e);
        }
    }

    /**
     * Informs all listeners that a traversal of a new connected component has started.
     *
     * @param e the connected component started event.
     */
    protected void fireConnectedComponentStarted(ConnectedComponentTraversalEvent e)
    {
        for (TraversalListener<V, E> l : traversalListeners) {
            l.connectedComponentStarted(e);
        }
    }

    /**
     * Informs all listeners that a the specified edge was visited.
     *
     * @param e the edge traversal event.
     */
    protected void fireEdgeTraversed(EdgeTraversalEvent<E> e)
    {
        for (TraversalListener<V, E> l : traversalListeners) {
            l.edgeTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was visited.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexTraversed(VertexTraversalEvent<V> e)
    {
        for (TraversalListener<V, E> l : traversalListeners) {
            l.vertexTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was finished.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexFinished(VertexTraversalEvent<V> e)
    {
        for (TraversalListener<V, E> l : traversalListeners) {
            l.vertexFinished(e);
        }
    }

    /**
     * Create a vertex traversal event.
     * 
     * @param vertex the vertex
     * @return the event
     */
    protected VertexTraversalEvent<V> createVertexTraversalEvent(V vertex)
    {
        if (reuseEvents) {
            reusableVertexEvent.setVertex(vertex);
            return reusableVertexEvent;
        } else {
            return new VertexTraversalEvent<>(this, vertex);
        }
    }

    /**
     * Create an edge traversal event.
     * 
     * @param edge the edge
     * @return the event
     */
    protected EdgeTraversalEvent<E> createEdgeTraversalEvent(E edge)
    {
        if (isReuseEvents()) {
            reusableEdgeEvent.setEdge(edge);
            return reusableEdgeEvent;
        } else {
            return new EdgeTraversalEvent<>(this, edge);
        }
    }

    /**
     * A reusable edge event.
     *
     * @author Barak Naveh
     */
    static class FlyweightEdgeEvent<VV, localE>
        extends
        EdgeTraversalEvent<localE>
    {
        private static final long serialVersionUID = 4051327833765000755L;

        /**
         * Creates a new FlyweightEdgeEvent.
         *
         * @param eventSource the source of the event.
         * @param edge the traversed edge.
         */
        public FlyweightEdgeEvent(Object eventSource, localE edge)
        {
            super(eventSource, edge);
        }

        /**
         * Sets the edge of this event.
         *
         * @param edge the edge to be set.
         */
        protected void setEdge(localE edge)
        {
            this.edge = edge;
        }
    }

    /**
     * A reusable vertex event.
     *
     * @author Barak Naveh
     */
    static class FlyweightVertexEvent<VV>
        extends
        VertexTraversalEvent<VV>
    {
        private static final long serialVersionUID = 3834024753848399924L;

        /**
         * Creates a new FlyweightVertexEvent.
         *
         * @param eventSource the source of the event.
         * @param vertex the traversed vertex.
         */
        public FlyweightVertexEvent(Object eventSource, VV vertex)
        {
            super(eventSource, vertex);
        }

        /**
         * Sets the vertex of this event.
         *
         * @param vertex the vertex to be set.
         */
        protected void setVertex(VV vertex)
        {
            this.vertex = vertex;
        }
    }

}
