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

/**
 * An empty do-nothing implementation of the {@link TraversalListener} interface used for
 * subclasses.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @since Aug 6, 2003
 */
public class TraversalListenerAdapter<V, E>
    implements
    TraversalListener<V, E>
{
    /**
     * @see TraversalListener#connectedComponentFinished(ConnectedComponentTraversalEvent)
     */
    @Override
    public void connectedComponentFinished(ConnectedComponentTraversalEvent e)
    {
    }

    /**
     * @see TraversalListener#connectedComponentStarted(ConnectedComponentTraversalEvent)
     */
    @Override
    public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
    {
    }

    /**
     * @see TraversalListener#edgeTraversed(EdgeTraversalEvent)
     */
    @Override
    public void edgeTraversed(EdgeTraversalEvent<E> e)
    {
    }

    /**
     * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
     */
    @Override
    public void vertexTraversed(VertexTraversalEvent<V> e)
    {
    }

    /**
     * @see TraversalListener#vertexFinished(VertexTraversalEvent)
     */
    @Override
    public void vertexFinished(VertexTraversalEvent<V> e)
    {
    }
}

// End TraversalListenerAdapter.java
