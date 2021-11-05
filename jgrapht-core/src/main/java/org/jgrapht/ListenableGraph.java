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
package org.jgrapht;

import org.jgrapht.event.*;

/**
 * A graph that supports listeners on structural change events.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GraphListener
 * @see VertexSetListener
 *
 * @author Barak Naveh
 */
public interface ListenableGraph<V, E>
    extends
    Graph<V, E>
{
    /**
     * Adds the specified graph listener to this graph, if not already present.
     *
     * @param l the listener to be added.
     */
    public void addGraphListener(GraphListener<V, E> l);

    /**
     * Adds the specified vertex set listener to this graph, if not already present.
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
