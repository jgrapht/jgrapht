/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.event.*;

import java.util.*;

/**
 * A graph iterator.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public interface GraphIterator<V, E>
    extends
    Iterator<V>
{
    /**
     * Test whether this iterator is set to traverse the graph across connected components.
     *
     * @return <code>true</code> if traverses across connected components, otherwise
     *         <code>false</code>.
     */
    boolean isCrossComponentTraversal();

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is set to
     * <code>true</code> this class will reuse previously fired events and will not create a new
     * object for each event. This option increases performance but should be used with care,
     * especially in multithreaded environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    boolean isReuseEvents();

    /**
     * Sets a value the <code>reuseEvents</code> flag. If the <code>
     * reuseEvents</code> flag is set to <code>true</code> this class will reuse previously fired
     * events and will not create a new object for each event. This option increases performance but
     * should be used with care, especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects instead of creating a new
     *        event object for each event.
     */
    void setReuseEvents(boolean reuseEvents);

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    void addTraversalListener(TraversalListener<V, E> l);

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    void removeTraversalListener(TraversalListener<V, E> l);

    /**
     * Unsupported.
     * 
     * @throws UnsupportedOperationException always since operation is not supported
     */
    @Override
    void remove();
}
