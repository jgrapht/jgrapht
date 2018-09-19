/*
 * (C) Copyright 2015-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.graph.specifics;

import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;

/**
 * A container for vertex edges.
 *
 * <p>
 * In this edge container we use array lists to minimize memory toll. However, for high-degree
 * vertices we replace the entire edge container with a direct access subclass (to be implemented).
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class UndirectedEdgeContainer<V, E>
    implements
    Serializable
{
    private static final long serialVersionUID = -6623207588411170010L;
    Set<E> vertexEdges;
    private transient Set<E> unmodifiableVertexEdges = null;

    UndirectedEdgeContainer(EdgeSetFactory<V, E> edgeSetFactory, V vertex)
    {
        vertexEdges = edgeSetFactory.createEdgeSet(vertex);
    }

    /**
     * A lazy build of unmodifiable list of vertex edges
     *
     * @return an unmodifiable set of vertex edges
     */
    public Set<E> getUnmodifiableVertexEdges()
    {
        if (unmodifiableVertexEdges == null) {
            unmodifiableVertexEdges = Collections.unmodifiableSet(vertexEdges);
        }
        return unmodifiableVertexEdges;
    }

    /**
     * Add a vertex edge
     *
     * @param e the edge to add
     */
    public void addEdge(E e)
    {
        vertexEdges.add(e);
    }

    /**
     * Get number of vertex edges
     *
     * @return the number of vertex edges
     */
    public int edgeCount()
    {
        return vertexEdges.size();
    }

    /**
     * Remove a vertex edge
     *
     * @param e the edge to remove
     */
    public void removeEdge(E e)
    {
        vertexEdges.remove(e);
    }
}
