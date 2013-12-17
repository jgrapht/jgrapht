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
 * DefaultListenableGraph.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 04-Aug-2003 : Strong refs to listeners instead of weak refs (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 07-Mar-2004 : Fixed unnecessary clone bug #819075 (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.util.*;


/**
 * A graph backed by the the graph specified at the constructor, which can be
 * listened by <code>GraphListener</code> s and by <code>
 * VertexSetListener</code> s. Operations on this graph "pass through" to the to
 * the backing graph. Any modification made to this graph or the backing graph
 * is reflected by the other.
 *
 * <p>This graph does <i>not</i> pass the hashCode and equals operations through
 * to the backing graph, but relies on <tt>Object</tt>'s <tt>equals</tt> and
 * <tt>hashCode</tt> methods.</p>
 *
 * @author Barak Naveh
 * @see GraphListener
 * @see VertexSetListener
 * @since Jul 20, 2003
 */
public class DefaultListenableGraph<V, E>
    extends GraphDelegator<V, E>
    implements ListenableGraph<V, E>,
        Cloneable
{
    

    private static final long serialVersionUID = 3977575900898471984L;

    

    private List<GraphListener<V, E>> graphListeners =
        new ArrayList<GraphListener<V, E>>();
    private List<VertexSetListener<V>> vertexSetListeners =
        new ArrayList<VertexSetListener<V>>();
    private FlyweightEdgeEvent<V, E> reuseableEdgeEvent;
    private FlyweightVertexEvent<V> reuseableVertexEvent;
    private boolean reuseEvents;

    

    /**
     * Creates a new listenable graph.
     *
     * @param g the backing graph.
     */
    public DefaultListenableGraph(Graph<V, E> g)
    {
        this(g, false);
    }

    /**
     * Creates a new listenable graph. If the <code>reuseEvents</code> flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @param g the backing graph.
     * @param reuseEvents whether to reuse previously fired event objects
     * instead of creating a new event object for each event.
     *
     * @throws IllegalArgumentException if the backing graph is already a
     * listenable graph.
     */
    public DefaultListenableGraph(Graph<V, E> g, boolean reuseEvents)
    {
        super(g);
        this.reuseEvents = reuseEvents;
        reuseableEdgeEvent = new FlyweightEdgeEvent<V, E>(this, -1, null);
        reuseableVertexEvent = new FlyweightVertexEvent<V>(this, -1, null);

        // the following restriction could be probably relaxed in the future.
        if (g instanceof ListenableGraph<?, ?>) {
            throw new IllegalArgumentException(
                "base graph cannot be listenable");
        }
    }

    

    /**
     * If the <code>reuseEvents</code> flag is set to <code>true</code> this
     * class will reuse previously fired events and will not create a new object
     * for each event. This option increases performance but should be used with
     * care, especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects
     * instead of creating a new event object for each event.
     */
    public void setReuseEvents(boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    public E addEdge(V sourceVertex, V targetVertex)
    {
        E e = super.addEdge(sourceVertex, targetVertex);

        if (e != null) {
            fireEdgeAdded(e, sourceVertex, targetVertex);
        }

        return e;
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        boolean added = super.addEdge(sourceVertex, targetVertex, e);

        if (added) {
            fireEdgeAdded(e, sourceVertex, targetVertex);
        }

        return added;
    }

    /**
     * @see ListenableGraph#addGraphListener(GraphListener)
     */
    public void addGraphListener(GraphListener<V, E> l)
    {
        addToListenerList(graphListeners, l);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    public boolean addVertex(V v)
    {
        boolean modified = super.addVertex(v);

        if (modified) {
            fireVertexAdded(v);
        }

        return modified;
    }

    /**
     * @see ListenableGraph#addVertexSetListener(VertexSetListener)
     */
    public void addVertexSetListener(VertexSetListener<V> l)
    {
        addToListenerList(vertexSetListeners, l);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try {
            TypeUtil<DefaultListenableGraph<V, E>> typeDecl = null;

            DefaultListenableGraph<V, E> g =
                TypeUtil.uncheckedCast(super.clone(), typeDecl);
            g.graphListeners = new ArrayList<GraphListener<V, E>>();
            g.vertexSetListeners = new ArrayList<VertexSetListener<V>>();

            return g;
        } catch (CloneNotSupportedException e) {
            // should never get here since we're Cloneable
            e.printStackTrace();
            throw new RuntimeException("internal error");
        }
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = super.removeEdge(sourceVertex, targetVertex);

        if (e != null) {
            fireEdgeRemoved(e, sourceVertex, targetVertex);
        }

        return e;
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    public boolean removeEdge(E e)
    {
        V sourceVertex = getEdgeSource(e);
        V targetVertex = getEdgeTarget(e);

        boolean modified = super.removeEdge(e);

        if (modified) {
            fireEdgeRemoved(e, sourceVertex, targetVertex);
        }

        return modified;
    }

    /**
     * @see ListenableGraph#removeGraphListener(GraphListener)
     */
    public void removeGraphListener(GraphListener<V, E> l)
    {
        graphListeners.remove(l);
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    public boolean removeVertex(V v)
    {
        if (containsVertex(v)) {
            Set<E> touchingEdgesList = edgesOf(v);

            // copy set to avoid ConcurrentModificationException
            removeAllEdges(new ArrayList<E>(touchingEdgesList));

            super.removeVertex(v); // remove the vertex itself

            fireVertexRemoved(v);

            return true;
        } else {
            return false;
        }
    }

    /**
     * @see ListenableGraph#removeVertexSetListener(VertexSetListener)
     */
    public void removeVertexSetListener(VertexSetListener<V> l)
    {
        vertexSetListeners.remove(l);
    }

    /**
     * Notify listeners that the specified edge was added.
     *
     * @param edge the edge that was added.
     * @param source edge source
     * @param target edge target
     */
    protected void fireEdgeAdded(E edge, V source, V target)
    {
        GraphEdgeChangeEvent<V, E> e =
            createGraphEdgeChangeEvent(
                GraphEdgeChangeEvent.EDGE_ADDED,
                edge,
                source,
                target);

        for (GraphListener<V, E> l : graphListeners) {
            l.edgeAdded(e);
        }
    }

    /**
     * Notify listeners that the specified edge was removed.
     *
     * @param edge the edge that was removed.
     * @param source edge source
     * @param target edge target
     */
    protected void fireEdgeRemoved(E edge, V source, V target)
    {
        GraphEdgeChangeEvent<V, E> e =
            createGraphEdgeChangeEvent(
                GraphEdgeChangeEvent.EDGE_REMOVED,
                edge,
                source,
                target);

        for (GraphListener<V, E> l : graphListeners) {
            l.edgeRemoved(e);
        }
    }

    /**
     * Notify listeners that the specified vertex was added.
     *
     * @param vertex the vertex that was added.
     */
    protected void fireVertexAdded(V vertex)
    {
        GraphVertexChangeEvent<V> e =
            createGraphVertexChangeEvent(
                GraphVertexChangeEvent.VERTEX_ADDED,
                vertex);

        for (VertexSetListener<V> l : vertexSetListeners) {
            l.vertexAdded(e);
        }

        for (GraphListener<V, E> l : graphListeners) {
            l.vertexAdded(e);
        }
    }

    /**
     * Notify listeners that the specified vertex was removed.
     *
     * @param vertex the vertex that was removed.
     */
    protected void fireVertexRemoved(V vertex)
    {
        GraphVertexChangeEvent<V> e =
            createGraphVertexChangeEvent(
                GraphVertexChangeEvent.VERTEX_REMOVED,
                vertex);

        for (VertexSetListener<V> l : vertexSetListeners) {
            l.vertexRemoved(e);
        }

        for (GraphListener<V, E> l : graphListeners) {
            l.vertexRemoved(e);
        }
    }

    private static <L extends EventListener> void addToListenerList(
        List<L> list,
        L l)
    {
        if (!list.contains(l)) {
            list.add(l);
        }
    }

    private GraphEdgeChangeEvent<V, E> createGraphEdgeChangeEvent(
        int eventType,
        E edge,
        V source,
        V target)
    {
        if (reuseEvents) {
            reuseableEdgeEvent.setType(eventType);
            reuseableEdgeEvent.setEdge(edge);
            reuseableEdgeEvent.setEdgeSource(source);
            reuseableEdgeEvent.setEdgeTarget(target);

            return reuseableEdgeEvent;
        } else {
            return new GraphEdgeChangeEvent<V, E>(
                this,
                eventType,
                edge,
                source,
                target);
        }
    }

    private GraphVertexChangeEvent<V> createGraphVertexChangeEvent(
        int eventType,
        V vertex)
    {
        if (reuseEvents) {
            reuseableVertexEvent.setType(eventType);
            reuseableVertexEvent.setVertex(vertex);

            return reuseableVertexEvent;
        } else {
            return new GraphVertexChangeEvent<V>(this, eventType, vertex);
        }
    }

    

    /**
     * A reuseable edge event.
     *
     * @author Barak Naveh
     * @since Aug 10, 2003
     */
    private static class FlyweightEdgeEvent<VV, EE>
        extends GraphEdgeChangeEvent<VV, EE>
    {
        private static final long serialVersionUID = 3907207152526636089L;

        /**
         * @see GraphEdgeChangeEvent#GraphEdgeChangeEvent(Object, int, Edge)
         */
        public FlyweightEdgeEvent(Object eventSource, int type, EE e)
        {
            super(eventSource, type, e);
        }

        /**
         * Sets the edge of this event.
         *
         * @param e the edge to be set.
         */
        protected void setEdge(EE e)
        {
            this.edge = e;
        }

        protected void setEdgeSource(VV v)
        {
            this.edgeSource = v;
        }

        protected void setEdgeTarget(VV v)
        {
            this.edgeTarget = v;
        }

        /**
         * Set the event type of this event.
         *
         * @param type the type to be set.
         */
        protected void setType(int type)
        {
            this.type = type;
        }
    }

    /**
     * A reuseable vertex event.
     *
     * @author Barak Naveh
     * @since Aug 10, 2003
     */
    private static class FlyweightVertexEvent<VV>
        extends GraphVertexChangeEvent<VV>
    {
        private static final long serialVersionUID = 3257848787857585716L;

        /**
         * @see GraphVertexChangeEvent#GraphVertexChangeEvent(Object, int,
         * Object)
         */
        public FlyweightVertexEvent(Object eventSource, int type, VV vertex)
        {
            super(eventSource, type, vertex);
        }

        /**
         * Set the event type of this event.
         *
         * @param type type to be set.
         */
        protected void setType(int type)
        {
            this.type = type;
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

// End DefaultListenableGraph.java
