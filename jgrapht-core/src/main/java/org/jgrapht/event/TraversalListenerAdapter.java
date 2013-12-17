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
/* -----------------------------
 * TraversalListenerAdapter.java
 * -----------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Aug-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An empty do-nothing implementation of the {@link TraversalListener} interface
 * used for subclasses.
 *
 * @author Barak Naveh
 * @since Aug 6, 2003
 */
public class TraversalListenerAdapter<V, E>
    implements TraversalListener<V, E>
{
    

    /**
     * @see TraversalListener#connectedComponentFinished(ConnectedComponentTraversalEvent)
     */
    public void connectedComponentFinished(
        ConnectedComponentTraversalEvent e)
    {
    }

    /**
     * @see TraversalListener#connectedComponentStarted(ConnectedComponentTraversalEvent)
     */
    public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
    {
    }

    /**
     * @see TraversalListener#edgeTraversed(EdgeTraversalEvent)
     */
    public void edgeTraversed(EdgeTraversalEvent<V, E> e)
    {
    }

    /**
     * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
     */
    public void vertexTraversed(VertexTraversalEvent<V> e)
    {
    }

    /**
     * @see TraversalListener#vertexFinished(VertexTraversalEvent)
     */
    public void vertexFinished(VertexTraversalEvent<V> e)
    {
    }
}

// End TraversalListenerAdapter.java
