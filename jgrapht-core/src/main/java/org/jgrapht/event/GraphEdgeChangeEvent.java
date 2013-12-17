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
 * GraphEdgeChangeEvent.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An event which indicates that a graph edge has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the edge
 * has been added or removed, or <i>before</i> it is added. The type of the
 * event can be tested using the {@link
 * org.jgrapht.event.GraphChangeEvent#getType()} method.
 *
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphEdgeChangeEvent<V, E>
    extends GraphChangeEvent
{
    

    private static final long serialVersionUID = 3618134563335844662L;

    /**
     * Before edge added event. This event is fired before an edge is added to a
     * graph.
     */
    public static final int BEFORE_EDGE_ADDED = 21;

    /**
     * Before edge removed event. This event is fired before an edge is removed
     * from a graph.
     */
    public static final int BEFORE_EDGE_REMOVED = 22;

    /**
     * Edge added event. This event is fired after an edge is added to a graph.
     */
    public static final int EDGE_ADDED = 23;

    /**
     * Edge removed event. This event is fired after an edge is removed from a
     * graph.
     */
    public static final int EDGE_REMOVED = 24;

    

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
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     *
     * @deprecated Use new constructor which takes vertex parameters.
     */
    public GraphEdgeChangeEvent(
        Object eventSource,
        int type,
        E edge)
    {
        this(eventSource, type, edge, null, null);
    }

    /**
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     * @param edgeSource edge source vertex
     * @param edgeTarget edge target vertex
     */
    public GraphEdgeChangeEvent(
        Object eventSource,
        int type,
        E edge,
        V edgeSource,
        V edgeTarget)
    {
        super(eventSource, type);
        this.edge = edge;
        this.edgeSource = edgeSource;
        this.edgeTarget = edgeTarget;
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
}

// End GraphEdgeChangeEvent.java
