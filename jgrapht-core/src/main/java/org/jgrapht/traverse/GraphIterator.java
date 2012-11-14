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

import org.jgrapht.event.TraversalListener;

import java.util.Iterator;


/**
 * A graph iterator.
 *
 * @author Barak Naveh
 * @since Jul 31, 2003
 */
public interface GraphIterator<V, E>
    extends Iterator<V>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Test whether this iterator is set to traverse the grpah across connected
     * components.
     *
     * @return <code>true</code> if traverses across connected components,
     * otherwise <code>false</code>.
     */
    boolean isCrossComponentTraversal();

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
    void setReuseEvents(boolean reuseEvents);

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    boolean isReuseEvents();

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    void addTraversalListener(TraversalListener<V, E> l);

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    void remove();

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    void removeTraversalListener(TraversalListener<V, E> l);
}

// End GraphIterator.java
