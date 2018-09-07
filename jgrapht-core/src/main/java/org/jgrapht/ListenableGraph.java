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
package org.jgrapht;

import org.jgrapht.event.*;

/**
 * A graph that supports listeners on structural change events.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GraphListener
 * @see VertexSetListener
 *
 * @author Barak Naveh
 * @since Jul 20, 2003
 */
public interface ListenableGraph<V, E>
    extends
    Graph<V, E>
{
    /**
     * Adds the specified graph listener to this graph, if not already present.
     *
     * @param l the listener to be added.
     */
    public void addGraphListener(GraphListener<V, E> l);

    /**
     * Adds the specified vertex set listener to this graph, if not already present.
     *
     * @param l the listener to be added.
     */
    public void addVertexSetListener(VertexSetListener<V> l);

    /**
     * Removes the specified graph listener from this graph, if present.
     *
     * @param l the listener to be removed.
     */
    public void removeGraphListener(GraphListener<V, E> l);

    /**
     * Removes the specified vertex set listener from this graph, if present.
     *
     * @param l the listener to be removed.
     */
    public void removeVertexSetListener(VertexSetListener<V> l);
}

// End ListenableGraph.java
