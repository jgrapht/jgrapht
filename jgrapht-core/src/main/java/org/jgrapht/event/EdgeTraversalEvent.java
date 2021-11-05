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
package org.jgrapht.event;

import java.util.*;

/**
 * A traversal event for a graph edge.
 *
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class EdgeTraversalEvent<E>
    extends
    EventObject
{
    private static final long serialVersionUID = 4050768173789820979L;

    /**
     * The traversed edge.
     */
    protected E edge;

    /**
     * Creates a new EdgeTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param edge the traversed edge.
     */
    public EdgeTraversalEvent(Object eventSource, E edge)
    {
        super(eventSource);
        this.edge = edge;
    }

    /**
     * Returns the traversed edge.
     *
     * @return the traversed edge.
     */
    public E getEdge()
    {
        return edge;
    }
}
