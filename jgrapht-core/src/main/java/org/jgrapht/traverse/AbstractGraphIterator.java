/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
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
/* --------------------------
 * AbstractGraphIterator.java
 * --------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 04-May-2004 : Made generic (CH)
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.event.*;


/**
 * An empty implementation of a graph iterator to minimize the effort required
 * to implement graph iterators.
 *
 * @author Barak Naveh
 * @since Jul 19, 2003
 */
public abstract class AbstractGraphIterator<V, E>
    implements GraphIterator<V, E>
{
    

    private List<TraversalListener<V, E>> traversalListeners =
        new ArrayList<TraversalListener<V, E>>();
    private boolean crossComponentTraversal = true;
    private boolean reuseEvents = false;

    // We keep this cached redundantly with traversalListeners.size()
    // so that subclasses can use it as a fast check to see if
    // event firing calls can be skipped.
    protected int nListeners = 0;

    

    /**
     * Sets the cross component traversal flag - indicates whether to traverse
     * the graph across connected components.
     *
     * @param crossComponentTraversal if <code>true</code> traverses across
     * connected components.
     */
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        this.crossComponentTraversal = crossComponentTraversal;
    }

    /**
     * Test whether this iterator is set to traverse the graph across connected
     * components.
     *
     * @return <code>true</code> if traverses across connected components,
     * otherwise <code>false</code>.
     */
    public boolean isCrossComponentTraversal()
    {
        return crossComponentTraversal;
    }

    /**
     * @see GraphIterator#setReuseEvents(boolean)
     */
    public void setReuseEvents(boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    /**
     * @see GraphIterator#isReuseEvents()
     */
    public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    public void addTraversalListener(TraversalListener<V, E> l)
    {
        if (!traversalListeners.contains(l)) {
            traversalListeners.add(l);
            nListeners = traversalListeners.size();
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    public void removeTraversalListener(TraversalListener<V, E> l)
    {
        traversalListeners.remove(l);
        nListeners = traversalListeners.size();
    }

    /**
     * Informs all listeners that the traversal of the current connected
     * component finished.
     *
     * @param e the connected component finished event.
     */
    protected void fireConnectedComponentFinished(
        ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.connectedComponentFinished(e);
        }
    }

    /**
     * Informs all listeners that a traversal of a new connected component has
     * started.
     *
     * @param e the connected component started event.
     */
    protected void fireConnectedComponentStarted(
        ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.connectedComponentStarted(e);
        }
    }

    /**
     * Informs all listeners that a the specified edge was visited.
     *
     * @param e the edge traversal event.
     */
    protected void fireEdgeTraversed(EdgeTraversalEvent<V, E> e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.edgeTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was visited.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexTraversed(VertexTraversalEvent<V> e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.vertexTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was finished.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexFinished(VertexTraversalEvent<V> e)
    {
        for (int i = 0; i < nListeners; i++) {
            TraversalListener<V, E> l = traversalListeners.get(i);
            l.vertexFinished(e);
        }
    }
}

// End AbstractGraphIterator.java
