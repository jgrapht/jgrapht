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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * A graph backed by the the graph specified at the constructor, which can be listened by
 * <code>GraphListener</code> s and by <code>
 * VertexSetListener</code> s. Operations on this graph "pass through" to the to the backing graph.
 * Any modification made to this graph or the backing graph is reflected by the other.
 *
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through to the backing graph,
 * but relies on <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @see GraphListener
 * @see VertexSetListener
 */
public class DefaultListenableGraph<V, E>
    extends
    GraphDelegator<V, E>
    implements
    ListenableGraph<V, E>,
    Cloneable
{
    private static final long serialVersionUID = -1156773351121025002L;

    private List<GraphListener<V, E>> graphListeners = new ArrayList<>();
    private List<VertexSetListener<V>> vertexSetListeners = new ArrayList<>();
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
     * Creates a new listenable graph. If the <code>reuseEvents</code> flag is set to
     * <code>true</code> this class will reuse previously fired events and will not create a new
     * object for each event. This option increases performance but should be used with care,
     * especially in multithreaded environment.
     *
     * @param g the backing graph.
     * @param reuseEvents whether to reuse previously fired event objects instead of creating a new
     *        event object for each event.
     *
     * @throws IllegalArgumentException if the backing graph is already a listenable graph.
     */
    public DefaultListenableGraph(Graph<V, E> g, boolean reuseEvents)
    {
        super(g);
        this.reuseEvents = reuseEvents;
        reuseableEdgeEvent = new FlyweightEdgeEvent<>(this, -1, null);
        reuseableVertexEvent = new FlyweightVertexEvent<>(this, -1, null);

        // the following restriction could be probably relaxed in the future.
        if (g instanceof ListenableGraph<?, ?>) {
            throw new IllegalArgumentException("base graph cannot be listenable");
        }
    }

    /**
     * If the <code>reuseEvents</code> flag is set to <code>true</code> this class will reuse
     * previously fired events and will not create a new object for each event. This option
     * increases performance but should be used with care, especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects instead of creating a new
     *        event object for each event.
     */
    public void setReuseEvents(boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is set to
     * <code>true</code> this class will reuse previously fired events and will not create a new
     * object for each event. This option increases performance but should be used with care,
     * especially in multithreaded environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        E e = super.addEdge(sourceVertex, targetVertex);

        if (e != null) {
            fireEdgeAdded(e, sourceVertex, targetVertex, Graph.DEFAULT_EDGE_WEIGHT);
        }

        return e;
    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        boolean added = super.addEdge(sourceVertex, targetVertex, e);

        if (added) {
            fireEdgeAdded(e, sourceVertex, targetVertex, Graph.DEFAULT_EDGE_WEIGHT);
        }

        return added;
    }

    @Override
    public void addGraphListener(GraphListener<V, E> l)
    {
        addToListenerList(graphListeners, l);
    }

    @Override
    public boolean addVertex(V v)
    {
        boolean modified = super.addVertex(v);

        if (modified) {
            fireVertexAdded(v);
        }

        return modified;
    }

    @Override
    public void addVertexSetListener(VertexSetListener<V> l)
    {
        addToListenerList(vertexSetListeners, l);
    }

    @Override
    public Object clone()
    {
        try {
            DefaultListenableGraph<V, E> g = TypeUtil.uncheckedCast(super.clone());
            g.graphListeners = new ArrayList<>();
            g.vertexSetListeners = new ArrayList<>();

            return g;
        } catch (CloneNotSupportedException e) {
            // should never get here since we're Cloneable
            e.printStackTrace();
            throw new RuntimeException("internal error");
        }
    }

    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = super.getEdge(sourceVertex, targetVertex);
        if (e != null) {
            double weight = super.getEdgeWeight(e);
            if (super.removeEdge(e)) {
                fireEdgeRemoved(e, sourceVertex, targetVertex, weight);
            }
        }
        return e;
    }

    @Override
    public boolean removeEdge(E e)
    {
        V sourceVertex = getEdgeSource(e);
        V targetVertex = getEdgeTarget(e);
        double weight = getEdgeWeight(e);

        boolean modified = super.removeEdge(e);

        if (modified) {
            fireEdgeRemoved(e, sourceVertex, targetVertex, weight);
        }

        return modified;
    }

    @Override
    public void removeGraphListener(GraphListener<V, E> l)
    {
        graphListeners.remove(l);
    }

    @Override
    public boolean removeVertex(V v)
    {
        if (containsVertex(v)) {
            Set<E> touchingEdgesList = edgesOf(v);

            // copy set to avoid ConcurrentModificationException
            removeAllEdges(new ArrayList<>(touchingEdgesList));

            super.removeVertex(v); // remove the vertex itself

            fireVertexRemoved(v);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setEdgeWeight(E e, double weight)
    {
        super.setEdgeWeight(e, weight);

        V sourceVertex = getEdgeSource(e);
        V targetVertex = getEdgeTarget(e);

        fireEdgeWeightUpdated(e, sourceVertex, targetVertex, weight);
    }

    @Override
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
     * @param weight edge weight
     */
    protected void fireEdgeAdded(E edge, V source, V target, double weight)
    {
        GraphEdgeChangeEvent<V, E> e = createGraphEdgeChangeEvent(
            GraphEdgeChangeEvent.EDGE_ADDED, edge, source, target, weight);

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
     * @param weight edge weight
     */
    protected void fireEdgeRemoved(E edge, V source, V target, double weight)
    {
        GraphEdgeChangeEvent<V, E> e = createGraphEdgeChangeEvent(
            GraphEdgeChangeEvent.EDGE_REMOVED, edge, source, target, weight);

        for (GraphListener<V, E> l : graphListeners) {
            l.edgeRemoved(e);
        }
    }

    /**
     * Notify listeners that the weight of an edge has changed.
     *
     * @param edge the edge whose weight has changed.
     * @param source edge source
     * @param target edge target
     * @param weight the edge weight
     */
    protected void fireEdgeWeightUpdated(E edge, V source, V target, double weight)
    {
        GraphEdgeChangeEvent<V, E> e = createGraphEdgeChangeEvent(
            GraphEdgeChangeEvent.EDGE_WEIGHT_UPDATED, edge, source, target, weight);

        for (GraphListener<V, E> l : graphListeners) {
            l.edgeWeightUpdated(e);
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
            createGraphVertexChangeEvent(GraphVertexChangeEvent.VERTEX_ADDED, vertex);

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
            createGraphVertexChangeEvent(GraphVertexChangeEvent.VERTEX_REMOVED, vertex);

        for (VertexSetListener<V> l : vertexSetListeners) {
            l.vertexRemoved(e);
        }

        for (GraphListener<V, E> l : graphListeners) {
            l.vertexRemoved(e);
        }
    }

    private static <L extends EventListener> void addToListenerList(List<L> list, L l)
    {
        if (!list.contains(l)) {
            list.add(l);
        }
    }

    private GraphEdgeChangeEvent<V, E> createGraphEdgeChangeEvent(
        int eventType, E edge, V source, V target, double weight)
    {
        if (reuseEvents) {
            reuseableEdgeEvent.setType(eventType);
            reuseableEdgeEvent.setEdge(edge);
            reuseableEdgeEvent.setEdgeSource(source);
            reuseableEdgeEvent.setEdgeTarget(target);
            reuseableEdgeEvent.setEdgeWeight(weight);

            return reuseableEdgeEvent;
        } else {
            return new GraphEdgeChangeEvent<>(this, eventType, edge, source, target, weight);
        }
    }

    private GraphVertexChangeEvent<V> createGraphVertexChangeEvent(int eventType, V vertex)
    {
        if (reuseEvents) {
            reuseableVertexEvent.setType(eventType);
            reuseableVertexEvent.setVertex(vertex);

            return reuseableVertexEvent;
        } else {
            return new GraphVertexChangeEvent<>(this, eventType, vertex);
        }
    }

    /**
     * A reuseable edge event.
     *
     * @author Barak Naveh
     */
    private static class FlyweightEdgeEvent<VV, EE>
        extends
        GraphEdgeChangeEvent<VV, EE>
    {
        private static final long serialVersionUID = 3907207152526636089L;

        /**
         * @see GraphEdgeChangeEvent
         */
        public FlyweightEdgeEvent(Object eventSource, int type, EE e)
        {
            super(eventSource, type, e, null, null);
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

        protected void setEdgeWeight(double weight)
        {
            this.edgeWeight = weight;
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
     */
    private static class FlyweightVertexEvent<VV>
        extends
        GraphVertexChangeEvent<VV>
    {
        private static final long serialVersionUID = 3257848787857585716L;

        /**
         * @see GraphVertexChangeEvent#GraphVertexChangeEvent(Object, int, Object)
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
