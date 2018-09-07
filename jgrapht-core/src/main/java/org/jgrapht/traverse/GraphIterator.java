/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
 * @since Jul 31, 2003
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

// End GraphIterator.java
