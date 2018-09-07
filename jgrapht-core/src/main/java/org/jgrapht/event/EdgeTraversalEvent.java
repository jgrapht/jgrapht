/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.event;

import java.util.*;

/**
 * A traversal event for a graph edge.
 *
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @since Aug 11, 2003
 */
public class EdgeTraversalEvent<E>
    extends
    EventObject
{
    private static final long serialVersionUID = 4050768173789820979L;

    /**
     * The traversed edge.
     */
    protected E edge;

    /**
     * Creates a new EdgeTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param edge the traversed edge.
     */
    public EdgeTraversalEvent(Object eventSource, E edge)
    {
        super(eventSource);
        this.edge = edge;
    }

    /**
     * Returns the traversed edge.
     *
     * @return the traversed edge.
     */
    public E getEdge()
    {
        return edge;
    }
}

// End EdgeTraversalEvent.java
