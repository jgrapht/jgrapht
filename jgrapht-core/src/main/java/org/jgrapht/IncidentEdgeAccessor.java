/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

/**
 * A vertex incident edges accessor. Incident edge accessors are helpful in creating algorithms
 * which operate on both directed and undirected graphs.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface IncidentEdgeAccessor<V, E>
{
    /**
     * Get an {@link Iterable} over incident edges of a vertex.
     *
     * @param vertex the input vertex
     * @return an {@link Iterable} over incident edges of a vertex
     */
    Iterable<E> edgesOf(V vertex);
}
