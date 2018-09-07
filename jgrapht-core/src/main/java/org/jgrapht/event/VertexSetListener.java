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
 * A listener that is notified when the graph's vertex set changes. It should be used when
 * <i>only</i> notifications on vertex-set changes are of interest. If all graph notifications are
 * of interest better use <code>
 * GraphListener</code>.
 *
 * @param <V> the graph vertex type
 *
 * @author Barak Naveh
 * @see GraphListener
 * @since Jul 18, 2003
 */
public interface VertexSetListener<V>
    extends
    EventListener
{
    /**
     * Notifies that a vertex has been added to the graph.
     *
     * @param e the vertex event.
     */
    void vertexAdded(GraphVertexChangeEvent<V> e);

    /**
     * Notifies that a vertex has been removed from the graph.
     *
     * @param e the vertex event.
     */
    void vertexRemoved(GraphVertexChangeEvent<V> e);
}

// End VertexSetListener.java
