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
/* ------------------
 * GraphIterator.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 31-Jul-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 04-May-2004 : Made generic (CH)
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.event.*;


/**
 * A graph iterator.
 *
 * @author Barak Naveh
 * @since Jul 31, 2003
 */
public interface GraphIterator<V, E>
    extends Iterator<V>
{
    

    /**
     * Test whether this iterator is set to traverse the grpah across connected
     * components.
     *
     * @return <code>true</code> if traverses across connected components,
     * otherwise <code>false</code>.
     */
    public boolean isCrossComponentTraversal();

    /**
     * Sets a value the <code>reuseEvents</code> flag. If the <code>
     * reuseEvents</code> flag is set to <code>true</code> this class will reuse
     * previously fired events and will not create a new object for each event.
     * This option increases performance but should be used with care,
     * especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects
     * instead of creating a new event object for each event.
     */
    public void setReuseEvents(boolean reuseEvents);

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    public boolean isReuseEvents();

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    public void addTraversalListener(TraversalListener<V, E> l);

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove();

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    public void removeTraversalListener(TraversalListener<V, E> l);
}

// End GraphIterator.java
