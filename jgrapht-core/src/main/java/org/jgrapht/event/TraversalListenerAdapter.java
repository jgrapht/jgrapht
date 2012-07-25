/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
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
    //~ Methods ----------------------------------------------------------------

    /**
     * @see TraversalListener#connectedComponentFinished(ConnectedComponentTraversalEvent)
     */
    @Override
    public void connectedComponentFinished(
        ConnectedComponentTraversalEvent e)
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
    public void edgeTraversed(EdgeTraversalEvent<V, E> e)
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
