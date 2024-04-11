/*
 * (C) Copyright 2003-2023, by Barak Naveh and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;

import java.io.*;
import java.util.*;

/**
 * An unmodifiable view of the backing graph specified in the constructor. This graph allows modules
 * to provide users with "read-only" access to internal graphs. Query operations on this graph "read
 * through" to the backing graph, and attempts to modify this graph result in an
 * {@link UnsupportedOperationException}.
 *
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through to the backing graph,
 * but relies on {@code Object}'s {@code equals} and {@code hashCode} methods. This
 * graph will be serializable if the backing graph is serializable.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 */
public class AsUnmodifiableGraph<V, E>
    extends GraphDelegator<V, E>
    implements Serializable
{
    private static final long serialVersionUID = -8186686968362705760L;

    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    /**
     * Creates a new unmodifiable graph based on the specified backing graph.
     *
     * @param g the backing graph on which an unmodifiable graph is to be created
     * 
     * @throws NullPointerException if argument is {@code null}
     */
    public AsUnmodifiableGraph(Graph<V, E> g)
    {
        super(g);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public V addVertex()
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeEdge(E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return super.getType().asUnmodifiable();
    }
}
