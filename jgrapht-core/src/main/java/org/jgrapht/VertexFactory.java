/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
 * A vertex factory used by graph algorithms for creating new vertices. Normally, vertices are
 * constructed by user code and added to a graph explicitly, but algorithms which generate new
 * vertices require a factory.
 *
 * @param <V> the graph vertex type
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 * @deprecated Use suppliers instead
 */
@Deprecated
public interface VertexFactory<V>
{
    /**
     * Creates a new vertex.
     *
     * @return the new vertex
     */
    V createVertex();
}

// End VertexFactory.java
