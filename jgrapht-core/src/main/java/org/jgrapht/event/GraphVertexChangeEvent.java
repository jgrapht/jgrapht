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
 * GraphVertexChangeEvent.java
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
 * 10-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An event which indicates that a graph vertex has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the vertex
 * has been added or removed, or <i>before</i> it is added. The type of the
 * event can be tested using the {@link
 * org.jgrapht.event.GraphChangeEvent#getType()} method.
 *
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphVertexChangeEvent<V>
    extends GraphChangeEvent
{
    

    private static final long serialVersionUID = 3690189962679104053L;

    /**
     * Before vertex added event. This event is fired before a vertex is added
     * to a graph.
     */
    public static final int BEFORE_VERTEX_ADDED = 11;

    /**
     * Before vertex removed event. This event is fired before a vertex is
     * removed from a graph.
     */
    public static final int BEFORE_VERTEX_REMOVED = 12;

    /**
     * Vertex added event. This event is fired after a vertex is added to a
     * graph.
     */
    public static final int VERTEX_ADDED = 13;

    /**
     * Vertex removed event. This event is fired after a vertex is removed from
     * a graph.
     */
    public static final int VERTEX_REMOVED = 14;

    

    /**
     * The vertex that this event is related to.
     */
    protected V vertex;

    

    /**
     * Creates a new GraphVertexChangeEvent object.
     *
     * @param eventSource the source of the event.
     * @param type the type of the event.
     * @param vertex the vertex that the event is related to.
     */
    public GraphVertexChangeEvent(Object eventSource, int type, V vertex)
    {
        super(eventSource, type);
        this.vertex = vertex;
    }

    

    /**
     * Returns the vertex that this event is related to.
     *
     * @return the vertex that this event is related to.
     */
    public V getVertex()
    {
        return vertex;
    }
}

// End GraphVertexChangeEvent.java
