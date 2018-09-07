/*
 * (C) Copyright 2015-2018, by Barak Naveh and Contributors.
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
