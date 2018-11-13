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

/**
 * A listener on graph iterator or on a graph traverser.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public interface TraversalListener<V, E>
{
    /**
     * Called to inform listeners that the traversal of the current connected component has
     * finished.
     *
     * @param e the traversal event.
     */
    void connectedComponentFinished(ConnectedComponentTraversalEvent e);

    /**
     * Called to inform listeners that a traversal of a new connected component has started.
     *
     * @param e the traversal event.
     */
    void connectedComponentStarted(ConnectedComponentTraversalEvent e);

    /**
     * Called to inform the listener that the specified edge have been visited during the graph
     * traversal. Depending on the traversal algorithm, edge might be visited more than once.
     *
     * @param e the edge traversal event.
     */
    void edgeTraversed(EdgeTraversalEvent<E> e);

    /**
     * Called to inform the listener that the specified vertex have been visited during the graph
     * traversal. Depending on the traversal algorithm, vertex might be visited more than once.
     *
     * @param e the vertex traversal event.
     */
    void vertexTraversed(VertexTraversalEvent<V> e);

    /**
     * Called to inform the listener that the specified vertex have been finished during the graph
     * traversal. Exact meaning of "finish" is algorithm-dependent; e.g. for DFS, it means that all
     * vertices reachable via the vertex have been visited as well.
     *
     * @param e the vertex traversal event.
     */
    void vertexFinished(VertexTraversalEvent<V> e);
}
