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
 * An empty do-nothing implementation of the {@link TraversalListener} interface used for
 * subclasses.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
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
