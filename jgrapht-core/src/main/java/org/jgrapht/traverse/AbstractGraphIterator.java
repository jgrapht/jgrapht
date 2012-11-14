/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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
    //~ Instance fields --------------------------------------------------------

    private final List<TraversalListener<V, E>> traversalListeners =
        new ArrayList<TraversalListener<V, E>>();
    private boolean crossComponentTraversal = true;
    private boolean reuseEvents = false;

    // We keep this cached redundantly with traversalListeners.size()
    // so that subclasses can use it as a fast check to see if
    // event firing calls can be skipped.
    protected int nListeners = 0;

    //~ Methods ----------------------------------------------------------------

    /**
     * Sets the cross component traversal flag - indicates whether to traverse
     * the graph across connected components.
     *
     * @param crossComponentTraversal if <code>true</code> traverses across
     * connected components.
     */
    public void setCrossComponentTraversal(final boolean crossComponentTraversal)
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
    @Override
    public boolean isCrossComponentTraversal()
    {
        return crossComponentTraversal;
    }

    /**
     * @see GraphIterator#setReuseEvents(boolean)
     */
    @Override
    public void setReuseEvents(final boolean reuseEvents)
    {
        this.reuseEvents = reuseEvents;
    }

    /**
     * @see GraphIterator#isReuseEvents()
     */
    @Override
    public boolean isReuseEvents()
    {
        return reuseEvents;
    }

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    @Override
    public void addTraversalListener(final TraversalListener<V, E> l)
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
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    @Override
    public void removeTraversalListener(final TraversalListener<V, E> l)
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
        final ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            final TraversalListener<V, E> l = traversalListeners.get(i);
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
        final ConnectedComponentTraversalEvent e)
    {
        for (int i = 0; i < nListeners; i++) {
            final TraversalListener<V, E> l = traversalListeners.get(i);
            l.connectedComponentStarted(e);
        }
    }

    /**
     * Informs all listeners that a the specified edge was visited.
     *
     * @param e the edge traversal event.
     */
    protected void fireEdgeTraversed(final EdgeTraversalEvent<V, E> e)
    {
        for (int i = 0; i < nListeners; i++) {
            final TraversalListener<V, E> l = traversalListeners.get(i);
            l.edgeTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was visited.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexTraversed(final VertexTraversalEvent<V> e)
    {
        for (int i = 0; i < nListeners; i++) {
            final TraversalListener<V, E> l = traversalListeners.get(i);
            l.vertexTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was finished.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexFinished(final VertexTraversalEvent<V> e)
    {
        for (int i = 0; i < nListeners; i++) {
            final TraversalListener<V, E> l = traversalListeners.get(i);
            l.vertexFinished(e);
        }
    }
}

// End AbstractGraphIterator.java
