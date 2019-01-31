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

/**
 * An event which indicates that a graph vertex has changed, or is about to change. The event can be
 * used either as an indication <i>after</i> the vertex has been added or removed, or <i>before</i>
 * it is added. The type of the event can be tested using the
 * {@link org.jgrapht.event.GraphChangeEvent#getType()} method.
 *
 * @param <V> the graph vertex type
 *
 * @author Barak Naveh
 */
public class GraphVertexChangeEvent<V>
    extends
    GraphChangeEvent
{
    private static final long serialVersionUID = 3690189962679104053L;

    /**
     * Before vertex added event. This event is fired before a vertex is added to a graph.
     */
    public static final int BEFORE_VERTEX_ADDED = 11;

    /**
     * Before vertex removed event. This event is fired before a vertex is removed from a graph.
     */
    public static final int BEFORE_VERTEX_REMOVED = 12;

    /**
     * Vertex added event. This event is fired after a vertex is added to a graph.
     */
    public static final int VERTEX_ADDED = 13;

    /**
     * Vertex removed event. This event is fired after a vertex is removed from a graph.
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
