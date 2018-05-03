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
package org.jgrapht;

/**
 * An edge factory used by graphs for creating new edges.
 * 
 * <p>
 * A graph uses the edge factory to create new edge objects whenever a user calls method
 * {@link Graph#addEdge(Object, Object)}. Users can also create the edge in user code and then use
 * method {@link Graph#addEdge(Object, Object, Object)} to add the edge.
 *
 * <p>
 * Note that when used inside a {@link Graph} the edge factory must return unique objects on each
 * call.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @since Jul 14, 2003
 * @deprecated Use suppliers instead
 */
@Deprecated
public interface EdgeFactory<V, E>
{
    /**
     * Creates a new edge whose endpoints are the specified source and target vertices.
     *
     * @param sourceVertex the source vertex.
     * @param targetVertex the target vertex.
     *
     * @return a new edge whose endpoints are the specified source and target vertices.
     * @deprecated Use suppliers instead
     */
    @Deprecated
    E createEdge(V sourceVertex, V targetVertex);
}

// End EdgeFactory.java
