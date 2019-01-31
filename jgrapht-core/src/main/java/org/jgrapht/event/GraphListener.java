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
 * A listener that is notified when the graph changes.
 *
 * <p>
 * If only notifications on vertex set changes are required it is more efficient to use the
 * VertexSetListener.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @see VertexSetListener
 */
public interface GraphListener<V, E>
    extends
    VertexSetListener<V>
{
    /**
     * Notifies that an edge has been added to the graph.
     *
     * @param e the edge event.
     */
    void edgeAdded(GraphEdgeChangeEvent<V, E> e);

    /**
     * Notifies that an edge has been removed from the graph.
     *
     * @param e the edge event.
     */
    void edgeRemoved(GraphEdgeChangeEvent<V, E> e);

    /**
     * Notifies that an edge weight has been updated.
     * 
     * @param e the edge event.
     */
    default void edgeWeightUpdated(GraphEdgeChangeEvent<V, E> e)
    {
    }

}
