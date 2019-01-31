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
package org.jgrapht.event;

import org.jgrapht.*;

/**
 * An event which indicates that a graph edge has changed, or is about to change. The event can be
 * used either as an indication <i>after</i> the edge has been added or removed, or <i>before</i> it
 * is added. The type of the event can be tested using the
 * {@link org.jgrapht.event.GraphChangeEvent#getType()} method.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class GraphEdgeChangeEvent<V, E>
    extends
    GraphChangeEvent
{
    private static final long serialVersionUID = -4421610303769803253L;

    /**
     * Before edge added event. This event is fired before an edge is added to a graph.
     */
    public static final int BEFORE_EDGE_ADDED = 21;

    /**
     * Before edge removed event. This event is fired before an edge is removed from a graph.
     */
    public static final int BEFORE_EDGE_REMOVED = 22;

    /**
     * Edge added event. This event is fired after an edge is added to a graph.
     */
    public static final int EDGE_ADDED = 23;

    /**
     * Edge removed event. This event is fired after an edge is removed from a graph.
     */
    public static final int EDGE_REMOVED = 24;

    /**
     * Edge weight updated event. This event is fired after an edge weight is updated in a graph.
     */
    public static final int EDGE_WEIGHT_UPDATED = 25;

    /**
     * The edge that this event is related to.
     */
    protected E edge;

    /**
     * The source vertex of the edge that this event is related to.
     */
    protected V edgeSource;

    /**
     * The target vertex of the edge that this event is related to.
     */
    protected V edgeTarget;

    /**
     * The weight of the edge that this event is related to.
     */
    protected double edgeWeight;

    /**
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     * @param edgeSource edge source vertex
     * @param edgeTarget edge target vertex
     */
    public GraphEdgeChangeEvent(Object eventSource, int type, E edge, V edgeSource, V edgeTarget)
    {
        this(eventSource, type, edge, edgeSource, edgeTarget, Graph.DEFAULT_EDGE_WEIGHT);
    }

    /**
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     * @param edgeSource edge source vertex
     * @param edgeTarget edge target vertex
     * @param edgeWeight edge weight
     */
    public GraphEdgeChangeEvent(
        Object eventSource, int type, E edge, V edgeSource, V edgeTarget, double edgeWeight)
    {
        super(eventSource, type);
        this.edge = edge;
        this.edgeSource = edgeSource;
        this.edgeTarget = edgeTarget;
        this.edgeWeight = edgeWeight;
    }

    /**
     * Returns the edge that this event is related to.
     *
     * @return event edge
     */
    public E getEdge()
    {
        return edge;
    }

    /**
     * Returns the source vertex that this event is related to.
     *
     * @return event source vertex
     */
    public V getEdgeSource()
    {
        return edgeSource;
    }

    /**
     * Returns the target vertex that this event is related to.
     *
     * @return event target vertex
     */
    public V getEdgeTarget()
    {
        return edgeTarget;
    }

    /**
     * Returns the weight of the edge that this event is related to.
     *
     * @return event edge weight
     */
    public double getEdgeWeight()
    {
        return edgeWeight;
    }

}
