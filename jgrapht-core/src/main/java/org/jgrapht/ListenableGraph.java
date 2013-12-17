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
/* --------------------
 * ListenableGraph.java
 * --------------------
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
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht;

import org.jgrapht.event.*;


/**
 * A graph that supports listeners on structural change events.
 *
 * @author Barak Naveh
 * @see GraphListener
 * @see VertexSetListener
 * @since Jul 20, 2003
 */
public interface ListenableGraph<V, E>
    extends Graph<V, E>
{
    

    /**
     * Adds the specified graph listener to this graph, if not already present.
     *
     * @param l the listener to be added.
     */
    public void addGraphListener(GraphListener<V, E> l);

    /**
     * Adds the specified vertex set listener to this graph, if not already
     * present.
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
