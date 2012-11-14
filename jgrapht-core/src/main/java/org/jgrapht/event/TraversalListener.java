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
/* ----------------------
 * TraversalListener.java
 * ----------------------
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
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * A listener on graph iterator or on a graph traverser.
 *
 * @author Barak Naveh
 * @since Jul 19, 2003
 */
public interface TraversalListener<V, E>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Called to inform listeners that the traversal of the current connected
     * component has finished.
     *
     * @param e the traversal event.
     */
    void connectedComponentFinished(
        ConnectedComponentTraversalEvent e);

    /**
     * Called to inform listeners that a traversal of a new connected component
     * has started.
     *
     * @param e the traversal event.
     */
    void connectedComponentStarted(ConnectedComponentTraversalEvent e);

    /**
     * Called to inform the listener that the specified edge have been visited
     * during the graph traversal. Depending on the traversal algorithm, edge
     * might be visited more than once.
     *
     * @param e the edge traversal event.
     */
    void edgeTraversed(EdgeTraversalEvent<V, E> e);

    /**
     * Called to inform the listener that the specified vertex have been visited
     * during the graph traversal. Depending on the traversal algorithm, vertex
     * might be visited more than once.
     *
     * @param e the vertex traversal event.
     */
    void vertexTraversed(VertexTraversalEvent<V> e);

    /**
     * Called to inform the listener that the specified vertex have been
     * finished during the graph traversal. Exact meaning of "finish" is
     * algorithm-dependent; e.g. for DFS, it means that all vertices reachable
     * via the vertex have been visited as well.
     *
     * @param e the vertex traversal event.
     */
    void vertexFinished(VertexTraversalEvent<V> e);
}

// End TraversalListener.java
