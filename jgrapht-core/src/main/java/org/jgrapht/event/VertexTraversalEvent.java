/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
 * A traversal event for a graph vertex.
 *
 * @param <V> the graph vertex type
 *
 * @author Barak Naveh
 */
public class VertexTraversalEvent<V>
    extends
    EventObject
{
    private static final long serialVersionUID = 3688790267213918768L;

    /**
     * The traversed vertex.
     */
    protected V vertex;

    /**
     * Creates a new VertexTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param vertex the traversed vertex.
     */
    public VertexTraversalEvent(Object eventSource, V vertex)
    {
        super(eventSource);
        this.vertex = vertex;
    }

    /**
     * Returns the traversed vertex.
     *
     * @return the traversed vertex.
     */
    public V getVertex()
    {
        return vertex;
    }
}
