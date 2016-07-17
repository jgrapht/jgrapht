/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
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
/* --------------------------
 * AbstractGraphIterator.java
 * --------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer, Assaf Mizrachi
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 04-May-2004 : Made generic (CH)
 * 15-Jul-2016 : Moved Specifics and Flyweight events from CrossComponentGraphItertor
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.event.*;


/**
 * An empty implementation of a graph iterator to minimize the effort required
 * to implement graph iterators.
 *
 * @author Barak Naveh
 * @since Jul 19, 2003
 */
public abstract class AbstractGraphIterator<V, E>
    implements GraphIterator<V, E>
{
    private List<TraversalListener<V, E>> traversalListeners =
            new ArrayList<>();
    private boolean crossComponentTraversal = true;
    private boolean reuseEvents = false;

    // We keep this cached redundantly with traversalListeners.size()
    // so that subclasses can use it as a fast check to see if
    // event firing calls can be skipped.
    protected int nListeners = 0;
    // TODO: support ConcurrentModificationException if graph modified
    // during iteration.
    protected FlyweightEdgeEvent<V, E> reusableEdgeEvent;
    protected FlyweightVertexEvent<V> reusableVertexEvent;
    protected Specifics<V, E> specifics;

    /**
     * Sets the cross component traversal flag - indicates whether to traverse
     * the graph across connected components.
     *
     * @param crossComponentTraversal if <code>true</code> traverses across
     * connected components.
     */
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        this.crossComponentTraversal = crossComponentTraversal;
    }

    /**
     * Test whether this iterator is set to traverse the graph across connected
     * components.
     *
     * @return <code>true</code> if traverses across connected components,
     * otherwise <code>false</code>.
     */
    @Override public boolean isCrossComponentTraversal()
    {
        return crossComponentTraversal;
    }

    /**
     * @see GraphIterator#setReuseEvents(boolean)
     */
    @Override public void setReuseEvents(boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    /**
     * @see GraphIterator#isReuseEvents()
     */
    @Override public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    @Override public void addTraversalListener(TraversalListener<V, E> l)
    {
        if (!traversalListeners.contains(l)) {
            traversalListeners.add(l);
            nListeners = traversalListeners.size();
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    @Override public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    @Override public void removeTraversalListener(TraversalListener<V, E> l)
    {
        traversalListeners.remove(l);
        nListeners = traversalListeners.size();
    }

    /**
     * Informs all listeners that the traversal of the current connected
     * component finished.
     *
     * @param e the connected component finished event.
     */
    protected void fireConnectedComponentFinished(
        ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.connectedComponentFinished(e);
        }
    }

    /**
     * Informs all listeners that a traversal of a new connected component has
     * started.
     *
     * @param e the connected component started event.
     */
    protected void fireConnectedComponentStarted(
        ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
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
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
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
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
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
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.vertexFinished(e);
        }
    }
    
    /**
     * A reusable edge event.
     *
     * @author Barak Naveh
     * @since Aug 11, 2003
     */
    static class FlyweightEdgeEvent<VV, localE>
        extends EdgeTraversalEvent<localE>
    {
        private static final long serialVersionUID = 4051327833765000755L;

        /**
         * @see EdgeTraversalEvent
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
     * @since Aug 11, 2003
     */
    static class FlyweightVertexEvent<VV>
        extends VertexTraversalEvent<VV>
    {
        private static final long serialVersionUID = 3834024753848399924L;

        /**
         * @see VertexTraversalEvent#VertexTraversalEvent(Object, Object)
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
    
    // -------------------------------------------------------------------------
    /**
     * Creates directed/undirected graph specifics according to the provided
     * graph - directed/undirected, respectively. 
     * 
     * @param g the graph to create specifics for
     *
     * @return the created specifics
     */
    static <V, E> Specifics<V, E> createGraphSpecifics(Graph<V, E> g)
    {
        if (g instanceof DirectedGraph<?, ?>) {
            return new DirectedSpecifics<>((DirectedGraph<V, E>) g);
        } else {
            return new UndirectedSpecifics<>(g);
        }
    }
    
    /**
     * Provides unified interface for operations that are different in directed
     * graphs and in undirected graphs.
     */
    abstract static class Specifics<VV, EE>
    {
        /**
         * Returns the edges outgoing from the specified vertex in case of
         * directed graph, and the edge touching the specified vertex in case of
         * undirected graph.
         *
         * @param vertex the vertex whose outgoing edges are to be returned.
         *
         * @return the edges outgoing from the specified vertex in case of
         * directed graph, and the edge touching the specified vertex in case of
         * undirected graph.
         */
        public abstract Set<? extends EE> edgesOf(VV vertex);
    }

    /**
     * An implementation of {@link Specifics} for a directed graph.
     */
    static class DirectedSpecifics<VV, EE>
        extends Specifics<VV, EE>
    {
        private DirectedGraph<VV, EE> graph;

        /**
         * Creates a new DirectedSpecifics object.
         *
         * @param g the graph for which this specifics object to be created.
         */
        public DirectedSpecifics(DirectedGraph<VV, EE> g)
        {
            graph = g;
        }

        /**
         * @see CrossComponentIterator.Specifics#edgesOf(Object)
         */
        @Override public Set<? extends EE> edgesOf(VV vertex)
        {
            return graph.outgoingEdgesOf(vertex);
        }
    }

    /**
     * An implementation of {@link Specifics} in which edge direction (if any)
     * is ignored.
     */
    static class UndirectedSpecifics<VV, EE>
        extends Specifics<VV, EE>
    {
        private Graph<VV, EE> graph;

        /**
         * Creates a new UndirectedSpecifics object.
         *
         * @param g the graph for which this specifics object to be created.
         */
        public UndirectedSpecifics(Graph<VV, EE> g)
        {
            graph = g;
        }

        /**
         * @see CrossComponentIterator.Specifics#edgesOf(Object)
         */
        @Override public Set<EE> edgesOf(VV vertex)
        {
            return graph.edgesOf(vertex);
        }
    }
}

// End AbstractGraphIterator.java
